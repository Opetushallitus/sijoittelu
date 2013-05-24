package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Component;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor.PostSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessorSort;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessorTasasijaArvonta;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakijaryhmaWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakukohdeWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HenkiloWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakijaryhma;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.HakukohdeItem;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;

/**
 * 
 * @author Kari Kammonen
 * 
 */
@Component
public class SijoitteluAlgorithmFactoryImpl implements SijoitteluAlgorithmFactory {

	public SijoitteluAlgorithmFactoryImpl() {

	}

	/**
	 * Luo sijoittelualgoritmi.
	 */
	@Override
	public SijoitteluAlgorithm constructAlgorithm(List<Hakukohde> hakukohteet) {

		// private Map<String, Tasasijasaanto> tasasijasaannot = new
		// HashMap<String,
		// Tasasijasaanto>();
		// tasasijasaannot.put("ARVONTA", new TasasijasaantoOletus());
		// tasasijasaannot.put("YLITAYTTO", new TasasijasaantoYlitaytto());
		// tasasijasaannot.put("ALITAYTTO", new TasasijasaantoAlitaytto());

		List<PreSijoitteluProcessor> preSijoitteluProcessors = new ArrayList<PreSijoitteluProcessor>();
		preSijoitteluProcessors.add(new PreSijoitteluProcessorTasasijaArvonta());
		preSijoitteluProcessors.add(new PreSijoitteluProcessorSort());

		List<PostSijoitteluProcessor> postSijoitteluProcessors = new ArrayList<PostSijoitteluProcessor>();

		SijoitteluAlgorithmImpl algorithm = new SijoitteluAlgorithmImpl();
		// algorithm.tasasijasaannot = this.tasasijasaannot;
		algorithm.preSijoitteluProcessors = preSijoitteluProcessors;
		algorithm.postSijoitteluProcessors = postSijoitteluProcessors;
		algorithm.sijoitteluAjo = wrapDomain(hakukohteet);
		return algorithm;
	}

	/**
	 * Luo sijoittelun tarvitsema domaini
	 * 
	 * @param hakukohteet
	 * @return
	 */
	private SijoitteluajoWrapper wrapDomain(List<Hakukohde> hakukohteet) {

		SijoitteluajoWrapper sijoitteluajoWrapper = new SijoitteluajoWrapper();
	//	sijoitteluajoWrapper.setSijoitteluajo(sijoitteluajo);

		HashMap<String, HenkiloWrapper> henkilot = new HashMap<String, HenkiloWrapper>();

		for (Hakukohde hakukohde : hakukohteet) {
			HakukohdeWrapper hakukohdeWrapper = new HakukohdeWrapper();
			hakukohdeWrapper.setHakukohde(hakukohde);
			sijoitteluajoWrapper.getHakukohteet().add(hakukohdeWrapper);
			hakukohdeWrapper.setSijoitteluajoWrapper(sijoitteluajoWrapper);

			for (Valintatapajono valintatapajono : hakukohde.getValintatapajonot()) {
				ValintatapajonoWrapper valintatapajonoWrapper = new ValintatapajonoWrapper();
				valintatapajonoWrapper.setValintatapajono(valintatapajono);
				hakukohdeWrapper.getValintatapajonot().add(valintatapajonoWrapper);
				valintatapajonoWrapper.setHakukohdeWrapper(hakukohdeWrapper);

				for (Hakemus hakemus : valintatapajono.getHakemukset()) {
					HakemusWrapper hakemusWrapper = new HakemusWrapper();
					hakemusWrapper.setHakemus(hakemus);
					valintatapajonoWrapper.getHakemukset().add(hakemusWrapper);
					hakemusWrapper.setValintatapajono(valintatapajonoWrapper);

					HenkiloWrapper henkiloWrapper = getOrCreateHenkilo(hakemus.getHakijaOid(), henkilot);
					henkiloWrapper.getHakemukset().add(hakemusWrapper);
					hakemusWrapper.setHenkilo(henkiloWrapper);
				}
			}

			for (Hakijaryhma hakijaryhma : hakukohde.getHakijaryhmat()) {
				HakijaryhmaWrapper hakijaryhmaWrapper = new HakijaryhmaWrapper();
				hakijaryhmaWrapper.setHakijaryhma(hakijaryhma);
				hakijaryhmaWrapper.setHakukohdeWrapper(hakukohdeWrapper);
				hakukohdeWrapper.getHakijaryhmaWrappers().add(hakijaryhmaWrapper);
				for (String oid : hakijaryhma.getHakijaOid()) {
					HenkiloWrapper henkilo = getOrCreateHenkilo(oid, henkilot);
					hakijaryhmaWrapper.getHenkiloWrappers().add(henkilo);
				}
			}
		}
		return sijoitteluajoWrapper;
	}

	private HenkiloWrapper getOrCreateHenkilo(String oid, HashMap<String, HenkiloWrapper> henkilot) {
		HenkiloWrapper henkiloWrapper = henkilot.get(oid);
		if (henkiloWrapper == null) {
			henkiloWrapper = new HenkiloWrapper();
			henkiloWrapper.setHenkiloOid(oid);
			henkilot.put(oid, henkiloWrapper);
		}
		return henkiloWrapper;
	}

}
