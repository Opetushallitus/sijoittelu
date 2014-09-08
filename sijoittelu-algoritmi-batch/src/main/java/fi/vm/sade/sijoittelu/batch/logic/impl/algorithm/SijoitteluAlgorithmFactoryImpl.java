package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor.PostSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessorPeruutaAlemmatPeruneetJaHyvaksytyt;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessorSort;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessorTasasijaArvonta;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.*;
import fi.vm.sade.sijoittelu.domain.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        preSijoitteluProcessors.add(new PreSijoitteluProcessorPeruutaAlemmatPeruneetJaHyvaksytyt());


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

                    HenkiloWrapper henkiloWrapper = getOrCreateHenkilo(hakemus, hakemusOidMap);
                    henkiloWrapper.getHakemukset().add(hakemusWrapper);
                    hakemusWrapper.setHenkilo(henkiloWrapper);

                    Valintatulos valintatulos = getValintatulos(hakukohde, valintatapajono, hakemus, valintatulokset);

                    if(valintatulos != null && valintatulos.getTila() != null)  {
                        ValintatuloksenTila tila =  valintatulos.getTila();
                        if(tila == ValintatuloksenTila.ILMOITETTU || tila == ValintatuloksenTila.VASTAANOTTANUT)  {
                            if (hakemus.getTila() == HakemuksenTila.VARALLA) {
                                hakemus.getTilanKuvaukset().put("FI","Varasijalta hyväksytty");
                                hakemus.getTilanKuvaukset().put("SV","Varasijalta hyväksytty");
                                hakemus.getTilanKuvaukset().put("EN","Varasijalta hyväksytty");
                            }

                            hakemus.setTila(HakemuksenTila.HYVAKSYTTY);
                            hakemus.setIlmoittautumisTila(valintatulos.getIlmoittautumisTila());
                        } else if(tila == ValintatuloksenTila.PERUNUT) {
                            hakemus.setTila(HakemuksenTila.PERUNUT);
                        } else if(tila == ValintatuloksenTila.EI_VASTAANOTETTU_MAARA_AIKANA) {
                            hakemus.setTila(HakemuksenTila.PERUNUT);
                            hakemus.getTilanKuvaukset().put("FI","Peruuntunut, ei vastaanottanut määräaikana");
                            hakemus.getTilanKuvaukset().put("SV","Peruuntunut, ei vastaanottanut määräaikana");
                            hakemus.getTilanKuvaukset().put("EN","Peruuntunut, ei vastaanottanut määräaikana");
                        } else if(tila == ValintatuloksenTila.PERUUTETTU) {
                            hakemus.setTila(HakemuksenTila.PERUUTETTU);
                        }
                        hakemusWrapper.setTilaVoidaanVaihtaa(false);
                        henkiloWrapper.getValintatulos().add(valintatulos);
                    }
                }
            }
            for (Hakijaryhma hakijaryhma : hakukohde.getHakijaryhmat()) {
                HakijaryhmaWrapper hakijaryhmaWrapper = new HakijaryhmaWrapper();
                hakijaryhmaWrapper.setHakijaryhma(hakijaryhma);
                hakijaryhmaWrapper.setHakukohdeWrapper(hakukohdeWrapper);
                hakukohdeWrapper.getHakijaryhmaWrappers().add(hakijaryhmaWrapper);
                for (String oid : hakijaryhma.getHakemusOid()) {
                    HenkiloWrapper henkilo = getHenkilo(oid, hakemusOidMap);
                    hakijaryhmaWrapper.getHenkiloWrappers().add(henkilo);
                }
            }
        }
        return sijoitteluajoWrapper;
    }

    private HenkiloWrapper getOrCreateHenkilo(Hakemus hakemus, Map<String, HenkiloWrapper> hakemusOidMap) {
        HenkiloWrapper henkiloWrapper = null;

        if(hakemus.getHakemusOid() != null && !hakemus.getHakemusOid().isEmpty()) {
            henkiloWrapper = hakemusOidMap.get(hakemus.getHakemusOid());
        }

        if(henkiloWrapper == null) {
            henkiloWrapper = new HenkiloWrapper();
            henkiloWrapper.setHakemusOid(hakemus.getHakemusOid());
            if(hakemus.getHakemusOid() != null && !hakemus.getHakemusOid().isEmpty()) {
                hakemusOidMap.put(hakemus.getHakemusOid(), henkiloWrapper);
            }
        }
        return henkiloWrapper;


    }

    private Valintatulos getValintatulos(Hakukohde hakukohde, Valintatapajono valintatapajono, Hakemus hakemus, List<Valintatulos> valintatulokset) {
        if(valintatulokset != null) {
            for(Valintatulos vt : valintatulokset) {
                if(vt.getHakukohdeOid().equals(hakukohde.getOid()) && vt.getValintatapajonoOid().equals(valintatapajono.getOid()) ) {
                    if( vt.getHakemusOid() != null && !vt.getHakemusOid().isEmpty() && vt.getHakemusOid().equals(hakemus.getHakemusOid()) ) {
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
