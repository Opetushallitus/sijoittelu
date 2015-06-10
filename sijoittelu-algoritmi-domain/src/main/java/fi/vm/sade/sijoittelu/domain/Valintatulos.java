package fi.vm.sade.sijoittelu.domain;

import org.mongodb.morphia.annotations.*;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity("Valintatulos")
@Indexes({
        @Index("hakukohdeOid, valintatapajonoOid, hakemusOid"),
        @Index("hakukohdeOid, valintatapajonoOid"),
        @Index("hakukohdeOid"),
        @Index("hakemusOid"),
        @Index("hakuOid, tila, julkaistavissa, mailStatus.sent, mailStatus.previousCheck")
})
public class Valintatulos implements Serializable {

    @SuppressWarnings("unused")
    @Id
    private ObjectId id;

    //Maarittaa 2 muun kanssa taman luokan hakemisen
    private String valintatapajonoOid;
    //Maarittaa 2 muun kanssa taman luokan hakemisen
    private String hakemusOid;
    //Maarittaa 2 muun kanssa taman luokan hakemisen
    private String hakukohdeOid;

    private boolean julkaistavissa;

    private boolean hyvaksyttyVarasijalta;

    private String hakijaOid;

    private String hakuOid;

    private int hakutoive;

    private ValintatuloksenTila tila = ValintatuloksenTila.KESKEN;

    private IlmoittautumisTila ilmoittautumisTila = IlmoittautumisTila.EI_TEHTY;

    @Embedded
    private List<LogEntry> logEntries = new ArrayList<LogEntry>();

    private ValintatulosMailStatus mailStatus = new ValintatulosMailStatus();

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

    public boolean getJulkaistavissa() {
        return julkaistavissa;
    }

    public void setJulkaistavissa(boolean julkaistavissa) {
        this.julkaistavissa = julkaistavissa;
    }

    public boolean getHyvaksyttyVarasijalta() {
        return hyvaksyttyVarasijalta;
    }

    public void setHyvaksyttyVarasijalta(boolean hyvaksyttyVarasijalta) {
        this.hyvaksyttyVarasijalta = hyvaksyttyVarasijalta;
    }
}
