package fi.vm.sade.sijoittelu.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;
import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.*;

import java.io.Serializable;
import java.util.*;

@Entity("Valintatulos")
@Indexes({
        @Index("hakukohdeOid, valintatapajonoOid, hakemusOid"),
        @Index("hakukohdeOid, valintatapajonoOid"),
        @Index("hakukohdeOid"),
        @Index("hakemusOid"),
        @Index("hakuOid, tila, julkaistavissa, mailStatus.sent, mailStatus.previousCheck")
})
public class Valintatulos implements Serializable {

    public static final String JARJESTELMA = "järjestelmä";
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

    private boolean hyvaksyPeruuntunut;

    private String hakijaOid;

    private String hakuOid;

    private int hakutoive;

    private ValintatuloksenTila tila = ValintatuloksenTila.KESKEN;

    private IlmoittautumisTila ilmoittautumisTila = IlmoittautumisTila.EI_TEHTY;

    private ValintatulosMailStatus mailStatus = new ValintatulosMailStatus();

    @Embedded
    private List<LogEntry> logEntries = new ArrayList<>();

    @Transient
    private Date read = new Date();

    @Transient
    private Date viimeinenMuutos;

    @JsonIgnore
    @Transient
    private List<LogEntry> originalLogEntries = Collections.emptyList();

    public Valintatulos() {}

    public Valintatulos(String valintatapajonoOid, String hakemusOid, String hakukohdeOid, String hakijaOid, String hakuOid, int hakutoive) {
        this.valintatapajonoOid = valintatapajonoOid;
        this.hakemusOid = hakemusOid;
        this.hakukohdeOid = hakukohdeOid;
        this.hakijaOid = hakijaOid;
        this.hakuOid = hakuOid;
        this.hakutoive = hakutoive;
    }

    public Valintatulos(String hakemusOid, String hakijaOid, String hakukohdeOid, String hakuOid,
                        int hakutoive, boolean hyvaksyttyVarasijalta, IlmoittautumisTila ilmoittautumisTila,
                        boolean julkaistavissa, ValintatuloksenTila tila, String valintatapajonoOid) {
        this.hakemusOid = hakemusOid;
        this.hakijaOid = hakijaOid;
        this.hakukohdeOid = hakukohdeOid;
        this.hakuOid = hakuOid;
        this.hakutoive = hakutoive;
        this.ilmoittautumisTila = ilmoittautumisTila;
        this.julkaistavissa = julkaistavissa;
        this.tila= tila;
        this.valintatapajonoOid = valintatapajonoOid;
        this.hyvaksyttyVarasijalta = hyvaksyttyVarasijalta;
    }

    public List<LogEntry> getOriginalLogEntries() {
        return originalLogEntries;
    }

    public Date getViimeinenMuutos() {
        return viimeinenMuutos;
    }

    @PostLoad
    public void postLoad() {
        originalLogEntries = ImmutableList.copyOf(logEntries);
        viimeinenMuutos = logEntries.stream()
                    .filter(e -> e.getLuotu() != null)
                    .map(LogEntry::getLuotu)
                    .sorted((e1,e2) -> e2.compareTo(e1)).findFirst().orElse(null);
    }

    private void modified(String field, Object oldVal, Object newVal, String muokkaaja, String selite) {
        if ((null == oldVal && null != newVal) || (null != oldVal && !oldVal.equals(newVal))) {
            LogEntry e = new LogEntry(muokkaaja, field + ": " + oldVal + " -> " + newVal, selite);
            logEntries.add(e);
            viimeinenMuutos = e.getLuotu();
        }
    }

    public int getHakutoive() {
        return hakutoive;
    }

    public void setHakutoive(int hakutoive, String selite) {
        setHakutoive(hakutoive, selite, JARJESTELMA);
    }

    public void setHakutoive(int hakutoive, String selite, String muokkaaja) {
        modified("hakutoive", this.hakutoive, hakutoive, muokkaaja, selite);
        this.hakutoive = hakutoive;
    }

    public String getHakukohdeOid() {
        return hakukohdeOid;
    }

    public void setHakukohdeOid(String hakukohdeOid, String selite) {
        setHakukohdeOid(hakukohdeOid, selite, JARJESTELMA);
    }

    public void setHakukohdeOid(String hakukohdeOid, String selite, String muokkaaja) {
        modified("hakukohdeOid", this.hakukohdeOid, hakukohdeOid, muokkaaja, selite);
        this.hakukohdeOid = hakukohdeOid;
    }

    public String getHakemusOid() {
        return hakemusOid;
    }

    public void setHakemusOid(String hakemusOid, String selite) {
        setHakemusOid(hakemusOid, selite, JARJESTELMA);
    }

