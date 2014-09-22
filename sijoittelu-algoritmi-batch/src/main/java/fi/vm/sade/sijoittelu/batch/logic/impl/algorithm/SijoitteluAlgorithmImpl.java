package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import fi.vm.sade.service.valintaperusteet.dto.model.Kieli;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor.PostSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.*;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.IlmoittautumisTila;
import fi.vm.sade.sijoittelu.domain.Tasasijasaanto;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;

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
                    // ei voida tehdä mitaan, tila on jo poistettu. ignoretetaan
                } else if (valituksiHaluavatHakemukset.contains(hk)) {
                    if (valintatapajono.getValintatapajono().getKaikkiEhdonTayttavatHyvaksytaan() != null
                            && valintatapajono.getValintatapajono().getKaikkiEhdonTayttavatHyvaksytaan()) {
                        hyvaksyttavaksi.add(hk);
                    } else if (tasaSijaTilanne && !tasasijaTilanneRatkaistu) {
                        if (saanto == Tasasijasaanto.ALITAYTTO) {
                            varalle.add(hk);
                        } else if (saanto == Tasasijasaanto.YLITAYTTO) {
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

        while (it.hasNext() && hakijaryhmassaVajaata(hakijaryhmaWrapper) > 0) {
            List<HakemusWrapper> tasasijaVarasijaHakemukset = it.next();
            for (HakemusWrapper paras : tasasijaVarasijaHakemukset) {
                ValintatapajonoWrapper v = paras.getValintatapajono();
                HakemusWrapper huonoin = haeHuonoinValittuEiVajaaseenRyhmaanKuuluva(v);
                muuttuneetHakemukset.addAll(hyvaksyHakemus(paras));
                paras.getHakemus().getTilanKuvaukset().put("FI","Varasijalta hyväksytty");
                paras.getHakemus().getTilanKuvaukset().put("SV","Godkänd från reservplats");
                paras.getHakemus().getTilanKuvaukset().put("EN","Accepted from a reserve place");
                muuttuneetHakemukset.add(paras);
                if (huonoin != null) {
                    muuttuneetHakemukset.addAll(asetaVaralleHakemus(huonoin));
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
            if (seuraava == null || seuraava.getHakemus().getJonosija() != h.getHakemus().getJonosija()) {
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
            if (hakijaryhmaWrapper.getHenkiloWrappers().contains(hakemusWrapper.getHenkilo())) {
                hakijanHakijaryhmat.add(hakijaryhmaWrapper);
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
                hakemus.getHakemus().getTilanKuvaukset().put("FI", "Varasijalta hyväksytty");
                hakemus.getHakemus().getTilanKuvaukset().put("SV", "Godkänd från reservplats");
                hakemus.getHakemus().getTilanKuvaukset().put("EN", "Accepted from a reserve place");
            } else {
                hakemus.getHakemus().setTila(HakemuksenTila.HYVAKSYTTY);
            }

            for (HakemusWrapper h : hakemus.getHenkilo().getHakemukset()) {
                if (h != hakemus && hakemus.getHakemus().getPrioriteetti() <= h.getHakemus().getPrioriteetti()) {
                    if(h.isTilaVoidaanVaihtaa()) {
                        if (h.getHakemus().getTila() == HakemuksenTila.HYVAKSYTTY || h.getHakemus().getTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY) {
                            h.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
                            h.getHakemus().getTilanKuvaukset().put("FI","Peruuntunut, hyväksytty ylemmälle hakutoiveelle");
                            h.getHakemus().getTilanKuvaukset().put("SV","Annullerad, godkänt till ansökningsmål med högre prioritet");
                            h.getHakemus().getTilanKuvaukset().put("EN","Cancelled, accepted for a study place with higher priority");
                            uudelleenSijoiteltavatHakukohteet.add(h);
                        } else {
                            if (h.getHakemus().getTila() != HakemuksenTila.HYLATTY
                                    && h.getHakemus().getTila() != HakemuksenTila.PERUUTETTU
                                    && h.getHakemus().getTila() != HakemuksenTila.PERUNUT) {
                                h.getHakemus().getTilanKuvaukset().put("FI","Peruuntunut, aloituspaikat täynnä");
                                h.getHakemus().getTilanKuvaukset().put("SV","Annullerad, nybörjarplatser fyllda");
                                h.getHakemus().getTilanKuvaukset().put("EN","Cancelled, study places are filled");
                                h.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
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
        if(hakemusWrapper.getValintatapajono().getValintatapajono().getEiVarasijatayttoa() != null && hakemusWrapper.getValintatapajono().getValintatapajono().getEiVarasijatayttoa()
                && !hakemusWrapper.getValintatapajono().getValintatapajono().getKaikkiEhdonTayttavatHyvaksytaan()) {
            hakijaAloistuspaikkojenSisallaTaiVarasijataytto  = hakijaAloistuspaikkojenSisalla(hakemusWrapper);
        }

        boolean eiPeruttuaKorkeampaaTaiSamaaHakutoivetta =  eiPeruttuaKorkeampaaTaiSamaaHakutoivetta(hakemusWrapper);

        return hakemuksenTila && hakijaAloistuspaikkojenSisallaTaiVarasijataytto && eiPeruttuaKorkeampaaTaiSamaaHakutoivetta;
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

    private boolean hakijaHaluaa(HakemusWrapper hakemusWrapper) {
        HenkiloWrapper henkilo = hakemusWrapper.getHenkilo();
        for (HakemusWrapper h : henkilo.getHakemukset()) {
            if ((HakemuksenTila.HYVAKSYTTY.equals(h.getHakemus().getTila()) || HakemuksenTila.VARASIJALTA_HYVAKSYTTY.equals(h.getHakemus().getTila())) && h.getHakemus().getPrioriteetti() <= hakemusWrapper.getHakemus().getPrioriteetti() && hakemusWrapper != h) {
                return false;
            }
        }
        return true;
    }

    private HakemusWrapper haeHuonoinValittuEiVajaaseenRyhmaanKuuluva(ValintatapajonoWrapper valintatapajonoWrapper) {
        HakemusWrapper huonoinHakemus = null;
        for (int i = valintatapajonoWrapper.getHakemukset().size() - 1; i >= 0; i--) {
            HakemusWrapper h = valintatapajonoWrapper.getHakemukset().get(i);
            if (h.getHakemus().getTila() == HakemuksenTila.HYVAKSYTTY || h.getHakemus().getTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY) {
                if (voidaanKorvata(h)) {
                    huonoinHakemus = h;
                    break;
                }
            }
        }

        return huonoinHakemus;
    }

    private int hakijaryhmassaVajaata(HakijaryhmaWrapper hakijaryhmaWrapper) {
        int needed = hakijaryhmaWrapper.getHakijaryhma().getPaikat();
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
