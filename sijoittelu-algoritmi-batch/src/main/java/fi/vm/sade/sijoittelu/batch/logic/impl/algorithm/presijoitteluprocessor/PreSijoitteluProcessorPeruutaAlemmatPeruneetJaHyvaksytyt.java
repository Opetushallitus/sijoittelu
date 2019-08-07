package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.HYVAKSYTTY;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.PERUNUT;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.PERUUTETTU;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.VARALLA;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.VARASIJALTA_HYVAKSYTTY;
import static fi.vm.sade.sijoittelu.domain.IlmoittautumisTila.EI_TEHTY;
import static fi.vm.sade.sijoittelu.domain.ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT;
import static fi.vm.sade.sijoittelu.domain.ValintatuloksenTila.KESKEN;
import static fi.vm.sade.sijoittelu.domain.ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilojenMuokkaus;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HenkiloWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.IlmoittautumisTila;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Peruutetaan jo jonnekkin hyväksyttyjen alemmat hakemukset.
 * Kaytannossa tata tarvitaan jos algoritmi on tyytyvainen nykytilanteeseeen eika koskaan muuta henkilon tilaa, jolloin varalla olevat jaavat koskemattomiksi.
 */
public class PreSijoitteluProcessorPeruutaAlemmatPeruneetJaHyvaksytyt implements PreSijoitteluProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(PreSijoitteluProcessorPeruutaAlemmatPeruneetJaHyvaksytyt.class);

    private static final List<ValintatuloksenTila> vastaanotot = Arrays.asList(EHDOLLISESTI_VASTAANOTTANUT, VASTAANOTTANUT_SITOVASTI);
    private static final List<HakemuksenTila> yliajettavat = Arrays.asList(HYVAKSYTTY, VARALLA, VARASIJALTA_HYVAKSYTTY);

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        Set<HenkiloWrapper> henkilot = getHenkiloWrappers(sijoitteluajoWrapper);
        henkilot.forEach(henkilo -> {
            Optional<Valintatulos> sitovaOpt = sitovastiVastaanottanut(henkilo);
            Optional<Valintatulos> ehdollinenOpt = ehdollisestiVastaanottanut(henkilo);
            if (sitovaOpt.isPresent() && sijoitteluajoWrapper.isKKHaku()) {
                peruutaMuutKuinSitovastiVastaanotettuHakemus(sijoitteluajoWrapper, henkilo, sitovaOpt.get());
            }
            else if (ehdollinenOpt.isPresent() && sijoitteluajoWrapper.isKKHaku()) {
                peruutaAlemmatHakemukset(sijoitteluajoWrapper, henkilo, ehdollinenOpt.get());
            }
            HakemusWrapper parasHyvaksyttyHakutoive = parasHyvaksyttyTaiPeruttuHakutoive(henkilo);
            if (sijoitteluajoWrapper.paivamaaraOhitettu()) {
                // Päivämäärä jolloin kaikki tulokset pitää olla siirrettynä sijoitteluun on ohitettu
                // Ei peruta enää hyväksyttyjä ja julkaistavissa olevia
                henkilo.getHakemukset().forEach(kasitteleHakemuksetKunKaikkiJonotSijoittelussa(sijoitteluajoWrapper, henkilo, parasHyvaksyttyHakutoive, ehdollinenOpt));
            } else {
                // Kaikki jonot ei vielä sijoittelussa, yliajetaan tylysti kaikki alemmat hyväksytyt ja varalla olot
                henkilo.getHakemukset().forEach(kasitteleHakemuksetKunKaikkiJonotEiVielaSijoittelussa(sijoitteluajoWrapper, henkilo, parasHyvaksyttyHakutoive));
            }
        });
    }

    private Consumer<HakemusWrapper> kasitteleHakemuksetKunKaikkiJonotSijoittelussa(SijoitteluajoWrapper sijoitteluajoWrapper, HenkiloWrapper henkilo, HakemusWrapper parasHyvaksyttyHakutoive, Optional<Valintatulos> ehdollinenOpt) {
        return hakemus -> {
            Hakemus h = hakemus.getHakemus();
            if (parasHyvaksyttyHakutoive != null) {
                if (hakemus.isTilaVoidaanVaihtaa() && h.getTila() == VARALLA && h.getPrioriteetti() > parasHyvaksyttyHakutoive.getHakemus().getPrioriteetti()) {
                    peruutaHakemusKoskaPeruuntunutYlempiToive(hakemus);
                } else {
                    if (hakemus.isTilaVoidaanVaihtaa() && h.getPrioriteetti().equals(parasHyvaksyttyHakutoive.getHakemus().getPrioriteetti())) {
                        // Jos tila on Perunut se periytetään kaikkiin jonoihin
                        if (parasHyvaksyttyHakutoive.getHakemus().getTila() == PERUNUT) {
                            periTila(hakemus, parasHyvaksyttyHakutoive);
                        } else if (parasHyvaksyttyHakutoive.getHakemus().getTila() == PERUUTETTU) {
                            periTila(hakemus, parasHyvaksyttyHakutoive);
                        }
                        // Hyväksytyltä laitetaan peruuntuneiksi huonomman prioriteetin jonot
                        else if (parasHyvaksyttyHakutoive.getValintatapajono().getValintatapajono().getPrioriteetti() < hakemus.getValintatapajono().getValintatapajono().getPrioriteetti()) {
                            peruutaHakemusKoskaHyvaksyttyToisessaJonossa(hakemus);
                        }
                    }
                    // Paras toive PERUNUT, toisessa jonossa hyväksytty
                    else if (!hakemus.isTilaVoidaanVaihtaa()
                            && h.getPrioriteetti().equals(parasHyvaksyttyHakutoive.getHakemus().getPrioriteetti())
                            && parasHyvaksyttyHakutoive.getHakemus().getTila() == PERUNUT
                            && yliajettavat.contains(h.getTila())) {
                        // Ei vastaanottoa, voidaan yliajaa
                        if (!vastaanOttoPerutussaKohteessa(henkilo, hakemus)) {
                            periTila(hakemus, parasHyvaksyttyHakutoive);
                        }
                    }
                    // Paras toive PERUUTETTU, toisessa jonossa hyväksytty
                    else if (!hakemus.isTilaVoidaanVaihtaa()
                            && h.getPrioriteetti().equals(parasHyvaksyttyHakutoive.getHakemus().getPrioriteetti())
                            && parasHyvaksyttyHakutoive.getHakemus().getTila() == PERUUTETTU
                            && yliajettavat.contains(h.getTila())) {
                        // Ei vastaanottoa, voidaan yliajaa
                        if (!vastaanOttoPerutussaKohteessa(henkilo, hakemus)) {
                            periTila(hakemus, parasHyvaksyttyHakutoive);
                        }
                    }
                    // Perutaan myös ehdollinen vastaanotto
                    else if (h.getPrioriteetti() > parasHyvaksyttyHakutoive.getHakemus().getPrioriteetti()
                            && ehdollinenOpt.isPresent()
                            && ehdollinenOpt.get().getValintatapajonoOid().equals(hakemus.getValintatapajono().getValintatapajono().getOid())) {
                        peruutaHakemusKoskaPeruuntunutYlempiToive(hakemus);
                        Optional<Valintatulos> nykyinenTulos = henkilo.getValintatulos().stream().filter(v -> v.getValintatapajonoOid().equals(hakemus.getValintatapajono().getValintatapajono().getOid())).findFirst();
                        if (nykyinenTulos.isPresent()) {
                            lisaaMuokattavaValintatulos(sijoitteluajoWrapper, nykyinenTulos.get());
                        }
                    }
                }
            }
        };
    }

    private Consumer<HakemusWrapper> kasitteleHakemuksetKunKaikkiJonotEiVielaSijoittelussa(SijoitteluajoWrapper sijoitteluajoWrapper, HenkiloWrapper henkilo, HakemusWrapper parasHyvaksyttyHakutoive) {
        return hakemus -> {
            Hakemus h = hakemus.getHakemus();
            if (parasHyvaksyttyHakutoive != null) {
                if (yliajettavat.contains(h.getTila()) && h.getPrioriteetti() > parasHyvaksyttyHakutoive.getHakemus().getPrioriteetti()) {
                    peruutaHakemusKoskaPeruuntunutYlempiToive(hakemus);
                    Optional<Valintatulos> nykyinenTulos = henkilo.getValintatulos().stream().filter(v -> v.getValintatapajonoOid().equals(hakemus.getValintatapajono().getValintatapajono().getOid())).findFirst();
                    if (nykyinenTulos.isPresent()) {
                        lisaaMuokattavaValintatulos(sijoitteluajoWrapper, nykyinenTulos.get());
                    }
                } else if (yliajettavat.contains(h.getTila()) && h.getPrioriteetti().equals(parasHyvaksyttyHakutoive.getHakemus().getPrioriteetti())) {
                    // Jos tila on Perunut se periytetään kaikkiin jonoihin
                    if (parasHyvaksyttyHakutoive.getHakemus().getTila() == PERUNUT) {
                        periTila(hakemus, parasHyvaksyttyHakutoive);
                    }
                    // Jos tila on PERUUTETTU se periytetään kaikkiin jonoihin
                    else if (parasHyvaksyttyHakutoive.getHakemus().getTila() == PERUUTETTU) {
                        periTila(hakemus, parasHyvaksyttyHakutoive);
                    }
                    // Hyväksytyltä laitetaan peruuntuneiksi huonomman prioriteetin jonot
                    else if (parasHyvaksyttyHakutoive.getValintatapajono().getValintatapajono().getPrioriteetti() < hakemus.getValintatapajono().getValintatapajono().getPrioriteetti()) {
                        peruutaHakemusKoskaHyvaksyttyToisessaJonossa(hakemus);
                    }
                }
            }
        };
    }

    private void peruutaAlemmatHakemukset(SijoitteluajoWrapper sijoitteluajoWrapper, HenkiloWrapper henkilo, Valintatulos ehdollinen) {
        HakemusWrapper ehdollinenHakemus = henkilo.getHakemukset()
                .stream()
                .filter(h -> h.getValintatapajono().getValintatapajono().getOid().equals(ehdollinen.getValintatapajonoOid()))
                .findFirst().get();
        henkilo.getHakemukset().forEach(hakemus -> {
            Hakemus h = hakemus.getHakemus();
            if (yliajettavat.contains(h.getTila())
                    && !hakemus.getValintatapajono().getValintatapajono().getOid().equals(ehdollinen.getValintatapajonoOid())
                    && h.getPrioriteetti() > ehdollinenHakemus.getHakemus().getPrioriteetti()) {
                peruutaHakemusKoskaPeruuntunutYlempiToive(hakemus);
                Optional<Valintatulos> nykyinenTulos = henkilo.getValintatulos().stream().filter(v -> v.getValintatapajonoOid().equals(hakemus.getValintatapajono().getValintatapajono().getOid())).findFirst();
                if (nykyinenTulos.isPresent()) {
                    lisaaMuokattavaValintatulos(sijoitteluajoWrapper, nykyinenTulos.get());
                }
            }
        });
    }

    private void peruutaMuutKuinSitovastiVastaanotettuHakemus(SijoitteluajoWrapper sijoitteluajoWrapper, HenkiloWrapper henkilo, Valintatulos sitova) {
        HakemusWrapper sitovaHakemus = henkilo.getHakemukset()
                .stream()
                .filter(h -> h.getValintatapajono().getValintatapajono().getOid().equals(sitova.getValintatapajonoOid()))
                .findFirst().get();
        henkilo.getHakemukset().forEach(hakemus -> {
            Hakemus h = hakemus.getHakemus();
            if (yliajettavat.contains(h.getTila())
                    && !hakemus.getValintatapajono().getValintatapajono().getOid().equals(sitova.getValintatapajonoOid())) {
                if (!hakemus.getHakemus().getPrioriteetti().equals(sitovaHakemus.getHakemus().getPrioriteetti())) {
                    peruutaHakemusKoskaVastaanottanutToisenOpiskelupaikan(hakemus);
                    Optional<Valintatulos> nykyinenTulos = henkilo.getValintatulos().stream().filter(v -> v.getValintatapajonoOid().equals(hakemus.getValintatapajono().getValintatapajono().getOid())).findFirst();
                    if (nykyinenTulos.isPresent()) {
                        lisaaMuokattavaValintatulos(sijoitteluajoWrapper, nykyinenTulos.get());
                    }
                } else if (sitovaHakemus.getValintatapajono().getValintatapajono().getPrioriteetti() < hakemus.getValintatapajono().getValintatapajono().getPrioriteetti()) {
                    peruutaHakemusKoskaHyvaksyttyToisessaJonossa(hakemus);
                    /*
                    Optional<Valintatulos> nykyinenTulos = henkilo.getValintatulos().stream().filter(v -> v.getValintatapajonoOid().equals(hakemus.getValintatapajono().getValintatapajono().getOid())).findFirst();
                    if (nykyinenTulos.isPresent()) {
                        lisaaMuokattavaValintatulos(sijoitteluajoWrapper, nykyinenTulos.get());
                    }
                    */
                }

            }
        });
    }

    private void peruutaHakemusKoskaPeruuntunutYlempiToive(HakemusWrapper hakemus) {
        TilojenMuokkaus.asetaTilaksiPeruuntunutYlempiToive(hakemus);
        hakemus.setTilaVoidaanVaihtaa(false);
    }

    private void peruutaHakemusKoskaHyvaksyttyToisessaJonossa(HakemusWrapper hakemus) {
        TilojenMuokkaus.asetaTilaksiPeruuntunutToinenJono(hakemus);
        hakemus.setTilaVoidaanVaihtaa(false);
    }

    private void peruutaHakemusKoskaVastaanottanutToisenOpiskelupaikan(HakemusWrapper hakemus) {
        TilojenMuokkaus.asetaTilaksiPeruuntunutVastaanottanutToisenPaikan(hakemus);
        hakemus.setTilaVoidaanVaihtaa(false);
    }

    private void periTila(HakemusWrapper perija, HakemusWrapper perittava) {
        TilojenMuokkaus.periTila(perija, perittava);
        perija.setTilaVoidaanVaihtaa(false);
    }

    private Set<HenkiloWrapper> getHenkiloWrappers(SijoitteluajoWrapper sijoitteluajoWrapper) {
        Set<HenkiloWrapper> henkilot = new HashSet<>();
        sijoitteluajoWrapper.getHakukohteet().forEach(hakukohdeWrapper -> {
            hakukohdeWrapper.getValintatapajonot().forEach(valintatapajonoWrapper -> {
                valintatapajonoWrapper.getHakemukset().forEach(hakemusWrapper -> {
                    if (!henkilot.contains(hakemusWrapper.getHenkilo())) {
                        henkilot.add(hakemusWrapper.getHenkilo());
                    }
                });
            });
        });
        return henkilot;
    }

    private void lisaaMuokattavaValintatulos(SijoitteluajoWrapper sijoitteluajoWrapper, Valintatulos nykyinen) {
        IlmoittautumisTila uusiIlmoittautumistila = EI_TEHTY;
        ValintatuloksenTila uusiValintatuloksenTila = KESKEN;
        boolean uusiHyvaksyttyVarasijaltaArvo = false;
        boolean muokattavatArvotOvatSamatKuinEnnen =
            uusiIlmoittautumistila.equals(nykyinen.getIlmoittautumisTila()) &&
            uusiValintatuloksenTila.equals(nykyinen.getTila()) &&
            uusiHyvaksyttyVarasijaltaArvo == nykyinen.getHyvaksyttyVarasijalta();
        if (muokattavatArvotOvatSamatKuinEnnen) {
            LOG.debug(String.format("Ei lisätä valintatulosta muuttuneisiin, koska muokattavat arvot ovat samat kuin ennen. Valintatulos: %s", nykyinen));
        } else {
            nykyinen.setIlmoittautumisTila(uusiIlmoittautumistila, "Peruutettu alempi hakutoive");
            nykyinen.setTila(uusiValintatuloksenTila, "Peruutettu alempi hakutoive");
            nykyinen.setHyvaksyttyVarasijalta(uusiHyvaksyttyVarasijaltaArvo, "Peruutettu alempi hakutoive");
            sijoitteluajoWrapper.addMuuttuneetValintatulokset(nykyinen);
        }
    }

    private Optional<Valintatulos> sitovastiVastaanottanut(HenkiloWrapper henkiloWrapper) {
        return henkiloWrapper.getValintatulos()
                .stream()
                .filter(v -> v.getTila().equals(VASTAANOTTANUT_SITOVASTI))
                .findFirst();
    }

    private Optional<Valintatulos> ehdollisestiVastaanottanut(HenkiloWrapper henkiloWrapper) {
        return henkiloWrapper.getValintatulos()
                .stream()
                .filter(v -> v.getTila().equals(EHDOLLISESTI_VASTAANOTTANUT))
                .findFirst();
    }

    private boolean vastaanOttoPerutussaKohteessa(HenkiloWrapper henkiloWrapper, HakemusWrapper hakemusWrapper) {
        return henkiloWrapper.getValintatulos()
                .stream()
                .filter(v -> v.getHakemusOid().equals(hakemusWrapper.getHakemus().getHakemusOid())
                        && v.getHakukohdeOid().equals(hakemusWrapper.getValintatapajono().getHakukohdeWrapper().getHakukohde().getOid()))
                .anyMatch(v -> vastaanotot.contains(v.getTila()));
    }

    private HakemusWrapper parasHyvaksyttyTaiPeruttuHakutoive(HenkiloWrapper wrapper) {
        HakemusWrapper parasHyvaksyttyHakutoive = null;
        for (HakemusWrapper hakemusWrapper : wrapper.getHakemukset()) {
            if (hakemusWrapper.getHakemus().getTila() == HYVAKSYTTY
                    || hakemusWrapper.getHakemus().getTila() == PERUNUT
                    || hakemusWrapper.getHakemus().getTila() == PERUUTETTU
                    || hakemusWrapper.getHakemus().getTila() == VARASIJALTA_HYVAKSYTTY) {
                if (parasHyvaksyttyHakutoive == null || parasHyvaksyttyHakutoive.getHakemus().getPrioriteetti() > hakemusWrapper.getHakemus().getPrioriteetti()) {
                    parasHyvaksyttyHakutoive = hakemusWrapper;
                } else if (parasHyvaksyttyHakutoive.getHakemus().getPrioriteetti().equals(hakemusWrapper.getHakemus().getPrioriteetti()) && (hakemusWrapper.getHakemus().getTila() == PERUNUT && hakemusWrapper.getHakemus().getTila() == PERUUTETTU)) {
                    // PERUNUT tai PERUUTETTU tila yliajaa hyväksynnät
                    parasHyvaksyttyHakutoive = hakemusWrapper;
                }
            }
        }
        return parasHyvaksyttyHakutoive;
    }
}
