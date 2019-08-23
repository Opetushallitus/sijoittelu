package fi.vm.sade.sijoittelu.domain;

import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private Map<String, String> tilanKuvaukset = new HashMap<String, String>();

    private TilankuvauksenTarkenne tilankuvauksenTarkenne;

    private String tarkenteenLisatieto;

    private Integer varasijanNumero;

    private Set<String> hyvaksyttyHakijaryhmista = new HashSet<>();

    private boolean siirtynytToisestaValintatapajonosta = false;

    private final List<Pair<TilankuvauksenTarkenne, Map<String, String>>> TARKENTEET_JA_KUVAUKSET = Arrays.asList(
            Pair.of(TilankuvauksenTarkenne.PERUUNTUNUT_HYVAKSYTTY_YLEMMALLE_HAKUTOIVEELLE, TilanKuvaukset.peruuntunutYlempiToive()),
            Pair.of(TilankuvauksenTarkenne.PERUUNTUNUT_ALOITUSPAIKAT_TAYNNA, TilanKuvaukset.peruuntunutAloituspaikatTaynna()),
            Pair.of(TilankuvauksenTarkenne.PERUUNTUNUT_HYVAKSYTTY_TOISESSA_JONOSSA, TilanKuvaukset.peruuntunutHyvaksyttyToisessaJonossa()),
            Pair.of(TilankuvauksenTarkenne.HYVAKSYTTY_VARASIJALTA, TilanKuvaukset.varasijaltaHyvaksytty()),
            Pair.of(TilankuvauksenTarkenne.PERUUNTUNUT_EI_VASTAANOTTANUT_MAARAAIKANA, TilanKuvaukset.peruuntunutEiVastaanottanutMaaraaikana()),
            Pair.of(TilankuvauksenTarkenne.PERUUNTUNUT_VASTAANOTTANUT_TOISEN_PAIKAN, TilanKuvaukset.peruuntunutVastaanottanutToisenOpiskelupaikan()),
            Pair.of(TilankuvauksenTarkenne.PERUUNTUNUT_EI_MAHDU_VARASIJOJEN_MAARAAN, TilanKuvaukset.peruuntunutEiMahduKasiteltavienVarasijojenMaaraan()),
            Pair.of(TilankuvauksenTarkenne.PERUUNTUNUT_HAKUKIERROS_PAATTYNYT, TilanKuvaukset.peruuntunutHakukierrosOnPaattynyt()),
            Pair.of(TilankuvauksenTarkenne.PERUUNTUNUT_EI_VARASIJATAYTTOA, TilanKuvaukset.peruuntunutEiVarasijaTayttoa()),
            Pair.of(TilankuvauksenTarkenne.HYVAKSYTTY_TAYTTOJONO_SAANNOLLA, TilanKuvaukset.hyvaksyttyTayttojonoSaannolla(getTarkenteenLisatieto())),
            Pair.of(TilankuvauksenTarkenne.HYLATTY_HAKIJARYHMAAN_KUULUMATTOMANA, TilanKuvaukset.hylattyHakijaryhmaanKuulumattomana(getTarkenteenLisatieto())),
            Pair.of(TilankuvauksenTarkenne.PERUUNTUNUT_VASTAANOTTANUT_TOISEN_PAIKAN_YHDEN_SAANNON_PAIKAN_PIIRISSA, TilanKuvaukset.peruuntunutVastaanottanutToisenOpiskelupaikanYhdenPaikanSaannonPiirissa()),
            Pair.of(TilankuvauksenTarkenne.PERUUNTUNUT_HYVAKSYTTY_ALEMMALLE_HAKUTOIVEELLE, TilanKuvaukset.peruuntunutHyvaksyttyAlemmallaHakutoiveella()),
            Pair.of(TilankuvauksenTarkenne.EI_TILANKUVAUKSEN_TARKENNETTA, TilanKuvaukset.tyhja)
    );

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
                ", tarkenteenLisatieto='" + tarkenteenLisatieto + '\'' +
                ", varasijanNumero=" + varasijanNumero +
                ", hyvaksyttyHakijaryhmista=" + hyvaksyttyHakijaryhmista +
                ", siirtynytToisestaValintatapajonosta=" + siirtynytToisestaValintatapajonosta +
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

    public void clearTilanKuvaukset() {
        this.tilanKuvaukset = Collections.emptyMap();
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

    public TilankuvauksenTarkenne getTilankuvauksenTarkenne() {
        return null == tilankuvauksenTarkenne ? findTilankuvauksenTarkenne(tilanKuvaukset) : tilankuvauksenTarkenne;
    }

    public void setTilankuvauksenTarkenne(TilankuvauksenTarkenne tilankuvauksenTarkenne) {
        this.tilankuvauksenTarkenne = tilankuvauksenTarkenne;
        setTilanKuvaukset(findTilanKuvaukset(tilankuvauksenTarkenne));
    }

    public String getTarkenteenLisatieto() {
        return tarkenteenLisatieto == null ? getTilankuvauksenTarkenteenLisatieto(tilankuvauksenTarkenne, tilanKuvaukset) : tarkenteenLisatieto;
    }

    public void setTarkenteenLisatieto(String tarkenteenLisatieto) {
        this.tarkenteenLisatieto = tarkenteenLisatieto;
    }

    public void periTila(Hakemus hakemus) {
        this.tila = hakemus.getTila();
        this.tilankuvauksenTarkenne = hakemus.getTilankuvauksenTarkenne();
        this.tilanKuvaukset = hakemus.getTilanKuvaukset();
    }

    private String getTilankuvauksenTarkenteenLisatieto(TilankuvauksenTarkenne tarkenne, Map<String, String> tilanKuvaukset) {
        if (tarkenne == null) return null;
        else if (tarkenne.equals(TilankuvauksenTarkenne.HYVAKSYTTY_TAYTTOJONO_SAANNOLLA) || tarkenne.equals(TilankuvauksenTarkenne.HYLATTY_HAKIJARYHMAAN_KUULUMATTOMANA)) {
            String tkFi = tilanKuvaukset.get("FI");
            return tkFi.substring(tkFi.lastIndexOf(":") + 1).trim();
        } else return null;
    }

    private Map<String, String> findTilanKuvaukset(TilankuvauksenTarkenne tilankuvauksenTarkenne) {
        if (tilankuvauksenTarkenne != null) {
            return TARKENTEET_JA_KUVAUKSET.stream().filter(p -> p.getLeft().equals(tilankuvauksenTarkenne))
                    .findFirst().map(m -> m.getRight()).orElse(TilanKuvaukset.tyhja);
        }
        return TilanKuvaukset.tyhja;
    }

    private TilankuvauksenTarkenne findTilankuvauksenTarkenne(Map<String, String> tilanKuvaukset) {
        if (tilanKuvaukset != null && tilanKuvaukset.get("FI") != null) {
            return TARKENTEET_JA_KUVAUKSET.stream().filter(p -> p.getRight().containsKey("FI") && p.getRight().get("FI").equals(tilanKuvaukset.get("FI")))
                .findFirst().map(m -> m.getLeft()).orElse(TilankuvauksenTarkenne.EI_TILANKUVAUKSEN_TARKENNETTA);
        }
        return TilankuvauksenTarkenne.EI_TILANKUVAUKSEN_TARKENNETTA;
    }
}
