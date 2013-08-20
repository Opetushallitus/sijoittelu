package fi.vm.sade.sijoittelu.domain;

import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.annotate.JsonView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Kari Kammonen
 * 
 */
@Entity("Hakukohde")
public class Hakukohde implements Serializable {

    @Id
    private ObjectId id;

    @JsonView(JsonViews.Basic.class)
    private Long sijoitteluajoId;

    @JsonView(JsonViews.Basic.class)
    private String oid;

    @JsonView(JsonViews.Basic.class)
    private HakukohdeTila tila;

    @Embedded
    @JsonView(JsonViews.Basic.class)
    private List<Valintatapajono> valintatapajonot = new ArrayList<Valintatapajono>();
    
    @Embedded
    @JsonView(JsonViews.Basic.class)
    private List<Hakijaryhma> hakijaryhmat = new ArrayList<Hakijaryhma>();


    public List<Valintatapajono> getValintatapajonot() {
        return valintatapajonot;
    }

    public void setValintatapajonot(List<Valintatapajono> valintatapajonot) {
        this.valintatapajonot = valintatapajonot;
    }

    public List<Hakijaryhma> getHakijaryhmat() {
        return hakijaryhmat;
    }

    public void setHakijaryhmat(List<Hakijaryhma> hakijaryhmat) {
        this.hakijaryhmat = hakijaryhmat;
    }

    public HakukohdeTila getTila() {
        return tila;
    }

    public void setTila(HakukohdeTila tila) {
        this.tila = tila;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public Long getSijoitteluajoId() {
        return sijoitteluajoId;
    }

    public void setSijoitteluajoId(Long sijoitteluajoId) {
        this.sijoitteluajoId = sijoitteluajoId;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Hakukohde{" +
                "id=" + id +
                ", sijoitteluajoId=" + sijoitteluajoId +
                ", oid='" + oid + '\'' +
                ", tila=" + tila +
                ", valintatapajonot=" + valintatapajonot +
                ", hakijaryhmat=" + hakijaryhmat +
                '}';
    }
}
