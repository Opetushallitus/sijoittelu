package fi.vm.sade.sijoittelu.laskenta.resource;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.routing.RoundRobinRouter;
import akka.util.Timeout;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintalaskenta.tulos.service.ValintalaskentaTulosService;
import fi.vm.sade.valintalaskenta.tulos.service.impl.ValintatietoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.*;
import java.util.*;

import static fi.vm.sade.sijoittelu.laskenta.actors.creators.SpringExtension.SpringExtProvider;

@Path("sijoittele")
@Component
//@PreAuthorize("isAuthenticated()")
@Api(value = "/tila", description = "Resurssi sijoitteluun")
public class SijoitteluResource {

	private final static Logger LOGGER = LoggerFactory
			.getLogger(SijoitteluResource.class);

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
        actorSystem = ActorSystem.create("SijoitteluActorSystem");
        SpringExtProvider.get(actorSystem).initialize(applicationContext);

        master = actorSystem.actorOf(
                SpringExtProvider.get(actorSystem).props("SijoitteluActor").withRouter(new RoundRobinRouter(1)), "SijoitteluRouter");
    }

    @PreDestroy
    public void tearDownActorSystem() {
        actorSystem.shutdown();
        actorSystem.awaitTermination();
    }

	@GET
	@Path("{hakuOid}")
//	@PreAuthorize(CRUD)
	@ApiOperation(value = "Hakemuksen valintatulosten haku")
	public String sijoittele(@PathParam("hakuOid") String hakuOid) {

        LOGGER.error("Valintatietoja valmistetaan haulle {}!", hakuOid);

        HakuDTO haku = valintatietoService.haeValintatiedot(hakuOid);
        LOGGER.error("Valintatiedot haettu serviceltä {}!", hakuOid);

        Timeout timeout = new Timeout(Duration.create(60, "minutes"));

        Future<Object> future = Patterns
                .ask(master, haku, timeout);

        try {
            LOGGER.error("############### Odotellaan sijoittelun valmistumista ###############");
            boolean onnistui = (boolean) Await.result(future, timeout.duration());
            LOGGER.error("############### Sijoittelu valmis ###############");
            return String.valueOf(onnistui);
        } catch (Exception e) {
            return "false";
        }

//        try {
//            sijoitteluBusinessService.sijoittele(haku);
//            LOGGER.error("Sijoittelu suoritettu onnistuneesti!");
//        } catch (Exception e) {
//            e.printStackTrace();
//            LOGGER.error("Sijoittelu epäonnistui syystä {}!\r\n{}",
//                    e.getMessage(), Arrays.toString(e.getStackTrace()));
//            return "false";
//        }
//        return "true";
	}

}
