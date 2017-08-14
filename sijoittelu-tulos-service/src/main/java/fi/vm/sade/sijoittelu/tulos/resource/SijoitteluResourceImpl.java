package fi.vm.sade.sijoittelu.tulos.resource;

import fi.vm.sade.authentication.business.service.Authorizer;
import static fi.vm.sade.sijoittelu.tulos.roles.SijoitteluRole.READ_UPDATE_CRUD;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.Arrays;
import java.util.Optional;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.tulos.dao.HakukohdeDao;
import fi.vm.sade.sijoittelu.tulos.roles.SijoitteluRole;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

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
@Api(value = "sijoittelu", description = "Resurssi sijoittelun tuloksien hakemiseen")
@Deprecated
public class SijoitteluResourceImpl implements SijoitteluResource {
    private final static Logger LOGGER = LoggerFactory.getLogger(SijoitteluResourceImpl.class);

    @Autowired
    private SijoitteluTulosService sijoitteluTulosService;

    @Autowired
    private RaportointiService raportointiService;

    @Autowired
    private HakukohdeDao hakukohdeDao;

    @Autowired
    private Authorizer authorizer;

    @Value("${root.organisaatio.oid}")
    private String OPH_tarjoajaOid;

    @PreAuthorize(READ_UPDATE_CRUD)
    @GET
    @Path("/{hakuOid}/sijoitteluajo/{sijoitteluajoId}/hakukohde/{hakukohdeOid}")
    @Produces(APPLICATION_JSON)
    @ApiOperation(position = 3, value = "Hakee hakukohteen tiedot tietyssa sijoitteluajossa.", response = HakukohdeDTO.class)
    public Response getHakukohdeBySijoitteluajo(
            @ApiParam(name = "hakuOid", value = "Haun tunniste", required = true) @PathParam("hakuOid") String hakuOid,
            @ApiParam(name = "sijoitteluajoId", value = "Sijoitteluajon tunniste tai 'latest' avainsana", required = true) @PathParam("sijoitteluajoId") String sijoitteluajoId,
            @ApiParam(name = "hakukohdeOid", value = "Hakukohteen tunniste", required = true) @PathParam("hakukohdeOid") String hakukohdeOid) {
        try {
            tarkistaOikeudetHakukohteeseen(hakukohdeOid);
        } catch(Exception e){
            return Response.noContent().build();
        }
        Optional<SijoitteluAjo> sijoitteluAjo = getSijoitteluAjo(sijoitteluajoId, hakuOid, Optional.empty());
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
        try {
            tarkistaOikeudetHakukohteeseen(hakukohdeOid);
        } catch(Exception e){
            return new HakukohdeDTO();
        }
        Optional<SijoitteluAjo> ajo = getSijoitteluAjo(sijoitteluajoId, hakuOid, Optional.of(hakukohdeOid));
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
            tarkistaOikeudetHakukohteeseen(hakukohdeOid);

            Optional<SijoitteluAjo> sijoitteluAjo = raportointiService.cachedLatestSijoitteluAjoForHakukohde(hakuOid, hakukohdeOid);

            return sijoitteluAjo.map(ajo -> raportointiService.cachedHakemukset(ajo, true, null, null, Arrays.asList(hakukohdeOid), null, null)).orElseGet(HakijaPaginationObject::new);
        } catch (Exception e) {
            LOGGER.error("Sijoittelun hakemuksia ei saatu hakukohteelle {}!", hakukohdeOid, e);
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

        try {
            HakijaDTO hakijaDTO = raportointiService.hakemus(hakuOid, sijoitteluajoId, hakemusOid);
            if (hakijaDTO == null) {
                LOGGER.warn(String.format("Got null hakijaDTO for hakuOid/sijoitteluajoId/hakemusOid %s/%s/%s", hakuOid, sijoitteluajoId, hakemusOid));
            }
            return hakijaDTO;
        } catch (Exception e) {
            LOGGER.error(String.format("Exception when retrieving /%s/sijoitteluajo/%s/hakemus/%s", hakuOid, sijoitteluajoId, hakemusOid), e);
            throw new RuntimeException(e);
        }
    }

    private Optional<SijoitteluAjo> getSijoitteluAjo(String sijoitteluajoId, String hakuOid, Optional<String> hakukohdeOidOpt) {
        if (LATEST.equals(sijoitteluajoId)) {
            if (hakukohdeOidOpt.isPresent()) {
                return raportointiService.cachedLatestSijoitteluAjoForHakukohde(hakuOid, hakukohdeOidOpt.get());
            }
            return raportointiService.cachedLatestSijoitteluAjoForHaku(hakuOid);
        } else {
            return raportointiService.getSijoitteluAjo(Long.parseLong(sijoitteluajoId));
        }
    }

    private void tarkistaOikeudetHakukohteeseen(String hakukohdeOid) throws Exception {
        if (hakukohdeOid != null) {
            Hakukohde hakukohde = hakukohdeDao.findTarjoajaOidByHakukohdeOid(hakukohdeOid);
            if (hakukohde != null) {
                String tarjoajaOid = hakukohde.getTarjoajaOid();
                authorizer.checkOrganisationAccess(tarjoajaOid, SijoitteluRole.READ_ROLE, SijoitteluRole.CRUD_ROLE, SijoitteluRole.UPDATE_ROLE);
            }
        }
    }

    private void tarkistaPaakayttajaoikeudet() throws Exception {
        authorizer.checkOrganisationAccess(OPH_tarjoajaOid, SijoitteluRole.READ_ROLE, SijoitteluRole.CRUD_ROLE, SijoitteluRole.UPDATE_ROLE);
    }

}
