package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.hakukohteet;

import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.hakukohteet.SijoitteleHakukohde.hakijaHaluaa;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.hakukohteet.SijoitteleHakukohde.hyvaksyHakemus;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.hakukohteet.SijoitteleHakukohde.saannotSallii;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.hakukohteet.SijoitteleHakukohde.uudelleenSijoiteltavatHakukohteet;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.hakukohteet.SijoitteleHakukohde.hyvaksyttyJotaEiVoiKorvata;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilaTaulukot.kuuluuHylattyihinTiloihin;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilaTaulukot.kuuluuHyvaksyttyihinTiloihin;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilojenMuokkaus.asetaTilaksiVaralla;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.WrapperHelperMethods.hakemuksenHakemusOid;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.WrapperHelperMethods.hakemuksenTila;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.WrapperHelperMethods.jononTasasijasaanto;
import com.google.common.collect.Lists;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.comparator.HakemusWrapperComparator;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakijaryhmaWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakukohdeWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.WrapperHelperMethods;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Tasasijasaanto;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

class SijoitteleHakijaryhma {
    private static final Logger LOG = LoggerFactory.getLogger(SijoitteleHakijaryhma.class);

    static Set<HakukohdeWrapper> sijoitteleHakijaryhma(SijoitteluajoWrapper sijoitteluAjo, HakijaryhmaWrapper hakijaryhmaWrapper) {
        // Hakijaryhmäsijoittelu ei uudelleenkäsittele jo hyväksyttyjä hakijoita, jolloin
        // hyväksytty hakijaryhmästä -merkintä jää asettamatta. Poistetaan mahdollisesti vanhentuneet merkinnät,
        // ja merkitään hakijaryhmästä hyväksytyt erillisellä algoritmilla.
        String hakijaryhmaOid = hakijaryhmaWrapper.getHakijaryhma().getOid();
        hakijaryhmaWrapper.getHakukohdeWrapper().hakukohteenHakemukset().map(HakemusWrapper::getHakemus).forEach(h ->
            h.getHyvaksyttyHakijaryhmista().remove(hakijaryhmaOid));
        merkitseHakijaryhmastaHyvaksytyt(sijoitteluAjo, hakijaryhmaWrapper);
        List<HakemusWrapper> muuttuneet = sijoitteleHakijaryhmaRecur(sijoitteluAjo, hakijaryhmaWrapper);
        for (HakemusWrapper h : muuttuneet) {
            if (kuuluuHyvaksyttyihinTiloihin(h.getHakemus().getTila()) && !h.getHakemus().getHyvaksyttyHakijaryhmista().contains(hakijaryhmaOid)) {
                LOG.error(String.format(
                    "Hakukohteen %s hakijaryhmän %s sijoittelussa jonosta %s hyväksytty hakemus %s ei ole merkitty hakijaryhmästä hyväksytyksi",
                    hakijaryhmaWrapper.getHakukohdeWrapper().getHakukohde().getOid(),
                    hakijaryhmaOid,
                    h.getValintatapajono().getValintatapajono().getOid(),
                    h.getHakemus().getHakemusOid()
                ));
            }
        }
        hakijaryhmaWrapper.getHakukohdeWrapper().hakukohteenHakemukset().forEach(h -> {
            if (h.getHakemus().getHyvaksyttyHakijaryhmista().contains(hakijaryhmaOid) &&
                !kuuluuHyvaksyttyihinTiloihin(h.getHakemus().getTila())) {
                // Hakijaryhmäsijoittelu on laskenut jonosijoittelussa hyväksyttyjä hakemuksia hakijaryhmäkiintiöön.
                // Tästä (tai jostain tunnistamattomasta syystä) johtuen hakemus h on merkattu hakijaryhmästä
                // hyväksytyksi, mutta hakijaryhmäsijoittelu ei ole hyväksynyt sitä, koska hakijaryhmäkiintiön on
                // katsottu olevan täynnä. Logitetaan virhe ja poistetaan merkintä jotta vain hyväksytyt ovat merkkity.
                Valintatapajono valintatapajono = h.getValintatapajono().getValintatapajono();
                LOG.error(String.format(
                    "Hakukohteen %s hakijaryhmästä %s jonossa %s hyväksytyksi merkitty hakemus %s on tilassa %s . " +
                    "Jonolla kaikkiEhdonTayttavatHyvaksytaan=%s, eiVarasijatayttoa=%s, varasijat=%s, tasasijasääntö=%s",
                    hakijaryhmaWrapper.getHakukohdeWrapper().getHakukohde().getOid(),
                    hakijaryhmaOid,
                    valintatapajono.getOid(),
                    h.getHakemus().getHakemusOid(),
                    h.getHakemus().getTila(),
                    valintatapajono.getKaikkiEhdonTayttavatHyvaksytaan(),
                    valintatapajono.getEiVarasijatayttoa(),
                    valintatapajono.getVarasijat(),
                    valintatapajono.getTasasijasaanto()
                ));
                h.getHakemus().getHyvaksyttyHakijaryhmista().remove(hakijaryhmaOid);
            }
        });
        return uudelleenSijoiteltavatHakukohteet(muuttuneet);
    }

