package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.*;
import fi.vm.sade.sijoittelu.domain.*;
import org.springframework.stereotype.Component;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor.PostSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessorSort;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessorTasasijaArvonta;

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

        List<PreSijoitteluProcessor> preSijoitteluProcessors = new ArrayList<PreSijoitteluProcessor>();
        preSijoitteluProcessors.add(new PreSijoitteluProcessorTasasijaArvonta());
        preSijoitteluProcessors.add(new PreSijoitteluProcessorSort());

        List<PostSijoitteluProcessor> postSijoitteluProcessors = new ArrayList<PostSijoitteluProcessor>();

        SijoitteluAlgorithmImpl algorithm = new SijoitteluAlgorithmImpl();
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

        Map<String, HenkiloWrapper> hakijaOidMap = new HashMap<String, HenkiloWrapper>();
        Map<String, HenkiloWrapper> hakemusOidMap = new HashMap<String, HenkiloWrapper>();

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

                    HenkiloWrapper henkiloWrapper = getOrCreateHenkilo(hakemus, hakijaOidMap, hakemusOidMap);
                    henkiloWrapper.getHakemukset().add(hakemusWrapper);
                    hakemusWrapper.setHenkilo(henkiloWrapper);

                    Valintatulos valintatulos = getValintatulos(hakukohde, valintatapajono, hakemus, valintatulokset);
                    if(valintatulos != null && valintatulos.getTila() != null)  {
                        ValintatuloksenTila tila =  valintatulos.getTila();
                        if(tila == ValintatuloksenTila.ILMOITETTU || tila == ValintatuloksenTila.VASTAANOTTANUT_LASNA || tila == ValintatuloksenTila.VASTAANOTTANUT_POISSAOLEVA)  {
                            hakemus.setTila(HakemuksenTila.HYVAKSYTTY);
                        } else if(tila== ValintatuloksenTila.PERUNUT) {
                            hakemus.setTila(HakemuksenTila.HYLATTY);
                        }
                        hakemusWrapper.setTilaVoidaanVaihtaa(false);
                        henkiloWrapper.getValintatulos().add(valintatulos);
                    }
                }
            } //TODO REFAKTOROI OIDIEN OSALTA, HENKILO JA HAKEMUS OIDIT AIKA SEKAVASTI
            for (Hakijaryhma hakijaryhma : hakukohde.getHakijaryhmat()) {
                HakijaryhmaWrapper hakijaryhmaWrapper = new HakijaryhmaWrapper();
                hakijaryhmaWrapper.setHakijaryhma(hakijaryhma);
                hakijaryhmaWrapper.setHakukohdeWrapper(hakukohdeWrapper);
                hakukohdeWrapper.getHakijaryhmaWrappers().add(hakijaryhmaWrapper);
                for (String oid : hakijaryhma.getHakijaOid()) {
                    HenkiloWrapper henkilo = getHenkilo(oid, hakijaOidMap);
                    hakijaryhmaWrapper.getHenkiloWrappers().add(henkilo);
                }
            }
        }
        return sijoitteluajoWrapper;
    }

    private HenkiloWrapper getOrCreateHenkilo(Hakemus hakemus, Map<String, HenkiloWrapper> hakijaOidMap, Map<String, HenkiloWrapper> hakemusOidMap) {
        HenkiloWrapper henkiloWrapper = null;

        if(hakemus.getHakemusOid() != null && !hakemus.getHakemusOid().isEmpty()) {
            henkiloWrapper =   hakemusOidMap.get(hakemus.getHakemusOid());
        }

        if(henkiloWrapper == null && hakemus.getHakijaOid() != null && !hakemus.getHakijaOid().isEmpty()) {
            henkiloWrapper = hakijaOidMap.get(hakemus.getHakijaOid());
        }

        if(henkiloWrapper == null) {
            henkiloWrapper = new HenkiloWrapper();
            henkiloWrapper.setHakemusOid(hakemus.getHakemusOid());
            henkiloWrapper.setHakijaOid(hakemus.getHakijaOid());
            if(hakemus.getHakemusOid() != null && !hakemus.getHakemusOid().isEmpty()) {
                hakemusOidMap.put(hakemus.getHakemusOid(), henkiloWrapper);
            }
            if(hakemus.getHakijaOid() != null && !hakemus.getHakijaOid().isEmpty()) {
                hakijaOidMap.put(hakemus.getHakijaOid(), henkiloWrapper);
            }
        }
        return henkiloWrapper;


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


    private HenkiloWrapper getHenkilo(String hakijaOid,  Map<String, HenkiloWrapper> hakijaOidMap ) {
        return hakijaOidMap.get(hakijaOid);
    }


}
