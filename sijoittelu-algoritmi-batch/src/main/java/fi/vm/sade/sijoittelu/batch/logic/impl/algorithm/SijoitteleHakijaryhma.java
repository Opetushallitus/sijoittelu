package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import com.google.common.collect.Lists;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.comparator.HakemusWrapperComparator;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.*;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Tasasijasaanto;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.sijoittelu.domain.comparator.HakemusComparator;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilaTaulukot.*;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilojenMuokkaus.asetaTilaksiVaralla;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.WrapperHelperMethods.*;

public class SijoitteleHakijaryhma {
    private static void merkitseHakijaryhmastaHyvaksytyt(HakijaryhmaWrapper hakijaryhmaWrapper) {
        Set<String> hakijaryhmaanKuuluvat = new HashSet<>(hakijaryhmaWrapper.getHakijaryhma().getHakemusOid());
        List<ValintatapajonoWrapper> liittyvatJonot = hakijaryhmaanLiittyvatJonot(hakijaryhmaWrapper);
        List<List<List<Hakemus>>> jonot = liittyvatJonot.stream()
                .sorted((j, jj) -> j.getValintatapajono().getPrioriteetti().compareTo(jj.getValintatapajono().getPrioriteetti()))
                .map(j -> {
                    if (Tasasijasaanto.ARVONTA == j.getValintatapajono().getTasasijasaanto()) {
                        return j.getHakemukset().stream()
                                .map(h -> h.getHakemus())
                                .sorted(new HakemusComparator())
                                .map(h -> Collections.singletonList(h))
                                .collect(Collectors.toList());
                    } else {
                        return j.getHakemukset().stream()
                                .map(h -> h.getHakemus())
                                .collect(Collectors.groupingBy(h -> h.getJonosija()))
                                .values().stream()
                                .sorted((h, hh) -> new HakemusComparator().compare(h.get(0), hh.get(0))) // FIXME hyväksy peruuntunut ei toimi
                                .collect(Collectors.toList());
                    }
                })
                .collect(Collectors.toList());
        List<Pair<Integer, List<Hakemus>>> hyvaksymisjarjestyksessa = new ArrayList<>();
        int suurinPrioriteetti = liittyvatJonot.stream()
                .flatMap(j -> j.getValintatapajono().getHakemukset().stream())
                .mapToInt(h -> h.getJonosija())
                .max().orElse(0);
        for (int p = 0; p <= suurinPrioriteetti; p++) {
            for (int i = 0; i < jonot.size(); i++) {
                List<List<Hakemus>> jono = jonot.get(i);
                while (!jono.isEmpty() && jono.get(0).get(0).getJonosija() == p) {
                    hyvaksymisjarjestyksessa.add(Pair.of(i, jono.get(0)));
                    jono.remove(0);
                }
            }
        }
        int[] paikkoja = liittyvatJonot.stream()
                .sorted((j, jj) -> j.getValintatapajono().getPrioriteetti().compareTo(jj.getValintatapajono().getPrioriteetti()))
                .mapToInt(j -> j.getValintatapajono().getAloituspaikat() - (int)j.getHakemukset().stream()
                        .filter(h -> kuuluuHyvaksyttyihinTiloihin(hakemuksenTila(h)) && !hakijaryhmaanKuuluvat.contains(h.getHakemus().getHakemusOid()))
                        .count())
                .toArray();
        List<Tasasijasaanto> tasasijasaannot = liittyvatJonot.stream()
                .sorted((j, jj) -> j.getValintatapajono().getPrioriteetti().compareTo(jj.getValintatapajono().getPrioriteetti()))
                .map(j -> j.getValintatapajono().getTasasijasaanto())
                .collect(Collectors.toList());
        int kiintio = Math.min(
                hakijaryhmaWrapper.getHakijaryhma().getKiintio(),
                liittyvatJonot.stream().mapToInt(j -> j.getValintatapajono().getAloituspaikat()).sum()
        );
        Map<String, Hakemus> hyvaksytyt = new HashMap<>(kiintio);
        for (Pair<Integer, List<Hakemus>> hyvaksyttavat : hyvaksymisjarjestyksessa) {
            if (hyvaksytyt.size() >= kiintio) {
                break;
            }
            List<Hakemus> hyvaksyttavissa = hyvaksyttavat.getRight().stream()
                    .filter(h -> hakijaryhmaanKuuluvat.contains(h.getHakemusOid()))
                    .filter(h -> kuuluuHyvaksyttyihinTiloihin(h.getTila()) || kuuluuVaraTiloihin(h.getTila()))
                    .filter(h -> !hyvaksytyt.containsKey(h.getHakemusOid()))
                    .collect(Collectors.toList());
            int paikkojaJaljella = paikkoja[hyvaksyttavat.getLeft()];
            boolean ylitaytto = Tasasijasaanto.YLITAYTTO == tasasijasaannot.get(hyvaksyttavat.getLeft());
            if (paikkojaJaljella > 0 && (ylitaytto || hyvaksyttavissa.size() <= paikkojaJaljella)) {
                for (Hakemus h : hyvaksyttavissa) {
                    paikkoja[hyvaksyttavat.getLeft()]--;
                    hyvaksytyt.put(h.getHakemusOid(), h);
                }
            }
        }
        hyvaksytyt.values().forEach(h -> {
            h.setHyvaksyttyHakijaryhmasta(true);
            h.setHakijaryhmaOid(hakijaryhmaWrapper.getHakijaryhma().getOid());
        });
    }

