package fi.vm.sade.sijoittelu.laskenta.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Status;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;

@Component("SijoitteluActor")
@Scope(value = "prototype")
public class SijoitteluActor extends AbstractActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    @Autowired
    private SijoitteluBusinessService sijoitteluBusinessService;

    public SijoitteluActor() {
        receive(ReceiveBuilder.match(HakuDTO.class, haku -> {
            try {
                log.info("Sijoittelukutsu haulle {} saapunut actorille!", haku.getHakuOid());
                sijoitteluBusinessService.sijoittele(haku, new HashSet<>(), new HashSet<>(), System.currentTimeMillis(), Collections.emptyMap());
                log.info("Sijoittelu haulle {} suoritettu onnistuneesti!", haku.getHakuOid());
                sender().tell(true, self());
            } catch (Exception e) {
                log.error("Sijoittelu haulle " + haku.getHakuOid() + " epäonnistui", e);
                sender().tell(new Status.Failure(e), ActorRef.noSender());
            }
        }).build());
    }
}
