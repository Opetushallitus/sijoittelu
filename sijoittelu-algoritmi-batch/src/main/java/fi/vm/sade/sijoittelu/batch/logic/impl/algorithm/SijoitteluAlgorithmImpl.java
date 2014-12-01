package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import fi.vm.sade.service.valintaperusteet.dto.model.Kieli;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.comparator.HakemusWrapperComparator;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor.PostSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilanKuvaukset;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.*;
import fi.vm.sade.sijoittelu.domain.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author Kari Kammonen
 *
 */
public class SijoitteluAlgorithmImpl implements SijoitteluAlgorithm {

    protected SijoitteluAlgorithmImpl() { 	}

    protected SijoitteluajoWrapper sijoitteluAjo;

    protected List<PreSijoitteluProcessor> preSijoitteluProcessors;

    protected List<PostSijoitteluProcessor> postSijoitteluProcessors;

    protected int depth = 0;

    private final List<HakemuksenTila> hyvaksytytTilat = Arrays.asList(HakemuksenTila.HYVAKSYTTY,HakemuksenTila.VARASIJALTA_HYVAKSYTTY);
    private final List<HakemuksenTila> varaTilat = Arrays.asList(HakemuksenTila.VARALLA,HakemuksenTila.VARASIJALTA_HYVAKSYTTY);
    private final List<IlmoittautumisTila> poissaoloTilat = Arrays.asList(IlmoittautumisTila.POISSA, IlmoittautumisTila.POISSA_KOKO_LUKUVUOSI, IlmoittautumisTila.POISSA_SYKSY);
    private final List<HakemuksenTila> hylatytTilat = Arrays.asList(HakemuksenTila.PERUNUT, HakemuksenTila.PERUUTETTU, HakemuksenTila.HYLATTY);

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
        for (HakukohdeWrapper hakukohde : sijoitteluAjo.getHakukohteet()) {
            sijoittele(hakukohde, 0);
        }
    }

    private void sijoittele(HakukohdeWrapper hakukohde, int n) {
        n++;
        if (n > depth) {
            depth = n;
        }

        for (ValintatapajonoWrapper valintatapajono : hakukohde.getValintatapajonot()) {
            this.sijoittele(valintatapajono, n);

        }

        for (HakijaryhmaWrapper hakijaryhmaWrapper : hakukohde.getHakijaryhmaWrappers()) {
            this.sijoittele(hakijaryhmaWrapper, n);
        }

        // Tayttöjonot
        ArrayList<HakemusWrapper> muuttuneetHakemukset = new ArrayList<>();
        hakukohde.getValintatapajonot().forEach(valintatapajono -> {
            int aloituspaikat = valintatapajono.getValintatapajono().getAloituspaikat();
            int hyvaksytyt = valintatapajono.getHakemukset().stream().filter(h->hyvaksytytTilat.contains(h.getHakemus().getTila())).collect(Collectors.toList()).size();
            int tilaa = aloituspaikat - hyvaksytyt;
            String tayttojono = valintatapajono.getValintatapajono().getTayttojono();

            LocalDateTime varasijaTayttoPaattyy = varasijaTayttoPaattyy(valintatapajono);

            if(sijoitteluAjo.varasijaSaannotVoimassa()
                    && sijoitteluAjo.getToday().isBefore(varasijaTayttoPaattyy)
                    && tilaa > 0 && tayttojono != null && !tayttojono.isEmpty()
                    && !tayttojono.equals(valintatapajono.getValintatapajono().getOid())
                    && !onkoVarasijaisia(valintatapajono)) {
                // Vielä on tilaa ja pitäis jostain täytellä

                Optional<ValintatapajonoWrapper> opt = hakukohde.getValintatapajonot()
                        .stream().filter(v -> v.getValintatapajono().getOid().equals(tayttojono)).findFirst();

                if(opt.isPresent()) {
                    // Jono löytyi hakukohteen jonoista
                    ValintatapajonoWrapper kasiteltava = opt.get();
                    List<HakemusWrapper> varasijajono = muodostaVarasijaJono(kasiteltava.getHakemukset())
                            .stream()
                                    //.filter(h -> !onHyvaksyttyHakukohteessa(hakukohde, h))
                            .filter(h -> onHylattyJonossa(valintatapajono, h))
                            .collect(Collectors.toList());

                    while(tilaa > 0 && !varasijajono.isEmpty()) {
                        // Vielä on tilaa ja hakemuksia, jotka ei oo tässä hakukohteessa hyväksyttyjä
                        //HakemusWrapper hyvaksyttava = varasijajono.get(0);
                        HakemusWrapper hyvaksyttava = valintatapajono.getHakemukset()
                                .stream()
                                .filter(h-> h.getHakemus().getHakemusOid().equals(varasijajono.get(0).getHakemus().getHakemusOid()))
                                .findFirst()
                                .get();
                        hyvaksyttava.getHakemus().setTilanKuvaukset(TilanKuvaukset.hyvaksyttyTayttojonoSaannolla(kasiteltava.getValintatapajono().getNimi()));
                        muuttuneetHakemukset.addAll(hyvaksyHakemus(hyvaksyttava));
                        tilaa--;
                        varasijajono.remove(hyvaksyttava);
                    }

                }
            }
        });

        for (HakukohdeWrapper v : uudelleenSijoiteltavatHakukohteet(muuttuneetHakemukset)) {
            sijoittele(v, n);
        }

    }

    private boolean onHyvaksyttyHakukohteessa(HakukohdeWrapper hakukohde, HakemusWrapper hakija) {
        return hakukohde.getValintatapajonot()
                .stream().flatMap(v -> v.getHakemukset().stream())
                .filter(h->h.getHakemus().getHakemusOid().equals(hakija.getHakemus().getHakemusOid()))
                .anyMatch(h->h.getHakemus().getTila().equals(HakemuksenTila.HYVAKSYTTY));
    }

    private boolean onHylattyJonossa(ValintatapajonoWrapper valintatapajonoWrapper, HakemusWrapper hakija) {
        return valintatapajonoWrapper.getHakemukset()
                .stream()
                .anyMatch(h-> h.getHakemus().getHakemusOid().equals(hakija.getHakemus().getHakemusOid()) && h.getHakemus().getTila().equals(HakemuksenTila.HYLATTY));
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

    private void sijoittele(ValintatapajonoWrapper valintatapajono, int n) {

        ArrayList<HakemusWrapper> hyvaksyttavaksi = new ArrayList<HakemusWrapper>();
        ArrayList<HakemusWrapper> varalle = new ArrayList<HakemusWrapper>();

        Tasasijasaanto saanto = valintatapajono.getValintatapajono().getTasasijasaanto();

        ArrayList<HakemusWrapper> eiKorvattavissaOlevatHyvaksytytHakemukset = eiKorvattavissaOlevatHyvaksytytHakemukset(valintatapajono);

        int aloituspaikat = valintatapajono.getValintatapajono().getAloituspaikat();
        int tilaa = aloituspaikat - eiKorvattavissaOlevatHyvaksytytHakemukset.size();

        LocalDateTime varasijaTayttoPaattyy = varasijaTayttoPaattyy(valintatapajono);

        ListIterator<HakemusWrapper> it = valintatapajono.getHakemukset().listIterator();
        boolean tasasijaSaantoKaytetty = false;
        Set<HakemusWrapper> kaytetyt = new HashSet<>();
        while (it.hasNext()) {
            ArrayList<HakemusWrapper> kaikkiTasasijaHakemukset = tasasijaHakemukset(it);
            if(kaytetyt.containsAll(kaikkiTasasijaHakemukset)) {
                continue;
            }
            List<HakemusWrapper> valituksiHaluavatHakemukset =
                    valituksiHaluavatHakemukset(kaikkiTasasijaHakemukset).stream()
                            .filter(hk -> !eiKorvattavissaOlevatHyvaksytytHakemukset.contains(hk))
                            .collect(Collectors.toList());

            if(sijoitteluAjo.hakukierrosOnPaattynyt() || sijoitteluAjo.getToday().isAfter(varasijaTayttoPaattyy)) {
                // Hakukierros on päättynyt tai käsiteltävän jonon varasijasäännöt eivät ole enää voimassa.
                // Asetetaan kaikki hakemukset joiden tila voidaan vaihtaa tilaan peruuntunut
                valituksiHaluavatHakemukset.forEach(hk -> {
                    if(hk.isTilaVoidaanVaihtaa()) {
                        hk.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
                        hk.getHakemus().setTilanKuvaukset(TilanKuvaukset.peruuntunutHakukierrosOnPaattynyt());
                        hk.setTilaVoidaanVaihtaa(false);
                    }

                });

            } else if(tasasijaSaantoKaytetty) {
                varalle.addAll(valituksiHaluavatHakemukset);
            } else if(tilaa - valituksiHaluavatHakemukset.size() >= 0) {
                hyvaksyttavaksi.addAll(valituksiHaluavatHakemukset);
                tilaa = tilaa - valituksiHaluavatHakemukset.size();
            } else if(tilaa > 0 && tilaa - valituksiHaluavatHakemukset.size() < 0) {
                if(saanto == Tasasijasaanto.ALITAYTTO) {
                    varalle.addAll(valituksiHaluavatHakemukset);
                } else if(saanto == Tasasijasaanto.YLITAYTTO) {
                    hyvaksyttavaksi.addAll(valituksiHaluavatHakemukset);
                } else { // Arvonta
                    for (HakemusWrapper aValituksiHaluavatHakemukset : valituksiHaluavatHakemukset) {
                        if (tilaa > 0) {
                            hyvaksyttavaksi.add(aValituksiHaluavatHakemukset);
                            tilaa--;
                        } else {
                            varalle.add(aValituksiHaluavatHakemukset);
                        }
                    }
                }
                tasasijaSaantoKaytetty = true;
            } else {
                varalle.addAll(valituksiHaluavatHakemukset);
            }
            kaytetyt.addAll(kaikkiTasasijaHakemukset);

        }

        // kay lavitse muutokset ja tarkista tarvitaanko rekursiota?
        ArrayList<HakemusWrapper> muuttuneetHakemukset = new ArrayList<HakemusWrapper>();

        hyvaksyttavaksi.stream()
                .filter(h -> !hyvaksytytTilat.contains(h.getHakemus().getTila()))
                .forEach(h -> muuttuneetHakemukset.addAll(hyvaksyHakemus(h)));

        varalle.stream()
                .filter(h->hyvaksytytTilat.contains(h.getHakemus().getTila()))
                .forEach(h->muuttuneetHakemukset.addAll(asetaVaralleHakemus(h)));

        uudelleenSijoiteltavatHakukohteet(muuttuneetHakemukset).forEach(v->sijoittele(v,n));

    }

    private void sijoittele(HakijaryhmaWrapper hakijaryhmaWrapper, int n) {
        ArrayList<HakemusWrapper> muuttuneetHakemukset = new ArrayList<HakemusWrapper>();
        List<List<HakemusWrapper>> hakijaryhmanVarasijallaOlevat = hakijaryhmanVarasijajarjestys(hakijaryhmaWrapper);
        ListIterator<List<HakemusWrapper>> it = hakijaryhmanVarasijallaOlevat.listIterator();
        Hakijaryhma ryhma = hakijaryhmaWrapper.getHakijaryhma();


        while (it.hasNext() && hakijaryhmassaVajaata(hakijaryhmaWrapper) > 0 && !hakijaryhmaWrapper.isAlitayttoSaantoTaytetty()) {
            List<HakemusWrapper> tasasijaVarasijaHakemukset = it.next();
            Map<String, List<HakemusWrapper>> jonoittain =
                tasasijaVarasijaHakemukset.stream()
                        .collect(Collectors.groupingBy(h->h.getValintatapajono().getValintatapajono().getOid()));

            for (HakemusWrapper paras : tasasijaVarasijaHakemukset) {
                ValintatapajonoWrapper v = paras.getValintatapajono();
                Tasasijasaanto saanto = v.getValintatapajono().getTasasijasaanto();

                if(ryhma.getValintatapajonoOid() == null || ryhma.getValintatapajonoOid().equals(v.getValintatapajono().getOid())) {
                    // Haetaan valintatapajonon hyväksytyt
                    int hyvaksytyt = v.getHakemukset().stream().filter(h->hyvaksytytTilat.contains(h.getHakemus().getTila())).collect(Collectors.toList()).size();
                    // Paljonko jonossa on tilaa
                    int tilaa = v.getValintatapajono().getAloituspaikat() - hyvaksytyt;

                    // Kuinka paljon hakijaryhmän halukkaista on tästä jonosta
                    int haluavat = jonoittain.getOrDefault(v.getValintatapajono().getOid(), new ArrayList<>()).size();

//                    if(ryhma.getValintatapajonoOid() == null) {
//                        haluavat = jonoittain.getOrDefault(v.getValintatapajono().getOid(), new ArrayList<>()).size();
//                    } else {
//                        haluavat = jonoittain.getOrDefault(v.getValintatapajono().getOid(), new ArrayList<>()).size();
//                    }


                    if(haluavat == 0) { // Jo käsitelty loopissa
                        // Ei tehä mitään
                    } else if(tilaa - haluavat < 0) { // tasasija tilanne
                        if(saanto == Tasasijasaanto.YLITAYTTO) {
                            jonoittain.get(v.getValintatapajono().getOid()).forEach(h-> {
                                h.setHyvaksyttyHakijaryhmastaTaiTayttoJonosta(true);
                                muuttuneetHakemukset.addAll(hyvaksyHakemus(h));
                            });
                        }
                        jonoittain.remove(v.getValintatapajono().getOid());

                        // Asetetaan huonoimmat varalle
                        huonoimmatTasasijaiset(v).forEach(h ->
                                muuttuneetHakemukset.addAll(asetaVaralleHakemus(h))
                        );

                        v.getHakemukset().forEach(h -> {
                            if(hyvaksytytTilat.contains(h.getHakemus().getTila()) && !h.isHyvaksyttyHakijaryhmastaTaiTayttoJonosta()) {
                                h.setHyvaksyttavissaHakijaryhmanJalkeen(true);
                            }
                        });

                        if(saanto == Tasasijasaanto.ALITAYTTO) {
                            hakijaryhmaWrapper.setAlitayttoSaantoTaytetty(true);
                        }

                    } else {
                        jonoittain.get(v.getValintatapajono().getOid()).forEach(h-> {
                            h.setHyvaksyttyHakijaryhmastaTaiTayttoJonosta(true);
                            muuttuneetHakemukset.addAll(hyvaksyHakemus(h));
                        });
                        jonoittain.remove(v.getValintatapajono().getOid());
                    }

//                    HakemusWrapper huonoin = haeHuonoinValittuEiVajaaseenRyhmaanKuuluva(v, hakijaryhmaWrapper);
//                    muuttuneetHakemukset.addAll(hyvaksyHakemus(paras));
//                    muuttuneetHakemukset.add(paras);
//                    paras.setTilaVoidaanVaihtaa(false);
//                    paras.setHyvaksyttyHakijaryhmastaTaiTayttoJonosta(true);
//                    if (huonoin != null) {
//                        muuttuneetHakemukset.addAll(asetaVaralleHakemus(huonoin));
//                    }

                }
            }
        }
        Set<HakukohdeWrapper> uudelleenSijoiteltavatHakukohteet = uudelleenSijoiteltavatHakukohteet(muuttuneetHakemukset);
        for (HakukohdeWrapper h : uudelleenSijoiteltavatHakukohteet) {
            sijoittele(h, n);
        }

    }

    private List<HakemusWrapper> valituksiHaluavatHakemukset(ArrayList<HakemusWrapper> hakemukset) {

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
                .filter(h -> !hyvaksytytTilat.contains(h.getHakemus().getTila())
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

    private Set<HakukohdeWrapper> uudelleenSijoiteltavatHakukohteet(ArrayList<HakemusWrapper> muuttuneetHakemukset) {
        return muuttuneetHakemukset.stream()
                .map(h -> h.getValintatapajono().getHakukohdeWrapper())
                .collect(Collectors.toSet());
    }

    private ArrayList<HakemusWrapper> eiKorvattavissaOlevatHyvaksytytHakemukset(ValintatapajonoWrapper valintatapajono) {
        ArrayList<HakemusWrapper> eiKorvattavissaOlevatHyvaksytytHakemukset = new ArrayList<HakemusWrapper>();
        boolean taytetaankoPoissaOlevat = false;
        if(valintatapajono.getValintatapajono().getPoissaOlevaTaytto() != null && valintatapajono.getValintatapajono().getPoissaOlevaTaytto()) {
            taytetaankoPoissaOlevat = true;
        }
        for (HakemusWrapper h : valintatapajono.getHakemukset()) {

            boolean korvattavissa = false;
            IlmoittautumisTila tila = h.getHakemus().getIlmoittautumisTila();

            if(poissaoloTilat.contains(tila) && taytetaankoPoissaOlevat) {
               korvattavissa = true;
            }

            if (hyvaksytytTilat.contains(h.getHakemus().getTila()) && !korvattavissa) {
                if (!voidaanKorvata(h)) {
                    eiKorvattavissaOlevatHyvaksytytHakemukset.add(h);
                }
            }
        }

        return eiKorvattavissaOlevatHyvaksytytHakemukset;
    }

    private boolean voidaanKorvata(HakemusWrapper hakemusWrapper) {

        boolean voidaanKorvata = true;

//        ArrayList<HakijaryhmaWrapper> hakijanHakijaryhmat = new ArrayList<HakijaryhmaWrapper>();
//        for (HakijaryhmaWrapper hakijaryhmaWrapper : hakemusWrapper.getValintatapajono().getHakukohdeWrapper().getHakijaryhmaWrappers()) {
//            Hakijaryhma hakijaryhma = hakijaryhmaWrapper.getHakijaryhma();
//            if(hakijaryhma.getValintatapajonoOid() == null || hakijaryhma.getValintatapajonoOid().equals(hakemusWrapper.getValintatapajono().getValintatapajono().getOid())) {
//                if (hakijaryhmaWrapper.getHenkiloWrappers().contains(hakemusWrapper.getHenkilo())) {
//                    hakijanHakijaryhmat.add(hakijaryhmaWrapper);
//                }
//            }
//        }
//        for (HakijaryhmaWrapper a : hakijanHakijaryhmat) {
//            if (hakijaryhmassaVajaata(a) >= 0) {
//                voidaanKorvata = false;
//            }
//        }

        if(hakemusWrapper.isHyvaksyttyHakijaryhmastaTaiTayttoJonosta()) {
            voidaanKorvata = false;
        }

        if (!hakemusWrapper.isTilaVoidaanVaihtaa()) {
            voidaanKorvata = false;
        }

        // Hakijaryhmän tasasijasäännön jälkeen vielä hyväksytään
        if (hakemusWrapper.isHyvaksyttavissaHakijaryhmanJalkeen()) {
            voidaanKorvata = false;
            hakemusWrapper.setHyvaksyttavissaHakijaryhmanJalkeen(false);
        }

        return voidaanKorvata;
    }

    private HashSet<HakemusWrapper> asetaVaralleHakemus(HakemusWrapper varalleAsetettavaHakemusWrapper) {
        HashSet<HakemusWrapper> uudelleenSijoiteltavatHakukohteet = new HashSet<HakemusWrapper>();

        if(varalleAsetettavaHakemusWrapper.isTilaVoidaanVaihtaa()) {
            if(hyvaksytytTilat.contains(varalleAsetettavaHakemusWrapper.getHakemus().getTila())) {
                for (HakemusWrapper hakemusWrapper : varalleAsetettavaHakemusWrapper.getHenkilo().getHakemukset()) {
                    if(hakemusWrapper.isTilaVoidaanVaihtaa()) {
                        if (!hylatytTilat.contains(hakemusWrapper.getHakemus().getTila())) {
                            hakemusWrapper.getHakemus().setTila(HakemuksenTila.VARALLA);
                            uudelleenSijoiteltavatHakukohteet.add(hakemusWrapper);
                        }
                    }
                }
            } else {
                if (!hylatytTilat.contains(varalleAsetettavaHakemusWrapper.getHakemus().getTila())) {
                    varalleAsetettavaHakemusWrapper.getHakemus().setTila(HakemuksenTila.VARALLA);
                }
            }
        }
        return uudelleenSijoiteltavatHakukohteet;
    }

    private HashSet<HakemusWrapper> hyvaksyHakemus(HakemusWrapper hakemus) {
        HashSet<HakemusWrapper> uudelleenSijoiteltavatHakukohteet = new HashSet<HakemusWrapper>();
        if(hakemus.isTilaVoidaanVaihtaa()) {
            if(varaTilat.contains(hakemus.getHakemus().getEdellinenTila())) {
                hakemus.getHakemus().setTila(HakemuksenTila.VARASIJALTA_HYVAKSYTTY);
                hakemus.getHakemus().setTilanKuvaukset(TilanKuvaukset.varasijaltaHyvaksytty());
            } else {
                hakemus.getHakemus().setTila(HakemuksenTila.HYVAKSYTTY);
            }

            for (HakemusWrapper h : hakemus.getHenkilo().getHakemukset()) {
                // Alemmat toiveet
                if (h != hakemus && hakemus.getHakemus().getPrioriteetti() < h.getHakemus().getPrioriteetti()) {
                    if(h.isTilaVoidaanVaihtaa()) {
                        if (hyvaksytytTilat.contains(h.getHakemus().getTila())) {
                            h.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
                            h.getHakemus().setTilanKuvaukset(TilanKuvaukset.peruuntunutYlempiToive());
                            uudelleenSijoiteltavatHakukohteet.add(h);
                        } else {
                            if (!hylatytTilat.contains(h.getHakemus().getTila())) {
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
                            if (!hylatytTilat.contains(h.getHakemus().getTila())) {
                                h.getHakemus().setTilanKuvaukset(TilanKuvaukset.peruuntunutHyvaksyttyToisessaJonossa());
                                h.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
                            }
                        } else {
                            // Hakemukselle merkattu, että tilaa ei voi vaihtaa, mutta vaihdetaan kuitenkin jos hyväksytty
                            HakemuksenTila vanhaTila = h.getHakemus().getTila();
                            if(hyvaksytytTilat.contains(vanhaTila)) {
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

        boolean hakemuksenTila = !hylatytTilat.contains(hakemus.getTila()); //hakemus.getTila() != HakemuksenTila.HYLATTY && hakemus.getTila() != HakemuksenTila.PERUUTETTU && hakemus.getTila() != HakemuksenTila.PERUNUT;

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
                .stream().filter(h->h != hakemusWrapper)
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
        for (HakemusWrapper h : henkilo.getHakemukset()) {
            if (hyvaksytytTilat.contains(h.getHakemus().getTila())
                    && (h.getHakemus().getPrioriteetti() < hakemusWrapper.getHakemus().getPrioriteetti() || (h.getHakemus().getPrioriteetti().equals(hakemusWrapper.getHakemus().getPrioriteetti()) && h.getValintatapajono().getValintatapajono().getPrioriteetti() < hakemusWrapper.getValintatapajono().getValintatapajono().getPrioriteetti()))
                    && hakemusWrapper != h) {
                return false;
            }
        }
        return true;
    }

    private HakemusWrapper haeHuonoinValittuEiVajaaseenRyhmaanKuuluva(ValintatapajonoWrapper valintatapajonoWrapper) {
        HakemusWrapperComparator comparator = new HakemusWrapperComparator();
        Optional<HakemusWrapper> huonoinHakemus = valintatapajonoWrapper.getHakemukset()
                .stream()
                .sorted((h1, h2) -> comparator.compare(h2, h1))
                .filter(h -> hyvaksytytTilat.contains(h.getHakemus().getTila()) && voidaanKorvata(h))
                .findFirst();
        if(huonoinHakemus.isPresent()) {
            return huonoinHakemus.get();
        } else {
            return null;
        }

    }

    private List<HakemusWrapper> huonoimmatTasasijaiset(ValintatapajonoWrapper valintatapajonoWrapper) {
        HakemusWrapperComparator comparator = new HakemusWrapperComparator();
        List<HakemusWrapper> sortattu = valintatapajonoWrapper.getHakemukset()
                .stream()
                .sorted((h1, h2) -> comparator.compare(h2, h1))
                .collect(Collectors.toList());


        Optional<HakemusWrapper> huonoinHakemus = sortattu.stream()
                .filter(h-> hyvaksytytTilat.contains(h.getHakemus().getTila()) && voidaanKorvata(h))
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
                if (hyvaksytytTilat.contains(h.getHakemus().getTila()) && hakijaryhmaWrapper.getHenkiloWrappers().contains(h.getHenkilo())) {
                    needed--;
                }
            }
        }
        return needed;
    }


    private boolean onkoTasasijaTilanne(int tilaa, int valituksiHaluavat, Tasasijasaanto saanto) {
        if(tilaa == 0) {
            return false;
        }
        if(tilaa - (valituksiHaluavat - 1) <= 0) {
            if(saanto == Tasasijasaanto.ALITAYTTO || saanto == Tasasijasaanto.YLITAYTTO) {
                return true;
            }
        }
        return false;
    }

}
