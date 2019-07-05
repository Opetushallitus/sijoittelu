package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class PostSijoitteluProcessorPeruuntuneetHakemuksenVastaanotonMuokkaus implements PostSijoitteluProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(PostSijoitteluProcessorPeruuntuneetHakemuksenVastaanotonMuokkaus.class);

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        LOG.info("Aloitetaan peruuntuneiden hakemusten vastaanottojen muokkaus.");
        peruuntuneetJaVastaanotetutValintatulokset(sijoitteluajoWrapper).forEach(v -> {
            LOG.info("Hakemus {} on PERUUNTUNUT ja valintatuloksentilassa {}, asetetaan valintatuloksentila KESKEN (hakukohde: {}, jono: {})",
                    v.getHakemusOid(), v.getTila(), v.getHakukohdeOid(), v.getValintatapajonoOid());
            poistaVastaanottoTieto(v);
            if (!sijoitteluajoWrapper.getMuuttuneetValintatulokset().contains(v)) {
                sijoitteluajoWrapper.addMuuttuneetValintatulokset(v);
            }
        });
        LOG.info("Lopetetaan peruuntuneiden hakemusten vastaanottojen muokkaus.");
    }

    private static boolean vastaanottanut(Valintatulos valintatulos) {
        return  ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI == valintatulos.getTila() ||
                ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT == valintatulos.getTila();
    }

    private static List<Valintatulos> peruuntuneetJaVastaanotetutValintatulokset(SijoitteluajoWrapper sijoitteluajoWrapper) {
        List<Valintatulos> peruuntuneetJaVastaanotetutValintatulokset = new ArrayList<>();
        sijoitteluajoWrapper.getHakukohteet().forEach(hk -> {
                Set<String> hakukohteenHyvaksytytHakemukset =  hk.getValintatapajonot().stream()
                        .flatMap(j -> j.getHakemukset().stream())
                        .filter(h -> HakemuksenTila.HYVAKSYTTY == h.getHakemus().getTila() || HakemuksenTila.VARASIJALTA_HYVAKSYTTY == h.getHakemus().getTila())
                        .map(h -> h.getHakemus().getHakemusOid())
                        .collect(Collectors.toSet());
                peruuntuneetJaVastaanotetutValintatulokset.addAll(hk.getValintatapajonot().stream()
                        .flatMap(j -> j.getHakemukset().stream())
                        .filter(h -> HakemuksenTila.PERUUNTUNUT == h.getHakemus().getTila() && !hakukohteenHyvaksytytHakemukset.contains(h.getHakemus().getHakemusOid()))
                        .map(HakemusWrapper::getValintatulos)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .filter(PostSijoitteluProcessorPeruuntuneetHakemuksenVastaanotonMuokkaus::vastaanottanut)
                        .collect(Collectors.toList())
                );
            }
        );
        return peruuntuneetJaVastaanotetutValintatulokset;
    }

    private static void poistaVastaanottoTieto(Valintatulos valintatulos) {
        valintatulos.setTila(ValintatuloksenTila.KESKEN, "Peruuntunut hakutoive");
        valintatulos.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY, "Peruuntunut hakutoive");
    }
}

