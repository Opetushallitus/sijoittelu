package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.hash.HashCode;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.comparator.HakemusWrapperComparator;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor.PostSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.*;
import fi.vm.sade.sijoittelu.domain.*;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilaTaulukot.*;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilojenMuokkaus.*;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.WrapperHelperMethods.*;

/**
 *
 * @author Kari Kammonen
 * @author Jussi Jartamo
 *
 */
public class SijoitteluAlgorithmImpl implements SijoitteluAlgorithm {
    private static final Logger LOG = LoggerFactory.getLogger(SijoitteluAlgorithmImpl.class);
    private final Set<HashCode> hashset = Sets.newHashSet();
    protected SijoitteluAlgorithmImpl() { 	}
    protected SijoitteluajoWrapper sijoitteluAjo;
    protected List<PreSijoitteluProcessor> preSijoitteluProcessors;
    protected List<PostSijoitteluProcessor> postSijoitteluProcessors;
    protected int depth = 0;

    @Override
    public SijoitteluajoWrapper getSijoitteluAjo() {
        return sijoitteluAjo;
    }

    @Override
    public void start() {
        runPreProcessors();
        sijoittele();
        runPostProcessors();
    }

    private void runPostProcessors() {
        for (PostSijoitteluProcessor processor : postSijoitteluProcessors) {
            processor.process(sijoitteluAjo);
        }
    }

    private void runPreProcessors() {
        for (PreSijoitteluProcessor processor : preSijoitteluProcessors) {
            processor.process(sijoitteluAjo);
        }
    }

    private void sijoittele() {
        Set<HakukohdeWrapper> muuttuneetHakukohteet = Sets.newHashSet(sijoitteluAjo.getHakukohteet());
        do {
            Set<HakukohdeWrapper> iteraationHakukohteet = muuttuneetHakukohteet;
            muuttuneetHakukohteet = Sets.newHashSet();
            for (HakukohdeWrapper hakukohde : iteraationHakukohteet) {
                muuttuneetHakukohteet.addAll(sijoitteleHakukohde(hakukohde));
            }
            HashCode hash = sijoitteluAjo.asHash();
            if(hashset.contains(hash)) {
                LOG.error("Sijoittelu on iteraatiolla {} uudelleen aikaisemmassa tilassa (tila {})", depth, hash);
            } else {
                LOG.debug("Iteraatio {} HASH {}", depth, hash);
            }
            ++depth;
        } while(!muuttuneetHakukohteet.isEmpty());
        --depth;
    }

