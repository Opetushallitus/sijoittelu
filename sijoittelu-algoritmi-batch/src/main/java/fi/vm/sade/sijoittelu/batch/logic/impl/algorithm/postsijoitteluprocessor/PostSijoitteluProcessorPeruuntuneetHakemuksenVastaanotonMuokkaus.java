package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class PostSijoitteluProcessorPeruuntuneetHakemuksenVastaanotonMuokkaus implements PostSijoitteluProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(PostSijoitteluProcessorEhdollisenVastaanotonSiirtyminenYlemmalleHakutoiveelle.class);

    @Override
    public String name() {
        return getClass().getSimpleName();
    }

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        final Map<String, List<Hakemus>> peruuntuneetHakemuksetValintapajonoittain = sijoittelunJalkeenPeruuntuneetHakemuksetValintapajonoittain(sijoitteluajoWrapper);

        peruuntuneetHakemuksetValintapajonoittain.keySet().forEach(valintatapajonoOid -> {
            peruuntuneetHakemuksetValintapajonoittain.get(valintatapajonoOid).forEach(hakemus -> {
                final Optional<Valintatulos> valintatulosOpt = hakemuksenValintatulos(sijoitteluajoWrapper, hakemus, valintatapajonoOid);
                if (isValintatuloksentilaVastaanottanutTila(valintatulosOpt)) {
                    LOG.info("Hakemus {} on PERUUNTUNUT ja valintatuloksentilassa {}, asetetaan valintatuloksentila KESKEN",
                            hakemus.getHakemusOid(), valintatulosOpt.get().getTila());
                    poistaVastaanottoTieto(valintatulosOpt.get());
                }
            });
        });

    }

    private boolean isValintatuloksentilaVastaanottanutTila(Optional<Valintatulos> valintatulosOpt) {
        return valintatulosOpt.isPresent() && (
                valintatulosOpt.get().getTila().equals(ValintatuloksenTila.VASTAANOTTANUT) ||
                valintatulosOpt.get().getTila().equals(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI) ||
                valintatulosOpt.get().getTila().equals(ValintatuloksenTila.VASTAANOTTANUT_LASNA) ||
                valintatulosOpt.get().getTila().equals(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT) ||
                valintatulosOpt.get().getTila().equals(ValintatuloksenTila.VASTAANOTTANUT_POISSAOLEVA));
    }

    private Map<String, List<Hakemus>> sijoittelunJalkeenPeruuntuneetHakemuksetValintapajonoittain(SijoitteluajoWrapper sijoitteluajoWrapper) {

        Map<String, List<Hakemus>> hakemuksetValintatapajonoittain = new HashMap<>();

        List<Valintatapajono> valintatapajonot = sijoitteluajoWrapper.sijoitteluAjonHakukohteet().flatMap(h -> h.getValintatapajonot().stream()).collect(Collectors.toList());
        valintatapajonot.forEach(valintatapajono -> {
            List<Hakemus> hakemukset = valintatapajono.getHakemukset().stream().filter(this::peruuntunutHakemus).collect(Collectors.toList());
            hakemuksetValintatapajonoittain.put(valintatapajono.getOid(), hakemukset);
        });

        return hakemuksetValintatapajonoittain;

    }

    private boolean peruuntunutHakemus(Hakemus h) {
        return h.getTila().equals(HakemuksenTila.PERUUNTUNUT);
    }

    private Optional<Valintatulos> hakemuksenValintatulos(SijoitteluajoWrapper sijoitteluajoWrapper, Hakemus hakemus, String valintatapajonoOid) {
        if (sijoitteluajoWrapper.getMuuttuneetValintatulokset() == null) {
            return Optional.empty();
        } else {
            return sijoitteluajoWrapper.getMuuttuneetValintatulokset().stream()
                    .filter(vt -> vt.getHakemusOid() != null && vt.getHakemusOid().equals(hakemus.getHakemusOid()) && vt.getValintatapajonoOid().equals(valintatapajonoOid))
                    .findFirst();
        }
    }

    private void poistaVastaanottoTieto(Valintatulos valintatulos) {

        if(!ValintatuloksenTila.KESKEN.equals(valintatulos.getTila())) {
            valintatulos.setTila(ValintatuloksenTila.KESKEN);
            valintatulos.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY);
            valintatulos.setHyvaksyttyVarasijalta(false);
            valintatulos.getLogEntries().add(createLogEntry(ValintatuloksenTila.KESKEN, "Poistettu vastaanottotieto koska peruuntunut"));
        }
    }

    private LogEntry createLogEntry(ValintatuloksenTila tila, String selite) {
        LogEntry logEntry = new LogEntry();
        logEntry.setLuotu(new Date());
        logEntry.setMuokkaaja("sijoittelu");
        logEntry.setSelite(selite);
        if (tila == null) {
            logEntry.setMuutos("");
        } else {
            logEntry.setMuutos(tila.name());
        }
        return logEntry;
    }
}

