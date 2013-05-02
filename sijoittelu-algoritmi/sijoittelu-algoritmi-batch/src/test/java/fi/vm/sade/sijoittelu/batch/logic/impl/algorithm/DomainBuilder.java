package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;


import fi.vm.sade.service.sijoittelu.types.SijoitteleTyyppi;
import fi.vm.sade.service.valintaperusteet.schema.TasasijasaantoTyyppi;
import fi.vm.sade.service.valintatiedot.schema.HakijaTyyppi;
import fi.vm.sade.service.valintatiedot.schema.HakukohdeTyyppi;
import fi.vm.sade.service.valintatiedot.schema.ValinnanvaiheTyyppi;
import fi.vm.sade.service.valintatiedot.schema.ValintatapajonoTyyppi;

public class DomainBuilder {

	SijoitteleTyyppi sijoitteleTyyppi;

	public DomainBuilder(SijoitteleTyyppi sijoitteleTyyppi) {
		this.sijoitteleTyyppi = sijoitteleTyyppi;
	}

	public void addRow(String hakijanro, int prioriteetti, String linjannimi, String hakukohdeId, int aloituspaikat, float pisteet, String tila) {

		// etsi tai luo hakukohde
		HakukohdeTyyppi hakukohdeTyyppi = null;
		for (HakukohdeTyyppi hkt : sijoitteleTyyppi.getTarjonta().getHakukohde()) {
			// System.out.println("WTF: " + hkt + " " + hakukohdeId + "# " +
			// hkt.getOid());
			if (hkt.getOid().equals(hakukohdeId)) {
				hakukohdeTyyppi = hkt;
				break;
			}
		}
		if (hakukohdeTyyppi == null) {
			hakukohdeTyyppi = new HakukohdeTyyppi();
			hakukohdeTyyppi.setOid(hakukohdeId);
            ValinnanvaiheTyyppi v= new ValinnanvaiheTyyppi();
			ValintatapajonoTyyppi valintatapajonotyyppi = new ValintatapajonoTyyppi();
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
			valintatapajonotyyppi.setTasasijasaanto(TasasijasaantoTyyppi.ARVONTA);

            hakukohdeTyyppi.getValinnanvaihe().add(v);
            v.getValintatapajono().add(valintatapajonotyyppi);
			sijoitteleTyyppi.getTarjonta().getHakukohde().add(hakukohdeTyyppi);
		}

		// lisää ensimmäiseen valintatapajonoon
		ValintatapajonoTyyppi valintatapajonoTyyppi = hakukohdeTyyppi.getValinnanvaihe().get(0).getValintatapajono().get(0);
		HakijaTyyppi hakijatyyppi = new HakijaTyyppi();
		hakijatyyppi.setJonosija(-((int) pisteet));
		hakijatyyppi.setPisteet(pisteet);
		hakijatyyppi.setPrioriteetti(prioriteetti);
		hakijatyyppi.setOid(hakijanro);
		if (tila != null) {
			hakijatyyppi.setTila(tila);
		}

		valintatapajonoTyyppi.getHakija().add(hakijatyyppi);
	}

}
