package fi.vm.sade.sijoittelu.tulos.dto.raportointi;

import fi.vm.sade.sijoittelu.tulos.dto.HakemuksenTila;
import fi.vm.sade.sijoittelu.tulos.dto.IlmoittautumisTila;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class KevytHakutoiveenValintatapajonoDTO {
    private String valintatapajonoOid;

    private HakemuksenTila tila;
    private IlmoittautumisTila ilmoittautumisTila = IlmoittautumisTila.EI_TEHTY;

    private Integer varasijanNumero;
    private Map<String, String> tilanKuvaukset = new HashMap<String, String>();

    private boolean eiVarasijatayttoa;
    private boolean hyvaksyttyHarkinnanvaraisesti = false;
    private boolean hyvaksyttyVarasijalta;
    private boolean julkaistavissa;
    private boolean ehdollisestiHyvaksyttavissa;
    private String ehdollisenHyvaksymisenEhtoKoodi;
    private String ehdollisenHyvaksymisenEhtoFI;
    private String ehdollisenHyvaksymisenEhtoSV;
    private String ehdollisenHyvaksymisenEhtoEN;

    private Date valintatuloksenViimeisinMuutos;
    private Date hakemuksenTilanViimeisinMuutos;
    private Date varasijojaKaytetaanAlkaen;
    private Date varasijojaTaytetaanAsti;

    private Integer jonosija;
    private BigDecimal pisteet;
    private Integer prioriteetti;

    public HakemuksenTila getTila() {
        return tila;
    }

    public void setTila(HakemuksenTila tila) {
        this.tila = tila;
    }

    public Integer getVarasijanNumero() {
        return varasijanNumero;
    }

    public void setVarasijanNumero(Integer varasijanNumero) {
        this.varasijanNumero = varasijanNumero;
    }

    public Map<String, String> getTilanKuvaukset() {
        return tilanKuvaukset;
    }

    public void setTilanKuvaukset(Map<String, String> tilanKuvaukset) {
        this.tilanKuvaukset = tilanKuvaukset;
    }

    public boolean isEiVarasijatayttoa() {
        return eiVarasijatayttoa;
    }

    public void setEiVarasijatayttoa(boolean eiVarasijatayttoa) {
        this.eiVarasijatayttoa = eiVarasijatayttoa;
    }

    public boolean isHyvaksyttyHarkinnanvaraisesti() {
        return hyvaksyttyHarkinnanvaraisesti;
    }

    public void setHyvaksyttyHarkinnanvaraisesti(boolean hyvaksyttyHarkinnanvaraisesti) {
        this.hyvaksyttyHarkinnanvaraisesti = hyvaksyttyHarkinnanvaraisesti;
    }

    public boolean isHyvaksyttyVarasijalta() {
        return hyvaksyttyVarasijalta;
    }

    public void setHyvaksyttyVarasijalta(boolean hyvaksyttyVarasijalta) {
        this.hyvaksyttyVarasijalta = hyvaksyttyVarasijalta;
    }

    public Date getHakemuksenTilanViimeisinMuutos() {
        return hakemuksenTilanViimeisinMuutos;
    }

    public void setHakemuksenTilanViimeisinMuutos(Date hakemuksenTilanViimeisinMuutos) {
        this.hakemuksenTilanViimeisinMuutos = hakemuksenTilanViimeisinMuutos;
    }

    public Date getValintatuloksenViimeisinMuutos() {
        return valintatuloksenViimeisinMuutos;
    }

    public void setValintatuloksenViimeisinMuutos(Date valintatuloksenViimeisinMuutos) {
        this.valintatuloksenViimeisinMuutos = valintatuloksenViimeisinMuutos;
    }

    public boolean isJulkaistavissa() {
        return julkaistavissa;
    }

    public void setJulkaistavissa(boolean julkaistavissa) {
        this.julkaistavissa = julkaistavissa;
    }

    public boolean isEhdollisestiHyvaksyttavissa() {
        return ehdollisestiHyvaksyttavissa;
    }

    public void setEhdollisestiHyvaksyttavissa(boolean ehdollisestiHyvaksyttavissa) {
        this.ehdollisestiHyvaksyttavissa = ehdollisestiHyvaksyttavissa;
    }

    public BigDecimal getPisteet() {
        return pisteet;
    }

    public void setPisteet(BigDecimal pisteet) {
        this.pisteet = pisteet;
    }

    public Integer getJonosija() {
        return jonosija;
    }

    public void setJonosija(Integer jonosija) {
        this.jonosija = jonosija;
    }

    public Date getVarasijojaKaytetaanAlkaen() {
        return varasijojaKaytetaanAlkaen;
    }

    public void setVarasijojaKaytetaanAlkaen(Date varasijojaKaytetaanAlkaen) {
        this.varasijojaKaytetaanAlkaen = varasijojaKaytetaanAlkaen;
    }

    public Date getVarasijojaTaytetaanAsti() {
        return varasijojaTaytetaanAsti;
    }

    public void setVarasijojaTaytetaanAsti(Date varasijojaTaytetaanAsti) {
        this.varasijojaTaytetaanAsti = varasijojaTaytetaanAsti;
    }

    public String getValintatapajonoOid() {
        return valintatapajonoOid;
    }

    public void setValintatapajonoOid(String valintatapajonoOid) {
        this.valintatapajonoOid = valintatapajonoOid.intern();
    }

    public IlmoittautumisTila getIlmoittautumisTila() {
        return ilmoittautumisTila;
    }

    public void setIlmoittautumisTila(IlmoittautumisTila ilmoittautumisTila) {
        this.ilmoittautumisTila = ilmoittautumisTila;
    }

    public void setEhdollisenHyvaksymisenEhtoKoodi(String ehdollisenHyvaksymisenEhtoKoodi){
        this.ehdollisenHyvaksymisenEhtoKoodi = ehdollisenHyvaksymisenEhtoKoodi;
    }
    public String getEhdollisenHyvaksymisenEhtoKoodi(){
        return ehdollisenHyvaksymisenEhtoKoodi;
    }

    public void setEhdollisenHyvaksymisenEhtoFI(String ehdollisenHyvaksymisenEhtoFI){
        this.ehdollisenHyvaksymisenEhtoFI = ehdollisenHyvaksymisenEhtoFI;
    }
    public String getEhdollisenHyvaksymisenEhtoFI(){
        return ehdollisenHyvaksymisenEhtoFI;
    }
    public void setEhdollisenHyvaksymisenEhtoSV(String ehdollisenHyvaksymisenEhtoSV){
        this.ehdollisenHyvaksymisenEhtoSV = ehdollisenHyvaksymisenEhtoSV;
    }
    public String getEhdollisenHyvaksymisenEhtoSV(){
        return ehdollisenHyvaksymisenEhtoSV;
    }
    public void setEhdollisenHyvaksymisenEhtoEN(String ehdollisenHyvaksymisenEhtoEN){
        this.ehdollisenHyvaksymisenEhtoEN = ehdollisenHyvaksymisenEhtoEN;
    }
    public String getEhdollisenHyvaksymisenEhtoEN(){
        return ehdollisenHyvaksymisenEhtoEN;
    }

    public Integer getPrioriteetti() {
        return prioriteetti;
    }

    public void setPrioriteetti(Integer prioriteetti) {
        this.prioriteetti = prioriteetti;
    }
}
