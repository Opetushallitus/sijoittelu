package fi.vm.sade.sijoittelu.domain.dto;

import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.JsonViews;
import org.codehaus.jackson.map.annotate.JsonView;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: kkammone
 * Date: 2.9.2013
 * Time: 13:22
 * To change this template use File | Settings | File Templates.
 */
public class HakemusDTO implements Serializable {

    @JsonView({JsonViews.Hakemus.class,JsonViews.Hakukohde.class})
    private String hakijaOid;

    @JsonView({JsonViews.Hakemus.class,JsonViews.Hakukohde.class})
    private String hakemusOid;

    @JsonView({JsonViews.Hakemus.class,JsonViews.Hakukohde.class})
    private String etunimi;

    @JsonView({JsonViews.Hakemus.class,JsonViews.Hakukohde.class})
    private String sukunimi;

    @JsonView({JsonViews.Hakemus.class,JsonViews.Hakukohde.class})
    private Integer prioriteetti;

    @JsonView({JsonViews.Hakemus.class,JsonViews.Hakukohde.class})
    private Integer jonosija;

    @JsonView({JsonViews.Hakemus.class,JsonViews.Hakukohde.class})
    private Integer tasasijaJonosija;

    @JsonView({JsonViews.Hakemus.class,JsonViews.Hakukohde.class})
    private HakemuksenTila tila;

    @JsonView({JsonViews.Hakemus.class,JsonViews.Hakukohde.class})
    private boolean hyvaksyttyHarkinnanvaraisesti = false;

    @JsonView({JsonViews.Hakemus.class,JsonViews.Hakukohde.class})
    private Integer varasijanNumero;

    @JsonView(JsonViews.Hakemus.class)
    private Long sijoitteluajoId;

    @JsonView(JsonViews.Hakemus.class)
    private String hakukohdeOid;

    @JsonView(JsonViews.Hakemus.class)
    private String tarjoajaOid;

    @JsonView(JsonViews.Hakemus.class)
    private String valintatapajonoOid;

    @JsonView(JsonViews.Hakemus.class)
    private String hakuOid;

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
}
