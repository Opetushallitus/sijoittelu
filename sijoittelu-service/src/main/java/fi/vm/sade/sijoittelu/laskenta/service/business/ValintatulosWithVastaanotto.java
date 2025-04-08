package fi.vm.sade.sijoittelu.laskenta.service.business;

import fi.vm.sade.sijoittelu.domain.LogEntry;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.valintatulosservice.valintarekisteri.domain.*;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class ValintatulosWithVastaanotto {

    private static final Logger LOG = LoggerFactory.getLogger(ValintatulosWithVastaanotto.class);

    final private WrappedVastaanottoService vastaanottoService;

    public ValintatulosWithVastaanotto(WrappedVastaanottoService vastaanottoService) {
        this.vastaanottoService = vastaanottoService;
    }

    public void persistVastaanotot(List<Valintatulos> valintatulokset) {
        valintatulokset.forEach(vt -> {
            LOG.info("Valintatulos - Hakukohde: {}, valintatapajono: {}, hakija: {}, hakemus: {}, vastaanoton tila: {}",
                    vt.getHakukohdeOid(),
                    vt.getValintatapajonoOid(),
                    vt.getHakijaOid(),
                    vt.getHakemusOid(),
                    vt.getTila());
        });

        final Map<Pair<String, String>, List<Valintatulos>> valintatulosGroups = valintatulokset.stream().
                collect(Collectors.groupingBy(valintatulos -> Pair.of(valintatulos.getHakukohdeOid(), valintatulos.getHakemusOid())));

        List<VastaanottoEventDto> vs = valintatulosGroups.values().stream()
              .map(list -> {
                  Set<Valintatulos> eiKeskenTilaiset = list.stream()
                          .filter(v -> v.getTila() != ValintatuloksenTila.KESKEN)
                          .collect(Collectors.toSet());
                  if (eiKeskenTilaiset.size() > 1) {
                      ValintatuloksenTila ensimmainenEiKeskenTila = eiKeskenTilaiset.iterator().next().getTila();
                      if (eiKeskenTilaiset.stream().anyMatch(v -> !v.getTila().equals(ensimmainenEiKeskenTila))) {
                          throw new RuntimeException("Hakijalle löytyi useampi kuin yksi ei-kesken-tilainen valintatulos, joilla on eri tilat: " + list);
                      } else {
                          LOG.warn("Hakijalle löytyi useampi kuin yksi ei-kesken-tilainen valintatulos:" + list);
                      }
                  }
                  // TODO jos ei kesken -tilaisia on useampia kuin yksi, olisi periaatteessa hyvä valita niistä valinta-tulos-serviceen
                  // tallennettavaksi sellainen, joka on oikeasta jonosta. Varsinaisen vastaanoton tallennuksen kannalta sillä ei ole
                  // väliä, mutta lokimerkintä voi mennä väärälle jonolle.
                  Valintatulos vt = (!eiKeskenTilaiset.isEmpty() ? eiKeskenTilaiset.stream().findFirst().get() : list.get(0));
                  return new VastaanottoEventDto(
                      new ValintatapajonoOid(vt.getValintatapajonoOid()),
                      vt.getHakijaOid(),
                      new HakemusOid(vt.getHakemusOid()),
                      new HakukohdeOid(vt.getHakukohdeOid()),
                      new HakuOid(vt.getHakuOid()),
                      vt.getTila().name(),
                      "järjestelmä",
                      extractSeliteFromValintatulos(vt));
              }).toList();

        vs.forEach(dto -> {
            LOG.info("VastaanottoEventDto - Hakukohde: {}, valintatapajono: {}, hakija: {}, hakemus: {}, vastaanoton tila: {}",
                    dto.hakukohdeOid(),
                    dto.valintatapajonoOid(),
                    dto.henkiloOid(),
                    dto.hakemusOid(),
                    dto.tila());
        });

        try {
            LOG.info("Vastaanota virkailijana {} valintatulosta", vs.size());
            vastaanottoService.vastaanotaVirkailijanaInTransaction(vs);
        } catch (Exception e) {
            LOG.error("Virhe tallennettaessa vastaanottoja, " +
                vs.size() + " tulosta ; response: " + e.getMessage(), e);
            throw e;
        }
    }

    private static String extractSeliteFromValintatulos(Valintatulos valintatulos) {
        return valintatulos.getLogEntries().stream()
                .sorted((a,b) -> b.getLuotu().compareTo(a.getLuotu()))
                .findFirst()
                .map(LogEntry::getSelite)
                .orElse("");
    }

}
