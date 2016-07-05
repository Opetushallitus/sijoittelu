package fi.vm.sade.sijoittelu.laskenta.actors.messages;

public class PoistaVanhatAjotSijoittelulta {
    private final long sijoitteluId;

    private final int ajojenMaaraMax;
    private final String hakuOid;

    public PoistaVanhatAjotSijoittelulta(long sijoitteluId, int ajojenMaaraMax, String hakuOid) {
        this.sijoitteluId = sijoitteluId;
        this.ajojenMaaraMax = ajojenMaaraMax;
        this.hakuOid = hakuOid;
    }

    public long getSijoitteluId() {
        return sijoitteluId;
    }

    public int getAjojenMaaraMax() {
        return ajojenMaaraMax;
    }

    public String getHakuOid() {
        return hakuOid;
    }
}
