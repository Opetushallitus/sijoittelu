package fi.vm.sade.sijoittelu.laskenta.actors.messages;

/**
 * Created by kjsaila on 24/09/14.
 */
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
