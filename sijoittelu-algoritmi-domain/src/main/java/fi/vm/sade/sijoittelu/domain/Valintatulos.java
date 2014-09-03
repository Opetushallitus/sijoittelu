package fi.vm.sade.sijoittelu.domain;

import org.mongodb.morphia.annotations.*;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.annotate.JsonView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
        @Index("hakukohdeOid")
})
public class Valintatulos implements Serializable {

    @SuppressWarnings("unused")
    @Id
    private ObjectId id;

    //Maarittaa 2 muun kanssa taman luokan hakemisen
    @JsonView(JsonViews.MonenHakemuksenTila.class)
    private String valintatapajonoOid;
    //Maarittaa 2 muun kanssa taman luokan hakemisen
    @JsonView(JsonViews.MonenHakemuksenTila.class)
    private String hakemusOid;
    //Maarittaa 2 muun kanssa taman luokan hakemisen
    @JsonView(JsonViews.MonenHakemuksenTila.class)
    private String hakukohdeOid;

    @JsonView(JsonViews.MonenHakemuksenTila.class)
    private String hakijaOid;

    @JsonView(JsonViews.MonenHakemuksenTila.class)
    private String hakuOid;

    @JsonView(JsonViews.MonenHakemuksenTila.class)
    private int hakutoive;

    @JsonView({JsonViews.Tila.class, JsonViews.MonenHakemuksenTila.class})
    private ValintatuloksenTila tila;

    @JsonView({JsonViews.Tila.class, JsonViews.MonenHakemuksenTila.class})
    private IlmoittautumisTila ilmoittautumisTila;

    @Embedded
    @JsonView({JsonViews.Tila.class, JsonViews.MonenHakemuksenTila.class})
    private List<LogEntry> logEntries = new ArrayList<LogEntry>();

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

    public String getHakuOid() {
        return hakuOid;
    }

    public void setHakuOid(String hakuOid) {
        this.hakuOid = hakuOid;
    }

    public List<LogEntry> getLogEntries() {
        return logEntries;
    }

    public void setLogEntries(List<LogEntry> logEntries) {
        this.logEntries = logEntries;
    }

    public IlmoittautumisTila getIlmoittautumisTila() {
        return ilmoittautumisTila;
    }

    public void setIlmoittautumisTila(final IlmoittautumisTila ilmoittautumisTila) {
        this.ilmoittautumisTila = ilmoittautumisTila;
    }
}
