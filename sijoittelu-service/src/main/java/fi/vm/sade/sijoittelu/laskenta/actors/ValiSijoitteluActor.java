package fi.vm.sade.sijoittelu.laskenta.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Status;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Named;
import java.util.Arrays;
import java.util.List;

/**
 * Created by kjsaila on 21/08/14.
 */
@Named("ValiSijoitteluActor")
@Component
@Scope(value = "prototype")
public class ValiSijoitteluActor extends AbstractActor {

    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    @Autowired
    private SijoitteluBusinessService sijoitteluBusinessService;

    public ValiSijoitteluActor() {
        receive(ReceiveBuilder.match(HakuDTO.class, haku -> {
            try {
                log.error("Sijoittelukutsu saapunut actorille!");
                List<HakukohdeDTO> tulokset = sijoitteluBusinessService.valisijoittele(haku);
                log.error("Sijoittelu suoritettu onnistuneesti!");
                sender().tell(tulokset, self());
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Sijoittelu epäonnistui syystä {}!\r\n{}",
                        e.getMessage(), Arrays.toString(e.getStackTrace()));
                sender().tell(new Status.Failure(e), ActorRef.noSender());
            }
        }).build());
    }


}
