package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.hash.HashCode;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.comparator.HakemusWrapperComparator;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor.PostSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor.PostSijoitteluProcessorMuutostiedonAsetus;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor.PostSijoitteluProcessorPeruuntuneetHakemuksenVastaanotonMuokkaus;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessorHylkaaHakijaRyhmaanKuulumattomat;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessorLahtotilanteenHash;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessorPeruutaAlemmatPeruneetJaHyvaksytyt;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessorSort;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessorTasasijaArvonta;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.*;
import fi.vm.sade.sijoittelu.domain.*;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilaTaulukot.*;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilojenMuokkaus.*;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.WrapperHelperMethods.*;

public abstract class SijoitteluAlgorithm {
    private static final Logger LOG = LoggerFactory.getLogger(SijoitteluAlgorithm.class);

    private final static List<PreSijoitteluProcessor> preSijoitteluProcessors = Arrays.asList(
        new PreSijoitteluProcessorTasasijaArvonta(),
        new PreSijoitteluProcessorSort(),
        new PreSijoitteluProcessorPeruutaAlemmatPeruneetJaHyvaksytyt(),
        new PreSijoitteluProcessorHylkaaHakijaRyhmaanKuulumattomat(),
        new PreSijoitteluProcessorLahtotilanteenHash()
    );
    private final static List<PostSijoitteluProcessor> postSijoitteluProcessors = Arrays.asList(
        //new PostSijoitteluProcessorEhdollisenVastaanotonSiirtyminenYlemmalleHakutoiveelle(),
        new PostSijoitteluProcessorPeruuntuneetHakemuksenVastaanotonMuokkaus(),
        new PostSijoitteluProcessorMuutostiedonAsetus()
    );

    public static SijoittelunTila sijoittele(SijoitteluAjo sijoitteluAjo, List<Hakukohde> hakukohteet, List<Valintatulos> valintatulokset) {
        return sijoittele(SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(sijoitteluAjo, hakukohteet, valintatulokset));
    }

    public static SijoittelunTila sijoittele(SijoitteluajoWrapper sijoitteluAjo) {
        LOG.info("(hakuOid={}) Starting sijoitteluajo {}",
                Optional.ofNullable(sijoitteluAjo.getSijoitteluajo()).orElse(new SijoitteluAjo()).getHakuOid(), sijoitteluAjo.getSijoitteluAjoId());
        runPreProcessors(sijoitteluAjo);
        final SijoittelunTila tila = suoritaSijoittelu(sijoitteluAjo);
        runPostProcessors(sijoitteluAjo);
        return tila;
    }

    private static void runPreProcessors(final SijoitteluajoWrapper sijoitteluAjo) {
        for (PreSijoitteluProcessor processor : preSijoitteluProcessors) {
            LOG.info("(hakuOid={}) Starting preprocessor {} for sijoitteluAjo {}",
                    Optional.ofNullable(sijoitteluAjo.getSijoitteluajo()).orElse(new SijoitteluAjo()).getHakuOid(), processor.name(), sijoitteluAjo.getSijoitteluAjoId());
            processor.process(sijoitteluAjo);
        }
    }

    private static SijoittelunTila suoritaSijoittelu(final SijoitteluajoWrapper sijoitteluAjo) {
        SijoittelunTila tila = new SijoittelunTila(sijoitteluAjo);
        SijoitteleKunnesValmisTaiSilmukkaHavaittu sijoittelunIterointi = new SijoitteleKunnesValmisTaiSilmukkaHavaittu();
        try {
            return sijoittelunIterointi.sijoittele(sijoitteluAjo, tila);
        } catch (SijoitteluSilmukkaException s) {
            return SijoitteleKunnesTavoiteHashTuleeVastaanTaiHeitaPoikkeus.sijoittele(
                sijoitteluAjo, tila, sijoittelunIterointi.edellinenHash.get(), sijoittelunIterointi.iteraationHakukohteet
            );
        }
    }

