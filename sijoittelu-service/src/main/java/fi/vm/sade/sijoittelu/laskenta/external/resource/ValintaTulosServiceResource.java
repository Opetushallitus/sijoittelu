package fi.vm.sade.sijoittelu.laskenta.external.resource;

import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.VastaanottoDTO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/haku")
public interface ValintaTulosServiceResource {
    @POST
    @Path("/{hakuOid}/hakemus/{hakemusOid}/vastaanota")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String vastaanota(@PathParam("hakuOid") String hakuOid, @PathParam("hakemusOid") String hakemusOid, VastaanottoDTO vastaanotto);
}
