package fi.vm.sade.sijoittelu.laskenta.resource;

import akka.pattern.Patterns;
import akka.util.Timeout;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import fi.vm.sade.service.valintaperusteet.dto.ValintatapajonoDTO;
import fi.vm.sade.service.valintaperusteet.resource.ValintalaskentakoostepalveluResource;
import fi.vm.sade.sijoittelu.laskenta.service.business.ActorService;
import fi.vm.sade.sijoittelu.tulos.dto.ValisijoitteluDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintalaskenta.tulos.service.impl.ValintatietoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import java.util.*;

import static fi.vm.sade.valintalaskenta.tulos.roles.ValintojenToteuttaminenRole.CRUD;


@Path("erillissijoittele")
@Controller
@PreAuthorize("isAuthenticated()")
@Api(value = "erillissijoittele", description = "Resurssi sijoitteluun")
public class ErillisSijoitteluResource {
    private final static Logger LOGGER = LoggerFactory.getLogger(ErillisSijoitteluResource.class);

    @Autowired
    private ValintatietoService valintatietoService;

    @Autowired
    ValintalaskentakoostepalveluResource valintalaskentakoostepalveluResource;

    @Autowired
    private ActorService actorService;

    @POST
    @Path("/{hakuOid}")
    @Consumes("application/json")
    @PreAuthorize(CRUD)
    @ApiOperation(consumes = "application/json", value = "Suorita erillissijoittelu", response = Long.class)
    public Long sijoittele(@PathParam("hakuOid") String hakuOid, ValisijoitteluDTO hakukohteet) {
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
        Map<String, List<ValintatapajonoDTO>> valintaperusteet =
                valintalaskentakoostepalveluResource.haeValintatapajonotSijoittelulle(new ArrayList<>(hakukohteet.getHakukohteet().keySet()));
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
