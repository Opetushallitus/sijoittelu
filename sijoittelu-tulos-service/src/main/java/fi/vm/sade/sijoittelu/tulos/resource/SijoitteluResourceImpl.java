package fi.vm.sade.sijoittelu.tulos.resource;

import static fi.vm.sade.sijoittelu.tulos.roles.SijoitteluRole.READ_UPDATE_CRUD;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.dto.SijoitteluDTO;
import fi.vm.sade.sijoittelu.tulos.dto.SijoitteluajoDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaPaginationObject;
import fi.vm.sade.sijoittelu.tulos.service.RaportointiService;
import fi.vm.sade.sijoittelu.tulos.service.SijoitteluTulosService;

@Path("sijoittelu")
@Controller
@PreAuthorize("isAuthenticated()")
@Api(value = "/sijoittelu", description = "Resurssi sijoittelun tuloksien hakemiseen")
public class SijoitteluResourceImpl implements SijoitteluResource {
    private final static Logger LOGGER = LoggerFactory.getLogger(SijoitteluResourceImpl.class);

    @Autowired
    private SijoitteluTulosService sijoitteluTulosService;

    @Autowired
    private RaportointiService raportointiService;

    @GET
    @Path("/{hakuOid}")
    @Produces(APPLICATION_JSON)
    @PreAuthorize(READ_UPDATE_CRUD)
    @ApiOperation(position = 1, value = "Hakee sijoittelun tiedot haulle. Paa-asiallinen kaytto sijoitteluajojen tunnisteiden hakuun.", response = SijoitteluDTO.class)
    public SijoitteluDTO getSijoitteluByHakuOid(
            @ApiParam(name = "hakuOid", value = "Haun yksilollinen tunniste", required = true) @PathParam("hakuOid") String hakuOid) {
        return sijoitteluTulosService.getSijoitteluByHakuOid(hakuOid);
    }

    @GET
    @Path("/{hakuOid}/sijoitteluajo/{sijoitteluajoId}")
    @Produces(APPLICATION_JSON)
    @PreAuthorize(READ_UPDATE_CRUD)
    @ApiOperation(position = 2, value = "Hakee sijoitteluajon tiedot. Paasiallinen kaytto sijoitteluun osallistuvien hakukohteiden hakemiseen", response = SijoitteluajoDTO.class)
    public SijoitteluajoDTO getSijoitteluajo(
            @ApiParam(name = "hakuOid", value = "Haun yksilollinen tunniste", required = true) @PathParam("hakuOid") String hakuOid,
            @ApiParam(value = "Sijoitteluajon tunniste tai 'latest' avainsana", required = true) @PathParam("sijoitteluajoId") String sijoitteluajoId) {
        Optional<SijoitteluAjo> ajo = getSijoitteluAjo(sijoitteluajoId, hakuOid);
        return ajo.map(sijoitteluTulosService::getSijoitteluajo).orElse(new SijoitteluajoDTO());
    }

    @PreAuthorize(READ_UPDATE_CRUD)
    @GET
    @Path("/{hakuOid}/sijoitteluajo/{sijoitteluajoId}/hakukohde/{hakukohdeOid}")
    @Produces(APPLICATION_JSON)
    @ApiOperation(position = 3, value = "Hakee hakukohteen tiedot tietyssa sijoitteluajossa.", response = HakukohdeDTO.class)
    public Response getHakukohdeBySijoitteluajo(
            @ApiParam(name = "hakuOid", value = "Haun tunniste", required = true) @PathParam("hakuOid") String hakuOid,
            @ApiParam(name = "sijoitteluajoId", value = "Sijoitteluajon tunniste tai 'latest' avainsana", required = true) @PathParam("sijoitteluajoId") String sijoitteluajoId,
            @ApiParam(name = "hakukohdeOid", value = "Hakukohteen tunniste", required = true) @PathParam("hakukohdeOid") String hakukohdeOid) {
        Optional<SijoitteluAjo> sijoitteluAjo = getSijoitteluAjo(sijoitteluajoId, hakuOid);
        return sijoitteluAjo.map(ajo -> {
            HakukohdeDTO hakukohdeBySijoitteluajo = sijoitteluTulosService.getHakukohdeBySijoitteluajo(ajo, hakukohdeOid);
            return Response.ok().entity(Optional.ofNullable(hakukohdeBySijoitteluajo).orElse(new HakukohdeDTO())).build();
        }).orElse(Response.ok().entity(new HakukohdeDTO()).build());

    }

    @GET
    @Produces(APPLICATION_JSON)
    @Path("/{hakuOid}/sijoitteluajo/{sijoitteluajoId}/hakukohdedto/{hakukohdeOid}")
    @PreAuthorize(READ_UPDATE_CRUD)
    @ApiOperation(position = 3, value = "Hakee hakukohteen tiedot tietyssa sijoitteluajossa.", response = HakukohdeDTO.class)
    public HakukohdeDTO getHakukohdeBySijoitteluajoPlainDTO(
            @ApiParam(value = "Haun tunniste", required = true) @PathParam("hakuOid") String hakuOid,
            @ApiParam(value = "Sijoitteluajon tunniste tai 'latest' avainsana", required = true) @PathParam("sijoitteluajoId") String sijoitteluajoId,
            @ApiParam(value = "Hakukohteen tunniste", required = true) @PathParam("hakukohdeOid") String hakukohdeOid) {
        Optional<SijoitteluAjo> ajo = getSijoitteluAjo(sijoitteluajoId, hakuOid);
        return ajo.map(a -> sijoitteluTulosService.getHakukohdeBySijoitteluajo(a, hakukohdeOid)).orElse(new HakukohdeDTO());
    }