    static boolean voidaanKorvata(HakemusWrapper hakemusWrapper, HakijaryhmaWrapper korvaavaRyhma) {
        boolean voidaanKorvata = true;
        // Tilaa ei voi vaihtaa, ei voida enää korvata käynnissä olevan sijoitteluajon aikana
        if (!hakemusWrapper.isTilaVoidaanVaihtaa()) {
            voidaanKorvata = false;
        }
        // Hyväksytty ylemmän prioriteetin hakijaryhmästä, ei voida enää korvata käynnissä olevan ajokierroksen aikana
        if (hakemusWrapper.isHyvaksyttyHakijaryhmastaTallaKierroksella() || hakemusWrapper.merkittyAiemminHyvaksytyksiKorkeammanPrioriteetinRyhmastaKuin(korvaavaRyhma)) {
            voidaanKorvata = false;
        }
        return voidaanKorvata;
    }

    private static void merkitseHakijaryhmastaHyvaksytyt(SijoitteluajoWrapper sijoitteluajo, HakijaryhmaWrapper hakijaryhmaWrapper) {
        Set<String> hakijaryhmaanKuuluvat = new HashSet<>(hakijaryhmaWrapper.getHakijaryhma().getHakemusOid());
        HyvaksyComparator ylimmanPrioriteetinJonoJossaYlimmallaJonosijallaOlevaHakijaEnsin = new HyvaksyComparator();
        VaralleComparator alimmanPrioriteetinJonoJossaAlimmallaJonosijallaOlevaHakijaEnsin = new VaralleComparator();
        List<HakijaryhmanValintatapajono> valintatapajonot = hakijaryhmaanLiittyvatJonot(hakijaryhmaWrapper).stream()
                .map(j -> new HakijaryhmanValintatapajono(sijoitteluajo, hakijaryhmaanKuuluvat, j, hakijaryhmaWrapper))
                .collect(Collectors.toList());
        String hakijaryhmaOid = hakijaryhmaWrapper.getHakijaryhma().getOid();
        int kiintio = hakijaryhmaWrapper.getHakijaryhma().getKiintio();
        boolean aloituspaikkojaVielaJaljella = true;
        while (laskeHakijaryhmastaHyvaksytyt(valintatapajonot) < kiintio && aloituspaikkojaVielaJaljella) {
            boolean hyvaksyttiin = false;
            valintatapajonot.sort(ylimmanPrioriteetinJonoJossaYlimmallaJonosijallaOlevaHakijaEnsin);
            for (HakijaryhmanValintatapajono jono : valintatapajonot) {
                if (!hyvaksyttiin) {
                    int kiintiotaJaljella = kiintio - laskeHakijaryhmastaHyvaksytyt(valintatapajonot);
                    List<Hakemus> hyvaksytyt = jono.hyvaksyAloituspaikkoihinJaKiintioonMahtuvatParhaallaJonosijallaOlevat(kiintiotaJaljella);
                    if (!hyvaksytyt.isEmpty()) {
                        Set<String> oidit = hyvaksytyt.stream().map(Hakemus::getHakemusOid).collect(Collectors.toSet());
                        valintatapajonot.stream().filter(j -> j.prioriteetti > jono.prioriteetti).forEach(j ->
                            j.kirjanpito.poistaHyvaksytyistaJaHyvaksyttavista(oidit));
                        hyvaksyttiin = true;
                    }
                }
            }
            if (!hyvaksyttiin) {
                // Toisen hakukohteen sijoittelu on voinut PERUUNNUTTAA tähän hakijaryhmään kuuluvan aiemmin hyväksytyn
                // hakijan. Ylitäytön takia jonon aloituspaikat voivat silti olla täynnä. Tässä tapauksessa siirretään
                // varalle hakijaryhmään kuulumattomia hakijoita, jotta hakijaryhmäkiintiö saataisiin täyteen.
                aloituspaikkojaVielaJaljella = valintatapajonot.stream()
                        .filter(j -> j.tasasijasaanto == Tasasijasaanto.YLITAYTTO)
                        .sorted(alimmanPrioriteetinJonoJossaAlimmallaJonosijallaOlevaHakijaEnsin)
                        .findFirst()
                        .map(HakijaryhmanValintatapajono::siirraVaralleAlimmallaJonosijallaOlevatHakijaryhmanUlkopuolisetHyvaksytyt)
                        .orElse(false);
            }
        }
        valintatapajonot.forEach(jono -> jono.kirjanpito.merkitseHyvaksytyksiHakijaryhmasta(hakijaryhmaOid));
    }

