package fi.vm.sade.sijoittelu.domain;

import org.mongodb.morphia.annotations.Converters;
import org.mongodb.morphia.annotations.Embedded;
import fi.vm.sade.sijoittelu.domain.converter.BigDecimalConverter;
import org.mongodb.morphia.annotations.Indexed;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

@Embedded
@Converters(BigDecimalConverter.class)
public class Hakemus implements Serializable {
    private String hakijaOid;

    @Indexed
    private String hakemusOid;

    private String etunimi;

    private String sukunimi;

    /**
     * defaulttina tosi epakorkea
     */
    private Integer prioriteetti;

    private Integer jonosija;

    private Boolean onkoMuuttunutViimeSijoittelussa;
    // ensimmaisen jarjestyskriteerin pisteet
    private BigDecimal pisteet;

    private Integer tasasijaJonosija;

    private HakemuksenTila tila;

    private HakemuksenTila edellinenTila;

    private IlmoittautumisTila ilmoittautumisTila;

    private List<TilaHistoria> tilaHistoria = new ArrayList<TilaHistoria>();

    private boolean hyvaksyttyHarkinnanvaraisesti = false;

    private List<Pistetieto> pistetiedot = new ArrayList<Pistetieto>();

    private Map<String,String> tilanKuvaukset = new HashMap<String,String>();

    private Integer varasijanNumero;

    public Integer getPrioriteetti() {
        return prioriteetti;
    }

    public void setPrioriteetti(int prioriteetti) {
        this.prioriteetti = prioriteetti;
    }

    public Integer getJonosija() {
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

    public Boolean isOnkoMuuttunutViimeSijoittelussa() {
        return Boolean.TRUE.equals(onkoMuuttunutViimeSijoittelussa);
    }

    public void setOnkoMuuttunutViimeSijoittelussa(boolean onkoMuuttunutViimeSijoittelussa) {
        this.onkoMuuttunutViimeSijoittelussa = onkoMuuttunutViimeSijoittelussa;
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

    public List<TilaHistoria> getTilaHistoria() {
        return tilaHistoria;
    }

    public void setTilaHistoria(List<TilaHistoria> tilaHistoria) {
        this.tilaHistoria = tilaHistoria;
    }

    public Map<String, String> getTilanKuvaukset() {
        return tilanKuvaukset;
    }

    public void setTilanKuvaukset(Map<String, String> tilanKuvaukset) {
        this.tilanKuvaukset = tilanKuvaukset;
    }

    public IlmoittautumisTila getIlmoittautumisTila() {
        return ilmoittautumisTila;
    }

    public void setIlmoittautumisTila(IlmoittautumisTila ilmoittautumisTila) {
        this.ilmoittautumisTila = ilmoittautumisTila;
    }

    public Integer getVarasijanNumero() {
        return varasijanNumero;
    }

    public void setVarasijanNumero(Integer varasijanNumero) {
        this.varasijanNumero = varasijanNumero;
    }

    public HakemuksenTila getEdellinenTila() {
        return edellinenTila;
    }

    public void setEdellinenTila(HakemuksenTila edellinenTila) {
        this.edellinenTila = edellinenTila;
    }
}