    private static void runPostProcessors(final SijoitteluajoWrapper sijoitteluAjo) {
        for (PostSijoitteluProcessor processor : postSijoitteluProcessors) {
            LOG.info("(hakuOid={}) Starting postprocessor {} for sijoitteluAjo {}",
                    Optional.ofNullable(sijoitteluAjo.getSijoitteluajo()).orElse(new SijoitteluAjo()).getHakuOid(), processor.name(), sijoitteluAjo.getSijoitteluAjoId());
            processor.process(sijoitteluAjo);
        }
    }

    private static class SijoitteleKunnesTavoiteHashTuleeVastaanTaiHeitaPoikkeus {
        public static SijoittelunTila sijoittele(SijoitteluajoWrapper sijoitteluAjo, SijoittelunTila tila, HashCode tavoiteHash, Set<HakukohdeWrapper> muuttuneetHakukohteet) {
            Set<HashCode> hashset = Sets.newHashSet();

            HashCode hash = sijoitteluAjo.asHash();
            if (hash.equals(tavoiteHash)) {
                LOG.error("###\r\n### Sijoittelu on silmukassa missä yhden iteraation jälkeen päädytään samaan tilaan samoilla muuttuneilla hakukohteilla.\r\n###");
                //return;
            }
            int i = 0;
            do {
                Set<HakukohdeWrapper> iteraationHakukohteet = muuttuneetHakukohteet;
                muuttuneetHakukohteet = Sets.newHashSet();
                for (HakukohdeWrapper hakukohde : iteraationHakukohteet) {
                    muuttuneetHakukohteet.addAll(sijoitteleHakukohde(sijoitteluAjo, hakukohde));
                }
                hash = sijoitteluAjo.asHash();
                ++i;
                if (hash.equals(tavoiteHash)) {
                    LOG.error("###\r\n### Sijoittelu päätettiin silmukan viimeiseen tilaan. Silmukan koko oli {} iteraatiota.\r\n###", i);
                    //return;
                }
                if (hashset.contains(hash)) {
                    LOG.error("Sijoittelu on iteraatiolla {} uudelleen aikaisemmassa tilassa (tila {}). Tämä tarkoittaa että sijoittelualgoritmi ei tuota aina samannäköisiä silmukoita.", tila.depth, hash);
                    //throw new SijoitteluFailedException("Sijoittelu on iteraatiolla "+depth+" uudelleen aikaisemmassa tilassa (tila " + hash + ")");
                } else {
                    LOG.debug("Iteraatio {} HASH {}", tila.depth, hash);
                    hashset.add(hash);
                }
                ++tila.depth;
            } while (!muuttuneetHakukohteet.isEmpty());
            --tila.depth;
            LOG.error("Sijoittelu meni läpi silmukasta huolimatta. Onko algoritmissa silmukan havaitsemislogiikkaa?");
            return tila;
        }
    }

    private static class SijoitteleKunnesValmisTaiSilmukkaHavaittu {
        private Optional<HashCode> edellinenHash = Optional.empty();
        private Set<HakukohdeWrapper> iteraationHakukohteet;
        public SijoittelunTila sijoittele(SijoitteluajoWrapper sijoitteluAjo, SijoittelunTila tila) {
            final Set<HashCode> hashset = Sets.newHashSet();
            iteraationHakukohteet = Sets.newHashSet(sijoitteluAjo.getHakukohteet());
            boolean jatkuukoSijoittelu;
            do {
                Set<HakukohdeWrapper> muuttuneetHakukohteet = Sets.newHashSet();
                for (HakukohdeWrapper hakukohde : iteraationHakukohteet) {
                    muuttuneetHakukohteet.addAll(sijoitteleHakukohde(sijoitteluAjo, hakukohde));
                }
                HashCode iteraationHash = sijoitteluAjo.asHash();
                ++tila.depth;
                iteraationHakukohteet = muuttuneetHakukohteet;
                jatkuukoSijoittelu = !muuttuneetHakukohteet.isEmpty();
                edellinenHash = Optional.ofNullable(iteraationHash);
                LOG.debug("Iteraatio {} HASH {} ja muuttuneet hakukohteet {}", tila.depth, iteraationHash, muuttuneetHakukohteet.size());
                if (jatkuukoSijoittelu && hashset.contains(iteraationHash)) {
                    LOG.error("Sijoittelu on iteraatiolla {} uudelleen aikaisemmassa tilassa (tila {})", tila.depth, iteraationHash);
                    throw new SijoitteluSilmukkaException("Sijoittelu on iteraatiolla " + tila.depth + " uudelleen aikaisemmassa tilassa (tila " + iteraationHash + ")");
                }
                hashset.add(iteraationHash);
            } while (jatkuukoSijoittelu);
            --tila.depth;
            return tila;
        }

    }

