package fi.vm.sade.sijoittelu.domain;

import com.google.code.morphia.annotations.Embedded;
import org.codehaus.jackson.map.annotate.JsonView;

import java.io.Serializable;

/**
 * 
 * @author Kari Kammonen
 * 
 */
@Embedded
public class Hakemus implements Serializable {

    @JsonView(JsonViews.Basic.class)
    private String hakijaOid;

    @JsonView(JsonViews.Basic.class)
    private String hakemusOid;

    @JsonView(JsonViews.Basic.class)
    private String etunimi;

    @JsonView(JsonViews.Basic.class)
    private String sukunimi;

    /**
     * defaulttina tosi epakorkea
     */
    @JsonView(JsonViews.Basic.class)
    private Integer prioriteetti;

    @JsonView(JsonViews.Basic.class)
    private Integer jonosija;

    @JsonView(JsonViews.Basic.class)
    private Integer tasasijaJonosija;

    @JsonView(JsonViews.Basic.class)
    private HakemuksenTila tila;

    @JsonView(JsonViews.Basic.class)
    private boolean hyvaksyttyHarkinnanvaraisesti = false;


     public int getPrioriteetti() {
        return prioriteetti;
    }

    public void setPrioriteetti(int prioriteetti) {
        this.prioriteetti = prioriteetti;
    }

    public int getJonosija() {
        return jonosija;
    }

    public void setJonosija(int jonosija) {
        this.jonosija = jonosija;
    }

    public HakemuksenTila getTila() {
        return tila;
    }

    public void setTila(HakemuksenTila tila) {
        this.tila = tila;
    }

    public String getHakijaOid() {
        return hakijaOid;
    }

    public void setHakijaOid(String hakijaOid) {
        this.hakijaOid = hakijaOid;
    }

    public Integer getTasasijaJonosija() {
        return tasasijaJonosija;
    }

    public void setTasasijaJonosija(Integer tasasijaJonosija) {
        this.tasasijaJonosija = tasasijaJonosija;
    }

    public String getHakemusOid() {
        return hakemusOid;
    }

    public void setHakemusOid(String hakemusOid) {
        this.hakemusOid = hakemusOid;
    }

    @Override
    public String toString() {
        return "Hakemus{" +
                "hakijaOid='" + hakijaOid + '\'' +
                ", prioriteetti=" + prioriteetti +
                ", jonosija=" + jonosija +
                ", tasasijaJonosija=" + tasasijaJonosija +
                ", tila=" + tila +
                '}';
    }

    public String getSukunimi() {
        return sukunimi;
    }

    public void setSukunimi(String sukunimi) {
        this.sukunimi = sukunimi;
    }

    public String getEtunimi() {
        return etunimi;
    }

    public void setEtunimi(String etunimi) {
        this.etunimi = etunimi;
    }

    public boolean isHyvaksyttyHarkinnanvaraisesti() {
        return hyvaksyttyHarkinnanvaraisesti;
    }

    public void setHyvaksyttyHarkinnanvaraisesti(boolean hyvaksyttyHarkinnanvaraisesti) {
        this.hyvaksyttyHarkinnanvaraisesti = hyvaksyttyHarkinnanvaraisesti;
    }
}
