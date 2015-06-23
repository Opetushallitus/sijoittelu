package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilanKuvaukset;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.*;
import fi.vm.sade.sijoittelu.domain.*;

import java.util.*;

/**
 * Peruutetaan jo jonnekkin hyväksyttyjen alemmat hakemukset.
 * Kaytannossa tata tarvitaan jos algoritmi on tyytyvainen nykytilanteeseeen eika koskaan muuta henkilon tilaa, jolloin varalla olevat jaavat koskemattomiksi.
 */
public class PreSijoitteluProcessorPeruutaAlemmatPeruneetJaHyvaksytyt implements PreSijoitteluProcessor {
    private final List<ValintatuloksenTila> vastaanotot = Arrays.asList(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT, ValintatuloksenTila.VASTAANOTTANUT, ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI);
    private final List<HakemuksenTila> yliajettavat = Arrays.asList(HakemuksenTila.HYVAKSYTTY, HakemuksenTila.VARALLA, HakemuksenTila.VARASIJALTA_HYVAKSYTTY);

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        Set<HenkiloWrapper> henkilot = getHenkiloWrappers(sijoitteluajoWrapper);
        henkilot.forEach(henkilo -> {
            Optional<Valintatulos> sitovaOpt = sitovastiVastaanottanut(henkilo);
            HakemusWrapper parasHyvaksyttyHakutoive = parasHyvaksyttyTaiPeruttuHakutoive(henkilo);
            Optional<Valintatulos> ehdollinenOpt = ehdollisestiVastaanottanut(henkilo);
            if (sitovaOpt.isPresent()) {
                peruutaMuutKuinSitovastiVastaanotettuHakemus(sijoitteluajoWrapper, henkilo, sitovaOpt.get());
            }
            else if (ehdollinenOpt.isPresent()) {
                peruutaAlemmatHakemukset(sijoitteluajoWrapper, henkilo, parasHyvaksyttyHakutoive, ehdollinenOpt);
            }
            // Päivämäärä jolloin kaikki tulokset pitää olla siirrettynä sijoitteluun on ohitettu
            // Ei peruta enää hyväksyttyjä ja julkaistavissa olevia
            if (sijoitteluajoWrapper.paivamaaraOhitettu()) {
                henkilo.getHakemukset().forEach(hakemus -> {
                    Hakemus h = hakemus.getHakemus();
                    if (parasHyvaksyttyHakutoive != null) {
                        if (hakemus.isTilaVoidaanVaihtaa() && h.getTila() == HakemuksenTila.VARALLA && h.getPrioriteetti() > parasHyvaksyttyHakutoive.getHakemus().getPrioriteetti()) {
                            peruutaHakemusKoskaPeruuntunutYlempiToive(hakemus, h);
                        } else if (hakemus.isTilaVoidaanVaihtaa() && h.getPrioriteetti().equals(parasHyvaksyttyHakutoive.getHakemus().getPrioriteetti())) {
                            // Jos tila on Perunut se periytetään kaikkiin jonoihin
                            if (parasHyvaksyttyHakutoive.getHakemus().getTila() == HakemuksenTila.PERUNUT) {
                                h.setTila(HakemuksenTila.PERUNUT);
                                h.setTilanKuvaukset(parasHyvaksyttyHakutoive.getHakemus().getTilanKuvaukset());
                                hakemus.setTilaVoidaanVaihtaa(false);
                            }
                            // Hyväksytyltä laitetaan peruuntuneiksi huonomman prioriteetin jonot
                            else if (parasHyvaksyttyHakutoive.getValintatapajono().getValintatapajono().getPrioriteetti() < hakemus.getValintatapajono().getValintatapajono().getPrioriteetti()) {
                                peruutaHakemusKoskaHyvaksyttyToisessaJonossa(hakemus, h);
                            }
                        }
                        // Paras toive PERUNUT, toisessa jonossa hyväksytty
                        else if (!hakemus.isTilaVoidaanVaihtaa()
                                && h.getPrioriteetti().equals(parasHyvaksyttyHakutoive.getHakemus().getPrioriteetti())
                                && parasHyvaksyttyHakutoive.getHakemus().getTila() == HakemuksenTila.PERUNUT
                                && yliajettavat.contains(h.getTila())) {
                            // Ei vastaanottoa, voidaan yliajaa
                            if (!vastaanOttoPerutussaKohteessa(henkilo, hakemus)) {
                                h.setTila(HakemuksenTila.PERUNUT);
                                h.setTilanKuvaukset(parasHyvaksyttyHakutoive.getHakemus().getTilanKuvaukset());
                                hakemus.setTilaVoidaanVaihtaa(false);
                            }
                        }
                        // Perutaan myös ehdollinen vastaanotto
                        else if (h.getPrioriteetti() > parasHyvaksyttyHakutoive.getHakemus().getPrioriteetti()
                                && ehdollinenOpt.isPresent()
                                && ehdollinenOpt.get().getValintatapajonoOid().equals(hakemus.getValintatapajono().getValintatapajono().getOid())) {
                            peruutaHakemusKoskaPeruuntunutYlempiToive(hakemus, h);
                            Optional<Valintatulos> nykyinenTulos = henkilo.getValintatulos().stream().filter(v -> v.getValintatapajonoOid().equals(hakemus.getValintatapajono().getValintatapajono().getOid())).findFirst();
                            if (nykyinenTulos.isPresent()) {
                                lisaaMuokattavaValintatulos(sijoitteluajoWrapper, nykyinenTulos.get());
                            }
                        }
                    }
                });
            }
            // Kaikki jonot ei vielä sijoittelussa, yliajetaan tylysti kaikki alemmat hyväksytyt ja varalla olot
            else {
                henkilo.getHakemukset().forEach(hakemus -> {
                    Hakemus h = hakemus.getHakemus();
                    if (parasHyvaksyttyHakutoive != null) {
                        if (yliajettavat.contains(h.getTila()) && h.getPrioriteetti() > parasHyvaksyttyHakutoive.getHakemus().getPrioriteetti()) {
                            peruutaHakemusKoskaPeruuntunutYlempiToive(hakemus, h);
                            Optional<Valintatulos> nykyinenTulos = henkilo.getValintatulos().stream().filter(v -> v.getValintatapajonoOid().equals(hakemus.getValintatapajono().getValintatapajono().getOid())).findFirst();
                            if (nykyinenTulos.isPresent()) {
                                lisaaMuokattavaValintatulos(sijoitteluajoWrapper, nykyinenTulos.get());
                            }
                        } else if (yliajettavat.contains(h.getTila()) && h.getPrioriteetti().equals(parasHyvaksyttyHakutoive.getHakemus().getPrioriteetti())) {
                            // Jos tila on Perunut se periytetään kaikkiin jonoihin
                            if (parasHyvaksyttyHakutoive.getHakemus().getTila() == HakemuksenTila.PERUNUT) {
                                h.setTila(HakemuksenTila.PERUNUT);
                                h.setTilanKuvaukset(parasHyvaksyttyHakutoive.getHakemus().getTilanKuvaukset());
                                hakemus.setTilaVoidaanVaihtaa(false);
                            }
                            // Hyväksytyltä laitetaan peruuntuneiksi huonomman prioriteetin jonot
                            else if (parasHyvaksyttyHakutoive.getValintatapajono().getValintatapajono().getPrioriteetti() < hakemus.getValintatapajono().getValintatapajono().getPrioriteetti()) {
                                peruutaHakemusKoskaHyvaksyttyToisessaJonossa(hakemus, h);
                            }
                        }
                    }
                });
            }
        });

    }

    private void peruutaAlemmatHakemukset(SijoitteluajoWrapper sijoitteluajoWrapper, HenkiloWrapper henkilo, HakemusWrapper parasHyvaksyttyHakutoive, Optional<Valintatulos> ehdollinenOpt) {
        Valintatulos ehdollinen = ehdollinenOpt.get();
        henkilo.getHakemukset().forEach(hakemus -> {
            Hakemus h = hakemus.getHakemus();
            if (yliajettavat.contains(h.getTila())
                    && !hakemus.getValintatapajono().getValintatapajono().getOid().equals(ehdollinen.getValintatapajonoOid())
                    && h.getPrioriteetti() > parasHyvaksyttyHakutoive.getHakemus().getPrioriteetti()) {
                peruutaHakemusKoskaPeruuntunutYlempiToive(hakemus, h);
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
                    h.setTilanKuvaukset(TilanKuvaukset.peruuntunutVastaanottanutToisenOpiskelupaikan());
                    h.setTila(HakemuksenTila.PERUUNTUNUT);
                    hakemus.setTilaVoidaanVaihtaa(false);
                    Optional<Valintatulos> nykyinenTulos = henkilo.getValintatulos().stream().filter(v -> v.getValintatapajonoOid().equals(hakemus.getValintatapajono().getValintatapajono().getOid())).findFirst();
                    if (nykyinenTulos.isPresent()) {
                        lisaaMuokattavaValintatulos(sijoitteluajoWrapper, nykyinenTulos.get());
                    }
                } else if (sitovaHakemus.getValintatapajono().getValintatapajono().getPrioriteetti() < hakemus.getValintatapajono().getValintatapajono().getPrioriteetti()) {
                    peruutaHakemusKoskaHyvaksyttyToisessaJonossa(hakemus, h);
                    Optional<Valintatulos> nykyinenTulos = henkilo.getValintatulos().stream().filter(v -> v.getValintatapajonoOid().equals(hakemus.getValintatapajono().getValintatapajono().getOid())).findFirst();
                    if (nykyinenTulos.isPresent()) {
                        lisaaMuokattavaValintatulos(sijoitteluajoWrapper, nykyinenTulos.get());
                    }
                }

            }
        });
    }

    private void peruutaHakemusKoskaPeruuntunutYlempiToive(HakemusWrapper hakemus, Hakemus h) {
        h.setTilanKuvaukset(TilanKuvaukset.peruuntunutYlempiToive());
        h.setTila(HakemuksenTila.PERUUNTUNUT);
        hakemus.setTilaVoidaanVaihtaa(false);
    }

    private void peruutaHakemusKoskaHyvaksyttyToisessaJonossa(HakemusWrapper hakemus, Hakemus h) {
        h.setTilanKuvaukset(TilanKuvaukset.peruuntunutHyvaksyttyToisessaJonossa());
        h.setTila(HakemuksenTila.PERUUNTUNUT);
        hakemus.setTilaVoidaanVaihtaa(false);
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
        nykyinen.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY);
        nykyinen.setTila(ValintatuloksenTila.KESKEN);
        nykyinen.setHyvaksyttyVarasijalta(false);
        sijoitteluajoWrapper.getMuuttuneetValintatulokset().add(nykyinen);
    }

    private Optional<Valintatulos> sitovastiVastaanottanut(HenkiloWrapper henkiloWrapper) {
        return henkiloWrapper.getValintatulos()
                .stream()
                .filter(v -> v.getTila().equals(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI))
                .findFirst();
    }

    private Optional<Valintatulos> ehdollisestiVastaanottanut(HenkiloWrapper henkiloWrapper) {
        return henkiloWrapper.getValintatulos()
                .stream()
                .filter(v -> v.getTila().equals(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT))
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
            if (hakemusWrapper.getHakemus().getTila() == HakemuksenTila.HYVAKSYTTY
                    || hakemusWrapper.getHakemus().getTila() == HakemuksenTila.PERUNUT
                    || hakemusWrapper.getHakemus().getTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY) {
                if (parasHyvaksyttyHakutoive == null || parasHyvaksyttyHakutoive.getHakemus().getPrioriteetti() > hakemusWrapper.getHakemus().getPrioriteetti()) {
                    parasHyvaksyttyHakutoive = hakemusWrapper;
                } else if (parasHyvaksyttyHakutoive.getHakemus().getPrioriteetti().equals(hakemusWrapper.getHakemus().getPrioriteetti()) && hakemusWrapper.getHakemus().getTila() == HakemuksenTila.PERUNUT) {
                    // PERUNUT tila yliajaa hyväksynnät
                    parasHyvaksyttyHakutoive = hakemusWrapper;
                }
            }
        }
        return parasHyvaksyttyHakutoive;
    }
}
