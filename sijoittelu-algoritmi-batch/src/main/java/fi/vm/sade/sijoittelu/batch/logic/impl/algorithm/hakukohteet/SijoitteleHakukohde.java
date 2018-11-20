package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.hakukohteet;

import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilaTaulukot.kuuluuHylattyihinTiloihin;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilaTaulukot.kuuluuHyvaksyttyihinTiloihin;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilaTaulukot.kuuluuPoissaoloTiloihin;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilaTaulukot.kuuluuPoissaoloTiloihin2Aste;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilaTaulukot.kuuluuVaraTiloihin;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilaTaulukot.kuuluuYliajettaviinHakemuksenTiloihin;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilojenMuokkaus.asetaTilaksiHyvaksytty;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilojenMuokkaus.asetaTilaksiPeruuntunutHakukierrosPaattynyt;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilojenMuokkaus.asetaTilaksiPeruuntunutToinenJono;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilojenMuokkaus.asetaTilaksiPeruuntunutYlempiToive;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilojenMuokkaus.asetaTilaksiVarasijaltaHyvaksytty;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilojenMuokkaus.asetaVastaanottanut;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilojenMuokkaus.siirraValintatulosHyvaksyttyynJonoon;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.WrapperHelperMethods.*;

