package fi.vm.sade.sijoittelu.laskenta.service.business.impl;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.routing.RoundRobinRouter;
import fi.vm.sade.sijoittelu.laskenta.service.business.ActorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import java.util.UUID;

import static fi.vm.sade.sijoittelu.laskenta.actors.creators.SpringExtension.SpringExtProvider;

@Service
public class ActorServiceImpl implements ActorService {
    @Autowired
    ApplicationContext applicationContext;

    private ActorSystem actorSystem;

    private ActorRef router;

    private ActorRef erillisRouter;

    @PostConstruct
    public void initActorSystem() {
        actorSystem = ActorSystem.create("SijoitteluActorSystem");
        SpringExtProvider.get(actorSystem).initialize(applicationContext);
        router = actorSystem.actorOf(SpringExtProvider.get(actorSystem).props("SijoitteluActor")
                        .withRouter(new RoundRobinRouter(1)), "SijoitteluRouter");
        erillisRouter = actorSystem.actorOf(SpringExtProvider.get(actorSystem).props("ErillisSijoitteluActor")
                        .withRouter(new RoundRobinRouter(3)), "ErillisSijoitteluRouter");
    }

    @PreDestroy
    public void tearDownActorSystem() {
        actorSystem.shutdown();
        actorSystem.awaitTermination();
    }

    @Override
    public ActorSystem getActorSystem() {
        return actorSystem;
    }

    @Override
    public ActorRef getSiivousActor() {
        return actorSystem.actorOf(SpringExtProvider.get(actorSystem).props("SijoitteluSiivousActor"), UUID.randomUUID().toString());
    }

    @Override
    public ActorRef getSijoitteluActor() {
        return router;
    }

    @Override
    public ActorRef getErillisSijoitteluActor() {
        return erillisRouter;
    }
}
