package fi.vm.sade.sijoittelu.domain;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Embedded;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Reference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Kari Kammonen
 */
@Entity("ValiSijoittelu")
public class ValiSijoittelu implements Serializable {

    @SuppressWarnings("unused")
    @Id
    private ObjectId id;

    private String hakuOid;

    private Long sijoitteluId;

    private Date created;

    /**
     * Maarittaa sijoitellaanko taman sijoittelun sijoitteluajot
     */
    private boolean sijoittele = true;

    //@Embedded
    // @JsonView(JsonViews.Basic.class)
    // private Haku haku;

    @Reference(value = "Valintatulos", lazy = true)
    private List<Valintatulos> valintatulokset = new ArrayList<Valintatulos>();

    // @Reference(value = "SijoitteluAjo", lazy = true)
    @Embedded
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

    //  public Haku getHaku() {
    //     return haku;
    // }

    // public void setHaku(Haku haku) {
    //      this.haku = haku;
    // }

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