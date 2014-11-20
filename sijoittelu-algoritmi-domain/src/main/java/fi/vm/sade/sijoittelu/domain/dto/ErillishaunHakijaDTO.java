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
	private String valintatapajonoOid;
	@ApiModelProperty(value = "Hakemuksen tunniste", required = false)
	private String hakemusOid;
	@ApiModelProperty(value = "Hakukohteen tunniste", required = false)
	private String hakukohdeOid;
	@ApiModelProperty(value = "Saako hakijan tulokset julkaista", required = false)
	private boolean julkaistavissa;
	//private boolean hyvaksyttyVarasijalta;
	@ApiModelProperty(value = "Hakijan tunniste", required = false)
	private String hakijaOid;
	@ApiModelProperty(value = "Haun tunniste", required = false)
	private String hakuOid;
	@ApiModelProperty(value = "Tarjoajan (organisaation) tunniste", required = false)
	private String tarjoajaOid;
	//private int hakutoive; // aina ensimmainen?
	@ApiModelProperty(value = "Valintatuloksen tila", required = false)
	private ValintatuloksenTila valintatuloksenTila;
	@ApiModelProperty(value = "Ilmoittautumisen tila", required = false)
	private IlmoittautumisTila ilmoittautumisTila;
	@ApiModelProperty(value = "Hakemuksen tila", required = false)
	private HakemuksenTila hakemuksenTila;
	@ApiModelProperty(value = "Hakijan etunimi", required = false)
	private String etunimi;
	@ApiModelProperty(value = "Hakijan sukunimi", required = false)
	private String sukunimi;
	
	public ErillishaunHakijaDTO() {
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
	
	public boolean isJulkaistavissa() {
		return julkaistavissa;
	}

	public String getHakemusOid() {
		return hakemusOid;
	}

	public String getHakijaOid() {
		return hakijaOid;
	}

	public String getHakukohdeOid() {
		return hakukohdeOid;
	}

	public String getHakuOid() {
		return hakuOid;
	}

	public IlmoittautumisTila getIlmoittautumisTila() {
		return ilmoittautumisTila;
	}

	public String getTarjoajaOid() {
		return tarjoajaOid;
	}

	public HakemuksenTila getHakemuksenTila() {
		return hakemuksenTila;
	}

	public ValintatuloksenTila getValintatuloksenTila() {
		return valintatuloksenTila;
	}

	public String getValintatapajonoOid() {
		return valintatapajonoOid;
	}
	public void setHakemuksenTila(HakemuksenTila hakemuksenTila) {
		this.hakemuksenTila = hakemuksenTila;
	}
	public void setHakemusOid(String hakemusOid) {
		this.hakemusOid = hakemusOid;
	}
	public void setHakijaOid(String hakijaOid) {
		this.hakijaOid = hakijaOid;
	}
	public void setHakukohdeOid(String hakukohdeOid) {
		this.hakukohdeOid = hakukohdeOid;
	}
	public void setHakuOid(String hakuOid) {
		this.hakuOid = hakuOid;
	}
	public void setIlmoittautumisTila(IlmoittautumisTila ilmoittautumisTila) {
		this.ilmoittautumisTila = ilmoittautumisTila;
	}
	public void setJulkaistavissa(boolean julkaistavissa) {
		this.julkaistavissa = julkaistavissa;
	}
	public void setTarjoajaOid(String tarjoajaOid) {
		this.tarjoajaOid = tarjoajaOid;
	}
	public void setValintatapajonoOid(String valintatapajonoOid) {
		this.valintatapajonoOid = valintatapajonoOid;
	}
	public void setValintatuloksenTila(ValintatuloksenTila valintatuloksenTila) {
		this.valintatuloksenTila = valintatuloksenTila;
	}
	
}
