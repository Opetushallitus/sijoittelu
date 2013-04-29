package fi.vm.sade.sijoittelu.resource;

import fi.vm.sade.sijoittelu.dao.DAO;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * User: wuoti
 * Date: 26.4.2013
 * Time: 12.41
 */
@Path("/sijoittelu")
@Component
public class SijoitteluResource {

    @Autowired
    private DAO dao;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{hakuOid}")
    public Sijoittelu getSijoitteluByHakuoid(@PathParam("hakuOid") String hakuOid) {
        Sijoittelu sijoittelu = dao.getSijoitteluByHakuOid(hakuOid);
        if (sijoittelu == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        return sijoittelu;
    }
}
