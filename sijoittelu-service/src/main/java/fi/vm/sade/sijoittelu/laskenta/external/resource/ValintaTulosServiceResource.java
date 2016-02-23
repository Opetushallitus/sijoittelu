package fi.vm.sade.sijoittelu.laskenta.external.resource;

import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.PoistaVastaanottoDTO;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.VastaanotettavuusDTO;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.VastaanottoRecordDTO;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.stream.Stream;

@Path("/virkailija/vastaanotto")
public interface ValintaTulosServiceResource {

    @GET
    @Path("/henkilo/{hakijaOid}/hakemus/{hakemusOid}/hakukohde/{hakukohdeOid}/vastaanotettavuus")
    @Produces(MediaType.APPLICATION_JSON)
    VastaanotettavuusDTO vastaanotettavuus(@PathParam("hakijaOid") String hakijaOid, @PathParam("hakemusOid") String hakemusOid, @PathParam("hakukohdeOid") String hakukohdeOid);

    @GET
    @Path("/hakukohde/:hakukohdeOid")
    @Produces(MediaType.APPLICATION_JSON)
    List<VastaanottoRecordDTO> hakukohteenVastaanotot(String hakukohdeOid);

    @POST
    @Path("/poista")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void poista(@BeanParam PoistaVastaanottoDTO poistaVastaanottoDTO);

    @POST
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    void tallenna(@BeanParam List<VastaanottoRecordDTO> tallennettavat);

}
