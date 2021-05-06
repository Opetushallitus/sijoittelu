package fi.vm.sade.sijoittelu.cas;

import fi.vm.sade.javautils.nio.cas.ApplicationSession;
import fi.vm.sade.javautils.nio.cas.CasConfig;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static fi.vm.sade.valinta.sharedutils.http.HttpResource.CSRF_VALUE;

@Configuration
public class CasInterceptors {
    private static final String CALLER_ID = "1.2.246.562.10.00000000001.sijoittelu-service";
    private static final Logger LOG = LoggerFactory.getLogger(CasInterceptors.class);

    @Bean(name = "SijoitteluCasInterceptor")
    @Autowired
    public AbstractPhaseInterceptor<Message> getSijoitteluCasInterceptor(
            @Value("${cas.service.valintaperusteet}") String targetService,
            @Value("${cas.session.valintaperusteet}") String sessionUrl,
            @Value("${sijoittelu-service.username.to.valintaperusteet}")
                    String appClientUsername,
            @Value("${sijoittelu-service.password.to.valintaperusteet}")
                    String appClientPassword,
            @Value("${cas.tickets.url}")
                    String ticketUrl) {
        return getCasInterceptor(
                targetService, appClientUsername, appClientPassword, ticketUrl, sessionUrl);
    }

    private AbstractPhaseInterceptor<Message> getCasInterceptor(
            String service,
            String username,
            String password,
            String ticketsUrl,
            String sessionUrl) {
        LOG.info("Creating casInterceptor for service {} with username {} and ticketsUrl {}", service, username, ticketsUrl);
        return new SijoitteluCasInterceptor(
                new ApplicationSession(new CasConfig(
                        username,
                        password,
                        ticketsUrl,
                        service,
                        CSRF_VALUE,
                        CALLER_ID,
                        "JSESSIONID",
                        "/j_spring_cas_security_check",
                        sessionUrl))
        );
    }
}