    private Set<HakukohdeWrapper> sijoitteleHakukohde(HakukohdeWrapper hakukohde) {
        Set<HakukohdeWrapper> muuttuneetHakukohteet = Sets.newHashSet();

        for (HakijaryhmaWrapper hakijaryhmaWrapper : hakukohde.getHakijaryhmaWrappers()) {
            muuttuneetHakukohteet.addAll(this.sijoitteleHakijaryhma(hakijaryhmaWrapper));
        }

        for (ValintatapajonoWrapper valintatapajono : hakukohde.getValintatapajonot()) {
            muuttuneetHakukohteet.addAll(this.sijoitteleValintatapajono(valintatapajono));
        }

        // Otetaan pois kunnes tiedetään pitääkö hakijaryhmät huomioida
//        // Tayttöjonot
//        ArrayList<HakemusWrapper> muuttuneetHakemukset = new ArrayList<>();
//        hakukohde.getValintatapajonot().forEach(valintatapajono -> {
//            int aloituspaikat = valintatapajono.getValintatapajono().getAloituspaikat();
//            int hyvaksytyt = valintatapajono.getHakemukset().stream().filter(h->kuuluuHyvaksyttyihinTiloihin(h.getHakemus().getTila())).collect(Collectors.toList()).size();
//            int tilaa = aloituspaikat - hyvaksytyt;
//            String tayttojono = valintatapajono.getValintatapajono().getTayttojono();
//
//            LocalDateTime varasijaTayttoPaattyy = varasijaTayttoPaattyy(valintatapajono);
//
//            if(sijoitteluAjo.varasijaSaannotVoimassa()
//                    && sijoitteluAjo.getToday().isBefore(varasijaTayttoPaattyy)
//                    && tilaa > 0 && tayttojono != null && !tayttojono.isEmpty()
//                    && !tayttojono.equals(valintatapajono.getValintatapajono().getOid())
//                    && !onkoVarasijaisia(valintatapajono)) {
//                // Vielä on tilaa ja pitäis jostain täytellä
//
//                Optional<ValintatapajonoWrapper> opt = hakukohde.getValintatapajonot()
//                        .stream().filter(v -> v.getValintatapajono().getOid().equals(tayttojono)).findFirst();
//
//                if(opt.isPresent()) {
//                    // Jono löytyi hakukohteen jonoista
//                    ValintatapajonoWrapper kasiteltava = opt.get();
//                    List<HakemusWrapper> varasijajono = muodostaVarasijaJono(kasiteltava.getHakemukset())
//                            .stream()
//                            .filter(h -> onHylattyJonossa(valintatapajono, h))
//                            .collect(Collectors.toList());
//
//                    varasijajono.sort(new HakemusWrapperComparator());
//
//                    while(tilaa > 0 && !varasijajono.isEmpty()) {
//                        // Vielä on tilaa ja hakemuksia, jotka ei oo tässä hakukohteessa hyväksyttyjä
//                        //HakemusWrapper hyvaksyttava = varasijajono.get(0);
//                        HakemusWrapper hyvaksyttava = valintatapajono.getHakemukset()
//                                .stream()
//                                .filter(h-> h.getHakemus().getHakemusOid().equals(varasijajono.get(0).getHakemus().getHakemusOid()))
//                                .findFirst()
//                                .get();
//                        hyvaksyttava.getHakemus().setTilanKuvaukset(TilanKuvaukset.hyvaksyttyTayttojonoSaannolla(kasiteltava.getValintatapajono().getNimi()));
//                        muuttuneetHakemukset.addAll(hyvaksyHakemus(hyvaksyttava));
//                        tilaa--;
//                        varasijajono.remove(hyvaksyttava);
//                    }
//
//                }
//            }
//        });
//        muuttuneetHakukohteet.addAll(uudelleenSijoiteltavatHakukohteet(muuttuneetHakemukset));

        // Poistetaan ajokierroksen lukot
        hakukohde.getValintatapajonot().forEach(v -> {
            v.setAlitayttoLukko(false);
            v.getHakemukset().forEach(h -> {
                h.setHyvaksyttyHakijaryhmasta(false);
                h.setHyvaksyttavissaHakijaryhmanJalkeen(true);
            });
        });

        return muuttuneetHakukohteet;
    }

    private List<HakemusWrapper> muodostaVarasijaJono(List<HakemusWrapper> hakemukset) {
        return hakemukset.stream()
                .filter(h -> !kuuluuHyvaksyttyihinTiloihin(hakemuksenTila(h))
                        && hakijaHaluaa(h)
                        && saannotSallii(h))
                .collect(Collectors.toList());
    }


    private boolean onHylattyJonossa(ValintatapajonoWrapper valintatapajonoWrapper, HakemusWrapper hakija) {
        return valintatapajonoWrapper.getHakemukset()
                .stream()
                .anyMatch(h -> hakemuksenHakemusOid(h).equals(hakemuksenHakemusOid(hakija)) && hakemuksenTila(h).equals(HakemuksenTila.HYLATTY));
    }

    private boolean onkoVarasijaisia(ValintatapajonoWrapper valintatapajonoWrapper) {
        return valintatapajonoWrapper.getHakemukset().parallelStream().anyMatch(h-> hakemuksenTila(h).equals(HakemuksenTila.VARALLA));
    }

    private LocalDateTime varasijaTayttoPaattyy(ValintatapajonoWrapper valintatapajono) {
        Date varasijojaTaytetaanAsti = valintatapajono.getValintatapajono().getVarasijojaTaytetaanAsti();
        LocalDateTime varasijaTayttoPaattyy = sijoitteluAjo.getHakuKierrosPaattyy();

        if(varasijojaTaytetaanAsti != null) {
            varasijaTayttoPaattyy = LocalDateTime.ofInstant(varasijojaTaytetaanAsti.toInstant(), ZoneId.systemDefault());
        }

        return varasijaTayttoPaattyy;
    }

    private List<HakemusWrapper> muuttuneetHyvaksytyt(List<HakemusWrapper> hakemukset) {
        return hakemukset.stream().filter(h -> !kuuluuHyvaksyttyihinTiloihin(hakemuksenTila(h))).collect(Collectors.toList());
    }

