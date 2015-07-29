package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PostSijoitteluProcessorPeruuntuneetHakemuksenVastaanotonMuokkaus implements PostSijoitteluProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(PostSijoitteluProcessorPeruuntuneetHakemuksenVastaanotonMuokkaus.class);

    @Override
    public String name() {
        return getClass().getSimpleName();
    }

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
        return ValintatuloksenTila.VASTAANOTTANUT == valintatulos.getTila() ||
                ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI == valintatulos.getTila() ||
                ValintatuloksenTila.VASTAANOTTANUT_LASNA == valintatulos.getTila() ||
                ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT == valintatulos.getTila() ||
                ValintatuloksenTila.VASTAANOTTANUT_POISSAOLEVA == valintatulos.getTila();
    }

    private static List<Valintatulos> peruuntuneetJaVastaanotetutValintatulokset(SijoitteluajoWrapper sijoitteluajoWrapper) {
        return sijoitteluajoWrapper.getHakukohteet().stream()
                .flatMap(hk -> hk.getValintatapajonot().stream())
                .flatMap(j -> j.getHakemukset().stream())
                .filter(h -> HakemuksenTila.PERUUNTUNUT == h.getHakemus().getTila())
                .map(HakemusWrapper::getValintatulos)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(PostSijoitteluProcessorPeruuntuneetHakemuksenVastaanotonMuokkaus::vastaanottanut)
                .collect(Collectors.toList());
    }

    private static void poistaVastaanottoTieto(Valintatulos valintatulos) {
        valintatulos.setTila(ValintatuloksenTila.KESKEN);
        valintatulos.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY);
        valintatulos.setHyvaksyttyVarasijalta(false);
        valintatulos.getLogEntries().add(createLogEntry(valintatulos.getTila() + " -> " + ValintatuloksenTila.KESKEN,
                "Poistettu vastaanottotieto koska peruuntunut"));
    }

    private static LogEntry createLogEntry(String muutos, String selite) {
        LogEntry logEntry = new LogEntry();
        logEntry.setLuotu(new Date());
        logEntry.setMuokkaaja("sijoittelu");
        logEntry.setMuutos(muutos);
        logEntry.setSelite(selite);
        return logEntry;
    }
}

