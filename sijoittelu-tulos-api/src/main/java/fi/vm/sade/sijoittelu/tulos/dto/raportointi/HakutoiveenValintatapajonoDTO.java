package fi.vm.sade.sijoittelu.tulos.dto.raportointi;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import fi.vm.sade.sijoittelu.tulos.dto.IlmoittautumisTila;
import fi.vm.sade.sijoittelu.tulos.dto.ValintatuloksenTila;

import fi.vm.sade.sijoittelu.tulos.dto.HakemuksenTila;

/**
 * Hakemukseen liittyvän hakumuksen sijoittelutila yksittäisessä valintatapajonossa
 */
public class HakutoiveenValintatapajonoDTO {

    private Integer valintatapajonoPrioriteetti;

    private String valintatapajonoOid;

    private String valintatapajonoNimi;

    private Integer jonosija;

    private BigDecimal paasyJaSoveltuvuusKokeenTulos;

    private Integer varasijanNumero;

    private HakemuksenTila tila;

    private Map<String, String> tilanKuvaukset = new HashMap<String, String>();

    private ValintatuloksenTila vastaanottotieto = ValintatuloksenTila.KESKEN;

    private IlmoittautumisTila ilmoittautumisTila = IlmoittautumisTila.EI_TEHTY;

    private boolean hyvaksyttyHarkinnanvaraisesti = false;

    private Integer tasasijaJonosija;

    private BigDecimal pisteet;

    private BigDecimal alinHyvaksyttyPistemaara;

    private Integer hakeneet;

    private Integer hyvaksytty;

    private Integer varalla;

    private Integer varasijat = 0;

    private Integer varasijaTayttoPaivat = 0;

    private Date varasijojaKaytetaanAlkaen;

    private Date varasijojaTaytetaanAsti;

    private String tayttojono;
    private boolean julkaistavissa;
    private boolean hyvaksyttyVarasijalta;

    private Date valintatuloksenViimeisinMuutos;
    private Date hakemuksenTilanViimeisinMuutos;

    public IlmoittautumisTila getIlmoittautumisTila() {
        return ilmoittautumisTila;
    }

    public void setIlmoittautumisTila(IlmoittautumisTila ilmoittautumisTila) {
        this.ilmoittautumisTila = ilmoittautumisTila;
    }

    public String getValintatapajonoOid() {
        return valintatapajonoOid;
    }

    public void setValintatapajonoOid(String valintatapajonoOid) {
        this.valintatapajonoOid = valintatapajonoOid;
    }

    public String getValintatapajonoNimi() {
        return valintatapajonoNimi;
    }

    public void setValintatapajonoNimi(String valintatapajonoNimi) {
        this.valintatapajonoNimi = valintatapajonoNimi;
    }

    public Integer getJonosija() {
        return jonosija;
    }

    public void setJonosija(Integer jonosija) {
        this.jonosija = jonosija;
    }

    public Integer getVarasijanNumero() {
        return varasijanNumero;
    }

    public void setVarasijanNumero(Integer varasijanNumero) {
        this.varasijanNumero = varasijanNumero;
    }

    public BigDecimal getPaasyJaSoveltuvuusKokeenTulos() {
        return paasyJaSoveltuvuusKokeenTulos;
    }

    public void setPaasyJaSoveltuvuusKokeenTulos(
            BigDecimal paasyJaSoveltuvuusKokeenTulos) {
        this.paasyJaSoveltuvuusKokeenTulos = paasyJaSoveltuvuusKokeenTulos;
    }

    public HakemuksenTila getTila() {
        return tila;
    }

    public void setTila(HakemuksenTila tila) {
        this.tila = tila;
    }

    public boolean isHyvaksyttyHarkinnanvaraisesti() {
        return hyvaksyttyHarkinnanvaraisesti;
    }

    public void setHyvaksyttyHarkinnanvaraisesti(
            boolean hyvaksyttyHarkinnanvaraisesti) {
        this.hyvaksyttyHarkinnanvaraisesti = hyvaksyttyHarkinnanvaraisesti;
    }

