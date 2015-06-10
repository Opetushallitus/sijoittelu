package fi.vm.sade.sijoittelu.laskenta.service.business;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import fi.vm.sade.sijoittelu.domain.IlmoittautumisTila;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;

import java.util.List;

public interface ActorService {
    ActorSystem getActorSystem();

    ActorRef getSiivousActor();

    ActorRef getSijoitteluActor();

    ActorRef getErillisSijoitteluActor();
}
