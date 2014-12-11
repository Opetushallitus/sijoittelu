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
	public final String valintatapajonoOid;
	@ApiModelProperty(value = "Hakemuksen tunniste", required = false)
	public final String hakemusOid;
	@ApiModelProperty(value = "Hakukohteen tunniste", required = false)
	public final String hakukohdeOid;
	@ApiModelProperty(value = "Saako hakijan tulokset julkaista", required = false)
	public final boolean julkaistavissa;
	//public boolean hyvaksyttyVarasijalta;
	@ApiModelProperty(value = "Hakijan tunniste", required = false)
	public final String hakijaOid;
	@ApiModelProperty(value = "Haun tunniste", required = false)
	public final String hakuOid;
	@ApiModelProperty(value = "Tarjoajan (organisaation) tunniste", required = false)
	public final String tarjoajaOid;
	//public int hakutoive; // aina ensimmainen?
	@ApiModelProperty(value = "Valintatuloksen tila", required = false)
	public final ValintatuloksenTila valintatuloksenTila;
	@ApiModelProperty(value = "Ilmoittautumisen tila", required = false)
	public final IlmoittautumisTila ilmoittautumisTila;
	@ApiModelProperty(value = "Hakemuksen tila", required = false)
	public final HakemuksenTila hakemuksenTila;
	@ApiModelProperty(value = "Hakijan etunimi", required = false)
	public final String etunimi;
	@ApiModelProperty(value = "Hakijan sukunimi", required = false)
	public final String sukunimi;

	public ErillishaunHakijaDTO(final String valintatapajonoOid, final String hakemusOid, final String hakukohdeOid, final boolean julkaistavissa, final String hakijaOid, final String hakuOid, final String tarjoajaOid, final ValintatuloksenTila valintatuloksenTila, final IlmoittautumisTila ilmoittautumisTila, final HakemuksenTila hakemuksenTila, final String etunimi, final String sukunimi) {
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
