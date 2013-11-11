package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers;

import fi.vm.sade.sijoittelu.domain.Hakukohde;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Kari Kammonen
 * 
 */
public class HakukohdeWrapper {

    private Hakukohde hakukohde;

    private SijoitteluajoWrapper sijoitteluajoWrapper;

    private List<HakijaryhmaWrapper> hakijaryhmaWrappers = new ArrayList<HakijaryhmaWrapper>();

    private List<ValintatapajonoWrapper> valintatapajonot = new ArrayList<ValintatapajonoWrapper>();

    public List<ValintatapajonoWrapper> getValintatapajonot() {
        return valintatapajonot;
    }

    public void setValintatapajonot(List<ValintatapajonoWrapper> valintatapajonot) {
        this.valintatapajonot = valintatapajonot;
    }

    public Hakukohde getHakukohde() {
        return hakukohde;
    }

    public void setHakukohde(Hakukohde hakukohde) {
        this.hakukohde = hakukohde;
    }

    public SijoitteluajoWrapper getSijoitteluajoWrapper() {
        return sijoitteluajoWrapper;
    }

    public void setSijoitteluajoWrapper(SijoitteluajoWrapper sijoitteluajoWrapper) {
        this.sijoitteluajoWrapper = sijoitteluajoWrapper;
    }

    public List<HakijaryhmaWrapper> getHakijaryhmaWrappers() {
        return hakijaryhmaWrappers;
    }

}