    private static int laskeHakijaryhmastaHyvaksytyt(List<HakijaryhmanValintatapajono> valintatapajonot) {
        return valintatapajonot.stream().mapToInt(v -> v.kirjanpito.countHakijaryhmastaHyvaksytyt()).sum();
    }

    private static List<HakemusWrapper> sijoitteleHakijaryhmaRecur(SijoitteluajoWrapper sijoitteluAjo, HakijaryhmaWrapper hakijaryhmaWrapper) {
        final List<ValintatapajonoWrapper> liittyvatJonot = hakijaryhmaanLiittyvatJonot(hakijaryhmaWrapper);
        final List<HakemusWrapper> hakemusWrappers = liittyvatJonot.stream().flatMap(j -> j.getHakemukset().stream()).collect(Collectors.toList());
        final List<HakemusWrapper> ryhmaanKuuluvat = hakijaRyhmaanKuuluvat(hakemusWrappers, hakijaryhmaWrapper);
        final long hyvaksyttyjenMaara = ryhmaanKuuluvat.stream()
            .filter(h -> hyvaksyttyJotaEiVoiKorvata(h.getValintatapajono(), sijoitteluAjo).test(h))
            .count();
        final boolean tarkkaKiintio = hakijaryhmaWrapper.getHakijaryhma().isTarkkaKiintio();
        final int aloituspaikat = sisaltaaSuoravalintajonon(liittyvatJonot) ? Integer.MAX_VALUE : liittyvatJonot.stream().mapToInt(WrapperHelperMethods::jononAloituspaikat).sum();

        int kiintio = hakijaryhmaWrapper.getHakijaryhma().getKiintio();
        if (kiintio > aloituspaikat) {
            // hakijaryhmän kiintiö on suurempi kuin mahdolliset aloituspaikat, asetetaan kiintiöksi aloituspaikka määrä
            kiintio = aloituspaikat;
        }

        if (tarkkaKiintio && hyvaksyttyjenMaara >= kiintio) {
            asetaEiHyvaksyttavissaHakijaryhmanJalkeen(sijoitteluAjo, ryhmaanKuuluvat);
            return new ArrayList<>();
        } else if (hyvaksyttyjenMaara < kiintio) {
            final List<ValintatapajonoWrapper> liittyvatJonotVarasijatayttoVoimassa = liittyvatJonot.stream()
                    .filter(vtj -> !sijoitteluAjo.onkoVarasijaSaannotVoimassaJaVarasijaTayttoPaattynyt(vtj))
                    .collect(Collectors.toList());
            return kasitteleValituksiHaluavat(sijoitteluAjo, hakijaryhmaWrapper, liittyvatJonotVarasijatayttoVoimassa, ryhmaanKuuluvat);
        }
        return new ArrayList<>();
    }

