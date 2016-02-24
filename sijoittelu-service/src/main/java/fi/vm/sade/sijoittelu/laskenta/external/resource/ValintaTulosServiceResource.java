package fi.vm.sade.sijoittelu.laskenta.external.resource;

import fi.vm.sade.sijoittelu.domain.VastaanotettavuusDTO;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/virkailija/vastaanotto")
public interface ValintaTulosServiceResource {

    @GET
    @Path("/henkilo/{hakijaOid}/hakemus/{hakemusOid}/hakukohde/{hakukohdeOid}/vastaanotettavuus")
    @Produces(MediaType.APPLICATION_JSON)
    VastaanotettavuusDTO vastaanotettavuus(@PathParam("hakijaOid") String hakijaOid, @PathParam("hakemusOid") String hakemusOid, @PathParam("hakukohdeOid") String hakukohdeOid);

}
