package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
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
        final List<Hakemus> peruuntuneetHakemukset = sijoittelunJalkeenPeruuntuneetHakemukset(sijoitteluajoWrapper);
        peruuntuneetHakemukset.forEach(hakemus -> {
            final Optional<Valintatulos> valintatulosOpt = hakemuksenValintatulos(sijoitteluajoWrapper, hakemus);
            if (isValintatuloksentilaVastaanottanutTila(valintatulosOpt)) {
                LOG.info("Hakemus {} on PERUUNTUNUT ja valintatuloksentilassa {}, asetetaan valintatuloksentila KESKEN",
                        hakemus.getHakemusOid(), valintatulosOpt.get().getTila());
                valintatulosOpt.get().setTila(ValintatuloksenTila.KESKEN);
            }
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

    private List<Hakemus> sijoittelunJalkeenPeruuntuneetHakemukset(SijoitteluajoWrapper sijoitteluajoWrapper) {
        return sijoitteluajoWrapper.sijoitteluAjonHakukohteet()
                .flatMap(s -> s.getValintatapajonot().stream())
                .flatMap(j -> j.getHakemukset().stream().filter(this::peruuntunutHakemus))
                .collect(Collectors.toList());
    }

    private boolean peruuntunutHakemus(Hakemus h) {
        return h.getTila().equals(HakemuksenTila.PERUUNTUNUT);
    }

    private Optional<Valintatulos> hakemuksenValintatulos(SijoitteluajoWrapper sijoitteluajoWrapper, Hakemus hakemus) {
        if (sijoitteluajoWrapper.getMuuttuneetValintatulokset() == null) {
            return Optional.empty();
        } else {
            return sijoitteluajoWrapper.getMuuttuneetValintatulokset().stream()
                    .filter(vt -> vt.getHakemusOid() != null && vt.getHakemusOid().equals(hakemus.getHakemusOid()))
                    .findFirst();
        }
    }
}

