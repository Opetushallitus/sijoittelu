package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import com.google.common.collect.Sets;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.*;
import fi.vm.sade.sijoittelu.domain.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilaTaulukot.*;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilojenMuokkaus.*;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.WrapperHelperMethods.*;

public class SijoitteleHakukohde {
    public static Set<HakukohdeWrapper> sijoitteleHakukohde(SijoitteluajoWrapper sijoitteluAjo, HakukohdeWrapper hakukohde) {
        Set<HakukohdeWrapper> muuttuneetHakukohteet = Sets.newHashSet();
        for (HakijaryhmaWrapper hakijaryhmaWrapper : hakukohde.getHakijaryhmaWrappers()) {
            muuttuneetHakukohteet.addAll(SijoitteleHakijaryhma.sijoitteleHakijaryhma(sijoitteluAjo, hakijaryhmaWrapper));
        }
        for (ValintatapajonoWrapper valintatapajono : hakukohde.getValintatapajonot()) {
            muuttuneetHakukohteet.addAll(sijoitteleValintatapajono(sijoitteluAjo, valintatapajono));
        }
        poistaAjokierroksenLukot(hakukohde);
        return muuttuneetHakukohteet;
    }

    private static Set<HakukohdeWrapper> sijoitteleValintatapajono(SijoitteluajoWrapper sijoitteluAjo, ValintatapajonoWrapper valintatapajono) {
        final boolean varasijaTayttoPaattyy = sijoitteluAjo.onkoVarasijaTayttoPaattynyt(valintatapajono);
        // Muutetaan ehdolliset vastaanotot sitoviksi jos jonon varasijatäyttö on päättynyt
        if (sijoitteluAjo.isKKHaku() && varasijaTayttoPaattyy) {
            muutaEhdollisetVastaanototSitoviksi(sijoitteluAjo, valintatapajono);
        }
        Set<HakukohdeWrapper> muuttuneetHakukohteet = new HashSet<>();
        if (valintatapajono.isAlitayttoLukko()) {
            // Hakijaryhmäkäsittelyssä alitäyttösääntö käytetty
            return muuttuneetHakukohteet;
        }
        List<HakemusWrapper> eiKorvattavissaOlevatHyvaksytytHakemukset =
                valintatapajononHyvaksytytHakemuksetJoitaEiVoiKorvata(valintatapajono, sijoitteluAjo);
        List<HakemusWrapper> valituksiHaluavatHakemukset =
            valintatapajono.getHakemukset().stream()
                .filter(h -> !eiKorvattavissaOlevatHyvaksytytHakemukset.contains(h))
                .filter(h -> !kuuluuHyvaksyttyihinTiloihin(hakemuksenTila(h)))
                .filter(h -> hakijaHaluaa(h) && saannotSallii(sijoitteluAjo, h))
                .collect(Collectors.toList());
        // Ei ketään valituksi haluavaa
        if (valituksiHaluavatHakemukset.isEmpty()) {
            return muuttuneetHakukohteet;
        }
        // Hakukierros on päättynyt tai käsiteltävän jonon varasijasäännöt eivät ole enää voimassa.
        // Asetetaan kaikki hakemukset joiden tila voidaan vaihtaa tilaan peruuntunut
        if (sijoitteluAjo.hakukierrosOnPaattynyt() || varasijaTayttoPaattyy) {
            hakukierrosPaattynyt(valituksiHaluavatHakemukset);
            return muuttuneetHakukohteet;
        }
        // Jonolle on merkitty, että kaikki ehdon täyttävät hyväksytään
        if (valintatapajono.getValintatapajono().getKaikkiEhdonTayttavatHyvaksytaan() != null
            && valintatapajono.getValintatapajono().getKaikkiEhdonTayttavatHyvaksytaan()) {
            valituksiHaluavatHakemukset.forEach((hakemus) -> hyvaksyHakemus(sijoitteluAjo, hakemus));
            muuttuneetHakukohteet.addAll(uudelleenSijoiteltavatHakukohteet(muuttuneetHyvaksytyt(valituksiHaluavatHakemukset)));
            return muuttuneetHakukohteet;
        }
        int aloituspaikat = jononAloituspaikat(valintatapajono);
        int tilaa = aloituspaikat - eiKorvattavissaOlevatHyvaksytytHakemukset.size();
        if (tilaa <= 0) {
            return muuttuneetHakukohteet;
        }
        Tasasijasaanto saanto = jononTasasijasaanto(valintatapajono);
        List<HakemusWrapper> kaikkiTasasijaHakemukset = getTasasijaHakemus(valituksiHaluavatHakemukset, saanto);
        List<HakemusWrapper> muuttuneet = new ArrayList<>();
        if (tilaa - kaikkiTasasijaHakemukset.size() >= 0) {
            hyvaksyKaikkiTasasijaHakemukset(sijoitteluAjo, kaikkiTasasijaHakemukset, muuttuneet);
            muuttuneetHakukohteet.addAll(uudelleenSijoiteltavatHakukohteet(muuttuneet));
            muuttuneetHakukohteet.addAll(sijoitteleValintatapajono(sijoitteluAjo, valintatapajono));
        } else {
            // Tasasijavertailu
            if (saanto.equals(Tasasijasaanto.YLITAYTTO)) {
                hyvaksyKaikkiTasasijaHakemukset(sijoitteluAjo, kaikkiTasasijaHakemukset, muuttuneet);
            }
            muuttuneetHakukohteet.addAll(uudelleenSijoiteltavatHakukohteet(muuttuneet));
        }
        return muuttuneetHakukohteet;
    }

