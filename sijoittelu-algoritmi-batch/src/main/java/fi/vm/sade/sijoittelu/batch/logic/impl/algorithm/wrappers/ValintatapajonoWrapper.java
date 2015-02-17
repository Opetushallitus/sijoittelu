package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers;

import fi.vm.sade.sijoittelu.domain.Valintatapajono;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Kari Kammonen
 * 
 */
public class ValintatapajonoWrapper {

    private Valintatapajono valintatapajono;

    private HakukohdeWrapper hakukohdeWrapper;

    // Yhden sijoittelukierroksen aikainen lukko, jos alitäyttösääntö on lauennut hakijaryhmäkäsittelyssä
    private boolean alitayttoLukko = false;

    private List<HakemusWrapper> hakemukset = new ArrayList<HakemusWrapper>();

    public void setHakemukset(List<HakemusWrapper> hakemukset) {
        this.hakemukset = hakemukset;
    }

    public List<HakemusWrapper> getHakemukset() {
        return hakemukset;
    }

    public Valintatapajono getValintatapajono() {
        return valintatapajono;
    }

    public void setValintatapajono(Valintatapajono valintatapajono) {
        this.valintatapajono = valintatapajono;
    }

    public HakukohdeWrapper getHakukohdeWrapper() {
        return hakukohdeWrapper;
    }

    public void setHakukohdeWrapper(HakukohdeWrapper hakukohdeWrapper) {
        this.hakukohdeWrapper = hakukohdeWrapper;
    }

    public boolean isAlitayttoLukko() {
        return alitayttoLukko;
    }

    public void setAlitayttoLukko(boolean alitayttoLukko) {
        this.alitayttoLukko = alitayttoLukko;
    }
}
