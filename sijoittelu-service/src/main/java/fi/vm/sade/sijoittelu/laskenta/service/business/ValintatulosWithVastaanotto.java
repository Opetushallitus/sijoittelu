package fi.vm.sade.sijoittelu.laskenta.service.business;

import fi.vm.sade.sijoittelu.domain.LogEntry;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.laskenta.external.resource.VirkailijaValintaTulosServiceResource;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.VastaanottoEventDto;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import java.util.*;
import java.util.stream.Collectors;

public class ValintatulosWithVastaanotto {

    private static final Logger LOG = LoggerFactory.getLogger(ValintatulosWithVastaanotto.class);

    final private VirkailijaValintaTulosServiceResource valintaTulosServiceResource;

    public ValintatulosWithVastaanotto(VirkailijaValintaTulosServiceResource valintaTulosServiceResource) {
        this.valintaTulosServiceResource = valintaTulosServiceResource;
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

        List<VastaanottoEventDto> vs = valintatulosGroups.entrySet().stream().map(e -> e.getValue())
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
                            vt.getHakijaOid(), vt.getHakemusOid(), vt.getValintatapajonoOid(), vt.getHakukohdeOid(), vt.getHakuOid(), vt.getTila(),
                            "järjestelmä", extractSeliteFromValintatulos(vt));
                }).collect(Collectors.toList());

        vs.forEach(dto -> {
            LOG.info("VastaanottoEventDto - Hakukohde: {}, valintatapajono: {}, hakija: {}, hakemus: {}, vastaanoton tila: {}",
                    dto.getHakukohdeOid(),
                    dto.getValintatapajonoOid(),
                    dto.getHenkiloOid(),
                    dto.getHakemusOid(),
                    dto.getTila());
        });

        try {
            valintaTulosServiceResource.valintatuloksetValinnantilalla(vs);
        } catch (WebApplicationException e) {
            LOG.error("Virhe lähetettäessä valintaTulosServiceResource.valintatuloksetValinnantilalla() -kutsulle " +
                vs.size() + " tulosta ; response: " + e.getResponse().readEntity(String.class), e);
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
