package fi.vm.sade.sijoittelu.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ValiSijoittelu implements Serializable {

    private String hakuOid;

    private Long sijoitteluId;

    private Date created;

    /**
     * Maarittaa sijoitellaanko taman sijoittelun sijoitteluajot
     */
    private boolean sijoittele = true;

    private List<Valintatulos> valintatulokset = new ArrayList<Valintatulos>();

    private List<SijoitteluAjo> sijoitteluajot = new ArrayList<SijoitteluAjo>();

    public SijoitteluAjo getLatestSijoitteluajo() {
        SijoitteluAjo latest = null;
        for(SijoitteluAjo sijoitteluAjo : sijoitteluajot) {
            if(latest == null || latest.getEndMils() == null || (sijoitteluAjo.getEndMils() != null && latest.getEndMils().compareTo(sijoitteluAjo.getEndMils()) < 0)) {
                latest = sijoitteluAjo;
            }
        }
        return latest;
    }

    public List<SijoitteluAjo> getSijoitteluajot() {
        return sijoitteluajot;
    }

    public Long getSijoitteluId() {
        return sijoitteluId;
    }

    public void setSijoitteluId(Long sijoitteluId) {
        this.sijoitteluId = sijoitteluId;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public boolean isSijoittele() {
        return sijoittele;
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

    public List<Valintatulos> getValintatulokset() {
        return valintatulokset;
    }

    public void setValintatulokset(List<Valintatulos> valintatulokset) {
        this.valintatulokset = valintatulokset;
    }
}