    private static boolean sisaltaaSuoravalintajonon(List<ValintatapajonoWrapper> liittyvatJonot) {
        return liittyvatJonot.stream().anyMatch(j -> BooleanUtils.isTrue(j.getValintatapajono().getKaikkiEhdonTayttavatHyvaksytaan()));
    }

    private static int jononAloituspaikatSuoravalintajonoHuomioiden(ValintatapajonoWrapper jono) {
        return BooleanUtils.isTrue(jono.getValintatapajono().getKaikkiEhdonTayttavatHyvaksytaan()) ? Integer.MAX_VALUE : WrapperHelperMethods.jononAloituspaikat(jono);
    }

    private static List<ValintatapajonoWrapper> hakijaryhmaanLiittyvatJonot(HakijaryhmaWrapper hakijaryhmaWrapper) {
        String jonoId = hakijaryhmaWrapper.getHakijaryhma().getValintatapajonoOid();
        if (jonoId != null) {
            return hakijaryhmaWrapper.getHakukohdeWrapper().getValintatapajonot()
                    .stream()
                    .filter(j -> j.getValintatapajono().getOid().equals(jonoId))
                    .collect(Collectors.toList());
        } else {
            return hakijaryhmaWrapper.getHakukohdeWrapper().getValintatapajonot();
        }
    }

    private static List<HakemusWrapper> hakijaRyhmaanKuuluvat(List<HakemusWrapper> hakemus, HakijaryhmaWrapper ryhma) {
        return hakemus.stream()
                .filter(h -> ryhma.getHenkiloWrappers().contains(h.getHenkilo()))
                .collect(Collectors.toList());
    }

    private static void asetaEiHyvaksyttavissaHakijaryhmanJalkeen(SijoitteluajoWrapper sijoitteluAjo, List<HakemusWrapper> ryhmaanKuuluvat) {
        ryhmaanKuuluvat
                .stream()
                .filter(h -> hakemuksenTila(h).equals(HakemuksenTila.VARALLA))
                .filter(h -> hakijaHaluaa(h) && saannotSallii(h, sijoitteluAjo))
                .forEach(h -> h.setHyvaksyttavissaHakijaryhmanJalkeen(false));
    }

