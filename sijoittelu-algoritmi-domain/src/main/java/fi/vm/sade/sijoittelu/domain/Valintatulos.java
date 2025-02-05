package fi.vm.sade.sijoittelu.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Valintatulos implements Serializable {

    public static final String JARJESTELMA = "järjestelmä";

    //Maarittaa 2 muun kanssa taman luokan hakemisen
    private String valintatapajonoOid;
    //Maarittaa 2 muun kanssa taman luokan hakemisen
    private String hakemusOid;
    //Maarittaa 2 muun kanssa taman luokan hakemisen
    private String hakukohdeOid;

    private boolean ehdollisestiHyvaksyttavissa;

    private String ehdollisenHyvaksymisenEhtoKoodi;
    private String ehdollisenHyvaksymisenEhtoFI;
    private String ehdollisenHyvaksymisenEhtoSV;
    private String ehdollisenHyvaksymisenEhtoEN;

    private boolean julkaistavissa;

    private boolean hyvaksyttyVarasijalta;

    private boolean hyvaksyPeruuntunut;

    private String hakijaOid;

    private String hakuOid;

    private int hakutoive;

    private IlmoittautumisTila ilmoittautumisTila = IlmoittautumisTila.EI_TEHTY;

    private ValintatulosMailStatus mailStatus = new ValintatulosMailStatus();

    private Date hyvaksymiskirjeLahetetty;

    private ValintatuloksenTila tila;

    private ValintatuloksenTila tilaHakijalle;

    private List<LogEntry> logEntries = new ArrayList<>();

    private Date read = new Date();

    private Date viimeinenMuutos;

    @JsonIgnore
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

    public Valintatulos(String hakemusOid, String hakijaOid, String hakukohdeOid, boolean hyvaksyttyVarasijalta, IlmoittautumisTila ilmoittautumisTila,
                        boolean julkaistavissa, ValintatuloksenTila tila, boolean ehdollisestiHyvaksyttavissa, String valintatapajonoOid) {
        this.hakemusOid = hakemusOid;
        this.hakijaOid = hakijaOid;
        this.hakukohdeOid = hakukohdeOid;
        this.ehdollisestiHyvaksyttavissa = ehdollisestiHyvaksyttavissa;
        this.ilmoittautumisTila = ilmoittautumisTila;
        this.julkaistavissa = julkaistavissa;
        this.tila = tila;
        this.valintatapajonoOid = valintatapajonoOid;
        this.hyvaksyttyVarasijalta = hyvaksyttyVarasijalta;
    }

    @Deprecated
    public Valintatulos(String hakemusOid, String hakijaOid, String hakukohdeOid, String hakuOid,
                         int hakutoive, boolean hyvaksyttyVarasijalta, IlmoittautumisTila ilmoittautumisTila,
                         boolean julkaistavissa, ValintatuloksenTila tila, boolean ehdollisestiHyvaksyttavissa, String valintatapajonoOid) {
        this.hakemusOid = hakemusOid;
        this.hakijaOid = hakijaOid;
        this.hakukohdeOid = hakukohdeOid;
        this.hakuOid = hakuOid;
        this.hakutoive = hakutoive;
        this.ehdollisestiHyvaksyttavissa = ehdollisestiHyvaksyttavissa;
        this.ilmoittautumisTila = ilmoittautumisTila;
        this.julkaistavissa = julkaistavissa;
        this.tila= tila;
        this.valintatapajonoOid = valintatapajonoOid;
        this.hyvaksyttyVarasijalta = hyvaksyttyVarasijalta;
    }

    public Valintatulos(String hakemusOid, String hakijaOid, String hakukohdeOid, String hakuOid,
                        int hakutoive, boolean hyvaksyttyVarasijalta, IlmoittautumisTila ilmoittautumisTila,
                        boolean julkaistavissa, ValintatuloksenTila tila, boolean ehdollisestiHyvaksyttavissa,
                        String valintatapajonoOid, Date hyvaksymiskirjeLahetetty, String ehdollisenHyvaksymisenEhtoKoodi,
                        String ehdollisenHyvaksymisenEhtoFI, String ehdollisenHyvaksymisenEhtoSV, String ehdollisenHyvaksymisenEhtoEN) {
        this.hakemusOid = hakemusOid;
        this.hakijaOid = hakijaOid;
        this.hakukohdeOid = hakukohdeOid;
        this.hakuOid = hakuOid;
        this.hakutoive = hakutoive;
        this.ehdollisestiHyvaksyttavissa = ehdollisestiHyvaksyttavissa;
        this.ilmoittautumisTila = ilmoittautumisTila;
        this.julkaistavissa = julkaistavissa;
        this.tila= tila;
        this.valintatapajonoOid = valintatapajonoOid;
        this.hyvaksyttyVarasijalta = hyvaksyttyVarasijalta;
        this.hyvaksymiskirjeLahetetty = hyvaksymiskirjeLahetetty;
        this.ehdollisenHyvaksymisenEhtoKoodi = ehdollisenHyvaksymisenEhtoKoodi;
        this.ehdollisenHyvaksymisenEhtoFI = ehdollisenHyvaksymisenEhtoFI;
        this.ehdollisenHyvaksymisenEhtoSV = ehdollisenHyvaksymisenEhtoSV;
        this.ehdollisenHyvaksymisenEhtoEN = ehdollisenHyvaksymisenEhtoEN;
    }

    public List<LogEntry> getOriginalLogEntries() {
        return originalLogEntries;
    }

    //This shuould only be used in in valintarekisteri!!
    public void setOriginalLogEntries(List<LogEntry> entries) { this.originalLogEntries = entries; }

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

    public ValintatulosMailStatus getMailStatus() {
        return mailStatus;
    }

    public void setMailStatus(ValintatulosMailStatus mailStatus) {
        this.mailStatus = mailStatus;
    }

    public ValintatuloksenTila getTila() {
        return tila;
    }

    public void setTila(ValintatuloksenTila tila, String selite) {
        setTila(tila, selite, JARJESTELMA);
    }

    public ValintatuloksenTila getTilaHakijalle() {
        return tilaHakijalle;
    }

    public void setTilaHakijalle(ValintatuloksenTila tilaHakijalle) {
        this.tilaHakijalle = tilaHakijalle;
    }

    public void setTila(ValintatuloksenTila tila, String selite, String muokkaaja) {
        modified("tila", this.tila, tila, muokkaaja, selite);
        this.tila = tila;
    }

    public void setTila(ValintatuloksenTila oldTila, ValintatuloksenTila tila, String selite, String muokkaaja) {
        modified("tila", oldTila, tila, muokkaaja, selite);
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

    public boolean getEhdollisestiHyvaksyttavissa() {
        return ehdollisestiHyvaksyttavissa;
    }

    public void setEhdollisestiHyvaksyttavissa(boolean ehdollisestiHyvaksyttavissa, String selite, String muokkaaja) {
        modified("ehdollisestiHyvaksyttavissa", this.ehdollisestiHyvaksyttavissa, ehdollisestiHyvaksyttavissa, muokkaaja, selite);
        this.ehdollisestiHyvaksyttavissa = ehdollisestiHyvaksyttavissa;
    }

    public void setRead(Date read) {
        this.read = read;
    }

    public Date getRead() {
        return read;
    }

    public Date getHyvaksymiskirjeLahetetty() {
        return hyvaksymiskirjeLahetetty;
    }

    public void setHyvaksymiskirjeLahetetty(Date hyvaksymiskirjeLahetetty) {
        setHyvaksymiskirjeLahetetty(hyvaksymiskirjeLahetetty, "");
    }

    public void setHyvaksymiskirjeLahetetty(Date hyvaksymiskirjeLahetetty, String selite) {
        setHyvaksymiskirjeLahetetty(hyvaksymiskirjeLahetetty, selite, JARJESTELMA);
    }

    public void setHyvaksymiskirjeLahetetty(Date hyvaksymiskirjeLahetetty, String selite, String muokkaaja) {
        modified("hyvaksymiskirjeLahetetty", this.hyvaksymiskirjeLahetetty, hyvaksymiskirjeLahetetty, muokkaaja, selite);
        this.hyvaksymiskirjeLahetetty = hyvaksymiskirjeLahetetty;
    }

    public String getEhdollisenHyvaksymisenEhtoKoodi(){ return ehdollisenHyvaksymisenEhtoKoodi; }
    public void setEhdollisenHyvaksymisenEhtoKoodi(String ehdollisenHyvaksymisenEhtoKoodi, String selite, String muokkaaja) {
        modified("ehdollisenHyvaksymisenEhtoKoodi", this.ehdollisenHyvaksymisenEhtoKoodi, ehdollisenHyvaksymisenEhtoKoodi, muokkaaja, selite);
        this.ehdollisenHyvaksymisenEhtoKoodi = ehdollisenHyvaksymisenEhtoKoodi;
    }

    public String getEhdollisenHyvaksymisenEhtoFI(){ return ehdollisenHyvaksymisenEhtoFI; }
    public void setEhdollisenHyvaksymisenEhtoFI(String ehdollisenHyvaksymisenEhtoFI, String selite, String muokkaaja) {
        modified("ehdollisenHyvaksymisenEhtoFI", this.ehdollisenHyvaksymisenEhtoFI, ehdollisenHyvaksymisenEhtoFI, muokkaaja, selite);
        this.ehdollisenHyvaksymisenEhtoFI = ehdollisenHyvaksymisenEhtoFI;
    }

    public String getEhdollisenHyvaksymisenEhtoSV(){ return ehdollisenHyvaksymisenEhtoSV; }
    public void setEhdollisenHyvaksymisenEhtoSV(String ehdollisenHyvaksymisenEhtoSV, String selite, String muokkaaja) {
        modified("ehdollisenHyvaksymisenEhtoSV", this.ehdollisenHyvaksymisenEhtoSV, ehdollisenHyvaksymisenEhtoSV, muokkaaja, selite);
        this.ehdollisenHyvaksymisenEhtoSV = ehdollisenHyvaksymisenEhtoSV;
    }

    public String getEhdollisenHyvaksymisenEhtoEN(){ return ehdollisenHyvaksymisenEhtoEN; }
    public void setEhdollisenHyvaksymisenEhtoEN(String ehdollisenHyvaksymisenEhtoEN, String selite, String muokkaaja) {
        modified("ehdollisenHyvaksymisenEhtoEN", this.ehdollisenHyvaksymisenEhtoEN, ehdollisenHyvaksymisenEhtoEN, muokkaaja, selite);
        this.ehdollisenHyvaksymisenEhtoEN = ehdollisenHyvaksymisenEhtoEN;
    }

    @Override
    public String toString() {
        return "Valintatulos{" +
                ", valintatapajonoOid='" + valintatapajonoOid + '\'' +
                ", hakemusOid='" + hakemusOid + '\'' +
                ", hakukohdeOid='" + hakukohdeOid + '\'' +
                ", julkaistavissa=" + julkaistavissa +
                ", hyvaksyttyVarasijalta=" + hyvaksyttyVarasijalta +
                ", hyvaksyPeruuntunut=" + hyvaksyPeruuntunut +
                ", ehdollisestiHyvaksyttavissa=" + ehdollisestiHyvaksyttavissa +
                ", hakijaOid='" + hakijaOid + '\'' +
                ", hakuOid='" + hakuOid + '\'' +
                ", hakutoive=" + hakutoive +
                ", ilmoittautumisTila=" + ilmoittautumisTila +
                ", mailStatus=" + mailStatus +
                ", tila=" + tila +
                ", tilaHakijalle=" + tilaHakijalle +
                ", hyvaksymiskirjeLahetetty=" + hyvaksymiskirjeLahetetty +
                ", logEntries=" + logEntries +
                ", read=" + read +
                ", viimeinenMuutos=" + viimeinenMuutos +
                ", originalLogEntries=" + originalLogEntries +
                ", ehdollisenHyvaksymisenEhtoKoodi=" + ehdollisenHyvaksymisenEhtoKoodi +
                ", ehdollisenHyvaksymisenEhtoFI=" + ehdollisenHyvaksymisenEhtoFI +
                ", ehdollisenHyvaksymisenEhtoSV=" + ehdollisenHyvaksymisenEhtoSV +
                ", ehdollisenHyvaksymisenEhtoEN=" + ehdollisenHyvaksymisenEhtoEN +
                '}';
    }
}
