package fi.vm.sade.sijoittelu.tulos.resource;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static fi.vm.sade.sijoittelu.tulos.roles.SijoitteluRole.READ_UPDATE_CRUD;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Controller
@PreAuthorize("isAuthenticated()")
@Path("/erillissijoittelu")
@Api(value = "/erillissijoittelu", description = "Resurssi erillissijoittelun tuloksien hakemiseen")
public class ErillisSijoitteluResourceImpl {
    private final static Logger LOGGER = LoggerFactory.getLogger(ErillisSijoitteluResourceImpl.class);

    @Autowired
    private SijoitteluTulosService sijoitteluTulosService;

    @Autowired
    private RaportointiService raportointiService;

    @GET
    @Produces(APPLICATION_JSON)
    @PreAuthorize(READ_UPDATE_CRUD)
    @ApiOperation(value = "Hakee hakukohteen tiedot tietyssa sijoitteluajossa.", response = HakukohdeDTO.class)
    public Response getHakukohdeBySijoitteluajo(
            @ApiParam(name = "hakuOid", value = "Haun tunniste", required = true)
            @PathParam("hakuOid") String hakuOid,
            @ApiParam(name = "sijoitteluajoId", value = "Sijoitteluajon tunniste", required = true)
            @PathParam("sijoitteluajoId") String sijoitteluajoId,
            @ApiParam(name = "hakukohdeOid", value = "Hakukohteen tunniste", required = true)
            @PathParam("hakukohdeOid") String hakukohdeOid) {

        if (sijoitteluajoId == null) {
            return Response.ok().entity(new HakukohdeDTO()).build();
        }
        SijoitteluAjo ajo = new SijoitteluAjo();
        ajo.setSijoitteluajoId(Long.parseLong(sijoitteluajoId));
        HakukohdeDTO hakukohdeBySijoitteluajo = sijoitteluTulosService
                .getHakukohdeBySijoitteluajo(ajo, hakukohdeOid);
        return Response.ok().entity(hakukohdeBySijoitteluajo).build();
    }
}
