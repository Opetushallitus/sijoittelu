package fi.vm.sade.sijoittelu.esb.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

/**
 * @author Eetu Blomqvist
 */
@Component
public class SijoitteluAlgoritmiRouteBuilder extends RouteBuilder {

    public static final String CXF_ENDPOINT = "cxf:bean:sijoitteluServiceRouter?dataFormat=PAYLOAD";
    public static final String SEDA_SIJOITTELUJONO = "seda:sijoittelujono";
    public static final String BEAN_SIJOITTELU_LAUNCHER = "bean:sijoitteluLauncher";
    public static final String BEAN_CALLBACK = "bean:callback";

    @Override
    public void configure() throws Exception {

        // route from webservice which consumes from CXF endpoint to a queue
        from(CXF_ENDPOINT).to(SEDA_SIJOITTELUJONO + "?waitForTaskToComplete=Never");

        // This route consumes 1 sijoittelu request at a time from the queue, passes it to the algorithm and then callback.
        from(SEDA_SIJOITTELUJONO + "?concurrentConsumers=1").to(BEAN_SIJOITTELU_LAUNCHER, BEAN_CALLBACK);

//        getContext().setTracing(true);
    }
}
