package fi.vm.sade.sijoittelu.laskenta.external.resource;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/haku")
public interface ValintaTulosServiceResource {
    @GET
    @Path("/{hakuOid}/hakemus/{hakemusOid}/hakukohde/{hakukohdeOid}/vastaanotettavuus")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String vastaanotettavuus(@PathParam("hakuOid") String hakuOid, @PathParam("hakemusOid") String hakemusOid, @PathParam("hakukohdeOid") String hakukohdeOid);
}
