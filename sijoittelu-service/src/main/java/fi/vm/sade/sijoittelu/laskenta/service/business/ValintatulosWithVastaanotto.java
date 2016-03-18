package fi.vm.sade.sijoittelu.laskenta.service.business;

import com.mysema.commons.lang.Pair;
import fi.vm.sade.sijoittelu.domain.LogEntry;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.laskenta.external.resource.ValintaTulosServiceResource;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.VastaanottoEventDto;
import fi.vm.sade.sijoittelu.tulos.dao.ValintatulosDao;
import org.apache.commons.lang3.tuple.Triple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ValintatulosWithVastaanotto {

    final private ValintatulosDao valintatulosDao;
    final private ValintaTulosServiceResource valintaTulosServiceResource;

    public ValintatulosWithVastaanotto(ValintatulosDao valintatulosDao,
                                       ValintaTulosServiceResource valintaTulosServiceResource) {
        this.valintatulosDao = valintatulosDao;
        this.valintaTulosServiceResource = valintaTulosServiceResource;
    }

    public List<Valintatulos> forHaku(String hakuOid) {
        List<Valintatulos> fromDb = valintatulosDao.loadValintatulokset(hakuOid);
        Map<Triple<String, String, String>, Valintatulos> vastaanottotiedot =
                indexValintatulokset(valintaTulosServiceResource.valintatuloksetValinnantilalla(hakuOid));
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
        return fromDb;
    }

    public void persistValintatulokset(List<Valintatulos> valintatulokset) {
        List<VastaanottoEventDto> vs = valintatulokset.stream()
                .map(v -> new VastaanottoEventDto(
                        v.getHakijaOid(), v.getHakemusOid(), v.getHakukohdeOid(), v.getHakuOid(), v.getTila(),
                        "järjestelmä", extractSeliteFromValintatulos(v)))
                .collect(Collectors.toList());
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
