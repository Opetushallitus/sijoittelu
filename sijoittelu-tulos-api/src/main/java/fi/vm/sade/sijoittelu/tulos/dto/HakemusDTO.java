package fi.vm.sade.sijoittelu.tulos.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA. User: kkammone Date: 2.9.2013 Time: 13:22 To
 * change this template use File | Settings | File Templates.
 */
public class HakemusDTO implements Serializable {

    private String hakijaOid;

    private String hakemusOid;

    private BigDecimal pisteet;

    private BigDecimal paasyJaSoveltuvuusKokeenTulos;

    private String etunimi;

    private String sukunimi;

    private Integer prioriteetti;

    private Integer jonosija;

    private Integer tasasijaJonosija;

    private HakemuksenTila tila;

    private Map<String,String> tilanKuvaukset = new HashMap<String,String>();

    private List<TilaHistoriaDTO> tilaHistoria = new ArrayList<TilaHistoriaDTO>();

    private boolean hyvaksyttyHarkinnanvaraisesti = false;

    private Integer varasijanNumero;

    private Long sijoitteluajoId;

    private String hakukohdeOid;

    private String tarjoajaOid;

    private String valintatapajonoOid;

    private String hakuOid;

    public int getTodellinenJonosija() {
    	if(jonosija == null) {
    		return 0;
    	} else {
    		if(tasasijaJonosija == null) {
    			return jonosija;
    		}
    		return jonosija + tasasijaJonosija - 1;
    	}
    }
    
    private List<PistetietoDTO> pistetiedot = new ArrayList<PistetietoDTO>();
   /*
    public String getValintatuloksenTila() {
        return valintatuloksenTila;
    }

    public void setValintatuloksenTila(String valintatuloksenTila) {
        this.valintatuloksenTila = valintatuloksenTila;
    }
     */
    public String getHakuOid() {
        return hakuOid;
    }

    public void setHakuOid(String hakuOid) {
        this.hakuOid = hakuOid;
    }

    public String getValintatapajonoOid() {
        return valintatapajonoOid;
    }

    public void setValintatapajonoOid(String valintatapajonoOid) {
        this.valintatapajonoOid = valintatapajonoOid;
    }

    public String getTarjoajaOid() {
        return tarjoajaOid;
    }

    public void setTarjoajaOid(String tarjoajaOid) {
        this.tarjoajaOid = tarjoajaOid;
    }

    public String getHakukohdeOid() {
        return hakukohdeOid;
    }

    public void setHakukohdeOid(String hakukohdeOid) {
        this.hakukohdeOid = hakukohdeOid;
    }

    public Long getSijoitteluajoId() {
        return sijoitteluajoId;
    }

    public void setSijoitteluajoId(Long sijoitteluajoId) {
        this.sijoitteluajoId = sijoitteluajoId;
    }

    public boolean isHyvaksyttyHarkinnanvaraisesti() {
        return hyvaksyttyHarkinnanvaraisesti;
    }

    public BigDecimal getPaasyJaSoveltuvuusKokeenTulos() {
        return paasyJaSoveltuvuusKokeenTulos;
    }

    public void setPaasyJaSoveltuvuusKokeenTulos(BigDecimal paasyJaSoveltuvuusKokeenTulos) {
        this.paasyJaSoveltuvuusKokeenTulos = paasyJaSoveltuvuusKokeenTulos;
    }

    public void setHyvaksyttyHarkinnanvaraisesti(boolean hyvaksyttyHarkinnanvaraisesti) {
        this.hyvaksyttyHarkinnanvaraisesti = hyvaksyttyHarkinnanvaraisesti;
    }

    public HakemuksenTila getTila() {
        return tila;
    }

    public void setTila(HakemuksenTila tila) {
        this.tila = tila;
    }

    public Integer getTasasijaJonosija() {
        return tasasijaJonosija;
    }

    public void setTasasijaJonosija(Integer tasasijaJonosija) {
        this.tasasijaJonosija = tasasijaJonosija;
    }

    public Integer getJonosija() {
        return jonosija;
    }

    public void setJonosija(Integer jonosija) {
        this.jonosija = jonosija;
    }

    public Integer getPrioriteetti() {
        return prioriteetti;
    }

    public void setPrioriteetti(Integer prioriteetti) {
        this.prioriteetti = prioriteetti;
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

    public List<PistetietoDTO> getPistetiedot() {
        return pistetiedot;
    }

    public List<TilaHistoriaDTO> getTilaHistoria() {
        return tilaHistoria;
    }

    public void setTilaHistoria(List<TilaHistoriaDTO> tilaHistoria) {
        this.tilaHistoria = tilaHistoria;
    }

    public Map<String, String> getTilanKuvaukset() {
        return tilanKuvaukset;
    }

    public void setTilanKuvaukset(Map<String, String> tilanKuvaukset) {
        this.tilanKuvaukset = tilanKuvaukset;
    }
}
