package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
                    valintatulosOpt.get().setTila(ValintatuloksenTila.KESKEN);
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
}

