package fi.vm.sade.sijoittelu.laskenta.service.business;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.LogEntry;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.laskenta.external.resource.ValintaTulosServiceResource;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.VastaanottoEventDto;
import fi.vm.sade.sijoittelu.tulos.dao.ValintatulosDao;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ValintatulosWithVastaanotto {

    private static final Logger LOG = LoggerFactory.getLogger(ValintatulosWithVastaanotto.class);

    final private ValintatulosDao valintatulosDao;
    final private ValintaTulosServiceResource valintaTulosServiceResource;

    public ValintatulosWithVastaanotto(ValintatulosDao valintatulosDao,
                                       ValintaTulosServiceResource valintaTulosServiceResource) {
        this.valintatulosDao = valintatulosDao;
        this.valintaTulosServiceResource = valintaTulosServiceResource;
    }

    public List<Valintatulos> forHaku(String hakuOid) {
        LOG.info("Haetaan valintatulokset haulle {}", hakuOid);
        List<Valintatulos> fromDb = valintatulosDao.loadValintatulokset(hakuOid);
        LOG.info("Valintatulokset haettu haulle {}", hakuOid);
        LOG.info("Haetaan haetaan vastaanottotiedot haulle {}", hakuOid);
        Map<Triple<String, String, String>, Valintatulos> vastaanottotiedot =
                indexValintatulokset(valintaTulosServiceResource.valintatuloksetValinnantilalla(hakuOid));
        LOG.info("Vastaanottotiedot haettu haulle {}", hakuOid);
        fromDb.forEach(v -> {
            ValintatuloksenTila tila = ValintatuloksenTila.KESKEN;
            Triple<String, String, String> key = Triple.of(
                    v.getHakukohdeOid(),
                    v.getValintatapajonoOid(),
                    v.getHakemusOid());
            if (vastaanottotiedot.containsKey(key)) {
                tila = vastaanottotiedot.get(key).getTila();
                if (null == tila) {
                    throw new IllegalStateException(String.format("Hakukohde: %s, jono: %s, hakemus: %s vastaanottotieto valinta-tulos-servicestä on null",
                            key.getLeft(), key.getMiddle(), key.getRight()));
                }
            }
            v.setTila(tila, "");
        });
        LOG.info("Valintatuloksia haettu {} kpl haulle {}", fromDb.size(), hakuOid);
        return fromDb;
    }

    public void persistValintatulokset(List<Valintatulos> valintatulokset) {

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
                    if(eiKeskenTilaiset.size() > 1) {
                        throw new RuntimeException("Hakijalle löytyi useampi kuin yksi ei-kesken-tilainen valintatulos: " + list);
                    }
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

        valintaTulosServiceResource.valintatuloksetValinnantilalla(vs);
        valintatulokset.forEach(valintatulosDao::createOrUpdateValintatulos);
    }

    private static String extractSeliteFromValintatulos(Valintatulos valintatulos) {
        return valintatulos.getLogEntries().stream()
                .sorted((a,b) -> b.getLuotu().compareTo(a.getLuotu()))
                .findFirst()
                .map(LogEntry::getSelite)
                .orElse("");
    }

    private static Map<Triple<String, String, String>, Valintatulos> indexValintatulokset(List<Valintatulos> valintatulokset) {
        Map<Triple<String, String, String>, Valintatulos> vs = new HashMap<>();
        valintatulokset.stream().forEach(v -> {
            Triple<String, String, String> key = Triple.of(
                    v.getHakukohdeOid(),
                    v.getValintatapajonoOid(),
                    v.getHakemusOid());
            if (vs.containsKey(key)) {
                throw new IllegalStateException(String.format("Hakukohde: %s, jono: %s, hakemus: %s useita valintatuloksia",
                        key.getLeft(), key.getMiddle(), key.getRight()));
            }
            vs.put(key, v);
        });
        return vs;
    }
}
