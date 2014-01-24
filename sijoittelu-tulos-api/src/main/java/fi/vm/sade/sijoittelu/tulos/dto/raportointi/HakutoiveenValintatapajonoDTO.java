package fi.vm.sade.sijoittelu.tulos.dto.raportointi;

import fi.vm.sade.sijoittelu.tulos.dto.HakemuksenTila;
import fi.vm.sade.sijoittelu.tulos.dto.JsonViews;
import fi.vm.sade.sijoittelu.tulos.dto.ValintatuloksenTila;
import org.codehaus.jackson.map.annotate.JsonView;

import java.math.BigDecimal;

/**
 * Created with IntelliJ IDEA. User: kkammone Date: 18.9.2013 Time: 14:11 To
 * change this template use File | Settings | File Templates.
 */
public class HakutoiveenValintatapajonoDTO {

    @JsonView(JsonViews.Hakija.class)
    private Integer valintatapajonoPrioriteetti;

    @JsonView(JsonViews.Hakija.class)
    private String valintatapajonoOid;

    @JsonView(JsonViews.Hakija.class)
    private String valintatapajonoNimi;

    @JsonView(JsonViews.Hakija.class)
    private Integer jonosija;

    @JsonView(JsonViews.Hakija.class)
    private BigDecimal paasyJaSoveltuvuusKokeenTulos;

    @JsonView(JsonViews.Hakija.class)
    private Integer varasijanNumero;

    @JsonView(JsonViews.Hakija.class)
    private HakemuksenTila tila;

    @JsonView(JsonViews.Hakija.class)
    private ValintatuloksenTila vastaanottotieto;

    @JsonView(JsonViews.Hakija.class)
    private boolean hyvaksyttyHarkinnanvaraisesti = false;

    @JsonView(JsonViews.Hakija.class)
    private Integer tasasijaJonosija;

    @JsonView(JsonViews.Hakija.class)
    private BigDecimal pisteet;

    @JsonView(JsonViews.Hakija.class)
    private BigDecimal alinHyvaksyttyPistemaara;

    @JsonView(JsonViews.Hakija.class)
    private Integer hakeneet;

    @JsonView(JsonViews.Hakija.class)
    private Integer hyvaksytty;

    @JsonView(JsonViews.Hakija.class)
    private Integer varalla;



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

    public void setPaasyJaSoveltuvuusKokeenTulos(BigDecimal paasyJaSoveltuvuusKokeenTulos) {
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

    public void setHyvaksyttyHarkinnanvaraisesti(boolean hyvaksyttyHarkinnanvaraisesti) {
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

    public void setValintatapajonoPrioriteetti(Integer valintatapajonoPrioriteetti) {
        this.valintatapajonoPrioriteetti = valintatapajonoPrioriteetti;
    }
}
