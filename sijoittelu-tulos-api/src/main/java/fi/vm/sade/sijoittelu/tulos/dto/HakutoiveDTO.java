package fi.vm.sade.sijoittelu.tulos.dto;

import org.codehaus.jackson.map.annotate.JsonView;

import java.math.BigDecimal;

/**
 * Created with IntelliJ IDEA.
 * User: kkammone
 * Date: 17.9.2013
 * Time: 9:47
 * To change this template use File | Settings | File Templates.
 */
public class HakutoiveDTO {

    @JsonView(JsonViews.Hakija.class)
    private Integer hakutoive ;

    @JsonView(JsonViews.Hakija.class)
    private String hakukohdeOid;

    @JsonView(JsonViews.Hakija.class)
    private Integer jonosija;

    @JsonView(JsonViews.Hakija.class)
    private BigDecimal pisteet;

    @JsonView(JsonViews.Hakija.class)
    private BigDecimal paasyJaSoveltuvuusKokeenTulos;

    @JsonView(JsonViews.Hakija.class)
    private Integer varasijanNumero;

    @JsonView(JsonViews.Hakija.class)
    private HakemuksenTila tila;

    @JsonView(JsonViews.Hakija.class)
    private boolean hyvaksyttyHarkinnanvaraisesti = false;

    @JsonView(JsonViews.Hakija.class)
    private Integer tasasijaJonosija;

    //etc...

    public Integer getHakutoive() {
        return hakutoive;
    }

    public void setHakutoive(Integer hakutoive) {
        this.hakutoive = hakutoive;
    }

    public String getHakukohdeOid() {
        return hakukohdeOid;
    }

    public Integer getJonosija() {
        return jonosija;
    }

    public void setJonosija(Integer jonosija) {
        this.jonosija = jonosija;
    }


    public void setHakukohdeOid(String hakukohdeOid) {
        this.hakukohdeOid = hakukohdeOid;
    }

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

    public BigDecimal getPaasyJaSoveltuvuusKokeenTulos() {
        return paasyJaSoveltuvuusKokeenTulos;
    }

    public void setPaasyJaSoveltuvuusKokeenTulos(BigDecimal paasyJaSoveltuvuusKokeenTulos) {
        this.paasyJaSoveltuvuusKokeenTulos = paasyJaSoveltuvuusKokeenTulos;
    }

    public BigDecimal getPisteet() {
        return pisteet;
    }

    public void setPisteet(BigDecimal pisteet) {
        this.pisteet = pisteet;
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
}
