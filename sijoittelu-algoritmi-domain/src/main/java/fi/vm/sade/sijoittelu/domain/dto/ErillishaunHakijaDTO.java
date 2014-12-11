package fi.vm.sade.sijoittelu.domain.dto;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;

import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.IlmoittautumisTila;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;

/**
 * 
 * @author Jussi Jartamo
 * 
 */
@ApiModel("Erillishaunhakija")
public class ErillishaunHakijaDTO {
	@ApiModelProperty(value = "Valintatapajonon tunniste", required = true)
	public  String valintatapajonoOid;
	@ApiModelProperty(value = "Hakemuksen tunniste", required = false)
    public  String hakemusOid;
	@ApiModelProperty(value = "Hakukohteen tunniste", required = false)
    public  String hakukohdeOid;
	@ApiModelProperty(value = "Saako hakijan tulokset julkaista", required = false)
    public  boolean julkaistavissa;
	//public boolean hyvaksyttyVarasijalta;
	@ApiModelProperty(value = "Hakijan tunniste", required = false)
    public  String hakijaOid;
	@ApiModelProperty(value = "Haun tunniste", required = false)
    public  String hakuOid;
	@ApiModelProperty(value = "Tarjoajan (organisaation) tunniste", required = false)
    public  String tarjoajaOid;
	//public int hakutoive; // aina ensimmainen?
	@ApiModelProperty(value = "Valintatuloksen tila", required = false)
    public  ValintatuloksenTila valintatuloksenTila;
	@ApiModelProperty(value = "Ilmoittautumisen tila", required = false)
    public  IlmoittautumisTila ilmoittautumisTila;
	@ApiModelProperty(value = "Hakemuksen tila", required = false)
    public  HakemuksenTila hakemuksenTila;
	@ApiModelProperty(value = "Hakijan etunimi", required = false)
    public  String etunimi;
	@ApiModelProperty(value = "Hakijan sukunimi", required = false)
    public  String sukunimi;

	public ErillishaunHakijaDTO( String valintatapajonoOid,  String hakemusOid,  String hakukohdeOid,  boolean julkaistavissa,  String hakijaOid,  String hakuOid,  String tarjoajaOid,  ValintatuloksenTila valintatuloksenTila,  IlmoittautumisTila ilmoittautumisTila,  HakemuksenTila hakemuksenTila,  String etunimi,  String sukunimi) {
		this.valintatapajonoOid = valintatapajonoOid;
		this.hakemusOid = hakemusOid;
		this.hakukohdeOid = hakukohdeOid;
		this.julkaistavissa = julkaistavissa;
		this.hakijaOid = hakijaOid;
		this.hakuOid = hakuOid;
		this.tarjoajaOid = tarjoajaOid;
		this.valintatuloksenTila = valintatuloksenTila;
		this.ilmoittautumisTila = ilmoittautumisTila;
		this.hakemuksenTila = hakemuksenTila;
		this.etunimi = etunimi;
		this.sukunimi = sukunimi;
	}

    public ErillishaunHakijaDTO() {}

    public String getValintatapajonoOid() {
        return valintatapajonoOid;
    }

    public void setValintatapajonoOid(String valintatapajonoOid) {
        this.valintatapajonoOid = valintatapajonoOid;
    }

    public String getHakemusOid() {
        return hakemusOid;
    }

    public void setHakemusOid(String hakemusOid) {
        this.hakemusOid = hakemusOid;
    }

    public String getHakukohdeOid() {
        return hakukohdeOid;
    }

    public void setHakukohdeOid(String hakukohdeOid) {
        this.hakukohdeOid = hakukohdeOid;
    }

    public boolean isJulkaistavissa() {
        return julkaistavissa;
    }

    public void setJulkaistavissa(boolean julkaistavissa) {
        this.julkaistavissa = julkaistavissa;
    }

    public String getHakijaOid() {
        return hakijaOid;
    }

    public void setHakijaOid(String hakijaOid) {
        this.hakijaOid = hakijaOid;
    }

    public String getHakuOid() {
        return hakuOid;
    }

    public void setHakuOid(String hakuOid) {
        this.hakuOid = hakuOid;
    }

    public String getTarjoajaOid() {
        return tarjoajaOid;
    }

    public void setTarjoajaOid(String tarjoajaOid) {
        this.tarjoajaOid = tarjoajaOid;
    }

    public ValintatuloksenTila getValintatuloksenTila() {
        return valintatuloksenTila;
    }

    public void setValintatuloksenTila(ValintatuloksenTila valintatuloksenTila) {
        this.valintatuloksenTila = valintatuloksenTila;
    }

    public IlmoittautumisTila getIlmoittautumisTila() {
        return ilmoittautumisTila;
    }

    public void setIlmoittautumisTila(IlmoittautumisTila ilmoittautumisTila) {
        this.ilmoittautumisTila = ilmoittautumisTila;
    }

    public HakemuksenTila getHakemuksenTila() {
        return hakemuksenTila;
    }

    public void setHakemuksenTila(HakemuksenTila hakemuksenTila) {
        this.hakemuksenTila = hakemuksenTila;
    }

    public String getEtunimi() {
        return etunimi;
    }

    public void setEtunimi(String etunimi) {
        this.etunimi = etunimi;
    }

    public String getSukunimi() {
        return sukunimi;
    }

    public void setSukunimi(String sukunimi) {
        this.sukunimi = sukunimi;
    }

    public Valintatulos asValintatulos() {
		Valintatulos v = new Valintatulos();
		v.setHakemusOid(hakemusOid);
		v.setHakijaOid(hakijaOid);
		v.setHakukohdeOid(hakukohdeOid);
		v.setHakuOid(hakuOid);
		v.setHakutoive(1);// aina ensimmainen?
		v.setHyvaksyttyVarasijalta(HakemuksenTila.VARASIJALTA_HYVAKSYTTY.equals(hakemuksenTila));
		v.setIlmoittautumisTila(ilmoittautumisTila);
		v.setJulkaistavissa(julkaistavissa);
		v.setTila(valintatuloksenTila);
		v.setValintatapajonoOid(valintatapajonoOid);
		return v;
	}
}
