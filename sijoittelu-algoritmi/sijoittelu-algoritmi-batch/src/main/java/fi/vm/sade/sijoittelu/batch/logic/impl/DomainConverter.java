package fi.vm.sade.sijoittelu.batch.logic.impl;

import java.util.ArrayList;
import java.util.List;

import fi.vm.sade.service.sijoittelu.schema.HakijaryhmaTyyppi;
import fi.vm.sade.service.sijoittelu.schema.HakukohdeTyyppi;
import fi.vm.sade.service.sijoittelu.schema.ValintatapajonoTyyppi;
import fi.vm.sade.service.valintaperusteet.schema.SaantoTyyppi;
import fi.vm.sade.service.valintatiedot.schema.HakijaTyyppi;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakijaryhma;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.HakukohdeItem;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.Tasasijasaanto;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.sijoittelu.domain.ValintatapajonoTila;

/**
 * 
 * @author Kari Kammonen
 * 
 */
public class DomainConverter {

	/**
	 * Luo sijoittelun domaini ja palauta algorimi/domain instanssi
	 * 
	 * @param haku
	 * @param hakukohdeTyyppit
	 * @return
	 */
	public static SijoitteluAjo convertToSijoitteluAjo(List<HakukohdeTyyppi> hakukohdeTyypit) {
		List<HakukohdeItem> hakukohteet = createDomain(hakukohdeTyypit);
		SijoitteluAjo ajo = new SijoitteluAjo();
		ajo.getHakukohteet().addAll(hakukohteet);
		return ajo;
	}

	/**
	 * 
	 * @param hakukohdeTyyppit
	 * @param hakukohteet
	 */
	private static List<HakukohdeItem> createDomain(List<HakukohdeTyyppi> hakukohdeTyyppit) {
		// hakukohteet
		List<HakukohdeItem> hakukohdeItems = new ArrayList<HakukohdeItem>();

		for (HakukohdeTyyppi hakukohdeTyyppi : hakukohdeTyyppit) {
			Hakukohde hakukohde = new Hakukohde();
			hakukohde.setOid(hakukohdeTyyppi.getOid());

			HakukohdeItem hakukohdeItem = new HakukohdeItem();
			hakukohdeItem.setOid(hakukohdeTyyppi.getOid());
			hakukohdeItem.setHakukohde(hakukohde);

			hakukohdeItems.add(hakukohdeItem);

			addValintatapaJonos(hakukohdeTyyppi, hakukohde);
			addHakijaRyhmas(hakukohdeTyyppi, hakukohde);

		}
		return hakukohdeItems;
	}

	private static void addValintatapaJonos(HakukohdeTyyppi hakukohdeTyyppi, Hakukohde hakukohde) {
		for (ValintatapajonoTyyppi valintatapajonoTyyppi : hakukohdeTyyppi.getValintatapajono()) {
			Valintatapajono valintatapajono = new Valintatapajono();
			valintatapajono.setOid(valintatapajonoTyyppi.getOid());
			valintatapajono.setPrioriteetti(valintatapajonoTyyppi.getPrioriteetti());
			valintatapajono.setAloituspaikat(valintatapajonoTyyppi.getAloituspaikat());
			/*if (valintatapajonoTyyppi.getTila() != null) {
				valintatapajono.setTila(ValintatapajonoTila.valueOf(valintatapajonoTyyppi.getTila().name()));
			}*/

			/*
            for (SaantoTyyppi s : valintatapajonoTyyppi.getSaanto()) {
				Saanto saanto = new Saanto();
				saanto.setNimi(s.getNimi());
				saanto.setTyyppi(s.getTyyppi());
				saanto.getParameters().addAll(s.getParametri());
				valintatapajono.getSaannot().add(saanto);
			}
*/

			valintatapajono.setTasasijasaanto(Tasasijasaanto.valueOf(valintatapajonoTyyppi.getTasasijasaanto().toString()));

			hakukohde.getValintatapajonot().add(valintatapajono);

			for (HakijaTyyppi hakijaTyyppi : valintatapajonoTyyppi.getHakija()) {
				addHakemus(hakijaTyyppi, valintatapajono);
			}
		}
	}

	private static void addHakijaRyhmas(HakukohdeTyyppi hakukohdeTyyppi, Hakukohde hakukohde) {
		for (HakijaryhmaTyyppi h : hakukohdeTyyppi.getHakijaryhma()) {
			Hakijaryhma hakijaryhma = new Hakijaryhma();
			hakijaryhma.setPaikat(h.getPaikat());
			hakijaryhma.setNimi(h.getNimi());
			hakijaryhma.setOid(h.getOid());
			hakijaryhma.setPrioriteetti(h.getPrioriteetti());
			for (String s : h.getHakijaOid()) {
				hakijaryhma.getHakijaOid().add(s);
			}
			hakukohde.getHakijaryhmat().add(hakijaryhma);
		}
	}

	/**
	 * 
	 * Luo hakemus valintatapajonoon. Pitaa sisallaan kohteen prioriteetin
	 * hakijalle, sijan jonossa, pisteet, etc. Naita luodaan per valintatapajono
	 * per henkilo
	 * 
	 * @param hakijaTyyppi
	 * @param valintatapajono
	 * @return
	 */
	private static void addHakemus(HakijaTyyppi hakijaTyyppi, Valintatapajono valintatapajono) {
		Hakemus hakemus = new Hakemus();
		hakemus.setHakijaOid(hakijaTyyppi.getOid());
		hakemus.setJonosija(hakijaTyyppi.getJonosija());
		hakemus.setPrioriteetti(hakijaTyyppi.getPrioriteetti());
		if (hakijaTyyppi.getTila() == null || hakijaTyyppi.getTila().isEmpty()) {
			// kaikki aloittavat varalla oletuksena
			hakemus.setTila(HakemuksenTila.VARALLA);
		} else {
			hakemus.setTila(HakemuksenTila.valueOf(hakijaTyyppi.getTila()));
		}

		valintatapajono.getHakemukset().add(hakemus);
	}

}
