package fi.vm.sade.sijoittelu.laskenta.resource;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.routing.RoundRobinRouter;
import akka.util.Timeout;
import com.wordnik.swagger.annotations.Api;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.dto.ValisijoitteluDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintakoe.Tasasijasaanto;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintalaskenta.tulos.service.ValintalaskentaTulosService;
import fi.vm.sade.valintalaskenta.tulos.service.impl.ValintatietoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.*;
import java.util.ArrayList;
import java.util.List;

import static fi.vm.sade.sijoittelu.laskenta.actors.creators.SpringExtension.SpringExtProvider;
import static fi.vm.sade.valintalaskenta.tulos.roles.ValintojenToteuttaminenRole.CRUD;

@Path("valisijoittele")
@Component
@PreAuthorize("isAuthenticated()")
@Api(value = "/tila", description = "Resurssi sijoitteluun")
public class ValiSijoitteluResource {

	private final static Logger LOGGER = LoggerFactory
			.getLogger(ValiSijoitteluResource.class);

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

		master = actorSystem.actorOf(
				SpringExtProvider.get(actorSystem).props("ValiSijoitteluActor")
						.withRouter(new RoundRobinRouter(1)),
				"ValiSijoitteluRouter");
	}

	@PreDestroy
	public void tearDownActorSystem() {
		actorSystem.shutdown();
		actorSystem.awaitTermination();
	}

	@POST
    @Path("{hakuOid}")
    @Consumes("application/json")
    @Produces("application/json")
	@PreAuthorize(CRUD)
	public List<HakukohdeDTO> sijoittele(@PathParam("hakuOid") String hakuOid, ValisijoitteluDTO hakukohteet) {

		LOGGER.error("Valintatietoja valmistetaan valisijottelulle");

		HakuDTO haku = valintatietoService.haeValintatiedotJonoille(hakuOid, hakukohteet.getHakukohteet());

        // Asetetaan välisijoittelun vaatimat valintaperusteet
        haku.getHakukohteet().forEach(hakukohde -> {
            hakukohde.getValinnanvaihe().forEach(vaihe -> {
                vaihe.getValintatapajonot().forEach(jono -> {
                    jono.setTasasijasaanto(Tasasijasaanto.ALITAYTTO);
                    jono.setAktiivinen(true);
                    jono.setSiirretaanSijoitteluun(true);
                    jono.setValmisSijoiteltavaksi(true);
                });
            });
        });

		LOGGER.error("Valintatiedot haettu serviceltä {}!", hakuOid);

		Timeout timeout = new Timeout(Duration.create(60, "minutes"));

		Future<Object> future = Patterns.ask(master, haku, timeout);

        try {
            LOGGER.error("############### Odotellaan sijoittelun valmistumista ###############");
            List<HakukohdeDTO> onnistui = (List<HakukohdeDTO>) Await.result(future, timeout.duration());
            LOGGER.error("############### Sijoittelu valmis ###############");
            return onnistui;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

		// try {
		// sijoitteluBusinessService.sijoittele(haku);
		// LOGGER.error("Sijoittelu suoritettu onnistuneesti!");
		// } catch (Exception e) {
		// e.printStackTrace();
		// LOGGER.error("Sijoittelu epäonnistui syystä {}!\r\n{}",
		// e.getMessage(), Arrays.toString(e.getStackTrace()));
		// return "false";
		// }
		// return "true";
	}

}
