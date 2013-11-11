package fi.vm.sade.sijoittelu.domain;

import com.google.code.morphia.annotations.Converters;
import com.google.code.morphia.annotations.Embedded;
import fi.vm.sade.sijoittelu.domain.converter.BigDecimalConverter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Kari Kammonen
 * 
 */
@Embedded
@Converters(BigDecimalConverter.class)
public class Hakemus implements Serializable {

    private String hakijaOid;

    private String hakemusOid;

    private String etunimi;

    private String sukunimi;

    /**
     * defaulttina tosi epakorkea
     */
    private Integer prioriteetti;

    private Integer jonosija;

    // ensimmaisen jarjestyskriteerin pisteet
    private BigDecimal pisteet;

    private Integer tasasijaJonosija;

    private HakemuksenTila tila;

    private boolean hyvaksyttyHarkinnanvaraisesti = false;

    private List<Pistetieto> pistetiedot = new ArrayList<Pistetieto>();

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
        return "Hakemus{" + "hakemusOid='" + hakemusOid + '\'' + ", prioriteetti=" + prioriteetti + ", jonosija="
                + jonosija + ", tasasijaJonosija=" + tasasijaJonosija + ", tila=" + tila + '}';
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

    public void setPisteet(BigDecimal pisteet) {
        this.pisteet = pisteet;
    }

    public BigDecimal getPisteet() {
        return pisteet;
    }

    public List<Pistetieto> getPistetiedot() {
        return pistetiedot;
    }

    public void setPistetiedot(List<Pistetieto> pistetiedot) {
        this.pistetiedot = pistetiedot;
    }
}
