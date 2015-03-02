package fi.vm.sade.sijoittelu.laskenta.resource;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.routing.RoundRobinRouter;
import akka.util.Timeout;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import fi.vm.sade.sijoittelu.laskenta.service.business.ActorService;
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

@Path("erillissijoittele")
@Component
@PreAuthorize("isAuthenticated()")
@Api(value = "/erillissijoittele", description = "Resurssi sijoitteluun")
public class ErillisSijoitteluResource {

	private final static Logger LOGGER = LoggerFactory
			.getLogger(ErillisSijoitteluResource.class);

	@Autowired
	private SijoitteluBusinessService sijoitteluBusinessService;

	@Autowired
	private ValintalaskentaTulosService tulosService;

	@Autowired
	private ValintatietoService valintatietoService;

	@Autowired
	private ApplicationContext applicationContext;


    @Autowired
    private ActorService actorService;


	@POST
    @Path("{hakuOid}")
    @Consumes("application/json")
	@PreAuthorize(CRUD)
	@ApiOperation(consumes = "application/json", value = "Valintatapajonon vienti taulukkolaskentaan", response = Long.class)
	public Long sijoittele(@PathParam("hakuOid") String hakuOid, ValisijoitteluDTO hakukohteet) {

		LOGGER.error("Valintatietoja valmistetaan valisijottelulle");

		HakuDTO haku = valintatietoService.haeValintatiedotJonoille(hakuOid, hakukohteet.getHakukohteet());

		LOGGER.error("Valintatiedot haettu serviceltä {}!", hakuOid);

		Timeout timeout = new Timeout(Duration.create(60, "minutes"));

		Future<Object> future = Patterns.ask(actorService.getErillisSijoitteluActor(), haku, timeout);

        try {
            LOGGER.error("############### Odotellaan erillissijoittelun valmistumista ###############");
            long onnistui = (long) Await.result(future, timeout.duration());
            LOGGER.error("############### Erillissijoittelu valmis ###############");
            return onnistui;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
	}

}
