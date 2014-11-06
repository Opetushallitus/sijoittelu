package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers;

import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.Valintatulos;

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

}