    @Override
    @PreAuthorize(READ_UPDATE_CRUD)
    @GET
    @Path("/{hakuOid}/hyvaksytyt/hakukohde/{hakukohdeOid}")
    @Produces(APPLICATION_JSON)
    @ApiOperation(position = 4, value = "Sivutettu listaus hakemuksien/hakijoiden listaukseen. Yksityiskohtainen listaus kaikista hakutoiveista ja niiden valintatapajonoista", response = HakijaPaginationObject.class)
    public HakijaPaginationObject hyvaksytytHakukohteeseen(
            @ApiParam(value = "Haun tunniste", required = true) @PathParam("hakuOid") String hakuOid,
            @ApiParam(value = "Hakukohteen tunniste", required = true) @PathParam("hakukohdeOid") String hakukohdeOid) {
        try {
            Optional<SijoitteluAjo> sijoitteluAjo = raportointiService.latestSijoitteluAjoForHaku(hakuOid);

            return sijoitteluAjo.map(ajo -> raportointiService.cahetetutHakemukset(ajo, true,
                            null, null, Arrays.asList(hakukohdeOid), null,
                            null)).orElseGet(() -> new HakijaPaginationObject());
        } catch (Exception e) {
            LOGGER.error("Sijoittelun hakemuksia ei saatu hakukohteelle {}! {}", hakukohdeOid, e.getMessage(), Arrays.toString(e.getStackTrace()));
            return new HakijaPaginationObject();
        }
    }

    @Override
    @PreAuthorize(READ_UPDATE_CRUD)
    @GET
    @Path("/{hakuOid}/sijoitteluajo/{sijoitteluajoId}/hakemukset")
    @Produces(APPLICATION_JSON)
    @ApiOperation(position = 4, value = "Sivutettu listaus hakemuksien/hakijoiden listaukseen. Yksityiskohtainen listaus kaikista hakutoiveista ja niiden valintatapajonoista", response = HakijaPaginationObject.class)
    public HakijaPaginationObject hakemukset(
            @ApiParam(value = "Haun tunniste", required = true) @PathParam("hakuOid") String hakuOid,
            @ApiParam(value = "Sijoitteluajon tunniste tai 'latest' avainsana", required = true) @PathParam("sijoitteluajoId") String sijoitteluajoId,
            @ApiParam(value = "Listaa jossakin kohteessa hyvaksytyt", required = false) @QueryParam("hyvaksytyt") Boolean hyvaksytyt,
            @ApiParam(value = "Listaa henkilot jotka ovat taysin ilman hyvaksyntaa (missaan kohteessa) ", required = false) @QueryParam("ilmanHyvaksyntaa") Boolean ilmanHyvaksyntaa,
            @ApiParam(value = "Listaa henkilot jotka ovat ottaneet paikan lasna tai poissaolevana vastaan", required = false) @QueryParam("vastaanottaneet") Boolean vastaanottaneet,
            @ApiParam(value = "Rajoita hakua niin etta naytetaan hakijat jotka ovat jollain toiveella hakeneet naihin kohteisiin", required = false) @QueryParam("hakukohdeOid") List<String> hakukohdeOid,
            @ApiParam(value = "Nayta n kappaletta tuloksia. Kayta sivutuksessa", required = false) @QueryParam("count") Integer count,
            @ApiParam(value = "Aloita nayttaminen kohdasta n. Kayta sivutuksessa.", required = false) @QueryParam("index") Integer index) {
        try {
            Optional<SijoitteluAjo> sijoitteluAjo = getSijoitteluAjo(sijoitteluajoId, hakuOid);
            return sijoitteluAjo.map(ajo ->
                            raportointiService.hakemukset(ajo, hyvaksytyt,
                                    ilmanHyvaksyntaa, vastaanottaneet, hakukohdeOid, count,
                                    index)
            ).orElseGet(() -> new HakijaPaginationObject());

        } catch (Exception e) {
            LOGGER.error("Sijoittelun hakemuksia ei saatu haulle {}! {}", hakuOid, e.getMessage(), Arrays.toString(e.getStackTrace()));
            return new HakijaPaginationObject();
        }
    }

    @PreAuthorize(READ_UPDATE_CRUD)
    @GET
    @Path("/{hakuOid}/sijoitteluajo/{sijoitteluajoId}/hakemus/{hakemusOid}")
    @Produces(APPLICATION_JSON)
    @ApiOperation(position = 5, value = "Nayttaa yksittaisen hakemuksen kaikki hakutoiveet ja tiedot kaikista valintatapajonoista", response = HakijaDTO.class)
    public HakijaDTO hakemus(
            @ApiParam(value = "Haun tunniste", required = true) @PathParam("hakuOid") String hakuOid,
            @ApiParam(value = "Sijoitteluajon tunniste tai 'latest' avainsana", required = true) @PathParam("sijoitteluajoId") String sijoitteluajoId,
            @ApiParam(value = "Hakemuksen tunniste", required = true) @PathParam("hakemusOid") String hakemusOid) {

        Optional<SijoitteluAjo> ajo = getSijoitteluAjo(sijoitteluajoId, hakuOid);
        return ajo.map(a -> raportointiService.hakemus(a, hakemusOid)).orElse(new HakijaDTO());
    }

    private Optional<SijoitteluAjo> getSijoitteluAjo(String sijoitteluajoId, String hakuOid) {
        if (LATEST.equals(sijoitteluajoId)) {
            return raportointiService.latestSijoitteluAjoForHaku(hakuOid);
        } else {
            return raportointiService.getSijoitteluAjo(Long.parseLong(sijoitteluajoId));
        }
    }
}