    private static List<HakemusWrapper> kasitteleValituksiHaluavat(SijoitteluajoWrapper sijoitteluAjo,
                                                                   HakijaryhmaWrapper hakijaryhmaWrapper,
                                                                   List<ValintatapajonoWrapper> liittyvatJonot,
                                                                   List<HakemusWrapper> ryhmaanKuuluvat) {
        List<HakemusWrapper> muuttuneetHakemukset = new ArrayList<>();
        List<Valintatapajono> valintatapajonot = liittyvatJonot.stream().map(ValintatapajonoWrapper::getValintatapajono).collect(Collectors.toList());
        List<HakemusWrapper> valituksiHaluavat = ryhmaanKuuluvat
                .stream()
                .filter(h -> valintatapajonot.contains(h.getValintatapajono().getValintatapajono())) // Varmistetaan, että hakemuksen valintatapajonon varasijasäännöt täyttyvät
                .filter(h -> hakemuksenTila(h).equals(HakemuksenTila.VARALLA))
                .filter(h -> hakijaHaluaa(h) && saannotSallii(h, sijoitteluAjo))
                .collect(Collectors.toList());
        final long ryhmastaHyvaksyttyjenMaara = ryhmaanKuuluvat.stream()
            .filter(h -> hyvaksyttyJotaEiVoiKorvata(h.getValintatapajono(), sijoitteluAjo).test(h))
            .count();

        if (!valituksiHaluavat.isEmpty()) {
            tarkistaEttaKaikkienTilaaVoidaanVaihtaa(valituksiHaluavat);

            Pair<List<HakemusWrapper>, List<HakemusWrapper>> valittavatJaVarasijat = seuraavaksiParhaatHakijaryhmasta(valituksiHaluavat, hakijaryhmaWrapper, sijoitteluAjo);

            // Aloituspaikat täynnä ylitäytöllä, joten tiputetaan varalle
            valittavatJaVarasijat.getRight().forEach(v -> {
                ValintatapajonoWrapper valintatapajonoWrapper = v.getValintatapajono();
                int aloituspaikat = jononAloituspaikatSuoravalintajonoHuomioiden(valintatapajonoWrapper);
                long hyvaksytyt = valintatapajonoWrapper.getHakemukset().stream().filter(hyvaksyttyJotaEiVoiKorvata(valintatapajonoWrapper, sijoitteluAjo)).count();

                LOG.info(String.format("Hakukohteen %s hakijaryhmän %s sijoittelussa asetetaan varalle jonon %s hakemus %s . " +
                        "Hakijaryhmän täyttöaste %s/%s, jonon täyttöaste %s/%s",
                        hakijaryhmaWrapper.getHakukohdeWrapper().getHakukohde().getOid(),
                        hakijaryhmaWrapper.getHakijaryhma().getOid(),
                        v.getValintatapajono().getValintatapajono().getOid(),
                        v.getHakemus().getHakemusOid(),
                        ryhmastaHyvaksyttyjenMaara, hakijaryhmaWrapper.getHakijaryhma().getKiintio(),
                        hyvaksytyt, aloituspaikat
                ));
                muuttuneetHakemukset.addAll(asetaVaralleHakemus(v));
            });
            // Hyväksytään valittavat
            valittavatJaVarasijat.getLeft().forEach(h -> {
                h.setHyvaksyttyHakijaryhmastaTallaKierroksella(true);
                muuttuneetHakemukset.addAll(hyvaksyHakemus(sijoitteluAjo, h));
            });
            boolean lukko = liittyvatJonot.stream().anyMatch(ValintatapajonoWrapper::isAlitayttoLukko);
            // Kiintiö ei täyty, koska alitäyttö
            if (!lukko && (!valittavatJaVarasijat.getLeft().isEmpty() || !valittavatJaVarasijat.getRight().isEmpty())) {
                muuttuneetHakemukset.addAll(sijoitteleHakijaryhmaRecur(sijoitteluAjo, hakijaryhmaWrapper));
            }
        } else {
            LOG.info(String.format("Hakukohteen %s hakijaryhmän %s sijoittelussa ei löytynyt lisää hyväksyttäviä hakijaryhmästä, " +
                    "vaikka ryhmän täyttöaste on vasta %s/%s !",
                    hakijaryhmaWrapper.getHakukohdeWrapper().getHakukohde().getOid(),
                    hakijaryhmaWrapper.getHakijaryhma().getOid(),
                    ryhmastaHyvaksyttyjenMaara, hakijaryhmaWrapper.getHakijaryhma().getKiintio()));
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("SijoitteleHakijaryhma.kasitteleValituksiHaluavat: muuttuneetHakemukset.size() == " + muuttuneetHakemukset.size());
        }
        return muuttuneetHakemukset;
    }

    private static void tarkistaEttaKaikkienTilaaVoidaanVaihtaa(List<HakemusWrapper> valituksiHaluavat) {
        List<HakemusWrapper> valituiksiHaluavatJoidenTilaaEiVoiVaihtaa = valituksiHaluavat.stream().filter(h -> !h.isTilaVoidaanVaihtaa()).collect(Collectors.toList());
        if (!valituiksiHaluavatJoidenTilaaEiVoiVaihtaa.isEmpty()) {
            StringBuilder errorMessage =  new StringBuilder(String.format("Löytyy %d hakemusta, joiden tilaa ei voi vaihtaa:", valituiksiHaluavatJoidenTilaaEiVoiVaihtaa.size()));
            for (HakemusWrapper hakemusWrapper : valituiksiHaluavatJoidenTilaaEiVoiVaihtaa) {
                String message = "[Hakemus: " + hakemusWrapper.getHakemus().getHakemusOid() +
                        ", hakija: " + hakemusWrapper.getHakemus().getHakijaOid() +
                        ", tila: " + hakemusWrapper.getHakemus().getTila() +
                        ", edellinen tila: " + hakemusWrapper.getHakemus().getEdellinenTila() +
                        ", hakukohde: " + hakemusWrapper.getHakukohdeOid() +
                        ", valintatapajono: " + hakemusWrapper.getValintatapajono().getValintatapajono().getOid() + "]";
                errorMessage.append(message).append(", ");
            }
            throw new IllegalStateException(errorMessage.toString());
        }
    }

