package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers;

import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.Valintatulos;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Kari Kammonen
 * 
 */
public class SijoitteluajoWrapper {

    private SijoitteluAjo sijoitteluajo;

    private List<HakukohdeWrapper> hakukohteet = new ArrayList<HakukohdeWrapper>();

    private List<Valintatulos> muuttuneetValintatulokset = new ArrayList<>();

    private final LocalDateTime today = LocalDateTime.now();

    private LocalDateTime kaikkiKohteetSijoittelussa = LocalDateTime.now().minusDays(1);

    private LocalDateTime varasijaSaannotAstuvatVoimaan = LocalDateTime.now().minusDays(1);

    private LocalDateTime hakuKierrosPaattyy = LocalDateTime.now().plusYears(100);

    private boolean isKKHaku = false;

    private List<String> varasijapomput = new ArrayList<>();

    public List<HakukohdeWrapper> getHakukohteet() {
        return hakukohteet;
    }


    public void setHakukohteet(List<HakukohdeWrapper> hakukohteet) {
        this.hakukohteet = hakukohteet;
    }

    public SijoitteluAjo getSijoitteluajo() {
        return sijoitteluajo;
    }

    public void setSijoitteluajo(SijoitteluAjo sijoitteluajo) {
        this.sijoitteluajo = sijoitteluajo;
    }

    public List<Valintatulos> getMuuttuneetValintatulokset() {
        return muuttuneetValintatulokset;
    }

    public void setMuuttuneetValintatulokset(List<Valintatulos> muuttuneetValintatulokset) {
        this.muuttuneetValintatulokset = muuttuneetValintatulokset;
    }

    public LocalDateTime getToday() {
        return today;
    }

    public LocalDateTime getKaikkiKohteetSijoittelussa() {
        return kaikkiKohteetSijoittelussa;
    }

    public void setKaikkiKohteetSijoittelussa(LocalDateTime kaikkiKohteetSijoittelussa) {
        this.kaikkiKohteetSijoittelussa = kaikkiKohteetSijoittelussa;
    }

    public boolean paivamaaraOhitettu() {
        return today.isAfter(kaikkiKohteetSijoittelussa);
    }

    public boolean varasijaSaannotVoimassa() {
        return today.isAfter(varasijaSaannotAstuvatVoimaan);
    }

    public boolean hakukierrosOnPaattynyt() {
        return today.isAfter(hakuKierrosPaattyy);
    }

    public boolean isKKHaku() {
        return isKKHaku;
    }

    public void setKKHaku(boolean isKKHaku) {
        this.isKKHaku = isKKHaku;
    }

    public LocalDateTime getVarasijaSaannotAstuvatVoimaan() {
        return varasijaSaannotAstuvatVoimaan;
    }

    public void setVarasijaSaannotAstuvatVoimaan(LocalDateTime varasijaSaannotAstuvatVoimaan) {
        this.varasijaSaannotAstuvatVoimaan = varasijaSaannotAstuvatVoimaan;
    }

    public LocalDateTime getHakuKierrosPaattyy() {
        return hakuKierrosPaattyy;
    }

    public void setHakuKierrosPaattyy(LocalDateTime hakuKierrosPaattyy) {
        this.hakuKierrosPaattyy = hakuKierrosPaattyy;
    }

    public List<String> getVarasijapomput() {
        return varasijapomput;
    }

    public void setVarasijapomput(List<String> varasijapomput) {
        this.varasijapomput = varasijapomput;
    }
}
