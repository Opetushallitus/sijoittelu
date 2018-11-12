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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
        final String baseStr = "HRS - Hakukohde " + valintatapajono.getHakukohdeWrapper().getHakukohde().getOid()+ " - ";
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

        int lisaPaikkaTapa = 1; //0 - ei käytetä lisäpaikkoja
        int ehdollisetAloituspaikatTapa1 = 0;
        int ehdollisetAloituspaikatTapa2 = 0;
        int aloituspaikat = jononAloituspaikat(valintatapajono);
        int tilaa = aloituspaikat - eiKorvattavissaOlevatHyvaksytytHakemukset.size();
        boolean hakukohteessaValintaryhmia = !valintatapajono.getHakukohdeWrapper().getHakijaryhmaWrappers().isEmpty();

        //EHTO 1: jonon alimmalla hyväksytyllä jonosijalla olevat hakijat ovat hakijaryhmästä hyväksyttyjä
        //EHTO 2: edellämainitut hyväksytyt ovat hakijaryhmänsä alimmat hyväksytyt
        //EHTO 3: tässä hakijaryhmässä on hyväksytty hakijoita yli kiintiön verran
        boolean ehto1 = false;
        boolean ehto2 = false;
        boolean ehto3 = false;
        boolean kaikkiEhdot = false;
        Set<String> alimpienHyvaksyttyjenHakijaryhmat = new HashSet<>();
        List<Pair<String, Integer>> kiintionYlityksetRyhmittain = new ArrayList<>();

        if (hakukohteessaValintaryhmia) {

            List<HakemusWrapper> alimmallaSijallaOlevatHyvaksytyt = alimmallaHyvaksytyllaJonosijallaOlevatHyvaksytyt(valintatapajono);
            ehto1 = alimmallaSijallaOlevatHyvaksytyt.size() > 0
                    && alimmallaSijallaOlevatHyvaksytyt.get(0).isHyvaksyttyHakijaryhmastaTallaKierroksella();
            alimmallaSijallaOlevatHyvaksytyt.forEach(h -> alimpienHyvaksyttyjenHakijaryhmat.addAll(h.getHakemus().getHyvaksyttyHakijaryhmista()));

            /*if(alimpienHyvaksyttyjenHakijaryhmat.size() > 1) {
                LOG.warn(baseStr+"Alimmalla jonosijalla on hyväksyttyjä useista hakijaryhmistä: " + alimpienHyvaksyttyjenHakijaryhmat);
            }
*/
            int kiintionYlitys = hakukohteenKiintionYlitysValintaryhmalle(valintatapajono, alimpienHyvaksyttyjenHakijaryhmat.size() >= 1 ? alimpienHyvaksyttyjenHakijaryhmat.iterator().next() : null);
            for (String ryhmaOid: alimpienHyvaksyttyjenHakijaryhmat) {
                kiintionYlityksetRyhmittain.add(Pair.of(ryhmaOid, hakukohteenKiintionYlitysValintaryhmalle(valintatapajono, ryhmaOid)));
            }

            ehto2 = huonoimmatHakijaryhmastaHyvaksytytOvatTastaJonosta(valintatapajono, alimpienHyvaksyttyjenHakijaryhmat);
            ehto3 = kiintionYlitys> 0;

            kaikkiEhdot = ehto1 && ehto2 && ehto3;

            Pair<Integer, Integer> huonoinHakijaryhmastaHyvaksyttyJonosijaJaMaara = huonoimpienHakijaryhmastaHyvaksyttyjenJonosijaJaMaara(valintatapajono, alimpienHyvaksyttyjenHakijaryhmat);
            ehdollisetAloituspaikatTapa1 = kiintionYlitys; //hakijaryhmästä hyväksyttyjen ja hakijaryhmän kiintiö erotus
            ehdollisetAloituspaikatTapa2 = huonoinHakijaryhmastaHyvaksyttyJonosijaJaMaara.getRight() - 1; //alimmalla hyväksytyllä jonosijalla olevien määrä vähennettynä yhdellä

        }


        if (tilaa <= 0) {
            if (kaikkiEhdot) {
                List<HakemusWrapper> kiinnostavat = samallaTaiParemmallaJonosijallaOlevatHakijaryhmaanKuulumattomat(valintatapajono, huonoimpienHakijaryhmastaHyvaksyttyjenJonosijaJaMaara(valintatapajono, alimpienHyvaksyttyjenHakijaryhmat).getLeft());
                List<Pair<String, Integer>> paremmatHakemuksetJaJonosijat = kiinnostavat.stream().map(hw -> Pair.of(hw.getHakemus().getHakemusOid(), hw.getHakemus().getJonosija())).collect(Collectors.toList());

                LOG.info(baseStr + "valintatapajono " + valintatapajono.getValintatapajono().getOid() +
                        ", aloituspaikkoja: "+ aloituspaikat + ", valituiksi haluaa: " + valituksiHaluavatHakemukset.size() + ", tilaa (ilman mahdollisia lisäpaikkoja): " + tilaa +
                        ". Hakukohteessa valintaryhmiä: " + valintatapajono.getHakukohdeWrapper().getHakijaryhmaWrappers().size());
                LOG.info(baseStr+"Jono " + valintatapajono.getValintatapajono().getOid() + " on täysi. Ehdolliset lisäpaikat: " +
                        "(TAPA 1) " + ehdollisetAloituspaikatTapa1 + ", (TAPA 2): " + ehdollisetAloituspaikatTapa2 +
                        ", jos jonosija vähintään: " + huonoimpienHakijaryhmastaHyvaksyttyjenJonosijaJaMaara(valintatapajono, alimpienHyvaksyttyjenHakijaryhmat).getLeft() +
                        ", ylitykset ryhmittäin: "+ kiintionYlityksetRyhmittain);
                LOG.info(baseStr+"Hakemuksia jotka eivät kuulu valintaryhmiin ja joilla on parempi jonosija: " + paremmatHakemuksetJaJonosijat);
            }
            return muuttuneetHakukohteet;
        }
        /*else if (kaikkiEhdot){ TODO mietitään tätä keissiä myöhemmin; lisäpaikat voivat olla relevantteja myös sellaisessa jonossa, jossa on muutenkin tilaa, jos seuraavaksi valituiksi on tulossa
        isompi joukko tasasijalaisia kerralla.
            LOG.info(baseStr+"Jonossa " + valintatapajono.getValintatapajono().getOid() + " on tilaa. Ehdolliset lisäpaikat: " +
                    "(TAPA 1) " + ehdollisetAloituspaikatTapa1 + ", (TAPA 2): " + ehdollisetAloituspaikatTapa2 +
                    ", jos jonosija vähintään: " + huonoimpienHakijaryhmastaHyvaksyttyjenJonosijaJaMaara(valintatapajono, alimpienHyvaksyttyjenHakijaryhmat).getLeft() +
                    ", relevantit hakijaryhmät: " + alimpienHyvaksyttyjenHakijaryhmat);
        }*/
        Tasasijasaanto saanto = jononTasasijasaanto(valintatapajono);
        List<HakemusWrapper> kaikkiTasasijaHakemukset = getTasasijaHakemus(valituksiHaluavatHakemukset, saanto);
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

    private static List<HakemusWrapper> samallaTaiParemmallaJonosijallaOlevatHakijaryhmaanKuulumattomat(ValintatapajonoWrapper jono, int jonosija) {
        return jono.getHakemukset().stream()
                .filter(hw -> jono.getHakukohdeWrapper().getHakijaryhmaWrappers().stream().filter(hrw -> hrw.getHakijaryhma().getHakemusOid().contains(hw.getHakemus().getHakemusOid())).collect(Collectors.toList()).size() == 0)
                .filter(hw -> !kuuluuHyvaksyttyihinTiloihin(hw.getHakemus().getTila()))
                .filter(hw -> hw.getHakemus().getJonosija() <= jonosija)
                .collect(Collectors.toList());
    }

    private static List<HakemusWrapper> alimmallaHyvaksytyllaJonosijallaOlevatHyvaksytyt(ValintatapajonoWrapper wrapper) {
        Optional<HakemusWrapper> hakemusHyvaksytyistaMatalimmallaJonosijalla = wrapper.getHakemukset()
                .stream()
                .filter(h -> kuuluuHyvaksyttyihinTiloihin(hakemuksenTila(h)))
                .sorted((h1, h2) -> comparator.compare(h2, h1))
                .findFirst();
        if(hakemusHyvaksytyistaMatalimmallaJonosijalla.isPresent()) {
            HakemusWrapper h = hakemusHyvaksytyistaMatalimmallaJonosijalla.get();
            int matalinJonosija = h.getHakemus().getJonosija();
            List<HakemusWrapper> kaikkiAlimmallaJonosijalla = wrapper.getHakemukset().stream()
                    .filter(hw -> hw.getHakemus().getJonosija() == matalinJonosija)
                    .collect(Collectors.toList());
            //LOG.info("-- Alimmalla jonosijalla hakemuksia " + kaikkiAlimmallaJonosijalla.size() + ", jonosija: " + kaikkiAlimmallaJonosijalla.get(0).getHakemus().getJonosija());
            return kaikkiAlimmallaJonosijalla;
        }
        //LOG.warn(" -- Tässä vaiheessa ei vaikuttaisi olevan hyväksyttyjä hakemuksia ");
        return Collections.emptyList();
    }

    private static int hakijaryhmastaHyvaksyttyjaHakukohteenKaikissaJonoissa(HakukohdeWrapper hw, String hakijaryhmaOid) {
       List<ValintatapajonoWrapper> kaikkiHakukohteenJonot = hw.getValintatapajonot();

       int hyvaksyttyjaYhteensa = 0;
       //lasketaan hakijaryhmästä hyväksytyt kaikista hakukohteen jonoista yhteensä
       for (ValintatapajonoWrapper jonoWrapper : kaikkiHakukohteenJonot) {
           hyvaksyttyjaYhteensa += jonoWrapper.getHakemukset()
                   .stream()
                   .filter(h -> h.getHakemus().getHyvaksyttyHakijaryhmista().contains(hakijaryhmaOid))
                   .collect(Collectors.toList()).size();
       }
       return hyvaksyttyjaYhteensa;
    }

    private static boolean huonoimmatHakijaryhmastaHyvaksytytOvatTastaJonosta(ValintatapajonoWrapper tamaJono, Set<String> hakijaryhmaOids) {
        int huonoinJonosija = -1;
        int huonoinJonoPrioriteetti = -1; //tiebreaker jos sama jonosija
        List<ValintatapajonoWrapper> kaikkiJonot = tamaJono.getHakukohdeWrapper().getValintatapajonot();
        for(ValintatapajonoWrapper ehdokasJono : kaikkiJonot) {
            int huonoinJonosijaTastaJonosta = huonoimpienHakijaryhmastaHyvaksyttyjenJonosijaJaMaara(ehdokasJono, hakijaryhmaOids).getLeft();
            if(huonoinJonosijaTastaJonosta >= huonoinJonosija) {
                if(huonoinJonosijaTastaJonosta == huonoinJonosija) {
                    if(huonoinJonoPrioriteetti < ehdokasJono.getValintatapajono().getPrioriteetti()) {
                        huonoinJonoPrioriteetti = ehdokasJono.getValintatapajono().getPrioriteetti(); //Löytyi sama jonosija heikomman prioriteetin jonosta, päivitetään siihen
                    }
                } else {
                    huonoinJonosija = huonoinJonosijaTastaJonosta; //Löytyi heikompi jonosija, päivitetään siihen
                    huonoinJonoPrioriteetti = ehdokasJono.getValintatapajono().getPrioriteetti();
                }
            }
        }

        return (huonoinJonosija == huonoimpienHakijaryhmastaHyvaksyttyjenJonosijaJaMaara(tamaJono, hakijaryhmaOids).getLeft()
                && huonoinJonoPrioriteetti == tamaJono.getValintatapajono().getPrioriteetti());

    }

    private static int hakukohteenKiintionYlitysValintaryhmalle(ValintatapajonoWrapper tutkittavaJono, String ryhmaOid) {
        String useOid = ryhmaOid != null ? ryhmaOid : ""; //ainakin testeissä näitä liikkuu nulleina, vaikka ei kai siinä järkeä ole
        int hyvaksyttyjaHakijaryhmasta = hakijaryhmastaHyvaksyttyjaHakukohteenKaikissaJonoissa(tutkittavaJono.getHakukohdeWrapper(), useOid);

        Optional<HakijaryhmaWrapper> relevantHW = tutkittavaJono.getHakukohdeWrapper().getHakijaryhmaWrappers().stream()
                .filter(w -> useOid.equals(w.getHakijaryhma().getOid())).findFirst();

        if(relevantHW.isPresent()) {
            int hakijaryhmaKiintio = relevantHW.get().getHakijaryhma().getKiintio();
            //LOG.debug("Returning, hyvaksyttyjaHakijaryhmasta:" + hyvaksyttyjaHakijaryhmasta + ", kiintiö:" + hakijaryhmaKiintio );
            return hyvaksyttyjaHakijaryhmasta - hakijaryhmaKiintio;
        } else {
            return -1;
        }

    }

    private static Pair<Integer, Integer> huonoimpienHakijaryhmastaHyvaksyttyjenJonosijaJaMaara(ValintatapajonoWrapper valintatapajonoWrapper, Set<String> hakijaryhmaOids) {
        Optional<HakemusWrapper> huonoin = valintatapajonoWrapper.getHakemukset()
                .stream()
                .filter(hw -> hakijaryhmaOids.containsAll(hw.getHakemus().getHyvaksyttyHakijaryhmista()))
                .filter(HakemusWrapper::isHyvaksyttyHakijaryhmastaTallaKierroksella).min((h1, h2) -> comparator.compare(h2, h1));

        final int viimeinenJonosija = huonoin.isPresent() ? huonoin.get().getHakemus().getJonosija() : -1;
        int hakemuksiaViimeisellaJonosijalla = valintatapajonoWrapper.getHakemukset()
                .stream()
                .filter(h -> h.getHakemus().getJonosija() == viimeinenJonosija)
                .collect(Collectors.toList()).size();

        return Pair.of(viimeinenJonosija, hakemuksiaViimeisellaJonosijalla);
    }

    static boolean eiPeruttuaKorkeampaaTaiSamaaHakutoivetta(HakemusWrapper hakemusWrapper) {
        List<HakemuksenTila> perututHakemuksenTilat = Arrays.asList(HakemuksenTila.PERUNUT, HakemuksenTila.PERUUTETTU);
        return hakemusWrapper.getHenkilo().getHakemukset()
            .stream()
            .filter(h -> h != hakemusWrapper)
            .noneMatch(h -> perututHakemuksenTilat.contains(hakemuksenTila(h)) && hakemuksenPrioriteetti(h) <= hakemuksenPrioriteetti(hakemusWrapper));
    }
}
