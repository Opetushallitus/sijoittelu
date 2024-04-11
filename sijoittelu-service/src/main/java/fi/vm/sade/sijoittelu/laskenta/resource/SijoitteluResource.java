package fi.vm.sade.sijoittelu.laskenta.resource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import fi.vm.sade.javautils.nio.cas.CasClient;
import fi.vm.sade.sijoittelu.laskenta.email.EmailService;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBookkeeperService;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.sijoittelu.laskenta.service.business.ToteutaSijoitteluService;
import fi.vm.sade.sijoittelu.laskenta.service.it.TarjontaIntegrationService;
import fi.vm.sade.sijoittelu.laskenta.util.UrlProperties;
import fi.vm.sade.valintalaskenta.tulos.service.impl.ValintatietoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

import static fi.vm.sade.valintalaskenta.tulos.roles.ValintojenToteuttaminenRole.OPH_CRUD;

@RequestMapping(value = "/resources/sijoittele")
@RestController
@PreAuthorize("isAuthenticated()")
@Tag(name = "sijoittele", description = "Resurssi sijoitteluun")
public class SijoitteluResource {
    private final static Logger LOGGER = LoggerFactory.getLogger(SijoitteluResource.class);

    private final ToteutaSijoitteluService toteutaSijoitteluService;

    private final SijoitteluBusinessService sijoitteluBusinessService;
    private final ValintatietoService valintatietoService;
    private final SijoitteluBookkeeperService sijoitteluBookkeeperService;
    private final CasClient sijoitteluCasClient;
    private final UrlProperties urlProperties;
    private final TarjontaIntegrationService tarjontaIntegrationService;
    private final EmailService emailService;
    private final Gson gson;

    @Autowired
    public SijoitteluResource(ToteutaSijoitteluService toteutaSijoitteluService,
                              SijoitteluBusinessService sijoitteluBusinessService,
                              ValintatietoService valintatietoService,
                              SijoitteluBookkeeperService sijoitteluBookkeeperService,
                              @Qualifier("SijoitteluCasClient") CasClient sijoitteluCasClient,
                              UrlProperties urlProperties,
                              TarjontaIntegrationService tarjontaIntegrationService,
                              EmailService emailService) {
        this.toteutaSijoitteluService = toteutaSijoitteluService;
        this.sijoitteluBusinessService = sijoitteluBusinessService;
        this.valintatietoService = valintatietoService;
        this.sijoitteluCasClient = sijoitteluCasClient;
        this.sijoitteluBookkeeperService = sijoitteluBookkeeperService;
        this.urlProperties = urlProperties;
        this.tarjontaIntegrationService = tarjontaIntegrationService;
        this.emailService = emailService;

        this.gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) ->
                        new Date(json.getAsJsonPrimitive().getAsLong()))
                .create();
    }

    @GetMapping(value = "/ajontila/{sijoitteluId}", produces = MediaType.TEXT_PLAIN_VALUE)
    @PreAuthorize(OPH_CRUD)
    @Operation(summary = "Sijoitteluajon tila", responses = { @ApiResponse(responseCode = "OK", content = @Content(schema = @Schema(implementation = String.class)))})
    public String sijoittelunTila(@PathVariable("sijoitteluId") Long id) {
        String tila = sijoitteluBookkeeperService.getSijoitteluAjonTila(id);
        LOGGER.info("/ajontila/sijoitteluId: Palautetaan sijoitteluajolle {} tila {}", id, tila);
        return tila;
    }

    @GetMapping(value = "/{hakuOid}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize(OPH_CRUD)
    @Operation(summary = "Käynnistä uusi sijoittelu haulle", responses = { @ApiResponse(responseCode = "OK", content = @Content(schema = @Schema(implementation = Long.class)))})
    public Long sijoittele(@PathVariable("hakuOid") String hakuOid) {
        return this.toteutaSijoitteluService.toteutaSijoittelu(hakuOid);
    }
}