    private static Pair<List<HakemusWrapper>, List<HakemusWrapper>> seuraavaksiParhaatHakijaryhmasta(List<HakemusWrapper> valituksiHaluavat,
                                                                                                     HakijaryhmaWrapper hakijaryhmaWrapper,
                                                                                                     SijoitteluajoWrapper sijoitteluajoWrapper) {
        HakemusWrapperComparator comparator = new HakemusWrapperComparator();
        // Valituksihaluavat jonoittain
        Map<ValintatapajonoWrapper, List<HakemusWrapper>> jonoittain = valituksiHaluavat
                .stream()
                .sorted(comparator)
                .collect(Collectors.groupingBy(HakemusWrapper::getValintatapajono));
        // Jonojen parhaat jonosijoittain
        Map<Integer, List<HakemusWrapper>> jonojenParhaat = jonoittain.values()
                .stream()
                .map(l -> l.get(0))
                .collect(Collectors.groupingBy(h -> h.getHakemus().getJonosija()));
        List<HakemusWrapper> valittavat = etsiValittavat(jonoittain, jonojenParhaat, sijoitteluajoWrapper);
        List<HakemusWrapper> varalleAsetetut = Lists.newArrayList();
        if (valittavat.isEmpty()) {
            // jossain on ylitäyttö ja pitäis saada hyväksytyks tai alitäyttö lukko
            List<ValintatapajonoWrapper> ylitayttoJonot = jonoittain.keySet()
                    .stream()
                    .filter(v -> jononTasasijasaanto(v).equals(Tasasijasaanto.YLITAYTTO))
                    .collect(Collectors.toList());
            if (!ylitayttoJonot.isEmpty()) {
                ylitayttoJonot.forEach(j -> {
                    List<HakemusWrapper> korvattavat = haeHuonoimmatValitutJotkaVoidaanKorvata(j, hakijaryhmaWrapper);
                    if (!korvattavat.isEmpty()) {
                        varalleAsetetut.addAll(korvattavat);
                    }
                });
            } else {
                jonoittain.keySet().forEach(j -> {
                    if (jononTasasijasaanto(j).equals(Tasasijasaanto.ALITAYTTO)) {
                        j.setAlitayttoLukko(true);
                    }
                });
            }
        }
        return Pair.of(valittavat, varalleAsetetut);
    }

    private static List<HakemusWrapper> etsiValittavat(Map<ValintatapajonoWrapper, List<HakemusWrapper>> jonoittain,
                                                       Map<Integer, List<HakemusWrapper>> jonojenParhaat,
                                                       SijoitteluajoWrapper sijoitteluajoWrapper) {
        for (Integer i : new TreeSet<>(jonojenParhaat.keySet())) {
            List<HakemusWrapper> parhaat = jonojenParhaat.get(i);
            if (parhaat.size() == 1) {
                // Vain yhdestä jonosta löytyi hakemus tältä sijalta
                HakemusWrapper paras = parhaat.get(0);
                List<HakemusWrapper> parhaatJonoonMahtuvat = haeParhaatJonoonMahtuvat(jonoittain, paras, sijoitteluajoWrapper);
                if (!parhaatJonoonMahtuvat.isEmpty()) {
                    return parhaatJonoonMahtuvat;
                }
            } else {
                List<HakemusWrapper> jononMukaanSortattu = parhaat
                        .stream()
                        .sorted(Comparator.comparingInt(WrapperHelperMethods::jononPrioriteetti))
                        .collect(Collectors.toList());
                for (HakemusWrapper paras : jononMukaanSortattu) {
                    List<HakemusWrapper> parhaatJonoonMahtuvat = haeParhaatJonoonMahtuvat(jonoittain, paras, sijoitteluajoWrapper);
                    if (!parhaatJonoonMahtuvat.isEmpty()) {
                        return parhaatJonoonMahtuvat;
                    }
                }
            }
        }
        return Lists.newArrayList();
    }

