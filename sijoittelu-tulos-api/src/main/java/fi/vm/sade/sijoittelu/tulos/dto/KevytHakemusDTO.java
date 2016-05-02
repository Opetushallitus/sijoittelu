package fi.vm.sade.sijoittelu.tulos.dto;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class KevytHakemusDTO {
    private String hakemusOid;
    private String hakijaOid;
    private Integer prioriteetti;
    private Integer varasijanNumero;
    private BigDecimal pisteet;
    private Integer jonosija;
    private HakemuksenTila tila;
    private Map<String, String> tilanKuvaukset = new HashMap<String, String>();
    private boolean hyvaksyttyHarkinnanvaraisesti;
    private Date viimeisenHakemuksenTilanMuutos;

    public KevytHakemusDTO() {};

    public String getHakemusOid() {
        return hakemusOid;
    }

    public void setHakemusOid(String hakemusOid) {
        this.hakemusOid = hakemusOid;
    }

    public String getHakijaOid() {
        return hakijaOid;
    }

    public void setHakijaOid(String hakijaOid) {
        this.hakijaOid = hakijaOid;
    }

    public Integer getPrioriteetti() {
        return prioriteetti;
    }

    public void setPrioriteetti(Integer prioriteetti) {
        this.prioriteetti = prioriteetti;
    }

    public Integer getVarasijanNumero() {
        return varasijanNumero;
    }

    public void setVarasijanNumero(Integer varasijanNumero) {
        this.varasijanNumero = varasijanNumero;
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

    public HakemuksenTila getTila() {
        return tila;
    }

    public void setTila(HakemuksenTila tila) {
        this.tila = tila;
    }

    public Map<String, String> getTilanKuvaukset() {
        return tilanKuvaukset;
    }

    public void setTilanKuvaukset(Map<String, String> tilanKuvaukset) {
        this.tilanKuvaukset = tilanKuvaukset;
    }

    public boolean isHyvaksyttyHarkinnanvaraisesti() {
        return hyvaksyttyHarkinnanvaraisesti;
    }

    public void setHyvaksyttyHarkinnanvaraisesti(boolean hyvaksyttyHarkinnanvaraisesti) {
        this.hyvaksyttyHarkinnanvaraisesti = hyvaksyttyHarkinnanvaraisesti;
    }

    public Date getViimeisenHakemuksenTilanMuutos() {
        return viimeisenHakemuksenTilanMuutos;
    }

    public void setViimeisenHakemuksenTilanMuutos(Date viimeisenHakemuksenTilanMuutos) {
        this.viimeisenHakemuksenTilanMuutos = viimeisenHakemuksenTilanMuutos;
    }
}
