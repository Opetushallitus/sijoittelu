package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilanKuvaukset;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class PostSijoitteluProcessorEhdollisenVastaanotonSiirtyminenYlemmalleHakutoiveelle implements PostSijoitteluProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(PostSijoitteluProcessorEhdollisenVastaanotonSiirtyminenYlemmalleHakutoiveelle.class);

    @Override
    public String name() {
        return getClass().getSimpleName();
    }

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        final List<Hakemus> varasijaltaHyvaksytytHakemukset = sijoitteluAjossaVarasijaltaHyvaksytyt(sijoitteluajoWrapper);
        final Map<String, List<Hakemus>> varasijaltaHyvaksyttyjenHakijoidenKaikkiHakemukset = varasijaltaHyvaksyttyjenHakijoidenKaikkiHakemukset(sijoitteluajoWrapper, varasijaltaHyvaksytytHakemukset);
        varasijaltaHyvaksytytHakemukset.forEach(hakemus -> {
            final List<Valintatulos> hakijanKaikkiValintatulokset = hakijanKaikkiValintatulokset(sijoitteluajoWrapper, hakemus);
            final Optional<Valintatulos> hakemuksenValintatulos = hakemuksenValintatulos(hakemus, hakijanKaikkiValintatulokset);
            if (hakemuksenValintatulos.isPresent()) {
                final List<Hakemus> hakijanKaikkiHakemukset = varasijaltaHyvaksyttyjenHakijoidenKaikkiHakemukset.get(hakemus.getHakijaOid());
                final List<Hakemus> hakijanAlemmatHakemukset = hakijanAlemmatHakemukset(hakemus, hakijanKaikkiHakemukset);
                final List<Valintatulos> hakijanAlemmatValintatulokset = hakijanAlemmatValintatulokset(hakemus, hakijanKaikkiValintatulokset);

                if (hasPeruuntunutHakemusJonkaValintatulosEhdollisestiHyvaksytty(hakijanAlemmatHakemukset, hakijanAlemmatValintatulokset)) {
                    if (hakemuksenValintatulos.get().getHakutoive() == 1) {
                        LOG.info("Hakijalta {} löytynyt peruutunut alempi ehdollisesti hyväksytty hakemus, joten muutetaan korkeimman prioriteetin hakemus {} vastaanotetuksi sitovasti.", hakemus.getHakijaOid(), hakemus.getHakemusOid());
                        vastaanOtaSitovasti(hakemuksenValintatulos.get());
                    } else {
                        LOG.info("Hakijalta {} löytynyt peruutunut alempi ehdollisesti hyväksytty hakemus, joten muutetaan hakemus {} ehdollisesti vastaanotetuksi.", hakemus.getHakijaOid(), hakemus.getHakemusOid());
                        vastaanOtaEhdollisesti(hakemuksenValintatulos.get());
                    }
                    poistaAlemmatEhdollisetVastaanotot(hakemus, hakijanAlemmatValintatulokset);
                }
            }
        });
    }

    private boolean hasPeruuntunutHakemusJonkaValintatulosEhdollisestiHyvaksytty(List<Hakemus> hakijanAlemmatHakemukset, List<Valintatulos> hakijanAlemmatValintatulokset) {
        final Optional<Hakemus> alempiPeruuntunutHakemus = hakijanAlemmatHakemukset.stream().filter(alempiHakemus -> alempiHakemus.getTila().equals(HakemuksenTila.PERUUNTUNUT)).findFirst();
        if (alempiPeruuntunutHakemus.isPresent()) {
            final Optional<Valintatulos> vastaavaValintatulos = hakijanAlemmatValintatulokset.stream().filter(alempiValintatulos -> alempiValintatulos.getHakutoive() == alempiPeruuntunutHakemus.get().getPrioriteetti()).findFirst();
            if (vastaavaValintatulos.isPresent()) {
                return vastaavaValintatulos.get().getTila().equals(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT);
            }
        }
        return false;
    }

    private List<Hakemus> hakijanAlemmatHakemukset(Hakemus hakemus, List<Hakemus> hakijanKaikkiHakemukset) {
        return hakijanKaikkiHakemukset.stream().filter(muuHakemus -> muuHakemus.getPrioriteetti() > hakemus.getPrioriteetti()).collect(Collectors.toList());
    }

    private Map<String, List<Hakemus>> varasijaltaHyvaksyttyjenHakijoidenKaikkiHakemukset(SijoitteluajoWrapper sijoitteluajoWrapper, List<Hakemus> varasijaltaHyvaksytytHakemukset) {
        final List<String> varasijaltaHyvaksytytHakijat = varasijaltaHyvaksytytHakemukset.stream().map(Hakemus::getHakijaOid).collect(Collectors.toList());
        final Map<String, List<Hakemus>> hakijanKaikkiHakemukset = new HashMap<>();
        sijoitteluajoWrapper.sijoitteluAjonHakukohteet()
                .flatMap(hakukohde -> hakukohde.getValintatapajonot().stream())
                .flatMap(valintatapajono -> valintatapajono.getHakemukset().stream())
                .forEach(hakemus -> {
                    if (varasijaltaHyvaksytytHakijat.contains(hakemus.getHakijaOid())) {
                        if (hakijanKaikkiHakemukset.containsKey(hakemus.getHakijaOid())) {
                            hakijanKaikkiHakemukset.get(hakemus.getHakijaOid()).add(hakemus);
                        } else {
                            hakijanKaikkiHakemukset.put(hakemus.getHakijaOid(), new LinkedList<Hakemus>() {{ add(hakemus); }});
                        }
                    }
                });
        return hakijanKaikkiHakemukset;
    }

    private void poistaAlemmatEhdollisetVastaanotot(Hakemus hakemus, List<Valintatulos> hakijanAlemmatValintatulokset) {
        hakijanAlemmatValintatulokset.forEach(alempiValintatulos -> {
            if (alempiValintatulos.getTila().equals(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT)) {
                LOG.info("Poistetaan hakijalta {} alempi ehdollinen vastaanotto hakemukselta {}", alempiValintatulos.getHakijaOid(), alempiValintatulos.getHakemusOid());
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

    private Optional<Valintatulos> hakemuksenValintatulos(Hakemus hakemus, List<Valintatulos> hakijanKaikkiValintatulokset) {
        return hakijanKaikkiValintatulokset.stream()
                .filter(valintatulos -> valintatulos.getHakemusOid().equals(hakemus.getHakemusOid()))
                .findFirst();
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