    public Integer getTasasijaJonosija() {
        return tasasijaJonosija;
    }

    public void setTasasijaJonosija(Integer tasasijaJonosija) {
        this.tasasijaJonosija = tasasijaJonosija;
    }

    public BigDecimal getAlinHyvaksyttyPistemaara() {
        return alinHyvaksyttyPistemaara;
    }

    public void setAlinHyvaksyttyPistemaara(BigDecimal alinHyvaksyttyPistemaara) {
        this.alinHyvaksyttyPistemaara = alinHyvaksyttyPistemaara;
    }

    public Integer getVaralla() {
        return varalla;
    }

    public void setVaralla(Integer varalla) {
        this.varalla = varalla;
    }

    public Integer getHyvaksytty() {
        return hyvaksytty;
    }

    public void setHyvaksytty(Integer hyvaksytty) {
        this.hyvaksytty = hyvaksytty;
    }

    public Integer getHakeneet() {
        return hakeneet;
    }

    public void setHakeneet(Integer hakeneet) {
        this.hakeneet = hakeneet;
    }

    public BigDecimal getPisteet() {
        return pisteet;
    }

    public void setPisteet(BigDecimal pisteet) {
        this.pisteet = pisteet;
    }

    public ValintatuloksenTila getVastaanottotieto() {
        return vastaanottotieto;
    }

    public void setVastaanottotieto(ValintatuloksenTila vastaanottotieto) {
        this.vastaanottotieto = vastaanottotieto;
    }

    public Integer getValintatapajonoPrioriteetti() {
        return valintatapajonoPrioriteetti;
    }

    public void setValintatapajonoPrioriteetti(
            Integer valintatapajonoPrioriteetti) {
        this.valintatapajonoPrioriteetti = valintatapajonoPrioriteetti;
    }

    public Map<String, String> getTilanKuvaukset() {
        return tilanKuvaukset;
    }

    public void setTilanKuvaukset(Map<String, String> tilanKuvaukset) {
        this.tilanKuvaukset = tilanKuvaukset;
    }

    public Integer getVarasijat() {
        return varasijat;
    }

    public void setVarasijat(Integer varasijat) {
        this.varasijat = varasijat;
    }

    public Integer getVarasijaTayttoPaivat() {
        return varasijaTayttoPaivat;
    }

    public void setVarasijaTayttoPaivat(Integer varasijaTayttoPaivat) {
        this.varasijaTayttoPaivat = varasijaTayttoPaivat;
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

    public String getTayttojono() {
        return tayttojono;
    }

    public void setTayttojono(String tayttojono) {
        this.tayttojono = tayttojono;
    }

    public void setJulkaistavissa(final boolean julkaistavissa) {
        this.julkaistavissa = julkaistavissa;
    }

    public boolean isJulkaistavissa() {
        return julkaistavissa;
    }

    public void setHyvaksyttyVarasijalta(final boolean hyvaksyttyVarasijalta) {
        this.hyvaksyttyVarasijalta = hyvaksyttyVarasijalta;
    }

    public boolean isHyvaksyttyVarasijalta() {
        return hyvaksyttyVarasijalta;
    }

    public Date getValintatuloksenViimeisinMuutos() {
        return valintatuloksenViimeisinMuutos;
    }

    public void setValintatuloksenViimeisinMuutos(Date valintatuloksenViimeisinMuutos) {
        this.valintatuloksenViimeisinMuutos = valintatuloksenViimeisinMuutos;
    }

    public Date getHakemuksenTilanViimeisinMuutos() {
        return hakemuksenTilanViimeisinMuutos;
    }

    public void setHakemuksenTilanViimeisinMuutos(Date hakemuksenTilanViimeisinMuutos) {
        this.hakemuksenTilanViimeisinMuutos = hakemuksenTilanViimeisinMuutos;
    }
}
