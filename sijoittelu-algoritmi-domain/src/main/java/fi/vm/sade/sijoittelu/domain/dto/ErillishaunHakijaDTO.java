package fi.vm.sade.sijoittelu.domain.dto;

import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.IlmoittautumisTila;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;

/**
 * 
 * @author Jussi Jartamo
 * 
 */
public class ErillishaunHakijaDTO {

	private String valintatapajonoOid;
	private String hakemusOid;
	private String hakukohdeOid;
	private boolean julkaistavissa;
	private boolean hyvaksyttyVarasijalta;
	private String hakijaOid;
	private String hakuOid;
	private String tarjoajaOid;
	//private int hakutoive; // aina ensimmainen?
	private ValintatuloksenTila valintatuloksenTila;
	private IlmoittautumisTila ilmoittautumisTila;
	private HakemuksenTila hakemuksenTila;

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
	
	public boolean isHyvaksyttyVarasijalta() {
		return hyvaksyttyVarasijalta;
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
}
