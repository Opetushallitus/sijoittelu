package fi.vm.sade.sijoittelu.esb.bean;

import org.apache.camel.Exchange;
import org.apache.cxf.common.util.Base64Utility;
import org.apache.cxf.jaxrs.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Eetu Blomqvist
 */
@Component("callback")
public class CallbackBean {

    @Autowired
    private WebClient client;

    @Value("${activiti.user}")
    private String user;

    @Value("${activiti.password}")
    private String passwd;

    private static final Logger LOGGER = LoggerFactory.getLogger(CallbackBean.class);

    public void callback(Exchange exchange) {

        LOGGER.info("Callback for sijoittelu {}", exchange.getIn().getHeader("SijoitteluId"));
        LOGGER.info("Was failure? {}", exchange.getOut().isFault());

        String status = exchange.getOut().isFault() ? "FAILED" : "OK";
        CallbackData data = new CallbackData();
        data.setStatus(status);

        client.reset();

        String authorizationHeader = "Basic " + Base64Utility.encode((user + ":" + passwd).getBytes());
        client.header("Authorization", authorizationHeader);

        Response response = client.path("process-instance")
                .path(exchange.getIn().getHeader("SijoitteluId"))
                .path("event")
                .path("callbackSignal")
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.WILDCARD_TYPE).post(data);

        if (response.getStatus() != Response.Status.OK.getStatusCode()) {
            // TODO handle errors
            LOGGER.error("Callback failed! status: {}", response.getStatus());
        }
    }
}
