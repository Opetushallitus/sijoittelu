package fi.vm.sade.sijoittelu.laskenta.external.resource;

import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.VastaanotettavuusDTO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/virkailija/vastaanotto")
public interface ValintaTulosServiceResource {
    @GET
    @Path("/henkilo/{hakijaOid}/hakemus/{hakemusOid}/hakukohde/{hakukohdeOid}/vastaanotettavuus")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public VastaanotettavuusDTO vastaanotettavuus(@PathParam("hakijaOid") String hakijaOid, @PathParam("hakemusOid") String hakemusOid, @PathParam("hakukohdeOid") String hakukohdeOid);
}
