package fi.vm.sade.sijoittelu.laskenta.resource;

import akka.pattern.Patterns;
import akka.util.Timeout;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
