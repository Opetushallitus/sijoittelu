package fi.vm.sade.sijoittelu.laskenta.resource;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import java.io.IOException;

/**
 * @author Jussi Jartamo
 */
public class SijoitteluResourceConfig extends ResourceConfig {
    public SijoitteluResourceConfig() {
        register(RequestContextFilter.class);
        // json output and input
        /**
         * CORS Filter
         */
        register(new ContainerResponseFilter() {

            @Override
            public void filter(ContainerRequestContext requestContext,
                               ContainerResponseContext responseContext)
                    throws IOException {
                responseContext.getHeaders().add("Access-Control-Allow-Origin",
                        "*");
            }
        });

        packages("fi.vm.sade.sijoittelu");

        register(com.fasterxml.jackson.jaxrs.annotation.JacksonFeatures.class);

        registerInstances(
                new com.wordnik.swagger.jaxrs.listing.ResourceListingProvider(),
                new com.wordnik.swagger.jaxrs.listing.ApiDeclarationProvider());
        register(com.wordnik.swagger.jaxrs.listing.ApiListingResourceJSON.class);
    }
}