    private static List<HakemusWrapper> haeParhaatJonoonMahtuvat(Map<ValintatapajonoWrapper, List<HakemusWrapper>> jonoittain,
                                                                 HakemusWrapper paras,
                                                                 SijoitteluajoWrapper sijoitteluajoWrapper) {
        List<HakemusWrapper> samallaSijalla =
                samallaSijalla(paras, jonoittain.get(paras.getValintatapajono()), jononTasasijasaanto(paras.getValintatapajono()));
        if (mahtuukoJonoon(samallaSijalla, paras.getValintatapajono(), sijoitteluajoWrapper)) {
            return samallaSijalla;
        } else {
            return Lists.newArrayList();
        }
    }

    private static List<HakemusWrapper> samallaSijalla(HakemusWrapper hakemus, List<HakemusWrapper> hakemukset, Tasasijasaanto saanto) {
        if (saanto.equals(Tasasijasaanto.ARVONTA)) {
            return Collections.singletonList(hakemus);
        } else {
            return hakemukset
                    .stream()
                    .filter(h -> h.getHakemus().getJonosija().equals(hakemus.getHakemus().getJonosija()))
                    .collect(Collectors.toList());
        }

    }

    private static boolean mahtuukoJonoon(List<HakemusWrapper> hakemukset,
                                          ValintatapajonoWrapper valintatapajonoWrapper,
                                          SijoitteluajoWrapper sijoitteluajoWrapper) {
        int aloituspaikat = jononAloituspaikatSuoravalintajonoHuomioiden(valintatapajonoWrapper);
        long hyvaksytyt = valintatapajonoWrapper.getHakemukset().stream().filter(hyvaksyttyJotaEiVoiKorvata(valintatapajonoWrapper, sijoitteluajoWrapper)).count();
        if (aloituspaikat - hyvaksytyt <= 0) {
            return false;
        } else {
            if (jononTasasijasaanto(valintatapajonoWrapper).equals(Tasasijasaanto.YLITAYTTO)) {
                return true;
            } else return aloituspaikat - hyvaksytyt - hakemukset.size() >= 0;
        }
    }

    private static void logError(String virhe, String hakemusOid, String hakukohdeOid) {
        LOG.error(virhe, hakemusOid, hakukohdeOid);
        throw new RuntimeException("Virheellinen tila hakemuksella asetettaessa varalle");
    }

    private static Set<HakemusWrapper> asetaVaralleHakemus(HakemusWrapper varalleAsetettavaHakemusWrapper) {
        Set<HakemusWrapper> uudelleenSijoiteltavatHakukohteet = new HashSet<>();
        if (!varalleAsetettavaHakemusWrapper.isTilaVoidaanVaihtaa()) {
            logError("Hakemuksta {} hakukohteessa {} yritetään asettaa varalle, mutta hakemuksen tilaa ei voida vaihtaa",
                    varalleAsetettavaHakemusWrapper.getHakemus().getHakemusOid(),
                    varalleAsetettavaHakemusWrapper.getHakukohdeOid());
        } else if (!kuuluuHyvaksyttyihinTiloihin(hakemuksenTila(varalleAsetettavaHakemusWrapper))) {
            logError("Hakemuksta {} hakukohteessa {} yritetään asettaa varalle, mutta hakemus ei kuulu hyväksyttyihin tiloihin",
                    varalleAsetettavaHakemusWrapper.getHakemus().getHakemusOid(),
                    varalleAsetettavaHakemusWrapper.getHakukohdeOid());
        }
        for (HakemusWrapper hakemusWrapper : varalleAsetettavaHakemusWrapper.getHenkilo().getHakemukset()) {
            if (!kuuluuHylattyihinTiloihin(hakemuksenTila(hakemusWrapper))) {
                // Mikäli valintatulos löytyy varalle asetettavalta hakemukselta, poistetaan se
                if (hakemusWrapper.getValintatulos().isPresent() && kuuluuHyvaksyttyihinTiloihin(hakemuksenTila(hakemusWrapper))) {
                    hakemusWrapper.getValintatulos().get().setTila(ValintatuloksenTila.KESKEN, "Asetettu varalle");
                }
                asetaTilaksiVaralla(hakemusWrapper);
                hakemusWrapper.setTilaVoidaanVaihtaa(true);
                uudelleenSijoiteltavatHakukohteet.add(hakemusWrapper);
            }
        }
        return uudelleenSijoiteltavatHakukohteet;
    }

