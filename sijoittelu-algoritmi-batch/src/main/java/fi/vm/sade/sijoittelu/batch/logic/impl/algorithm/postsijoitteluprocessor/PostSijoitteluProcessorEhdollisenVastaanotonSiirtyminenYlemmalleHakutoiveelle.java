package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilanKuvaukset;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HenkiloWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.*;
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

        LOG.info("Aloitetaan ehdollisten vastaanottojen siirtäminen ylemmille hakutoiveille.");
        final Map<String, List<Hakemus>> varasijaltaHyvaksytytHakemuksetValintatapajonoittain = sijoitteluAjossaVarasijaltaHyvaksytytValintapajonoittain(sijoitteluajoWrapper);
        final Map<String, List<Hakemus>> varasijaltaHyvaksyttyjenHakijoidenKaikkiHakemukset = varasijaltaHyvaksyttyjenHakijoidenKaikkiHakemukset(sijoitteluajoWrapper, sijoitteluAjossaVarasijaltaHyvaksytyt(sijoitteluajoWrapper));

        varasijaltaHyvaksytytHakemuksetValintatapajonoittain.keySet().forEach(valintatapajonoOid -> {
            List<Hakemus> varasijaltaHyvaksytytHakemukset = varasijaltaHyvaksytytHakemuksetValintatapajonoittain.get(valintatapajonoOid);

            if(varasijaltaHyvaksytytHakemukset.size() > 0) {
                String oidit = varasijaltaHyvaksytytHakemukset.stream().map(Hakemus::getHakemusOid).collect(Collectors.joining(", "));
                LOG.debug("Jonosta {} löytyi varasijalta hyväksyttyjä hakemuksia {} kpl ( {} )", valintatapajonoOid, varasijaltaHyvaksytytHakemukset.size(), oidit);
            }

            varasijaltaHyvaksytytHakemukset.forEach(hakemus -> {
                final List<Valintatulos> hakijanKaikkiValintatulokset = hakijanKaikkiValintatulokset(sijoitteluajoWrapper, hakemus);
                final Optional<Valintatulos> hakemuksenValintatulosOpt = hakemuksenValintatulos(hakemus, hakijanKaikkiValintatulokset, valintatapajonoOid);

                final List<Hakemus> hakijanKaikkiHakemukset = varasijaltaHyvaksyttyjenHakijoidenKaikkiHakemukset.get(hakemus.getHakijaOid());
                final List<Hakemus> hakijanAlemmatHakemukset = hakijanAlemmatHakemukset(hakemus, hakijanKaikkiHakemukset);
                final List<Valintatulos> hakijanAlemmatValintatulokset = hakijanAlemmatValintatulokset(hakemus, hakijanKaikkiValintatulokset);

                Optional<Valintatulos> ehdollinenValintatulosOpt = getPeruuntunutHakemusJonkaValintatulosEhdollisestiHyvaksytty(hakijanAlemmatHakemukset, hakijanAlemmatValintatulokset);

                if (ehdollinenValintatulosOpt.isPresent()) {

                    // TODO: Jos valintatulosta ei löydy, pitäisi luoda uusi
                    if(hakemuksenValintatulosOpt.isPresent()) {

                        Valintatulos hakemuksenValintatulos = hakemuksenValintatulosOpt.get();

                        if (hakemuksenValintatulos.getHakutoive() == 1) {
                            LOG.info("DRYRUN: Hakijalta {} löytynyt peruutunut alempi ehdollisesti hyväksytty hakemus, joten muutetaan korkeimman prioriteetin hakemus {} vastaanotetuksi sitovasti.", hakemus.getHakijaOid(), hakemus.getHakemusOid());
                            vastaanOtaSitovasti(hakemuksenValintatulos);
                        } else {
                            LOG.info("DRYRUN: Hakijalta {} löytynyt peruutunut alempi ehdollisesti hyväksytty hakemus, joten muutetaan hakemus {} ehdollisesti vastaanotetuksi.", hakemus.getHakijaOid(), hakemus.getHakemusOid());
                            vastaanOtaEhdollisesti(hakemuksenValintatulos);
                        }
                        if (!sijoitteluajoWrapper.getMuuttuneetValintatulokset().contains(hakemuksenValintatulos)) {
                            sijoitteluajoWrapper.getMuuttuneetValintatulokset().add(hakemuksenValintatulos);
                        }
                        sijoitteluajoWrapper.getMuuttuneetValintatulokset().add(ehdollinenValintatulosOpt.get());

                    } else {
                        LOG.warn("Valintatulosta ei löytynyt vaikka alemmalla hakutoiveella oli ehdollinen vastaanotto. Hakemus: {}, Valintatapajono: {}", hakemus.getHakemusOid(), valintatapajonoOid);
                    }
                }
            });

        });

        LOG.info("Lopetetaan ehdollisten vastaanottojen siirtäminen ylemmille hakutoiveille.");

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

    private Optional<Valintatulos> getPeruuntunutHakemusJonkaValintatulosEhdollisestiHyvaksytty(List<Hakemus> hakijanAlemmatHakemukset, List<Valintatulos> hakijanAlemmatValintatulokset) {
        final Optional<Hakemus> alempiPeruuntunutHakemus = hakijanAlemmatHakemukset.stream().filter(alempiHakemus -> alempiHakemus.getTila().equals(HakemuksenTila.PERUUNTUNUT)).findFirst();
        if (alempiPeruuntunutHakemus.isPresent()) {
            final Optional<Valintatulos> vastaavaValintatulos = hakijanAlemmatValintatulokset.stream().filter(alempiValintatulos -> alempiValintatulos.getHakutoive() == alempiPeruuntunutHakemus.get().getPrioriteetti()).findFirst();
            return vastaavaValintatulos;
        }
        return Optional.empty();
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

    private void vastaanOtaEhdollisesti(Valintatulos hakemuksenValintatulos) {
        paivitaVastaanottotieto(hakemuksenValintatulos, ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT);
    }

    private void vastaanOtaSitovasti(Valintatulos hakemuksenValintatulos) {
        paivitaVastaanottotieto(hakemuksenValintatulos, ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI);
    }

    private List<Valintatulos> hakijanAlemmatValintatulokset(Hakemus hakemus, List<Valintatulos> hakijanKaikkiValintatulokset) {
        return hakijanKaikkiValintatulokset.stream().filter(valintatulos -> valintatulos.getHakutoive() > hakemus.getPrioriteetti()).collect(Collectors.toList());
    }

    private Optional<Valintatulos> hakemuksenValintatulos(Hakemus hakemus, List<Valintatulos> hakijanKaikkiValintatulokset, String valintatapajonoOid) {
        return hakijanKaikkiValintatulokset.stream()
                .filter(valintatulos -> valintatulos.getHakemusOid().equals(hakemus.getHakemusOid()) && valintatulos.getValintatapajonoOid().equals(valintatapajonoOid))
                .findFirst();
    }

    private List<Hakemus> sijoitteluAjossaVarasijaltaHyvaksytyt(SijoitteluajoWrapper sijoitteluajoWrapper) {
        return sijoitteluajoWrapper.sijoitteluAjonHakukohteet()
                .flatMap(s -> s.getValintatapajonot().stream())
                .flatMap(j -> j.getHakemukset().stream().filter(this::muuttunutVaraltaVarasijaltaHyvaksytyksi))
                .collect(Collectors.toList());
    }

    private Map<String, List<Hakemus>> sijoitteluAjossaVarasijaltaHyvaksytytValintapajonoittain(SijoitteluajoWrapper sijoitteluajoWrapper) {
        return sijoitteluajoWrapper.sijoitteluAjonHakukohteet()
                .flatMap(s -> s.getValintatapajonot().stream())
                .collect(Collectors.toMap(
                        Valintatapajono::getOid,
                        j -> j.getHakemukset().stream().filter(this::muuttunutVaraltaVarasijaltaHyvaksytyksi).collect(Collectors.toList())
                ));
    }

    private boolean muuttunutVaraltaVarasijaltaHyvaksytyksi(Hakemus h) {
        return h.getTila().equals(HakemuksenTila.VARASIJALTA_HYVAKSYTTY) && h.getEdellinenTila().equals(HakemuksenTila.VARALLA);
    }

    private List<Valintatulos> hakijanKaikkiValintatulokset(SijoitteluajoWrapper sijoitteluajoWrapper, Hakemus hakemus) {

        Optional<HenkiloWrapper> henkiloWrapperOpt = sijoitteluajoWrapper.getHakukohteet().stream()
                .flatMap(hk -> hk.hakukohteenHakijat())
                .filter(h -> h.getHakemusOid() != null && h.getHakemusOid().equals(hakemus.getHakemusOid())).findFirst();

        if(henkiloWrapperOpt.isPresent()) {
            return henkiloWrapperOpt.get().getValintatulos();
        } else {
            return new LinkedList<>();
        }
    }

    private void paivitaVastaanottotieto(Valintatulos valintatulos, ValintatuloksenTila tila) {
        valintatulos.setTila(tila, "Vastaanottotieto peritynyt alemmalta hakutoiveelta");
    }
}