    private static void poistaAjokierroksenLukot(HakukohdeWrapper hakukohde) {
        hakukohde.getValintatapajonot().forEach(v -> {
            v.setAlitayttoLukko(false);
            v.getHakemukset().forEach(h -> {
                h.setHyvaksyttyHakijaryhmasta(false);
                h.setHyvaksyttavissaHakijaryhmanJalkeen(true);
            });
        });
    }

    static Set<HakukohdeWrapper> uudelleenSijoiteltavatHakukohteet(List<HakemusWrapper> muuttuneetHakemukset) {
        return muuttuneetHakemukset.stream()
            .map(h -> h.getValintatapajono().getHakukohdeWrapper())
            .collect(Collectors.toSet());
    }

    private static void muutaEhdollisetVastaanototSitoviksi(SijoitteluajoWrapper sijoitteluAjo, ValintatapajonoWrapper valintatapajono) {
        valintatapajono.ehdollisestiVastaanottaneetJonossa()
                .forEach(h -> {
                    if(!h.getYlemmatTaiSamanarvoisetMuttaKorkeammallaJonoPrioriteetillaOlevatHakutoiveet()
                            // On varalla olevia ylempiarvoisia hakutoiveita
                            .filter(HakemusWrapper::isVaralla)
                            // eika varasijataytto ole viela paattynyt niissa
                            .filter(h0 -> !sijoitteluAjo.onkoVarasijaTayttoPaattynyt(h0.getValintatapajono()))
                            .findAny().isPresent()) {
                        h.getValintatulos().ifPresent(v -> {
                            v.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI,
                                    "Ehdollinen vastaanotto ylimmässä mahdollisessa hakutoiveessa sitovaksi");
                            sijoitteluAjo.addMuuttuneetValintatulokset(v);
                        });
                    }
                });
    }

    private static List<HakemusWrapper> valintatapajononHyvaksytytHakemuksetJoitaEiVoiKorvata(
            ValintatapajonoWrapper valintatapajono, SijoitteluajoWrapper sijoitteluajo) {
        List<Predicate<HakemusWrapper>> filters = new ArrayList<>();
        filters.add(h -> kuuluuHyvaksyttyihinTiloihin(hakemuksenTila(h)));
        if (taytetaankoPoissaOlevat(valintatapajono)) {
            if (sijoitteluajo.isKKHaku()) {
                filters.add(h -> !kuuluuPoissaoloTiloihin(h.getHakemus().getIlmoittautumisTila()));
            } else {
                filters.add(h -> !kuuluuPoissaoloTiloihin2Aste(h.getHakemus().getIlmoittautumisTila()));
            }
        }
        return valintatapajono.getHakemukset()
            .stream()
            .filter(filters.stream().reduce(h -> true, Predicate::and))
            .collect(Collectors.toList());
    }

    static boolean hakijaHaluaa(HakemusWrapper hakemusWrapper) {
        HenkiloWrapper henkilo = hakemusWrapper.getHenkilo();
        // Tila on PERUUNUTUNUT eikä sitä voi vaihtaa
        if (!hakemusWrapper.isTilaVoidaanVaihtaa() && hakemuksenTila(hakemusWrapper) == HakemuksenTila.PERUUNTUNUT) {
            return false;
        }
        for (HakemusWrapper h : henkilo.getHakemukset()) {
            if (kuuluuHyvaksyttyihinTiloihin(hakemuksenTila(h))
                &&
                // Hakija hyväksytty paremmalle hakutoiveelle
                (hakemuksenPrioriteetti(h) < hakemuksenPrioriteetti(hakemusWrapper)
                    ||
                    // Hakija hyväksytty paremman prioriteetin jonossa
                    (hakemuksenPrioriteetti(h).equals(hakemuksenPrioriteetti(hakemusWrapper))
                        && jononPrioriteetti(h) < jononPrioriteetti(hakemusWrapper)))
                &&
                // eikä vertailla itseensä
                hakemusWrapper != h) {
                return false;
            }
        }
        return true;
    }

    static boolean saannotSallii(SijoitteluajoWrapper sijoitteluAjo, HakemusWrapper hakemusWrapper) {
        boolean hakemuksenTila = !kuuluuHylattyihinTiloihin(hakemuksenTila(hakemusWrapper));
        boolean hakijaAloistuspaikkojenSisallaTaiVarasijataytto = true;
        boolean eiVarasijaTayttoa = false;
        // Jos varasijasäännöt ovat astuneet voimaan niin katsotaan saako varasijoilta täyttää
        if (sijoitteluAjo.varasijaSaannotVoimassa()) {
            if (jononEiVarasijatayttoa(hakemusWrapper) != null) {
                eiVarasijaTayttoa = jononEiVarasijatayttoa(hakemusWrapper);
            }
        }
        if (eiVarasijaTayttoa && !jononKaikkiEhdonTayttavatHyvaksytaan(hakemusWrapper)) {
            hakijaAloistuspaikkojenSisallaTaiVarasijataytto = hakijaAloistuspaikkojenSisalla(hakemusWrapper);
            if (!hakijaAloistuspaikkojenSisallaTaiVarasijataytto && sijoitteluAjo.isKKHaku() && hakemusWrapper.isTilaVoidaanVaihtaa()) {
                asetaTilaksiPeruuntunutAloituspaikatTaynna(hakemusWrapper);
            }
        }
        Integer varasijat = jononVarasijat(hakemusWrapper);
        boolean huomioitavienVarasijojenSisalla = true;
        if (sijoitteluAjo.varasijaSaannotVoimassa() && varasijat != null && varasijat > 0) {
            huomioitavienVarasijojenSisalla = hakijaKasiteltavienVarasijojenSisalla(hakemusWrapper, varasijat);
            if (!huomioitavienVarasijojenSisalla && hakemusWrapper.isTilaVoidaanVaihtaa()) {
                asetaTilaksiPeruuntunutEiMahduKasiteltaviinSijoihin(hakemusWrapper);
            }
        }
        boolean eiPeruttuaKorkeampaaTaiSamaaHakutoivetta = eiPeruttuaKorkeampaaTaiSamaaHakutoivetta(hakemusWrapper);
        return hakemuksenTila
            && hakijaAloistuspaikkojenSisallaTaiVarasijataytto
            && eiPeruttuaKorkeampaaTaiSamaaHakutoivetta
            && huomioitavienVarasijojenSisalla
            && hakemusWrapper.isHyvaksyttavissaHakijaryhmanJalkeen();
    }

    private static void hakukierrosPaattynyt(List<HakemusWrapper> hakemukset) {
        hakemukset.forEach(hk -> {
            if (hk.isTilaVoidaanVaihtaa()) {
                asetaTilaksiPeruuntunutHakukierrosPaattynyt(hk);
                hk.setTilaVoidaanVaihtaa(false);
            }
        });
    }

    static Set<HakemusWrapper> hyvaksyHakemus(SijoitteluajoWrapper sijoitteluAjo, HakemusWrapper hakemus) {
        Set<HakemusWrapper> uudelleenSijoiteltavatHakukohteet = new HashSet<HakemusWrapper>();
        if (hakemus.isTilaVoidaanVaihtaa()) {
            if (kuuluuVaraTiloihin(hakemus.getHakemus().getEdellinenTila()) && sijoitteluAjo.varasijaSaannotVoimassa()) {
                asetaTilaksiVarasijaltaHyvaksytty(hakemus);
            } else {
                asetaTilaksiHyvaksytty(hakemus);
            }

            Optional<Valintatulos> ehdollinenOpt = hakemus.getHenkilo().getValintatulos().stream()
                .filter(v -> v.getHakemusOid().equals(hakemus.getHakemus().getHakemusOid()) && v.getTila().equals(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT))
                .findFirst();

            for (HakemusWrapper h : hakemus.getHenkilo().getHakemukset()) {
                // Alemmat toiveet
                if (h != hakemus && hakemuksenPrioriteetti(hakemus) < hakemuksenPrioriteetti(h)) {
                    if (h.isTilaVoidaanVaihtaa() || ehdollinenOpt.isPresent()) {
                        if (kuuluuHyvaksyttyihinTiloihin(hakemuksenTila(h))) {
                            asetaTilaksiPeruuntunutYlempiToive(h);
                            ehdollinenOpt.ifPresent(e -> asetaVastaanottanut(sijoitteluAjo, hakemus));
                            uudelleenSijoiteltavatHakukohteet.add(h);
                        } else if (!kuuluuHylattyihinTiloihin(hakemuksenTila(h))) {
                            asetaTilaksiPeruuntunutYlempiToive(h);
                            ehdollinenOpt.ifPresent(e -> asetaVastaanottanut(sijoitteluAjo, hakemus));
                        }
                    }

                    // Kaikki jonot ei vielä sijoittelussa, yliajetaan tylysti kaikki alemmat hyväksytyt ja varalla olot
                    if(!sijoitteluAjo.paivamaaraOhitettu() && kuuluuYliajettaviinHakemuksenTiloihin(hakemuksenTila(h))) {
                        asetaTilaksiPeruuntunutYlempiToive(h);
                        ehdollinenOpt.ifPresent(e -> asetaVastaanottanut(sijoitteluAjo, hakemus));
                        hakemus.setTilaVoidaanVaihtaa(false);
                        uudelleenSijoiteltavatHakukohteet.add(h);
                    }
                }

                // Saman toiveen muut jonot
                if (h != hakemus && hakemuksenPrioriteetti(hakemus).equals(hakemuksenPrioriteetti(h))) {
                    Valintatapajono current = h.getValintatapajono().getValintatapajono();
                    Valintatapajono hyvaksyttyJono = hakemus.getValintatapajono().getValintatapajono();

                    // Peruutetaan vain jonot joissa prioriteetti integer on suurempi
                    if (hyvaksyttyJono.getPrioriteetti() < current.getPrioriteetti()) {
                        // Perustapaus
                        if (h.isTilaVoidaanVaihtaa()) {
                            if (!kuuluuHylattyihinTiloihin(hakemuksenTila(h))) {
                                HakemuksenTila vanhaTila = hakemuksenTila(h);
                                asetaTilaksiPeruuntunutToinenJono(h);
                                asetaLippuJosSiirtynytToisestaValintatapajonosta(hakemus, h);
                                if (kuuluuHyvaksyttyihinTiloihin(vanhaTila)) {
                                    uudelleenSijoiteltavatHakukohteet.add(h);
                                }
                            }
                        } else {
                            // Hakemukselle merkattu, että tilaa ei voi vaihtaa, mutta vaihdetaan kuitenkin jos hyväksytty
                            HakemuksenTila vanhaTila = hakemuksenTila(h);
                            if (kuuluuHyvaksyttyihinTiloihin(vanhaTila)) {
                                asetaTilaksiPeruuntunutToinenJono(h);
                                asetaLippuJosSiirtynytToisestaValintatapajonosta(hakemus, h);
                                Optional<Valintatulos> jononTulos = h.getHenkilo().getValintatulos().stream().filter(v -> v.getValintatapajonoOid().equals(current.getOid())).findFirst();
                                if (jononTulos.isPresent() && !jononTulos.get().getTila().equals(ValintatuloksenTila.KESKEN)) {
                                    Valintatulos muokattava = jononTulos.get();
                                    Valintatulos nykyinen = siirraValintatulosHyvaksyttyynJonoon(hakemus, h, hyvaksyttyJono, muokattava);
                                    // Lisää muokatut valintatulokset listaan tallennusta varten
                                    sijoitteluAjo.addMuuttuneetValintatulokset(muokattava, nykyinen);
                                }
                                if (vanhaTila == HakemuksenTila.HYVAKSYTTY) {
                                    asetaTilaksiHyvaksytty(hakemus);
                                }
                                hakemus.setTilaVoidaanVaihtaa(false);
                                uudelleenSijoiteltavatHakukohteet.add(h);
                            }
                        }
                    }
                }
            }
        }
        return uudelleenSijoiteltavatHakukohteet;
    }

    private static List<HakemusWrapper> muuttuneetHyvaksytyt(List<HakemusWrapper> hakemukset) {
        return hakemukset.stream().filter(h -> !kuuluuHyvaksyttyihinTiloihin(hakemuksenTila(h))).collect(Collectors.toList());
    }

    private static void asetaLippuJosSiirtynytToisestaValintatapajonosta(HakemusWrapper hakemus, HakemusWrapper currentHakemus) {
        if (currentHakemus.getHakemus().getEdellinenTila() != null
                && kuuluuHyvaksyttyihinTiloihin(currentHakemus.getHakemus().getEdellinenTila())
                && !siirtynytToisestaValintatapajonosta(hakemus)) {
            asetaSiirtynytToisestaValintatapajonosta(hakemus, true);
        }
    }

    private static List<HakemusWrapper> getTasasijaHakemus(List<HakemusWrapper> valituksiHaluavatHakemukset, Tasasijasaanto saanto) {
        HakemusWrapper paras = valituksiHaluavatHakemukset.get(0);
        if (saanto.equals(Tasasijasaanto.ARVONTA)) {
            return Arrays.asList(paras);
        } else {
            return valituksiHaluavatHakemukset
                .stream()
                .filter(h -> h.getHakemus().getJonosija().equals(paras.getHakemus().getJonosija()))
                .collect(Collectors.toList());
        }
    }

    private static void hyvaksyKaikkiTasasijaHakemukset(SijoitteluajoWrapper sijoitteluAjo, List<HakemusWrapper> kaikkiTasasijaHakemukset, List<HakemusWrapper> muuttuneet) {
        muuttuneetHyvaksytyt(kaikkiTasasijaHakemukset).forEach(h -> {
            muuttuneet.addAll(hyvaksyHakemus(sijoitteluAjo, h));
        });
    }

    private static boolean taytetaankoPoissaOlevat(ValintatapajonoWrapper valintatapajono) {
        return valintatapajono.getValintatapajono().getPoissaOlevaTaytto() != null && valintatapajono.getValintatapajono().getPoissaOlevaTaytto();
    }

    private static boolean hakijaAloistuspaikkojenSisalla(HakemusWrapper hakemusWrapper) {
        ValintatapajonoWrapper valintatapajono = hakemusWrapper.getValintatapajono();
        int aloituspaikat = jononAloituspaikat(valintatapajono);
        return onkoPaikkojenSisalla(hakemusWrapper, aloituspaikat, valintatapajono.getHakemukset());
    }

    private static boolean onkoPaikkojenSisalla(HakemusWrapper hakemusWrapper, int aloituspaikat, List<HakemusWrapper> hakemukset) {
        int i = 0;
        for (HakemusWrapper h : hakemukset) {
            if (hakemuksenTila(h) != HakemuksenTila.HYLATTY) {
                i++;
            }
            if (h == hakemusWrapper && i <= aloituspaikat) { //vertaa instanssia
                return true;
            } else if (i > aloituspaikat) {
                return false;
            }
        }
        return true;
    }

    private static boolean hakijaKasiteltavienVarasijojenSisalla(HakemusWrapper hakemusWrapper, Integer varasijat) {
        ValintatapajonoWrapper valintatapajono = hakemusWrapper.getValintatapajono();
        int aloituspaikat = jononAloituspaikat(valintatapajono) + varasijat;
        return onkoPaikkojenSisalla(hakemusWrapper, aloituspaikat, valintatapajono.getHakemukset());
    }

    static boolean eiPeruttuaKorkeampaaTaiSamaaHakutoivetta(HakemusWrapper hakemusWrapper) {
        List<HakemuksenTila> perututHakemuksenTilat = Arrays.asList(HakemuksenTila.PERUNUT, HakemuksenTila.PERUUTETTU);
        return hakemusWrapper.getHenkilo().getHakemukset()
            .stream()
            .filter(h -> h != hakemusWrapper)
            .noneMatch(h -> perututHakemuksenTilat.contains(hakemuksenTila(h)) && hakemuksenPrioriteetti(h) <= hakemuksenPrioriteetti(hakemusWrapper));
    }
}
