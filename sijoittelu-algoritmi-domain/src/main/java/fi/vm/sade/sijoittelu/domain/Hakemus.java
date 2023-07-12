package fi.vm.sade.sijoittelu.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

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

    private Map<String, String> tilanKuvaukset = TilanKuvaukset.tyhja;

    private TilankuvauksenTarkenne tilankuvauksenTarkenne = TilankuvauksenTarkenne.EI_TILANKUVAUKSEN_TARKENNETTA;

    private Integer varasijanNumero;

    private Set<String> hyvaksyttyHakijaryhmista = new HashSet<>();

    private boolean siirtynytToisestaValintatapajonosta = false;

    private Boolean vastaanottoMyohassa = null;

    private Date vastaanottoDeadline = null;

    public Set<String> getHyvaksyttyHakijaryhmista() {
        return this.hyvaksyttyHakijaryhmista;
    }

    public void setHyvaksyttyHakijaryhmista(Set<String> hyvaksyttyHakijaryhmista) {
        this.hyvaksyttyHakijaryhmista = hyvaksyttyHakijaryhmista;
    }

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

    public Hakemus setHakemusOid(String hakemusOid) {
        this.hakemusOid = hakemusOid;
        return this;
    }

    @Override
    public String toString() {
        return "Hakemus{" +
                "hakijaOid='" + hakijaOid + '\'' +
                ", hakemusOid='" + hakemusOid + '\'' +
                ", etunimi='" + etunimi + '\'' +
                ", sukunimi='" + sukunimi + '\'' +
                ", prioriteetti=" + prioriteetti +
                ", jonosija=" + jonosija +
                ", onkoMuuttunutViimeSijoittelussa=" + onkoMuuttunutViimeSijoittelussa +
                ", pisteet=" + pisteet +
                ", tasasijaJonosija=" + tasasijaJonosija +
                ", tila=" + tila +
                ", edellinenTila=" + edellinenTila +
                ", ilmoittautumisTila=" + ilmoittautumisTila +
                ", tilaHistoria=" + tilaHistoria +
                ", hyvaksyttyHarkinnanvaraisesti=" + hyvaksyttyHarkinnanvaraisesti +
                ", pistetiedot=" + pistetiedot +
                ", tilanKuvaukset=" + tilanKuvaukset +
                ", tilankuvauksenTarkenne=" + tilankuvauksenTarkenne +
                ", varasijanNumero=" + varasijanNumero +
                ", hyvaksyttyHakijaryhmista=" + hyvaksyttyHakijaryhmista +
                ", siirtynytToisestaValintatapajonosta=" + siirtynytToisestaValintatapajonosta +
                ", vastaanottoMyohassa=" + vastaanottoMyohassa +
                ", vastaanottoDeadline=" + vastaanottoDeadline +
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

    public void setSiirtynytToisestaValintatapajonosta(boolean siirtynytToisestaValintatapajonosta) {
        this.siirtynytToisestaValintatapajonosta = siirtynytToisestaValintatapajonosta;
    }

    public boolean getSiirtynytToisestaValintatapajonosta() {
        return this.siirtynytToisestaValintatapajonosta;
    }

    public void setVastaanottoMyohassa(Boolean vastaanottoMyohassa) {
        this.vastaanottoMyohassa = vastaanottoMyohassa;
    }

    public Boolean isVastaanottoMyohassa() {
        return vastaanottoMyohassa;
    }

    public void setVastaanottoDeadline(Date vastaanottoDeadline) {
        this.vastaanottoDeadline = vastaanottoDeadline;
    }

    public Date getVastaanottoDeadline() {
        return vastaanottoDeadline;
    }

    public TilankuvauksenTarkenne getTilankuvauksenTarkenne() {
        return this.tilankuvauksenTarkenne;
    }

    public void setTilankuvauksenTarkenne(TilankuvauksenTarkenne tilankuvauksenTarkenne) {
        if (!tilankuvauksenTarkenne.vakioTilanKuvaus().isPresent()) {
            throw new IllegalArgumentException(String.format(
                    "Ei voida asettaa tilan kuvauksen tarkennetta %s ilman tilan kuvausta",
                    tilankuvauksenTarkenne
            ));
        }
        this.tilanKuvaukset = tilankuvauksenTarkenne.vakioTilanKuvaus().get();
        this.tilankuvauksenTarkenne = tilankuvauksenTarkenne;
    }

    public void setTilankuvauksenTarkenne(TilankuvauksenTarkenne tilankuvauksenTarkenne, Map<String, String> tilanKuvaukset) {
        this.tilankuvauksenTarkenne = tilankuvauksenTarkenne;
        this.tilanKuvaukset = tilanKuvaukset;
    }

    public void periTila(Hakemus hakemus) {
        this.tila = hakemus.getTila();
        this.tilankuvauksenTarkenne = hakemus.getTilankuvauksenTarkenne();
        this.tilanKuvaukset = hakemus.getTilanKuvaukset();
    }
}
