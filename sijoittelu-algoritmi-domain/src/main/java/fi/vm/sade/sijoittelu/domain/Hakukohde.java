package fi.vm.sade.sijoittelu.domain;

import org.mongodb.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Kari Kammonen
 * 
 */
@Entity("Hakukohde")
@Indexes( {
        @Index("oid, sijoitteluajoId"),
        @Index("sijoitteluajoId"),
        @Index("sijoitteluajoId, valintatapajonot.hakemukset.hakemusOid")}
)
public class Hakukohde implements Serializable {

    @Id
    private ObjectId id;

    private Long sijoitteluajoId;

    private String oid;

    private HakukohdeTila tila;

    private String tarjoajaOid;

    private boolean kaikkiJonotSijoiteltu = true;

    @Embedded
    private List<Valintatapajono> valintatapajonot = new ArrayList<Valintatapajono>();
    
    @Embedded
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

    public String getTarjoajaOid() {
        return tarjoajaOid;
    }

    public void setTarjoajaOid(String tarjoajaOid) {
        this.tarjoajaOid = tarjoajaOid;
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

    public boolean isKaikkiJonotSijoiteltu() {
        return kaikkiJonotSijoiteltu;
    }

    public void setKaikkiJonotSijoiteltu(boolean kaikkiJonotSijoiteltu) {
        this.kaikkiJonotSijoiteltu = kaikkiJonotSijoiteltu;
    }
}