    public static Set<HakukohdeWrapper> sijoitteleHakijaryhma(SijoitteluajoWrapper sijoitteluAjo, HakijaryhmaWrapper hakijaryhmaWrapper) {
        merkitseHakijaryhmastaHyvaksytyt(hakijaryhmaWrapper);
        return sijoitteleHakijaryhmaRecur(sijoitteluAjo, hakijaryhmaWrapper);
    }

    public static Set<HakukohdeWrapper> sijoitteleHakijaryhmaRecur(SijoitteluajoWrapper sijoitteluAjo, HakijaryhmaWrapper hakijaryhmaWrapper) {
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
            final List<ValintatapajonoWrapper> liittyvatJonotVarasijatayttoVoimassa = liittyvatJonot.stream().filter(vtj -> sijoitteluAjo.onkoVarasijaSaannotVoimassaJaVarasijaTayttoKaynnissa(vtj)).collect(Collectors.toList());
            kasitteleValituksiHaluavat(sijoitteluAjo, hakijaryhmaWrapper, liittyvatJonotVarasijatayttoVoimassa, ryhmaanKuuluvat, muuttuneet, muuttuneetHakemukset);
        }
        muuttuneet.addAll(SijoitteleHakukohde.uudelleenSijoiteltavatHakukohteet(muuttuneetHakemukset));
        return muuttuneet;
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
                .filter(h -> SijoitteleHakukohde.hakijaHaluaa(h) && SijoitteleHakukohde.saannotSallii(sijoitteluAjo, h))
                .forEach(h -> h.setHyvaksyttavissaHakijaryhmanJalkeen(false));
    }

    private static void kasitteleValituksiHaluavat(SijoitteluajoWrapper sijoitteluAjo, HakijaryhmaWrapper hakijaryhmaWrapper, List<ValintatapajonoWrapper> liittyvatJonot, List<HakemusWrapper> ryhmaanKuuluvat, Set<HakukohdeWrapper> muuttuneet, List<HakemusWrapper> muuttuneetHakemukset) {
        List<Valintatapajono> valintatapajonot = liittyvatJonot.stream().map(ValintatapajonoWrapper::getValintatapajono).collect(Collectors.toList());
        List<HakemusWrapper> valituksiHaluavat = ryhmaanKuuluvat
                .stream()
                .filter(h -> valintatapajonot.contains(h.getValintatapajono().getValintatapajono())) // Varmistetaan, että hakemuksen valintatapajonon varasijasäännöt täyttyvät
                .filter(h -> hakemuksenTila(h).equals(HakemuksenTila.VARALLA))
                .filter(h -> SijoitteleHakukohde.hakijaHaluaa(h) && SijoitteleHakukohde.saannotSallii(sijoitteluAjo, h))
                .collect(Collectors.toList());
        if (!valituksiHaluavat.isEmpty()) {
            tarkistaEttaKaikkienTilaaVoidaanVaihtaa(valituksiHaluavat);

            Pair<List<HakemusWrapper>, List<HakemusWrapper>> valittavatJaVarasijat = seuraavaksiParhaatHakijaryhmasta(valituksiHaluavat, hakijaryhmaWrapper);
            // Aloituspaikat täynnä ylitäytöllä, joten tiputetaan varalle
            valittavatJaVarasijat.getRight().forEach(v -> {
                muuttuneetHakemukset.addAll(asetaVaralleHakemus(v));
            });
            // Hyväksytään valittavat
            valittavatJaVarasijat.getLeft().forEach(h -> {
                if (!h.getHakemus().isHyvaksyttyHakijaryhmasta()) {
                    throw new IllegalStateException("");
                }
                h.setHyvaksyttyHakijaryhmasta(true);
                muuttuneetHakemukset.addAll(SijoitteleHakukohde.hyvaksyHakemus(sijoitteluAjo, h));
            });
            boolean lukko = liittyvatJonot.stream().anyMatch(ValintatapajonoWrapper::isAlitayttoLukko);
            // Kiintiö ei täyty, koska alitäyttö
            if (!lukko && (!valittavatJaVarasijat.getLeft().isEmpty() || !valittavatJaVarasijat.getRight().isEmpty())) {
                muuttuneet.addAll(sijoitteleHakijaryhmaRecur(sijoitteluAjo, hakijaryhmaWrapper));
            }
        }
    }

    private static void tarkistaEttaKaikkienTilaaVoidaanVaihtaa(List<HakemusWrapper> valituksiHaluavat) {
        List<HakemusWrapper> valituiksiHaluavatJoidenTilaaEiVoiVaihtaa = valituksiHaluavat.stream().filter(h -> !h.isTilaVoidaanVaihtaa()).collect(Collectors.toList());
        if (!valituiksiHaluavatJoidenTilaaEiVoiVaihtaa.isEmpty()) {
            StringBuilder errorMessage =  new StringBuilder(String.format("Löytyy %d hakemusta, joiden tilaa ei voi vaihtaa:", valituiksiHaluavatJoidenTilaaEiVoiVaihtaa.size()));
            for (HakemusWrapper hakemusWrapper : valituiksiHaluavatJoidenTilaaEiVoiVaihtaa) {
                errorMessage.append(ToStringBuilder.reflectionToString(hakemusWrapper)).append(", ");
            }
            throw new IllegalStateException(errorMessage.toString());
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
}
