package fi.vm.sade.sijoittelu.laskenta.external.resource;

import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.domain.VastaanotettavuusDTO;
import fi.vm.sade.sijoittelu.domain.dto.VastaanottoDTO;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.VastaanottoEventDto;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/virkailija")
public interface VirkailijaValintaTulosServiceResource {

    @GET
    @Path("/henkilo/{hakijaOid}/hakemus/{hakemusOid}/hakukohde/{hakukohdeOid}/vastaanotettavuus")
    @Produces(MediaType.APPLICATION_JSON)
    VastaanotettavuusDTO vastaanotettavuus(@PathParam("hakijaOid") String hakijaOid, @PathParam("hakemusOid") String hakemusOid, @PathParam("hakukohdeOid") String hakukohdeOid);


    @GET
    @Path("/valintatulos/haku/{hakuOid}")
    @Produces(MediaType.APPLICATION_JSON)
    List<Valintatulos> valintatuloksetValinnantilalla(@PathParam("hakuOid") String hakuOid);

    @GET
    @Path("/vastaanotot/haku/{hakuOid}")
    @Produces(MediaType.APPLICATION_JSON)
    List<VastaanottoDTO> haunKoulutuksenAlkamiskaudenVastaanototYhdenPaikanSaadoksenPiirissa(@PathParam("hakuOid") String hakuOid);

    @POST
    @Path("/transactional-vastaanotto")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    void valintatuloksetValinnantilalla(List<VastaanottoEventDto> valintatuloses);
}
