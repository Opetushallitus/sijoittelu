package fi.vm.sade.sijoittelu.domain;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.annotate.JsonView;

/**
 * Created with IntelliJ IDEA.
 * User: kkammone
 * Date: 20.5.2013
 * Time: 13:28
 * To change this template use File | Settings | File Templates.
 */

@Entity("Valintatulos")
@Indexes({
        @Index("hakukohdeOid, valintatapajonoOid, hakemusOid"),
        @Index("hakukohdeOid, valintatapajonoOid"),
        @Index("hakukohdeOid"),
})
public class Valintatulos {

    @SuppressWarnings("unused")
    @Id
    private ObjectId id;

    //Maarittaa 2 muun kanssa taman luokan hakemisen
    //@JsonView(JsonViews.Basic.class)
    private String valintatapajonoOid;
    //Maarittaa 2 muun kanssa taman luokan hakemisen
    // @JsonView(JsonViews.Basic.class)
    private String hakemusOid;
    //Maarittaa 2 muun kanssa taman luokan hakemisen
    //@JsonView(JsonViews.Basic.class)
    private String hakukohdeOid;

    //@JsonView(JsonViews.Basic.class)
    private String hakijaOid;

    //@JsonView(JsonViews.Basic.class)
    private int hakutoive;

    @JsonView(JsonViews.Basic.class)
    private ValintatuloksenTila tila;

    public int getHakutoive() {
        return hakutoive;
    }

    public void setHakutoive(int hakutoive) {
        this.hakutoive = hakutoive;
    }

    public String getHakukohdeOid() {
        return hakukohdeOid;
    }

    public void setHakukohdeOid(String hakukohdeOid) {
        this.hakukohdeOid = hakukohdeOid;
    }

    public String getHakemusOid() {
        return hakemusOid;
    }

    public void setHakemusOid(String hakemusOid) {
        this.hakemusOid = hakemusOid;
    }

    public String getHakijaOid() {
        return hakijaOid;
    }

    public void setHakijaOid(String hakijaOid) {
        this.hakijaOid = hakijaOid;
    }

    public ValintatuloksenTila getTila() {
        return tila;
    }

    public void setTila(ValintatuloksenTila tila) {
        this.tila = tila;
    }

    public String getValintatapajonoOid() {
        return valintatapajonoOid;
    }

    public void setValintatapajonoOid(String valintatapajonoOid) {
        this.valintatapajonoOid = valintatapajonoOid;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }
}
