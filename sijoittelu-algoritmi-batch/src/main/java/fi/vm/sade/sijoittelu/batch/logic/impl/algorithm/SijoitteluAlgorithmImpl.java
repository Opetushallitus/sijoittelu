package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.hash.HashCode;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.comparator.HakemusWrapperComparator;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor.PostSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilanKuvaukset;
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
            muuttuneetHakukohteet.addAll(this.sijoitteleValintatapajon(valintatapajono));
        }


        // Otetaan pois kunnes tiedetään pitääkö hakijaryhmät huomioida
//        // Tayttöjonot
//        ArrayList<HakemusWrapper> muuttuneetHakemukset = new ArrayList<>();
//        hakukohde.getValintatapajonot().forEach(valintatapajono -> {
//            int aloituspaikat = valintatapajono.getValintatapajono().getAloituspaikat();
//            int hyvaksytyt = valintatapajono.getHakemukset().stream().filter(h->hyvaksytytTilat.contains(h.getHakemus().getTila())).collect(Collectors.toList()).size();
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
                h.setHyvaksyttyValintatapaJonosta(false);
            });
        });

        return muuttuneetHakukohteet;
    }

    private boolean onHyvaksyttyHakukohteessa(HakukohdeWrapper hakukohde, HakemusWrapper hakija) {
        return hakukohde.getValintatapajonot()
                .stream().flatMap(v -> v.getHakemukset().stream())
                .filter(h -> h.getHakemus().getHakemusOid().equals(hakija.getHakemus().getHakemusOid()))
                .anyMatch(h->h.getHakemus().getTila().equals(HakemuksenTila.HYVAKSYTTY));
    }

    private boolean onHylattyJonossa(ValintatapajonoWrapper valintatapajonoWrapper, HakemusWrapper hakija) {
        return valintatapajonoWrapper.getHakemukset()
                .stream()
                .anyMatch(h -> h.getHakemus().getHakemusOid().equals(hakija.getHakemus().getHakemusOid()) && h.getHakemus().getTila().equals(HakemuksenTila.HYLATTY));
    }

    private boolean onkoVarasijaisia(ValintatapajonoWrapper valintatapajonoWrapper) {
        return valintatapajonoWrapper.getHakemukset().parallelStream().anyMatch(h->h.getHakemus().getTila().equals(HakemuksenTila.VARALLA));
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
        return hakemukset.stream().filter(h -> !kuuluuHyvaksyttyihinTiloihin(h.getHakemus().getTila())).collect(Collectors.toList());
    }

    private List<HakemusWrapper> muuttuneetVarallaOlijat(List<HakemusWrapper> hakemukset) {
        return hakemukset.stream().filter(h->kuuluuHyvaksyttyihinTiloihin(h.getHakemus().getTila())).collect(Collectors.toList());
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
                hk.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
                hk.getHakemus().setTilanKuvaukset(TilanKuvaukset.peruuntunutHakukierrosOnPaattynyt());
                hk.setTilaVoidaanVaihtaa(false);
            }
        });
    }

    private Set<HakukohdeWrapper> sijoitteleValintatapajon(ValintatapajonoWrapper valintatapajono) {


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

        //List<HakemusWrapper> muuttuneetHakemukset = new ArrayList<HakemusWrapper>();

        Tasasijasaanto saanto = valintatapajono.getValintatapajono().getTasasijasaanto();

        List<HakemusWrapper> eiKorvattavissaOlevatHyvaksytytHakemukset = valintatapajononHyvaksytytHakemuksetJoitaEiVoiKorvata(valintatapajono);

        List<HakemusWrapper> valituksiHaluavatHakemukset =
                valintatapajono.getHakemukset().stream()
                        .filter(h -> !eiKorvattavissaOlevatHyvaksytytHakemukset.contains(h))
                        .filter(h -> !kuuluuHyvaksyttyihinTiloihin(h.getHakemus().getTila()))
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

            valituksiHaluavatHakemukset.forEach(h -> {
                hyvaksyHakemus(h);
                h.setHyvaksyttyValintatapaJonosta(true);
            });
            muuttuneetHakukohteet.addAll(uudelleenSijoiteltavatHakukohteet(muuttuneetHyvaksytyt(valituksiHaluavatHakemukset)));
            return muuttuneetHakukohteet;
        }

        int aloituspaikat = valintatapajono.getValintatapajono().getAloituspaikat();
        int tilaa = aloituspaikat - eiKorvattavissaOlevatHyvaksytytHakemukset.size();

        if(tilaa <= 0) {

            return muuttuneetHakukohteet;
        }

        List<HakemusWrapper> kaikkiTasasijaHakemukset;
        HakemusWrapper paras = valituksiHaluavatHakemukset.get(0);
        if(saanto.equals(Tasasijasaanto.ARVONTA)) {
            kaikkiTasasijaHakemukset = Arrays.asList(paras);
        } else {
            kaikkiTasasijaHakemukset = valituksiHaluavatHakemukset.stream().filter(h-> h.getHakemus().getJonosija().equals(paras.getHakemus().getJonosija())).collect(Collectors.toList());
        }

        List<HakemusWrapper> muuttuneet = new ArrayList<>();


        if(tilaa - kaikkiTasasijaHakemukset.size() >= 0) {
            muuttuneetHyvaksytyt(kaikkiTasasijaHakemukset).forEach(h -> {
                h.setHyvaksyttyValintatapaJonosta(true);
                muuttuneet.addAll(hyvaksyHakemus(h));
            });


            muuttuneetHakukohteet.addAll(uudelleenSijoiteltavatHakukohteet(muuttuneet));

            muuttuneetHakukohteet.addAll(sijoitteleValintatapajon(valintatapajono));

        }
        // Tasasijavertailu
        else {
            if(saanto.equals(Tasasijasaanto.YLITAYTTO)) {
                muuttuneetHyvaksytyt(kaikkiTasasijaHakemukset).forEach(h -> {
                    h.setHyvaksyttyValintatapaJonosta(true);
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
        int aloituspaikat = valintatapajonoWrapper.getValintatapajono().getAloituspaikat();
        int hyvaksytyt = valintatapajonoWrapper.getHakemukset().stream().filter(h-> kuuluuHyvaksyttyihinTiloihin(h.getHakemus().getTila())).collect(Collectors.toList()).size();
        if(aloituspaikat - hyvaksytyt <= 0) {
            return false;
        } else {
            if(valintatapajonoWrapper.getValintatapajono().getTasasijasaanto().equals(Tasasijasaanto.YLITAYTTO)) {
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

        Map<ValintatapajonoWrapper, List<HakemusWrapper>> jonoittain = valituksiHaluavat
                .stream()
                .sorted(comparator::compare)
                .collect(Collectors.groupingBy(HakemusWrapper::getValintatapajono));

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
                    .filter(v -> v.getValintatapajono().getTasasijasaanto().equals(Tasasijasaanto.YLITAYTTO))
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
                    if(j.getValintatapajono().getTasasijasaanto().equals(Tasasijasaanto.ALITAYTTO)) {
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
                .filter(h -> kuuluuHyvaksyttyihinTiloihin(h.getHakemus().getTila()) && voidaanKorvata(h))
                .filter(h -> !hakijaryhmaWrapper.getHakijaryhma().getHakemusOid().contains(h.getHakemus().getHakemusOid()))
                .collect(Collectors.toList());
    }


    private List<HakemusWrapper> etsiValittavat(Map<ValintatapajonoWrapper, List<HakemusWrapper>> jonoittain, Map<Integer, List<HakemusWrapper>> jonojenParhaat) {
        for(Integer i : jonojenParhaat.keySet()) {
            List<HakemusWrapper> parhaat = jonojenParhaat.get(i);
            if(parhaat.size() == 1) {
                HakemusWrapper paras = parhaat.get(0);
                List<HakemusWrapper> parhaatJonoonMahtuvat = haeParhaatJonoonMahtuvat(jonoittain, paras);
                if(!parhaatJonoonMahtuvat.isEmpty()) {
                    return parhaatJonoonMahtuvat;
                }
            } else {
                List<HakemusWrapper> jononMukaanSortattu = parhaat
                        .stream()
                        .sorted((h1, h2) ->
                                Integer.compare(h1.getValintatapajono().getValintatapajono().getPrioriteetti(),
                                        h2.getValintatapajono().getValintatapajono().getPrioriteetti()))
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
        List<HakemusWrapper> samallaSijalla = samallaSijalla(paras, jonoittain.get(paras.getValintatapajono()), paras.getValintatapajono().getValintatapajono().getTasasijasaanto());
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
                .filter(h -> kuuluuHyvaksyttyihinTiloihin(h.getHakemus().getTila()))
                .collect(Collectors.toList()).size();

        int kiintio = hakijaryhmaWrapper.getHakijaryhma().getKiintio();
        boolean tarkkaKiintio = hakijaryhmaWrapper.getHakijaryhma().isTarkkaKiintio();

        ArrayList<HakemusWrapper> muuttuneetHakemukset = new ArrayList<HakemusWrapper>();

        if(tarkkaKiintio && hyvaksyttyjenMaara > kiintio) {
            // Tarkka kiintiö on ylittynyt, pitäis mahdollisesti tiputtaa joku varalle
        } else if(hyvaksyttyjenMaara < kiintio) {
            // Hakijaryhmän valituksi haluavat
            List<HakemusWrapper> valituksiHaluavat = ryhmaanKuuluvat
                    .stream()
                    .filter(h -> h.getHakemus().getTila().equals(HakemuksenTila.VARALLA))
                    .filter(h -> hakijaHaluaa(h) && saannotSallii(h))
                    .collect(Collectors.toList());

            if(!valituksiHaluavat.isEmpty()) {
                Pair<List<HakemusWrapper>, List<HakemusWrapper>> valittavat = seuraavaksiParhaatHakijaryhmasta(valituksiHaluavat, hakijaryhmaWrapper);

                valittavat.getRight().forEach(v -> {
                    muuttuneetHakemukset.addAll(asetaVaralleHakemus(v));
                });

                valittavat.getLeft().forEach(h -> {
                    h.setHyvaksyttyHakijaryhmasta(true);
                    muuttuneetHakemukset.addAll(hyvaksyHakemus(h));
                });

                muuttuneet.addAll(sijoitteleHakijaryhma(hakijaryhmaWrapper));

            }
        }

        muuttuneet.addAll(uudelleenSijoiteltavatHakukohteet(muuttuneetHakemukset));
        return muuttuneet;
    }

    private List<HakemusWrapper> valituksiHaluavatHakemukset(List<HakemusWrapper> hakemukset) {

        return hakemukset.stream()
                .filter(h -> hakijaHaluaa(h) && saannotSallii(h))
                .collect(Collectors.toList());

    }

    // hae hakemukset jotka ovat seuraavana samalla sijalla
    private ArrayList<HakemusWrapper> tasasijaHakemukset(ListIterator<HakemusWrapper> it) {
        ArrayList<HakemusWrapper> tasasijaHakemukset = new ArrayList<HakemusWrapper>();
        while (it.hasNext()) {
            HakemusWrapper h = it.next();
            tasasijaHakemukset.add(h);
            HakemusWrapper seuraava = SijoitteluHelper.peek(it);
            if (seuraava == null || !seuraava.getHakemus().getJonosija().equals(h.getHakemus().getJonosija())) {
                break;
            }
        }
        return tasasijaHakemukset;
    }

    private List<HakemusWrapper> hakijaRyhmaanKuuluvat(List<HakemusWrapper> hakemus, HakijaryhmaWrapper ryhma) {

        return hakemus.stream()
                .filter(h -> ryhma.getHenkiloWrappers().contains(h.getHenkilo()))
                .collect(Collectors.toList());

    }

    private List<List<HakemusWrapper>> hakijaryhmanVarasijajarjestys(HakijaryhmaWrapper hakijaryhmaWrapper) {

        List<List<HakemusWrapper>> list = new ArrayList<List<HakemusWrapper>>();
        List<ListIterator<HakemusWrapper>> iterators = new ArrayList<ListIterator<HakemusWrapper>>();

        for (ValintatapajonoWrapper valintatapajonoWrapper : hakijaryhmaWrapper.getHakukohdeWrapper().getValintatapajonot()) {
            iterators.add(muodostaVarasijaJono(valintatapajonoWrapper.getHakemukset()).listIterator());
        }
        while (true) {
            boolean jatka = false;
            for (ListIterator<HakemusWrapper> it : iterators) {
                if (it.hasNext()) {

                    ArrayList<HakemusWrapper> kaikkiTasasijaHakemukset = tasasijaHakemukset(it);
                    List<HakemusWrapper> valituksiHaluavatHakemukset = valituksiHaluavatHakemukset(kaikkiTasasijaHakemukset);

                    List<HakemusWrapper> a = hakijaRyhmaanKuuluvat(valituksiHaluavatHakemukset, hakijaryhmaWrapper);
                    a = poistaDuplikaatit(a, list);
                    if (a != null && !a.isEmpty()) {
                        list.add(a);
                    }
                    jatka = true;
                }
            }
            if (!jatka) {
                break;
            }
        }

        return list;
    }

    private List<HakemusWrapper> muodostaVarasijaJono(List<HakemusWrapper> hakemukset) {

        return hakemukset.stream()
                .filter(h -> !kuuluuHyvaksyttyihinTiloihin(h.getHakemus().getTila())
                        && hakijaHaluaa(h)
                        && saannotSallii(h))
                .collect(Collectors.toList());
    }

    private List<HakemusWrapper> poistaDuplikaatit(List<HakemusWrapper> hakemukset, List<List<HakemusWrapper>> lista) {
        List<HakemusWrapper> returnable = new ArrayList<HakemusWrapper>();
        for (HakemusWrapper h : hakemukset) {
            boolean found = false;
            for (List<HakemusWrapper> l : lista) {
                for (HakemusWrapper hakemusWrapper : l) {
                    if (hakemusWrapper.getHenkilo().equals(h.getHenkilo())) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    break;
                }
            }
            if (!found) {
                returnable.add(h);
            }
        }
        return returnable;
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
        filters.add(h -> kuuluuHyvaksyttyihinTiloihin(h.getHakemus().getTila()));

        if(taytetaankoPoissaOlevat(valintatapajono)) {
            filters.add(h -> !kuuluuPoissaoloTiloihin(h.getHakemus().getIlmoittautumisTila()));
        }

        return valintatapajono.getHakemukset()
                .stream()
                .filter(filters.stream().reduce(h -> true, Predicate::and))
                .collect(Collectors.toList());



    }

    private ArrayList<HakemusWrapper> eiKorvattavissaOlevatHyvaksytytHakemukset(ValintatapajonoWrapper valintatapajono) {
        ArrayList<HakemusWrapper> eiKorvattavissaOlevatHyvaksytytHakemukset = new ArrayList<HakemusWrapper>();

        boolean taytetaankoPoissaOlevat = taytetaankoPoissaOlevat(valintatapajono);

        for (HakemusWrapper h : valintatapajono.getHakemukset()) {

            boolean korvattavissa = false;
            IlmoittautumisTila tila = h.getHakemus().getIlmoittautumisTila();

            // Jos jonolle on merkitty että täytetään poissa olevien tilalle
            // ja hakija on ilmoittautunut poissaolevaksi voidaan korvata
            if(kuuluuPoissaoloTiloihin(tila) && taytetaankoPoissaOlevat) {
               korvattavissa = true;
            }

            // Hakija on hyväksytty eikä korvattavissa poissolosäännön jälkeen
            if (kuuluuHyvaksyttyihinTiloihin(h.getHakemus().getTila()) && !korvattavissa) {
                // Tarkistetaan onko hakemuswrapperissa sijotteluajon aikaisia merkintöjä
                // sille voidaanko korvata
                if (!voidaanKorvata(h)) {
                    eiKorvattavissaOlevatHyvaksytytHakemukset.add(h);
                }
            }
        }

        return eiKorvattavissaOlevatHyvaksytytHakemukset;
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

        // Hyväksytty valintatapajonosta, ei voida enää korvata käynnissä olevan ajokierroksen aikana
//        if(hakemusWrapper.isHyvaksyttyValintatapaJonosta()) {
//            voidaanKorvata = false;
//        }

        return voidaanKorvata;
    }

    private HashSet<HakemusWrapper> asetaVaralleHakemus(HakemusWrapper varalleAsetettavaHakemusWrapper) {
        HashSet<HakemusWrapper> uudelleenSijoiteltavatHakukohteet = new HashSet<HakemusWrapper>();

        if(varalleAsetettavaHakemusWrapper.isTilaVoidaanVaihtaa()) {
            if(kuuluuHyvaksyttyihinTiloihin(varalleAsetettavaHakemusWrapper.getHakemus().getTila())) {
                for (HakemusWrapper hakemusWrapper : varalleAsetettavaHakemusWrapper.getHenkilo().getHakemukset()) {
                    if(hakemusWrapper.isTilaVoidaanVaihtaa()) {
                        if (!kuuluuHylattyihinTiloihin(hakemusWrapper.getHakemus().getTila())) {
                            hakemusWrapper.getHakemus().setTila(HakemuksenTila.VARALLA);
                            hakemusWrapper.getHakemus().getTilanKuvaukset().clear();
                            uudelleenSijoiteltavatHakukohteet.add(hakemusWrapper);
                        }
                    }
                }
            } else {
                if (!kuuluuHyvaksyttyihinTiloihin(varalleAsetettavaHakemusWrapper.getHakemus().getTila())) {
                    varalleAsetettavaHakemusWrapper.getHakemus().setTila(HakemuksenTila.VARALLA);
                    varalleAsetettavaHakemusWrapper.getHakemus().getTilanKuvaukset().clear();
                }
            }
        }
        return uudelleenSijoiteltavatHakukohteet;
    }

    private HashSet<HakemusWrapper> hyvaksyHakemus(HakemusWrapper hakemus) {
        HashSet<HakemusWrapper> uudelleenSijoiteltavatHakukohteet = new HashSet<HakemusWrapper>();
        if(hakemus.isTilaVoidaanVaihtaa()) {
            if(kuuluuVaraTiloihin(hakemus.getHakemus().getEdellinenTila())) {
                hakemus.getHakemus().setTila(HakemuksenTila.VARASIJALTA_HYVAKSYTTY);
                hakemus.getHakemus().setTilanKuvaukset(TilanKuvaukset.varasijaltaHyvaksytty());
            } else {
                hakemus.getHakemus().setTila(HakemuksenTila.HYVAKSYTTY);
            }

            for (HakemusWrapper h : hakemus.getHenkilo().getHakemukset()) {
                // Alemmat toiveet
                if (h != hakemus && hakemus.getHakemus().getPrioriteetti() < h.getHakemus().getPrioriteetti()) {
                    if(h.isTilaVoidaanVaihtaa()) {
                        if (kuuluuHyvaksyttyihinTiloihin(h.getHakemus().getTila())) {
                            h.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
                            h.getHakemus().setTilanKuvaukset(TilanKuvaukset.peruuntunutYlempiToive());
                            uudelleenSijoiteltavatHakukohteet.add(h);
                        } else {
                            if (!kuuluuHylattyihinTiloihin(h.getHakemus().getTila())) {
                                h.getHakemus().setTilanKuvaukset(TilanKuvaukset.peruuntunutYlempiToive());
                                h.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
                            }
                        }
                    }
                }
                // Saman toiveen muut jonot
                if (h != hakemus && hakemus.getHakemus().getPrioriteetti().equals(h.getHakemus().getPrioriteetti())) {
                    Valintatapajono current = h.getValintatapajono().getValintatapajono();
                    Valintatapajono hyvaksyttyJono = hakemus.getValintatapajono().getValintatapajono();
                    // Peruutetaan vain korkeamman prioriteetin jonot
                    if(hyvaksyttyJono.getPrioriteetti() < current.getPrioriteetti()) {
                        // Perustapaus
                        if(h.isTilaVoidaanVaihtaa()) {
                            if (!kuuluuHylattyihinTiloihin(h.getHakemus().getTila())) {
                                HakemuksenTila vanhaTila = h.getHakemus().getTila();
                                h.getHakemus().setTilanKuvaukset(TilanKuvaukset.peruuntunutHyvaksyttyToisessaJonossa());
                                h.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
                                if(kuuluuHyvaksyttyihinTiloihin(vanhaTila)) {
                                    uudelleenSijoiteltavatHakukohteet.add(h);
                                }
                            }
                        } else {
                            // Hakemukselle merkattu, että tilaa ei voi vaihtaa, mutta vaihdetaan kuitenkin jos hyväksytty
                            HakemuksenTila vanhaTila = h.getHakemus().getTila();
                            if(kuuluuHyvaksyttyihinTiloihin(vanhaTila)) {
                                h.getHakemus().setTilanKuvaukset(TilanKuvaukset.peruuntunutHyvaksyttyToisessaJonossa());
                                h.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);

                                Optional<Valintatulos> jononTulos = h.getHenkilo().getValintatulos().stream().filter(v -> v.getValintatapajonoOid().equals(current.getOid())).findFirst();
                                if(jononTulos.isPresent() && !jononTulos.get().getTila().equals(ValintatuloksenTila.KESKEN)) {

                                    Valintatulos muokattava = jononTulos.get();

                                    Optional<Valintatulos> nykyinenTulos = h.getHenkilo().getValintatulos().stream().filter(v -> v.getValintatapajonoOid().equals(hyvaksyttyJono.getOid())).findFirst();
                                    Valintatulos nykyinen;
                                    if(nykyinenTulos.isPresent()) {
                                        nykyinen = nykyinenTulos.get();
                                        nykyinen.setHyvaksyttyVarasijalta(muokattava.getHyvaksyttyVarasijalta());
                                        nykyinen.setIlmoittautumisTila(muokattava.getIlmoittautumisTila());
                                        nykyinen.setJulkaistavissa(muokattava.getJulkaistavissa());
                                        nykyinen.setLogEntries(muokattava.getLogEntries());
                                        nykyinen.setTila(muokattava.getTila());
                                    } else {
                                        nykyinen = new Valintatulos();
                                        nykyinen.setHyvaksyttyVarasijalta(muokattava.getHyvaksyttyVarasijalta());
                                        nykyinen.setIlmoittautumisTila(muokattava.getIlmoittautumisTila());
                                        nykyinen.setJulkaistavissa(muokattava.getJulkaistavissa());
                                        nykyinen.setLogEntries(muokattava.getLogEntries());
                                        nykyinen.setTila(muokattava.getTila());
                                        nykyinen.setValintatapajonoOid(hyvaksyttyJono.getOid());
                                        nykyinen.setHakemusOid(muokattava.getHakemusOid());
                                        nykyinen.setHakijaOid(muokattava.getHakijaOid());
                                        nykyinen.setHakukohdeOid(muokattava.getHakukohdeOid());
                                        nykyinen.setHakuOid(muokattava.getHakuOid());
                                        nykyinen.setHakutoive(muokattava.getHakutoive());
                                        hakemus.getHenkilo().getValintatulos().add(nykyinen);
                                    }

                                    muokattava.setTila(ValintatuloksenTila.KESKEN);
                                    muokattava.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY);
                                    muokattava.setHyvaksyttyVarasijalta(false);

                                    // Lisää muokatut valintatulokset listaan tallennusta varten
                                    sijoitteluAjo.getMuuttuneetValintatulokset()
                                            .addAll(Arrays.asList(muokattava, nykyinen));

                                }
                                if(vanhaTila == HakemuksenTila.HYVAKSYTTY) {
                                    hakemus.getHakemus().setTila(HakemuksenTila.HYVAKSYTTY);
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
        Hakemus hakemus = hakemusWrapper.getHakemus();

        boolean hakemuksenTila = !kuuluuHylattyihinTiloihin(hakemus.getTila());

        boolean hakijaAloistuspaikkojenSisallaTaiVarasijataytto = true;

        boolean eiVarasijaTayttoa = false;


        // Jos varasijasäännöt ovat astuneet voimaan niin katsotaan saako varasijoilta täyttää
        if(sijoitteluAjo.varasijaSaannotVoimassa()) {
            if(hakemusWrapper.getValintatapajono().getValintatapajono().getEiVarasijatayttoa() != null) {
                eiVarasijaTayttoa = hakemusWrapper.getValintatapajono().getValintatapajono().getEiVarasijatayttoa();
            }
        }

        if(eiVarasijaTayttoa && !hakemusWrapper.getValintatapajono().getValintatapajono().getKaikkiEhdonTayttavatHyvaksytaan()) {
            hakijaAloistuspaikkojenSisallaTaiVarasijataytto  = hakijaAloistuspaikkojenSisalla(hakemusWrapper);
            if(!hakijaAloistuspaikkojenSisallaTaiVarasijataytto && sijoitteluAjo.isKKHaku() && hakemusWrapper.isTilaVoidaanVaihtaa()) {
                hakemusWrapper.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
                hakemusWrapper.getHakemus().setTilanKuvaukset(TilanKuvaukset.peruuntunutAloituspaikatTaynna());
            }
        }

        Integer varasijat = hakemusWrapper.getValintatapajono().getValintatapajono().getVarasijat();
        boolean huomioitavienVarasijojenSisalla = true;

        if(sijoitteluAjo.varasijaSaannotVoimassa()
                && varasijat != null
                && varasijat > 0) {
            huomioitavienVarasijojenSisalla = hakijaKasiteltavienVarasijojenSisalla(hakemusWrapper, varasijat);
            if(!huomioitavienVarasijojenSisalla && hakemusWrapper.isTilaVoidaanVaihtaa()) {
                hakemusWrapper.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
                hakemusWrapper.getHakemus().setTilanKuvaukset(TilanKuvaukset.peruuntunutEiMahduKasiteltavienVarasijojenMaaraan());
            }

        }

        boolean eiPeruttuaKorkeampaaTaiSamaaHakutoivetta =  eiPeruttuaKorkeampaaTaiSamaaHakutoivetta(hakemusWrapper);

        return hakemuksenTila && hakijaAloistuspaikkojenSisallaTaiVarasijataytto && eiPeruttuaKorkeampaaTaiSamaaHakutoivetta && huomioitavienVarasijojenSisalla;
    }

    protected boolean eiPeruttuaKorkeampaaTaiSamaaHakutoivetta(HakemusWrapper hakemusWrapper) {

        return hakemusWrapper.getHenkilo().getHakemukset()
                .stream()
                .filter(h -> h != hakemusWrapper)
                .noneMatch(h -> h.getHakemus().getTila() == HakemuksenTila.PERUNUT
                        && h.getHakemus().getPrioriteetti() <= hakemusWrapper.getHakemus().getPrioriteetti());
    }

    private boolean hakijaAloistuspaikkojenSisalla(HakemusWrapper hakemusWrapper) {
        ValintatapajonoWrapper valintatapajono = hakemusWrapper.getValintatapajono() ;
        int aloituspaikat = valintatapajono.getValintatapajono().getAloituspaikat();
        List<HakemusWrapper> hakemukset = valintatapajono.getHakemukset();

        int i = 0;

        for(HakemusWrapper h : hakemukset )  {

            if(h.getHakemus().getTila() != HakemuksenTila.HYLATTY) {
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

    private boolean hakijaKasiteltavienVarasijojenSisalla(HakemusWrapper hakemusWrapper, Integer varasijat) {
        ValintatapajonoWrapper valintatapajono = hakemusWrapper.getValintatapajono() ;
        int aloituspaikat = valintatapajono.getValintatapajono().getAloituspaikat() + varasijat;
        List<HakemusWrapper> hakemukset = valintatapajono.getHakemukset();

        int i = 0;

        for(HakemusWrapper h : hakemukset )  {

            if(h.getHakemus().getTila() != HakemuksenTila.HYLATTY) {
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
        if(!hakemusWrapper.isTilaVoidaanVaihtaa() && hakemusWrapper.getHakemus().getTila() == HakemuksenTila.PERUUNTUNUT) {
            return false;
        }

        for (HakemusWrapper h : henkilo.getHakemukset()) {
            if (kuuluuHyvaksyttyihinTiloihin(h.getHakemus().getTila())
                    &&
                    // Hakija hyväksytty paremmalle hakutoiveelle
                    (h.getHakemus().getPrioriteetti() < hakemusWrapper.getHakemus().getPrioriteetti()
                            ||
                            // Hakija hyväksytty paremman prioriteetin jonossa
                            (h.getHakemus().getPrioriteetti().equals(hakemusWrapper.getHakemus().getPrioriteetti())
                                    && h.getValintatapajono().getValintatapajono().getPrioriteetti() < hakemusWrapper.getValintatapajono().getValintatapajono().getPrioriteetti()))
                    &&
                    // eikä vertailla itseensä
                    hakemusWrapper != h) {
                return false;
            }
        }
        return true;
    }



    private List<HakemusWrapper> huonoimmatTasasijaiset(ValintatapajonoWrapper valintatapajonoWrapper) {
        HakemusWrapperComparator comparator = new HakemusWrapperComparator();
        List<HakemusWrapper> sortattu = valintatapajonoWrapper.getHakemukset()
                .stream()
                .sorted((h1, h2) -> comparator.compare(h2, h1))
                .collect(Collectors.toList());


        Optional<HakemusWrapper> huonoinHakemus = sortattu.stream()
                .filter(h -> kuuluuHyvaksyttyihinTiloihin(h.getHakemus().getTila()) && voidaanKorvata(h))
                .findFirst();

        if(huonoinHakemus.isPresent()) {
            return sortattu.stream()
                    .filter(h-> h.getHakemus().getJonosija().equals(huonoinHakemus.get().getHakemus().getJonosija()) && voidaanKorvata(h))
                    .collect(Collectors.toList());
        } else {
            return new ArrayList<>();
        }

    }

    private int hakijaryhmassaVajaata(HakijaryhmaWrapper hakijaryhmaWrapper) {
        int needed = hakijaryhmaWrapper.getHakijaryhma().getKiintio();
        for (ValintatapajonoWrapper valintatapajonoWrapper : hakijaryhmaWrapper.getHakukohdeWrapper().getValintatapajonot()) {
            for (HakemusWrapper h : valintatapajonoWrapper.getHakemukset()) {
                if (kuuluuHyvaksyttyihinTiloihin(h.getHakemus().getTila()) && hakijaryhmaWrapper.getHenkiloWrappers().contains(h.getHenkilo())) {
                    needed--;
                }
            }
        }
        return needed;
    }

}
