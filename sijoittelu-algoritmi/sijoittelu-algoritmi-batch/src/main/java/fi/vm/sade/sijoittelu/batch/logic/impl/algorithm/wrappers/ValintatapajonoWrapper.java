package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers;

import java.util.ArrayList;
import java.util.List;

import fi.vm.sade.sijoittelu.domain.Valintatapajono;

/**
 * 
 * @author Kari Kammonen
 * 
 */
public class ValintatapajonoWrapper {

    private Valintatapajono valintatapajono;

    private HakukohdeWrapper hakukohdeWrapper;

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

}
