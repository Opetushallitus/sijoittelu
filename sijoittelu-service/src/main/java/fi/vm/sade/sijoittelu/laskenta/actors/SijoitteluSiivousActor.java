package fi.vm.sade.sijoittelu.laskenta.actors;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.PoisonPill;
import akka.actor.Status;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.laskenta.actors.messages.PoistaHaamuHakukohteet;
import fi.vm.sade.sijoittelu.laskenta.actors.messages.PoistaHakukohteet;
import fi.vm.sade.sijoittelu.laskenta.actors.messages.PoistaVanhatAjotSijoittelulta;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.sijoittelu.tulos.dao.HakukohdeDao;
import fi.vm.sade.sijoittelu.tulos.dao.SijoitteluDao;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named("SijoitteluSiivousActor")
@Component
@Scope(value = "prototype")
public class SijoitteluSiivousActor extends AbstractActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);

    @Autowired
    private SijoitteluDao sijoitteluDao;

    @Autowired
    private HakukohdeDao hakukohdeDao;

    public SijoitteluSiivousActor() {
        receive(ReceiveBuilder.match(PoistaVanhatAjotSijoittelulta.class, sijoittelu -> {
            log.info("Poistetaan vanhat sijoitteluajot haun {} sijoittelulta {} - ajojen maksimimäärä {}",
                    sijoittelu.getHakuOid(), sijoittelu.getSijoitteluId(), sijoittelu.getAjojenMaaraMax());
            Optional<Sijoittelu> sijoitteluOpt = sijoitteluDao.getSijoitteluById(sijoittelu.getSijoitteluId());
            sijoitteluOpt.ifPresent(s -> {
                if(s.getSijoitteluajot().size() > sijoittelu.getAjojenMaaraMax()) {
                    Set<SijoitteluAjo> saastettavat = s.getSijoitteluajot().stream()
                            .sorted((a1, a2) -> -(a1.getEndMils().compareTo(a2.getEndMils())))
                            .limit(sijoittelu.getAjojenMaaraMax())
                            .sorted((a1, a2) -> a1.getEndMils().compareTo(a2.getEndMils()))
                            .collect(Collectors.toSet());
                    Set<SijoitteluAjo> poistettavat = s.getSijoitteluajot().stream()
                            .sorted((a1, a2) -> a1.getEndMils().compareTo(a2.getEndMils()))
                            .limit(s.getSijoitteluajot().size() - sijoittelu.getAjojenMaaraMax())
                            .collect(Collectors.toSet());

                    Set<SijoitteluAjo> common = new HashSet<>();
                    common.addAll(saastettavat);
                    common.retainAll(poistettavat);
                    if(common.size() > 0) {
                        log.error("Säästettävissä ja poistettavissa ajoissa on yhtäläisyyksiä kpl: {} , hakuOid: ", common.size(), sijoittelu.getHakuOid());
                    }
                    if (!saastettavat.isEmpty() && common.size() == 0) {
                        log.info("Säästetään haulle {} sijoitteluajoja {} kpl, poistetaan {} kpl", sijoittelu.getHakuOid(), saastettavat.size(), poistettavat.size());
                        s.getSijoitteluajot().clear();
                        s.getSijoitteluajot().addAll(saastettavat);
                        sijoitteluDao.persistSijoittelu(s);
                        poistettavat.stream().forEach(a -> {
                            List<Hakukohde> kohde = hakukohdeDao.getHakukohdeForSijoitteluajo(a.getSijoitteluajoId());
                            log.info("Poistetaan haun {} sijoitteluajolta {} hakukohteita {} kpl", sijoittelu.getHakuOid(), a.getSijoitteluajoId(), kohde.size());
                            kohde.stream().forEach(hakukohdeDao::removeHakukohde);
                        });
                    } else {
                        log.warning("Ei poisteta haun {} vanhoja sijoitteluajoja. Säästettäviä {} , päällekkäisiä {}", sijoittelu.getHakuOid(), saastettavat.size(), common.size());
                    }
                }
            });
            self().tell(PoisonPill.getInstance(), ActorRef.noSender());
        }).match(PoistaHaamuHakukohteet.class, p -> {
            log.info("Poistetaan haamu hakukohteet sijoittelusta");
            List<Long> ajot = new ArrayList<>();
            List<Sijoittelu> kaikki = sijoitteluDao.findAll();
            kaikki.stream().forEach(s -> {
                ajot.addAll(s.getSijoitteluajot().stream().map(SijoitteluAjo::getSijoitteluajoId).collect(Collectors.toList()));
            });

            List<Hakukohde> kohteet = hakukohdeDao.findAll();
            kohteet.stream().filter(h -> !ajot.contains(h.getSijoitteluajoId())).forEach(hakukohdeDao::removeHakukohde);
            self().tell(PoisonPill.getInstance(), ActorRef.noSender());
        }).match(PoistaHakukohteet.class, p -> {
            log.info("Poistetaan hakukohteet pieleen menneeltä sijoittelulta");

            Optional<SijoitteluAjo> ajo = sijoitteluDao.getSijoitteluajo(p.getAjoId());

            if(ajo.isPresent()) {
                log.error("Yritettiin poistaa ajon {} hakukohteita, vaikka ajo löytyi kannasta!", p.getAjoId());
            } else {
                List<Hakukohde> kohteet = hakukohdeDao.getHakukohdeForSijoitteluajo(p.getAjoId());
                kohteet.stream().forEach(hakukohdeDao::removeHakukohde);
                self().tell(PoisonPill.getInstance(), ActorRef.noSender());
            }

        }).build());
    }


}