    private static List<HakemusWrapper> haeHuonoimmatValitutJotkaVoidaanKorvata(ValintatapajonoWrapper valintatapajonoWrapper, HakijaryhmaWrapper hakijaryhmaWrapper) {
        HakemusWrapperComparator comparator = new HakemusWrapperComparator();
        List<HakemusWrapper> korvattavat = haeHyvaksytytEiHakijaryhmaanKuuluvat(valintatapajonoWrapper, hakijaryhmaWrapper);
        Optional<HakemusWrapper> huonoinHakemus = korvattavat
                .stream()
                .sorted((h1, h2) -> comparator.compare(h2, h1))
                .findFirst();
        if (huonoinHakemus.isPresent()) {
            HakemusWrapper huonoin = huonoinHakemus.get();
            return korvattavat
                    .stream()
                    .filter(h -> h.getHakemus().getJonosija().equals(huonoin.getHakemus().getJonosija()))
                    .collect(Collectors.toList());
        } else {
            return Lists.newArrayList();
        }

    }

    private static List<HakemusWrapper> haeHyvaksytytEiHakijaryhmaanKuuluvat(ValintatapajonoWrapper valintatapajonoWrapper, HakijaryhmaWrapper hakijaryhmaWrapper) {
        return valintatapajonoWrapper.getHakemukset()
                .stream()
                .filter(h -> kuuluuHyvaksyttyihinTiloihin(hakemuksenTila(h)) && voidaanKorvata(h, hakijaryhmaWrapper))
                .filter(h -> !hakijaryhmaWrapper.getHakijaryhma().getHakemusOid().contains(hakemuksenHakemusOid(h)))
                .collect(Collectors.toList());
    }

    private static class HyvaksyComparator implements Comparator<HakijaryhmanValintatapajono> {
        @Override
        public int compare(HakijaryhmanValintatapajono j, HakijaryhmanValintatapajono jj) {
            if (j.kirjanpito.eiKetaanHyvaksyttavissaHakijaryhmasta() && jj.kirjanpito.eiKetaanHyvaksyttavissaHakijaryhmasta()) {
                return 0;
            }
            if (j.kirjanpito.eiKetaanHyvaksyttavissaHakijaryhmasta()) {
                return 1;
            }
            if (jj.kirjanpito.eiKetaanHyvaksyttavissaHakijaryhmasta()) {
                return -1;
            }
            int c = Integer.compare(
                    j.kirjanpito.ensimmainenHakijaryhmastaHyvaksyttavissaOleva().getJonosija(),
                    jj.kirjanpito.ensimmainenHakijaryhmastaHyvaksyttavissaOleva().getJonosija()
            );
            return c == 0 ? Integer.compare(j.prioriteetti, jj.prioriteetti) : c;
        }
    }

    private static class VaralleComparator implements Comparator<HakijaryhmanValintatapajono> {
        @Override
        public int compare(HakijaryhmanValintatapajono j, HakijaryhmanValintatapajono jj) {
            if (j.kirjanpito.eiOleHakijaryhmanUlkopuoleltaHyvaksyttyjaJoitaVoidaanSiirtaaVaralle() &&
                jj.kirjanpito.eiOleHakijaryhmanUlkopuoleltaHyvaksyttyjaJoitaVoidaanSiirtaaVaralle()) {
                return 0;
            }
            if (j.kirjanpito.eiOleHakijaryhmanUlkopuoleltaHyvaksyttyjaJoitaVoidaanSiirtaaVaralle()) {
                return 1;
            }
            if (jj.kirjanpito.eiOleHakijaryhmanUlkopuoleltaHyvaksyttyjaJoitaVoidaanSiirtaaVaralle()) {
                return -1;
            }
            int c = Integer.compare(
                    jj.kirjanpito.viimeinenHakijaryhmanUlkopuoleltaHyvaksyttyJokaVoidaanSiirtaaVaralle().getJonosija(),
                    j.kirjanpito.viimeinenHakijaryhmanUlkopuoleltaHyvaksyttyJokaVoidaanSiirtaaVaralle().getJonosija()
            );
            return c == 0 ? Integer.compare(jj.prioriteetti, j.prioriteetti) : c;
        }
    }
}
