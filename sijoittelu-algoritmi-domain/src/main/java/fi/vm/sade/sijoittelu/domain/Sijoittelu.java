package fi.vm.sade.sijoittelu.domain;

import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Reference;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.annotate.JsonView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Kari Kammonen
 */
@Entity("Sijoittelu")
public class Sijoittelu {

    @SuppressWarnings("unused")
    @Id
    private ObjectId id;

    @JsonView(JsonViews.Basic.class)
    private String hakuOid;

    @JsonView(JsonViews.Basic.class)
    private Long sijoitteluId;

    @JsonView(JsonViews.Basic.class)
    private Date created;

    /**
     * Maarittaa sijoitellaanko taman sijoittelun sijoitteluajot
     */
    @JsonView(JsonViews.Basic.class)
    private boolean sijoittele = true;

    //@Embedded
   // @JsonView(JsonViews.Basic.class)
   // private Haku haku;

    @Reference(value = "SijoitteluAjo", lazy = true)
    private List<SijoitteluAjo> sijoitteluajot = new ArrayList<SijoitteluAjo>();

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
}