import com.google.common.collect.Sets;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.comparator.HakemusWrapperComparator;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakijaryhmaWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakukohdeWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HenkiloWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Tasasijasaanto;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class SijoitteleHakukohde {
    private static final Logger LOG = LoggerFactory.getLogger(SijoitteleHakukohde.class);
    private static HakemusWrapperComparator comparator = new HakemusWrapperComparator();
    public static Set<HakukohdeWrapper> sijoitteleHakukohde(SijoitteluajoWrapper sijoitteluAjo, HakukohdeWrapper hakukohde) {

        Set<HakukohdeWrapper> muuttuneetHakukohteet = Sets.newHashSet();
        for (HakijaryhmaWrapper hakijaryhmaWrapper : hakukohde.getHakijaryhmaWrappers()) {
            muuttuneetHakukohteet.addAll(SijoitteleHakijaryhma.sijoitteleHakijaryhma(sijoitteluAjo, hakijaryhmaWrapper));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Hakemusten tilat hakijaryhmäsijoittelun jälkeen:");
            debugLogHakemusStates(hakukohde);
        }

        for (ValintatapajonoWrapper valintatapajono : hakukohde.getValintatapajonot()) {
            muuttuneetHakukohteet.addAll(sijoitteleValintatapajono(sijoitteluAjo, valintatapajono));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Hakemusten tilat valintatapajonosijoittelun jälkeen:");
            debugLogHakemusStates(hakukohde);
        }

        poistaAjokierroksenLukot(hakukohde);
        return muuttuneetHakukohteet;
    }

    private static void debugLogHakemusStates(HakukohdeWrapper hakukohde) {
        List<HakijaryhmaWrapper> hrws = hakukohde.getHakijaryhmaWrappers();
        Optional<HakijaryhmaWrapper> hrw;
        if (hrws.size() > 0 ) { hrw = Optional.of(hrws.get(0)); } else { hrw = Optional.empty(); }
        Optional<HakijaryhmaWrapper> finalHrw = hrw;
        hakukohde.getValintatapajonot().forEach(jono -> {
            LOG.debug("        jono " + jono.getValintatapajono().getOid() + " :");
            List<HakemusWrapper> jononHakemukset = jono.getHakemukset().stream()
                    .sorted(new HakemusWrapperComparator()).collect(Collectors.toList());
            jononHakemukset.forEach(h -> LOG.debug("                " + (finalHrw.isPresent() && finalHrw.get().getHenkiloWrappers().contains(h.getHenkilo()) ? " * " : " - ") +
                    h.getHakemus().getHakemusOid() + " / jonosija " + h.getHakemus().getJonosija() + " / " + h.getHakemus().getTila() + " / hyväksytty hakijaryhmistä " +
                    h.getHakemus().getHyvaksyttyHakijaryhmista() + " / hyväksytty hakijaryhmästä tällä kierroksella " + h.isHyvaksyttyHakijaryhmastaTallaKierroksella()));
        });

    }

    private static Set<HakukohdeWrapper> sijoitteleValintatapajono(SijoitteluajoWrapper sijoitteluAjo, ValintatapajonoWrapper valintatapajono) {
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
                .filter(h -> hakijaHaluaa(h) && saannotSallii(h, sijoitteluAjo))
                .collect(Collectors.toList());

        // Ei ketään valituksi haluavaa
        if (valituksiHaluavatHakemukset.isEmpty()) {
            return muuttuneetHakukohteet;
        }
        // Hakukierros on päättynyt tai käsiteltävän jonon varasijasäännöt eivät ole enää voimassa.
        // Asetetaan kaikki hakemukset joiden tila voidaan vaihtaa tilaan peruuntunut, paitsi jos ne on hyväksytty tai varasijalta hyväksytty edellisessä sijoitteluajossa.
        if (sijoitteluAjo.hakukierrosOnPaattynyt() || sijoitteluAjo.onkoVarasijaSaannotVoimassaJaVarasijaTayttoPaattynyt(valintatapajono)) {
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

        Tasasijasaanto saanto = jononTasasijasaanto(valintatapajono);
        List<HakemusWrapper> kaikkiTasasijaHakemukset = getTasasijaHakemus(valituksiHaluavatHakemukset, saanto);

        LisapaikatHakijaryhmasijoittelunYlitaytonSeurauksena lisaPaikat = new LisapaikatHakijaryhmasijoittelunYlitaytonSeurauksena(valintatapajono, tilaa, kaikkiTasasijaHakemukset.size());

        if (tilaa <= 0) {
            if (lisaPaikat.isKaikkiEhdot()) {
                LOG.info(lisaPaikat.toString());
            }
            return muuttuneetHakukohteet;
        } else if (lisaPaikat.isKaikkiEhdot() && tilaa-kaikkiTasasijaHakemukset.size() < 0){
            LOG.info("JONOSSA TILAA MUTTA LOGITETAAN KUITENKIN! " + lisaPaikat.toString());
        }

        List<HakemusWrapper> muuttuneet = new ArrayList<>();
        if (tilaa - kaikkiTasasijaHakemukset.size() >= 0) {
            hyvaksyKaikkiTasasijaHakemukset(sijoitteluAjo, kaikkiTasasijaHakemukset, muuttuneet);
            muuttuneetHakukohteet.addAll(uudelleenSijoiteltavatHakukohteet(muuttuneet));
            try {
                muuttuneetHakukohteet.addAll(sijoitteleValintatapajono(sijoitteluAjo, valintatapajono));
            } catch (Throwable t) {
                String msg = "Sijoitteluajon " + sijoitteluAjo.getSijoitteluAjoId() +
                    " muuttuneetHakukohteet.addAll(sijoitteleValintatapajono(sijoitteluAjo, valintatapajono)) kaatui." +
                    " Hakukohde : " + valintatapajono.getHakukohdeWrapper().getHakukohde().getOid() + " , " +
                    "valintatapajono: " + valintatapajono.getValintatapajono().getOid() + " valituksi haluavat: " +
                    valituksiHaluavatHakemukset;
                LOG.error(msg, t);
                throw new RuntimeException(t);
            }
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
                h.setHyvaksyttyHakijaryhmastaTallaKierroksella(false);
                h.setHyvaksyttavissaHakijaryhmanJalkeen(true);
            });
        });
    }

    static Set<HakukohdeWrapper> uudelleenSijoiteltavatHakukohteet(List<HakemusWrapper> muuttuneetHakemukset) {
        return muuttuneetHakemukset.stream()
            .map(h -> h.getValintatapajono().getHakukohdeWrapper())
            .collect(Collectors.toSet());
    }

    private static List<HakemusWrapper> valintatapajononHyvaksytytHakemuksetJoitaEiVoiKorvata(
            ValintatapajonoWrapper valintatapajono, SijoitteluajoWrapper sijoitteluajo) {
        Predicate<HakemusWrapper> hyvaksyttyJotaEiVoiKorvata = hyvaksyttyJotaEiVoiKorvata(valintatapajono, sijoitteluajo);
        return valintatapajono.getHakemukset()
            .stream()
            .filter(hyvaksyttyJotaEiVoiKorvata)
            .collect(Collectors.toList());
    }

    static Predicate<Hakemus> eiVoiKorvataIlmoittautumistilanPerusteella(ValintatapajonoWrapper valintatapajonoWrapper,
                                                                         SijoitteluajoWrapper sijoitteluajoWrapper) {
        if (taytetaankoPoissaOlevat(valintatapajonoWrapper)) {
            if (sijoitteluajoWrapper.isKKHaku()) {
                return h -> !kuuluuPoissaoloTiloihin(h.getIlmoittautumisTila());
            } else {
                return h -> !kuuluuPoissaoloTiloihin2Aste(h.getIlmoittautumisTila());
            }
        } else {
            return h -> true;
        }
    }

    static Predicate<HakemusWrapper> hyvaksyttyJotaEiVoiKorvata(ValintatapajonoWrapper valintatapajono, SijoitteluajoWrapper sijoitteluajo) {
        return hakemusWrapper -> {
            List<Predicate<Hakemus>> filters = new ArrayList<>();
            filters.add(h -> kuuluuHyvaksyttyihinTiloihin(h.getTila()));
            filters.add(eiVoiKorvataIlmoittautumistilanPerusteella(valintatapajono, sijoitteluajo));
            return filters.stream().reduce(h -> true, Predicate::and).test(hakemusWrapper.getHakemus());
        };
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

    static boolean saannotSallii(HakemusWrapper hakemusWrapper, SijoitteluajoWrapper sijoittaluajo) {
        Supplier<Boolean> eiVarasijatayttoa = () -> sijoittaluajo.varasijaSaannotVoimassa() &&
                Boolean.TRUE.equals(jononEiVarasijatayttoa(hakemusWrapper)) &&
                !jononKaikkiEhdonTayttavatHyvaksytaan(hakemusWrapper);

        if (!sijoittaluajo.isKKHaku() && eiVarasijatayttoa.get() && !hakijaAloistuspaikkojenSisalla(hakemusWrapper)) {
            return false;
        }
        return !kuuluuHylattyihinTiloihin(hakemuksenTila(hakemusWrapper)) &&
                eiPeruttuaKorkeampaaTaiSamaaHakutoivetta(hakemusWrapper) &&
                hakemusWrapper.isHyvaksyttavissaHakijaryhmanJalkeen();
    }

    //Asetetaan kaikki syötteenä saadut hakemukset tilaan PERUUNTUNUT, paitsi jos hakemus oli edellisessä sijoitteluajossa HYVAKSYTTY tai VARASIJALTA_HYVAKSYTTY
    private static void hakukierrosPaattynyt(List<HakemusWrapper> hakemukset) {
        hakemukset.forEach(hakemusWrapper -> {
            if (hakemusWrapper.isTilaVoidaanVaihtaa()) {
                Hakemus hakemus = hakemusWrapper.getHakemus();
                HakemuksenTila edellinenTila = hakemus.getEdellinenTila();
                if (kuuluuHyvaksyttyihinTiloihin(edellinenTila)) {
                    String hakemuksenTunniste = String.format("Hakemus %s jonossa %s kohteessa %s tilassa %s, edellinen tila %s: ",
                        hakemus.getHakemusOid(), hakemusWrapper.getValintatapajono().getValintatapajono().getOid(), hakemusWrapper.getHakukohdeOid(), hakemus.getTila(), edellinenTila);
                    LOG.info(hakemuksenTunniste + "Ei merkitä hakemuksen tilaa peruuntuneeksi varasijatäytön päätyttyä, " +
                        "koska hakemus on hyväksytty edellisessä sijoitteluajossa. " +
                        "Pidetään voimassa hakemuksen edellinen tila");
                    if (HakemuksenTila.HYVAKSYTTY.equals(edellinenTila)) {
                        asetaTilaksiHyvaksytty(hakemusWrapper);
                        hakemusWrapper.setTilaVoidaanVaihtaa(false);
                    } else if (HakemuksenTila.VARASIJALTA_HYVAKSYTTY.equals(edellinenTila)) {
                        asetaTilaksiVarasijaltaHyvaksytty(hakemusWrapper);
                        hakemusWrapper.setTilaVoidaanVaihtaa(false);
                    } else {
                        throw new IllegalStateException(String.format("%sKuuluuko %s hyväksyttyihin tiloihin? Vaikuttaa bugilta.",
                            hakemuksenTunniste, edellinenTila));
                    }
                } else {
                    asetaTilaksiPeruuntunutHakukierrosPaattynyt(hakemusWrapper);
                    hakemusWrapper.setTilaVoidaanVaihtaa(false);
                }
            }
        });
    }

    static Set<HakemusWrapper> hyvaksyHakemus(SijoitteluajoWrapper sijoitteluAjo, HakemusWrapper hakemus) {
        Set<HakemusWrapper> uudelleenSijoiteltavatHakukohteet = new HashSet<>();
        if(hakemus.isTilaVoidaanVaihtaa()) {
            if (kuuluuVaraTiloihin(hakemus.getHakemus().getEdellinenTila()) && sijoitteluAjo.onkoVarasijasaannotVoimassaJaKaikkiJonotSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa()) {
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
            return Collections.singletonList(paras);
        } else {
            return valituksiHaluavatHakemukset
                    .stream()
                    .filter(h -> h.getHakemus().getJonosija().equals(paras.getHakemus().getJonosija()))
                    .collect(Collectors.toList());
        }
    }

    private static void hyvaksyKaikkiTasasijaHakemukset(SijoitteluajoWrapper sijoitteluAjo, List<HakemusWrapper> kaikkiTasasijaHakemukset, List<HakemusWrapper> muuttuneet) {
        muuttuneetHyvaksytyt(kaikkiTasasijaHakemukset).forEach(h -> muuttuneet.addAll(hyvaksyHakemus(sijoitteluAjo, h)));
    }

    private static boolean taytetaankoPoissaOlevat(ValintatapajonoWrapper valintatapajono) {
        return valintatapajono.getValintatapajono().getPoissaOlevaTaytto() != null && valintatapajono.getValintatapajono().getPoissaOlevaTaytto();
    }

    private static boolean hakijaAloistuspaikkojenSisalla(HakemusWrapper hakemusWrapper) {
        ValintatapajonoWrapper valintatapajono = hakemusWrapper.getValintatapajono();
        int aloituspaikat = jononAloituspaikat(valintatapajono);
        LOG.info("Aloituspaikat: " + aloituspaikat );
        return onkoPaikkojenSisalla(hakemusWrapper, aloituspaikat, valintatapajono.getHakemukset());
    }

    private static boolean onkoPaikkojenSisalla(HakemusWrapper hakemusWrapper, int aloituspaikat, List<HakemusWrapper> hakemukset) {

        Set<HakemusWrapper> aloituspaikkojaVievat = hakemukset.stream()
                .filter(h -> h != hakemusWrapper)
                .filter(h -> h.getHakemus().getJonosija() < hakemusWrapper.getHakemus().getJonosija())
                .filter(h -> hakemuksenTila(h) != HakemuksenTila.HYLATTY)
                .collect(Collectors.toSet());

        Set<HakemusWrapper> hakijaryhmastaHyvaksytyt = hakemukset.stream()
                .filter(h -> h != hakemusWrapper)
                .filter(h -> kuuluuHyvaksyttyihinTiloihin(hakemuksenTila(h)))
                .filter(h -> !h.getHakemus().getHyvaksyttyHakijaryhmista().isEmpty())
                .collect(Collectors.toSet());

        aloituspaikkojaVievat.addAll(hakijaryhmastaHyvaksytyt);

        return aloituspaikat - aloituspaikkojaVievat.size() > 0;

    }

    static boolean eiPeruttuaKorkeampaaTaiSamaaHakutoivetta(HakemusWrapper hakemusWrapper) {
        List<HakemuksenTila> perututHakemuksenTilat = Arrays.asList(HakemuksenTila.PERUNUT, HakemuksenTila.PERUUTETTU);
        return hakemusWrapper.getHenkilo().getHakemukset()
            .stream()
            .filter(h -> h != hakemusWrapper)
            .noneMatch(h -> perututHakemuksenTilat.contains(hakemuksenTila(h)) && hakemuksenPrioriteetti(h) <= hakemuksenPrioriteetti(hakemusWrapper));
    }
}
