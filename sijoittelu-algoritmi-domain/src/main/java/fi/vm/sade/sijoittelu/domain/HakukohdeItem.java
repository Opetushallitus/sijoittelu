package fi.vm.sade.sijoittelu.domain;

import java.io.Serializable;

public class HakukohdeItem implements Serializable {

    //hakukohdeoid
    private String oid;

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }
}
