package fi.vm.sade.sijoittelu.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Deprecated
public class Sijoittelu implements Serializable {
    private String hakuOid;

    private Long sijoitteluId;

    private Date created;

    /**
     * Maarittaa sijoitellaanko taman sijoittelun sijoitteluajot
     */
    private boolean sijoittele = true;

    private List<SijoitteluAjo> sijoitteluajot = new ArrayList<SijoitteluAjo>();

    public List<SijoitteluAjo> getSijoitteluajot() {
        return sijoitteluajot;
    }

    public void setSijoitteluId(Long sijoitteluId) {
        this.sijoitteluId = sijoitteluId;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setSijoittele(boolean sijoittelu) {
        this.sijoittele = sijoittelu;
    }

    public String getHakuOid() {
        return hakuOid;
    }

    public void setHakuOid(String hakuOid) {
        this.hakuOid = hakuOid;
    }

}
