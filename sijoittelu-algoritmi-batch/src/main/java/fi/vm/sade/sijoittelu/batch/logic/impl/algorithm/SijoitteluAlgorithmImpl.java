package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import fi.vm.sade.service.valintaperusteet.dto.model.Kieli;
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

        final int rekursio = n;

        // Tayttöjonot
        ArrayList<HakemusWrapper> muuttuneetHakemukset = new ArrayList<>();
        hakukohde.getValintatapajonot().forEach(valintatapajono -> {
            int aloituspaikat = valintatapajono.getValintatapajono().getAloituspaikat();
            List<HakemuksenTila> tilat = Arrays.asList(HakemuksenTila.HYVAKSYTTY, HakemuksenTila.VARASIJALTA_HYVAKSYTTY);
            int hyvaksytyt = valintatapajono.getHakemukset().stream().filter(h->tilat.contains(h.getHakemus().getTila())).collect(Collectors.toList()).size();
            int tilaa = aloituspaikat - hyvaksytyt;
            String tayttojono = valintatapajono.getValintatapajono().getTayttojono();

            LocalDateTime varasijaTayttoPaattyy = varasijaTayttoPaattyy(valintatapajono);

            if(sijoitteluAjo.varasijaSaannotVoimassa()
                    && sijoitteluAjo.getToday().isBefore(varasijaTayttoPaattyy)
                    && tilaa > 0 && tayttojono != null && !tayttojono.isEmpty()
                    && !tayttojono.equals(valintatapajono.getValintatapajono().getOid())) {
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
            sijoittele(v, rekursio);
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
        boolean tasaSijaTilanne = false;
        boolean tasasijaTilanneRatkaistu = false;

        LocalDateTime varasijaTayttoPaattyy = varasijaTayttoPaattyy(valintatapajono);

        ListIterator<HakemusWrapper> it = valintatapajono.getHakemukset().listIterator();
        while (it.hasNext()) {
            ArrayList<HakemusWrapper> kaikkiTasasijaHakemukset = tasasijaHakemukset(it);
            ArrayList<HakemusWrapper> valituksiHaluavatHakemukset = valituksiHaluavatHakemukset(kaikkiTasasijaHakemukset);

            if (tilaa - valituksiHaluavatHakemukset.size() <= 0) {
                // vain ylitaytto tai alitaytto vaatii erillista
                // tasasijanhallintaa
                if (saanto == Tasasijasaanto.YLITAYTTO || saanto == Tasasijasaanto.ALITAYTTO) {
                    tasaSijaTilanne = true;
                }
            }

            for (HakemusWrapper hk : kaikkiTasasijaHakemukset) {
                if (eiKorvattavissaOlevatHyvaksytytHakemukset.contains(hk)) {
                    // Ei tehdä mitään
                }
                else if(sijoitteluAjo.hakukierrosOnPaattynyt() || sijoitteluAjo.getToday().isAfter(varasijaTayttoPaattyy)) {
                    // Hakukierros on päättynyt tai käsiteltävän jonon varasijasäännöt eivät ole enää voimassa.
                    // Asetetaan kaikki hakemukset joiden tila voidaan vaihtaa tilaan peruuntunut
                    hk.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
                    hk.getHakemus().setTilanKuvaukset(TilanKuvaukset.peruuntunutHakukierrosOnPaattynyt());
                    hk.setTilaVoidaanVaihtaa(false);
                } else if (valituksiHaluavatHakemukset.contains(hk)) {
                    if (valintatapajono.getValintatapajono().getKaikkiEhdonTayttavatHyvaksytaan() != null
                            && valintatapajono.getValintatapajono().getKaikkiEhdonTayttavatHyvaksytaan()) {
                        hyvaksyttavaksi.add(hk);
                    } else if (tasaSijaTilanne && !tasasijaTilanneRatkaistu) {
                        if (saanto == Tasasijasaanto.ALITAYTTO) {
                            if(tilaa == valituksiHaluavatHakemukset.size())
                                hyvaksyttavaksi.add(hk);
                            else
                                varalle.add(hk);
                        } else if (saanto == Tasasijasaanto.YLITAYTTO) {
                            if(tilaa == 0 && valituksiHaluavatHakemukset.size() == 1)
                                varalle.add(hk);
                            else
                                hyvaksyttavaksi.add(hk);
                        }
                    } else if (tasasijaTilanneRatkaistu) {
                        varalle.add(hk);
                    } else if (tilaa > 0) {
                        hyvaksyttavaksi.add(hk);
                        tilaa--;
                    } else {
                        varalle.add(hk);
                    }
                } else {
                    varalle.add(hk);
                }
            }
            if (tasaSijaTilanne) {
                tasasijaTilanneRatkaistu = true;
            }

        }

        // kay lavitse muutokset ja tarkista tarvitaanko rekursiota?
        ArrayList<HakemusWrapper> muuttuneetHakemukset = new ArrayList<HakemusWrapper>();
        for (HakemusWrapper hakemusWrapper : hyvaksyttavaksi) {
            if (hakemusWrapper.getHakemus().getTila() != HakemuksenTila.HYVAKSYTTY && hakemusWrapper.getHakemus().getTila() != HakemuksenTila.VARASIJALTA_HYVAKSYTTY) {
                muuttuneetHakemukset.addAll(hyvaksyHakemus(hakemusWrapper));
            }
        }
        for (HakemusWrapper hakemusWrapper : varalle) {
            if (hakemusWrapper.getHakemus().getTila() == HakemuksenTila.HYVAKSYTTY || hakemusWrapper.getHakemus().getTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY) {
                muuttuneetHakemukset.addAll(asetaVaralleHakemus(hakemusWrapper));
            }
        }


        for (HakukohdeWrapper v : uudelleenSijoiteltavatHakukohteet(muuttuneetHakemukset)) {
            sijoittele(v, n);
        }

    }

    private void sijoittele(HakijaryhmaWrapper hakijaryhmaWrapper, int n) {
        ArrayList<HakemusWrapper> muuttuneetHakemukset = new ArrayList<HakemusWrapper>();
        List<List<HakemusWrapper>> hakijaryhmanVarasijallaOlevat = hakijaryhmanVarasijajarjestys(hakijaryhmaWrapper);
        ListIterator<List<HakemusWrapper>> it = hakijaryhmanVarasijallaOlevat.listIterator();
        Hakijaryhma ryhma = hakijaryhmaWrapper.getHakijaryhma();


        while (it.hasNext() && hakijaryhmassaVajaata(hakijaryhmaWrapper) > 0) {
            List<HakemusWrapper> tasasijaVarasijaHakemukset = it.next();


            for (HakemusWrapper paras : tasasijaVarasijaHakemukset) {
                ValintatapajonoWrapper v = paras.getValintatapajono();
                Tasasijasaanto saanto = v.getValintatapajono().getTasasijasaanto();



                if(ryhma.getValintatapajonoOid() == null || ryhma.getValintatapajonoOid().equals(v.getValintatapajono().getOid())) {
                    List<HakemuksenTila> tilat = Arrays.asList(HakemuksenTila.HYVAKSYTTY, HakemuksenTila.VARASIJALTA_HYVAKSYTTY);
                    int hyvaksytyt = v.getHakemukset().stream().filter(h->tilat.contains(h.getHakemus().getTila())).collect(Collectors.toList()).size();
                    int tilaa = v.getValintatapajono().getAloituspaikat() - hyvaksytyt;
                    boolean tasaSijaTilanne = false;
                    if(tilaa <= 0) {
                        if (saanto == Tasasijasaanto.YLITAYTTO || saanto == Tasasijasaanto.ALITAYTTO) {
                            tasaSijaTilanne = true;
                        }
                    }
                    if (tasaSijaTilanne) {
                        if (saanto == Tasasijasaanto.YLITAYTTO) {
                            muuttuneetHakemukset.addAll(hyvaksyHakemus(paras));
                            muuttuneetHakemukset.add(paras);
                            // TODO kierrä tämä jotenkin
                            paras.setTilaVoidaanVaihtaa(false);
                        }
                    } else {
                        HakemusWrapper huonoin = haeHuonoinValittuEiVajaaseenRyhmaanKuuluva(v, hakijaryhmaWrapper);
                        muuttuneetHakemukset.addAll(hyvaksyHakemus(paras));
                        muuttuneetHakemukset.add(paras);
                        if (huonoin != null) {
                            muuttuneetHakemukset.addAll(asetaVaralleHakemus(huonoin));
                        }
                    }

                }
            }
        }
        HashSet<HakukohdeWrapper> uudelleenSijoiteltavatHakukohteet = uudelleenSijoiteltavatHakukohteet(muuttuneetHakemukset);
        for (HakukohdeWrapper h : uudelleenSijoiteltavatHakukohteet) {
            sijoittele(h, n);
        }

    }

    private ArrayList<HakemusWrapper> valituksiHaluavatHakemukset(ArrayList<HakemusWrapper> hakemukset) {
        ArrayList<HakemusWrapper> tasasijaHakemukset = new ArrayList<HakemusWrapper>();
        for (HakemusWrapper hakemusWrapper : hakemukset) {
            if (hakijaHaluaa(hakemusWrapper) && saannotSallii(hakemusWrapper)) {
                tasasijaHakemukset.add(hakemusWrapper);
            }
        }
        return tasasijaHakemukset;
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
        List<HakemusWrapper> kuuluvat = new ArrayList<HakemusWrapper>();
        for (HakemusWrapper hakemusWrapper : hakemus) {
            if (ryhma.getHenkiloWrappers().contains(hakemusWrapper.getHenkilo())) {
                kuuluvat.add(hakemusWrapper);
            }
        }
        return kuuluvat;
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
                    ArrayList<HakemusWrapper> valituksiHaluavatHakemukset = valituksiHaluavatHakemukset(kaikkiTasasijaHakemukset);

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
        List<HakemusWrapper> list = new ArrayList<HakemusWrapper>();
        for (HakemusWrapper hakemusWrapper : hakemukset) {
            if (hakemusWrapper.getHakemus().getTila() != HakemuksenTila.HYVAKSYTTY
                    && hakemusWrapper.getHakemus().getTila() != HakemuksenTila.VARASIJALTA_HYVAKSYTTY
                    && hakijaHaluaa(hakemusWrapper)
                    && saannotSallii(hakemusWrapper)) {
                list.add(hakemusWrapper);
            }
        }
        return list;
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

    private HashSet<HakukohdeWrapper> uudelleenSijoiteltavatHakukohteet(ArrayList<HakemusWrapper> muuttuneetHakemukset) {
        HashSet<HakukohdeWrapper> hakukohteet = new HashSet<HakukohdeWrapper>();
        for (HakemusWrapper h : muuttuneetHakemukset) {
            hakukohteet.add(h.getValintatapajono().getHakukohdeWrapper());
        }
        return hakukohteet;
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

            if(tila != null && (tila.equals(IlmoittautumisTila.POISSA_SYKSY) || tila.equals(IlmoittautumisTila.POISSA_KOKO_LUKUVUOSI) || tila.equals(IlmoittautumisTila.POISSA)) && taytetaankoPoissaOlevat) {
               korvattavissa = true;
            }

            if ((h.getHakemus().getTila() == HakemuksenTila.HYVAKSYTTY || h.getHakemus().getTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY) && !korvattavissa) {
                if (!voidaanKorvata(h)) {
                    eiKorvattavissaOlevatHyvaksytytHakemukset.add(h);
                }
            }
        }

        return eiKorvattavissaOlevatHyvaksytytHakemukset;
    }

    private boolean voidaanKorvata(HakemusWrapper hakemusWrapper) {

        boolean voidaanKorvata = true;

        ArrayList<HakijaryhmaWrapper> hakijanHakijaryhmat = new ArrayList<HakijaryhmaWrapper>();
        for (HakijaryhmaWrapper hakijaryhmaWrapper : hakemusWrapper.getValintatapajono().getHakukohdeWrapper().getHakijaryhmaWrappers()) {
            Hakijaryhma hakijaryhma = hakijaryhmaWrapper.getHakijaryhma();
            if(hakijaryhma.getValintatapajonoOid() == null || hakijaryhma.getValintatapajonoOid().equals(hakemusWrapper.getValintatapajono().getValintatapajono().getOid())) {
                if (hakijaryhmaWrapper.getHenkiloWrappers().contains(hakemusWrapper.getHenkilo())) {
                    hakijanHakijaryhmat.add(hakijaryhmaWrapper);
                }
            }
        }
        for (HakijaryhmaWrapper a : hakijanHakijaryhmat) {
            if (hakijaryhmassaVajaata(a) >= 0) {
                voidaanKorvata = false;
            }
        }

        if (!hakemusWrapper.isTilaVoidaanVaihtaa()) {
            voidaanKorvata = false;
        }

        return voidaanKorvata;
    }

    private HashSet<HakemusWrapper> asetaVaralleHakemus(HakemusWrapper varalleAsetettavaHakemusWrapper) {
        HashSet<HakemusWrapper> uudelleenSijoiteltavatHakukohteet = new HashSet<HakemusWrapper>();

        if(varalleAsetettavaHakemusWrapper.isTilaVoidaanVaihtaa()) {
            if(varalleAsetettavaHakemusWrapper.getHakemus().getTila() == HakemuksenTila.HYVAKSYTTY || varalleAsetettavaHakemusWrapper.getHakemus().getTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY) {
                for (HakemusWrapper hakemusWrapper : varalleAsetettavaHakemusWrapper.getHenkilo().getHakemukset()) {
                    if(hakemusWrapper.isTilaVoidaanVaihtaa()) {
                        if (hakemusWrapper.getHakemus().getTila() != HakemuksenTila.HYLATTY
                                && hakemusWrapper.getHakemus().getTila() != HakemuksenTila.PERUUTETTU
                                && hakemusWrapper.getHakemus().getTila() != HakemuksenTila.PERUNUT ) {
                            hakemusWrapper.getHakemus().setTila(HakemuksenTila.VARALLA);
                            uudelleenSijoiteltavatHakukohteet.add(hakemusWrapper);
                        }
                    }
                }
            } else {
                if (varalleAsetettavaHakemusWrapper.getHakemus().getTila() != HakemuksenTila.HYLATTY
                        && varalleAsetettavaHakemusWrapper.getHakemus().getTila() != HakemuksenTila.PERUUTETTU
                        && varalleAsetettavaHakemusWrapper.getHakemus().getTila() != HakemuksenTila.PERUNUT) {
                    if(varalleAsetettavaHakemusWrapper.getHakemus().getTila() != HakemuksenTila.VARALLA) {
                        String lokitus = varalleAsetettavaHakemusWrapper.getValintatapajono().getHakukohdeWrapper().getHakukohde().getOid() + " - " + varalleAsetettavaHakemusWrapper.getHakemus().getHakemusOid() + " - " + varalleAsetettavaHakemusWrapper.getHakemus().getTilanKuvaukset().getOrDefault("FI", varalleAsetettavaHakemusWrapper.getHakemus().getTila().name());
                        sijoitteluAjo.getVarasijapomput().add(lokitus);
                    }
                    varalleAsetettavaHakemusWrapper.getHakemus().setTila(HakemuksenTila.VARALLA);

                }
            }
        }
        return uudelleenSijoiteltavatHakukohteet;
    }

    private HashSet<HakemusWrapper> hyvaksyHakemus(HakemusWrapper hakemus) {
        HashSet<HakemusWrapper> uudelleenSijoiteltavatHakukohteet = new HashSet<HakemusWrapper>();
        if(hakemus.isTilaVoidaanVaihtaa()) {
            if(hakemus.getHakemus().getEdellinenTila() == HakemuksenTila.VARALLA || hakemus.getHakemus().getEdellinenTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY) {
                hakemus.getHakemus().setTila(HakemuksenTila.VARASIJALTA_HYVAKSYTTY);
                hakemus.getHakemus().setTilanKuvaukset(TilanKuvaukset.varasijaltaHyvaksytty());
            } else {
                hakemus.getHakemus().setTila(HakemuksenTila.HYVAKSYTTY);
            }

            for (HakemusWrapper h : hakemus.getHenkilo().getHakemukset()) {
                // Alemmat toiveet
                if (h != hakemus && hakemus.getHakemus().getPrioriteetti() < h.getHakemus().getPrioriteetti()) {
                    if(h.isTilaVoidaanVaihtaa()) {
                        if (h.getHakemus().getTila() == HakemuksenTila.HYVAKSYTTY
                                || h.getHakemus().getTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY) {
                            h.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
                            h.getHakemus().setTilanKuvaukset(TilanKuvaukset.peruuntunutYlempiToive());
                            uudelleenSijoiteltavatHakukohteet.add(h);
                        } else {
                            if (h.getHakemus().getTila() != HakemuksenTila.HYLATTY
                                    && h.getHakemus().getTila() != HakemuksenTila.PERUUTETTU
                                    && h.getHakemus().getTila() != HakemuksenTila.PERUNUT) {
                                //h.getHakemus().setTilanKuvaukset(TilanKuvaukset.peruuntunutAloituspaikatTaynna());
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
                            if (h.getHakemus().getTila() != HakemuksenTila.HYLATTY
                                    && h.getHakemus().getTila() != HakemuksenTila.PERUUTETTU
                                    && h.getHakemus().getTila() != HakemuksenTila.PERUNUT) {
                                h.getHakemus().setTilanKuvaukset(TilanKuvaukset.peruuntunutHyvaksyttyToisessaJonossa());
                                h.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
                            }
                        } else {
                            // Hakemukselle merkattu, että tilaa ei voi vaihtaa, mutta vaihdetaan kuitenkin jos hyväksytty
                            HakemuksenTila vanhaTila = h.getHakemus().getTila();
                            if(vanhaTila == HakemuksenTila.HYVAKSYTTY || vanhaTila == HakemuksenTila.VARASIJALTA_HYVAKSYTTY) {
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

        boolean hakemuksenTila = hakemus.getTila() != HakemuksenTila.HYLATTY && hakemus.getTila() != HakemuksenTila.PERUUTETTU && hakemus.getTila() != HakemuksenTila.PERUNUT;

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
        HenkiloWrapper henkilo = hakemusWrapper.getHenkilo();
        for (HakemusWrapper h : henkilo.getHakemukset()) {
            if (HakemuksenTila.PERUNUT.equals(h.getHakemus().getTila())
                    && h.getHakemus().getPrioriteetti() <= hakemusWrapper.getHakemus().getPrioriteetti()
                    && hakemusWrapper != h) {
                return false;
            }
        }
        return true;
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
            if ((HakemuksenTila.HYVAKSYTTY.equals(h.getHakemus().getTila()) || HakemuksenTila.VARASIJALTA_HYVAKSYTTY.equals(h.getHakemus().getTila()))
                    && (h.getHakemus().getPrioriteetti() < hakemusWrapper.getHakemus().getPrioriteetti() || (h.getHakemus().getPrioriteetti().equals(hakemusWrapper.getHakemus().getPrioriteetti()) && h.getValintatapajono().getValintatapajono().getPrioriteetti() < hakemusWrapper.getValintatapajono().getValintatapajono().getPrioriteetti()))
                    && hakemusWrapper != h) {
                return false;
            }
        }
        return true;
    }

    private HakemusWrapper haeHuonoinValittuEiVajaaseenRyhmaanKuuluva(ValintatapajonoWrapper valintatapajonoWrapper, HakijaryhmaWrapper hakijaryhmaWrapper) {
        HakemusWrapper huonoinHakemus = null;
        for (int i = valintatapajonoWrapper.getHakemukset().size() - 1; i >= 0; i--) {
            HakemusWrapper h = valintatapajonoWrapper.getHakemukset().get(i);
            boolean kuuluuRyhmaan = hakijaryhmaWrapper.getHakijaryhma().getHakemusOid().contains(h.getHakemus().getHakemusOid());
            if ((h.getHakemus().getTila() == HakemuksenTila.HYVAKSYTTY || h.getHakemus().getTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY) && kuuluuRyhmaan) {
                if (voidaanKorvata(h)) {
                    huonoinHakemus = h;
                    break;
                }
            }
        }

        return huonoinHakemus;
    }

    private int hakijaryhmassaVajaata(HakijaryhmaWrapper hakijaryhmaWrapper) {
        int needed = hakijaryhmaWrapper.getHakijaryhma().getKiintio();
        for (ValintatapajonoWrapper valintatapajonoWrapper : hakijaryhmaWrapper.getHakukohdeWrapper().getValintatapajonot()) {
            for (HakemusWrapper h : valintatapajonoWrapper.getHakemukset()) {
                if ((h.getHakemus().getTila() == HakemuksenTila.HYVAKSYTTY || h.getHakemus().getTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY) && hakijaryhmaWrapper.getHenkiloWrappers().contains(h.getHenkilo())) {
                    needed--;
                }
            }
        }
        return needed;
    }

}
