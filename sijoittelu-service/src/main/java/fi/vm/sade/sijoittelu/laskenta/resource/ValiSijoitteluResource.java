package fi.vm.sade.sijoittelu.laskenta.resource;

import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.dto.ValisijoitteluDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintakoe.Tasasijasaanto;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintalaskenta.tulos.service.ValintalaskentaTulosService;
import fi.vm.sade.valintalaskenta.tulos.service.impl.ValintatietoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static fi.vm.sade.sijoittelu.laskenta.actors.creators.SpringExtension.SpringExtProvider;
import static fi.vm.sade.valintalaskenta.tulos.roles.ValintojenToteuttaminenRole.OPH_CRUD;

@RequestMapping(value = "/resources/valisijoittele")
@RestController
@PreAuthorize("isAuthenticated()")
@Tag(name = "valisijoittele", description = "Resurssi sijoitteluun")
public class ValiSijoitteluResource {
    private final static Logger LOGGER = LoggerFactory.getLogger(ValiSijoitteluResource.class);

    @Autowired
    private SijoitteluBusinessService sijoitteluBusinessService;

    @Autowired
    private ValintalaskentaTulosService tulosService;

    @Autowired
    private ValintatietoService valintatietoService;

    @Autowired
    private ApplicationContext applicationContext;

    private ActorSystem actorSystem;

    private ActorRef master;

    @PostConstruct
    public void initActorSystem() {
        actorSystem = ActorSystem.create("ValiSijoitteluActorSystem");
        SpringExtProvider.get(actorSystem).initialize(applicationContext);

        master = actorSystem.actorOf(SpringExtProvider.get(actorSystem).props("ValiSijoitteluActor")
                        .withRouter(new RoundRobinRouter(1)), "ValiSijoitteluRouter");
    }

    @PreDestroy
    public void tearDownActorSystem() {
        actorSystem.shutdown();
        actorSystem.awaitTermination();
    }

    @PreAuthorize(OPH_CRUD)
    @PostMapping(value = "/{hakuOid}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Välisijoittelun suorittaminen")
    public List<HakukohdeDTO> sijoittele(@PathVariable("hakuOid") String hakuOid, @RequestBody ValisijoitteluDTO hakukohteet) {
        LOGGER.info("Valintatietoja valmistetaan valisijottelulle haussa {}", hakuOid);
        HakuDTO haku = valintatietoService.haeValintatiedotJonoille(hakuOid, hakukohteet.getHakukohteet(), Optional.empty());

        // Asetetaan välisijoittelun vaatimat valintaperusteet
        haku.getHakukohteet().forEach(hakukohde -> {
            hakukohde.getValinnanvaihe().forEach(vaihe -> {
                vaihe.getValintatapajonot().forEach(jono -> {
                    jono.setTasasijasaanto(Tasasijasaanto.YLITAYTTO);
                    jono.setAktiivinen(true);
                    jono.setSiirretaanSijoitteluun(true);
                    jono.setValmisSijoiteltavaksi(true);
                });
            });
        });
        LOGGER.info("Valintatiedot haettu serviceltä haussa {}!", hakuOid);
        Timeout timeout = new Timeout(Duration.create(60, "minutes"));
        Future<Object> future = Patterns.ask(master, haku, timeout);
        try {
            LOGGER.info("############### Odotellaan välisijoittelun valmistumista haussa {} ###############", hakuOid);
            List<HakukohdeDTO> onnistui = (List<HakukohdeDTO>) Await.result(future, timeout.duration());
            LOGGER.info("############### Välisijoittelu valmis haussa {} ###############", hakuOid);
            return onnistui;
        } catch (Exception e) {
            LOGGER.error("Välisijoittelu epäonnistui haulle " + hakuOid, e);
            return new ArrayList<>();
        }
    }
}
