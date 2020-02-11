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
                log.info("Välisijoittelukutsu haulle {} saapunut actorille!", haku.getHakuOid());
                List<HakukohdeDTO> tulokset = sijoitteluBusinessService.valisijoittele(haku);
                tulokset.forEach(hk -> {
                    hk.getValintatapajonot().forEach(j -> {
                        j.getHakemukset().forEach(h -> {
                            log.info("Hakukohteen {} valintatapajonon {} hakemuksen {} sijoittelun tulos: {}", hk.getOid(), j.getOid(), h.getHakemusOid(), h.getTila());
                        });
                    });
                });
                log.info("Välisijoittelu haulle {} suoritettu onnistuneesti!", haku.getHakuOid());
                sender().tell(tulokset, self());
            } catch (Exception e) {
                log.error("Välisijoittelu haulle " + haku.getHakuOid() + " epäonnistui", e);
                sender().tell(new Status.Failure(e), ActorRef.noSender());
            }
        }).build());
    }
}