    private static Set<HakukohdeWrapper> sijoitteleHakukohde(SijoitteluajoWrapper sijoitteluAjo, HakukohdeWrapper hakukohde) {
        Set<HakukohdeWrapper> muuttuneetHakukohteet = Sets.newHashSet();
        for (HakijaryhmaWrapper hakijaryhmaWrapper : hakukohde.getHakijaryhmaWrappers()) {
            muuttuneetHakukohteet.addAll(sijoitteleHakijaryhma(sijoitteluAjo, hakijaryhmaWrapper));
        }
        for (ValintatapajonoWrapper valintatapajono : hakukohde.getValintatapajonot()) {
            muuttuneetHakukohteet.addAll(sijoitteleValintatapajono(sijoitteluAjo, valintatapajono));
        }
        poistaAjokierroksenLukot(hakukohde);
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

    private static List<HakemusWrapper> muuttuneetHyvaksytyt(List<HakemusWrapper> hakemukset) {
        return hakemukset.stream().filter(h -> !kuuluuHyvaksyttyihinTiloihin(hakemuksenTila(h))).collect(Collectors.toList());
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
                            sijoitteluAjo.getMuuttuneetValintatulokset().add(v);
                        });
                    }
                });
    }

    private static void hakukierrosPaattynyt(List<HakemusWrapper> hakemukset) {
        hakemukset.forEach(hk -> {
            if (hk.isTilaVoidaanVaihtaa()) {
                asetaTilaksiPeruuntunutHakukierrosPaattynyt(hk);
                hk.setTilaVoidaanVaihtaa(false);
            }
        });
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
        List<HakemusWrapper> eiKorvattavissaOlevatHyvaksytytHakemukset = valintatapajononHyvaksytytHakemuksetJoitaEiVoiKorvata(valintatapajono);
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

    private static void hyvaksyKaikkiTasasijaHakemukset(SijoitteluajoWrapper sijoitteluAjo, List<HakemusWrapper> kaikkiTasasijaHakemukset, List<HakemusWrapper> muuttuneet) {
        muuttuneetHyvaksytyt(kaikkiTasasijaHakemukset).forEach(h -> {
            muuttuneet.addAll(hyvaksyHakemus(sijoitteluAjo, h));
        });
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

    private static boolean mahtuukoJonoon(List<HakemusWrapper> hakemukset, ValintatapajonoWrapper valintatapajonoWrapper) {
        int aloituspaikat = jononAloituspaikat(valintatapajonoWrapper);
        int hyvaksytyt = valintatapajonoWrapper.getHakemukset().stream().filter(h -> kuuluuHyvaksyttyihinTiloihin(hakemuksenTila(h))).collect(Collectors.toList()).size();
        if (aloituspaikat - hyvaksytyt <= 0) {
            return false;
        } else {
            if (jononTasasijasaanto(valintatapajonoWrapper).equals(Tasasijasaanto.YLITAYTTO)) {
                return true;
            } else if (aloituspaikat - hyvaksytyt - hakemukset.size() >= 0) {
                return true;
            }
        }
        return false;
    }

    private static List<HakemusWrapper> samallaSijalla(HakemusWrapper hakemus, List<HakemusWrapper> hakemukset, Tasasijasaanto saanto) {
        if (saanto.equals(Tasasijasaanto.ARVONTA)) {
            return Arrays.asList(hakemus);
        } else {
            return hakemukset
                .stream()
                .filter(h -> h.getHakemus().getJonosija().equals(hakemus.getHakemus().getJonosija()))
                .collect(Collectors.toList());
        }

    }

    private static Pair<List<HakemusWrapper>, List<HakemusWrapper>> seuraavaksiParhaatHakijaryhmasta(List<HakemusWrapper> valituksiHaluavat, HakijaryhmaWrapper hakijaryhmaWrapper) {
        HakemusWrapperComparator comparator = new HakemusWrapperComparator();
        // Valituksihaluavat jonoittain
        Map<ValintatapajonoWrapper, List<HakemusWrapper>> jonoittain = valituksiHaluavat
            .stream()
            .sorted(comparator::compare)
            .collect(Collectors.groupingBy(HakemusWrapper::getValintatapajono));
        // Jonojen parhaat jonosijoittain
        Map<Integer, List<HakemusWrapper>> jonojenParhaat = jonoittain.values()
            .stream()
            .map(l -> l.get(0))
            .collect(Collectors.groupingBy(h -> h.getHakemus().getJonosija()));
        List<HakemusWrapper> valittavat = etsiValittavat(jonoittain, jonojenParhaat);
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
            .filter(h -> kuuluuHyvaksyttyihinTiloihin(hakemuksenTila(h)) && voidaanKorvata(h))
            .filter(h -> !hakijaryhmaWrapper.getHakijaryhma().getHakemusOid().contains(hakemuksenHakemusOid(h)))
            .collect(Collectors.toList());
    }


    private static List<HakemusWrapper> etsiValittavat(Map<ValintatapajonoWrapper, List<HakemusWrapper>> jonoittain, Map<Integer, List<HakemusWrapper>> jonojenParhaat) {
        for (Integer i : jonojenParhaat.keySet()) {
            List<HakemusWrapper> parhaat = jonojenParhaat.get(i);
            if (parhaat.size() == 1) {
                // Vain yhdestä jonosta löytyi hakemus tältä sijalta
                HakemusWrapper paras = parhaat.get(0);
                List<HakemusWrapper> parhaatJonoonMahtuvat = haeParhaatJonoonMahtuvat(jonoittain, paras);
                if (!parhaatJonoonMahtuvat.isEmpty()) {
                    return parhaatJonoonMahtuvat;
                }
            } else {
                List<HakemusWrapper> jononMukaanSortattu = parhaat
                    .stream()
                    .sorted((h1, h2) ->
                        Integer.compare(jononPrioriteetti(h1),
                            jononPrioriteetti(h2)))
                    .collect(Collectors.toList());
                for (HakemusWrapper paras : jononMukaanSortattu) {
                    List<HakemusWrapper> parhaatJonoonMahtuvat = haeParhaatJonoonMahtuvat(jonoittain, paras);
                    if (!parhaatJonoonMahtuvat.isEmpty()) {
                        return parhaatJonoonMahtuvat;
                    }
                }
            }
        }
        return Lists.newArrayList();
    }

    private static List<HakemusWrapper> haeParhaatJonoonMahtuvat(Map<ValintatapajonoWrapper, List<HakemusWrapper>> jonoittain, HakemusWrapper paras) {
        List<HakemusWrapper> samallaSijalla =
            samallaSijalla(paras, jonoittain.get(paras.getValintatapajono()), jononTasasijasaanto(paras.getValintatapajono()));
        if (mahtuukoJonoon(samallaSijalla, paras.getValintatapajono())) {
            return samallaSijalla;
        } else {
            return Lists.newArrayList();
        }
    }

    private static Set<HakukohdeWrapper> sijoitteleHakijaryhma(SijoitteluajoWrapper sijoitteluAjo, HakijaryhmaWrapper hakijaryhmaWrapper) {
        final List<ValintatapajonoWrapper> liittyvatJonot = hakijaryhmaanLiittyvatJonot(hakijaryhmaWrapper);
        final List<HakemusWrapper> hakemusWrappers = liittyvatJonot.stream().flatMap(j -> j.getHakemukset().stream()).collect(Collectors.toList());
        final List<HakemusWrapper> ryhmaanKuuluvat = hakijaRyhmaanKuuluvat(hakemusWrappers, hakijaryhmaWrapper);
        final int hyvaksyttyjenMaara = ryhmaanKuuluvat.stream().filter(h -> kuuluuHyvaksyttyihinTiloihin(hakemuksenTila(h))).collect(Collectors.toList()).size();
        final boolean tarkkaKiintio = hakijaryhmaWrapper.getHakijaryhma().isTarkkaKiintio();
        final int aloituspaikat = liittyvatJonot.stream().map(v -> jononAloituspaikat(v)).reduce(0, (a, b) -> a + b);

        int kiintio = hakijaryhmaWrapper.getHakijaryhma().getKiintio();
        if (kiintio > aloituspaikat) {
            // hakijaryhmän kiintiö on suurempi kuin mahdolliset aloituspaikat, asetetaan kiintiöksi aloituspaikka määrä
            kiintio = aloituspaikat;
        }
        Set<HakukohdeWrapper> muuttuneet = new HashSet<>();
        List<HakemusWrapper> muuttuneetHakemukset = new ArrayList<>();

        if (tarkkaKiintio && hyvaksyttyjenMaara >= kiintio) {
            asetaEiHyvaksyttavissaHakijaryhmanJalkeen(sijoitteluAjo, ryhmaanKuuluvat);
        } else if (hyvaksyttyjenMaara < kiintio) {
            kasitteleValituksiHaluavat(sijoitteluAjo, hakijaryhmaWrapper, liittyvatJonot, ryhmaanKuuluvat, muuttuneet, muuttuneetHakemukset);
        }
        muuttuneet.addAll(uudelleenSijoiteltavatHakukohteet(muuttuneetHakemukset));
        return muuttuneet;
    }

    private static void kasitteleValituksiHaluavat(SijoitteluajoWrapper sijoitteluAjo, HakijaryhmaWrapper hakijaryhmaWrapper, List<ValintatapajonoWrapper> liittyvatJonot, List<HakemusWrapper> ryhmaanKuuluvat, Set<HakukohdeWrapper> muuttuneet, List<HakemusWrapper> muuttuneetHakemukset) {
        List<HakemusWrapper> valituksiHaluavat = ryhmaanKuuluvat
            .stream()
            .filter(h -> hakemuksenTila(h).equals(HakemuksenTila.VARALLA))
            .filter(h -> hakijaHaluaa(h) && saannotSallii(sijoitteluAjo, h))
            .collect(Collectors.toList());
        if (!valituksiHaluavat.isEmpty()) {
            Pair<List<HakemusWrapper>, List<HakemusWrapper>> valittavatJaVarasijat = seuraavaksiParhaatHakijaryhmasta(valituksiHaluavat, hakijaryhmaWrapper);
            // Aloituspaikat täynnä ylitäytöllä, joten tiputetaan varalle
            valittavatJaVarasijat.getRight().forEach(v -> {
                muuttuneetHakemukset.addAll(asetaVaralleHakemus(v));
            });
            // Hyväksytään valittavat
            valittavatJaVarasijat.getLeft().forEach(h -> {
                h.setHyvaksyttyHakijaryhmasta(true);
                muuttuneetHakemukset.addAll(hyvaksyHakemus(sijoitteluAjo, h));
            });
            boolean lukko = liittyvatJonot.stream().anyMatch(ValintatapajonoWrapper::isAlitayttoLukko);
            // Kiintiö ei täyty, koska alitäyttö
            if (!lukko && (!valittavatJaVarasijat.getLeft().isEmpty() || !valittavatJaVarasijat.getRight().isEmpty())) {
                muuttuneet.addAll(sijoitteleHakijaryhma(sijoitteluAjo, hakijaryhmaWrapper));
            }
        }
    }

    private static void asetaEiHyvaksyttavissaHakijaryhmanJalkeen(SijoitteluajoWrapper sijoitteluAjo, List<HakemusWrapper> ryhmaanKuuluvat) {
        ryhmaanKuuluvat
            .stream()
            .filter(h -> hakemuksenTila(h).equals(HakemuksenTila.VARALLA))
            .filter(h -> hakijaHaluaa(h) && saannotSallii(sijoitteluAjo, h))
            .forEach(h -> h.setHyvaksyttavissaHakijaryhmanJalkeen(false));
    }

    private static List<HakemusWrapper> hakijaRyhmaanKuuluvat(List<HakemusWrapper> hakemus, HakijaryhmaWrapper ryhma) {
        return hakemus.stream()
            .filter(h -> ryhma.getHenkiloWrappers().contains(h.getHenkilo()))
            .collect(Collectors.toList());
    }

    private static Set<HakukohdeWrapper> uudelleenSijoiteltavatHakukohteet(List<HakemusWrapper> muuttuneetHakemukset) {
        return muuttuneetHakemukset.stream()
            .map(h -> h.getValintatapajono().getHakukohdeWrapper())
            .collect(Collectors.toSet());
    }

    private static boolean taytetaankoPoissaOlevat(ValintatapajonoWrapper valintatapajono) {
        return valintatapajono.getValintatapajono().getPoissaOlevaTaytto() != null && valintatapajono.getValintatapajono().getPoissaOlevaTaytto();
    }

    private static List<HakemusWrapper> valintatapajononHyvaksytytHakemuksetJoitaEiVoiKorvata(ValintatapajonoWrapper valintatapajono) {
        List<Predicate<HakemusWrapper>> filters = new ArrayList<>();
        filters.add(h -> kuuluuHyvaksyttyihinTiloihin(hakemuksenTila(h)));
        if (taytetaankoPoissaOlevat(valintatapajono)) {
            filters.add(h -> !kuuluuPoissaoloTiloihin(h.getHakemus().getIlmoittautumisTila()));
        }
        return valintatapajono.getHakemukset()
            .stream()
            .filter(filters.stream().reduce(h -> true, Predicate::and))
            .collect(Collectors.toList());
    }

    private static boolean voidaanKorvata(HakemusWrapper hakemusWrapper) {
        boolean voidaanKorvata = true;
        // Tilaa ei voi vaihtaa, ei voida enää korvata käynnissä olevan sijoitteluajon aikana
        if (!hakemusWrapper.isTilaVoidaanVaihtaa()) {
            voidaanKorvata = false;
        }
        // Hyväksytty hakijaryhmästä, ei voida enää korvata käynnissä olevan ajokierroksen aikana
        if (hakemusWrapper.isHyvaksyttyHakijaryhmasta()) {
            voidaanKorvata = false;
        }
        return voidaanKorvata;
    }

    private static Set<HakemusWrapper> asetaVaralleHakemus(HakemusWrapper varalleAsetettavaHakemusWrapper) {
        Set<HakemusWrapper> uudelleenSijoiteltavatHakukohteet = new HashSet<>();
        if (varalleAsetettavaHakemusWrapper.isTilaVoidaanVaihtaa()) {
            if (kuuluuHyvaksyttyihinTiloihin(hakemuksenTila(varalleAsetettavaHakemusWrapper))) {
                for (HakemusWrapper hakemusWrapper : varalleAsetettavaHakemusWrapper.getHenkilo().getHakemukset()) {
                    if (hakemusWrapper.isTilaVoidaanVaihtaa()) {
                        if (!kuuluuHylattyihinTiloihin(hakemuksenTila(hakemusWrapper))) {
                            asetaTilaksiVaralla(hakemusWrapper);
                            uudelleenSijoiteltavatHakukohteet.add(hakemusWrapper);
                        }
                    }
                }
            } else {
                if (!kuuluuHyvaksyttyihinTiloihin(hakemuksenTila(varalleAsetettavaHakemusWrapper))) {
                    asetaTilaksiVaralla(varalleAsetettavaHakemusWrapper);
                }
            }
        }
        return uudelleenSijoiteltavatHakukohteet;
    }

    private static Set<HakemusWrapper> hyvaksyHakemus(SijoitteluajoWrapper sijoitteluAjo, HakemusWrapper hakemus) {
        Set<HakemusWrapper> uudelleenSijoiteltavatHakukohteet = new HashSet<HakemusWrapper>();
        if (hakemus.isTilaVoidaanVaihtaa()) {
            if (kuuluuVaraTiloihin(hakemus.getHakemus().getEdellinenTila())) {
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
                    // Peruutetaan vain korkeamman prioriteetin jonot
                    if (hyvaksyttyJono.getPrioriteetti() < current.getPrioriteetti()) {
                        // Perustapaus
                        if (h.isTilaVoidaanVaihtaa()) {
                            if (!kuuluuHylattyihinTiloihin(hakemuksenTila(h))) {
                                HakemuksenTila vanhaTila = hakemuksenTila(h);
                                asetaTilaksiPeruuntunutToinenJono(h);
                                if (kuuluuHyvaksyttyihinTiloihin(vanhaTila)) {
                                    uudelleenSijoiteltavatHakukohteet.add(h);
                                }
                            }
                        } else {
                            // Hakemukselle merkattu, että tilaa ei voi vaihtaa, mutta vaihdetaan kuitenkin jos hyväksytty
                            HakemuksenTila vanhaTila = hakemuksenTila(h);
                            if (kuuluuHyvaksyttyihinTiloihin(vanhaTila)) {
                                asetaTilaksiPeruuntunutToinenJono(h);
                                Optional<Valintatulos> jononTulos = h.getHenkilo().getValintatulos().stream().filter(v -> v.getValintatapajonoOid().equals(current.getOid())).findFirst();
                                if (jononTulos.isPresent() && !jononTulos.get().getTila().equals(ValintatuloksenTila.KESKEN)) {
                                    Valintatulos muokattava = jononTulos.get();
                                    Valintatulos nykyinen = siirraValintatulosHyvaksyttyynJonoon(hakemus, h, hyvaksyttyJono, muokattava);
                                    // Lisää muokatut valintatulokset listaan tallennusta varten
                                    sijoitteluAjo.getMuuttuneetValintatulokset().addAll(Arrays.asList(muokattava, nykyinen));
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

    private static boolean saannotSallii(SijoitteluajoWrapper sijoitteluAjo, HakemusWrapper hakemusWrapper) {
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

    protected static boolean eiPeruttuaKorkeampaaTaiSamaaHakutoivetta(HakemusWrapper hakemusWrapper) {
        return hakemusWrapper.getHenkilo().getHakemukset()
            .stream()
            .filter(h -> h != hakemusWrapper)
            .noneMatch(h -> hakemuksenTila(h) == HakemuksenTila.PERUNUT && hakemuksenPrioriteetti(h) <= hakemuksenPrioriteetti(hakemusWrapper));
    }

    private static boolean hakijaAloistuspaikkojenSisalla(HakemusWrapper hakemusWrapper) {
        ValintatapajonoWrapper valintatapajono = hakemusWrapper.getValintatapajono();
        int aloituspaikat = jononAloituspaikat(valintatapajono);
        return onkoPaikkojenSisalla(hakemusWrapper, aloituspaikat, valintatapajono.getHakemukset());
    }

    private static boolean hakijaKasiteltavienVarasijojenSisalla(HakemusWrapper hakemusWrapper, Integer varasijat) {
        ValintatapajonoWrapper valintatapajono = hakemusWrapper.getValintatapajono();
        int aloituspaikat = jononAloituspaikat(valintatapajono) + varasijat;
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

    private static boolean hakijaHaluaa(HakemusWrapper hakemusWrapper) {
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
}
