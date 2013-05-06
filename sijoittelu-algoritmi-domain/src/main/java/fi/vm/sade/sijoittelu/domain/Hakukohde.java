package fi.vm.sade.sijoittelu.domain;

import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.annotate.JsonView;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 
 * @author Kari Kammonen
 * 
 */
@Entity("Hakukohde")
public class Hakukohde implements Serializable {

    @SuppressWarnings("unused")
    @Id
    private ObjectId id;

    @JsonView(JsonViews.Basic.class)
    private String oid;

    @JsonView(JsonViews.Basic.class)
    private HakukohdeTila tila;

    @Embedded
    @JsonView(JsonViews.Basic.class)
    private ArrayList<Valintatapajono> valintatapajonot = new ArrayList<Valintatapajono>();
    
    @Embedded
    @JsonView(JsonViews.Basic.class)
    private ArrayList<Hakijaryhma> hakijaryhmat = new ArrayList<Hakijaryhma>();

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public HakukohdeTila getTila() {
        return tila;
    }

    public void setTila(HakukohdeTila tila) {
        this.tila = tila;
    }

    public ArrayList<Valintatapajono> getValintatapajonot() {
        return valintatapajonot;
    }

    public ArrayList<Hakijaryhma> getHakijaryhmat() {
        return hakijaryhmat;
    }

    @Override
    public String toString() {
        return "Hakukohde{" +
                "id=" + id +
                ", oid='" + oid + '\'' +
                ", tila=" + tila +
                ", valintatapajonot=" + valintatapajonot +
                ", hakijaryhmat=" + hakijaryhmat +
                '}';
    }
}