    public void setHakemusOid(String hakemusOid, String selite, String muokkaaja) {
        modified("hakemusOid", this.hakemusOid, hakemusOid, muokkaaja, selite);
        this.hakemusOid = hakemusOid;
    }

    public String getHakijaOid() {
        return hakijaOid;
    }

    public void setHakijaOid(String hakijaOid, String selite) {
        setHakijaOid(hakijaOid, selite, JARJESTELMA);
    }

    public void setHakijaOid(String hakijaOid, String selite, String muokkaaja) {
        modified("hakijaOid", this.hakijaOid, hakijaOid, muokkaaja, selite);
        this.hakijaOid = hakijaOid;
    }

    public ValintatuloksenTila getTila() {
        return tila;
    }

    public void setTila(ValintatuloksenTila tila, String selite) {
        setTila(tila, selite, JARJESTELMA);
    }

    public void setTila(ValintatuloksenTila tila, String selite, String muokkaaja) {
        modified("tila", this.tila, tila, muokkaaja, selite);
        this.tila = tila;
    }

    public String getValintatapajonoOid() {
        return valintatapajonoOid;
    }

    public void setValintatapajonoOid(String valintatapajonoOid, String selite) {
        setValintatapajonoOid(valintatapajonoOid, selite, JARJESTELMA);
    }

    public void setValintatapajonoOid(String valintatapajonoOid, String selite, String muokkaaja) {
        modified("valintatapajonoOid", this.valintatapajonoOid, valintatapajonoOid, muokkaaja, selite);
        this.valintatapajonoOid = valintatapajonoOid;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id, String selite) {
        setId(id, selite, JARJESTELMA);
    }

    public void setId(ObjectId id, String selite, String muokkaaja) {
        modified("id", this.id, id, muokkaaja, selite);
        this.id = id;
    }

    public String getHakuOid() {
        return hakuOid;
    }

    public void setHakuOid(String hakuOid, String selite) {
        setHakuOid(hakuOid, selite, JARJESTELMA);
    }

    public void setHakuOid(String hakuOid, String selite, String muokkaaja) {
        modified("hakuOid", this.hakuOid, hakuOid, muokkaaja, selite);
        this.hakuOid = hakuOid;
    }

    public List<LogEntry> getLogEntries() {
        return ImmutableList.copyOf(logEntries);
    }

    public IlmoittautumisTila getIlmoittautumisTila() {
        return ilmoittautumisTila;
    }

    public void setIlmoittautumisTila(final IlmoittautumisTila ilmoittautumisTila, String selite) {
        setIlmoittautumisTila(ilmoittautumisTila, selite, JARJESTELMA);
    }

    public void setIlmoittautumisTila(final IlmoittautumisTila ilmoittautumisTila, String selite, String muokkaaja) {
        modified("ilmoittautumisTila", this.ilmoittautumisTila, ilmoittautumisTila, muokkaaja, selite);
        this.ilmoittautumisTila = ilmoittautumisTila;
    }

    public boolean getJulkaistavissa() {
        return julkaistavissa;
    }

    public void setJulkaistavissa(boolean julkaistavissa, String selite) {
        setJulkaistavissa(julkaistavissa, selite, JARJESTELMA);
    }

    public void setJulkaistavissa(boolean julkaistavissa, String selite, String muokkaaja) {
        modified("julkaistavissa", this.julkaistavissa, julkaistavissa, muokkaaja, selite);
        this.julkaistavissa = julkaistavissa;
    }

    public boolean getHyvaksyttyVarasijalta() {
        return hyvaksyttyVarasijalta;
    }

    public void setHyvaksyttyVarasijalta(boolean hyvaksyttyVarasijalta, String selite) {
        setHyvaksyttyVarasijalta(hyvaksyttyVarasijalta, selite, JARJESTELMA);
    }

    public void setHyvaksyttyVarasijalta(boolean hyvaksyttyVarasijalta, String selite, String muokkaaja) {
        modified("hyvaksyttyVarasijalta", this.hyvaksyttyVarasijalta, hyvaksyttyVarasijalta, muokkaaja, selite);
        this.hyvaksyttyVarasijalta = hyvaksyttyVarasijalta;
    }

    public boolean getHyvaksyPeruuntunut() {
        return this.hyvaksyPeruuntunut;
    }

    public void setHyvaksyPeruuntunut(boolean hyvaksyPeruuntunut, String selite) {
        setHyvaksyPeruuntunut(hyvaksyPeruuntunut, selite, JARJESTELMA);
    }

    public void setHyvaksyPeruuntunut(boolean hyvaksyPeruuntunut, String selite, String muokkaaja) {
        modified("hyvaksyPeruuntunut", this.hyvaksyPeruuntunut, hyvaksyPeruuntunut, muokkaaja, selite);
        this.hyvaksyPeruuntunut = hyvaksyPeruuntunut;
    }

    public Date getRead() {
        return read;
    }
}
