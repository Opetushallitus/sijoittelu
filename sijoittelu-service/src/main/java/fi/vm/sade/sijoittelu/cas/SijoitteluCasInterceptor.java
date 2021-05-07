package fi.vm.sade.sijoittelu.cas;

import fi.vm.sade.javautils.cxf.OphCxfMessageUtil;
import fi.vm.sade.javautils.nio.cas.ApplicationSession;
import fi.vm.sade.javautils.nio.cas.CasSession;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class SijoitteluCasInterceptor extends AbstractPhaseInterceptor<Message> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SijoitteluCasInterceptor.class);

    private static final String CSRF_VALUE = "CSRF";
    private static final String EXCHANGE_SESSION_TOKEN = "SessionToken";

    private final ApplicationSession applicationSession;

    public SijoitteluCasInterceptor(ApplicationSession applicationSession) {
        super(Phase.PRE_PROTOCOL);
        this.applicationSession = applicationSession;
    }

    @Override
    public void handleMessage(Message message) throws Fault {
        try {
            if ((Boolean) message.get(Message.INBOUND_MESSAGE)) {
                handleInboundMessage(message);
            } else {
                handleOutboundMessage(message);
            }
        } catch (Exception e) {
            LOGGER.error("handleMessage throws", e);
        }
    }

    private void handleInboundMessage(Message message) {
        if (isUnauthorized(message) || isRedirectToCas(message)) {
            //SessionToken session = (SessionToken) message.getExchange().get(EXCHANGE_SESSION_TOKEN);
            LOGGER.info("Getting session from CAS, inbound message.");
            CasSession session = this.applicationSession.getSessionBlocking();
            LOGGER.info(String.format("Authentication failed using session %s", session));
            OphCxfMessageUtil.appendToHeader(
                    message, "Cookie", session.getSessionCookieName() + "=" + session.getSessionCookie(), ";");
            //this.applicationSession.invalidateSession(session);
            //OphCxfMessageUtil.addHeader(
            //        message, CAS_302_REDIRECT_MARKER.getKey(), CAS_302_REDIRECT_MARKER.getValue());
        } else {
            try {
                LOGGER.info("Inbound message ok, headers: {}", message.get(Message.PROTOCOL_HEADERS));
            } catch (Exception e) {
                LOGGER.warn("Something went wrong: ", e);
            }
        }
    }

    private void handleOutboundMessage(Message message)
            throws ExecutionException, InterruptedException, TimeoutException {
        LOGGER.info("Getting session from CAS, outbound message.");
        CasSession session = this.applicationSession.getSessionBlocking();
        message.getExchange().put(EXCHANGE_SESSION_TOKEN, session);
        LOGGER.info(String.format("Using session %s", session.getSessionCookie()));
        OphCxfMessageUtil.addHeader(message, "CSRF", CSRF_VALUE);
        OphCxfMessageUtil.appendToHeader(message, "Cookie", "CSRF=" + CSRF_VALUE, ";");
        OphCxfMessageUtil.appendToHeader(
                message, "Cookie", session.getSessionCookieName() + "=" + session.getSessionCookie(), ";");
    }

    private boolean isRedirectToCas(Message message) {
        for (String location : OphCxfMessageUtil.getHeader(message, "Location")) {
            try {
                if (new URL(location).getPath().startsWith("/cas/login")) {
                    return true;
                }
            } catch (MalformedURLException ignored) {
            }
        }
        return false;
    }

    private boolean isUnauthorized(Message message) {
        return ((Integer) 401).equals(message.get(Message.RESPONSE_CODE));
    }
}
