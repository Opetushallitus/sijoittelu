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

import javax.inject.Named;
import java.util.Arrays;

/**
 * Created by kjsaila on 21/08/14.
 */
@Named("ErillisSijoitteluActor")
@Component
@Scope(value = "prototype")
public class ErillisSijoitteluActor extends AbstractActor {

    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    @Autowired
    private SijoitteluBusinessService sijoitteluBusinessService;

    public ErillisSijoitteluActor() {
        receive(ReceiveBuilder.match(HakuDTO.class, haku -> {
            try {
                log.info("Erillissijoittelukutsu haulle {} saapunut actorille!", haku.getHakuOid());
                long ajo = sijoitteluBusinessService.erillissijoittele(haku);
                log.info("Erillissijoittelu haulle {} suoritettu onnistuneesti!", haku.getHakuOid());
                sender().tell(ajo, self());
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Erillissijoittelu haulle {} epäonnistui syystä {}!\r\n{}", haku.getHakuOid(),
                        e.getMessage(), Arrays.toString(e.getStackTrace()));
                sender().tell(new Status.Failure(e), ActorRef.noSender());
            }
        }).build());
    }


}
