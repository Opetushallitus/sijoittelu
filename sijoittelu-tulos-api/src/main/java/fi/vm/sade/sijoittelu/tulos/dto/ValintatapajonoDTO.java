package fi.vm.sade.sijoittelu.tulos.dto;

import org.codehaus.jackson.map.annotate.JsonView;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA. User: kkammone Date: 5.9.2013 Time: 12:47 To
 * change this template use File | Settings | File Templates.
 */
public class ValintatapajonoDTO implements Serializable {

    @JsonView(JsonViews.Hakukohde.class)
    private Tasasijasaanto tasasijasaanto;

    @JsonView(JsonViews.Hakukohde.class)
    private ValintatapajonoTila tila;

    @JsonView(JsonViews.Hakukohde.class)
    private String oid;

    @JsonView(JsonViews.Hakukohde.class)
    private String nimi;

    @JsonView(JsonViews.Hakukohde.class)
    private Integer prioriteetti;

    @JsonView(JsonViews.Hakukohde.class)
    private Integer aloituspaikat;

    @JsonView(JsonViews.Hakukohde.class)
    private BigDecimal alinHyvaksyttyPistemaara;

    @JsonView(JsonViews.Hakukohde.class)
    private Boolean eiVarasijatayttoa;

    @JsonView(JsonViews.Hakukohde.class)
    private Boolean kaikkiEhdonTayttavatHyvaksytaan;

    @JsonView(JsonViews.Hakukohde.class)
    private List<HakemusDTO> hakemukset = new ArrayList<HakemusDTO>();

    @JsonView(JsonViews.Hakukohde.class)
    private Integer hakeneet;

    @JsonView(JsonViews.Hakukohde.class)
    private Integer hyvaksytty;

    @JsonView(JsonViews.Hakukohde.class)
    private Integer varalla;

    public Integer getHakeneet() {
        return hakeneet;
    }

    public void setHakeneet(Integer hakeneet) {
        this.hakeneet = hakeneet;
    }

    public Tasasijasaanto getTasasijasaanto() {
        return tasasijasaanto;
    }

    public void setTasasijasaanto(Tasasijasaanto tasasijasaanto) {
        this.tasasijasaanto = tasasijasaanto;
    }

    public ValintatapajonoTila getTila() {
        return tila;
    }

    public BigDecimal getAlinHyvaksyttyPistemaara() {
        return alinHyvaksyttyPistemaara;
    }

    public void setAlinHyvaksyttyPistemaara(BigDecimal alinHyvaksyttyPistemaara) {
        this.alinHyvaksyttyPistemaara = alinHyvaksyttyPistemaara;
    }

    public void setTila(ValintatapajonoTila tila) {
        this.tila = tila;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public Integer getPrioriteetti() {
        return prioriteetti;
    }

    public void setPrioriteetti(Integer prioriteetti) {
        this.prioriteetti = prioriteetti;
    }

    public Integer getAloituspaikat() {
        return aloituspaikat;
    }

    public void setAloituspaikat(Integer aloituspaikat) {
        this.aloituspaikat = aloituspaikat;
    }

    public Boolean getEiVarasijatayttoa() {
        return eiVarasijatayttoa;
    }

    public void setEiVarasijatayttoa(Boolean eiVarasijatayttoa) {
        this.eiVarasijatayttoa = eiVarasijatayttoa;
    }

    public List<HakemusDTO> getHakemukset() {
        return hakemukset;
    }

    public void setHakemukset(List<HakemusDTO> hakemukset) {
        this.hakemukset = hakemukset;
    }

    public Integer getHyvaksytty() {
        return hyvaksytty;
    }

    public void setHyvaksytty(Integer hyvaksytty) {
        this.hyvaksytty = hyvaksytty;
    }

    public Integer getVaralla() {
        return varalla;
    }

    public void setVaralla(Integer varalla) {
        this.varalla = varalla;
    }

    public String getNimi() {
        return nimi;
    }

    public void setNimi(String nimi) {
        this.nimi = nimi;
    }

    public Boolean getKaikkiEhdonTayttavatHyvaksytaan() {
        return kaikkiEhdonTayttavatHyvaksytaan;
    }

    public void setKaikkiEhdonTayttavatHyvaksytaan(Boolean kaikkiEhdonTayttavatHyvaksytaan) {
        this.kaikkiEhdonTayttavatHyvaksytaan = kaikkiEhdonTayttavatHyvaksytaan;
    }
}
