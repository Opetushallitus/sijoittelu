package fi.vm.sade.sijoittelu.tulos.resource;

import fi.vm.sade.sijoittelu.domain.JsonViews;
import fi.vm.sade.sijoittelu.domain.dto.HakemusDTO;
import fi.vm.sade.sijoittelu.domain.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.domain.dto.SijoitteluajoDTO;
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
 * User: wuoti
 * Date: 29.4.2013
 * Time: 15.34
 */
@Path("sijoitteluajo")
@Component
@PreAuthorize("isAuthenticated()")
public class SijoitteluajoResource {

    private final static Logger LOGGER = LoggerFactory.getLogger(SijoitteluajoResource.class);

    @Autowired
    private SijoitteluTulosService sijoitteluTulosService;

    @GET
    @JsonView(JsonViews.Sijoitteluajo.class)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{sijoitteluajoId}")
    @Secured({READ, UPDATE, CRUD})
    public SijoitteluajoDTO getSijoitteluajo(@PathParam("sijoitteluajoId") Long sijoitteluajoId) {
        return sijoitteluTulosService.getSijoitteluajo(sijoitteluajoId);
    }

    @GET
    @JsonView(JsonViews.Hakukohde.class)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{sijoitteluajoId}/hakukohde/{hakukohdeOid}")
    @Secured({READ, UPDATE, CRUD})
    public HakukohdeDTO getHakukohdeBySijoitteluajo(@PathParam("sijoitteluajoId") Long sijoitteluajoId,
                                                    @PathParam("hakukohdeOid") String hakukohdeOid) {
        return sijoitteluTulosService.getHakukohdeBySijoitteluajo(sijoitteluajoId, hakukohdeOid);
    }

    @GET
    @JsonView(JsonViews.Hakemus.class)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{sijoitteluajoId}/hakemus/{hakemusOid}")
    @Secured({READ, UPDATE, CRUD})
    public List<HakemusDTO> getHakemusBySijoitteluajo(@PathParam("sijoitteluajoId") Long sijoitteluajoId,
                                                      @PathParam("hakemusOid") String hakemusOid) {
        return sijoitteluTulosService.haeHakukohteetJoihinHakemusOsallistuu(sijoitteluajoId, hakemusOid);
    }
}
