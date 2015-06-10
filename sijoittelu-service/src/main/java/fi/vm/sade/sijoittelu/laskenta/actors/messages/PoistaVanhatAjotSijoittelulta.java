package fi.vm.sade.sijoittelu.laskenta.actors.messages;

public class PoistaVanhatAjotSijoittelulta {
    private final long sijoitteluId;

    private final int ajojenMaaraMax;

    public PoistaVanhatAjotSijoittelulta(long sijoitteluId, int ajojenMaaraMax) {
        this.sijoitteluId = sijoitteluId;
        this.ajojenMaaraMax = ajojenMaaraMax;
    }

    public long getSijoitteluId() {
        return sijoitteluId;
    }

    public int getAjojenMaaraMax() {
        return ajojenMaaraMax;
    }
}
