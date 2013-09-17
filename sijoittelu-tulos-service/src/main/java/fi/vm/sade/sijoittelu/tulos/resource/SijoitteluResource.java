package fi.vm.sade.sijoittelu.tulos.resource;

import fi.vm.sade.sijoittelu.domain.JsonViews;
import fi.vm.sade.sijoittelu.domain.dto.*;
import fi.vm.sade.sijoittelu.tulos.service.RaportointiService;
import fi.vm.sade.sijoittelu.tulos.service.SijoitteluTulosService;
import org.codehaus.jackson.map.annotate.JsonView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

import static fi.vm.sade.sijoittelu.tulos.roles.SijoitteluRole.*;

/**
 * User: wuoti Date: 26.4.2013 Time: 12.41
 */
@Path("sijoittelu")
@Component
@PreAuthorize("isAuthenticated()")
public class SijoitteluResource {

    private final static Logger LOGGER = LoggerFactory.getLogger(SijoitteluResource.class);

    @Autowired
    private SijoitteluTulosService sijoitteluTulosService;

    @Autowired
    private RaportointiService raportointiService;


    @GET
    @JsonView(JsonViews.Sijoittelu.class)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{hakuOid}")
    @Secured({ READ, UPDATE, CRUD })
    public SijoitteluDTO getSijoitteluByHakuOid(@PathParam("hakuOid") String hakuOid) {
        return sijoitteluTulosService.getSijoitteluByHakuOid(hakuOid);
    }

    @GET
    @JsonView(JsonViews.Sijoitteluajo.class)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{hakuOid}/sijoitteluajo/{sijoitteluajoId}")
    @Secured({ READ, UPDATE, CRUD })
    public SijoitteluajoDTO getSijoitteluajo(@PathParam("hakuOid") String hakuOid,
                                             @PathParam("sijoitteluajoId") String sijoitteluajoId) {
        if ("latest".equals(sijoitteluajoId)) {
            return sijoitteluTulosService.getLatestSijoitteluajo(hakuOid);
        } else {
            return sijoitteluTulosService.getSijoitteluajo(Long.parseLong(sijoitteluajoId));
        }
    }

    @GET
    @JsonView(JsonViews.Hakukohde.class)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{hakuOid}/sijoitteluajo/{sijoitteluajoId}/hakukohde/{hakukohdeOid}")
    @Secured({ READ, UPDATE, CRUD })
    public HakukohdeDTO getHakukohdeBySijoitteluajo(@PathParam("hakuOid") String hakuOid,
                                                    @PathParam("sijoitteluajoId") String sijoitteluajoId,
                                                    @PathParam("hakukohdeOid") String hakukohdeOid) {
        if ("latest".equals(sijoitteluajoId)) {
            return sijoitteluTulosService.getLatestHakukohdeBySijoitteluajo(hakuOid, hakukohdeOid);
        } else {
            return sijoitteluTulosService.getHakukohdeBySijoitteluajo(Long.parseLong(sijoitteluajoId), hakukohdeOid);
        }
    }


    @GET
    @JsonView(JsonViews.Hakemus.class)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{hakuOid}/sijoitteluajo/{sijoitteluajoId}/koulutuspaikalliset}")
    @Secured({ READ, UPDATE, CRUD })
    public List<HakijaRaportointiDTO> koulutuspaikalliset(@PathParam("hakuOid") String hakuOid,
                                                          @PathParam("sijoitteluajoId") String sijoitteluajoId,
                                                          @PathParam("hakemusOid") String hakemusOid) {
        if ("latest".equals(sijoitteluajoId)) {
            return raportointiService.latestKoulutuspaikalliset(hakuOid);
        } else {
            return raportointiService.Koulutuspaikalliset((Long.parseLong(sijoitteluajoId)));
        }
    }
    @GET
    @JsonView(JsonViews.Hakemus.class)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{hakuOid}/sijoitteluajo/{sijoitteluajoId}/ilmankoulutuspaikkaa}")
    @Secured({ READ, UPDATE, CRUD })
    public List<HakijaRaportointiDTO> ilmankoulutuspaikkaa(@PathParam("hakuOid") String hakuOid,
                                                           @PathParam("sijoitteluajoId") String sijoitteluajoId,
                                                           @PathParam("hakemusOid") String hakemusOid) {
        if ("latest".equals(sijoitteluajoId)) {
            return raportointiService.latestIlmankoulutuspaikkaa(hakuOid, hakemusOid);
        } else {
            return raportointiService.ilmankoulutuspaikkaa(Long.parseLong(sijoitteluajoId));
        }
    }
}
