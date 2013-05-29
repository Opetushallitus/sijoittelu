package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import java.util.ArrayList;
import java.util.List;

import fi.vm.sade.sijoittelu.domain.*;
import org.springframework.stereotype.Component;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor.PostSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessorSort;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessorTasasijaArvonta;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
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

        List<HenkiloWrapper> henkilot = new ArrayList<HenkiloWrapper>();

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

                    HenkiloWrapper henkiloWrapper = getOrCreateHenkilo(hakemus, henkilot);
                    henkiloWrapper.getHakemukset().add(hakemusWrapper);
                    hakemusWrapper.setHenkilo(henkiloWrapper);

                    Valintatulos valintatulos = getValintatulos(hakukohde, valintatapajono, hakemus, valintatulokset);
                    if(valintatulos != null && valintatulos.getTila() != null)  {
                        ValintatuloksenTila tila =  valintatulos.getTila();
                        if(tila == ValintatuloksenTila.ILMOITETTU || tila == ValintatuloksenTila.VASTAANOTTANUT_LASNA || tila == ValintatuloksenTila.VASTAANOTTANUT_POISSAOLEVA)  {
                            hakemus.setTila(HakemuksenTila.HYVAKSYTTY);
                        } else if(tila== ValintatuloksenTila.PERUNUT) {
                            hakemus.setTila(HakemuksenTila.VARALLA);
                        }
                        hakemusWrapper.setTilaVoidaanVaihtaa(false);
                        henkiloWrapper.getValintatulos().add(valintatulos);
                    }
                }
            }
            //TODO enabloi hakijaryhmat takaisin myohemmin
            /*
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
			*/
        }
        return sijoitteluajoWrapper;
    }

    private Valintatulos getValintatulos(Hakukohde hakukohde, Valintatapajono valintatapajono, Hakemus hakemus, List<Valintatulos> valintatulokset) {
        if(valintatulokset != null) {
            for(Valintatulos vt : valintatulokset) {
                if(vt.getHakukohdeOid().equals(hakukohde.getOid()) && vt.getValintatapajonoOid().equals(valintatapajono.getOid()) ) {
                    if( (vt.getHakijaOid() != null && !vt.getHakijaOid().isEmpty() && vt.getHakijaOid().equals(hakemus.getHakijaOid()) ) ||
                            vt.getHakemusOid() != null && !vt.getHakemusOid().isEmpty() && vt.getHakemusOid().equals(hakemus.getHakemusOid()) ) {
                        return vt;
                    }
                }
            }
        }
        return null;
    }


    private HenkiloWrapper getOrCreateHenkilo(Hakemus hakemus, List<HenkiloWrapper> henkilot) {
        for(HenkiloWrapper h : henkilot) {
            if(hakemus.getHakemusOid() != null && !hakemus.getHakemusOid().isEmpty() && hakemus.getHakemusOid().equals(h.getHakemusOid()))  {
                return h;
            }
            if(hakemus.getHakijaOid() != null && !hakemus.getHakijaOid().isEmpty() && hakemus.getHakijaOid().equals(h.getHakijaOid()))  {
                return h;
            }
        }
        HenkiloWrapper henkiloWrapper = new HenkiloWrapper();
        henkiloWrapper.setHakemusOid(hakemus.getHakemusOid());
        henkiloWrapper.setHakijaOid(hakemus.getHakijaOid());
        return henkiloWrapper;
    }

}
