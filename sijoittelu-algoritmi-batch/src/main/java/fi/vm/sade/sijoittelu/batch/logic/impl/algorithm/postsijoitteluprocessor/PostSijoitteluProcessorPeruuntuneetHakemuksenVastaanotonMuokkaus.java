package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilaTaulukot;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.*;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class PostSijoitteluProcessorPeruuntuneetHakemuksenVastaanotonMuokkaus implements PostSijoitteluProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(PostSijoitteluProcessorPeruuntuneetHakemuksenVastaanotonMuokkaus.class);



    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        LOG.info("Aloitetaan peruuntuneiden hakemusten vastaanottojen muokkaus.");
        peruuntuneetJaVastaanotetutValintatulokset(sijoitteluajoWrapper).forEach(v -> {
            LOG.info("Hakemus {} on PERUUNTUNUT ja valintatuloksentilassa {}, asetetaan valintatuloksentila KESKEN",
                    v.getHakemusOid(), v.getTila());
            poistaVastaanottoTieto(v);
            if (!sijoitteluajoWrapper.getMuuttuneetValintatulokset().contains(v)) {
                sijoitteluajoWrapper.getMuuttuneetValintatulokset().add(v);
            }
        });
        LOG.info("Lopetetaan peruuntuneiden hakemusten vastaanottojen muokkaus.");
    }

    private static boolean vastaanottanut(Valintatulos valintatulos) {
        return  ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI == valintatulos.getTila() ||
                ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT == valintatulos.getTila();
    }

    private static boolean hyvaksyttyHakukohteelle(Valintatulos valintatulos, Set<Pair<String, String>> hyvaksytytHakemuksetIndex) {
        Pair pair = Pair.of(valintatulos.getHakukohdeOid(), valintatulos.getHakemusOid());
        return !hyvaksytytHakemuksetIndex.contains(pair);
    }

    private static List<Valintatulos> peruuntuneetJaVastaanotetutValintatulokset(SijoitteluajoWrapper sijoitteluajoWrapper) {
        Set<Pair<String, String>> hyvaksytytHakemuksetIndex = indexHyvaksytytHakemukset(sijoitteluajoWrapper);
        return sijoitteluajoWrapper.getHakukohteet().stream()
                .flatMap(hk -> hk.getValintatapajonot().stream())
                .flatMap(j -> j.getHakemukset().stream())
                .filter(h -> HakemuksenTila.PERUUNTUNUT == h.getHakemus().getTila())
                .map(HakemusWrapper::getValintatulos)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(PostSijoitteluProcessorPeruuntuneetHakemuksenVastaanotonMuokkaus::vastaanottanut)
                .filter(vt -> !hyvaksyttyHakukohteelle(vt, hyvaksytytHakemuksetIndex))
                .collect(Collectors.toList());
    }

    private static void poistaVastaanottoTieto(Valintatulos valintatulos) {
        valintatulos.setTila(ValintatuloksenTila.KESKEN, "Peruuntunut hakutoive");
        valintatulos.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY, "Peruuntunut hakutoive");
        valintatulos.setHyvaksyttyVarasijalta(false, "Peruuntunut hakutoive");
    }

    private static Set<Pair<String, String>> indexHyvaksytytHakemukset(SijoitteluajoWrapper sijoitteluajoWrapper) {

        return sijoitteluajoWrapper.getHakukohteet().stream()
                .map(hkw -> {
                    String hakukohdeOid = hkw.getHakukohde().getOid();
                    return hkw.getValintatapajonot().stream()
                            .flatMap(jono -> jono.getHakemukset().stream())
                            .filter(hw -> TilaTaulukot.kuuluuHyvaksyttyihinTiloihin(hw.getHakemus().getTila()))
                            .map(hw -> Pair.of(hakukohdeOid, hw.getHakemus().getHakemusOid()))
                            .collect(Collectors.toSet());
                }).flatMap(set -> set.stream()).collect(Collectors.toSet());
    }
}