    // TODO: tämä ei toimi oikein, jos päivämäärät vaihtelee. Muutetaan postprocessoriksi
    private void muutaEhdollisetVastaanototSitoviksi(ValintatapajonoWrapper valintatapajono) {
        valintatapajono.getHakemukset()
                .stream()
                .flatMap(h -> h.getHenkilo().getValintatulos().stream())
                .filter(v -> v.getValintatapajonoOid().equals(valintatapajono.getValintatapajono().getOid())
                        && v.getTila() == ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT)
                .forEach(v -> {
                    v.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI);
                    sijoitteluAjo.getMuuttuneetValintatulokset().add(v);
                });
    }

    private void hakukierrosPaattynyt(List<HakemusWrapper> hakemukset) {
        hakemukset.forEach(hk -> {
            if(hk.isTilaVoidaanVaihtaa()) {
                asetaTilaksiPeruuntunutHakukierrosPaattynyt(hk);
                hk.setTilaVoidaanVaihtaa(false);
            }
        });
    }

    private Set<HakukohdeWrapper> sijoitteleValintatapajono(ValintatapajonoWrapper valintatapajono) {

        LocalDateTime varasijaTayttoPaattyy = varasijaTayttoPaattyy(valintatapajono);

        // Muutetaan ehdolliset vastaanotot sitoviksi jos jonon varasijatäyttö on päättynyt
        if(sijoitteluAjo.getToday().isAfter(varasijaTayttoPaattyy) && sijoitteluAjo.isKKHaku()) {
            muutaEhdollisetVastaanototSitoviksi(valintatapajono);
        }

        Set<HakukohdeWrapper> muuttuneetHakukohteet = new HashSet<>();

        // Hakijaryhmäkäsittelyssä alitäyttösääntö käytetty
        if(valintatapajono.isAlitayttoLukko()) {
            return muuttuneetHakukohteet;
        }

        Tasasijasaanto saanto = jononTasasijasaanto(valintatapajono);

        List<HakemusWrapper> eiKorvattavissaOlevatHyvaksytytHakemukset = valintatapajononHyvaksytytHakemuksetJoitaEiVoiKorvata(valintatapajono);

        List<HakemusWrapper> valituksiHaluavatHakemukset =
                valintatapajono.getHakemukset().stream()
                        .filter(h -> !eiKorvattavissaOlevatHyvaksytytHakemukset.contains(h))
                        .filter(h -> !kuuluuHyvaksyttyihinTiloihin(hakemuksenTila(h)))
                        .filter(h ->
                                hakijaHaluaa(h)
                                        && saannotSallii(h))

                        .collect(Collectors.toList());

        // Ei ketään valituksi haluavaa
        if(valituksiHaluavatHakemukset.isEmpty()) {
            return muuttuneetHakukohteet;
        }

        // Hakukierros on päättynyt tai käsiteltävän jonon varasijasäännöt eivät ole enää voimassa.
        // Asetetaan kaikki hakemukset joiden tila voidaan vaihtaa tilaan peruuntunut
        if(sijoitteluAjo.hakukierrosOnPaattynyt() || sijoitteluAjo.getToday().isAfter(varasijaTayttoPaattyy)) {
            hakukierrosPaattynyt(valituksiHaluavatHakemukset);
            return muuttuneetHakukohteet;
        }

        // Jonolle on merkitty, että kaikki ehdon täyttävät hyväksytään
        if (valintatapajono.getValintatapajono().getKaikkiEhdonTayttavatHyvaksytaan() != null
                && valintatapajono.getValintatapajono().getKaikkiEhdonTayttavatHyvaksytaan()) {
            valituksiHaluavatHakemukset.forEach(this::hyvaksyHakemus);
            muuttuneetHakukohteet.addAll(uudelleenSijoiteltavatHakukohteet(muuttuneetHyvaksytyt(valituksiHaluavatHakemukset)));
            return muuttuneetHakukohteet;
        }

        int aloituspaikat = jononAloituspaikat(valintatapajono);
        int tilaa = aloituspaikat - eiKorvattavissaOlevatHyvaksytytHakemukset.size();

        if(tilaa <= 0) {
            return muuttuneetHakukohteet;
        }

        List<HakemusWrapper> kaikkiTasasijaHakemukset;
        HakemusWrapper paras = valituksiHaluavatHakemukset.get(0);
        if(saanto.equals(Tasasijasaanto.ARVONTA)) {
            kaikkiTasasijaHakemukset = Arrays.asList(paras);
        } else {
            kaikkiTasasijaHakemukset = valituksiHaluavatHakemukset
                    .stream()
                    .filter(h -> h.getHakemus().getJonosija().equals(paras.getHakemus().getJonosija()))
                    .collect(Collectors.toList());
        }

        List<HakemusWrapper> muuttuneet = new ArrayList<>();

        if(tilaa - kaikkiTasasijaHakemukset.size() >= 0) {
            muuttuneetHyvaksytyt(kaikkiTasasijaHakemukset).forEach(h -> {
                muuttuneet.addAll(hyvaksyHakemus(h));
            });
            muuttuneetHakukohteet.addAll(uudelleenSijoiteltavatHakukohteet(muuttuneet));
            muuttuneetHakukohteet.addAll(sijoitteleValintatapajono(valintatapajono));
        }
        // Tasasijavertailu
        else {
            if(saanto.equals(Tasasijasaanto.YLITAYTTO)) {
                muuttuneetHyvaksytyt(kaikkiTasasijaHakemukset).forEach(h -> {
                    muuttuneet.addAll(hyvaksyHakemus(h));
                });
            }
            muuttuneetHakukohteet.addAll(uudelleenSijoiteltavatHakukohteet(muuttuneet));
        }

        return muuttuneetHakukohteet;
    }

    private List<ValintatapajonoWrapper> hakijaryhmaanLiittyvatJonot(HakijaryhmaWrapper hakijaryhmaWrapper) {
        String jonoId = hakijaryhmaWrapper.getHakijaryhma().getValintatapajonoOid();
        if(jonoId != null) {
            return hakijaryhmaWrapper.getHakukohdeWrapper().getValintatapajonot()
                    .stream()
                    .filter(j -> j.getValintatapajono().getOid().equals(jonoId))
                    .collect(Collectors.toList());
        } else {
            return hakijaryhmaWrapper.getHakukohdeWrapper().getValintatapajonot();
        }
    }

    private boolean mahtuukoJonoon(List<HakemusWrapper> hakemukset, ValintatapajonoWrapper valintatapajonoWrapper) {
        int aloituspaikat = jononAloituspaikat(valintatapajonoWrapper);
        int hyvaksytyt = valintatapajonoWrapper.getHakemukset().stream().filter(h-> kuuluuHyvaksyttyihinTiloihin(hakemuksenTila(h))).collect(Collectors.toList()).size();
        if(aloituspaikat - hyvaksytyt <= 0) {
            return false;
        } else {
            if(jononTasasijasaanto(valintatapajonoWrapper).equals(Tasasijasaanto.YLITAYTTO)) {
                return true;
            } else if(aloituspaikat - hyvaksytyt - hakemukset.size() >= 0) {
                return true;
            }
        }
        return false;
    }

    private List<HakemusWrapper> samallaSijalla(HakemusWrapper hakemus, List<HakemusWrapper> hakemukset, Tasasijasaanto saanto) {
        if(saanto.equals(Tasasijasaanto.ARVONTA)) {
            return Arrays.asList(hakemus);
        } else {
            return hakemukset
                    .stream()
                    .filter(h -> h.getHakemus().getJonosija().equals(hakemus.getHakemus().getJonosija()))
                    .collect(Collectors.toList());
        }

    }

    private Pair<List<HakemusWrapper>, List<HakemusWrapper>> seuraavaksiParhaatHakijaryhmasta(List<HakemusWrapper> valituksiHaluavat, HakijaryhmaWrapper hakijaryhmaWrapper) {
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

        if(valittavat.isEmpty()) {
            // jossain on ylitäyttö ja pitäis saada hyväksytyks tai alitäyttö lukko
            List<ValintatapajonoWrapper> ylitayttoJonot = jonoittain.keySet()
                    .stream()
                    .filter(v -> jononTasasijasaanto(v).equals(Tasasijasaanto.YLITAYTTO))
                    .collect(Collectors.toList());

            if(!ylitayttoJonot.isEmpty()) {
                ylitayttoJonot.forEach(j -> {
                    List<HakemusWrapper> korvattavat = haeHuonoimmatValitutJotkaVoidaanKorvata(j, hakijaryhmaWrapper);
                    if(!korvattavat.isEmpty()) {
                        varalleAsetetut.addAll(korvattavat);
                    }
                });
            } else {
                jonoittain.keySet().forEach(j -> {
                    if(jononTasasijasaanto(j).equals(Tasasijasaanto.ALITAYTTO)) {
                        j.setAlitayttoLukko(true);
                    }
                });
            }
        }

        return Pair.of(valittavat,varalleAsetetut);

    }

    private List<HakemusWrapper> haeHuonoimmatValitutJotkaVoidaanKorvata(ValintatapajonoWrapper valintatapajonoWrapper, HakijaryhmaWrapper hakijaryhmaWrapper) {
        HakemusWrapperComparator comparator = new HakemusWrapperComparator();
        List<HakemusWrapper> korvattavat = haeHyvaksytytEiHakijaryhmaanKuuluvat(valintatapajonoWrapper, hakijaryhmaWrapper);
        Optional<HakemusWrapper> huonoinHakemus = korvattavat
                .stream()
                .sorted((h1, h2) -> comparator.compare(h2, h1))
                .findFirst();
        if(huonoinHakemus.isPresent()) {
            HakemusWrapper huonoin = huonoinHakemus.get();
            return korvattavat
                    .stream()
                    .filter(h -> h.getHakemus().getJonosija().equals(huonoin.getHakemus().getJonosija()))
                    .collect(Collectors.toList());

        } else {
            return Lists.newArrayList();
        }

    }

    private List<HakemusWrapper> haeHyvaksytytEiHakijaryhmaanKuuluvat(ValintatapajonoWrapper valintatapajonoWrapper, HakijaryhmaWrapper hakijaryhmaWrapper) {
        return valintatapajonoWrapper.getHakemukset()
                .stream()
                .filter(h -> kuuluuHyvaksyttyihinTiloihin(hakemuksenTila(h)) && voidaanKorvata(h))
                .filter(h -> !hakijaryhmaWrapper.getHakijaryhma().getHakemusOid().contains(hakemuksenHakemusOid(h)))
                .collect(Collectors.toList());
    }


    private List<HakemusWrapper> etsiValittavat(Map<ValintatapajonoWrapper, List<HakemusWrapper>> jonoittain, Map<Integer, List<HakemusWrapper>> jonojenParhaat) {
        for(Integer i : jonojenParhaat.keySet()) {
            List<HakemusWrapper> parhaat = jonojenParhaat.get(i);
            if(parhaat.size() == 1) {
                // Vain yhdestä jonosta löytyi hakemus tältä sijalta
                HakemusWrapper paras = parhaat.get(0);
                List<HakemusWrapper> parhaatJonoonMahtuvat = haeParhaatJonoonMahtuvat(jonoittain, paras);
                if(!parhaatJonoonMahtuvat.isEmpty()) {
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
                    if(!parhaatJonoonMahtuvat.isEmpty()) {
                        return parhaatJonoonMahtuvat;
                    }
                }
            }
        }
        return Lists.newArrayList();
    }

    private List<HakemusWrapper> haeParhaatJonoonMahtuvat(Map<ValintatapajonoWrapper, List<HakemusWrapper>> jonoittain, HakemusWrapper paras) {
        List<HakemusWrapper> samallaSijalla =
                samallaSijalla(paras, jonoittain.get(paras.getValintatapajono()), jononTasasijasaanto(paras.getValintatapajono()));
        if(mahtuukoJonoon(samallaSijalla, paras.getValintatapajono())) {
            return samallaSijalla;
        } else {
            return Lists.newArrayList();
        }
    }

    private Set<HakukohdeWrapper> sijoitteleHakijaryhma(HakijaryhmaWrapper hakijaryhmaWrapper) {

        Set<HakukohdeWrapper> muuttuneet = new HashSet<>();

        // Tähän hakijaryhmään liittyvät valintatapajonot
        List<ValintatapajonoWrapper> liittyvatJonot = hakijaryhmaanLiittyvatJonot(hakijaryhmaWrapper);

        // Näiden jonojen HakemusWrapperit, jotka kuuluvat tähän hakijaryhmään
        List<HakemusWrapper> ryhmaanKuuluvat = hakijaRyhmaanKuuluvat(
                liittyvatJonot
                        .stream()
                        .flatMap(j -> j.getHakemukset().stream())
                        .collect(Collectors.toList()),
                hakijaryhmaWrapper);

        // Hakijaryhmään kuuluvien hyväksyttyjen määrä
        int hyvaksyttyjenMaara = ryhmaanKuuluvat
                .stream()
                .filter(h -> kuuluuHyvaksyttyihinTiloihin(hakemuksenTila(h)))
                .collect(Collectors.toList()).size();

        int kiintio = hakijaryhmaWrapper.getHakijaryhma().getKiintio();
        int alotuspaikat = liittyvatJonot
                .stream()
                .map(v -> jononAloituspaikat(v))
                .reduce(0, (a, b) -> a + b);

        // Jos hakijaryhmän kiintiö on suurempi kuin mahdolliset aloituspaikat niin asetetaan kiintiöksi aloituspaikka määrä
        if(kiintio > alotuspaikat) {
            kiintio = alotuspaikat;
        }

        boolean tarkkaKiintio = hakijaryhmaWrapper.getHakijaryhma().isTarkkaKiintio();
        List<HakemusWrapper> muuttuneetHakemukset = new ArrayList<>();

        if(tarkkaKiintio && hyvaksyttyjenMaara >= kiintio) {
            asetaEiHyvaksyttavissaHakijaryhmanJalkeen(ryhmaanKuuluvat);

        } else if(hyvaksyttyjenMaara < kiintio) {
            // Hakijaryhmän valituksi haluavat
            List<HakemusWrapper> valituksiHaluavat = ryhmaanKuuluvat
                    .stream()
                    .filter(h -> hakemuksenTila(h).equals(HakemuksenTila.VARALLA))
                    .filter(h -> hakijaHaluaa(h) && saannotSallii(h))
                    .collect(Collectors.toList());

            if(!valituksiHaluavat.isEmpty()) {
                Pair<List<HakemusWrapper>, List<HakemusWrapper>> valittavat
                        = seuraavaksiParhaatHakijaryhmasta(valituksiHaluavat, hakijaryhmaWrapper);

                // Aloituspaikat täynnä ylitäytöllä, joten tiputetaan varalle
                valittavat.getRight().forEach(v -> {
                    muuttuneetHakemukset.addAll(asetaVaralleHakemus(v));
                });

                // Hyväksytään valittavat
                valittavat.getLeft().forEach(h -> {
                    h.setHyvaksyttyHakijaryhmasta(true);
                    muuttuneetHakemukset.addAll(hyvaksyHakemus(h));
                });

                boolean lukko = liittyvatJonot.stream().anyMatch(ValintatapajonoWrapper::isAlitayttoLukko);

                // Kiintiö ei täyty, koska alitäyttö
                if(!lukko && (!valittavat.getLeft().isEmpty() || !valittavat.getRight().isEmpty())) {
                    muuttuneet.addAll(sijoitteleHakijaryhma(hakijaryhmaWrapper));
                }

            }
        }

        muuttuneet.addAll(uudelleenSijoiteltavatHakukohteet(muuttuneetHakemukset));
        return muuttuneet;
    }

    private void asetaEiHyvaksyttavissaHakijaryhmanJalkeen(List<HakemusWrapper> ryhmaanKuuluvat) {
        ryhmaanKuuluvat
                .stream()
                .filter(h -> hakemuksenTila(h).equals(HakemuksenTila.VARALLA))
                .filter(h -> hakijaHaluaa(h) && saannotSallii(h))
                .forEach(h -> h.setHyvaksyttavissaHakijaryhmanJalkeen(false));
    }

    private List<HakemusWrapper> hakijaRyhmaanKuuluvat(List<HakemusWrapper> hakemus, HakijaryhmaWrapper ryhma) {
        return hakemus.stream()
                .filter(h -> ryhma.getHenkiloWrappers().contains(h.getHenkilo()))
                .collect(Collectors.toList());

    }

    private Set<HakukohdeWrapper> uudelleenSijoiteltavatHakukohteet(List<HakemusWrapper> muuttuneetHakemukset) {
        return muuttuneetHakemukset.stream()
                .map(h -> h.getValintatapajono().getHakukohdeWrapper())
                .collect(Collectors.toSet());
    }

    private boolean taytetaankoPoissaOlevat(ValintatapajonoWrapper valintatapajono) {
        return valintatapajono.getValintatapajono().getPoissaOlevaTaytto() != null && valintatapajono.getValintatapajono().getPoissaOlevaTaytto();
    }

    private List<HakemusWrapper> valintatapajononHyvaksytytHakemuksetJoitaEiVoiKorvata(ValintatapajonoWrapper valintatapajono) {
        List<Predicate<HakemusWrapper>> filters = new ArrayList<>();
        filters.add(h -> kuuluuHyvaksyttyihinTiloihin(hakemuksenTila(h)));

        if(taytetaankoPoissaOlevat(valintatapajono)) {
            filters.add(h -> !kuuluuPoissaoloTiloihin(h.getHakemus().getIlmoittautumisTila()));
        }

        return valintatapajono.getHakemukset()
                .stream()
                .filter(filters.stream().reduce(h -> true, Predicate::and))
                .collect(Collectors.toList());
    }

    private boolean voidaanKorvata(HakemusWrapper hakemusWrapper) {
        boolean voidaanKorvata = true;
        // Tilaa ei voi vaihtaa, ei voida enää korvata käynnissä olevan sijoitteluajon aikana
        if (!hakemusWrapper.isTilaVoidaanVaihtaa()) {
            voidaanKorvata = false;
        }
        // Hyväksytty hakijaryhmästä, ei voida enää korvata käynnissä olevan ajokierroksen aikana
        if(hakemusWrapper.isHyvaksyttyHakijaryhmasta()) {
            voidaanKorvata = false;
        }

        return voidaanKorvata;
    }

    private Set<HakemusWrapper> asetaVaralleHakemus(HakemusWrapper varalleAsetettavaHakemusWrapper) {
        Set<HakemusWrapper> uudelleenSijoiteltavatHakukohteet = new HashSet<>();
        if(varalleAsetettavaHakemusWrapper.isTilaVoidaanVaihtaa()) {
            if(kuuluuHyvaksyttyihinTiloihin(hakemuksenTila(varalleAsetettavaHakemusWrapper))) {
                for (HakemusWrapper hakemusWrapper : varalleAsetettavaHakemusWrapper.getHenkilo().getHakemukset()) {
                    if(hakemusWrapper.isTilaVoidaanVaihtaa()) {
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

    private Set<HakemusWrapper> hyvaksyHakemus(HakemusWrapper hakemus) {
        Set<HakemusWrapper> uudelleenSijoiteltavatHakukohteet = new HashSet<HakemusWrapper>();
        if(hakemus.isTilaVoidaanVaihtaa()) {
            if(kuuluuVaraTiloihin(hakemus.getHakemus().getEdellinenTila())) {
                asetaTilaksiVarasijaltaHyvaksytty(hakemus);
            } else {
                asetaTilaksiHyvaksytty(hakemus);
            }

            for (HakemusWrapper h : hakemus.getHenkilo().getHakemukset()) {
                // Alemmat toiveet
                if (h != hakemus && hakemuksenPrioriteetti(hakemus) < hakemuksenPrioriteetti(h)) {
                    if(h.isTilaVoidaanVaihtaa()) {
                        if (kuuluuHyvaksyttyihinTiloihin(hakemuksenTila(h))) {
                            asetaTilaksiPeruuntunutYlempiToive(h);
                            uudelleenSijoiteltavatHakukohteet.add(h);
                        } else {
                            if (!kuuluuHylattyihinTiloihin(hakemuksenTila(h))) {
                                asetaTilaksiPeruuntunutYlempiToive(h);
                            }
                        }
                    }
                }
                // Saman toiveen muut jonot
                if (h != hakemus && hakemuksenPrioriteetti(hakemus).equals(hakemuksenPrioriteetti(h))) {
                    Valintatapajono current = h.getValintatapajono().getValintatapajono();
                    Valintatapajono hyvaksyttyJono = hakemus.getValintatapajono().getValintatapajono();
                    // Peruutetaan vain korkeamman prioriteetin jonot
                    if(hyvaksyttyJono.getPrioriteetti() < current.getPrioriteetti()) {
                        // Perustapaus
                        if(h.isTilaVoidaanVaihtaa()) {
                            if (!kuuluuHylattyihinTiloihin(hakemuksenTila(h))) {
                                HakemuksenTila vanhaTila = hakemuksenTila(h);
                                asetaTilaksiPeruuntunutToinenJono(h);
                                if(kuuluuHyvaksyttyihinTiloihin(vanhaTila)) {
                                    uudelleenSijoiteltavatHakukohteet.add(h);
                                }
                            }
                        } else {
                            // Hakemukselle merkattu, että tilaa ei voi vaihtaa, mutta vaihdetaan kuitenkin jos hyväksytty
                            HakemuksenTila vanhaTila = hakemuksenTila(h);
                            if(kuuluuHyvaksyttyihinTiloihin(vanhaTila)) {
                                asetaTilaksiPeruuntunutToinenJono(h);

                                Optional<Valintatulos> jononTulos = h.getHenkilo().getValintatulos().stream().filter(v -> v.getValintatapajonoOid().equals(current.getOid())).findFirst();
                                if(jononTulos.isPresent() && !jononTulos.get().getTila().equals(ValintatuloksenTila.KESKEN)) {

                                    Valintatulos muokattava = jononTulos.get();

                                    Valintatulos nykyinen = muokkaaValintatulos(hakemus, h, hyvaksyttyJono, muokattava);

                                    // Lisää muokatut valintatulokset listaan tallennusta varten
                                    sijoitteluAjo.getMuuttuneetValintatulokset()
                                            .addAll(Arrays.asList(muokattava, nykyinen));

                                }
                                if(vanhaTila == HakemuksenTila.HYVAKSYTTY) {
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

    private boolean saannotSallii(HakemusWrapper hakemusWrapper) {
        boolean hakemuksenTila = !kuuluuHylattyihinTiloihin(hakemuksenTila(hakemusWrapper));
        boolean hakijaAloistuspaikkojenSisallaTaiVarasijataytto = true;
        boolean eiVarasijaTayttoa = false;

        // Jos varasijasäännöt ovat astuneet voimaan niin katsotaan saako varasijoilta täyttää
        if(sijoitteluAjo.varasijaSaannotVoimassa()) {
            if(jononEiVarasijatayttoa(hakemusWrapper) != null) {
                eiVarasijaTayttoa = jononEiVarasijatayttoa(hakemusWrapper);
            }
        }

        if(eiVarasijaTayttoa && !jononKaikkiEhdonTayttavatHyvaksytaan(hakemusWrapper)) {
            hakijaAloistuspaikkojenSisallaTaiVarasijataytto  = hakijaAloistuspaikkojenSisalla(hakemusWrapper);
            if(!hakijaAloistuspaikkojenSisallaTaiVarasijataytto && sijoitteluAjo.isKKHaku() && hakemusWrapper.isTilaVoidaanVaihtaa()) {
                asetaTilaksiPeruuntunutAloituspaikatTaynna(hakemusWrapper);
            }
        }

        Integer varasijat = jononVarasijat(hakemusWrapper);
        boolean huomioitavienVarasijojenSisalla = true;

        if(sijoitteluAjo.varasijaSaannotVoimassa()
                && varasijat != null
                && varasijat > 0) {
            huomioitavienVarasijojenSisalla = hakijaKasiteltavienVarasijojenSisalla(hakemusWrapper, varasijat);
            if(!huomioitavienVarasijojenSisalla && hakemusWrapper.isTilaVoidaanVaihtaa()) {
                asetaTilaksiPeruuntunutEiMahduKasiteltaviinSijoihin(hakemusWrapper);
            }

        }

        boolean eiPeruttuaKorkeampaaTaiSamaaHakutoivetta =  eiPeruttuaKorkeampaaTaiSamaaHakutoivetta(hakemusWrapper);

        return hakemuksenTila
                && hakijaAloistuspaikkojenSisallaTaiVarasijataytto
                && eiPeruttuaKorkeampaaTaiSamaaHakutoivetta
                && huomioitavienVarasijojenSisalla
                && hakemusWrapper.isHyvaksyttavissaHakijaryhmanJalkeen();
    }

    protected boolean eiPeruttuaKorkeampaaTaiSamaaHakutoivetta(HakemusWrapper hakemusWrapper) {
        return hakemusWrapper.getHenkilo().getHakemukset()
                .stream()
                .filter(h -> h != hakemusWrapper)
                .noneMatch(h -> hakemuksenTila(h) == HakemuksenTila.PERUNUT
                        && hakemuksenPrioriteetti(h) <= hakemuksenPrioriteetti(hakemusWrapper));
    }

    private boolean hakijaAloistuspaikkojenSisalla(HakemusWrapper hakemusWrapper) {
        ValintatapajonoWrapper valintatapajono = hakemusWrapper.getValintatapajono() ;
        int aloituspaikat = jononAloituspaikat(valintatapajono);

        return onkoPaikkojenSisalla(hakemusWrapper, aloituspaikat, valintatapajono.getHakemukset());
    }

    private boolean hakijaKasiteltavienVarasijojenSisalla(HakemusWrapper hakemusWrapper, Integer varasijat) {
        ValintatapajonoWrapper valintatapajono = hakemusWrapper.getValintatapajono() ;
        int aloituspaikat = jononAloituspaikat(valintatapajono) + varasijat;

        return onkoPaikkojenSisalla(hakemusWrapper, aloituspaikat, valintatapajono.getHakemukset());
    }

    private boolean onkoPaikkojenSisalla(HakemusWrapper hakemusWrapper, int aloituspaikat, List<HakemusWrapper> hakemukset) {
        int i = 0;

        for(HakemusWrapper h : hakemukset )  {

            if(hakemuksenTila(h) != HakemuksenTila.HYLATTY) {
                i++;
            }
            if(h == hakemusWrapper && i <= aloituspaikat) { //vertaa instanssia
                return true;
            }  else if(i>aloituspaikat)   {
                return false;
            }
        }
        return true;
    }

    private boolean hakijaHaluaa(HakemusWrapper hakemusWrapper) {
        HenkiloWrapper henkilo = hakemusWrapper.getHenkilo();

        // Tila on PERUUNUTUNUT eikä sitä voi vaihtaa
        if(!hakemusWrapper.isTilaVoidaanVaihtaa() && hakemuksenTila(hakemusWrapper) == HakemuksenTila.PERUUNTUNUT) {
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
