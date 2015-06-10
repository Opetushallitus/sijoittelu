package fi.vm.sade.sijoittelu.laskenta.external.resource;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/v1/rest/parametri")
public interface OhjausparametriResource {

    @GET
    @Path("/{oid}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    String haePaivamaara(@PathParam("oid") String oid);
}
