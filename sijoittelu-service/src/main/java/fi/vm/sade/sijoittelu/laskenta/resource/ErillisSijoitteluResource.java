package fi.vm.sade.sijoittelu.laskenta.resource;

import akka.pattern.Patterns;
import akka.util.Timeout;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSyntaxException;
import fi.vm.sade.javautils.nio.cas.CasClient;
import fi.vm.sade.service.valintaperusteet.dto.ValintatapajonoDTO;
import fi.vm.sade.sijoittelu.laskenta.external.resource.HttpClients;
import fi.vm.sade.sijoittelu.laskenta.service.business.ActorService;
import fi.vm.sade.sijoittelu.laskenta.util.UrlProperties;
import fi.vm.sade.sijoittelu.tulos.dto.ValisijoitteluDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintalaskenta.tulos.service.impl.ValintatietoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static fi.vm.sade.valintalaskenta.tulos.roles.ValintojenToteuttaminenRole.OPH_CRUD;

@RequestMapping(value = "/resources/erillissijoittele")
@RestController
@PreAuthorize("isAuthenticated()")
@Api(value = "erillissijoittele", description = "Resurssi sijoitteluun")
public class ErillisSijoitteluResource {
    private final static Logger LOGGER = LoggerFactory.getLogger(ErillisSijoitteluResource.class);

    @Autowired
    private ValintatietoService valintatietoService;

    @Autowired
    private ActorService actorService;

    private final CasClient sijoitteluCasClient;
    private UrlProperties urlProperties;
    private Gson gson;

    public ErillisSijoitteluResource(@Qualifier("SijoitteluCasClient") CasClient sijoitteluCasClient, UrlProperties urlProperties) {
        this.sijoitteluCasClient = sijoitteluCasClient;
        this.urlProperties = urlProperties;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) ->
                        new Date(json.getAsJsonPrimitive().getAsLong()))
                .create();
    }

    @PostMapping(value = "/{hakuOid}", consumes = "application/json", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize(OPH_CRUD)
    @ApiOperation(consumes = "application/json", value = "Suorita erillissijoittelu", response = Long.class)
    public Long sijoittele(@PathVariable("hakuOid") String hakuOid, @RequestBody ValisijoitteluDTO hakukohteet) {
        long id = ErillisSijoitteluQueue.getInstance().queueNewErillissijoittelu(hakuOid);

        try {
            while (!ErillisSijoitteluQueue.getInstance().isOkToStartErillissijoittelu(hakuOid, id)) {
                LOGGER.warn("Edellinen erillissijoittelu haulle {} on vielä käynnissä. Yritetään minuutin päästä uudelleen...");
                Thread.sleep(1000 * 60);
            }
            return erillissijoittele(hakuOid, hakukohteet);
        } catch (InterruptedException ie) {
            LOGGER.error("Erillissijoittelun vuoron odottaminen keskeytyi haulle {}", hakuOid, ie);
        } finally {
            if(!ErillisSijoitteluQueue.getInstance().erillissijoitteluDone(hakuOid, id)) {
                LOGGER.error("Tapahtui jotain outoa! Yritettiin lopettaa haulle {} erillissijoitteluajoa, jota ei ollut olemassa!", hakuOid);
            }
        }
        return null;
    }

    private Long erillissijoittele(String hakuOid, ValisijoitteluDTO hakukohteet) {
        LOGGER.info("Valintatietoja valmistetaan erillissijoittelulle haussa {}", hakuOid);

        Request valintatapajonoRequest = new RequestBuilder()
                .setUrl(urlProperties.url("valintaperusteet.valintatapajono.rest.url"))
                .setMethod("POST")
                .setBody(this.gson.toJson(new ArrayList<>(hakukohteet.getHakukohteet().keySet())))
                .addHeader("Accept", "application/json")
                .addHeader("Content-type", "application/json")
                .addHeader("Caller-Id", HttpClients.CALLER_ID)
                .setRequestTimeout(120000)
                .setReadTimeout(120000)
                .build();

        Map<String, List<ValintatapajonoDTO>> valintaperusteet = null;
        TypeToken<Map<String, List<ValintatapajonoDTO>>> token = new TypeToken<Map<String, List<ValintatapajonoDTO>>>() {};

        try {
            Response valintatapajonoResponse = sijoitteluCasClient.executeWithServiceTicketBlocking(valintatapajonoRequest);
            if (valintatapajonoResponse.getStatusCode() == 200) {
                try {
                    valintaperusteet = gson.fromJson(valintatapajonoResponse.getResponseBody(), token.getType());
                } catch (JsonSyntaxException e) {
                    throw new RuntimeException(String.format("Failed to parse response from JSON %s", valintatapajonoResponse.getResponseBody()));
                }
            } else {
                throw new RuntimeException(String.format("Failed to fetch valintatapajonot from %s. Response status: %s", valintatapajonoResponse.getUri().toString(), valintatapajonoResponse.getStatusCode()));
            }

        } catch (ExecutionException e) {
            LOGGER.error("Valintatietojen haku erillissijoittelun haulle epäonnistui");
        }

        HakuDTO haku = valintatietoService.haeValintatiedotJonoille(hakuOid, hakukohteet.getHakukohteet(), Optional.of(valintaperusteet));
        LOGGER.info("Valintatiedot haettu serviceltä haulle {}!", hakuOid);
        Timeout timeout = new Timeout(Duration.create(60, "minutes"));
        Future<Object> future = Patterns.ask(actorService.getErillisSijoitteluActor(), haku, timeout);
        try {
            LOGGER.info("############### Odotellaan erillissijoittelun valmistumista haulle {} ###############", hakuOid);
            long onnistui = (long) Await.result(future, timeout.duration());
            LOGGER.info("############### Erillissijoittelu valmis haulle {} ###############", hakuOid);
            return onnistui;
        } catch (Exception e) {
            LOGGER.error("Erillissijoittelu haulle " + haku.getHakuOid() + " epäonnistui.", e);
            return null;
        }
    }
}
