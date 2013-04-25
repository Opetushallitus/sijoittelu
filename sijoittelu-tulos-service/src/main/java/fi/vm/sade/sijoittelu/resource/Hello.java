package fi.vm.sade.sijoittelu.resource;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.annotate.JsonView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;



/**
 *
 */
@Path("/")
@Component
public class Hello {

    protected static final Logger LOGGER = LoggerFactory.getLogger(Hello.class);


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("hello")
    public String hakukohde() {
        return "hello there";
    }
}
