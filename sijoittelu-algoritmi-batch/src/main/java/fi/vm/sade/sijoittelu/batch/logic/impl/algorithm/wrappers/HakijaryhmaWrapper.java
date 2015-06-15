package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers;

import fi.vm.sade.sijoittelu.domain.Hakijaryhma;

import java.util.ArrayList;
import java.util.List;

public class HakijaryhmaWrapper {

    private Hakijaryhma hakijaryhma;

    private HakukohdeWrapper hakukohdeWrapper;

    private List<HenkiloWrapper> henkiloWrappers = new ArrayList<HenkiloWrapper>();

    private boolean alitayttoSaantoTaytetty = false;

    public Hakijaryhma getHakijaryhma() {
        return hakijaryhma;
    }

    public void setHakijaryhma(Hakijaryhma hakijaryhma) {
        this.hakijaryhma = hakijaryhma;
    }

    public HakukohdeWrapper getHakukohdeWrapper() {
        return hakukohdeWrapper;
    }

    public void setHakukohdeWrapper(HakukohdeWrapper hakukohdeWrapper) {
        this.hakukohdeWrapper = hakukohdeWrapper;
    }

    public List<HenkiloWrapper> getHenkiloWrappers() {
        return henkiloWrappers;
    }

    public boolean isAlitayttoSaantoTaytetty() {
        return alitayttoSaantoTaytetty;
    }

    public void setAlitayttoSaantoTaytetty(boolean alitayttoSaantoTaytetty) {
        this.alitayttoSaantoTaytetty = alitayttoSaantoTaytetty;
    }
}
