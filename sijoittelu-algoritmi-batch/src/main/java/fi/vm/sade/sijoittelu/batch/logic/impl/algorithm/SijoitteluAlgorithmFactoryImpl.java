package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fi.vm.sade.sijoittelu.domain.*;
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
	public SijoitteluAlgorithm constructAlgorithm(List<Hakukohde> hakukohteet, List<Valintatulos> valintatulokset) {

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
		algorithm.sijoitteluAjo = wrapDomain(hakukohteet, valintatulokset);
		return algorithm;
	}

	/**
	 * Luo sijoittelun tarvitsema domaini
	 * 
	 * @param hakukohteet
	 * @return
	 */
	private SijoitteluajoWrapper wrapDomain(List<Hakukohde> hakukohteet, List<Valintatulos> valintatulokset) {

		SijoitteluajoWrapper sijoitteluajoWrapper = new SijoitteluajoWrapper();
	//	sijoitteluajoWrapper.setSijoitteluajo(sijoitteluajo);

		HashMap<String, HenkiloWrapper> hakemusOidHenkiloMap = new HashMap<String, HenkiloWrapper>();

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

					HenkiloWrapper henkiloWrapper = getOrCreateHenkilo(hakemus.getHakijaOid(), hakemusOidHenkiloMap);
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
					HenkiloWrapper henkilo = getOrCreateHenkilo(oid, hakemusOidHenkiloMap);
					hakijaryhmaWrapper.getHenkiloWrappers().add(henkilo);
				}
			}
		}
		return sijoitteluajoWrapper;
	}

	private HenkiloWrapper getOrCreateHenkilo(String oid, HashMap<String, HenkiloWrapper> hakemusOidHenkiloMap) {
	    /*
			HenkiloWrapper henkiloWrapper = henkilot.get(oid);
		if (henkiloWrapper == null) {
			henkiloWrapper = new HenkiloWrapper();
			henkiloWrapper.setHakemusOid(oid);
			henkilot.put(oid, henkiloWrapper);
		}
		return henkiloWrapper;

		*/
        return null;
	}

}
