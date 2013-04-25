package fi.vm.sade.sijoittelu.domain;

import java.util.ArrayList;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

/**
 * 
 * @author Kari Kammonen
 * 
 */
@Entity("Hakukohde")
public class Hakukohde {

    @SuppressWarnings("unused")
    @Id
    private ObjectId id;

    private String oid;

    private HakukohdeTila tila;

    @Embedded
    private ArrayList<Valintatapajono> valintatapajonot = new ArrayList<Valintatapajono>();
    
    @Embedded
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
}
