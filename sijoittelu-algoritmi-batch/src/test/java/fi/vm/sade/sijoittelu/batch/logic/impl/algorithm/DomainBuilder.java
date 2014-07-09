package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import fi.vm.sade.valintalaskenta.domain.dto.HakijaDTO;
import fi.vm.sade.valintalaskenta.domain.dto.HakukohdeDTO;
import fi.vm.sade.valintalaskenta.domain.dto.JarjestyskriteerituloksenTilaDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintakoe.Tasasijasaanto;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.ValintatietoValinnanvaiheDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.ValintatietoValintatapajonoDTO;

import java.math.BigDecimal;

public class DomainBuilder {

    HakuDTO sijoitteleTyyppi;

	public DomainBuilder(HakuDTO sijoitteleTyyppi) {
		this.sijoitteleTyyppi = sijoitteleTyyppi;
	}

	public void addRow(String hakijanro, int prioriteetti, String linjannimi, String hakukohdeId, int aloituspaikat, float pisteet, String tila) {

		// etsi tai luo hakukohde
		HakukohdeDTO hakukohdeTyyppi = null;
		for (HakukohdeDTO hkt : sijoitteleTyyppi.getHakukohteet()) {
			// System.out.println("WTF: " + hkt + " " + hakukohdeId + "# " +
			// hkt.getOid());
			if (hkt.getOid().equals(hakukohdeId)) {
				hakukohdeTyyppi = hkt;
				break;
			}
		}
		if (hakukohdeTyyppi == null) {
			hakukohdeTyyppi = new HakukohdeDTO();
			hakukohdeTyyppi.setOid(hakukohdeId);
            ValintatietoValinnanvaiheDTO v= new ValintatietoValinnanvaiheDTO();
			ValintatietoValintatapajonoDTO valintatapajonotyyppi = new ValintatietoValintatapajonoDTO();
			valintatapajonotyyppi.setAloituspaikat(20);
			valintatapajonotyyppi.setOid(hakukohdeId + "_jono1");
			valintatapajonotyyppi.setPrioriteetti(1);
			valintatapajonotyyppi.setAloituspaikat(aloituspaikat);
			valintatapajonotyyppi.setSiirretaanSijoitteluun(true);
			//
			// SaantoTyyppi saantoTyyppi = new SaantoTyyppi();
			// saantoTyyppi.setNimi("ARVONTA");
			// saantoTyyppi.setTyyppi("TASASIJA");
			// valintatapajonotyyppi.getSaanto().add(saantoTyyppi);
			valintatapajonotyyppi.setTasasijasaanto(Tasasijasaanto.ARVONTA);

            hakukohdeTyyppi.getValinnanvaihe().add(v);
            v.getValintatapajonot().add(valintatapajonotyyppi);
			sijoitteleTyyppi.getHakukohteet().add(hakukohdeTyyppi);
		}

		// lisää ensimmäiseen valintatapajonoon
        ValintatietoValintatapajonoDTO valintatapajonoTyyppi = hakukohdeTyyppi.getValinnanvaihe().get(0).getValintatapajonot().get(0);

        HakijaDTO hakijatyyppi = new HakijaDTO();
		hakijatyyppi.setJonosija(-((int) pisteet));
		hakijatyyppi.setPisteet(new BigDecimal(pisteet));
		hakijatyyppi.setPrioriteetti(prioriteetti);
		hakijatyyppi.setOid(hakijanro);
        hakijatyyppi.setTila(JarjestyskriteerituloksenTilaDTO.HYVAKSYTTAVISSA);


		valintatapajonoTyyppi.getHakija().add(hakijatyyppi);
	}

}
