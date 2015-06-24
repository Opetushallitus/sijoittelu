package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilanKuvaukset;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;

import java.util.*;
import java.util.stream.Collectors;

public class PostSijoitteluProcessorEhdollisenVastaanotonSiirtyminenYlemmalleHakutoiveelle implements PostSijoitteluProcessor {
    @Override
    public String name() {
        return getClass().getSimpleName();
    }

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        final List<Hakemus> varasijaltaHyvaksytytHakemukset = sijoitteluAjossaVarasijaltaHyvaksytyt(sijoitteluajoWrapper);
        varasijaltaHyvaksytytHakemukset.forEach(hakemus -> {
            final List<Valintatulos> hakijanKaikkiValintatulokset = hakijanKaikkiValintatulokset(sijoitteluajoWrapper, hakemus);
            final Valintatulos hakemuksenValintatulos = hakemuksenValintatulos(hakemus, hakijanKaikkiValintatulokset);
            final List<Valintatulos> hakijanAlemmatValintatulokset = hakijanAlemmatValintatulokset(hakemus, hakijanKaikkiValintatulokset);

            if (hasEhdollisestiVastaanotettuAlempiHakutoive(hakijanAlemmatValintatulokset)) {
                if (hakemuksenValintatulos.getHakutoive() == 0) {
                    vastaanOtaEhdollisesti(hakemuksenValintatulos);
                } else {
                    vastaanOtaSitovasti(hakemuksenValintatulos);
                }
                poistaAlemmatEhdollisetVastaanotot(hakemus, hakijanAlemmatValintatulokset);
            }
        });
    }

    private void poistaAlemmatEhdollisetVastaanotot(Hakemus hakemus, List<Valintatulos> hakijanAlemmatValintatulokset) {
        hakijanAlemmatValintatulokset.forEach(alempiValintatulos -> {
            if (alempiValintatulos.getTila().equals(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT)) {
                alempiValintatulos.setTila(ValintatuloksenTila.PERUUTETTU);
                hakemus.setTilanKuvaukset(TilanKuvaukset.peruuntunutYlempiToive());
            }
        });
    }

    private void vastaanOtaEhdollisesti(Valintatulos hakemuksenValintatulos) {
        hakemuksenValintatulos.setTila(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT);
    }

    private void vastaanOtaSitovasti(Valintatulos hakemuksenValintatulos) {
        hakemuksenValintatulos.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI);
    }

    private List<Valintatulos> hakijanAlemmatValintatulokset(Hakemus hakemus, List<Valintatulos> hakijanKaikkiValintatulokset) {
        return hakijanKaikkiValintatulokset.stream().filter(valintatulos -> valintatulos.getHakutoive() > hakemus.getPrioriteetti()).collect(Collectors.toList());
    }

    private Valintatulos hakemuksenValintatulos(Hakemus hakemus, List<Valintatulos> hakijanKaikkiValintatulokset) {
        return hakijanKaikkiValintatulokset.stream()
                .filter(valintatulos -> valintatulos.getHakemusOid().equals(hakemus.getHakemusOid()))
                .findFirst().get();
    }

    private boolean hasEhdollisestiVastaanotettuAlempiHakutoive(List<Valintatulos> hakijanAlemmatValintatulokset) {
        return hakijanAlemmatValintatulokset.stream().anyMatch(valintatulos -> valintatulos.getTila().equals(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT));
    }

    private List<Hakemus> sijoitteluAjossaVarasijaltaHyvaksytyt(SijoitteluajoWrapper sijoitteluajoWrapper) {
        return sijoitteluajoWrapper.sijoitteluAjonHakukohteet()
                .flatMap(s -> s.getValintatapajonot().stream())
                .flatMap(j -> j.getHakemukset().stream().filter(this::muuttunutVaraltaVarasijaltaHyvaksytyksi))
                .collect(Collectors.toList());
    }

    private boolean muuttunutVaraltaVarasijaltaHyvaksytyksi(Hakemus h) {
        return h.getTila().equals(HakemuksenTila.VARASIJALTA_HYVAKSYTTY) && h.getEdellinenTila().equals(HakemuksenTila.VARALLA);
    }

    private List<Valintatulos> hakijanKaikkiValintatulokset(SijoitteluajoWrapper sijoitteluajoWrapper, Hakemus hakemus) {
        return sijoitteluajoWrapper.getMuuttuneetValintatulokset().stream()
                .filter(vt -> vt.getHakijaOid().equals(hakemus.getHakijaOid()))
                .collect(Collectors.toList());
    }
}
