package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor.PostSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.*;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilanKuvaukset;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.*;
import fi.vm.sade.sijoittelu.domain.*;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SijoitteluAlgorithmFactoryImpl implements SijoitteluAlgorithmFactory {

    public SijoitteluAlgorithmFactoryImpl() {

    }

    @Override
    public SijoitteluAlgorithm constructAlgorithm(List<Hakukohde> hakukohteet, List<Valintatulos> valintatulokset) {
        List<PreSijoitteluProcessor> preSijoitteluProcessors = new ArrayList<PreSijoitteluProcessor>();
        preSijoitteluProcessors.add(new PreSijoitteluProcessorTasasijaArvonta());
        preSijoitteluProcessors.add(new PreSijoitteluProcessorSort());
        preSijoitteluProcessors.add(new PreSijoitteluProcessorPeruutaAlemmatPeruneetJaHyvaksytyt());
        preSijoitteluProcessors.add(new PreSijoitteluProcessorHylkaaHakijaRyhmaanKuulumattomat());
        List<PostSijoitteluProcessor> postSijoitteluProcessors = new ArrayList<PostSijoitteluProcessor>();
        SijoitteluAlgorithmImpl algorithm = new SijoitteluAlgorithmImpl();
        algorithm.preSijoitteluProcessors = preSijoitteluProcessors;
        algorithm.postSijoitteluProcessors = postSijoitteluProcessors;
        algorithm.sijoitteluAjo = wrapDomain(hakukohteet, valintatulokset);
        return algorithm;
    }

    private SijoitteluajoWrapper wrapDomain(List<Hakukohde> hakukohteet, List<Valintatulos> valintatulokset) {
        SijoitteluajoWrapper sijoitteluajoWrapper = new SijoitteluajoWrapper();
        Map<String, HenkiloWrapper> hakemusOidMap = new HashMap<String, HenkiloWrapper>();
        hakukohteet.forEach(hakukohde -> {
            HakukohdeWrapper hakukohdeWrapper = new HakukohdeWrapper();
            hakukohdeWrapper.setHakukohde(hakukohde);
            sijoitteluajoWrapper.getHakukohteet().add(hakukohdeWrapper);
            hakukohdeWrapper.setSijoitteluajoWrapper(sijoitteluajoWrapper);
            hakukohde.getValintatapajonot().forEach(valintatapajono -> {
                ValintatapajonoWrapper valintatapajonoWrapper = new ValintatapajonoWrapper();
                valintatapajonoWrapper.setValintatapajono(valintatapajono);
                hakukohdeWrapper.getValintatapajonot().add(valintatapajonoWrapper);
                valintatapajonoWrapper.setHakukohdeWrapper(hakukohdeWrapper);
                valintatapajono.getHakemukset().forEach(hakemus -> {
                    HakemusWrapper hakemusWrapper = new HakemusWrapper();
                    hakemusWrapper.setHakemus(hakemus);
                    valintatapajonoWrapper.getHakemukset().add(hakemusWrapper);
                    hakemusWrapper.setValintatapajono(valintatapajonoWrapper);
                    HenkiloWrapper henkiloWrapper = getOrCreateHenkilo(hakemus, hakemusOidMap);
                    henkiloWrapper.getHakemukset().add(hakemusWrapper);
                    hakemusWrapper.setHenkilo(henkiloWrapper);
                    Valintatulos valintatulos = getValintatulos(hakukohde, valintatapajono, hakemus, valintatulokset);
                    List<ValintatuloksenTila> hyvaksyttylista = Arrays.asList(ValintatuloksenTila.ILMOITETTU, ValintatuloksenTila.VASTAANOTTANUT, ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI);
                    if (valintatulos != null && valintatulos.getTila() != null) {
                        ValintatuloksenTila tila = valintatulos.getTila();
                        boolean voidaanVaihtaa = true;
                        if (tila == ValintatuloksenTila.PERUNUT) {
                            hakemus.setTila(HakemuksenTila.PERUNUT);
                            voidaanVaihtaa = false;
                        } else if (tila == ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT) {
                            if (hakemus.getEdellinenTila() == HakemuksenTila.VARALLA || hakemus.getEdellinenTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY) {
                                hakemus.setTilanKuvaukset(TilanKuvaukset.varasijaltaHyvaksytty());
                                hakemus.setTila(HakemuksenTila.VARASIJALTA_HYVAKSYTTY);
                            } else {
                                hakemus.setTila(HakemuksenTila.HYVAKSYTTY);
                            }
                            hakemus.setIlmoittautumisTila(valintatulos.getIlmoittautumisTila());
                            voidaanVaihtaa = false;
                        } else if (tila == ValintatuloksenTila.EI_VASTAANOTETTU_MAARA_AIKANA) {
                            hakemus.setTila(HakemuksenTila.PERUNUT);
                            hakemus.setTilanKuvaukset(TilanKuvaukset.peruuntunutEiVastaanottanutMaaraaikana());
                            voidaanVaihtaa = false;
                        } else if (tila == ValintatuloksenTila.PERUUTETTU) {
                            hakemus.setTila(HakemuksenTila.PERUUTETTU);
                            voidaanVaihtaa = false;
                        } else if (hyvaksyttylista.contains(tila)) {
                            if (hakemus.getEdellinenTila() == HakemuksenTila.VARALLA || hakemus.getEdellinenTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY) {
                                hakemus.setTilanKuvaukset(TilanKuvaukset.varasijaltaHyvaksytty());
                                hakemus.setTila(HakemuksenTila.VARASIJALTA_HYVAKSYTTY);
                            } else {
                                hakemus.setTila(HakemuksenTila.HYVAKSYTTY);
                            }
                            voidaanVaihtaa = false;
                            hakemus.setIlmoittautumisTila(valintatulos.getIlmoittautumisTila());
                        } else if (valintatulos.getJulkaistavissa() && (hakemus.getEdellinenTila() == HakemuksenTila.HYVAKSYTTY || hakemus.getEdellinenTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY)) {
                            if (hakemus.getEdellinenTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY) {
                                hakemus.setTilanKuvaukset(TilanKuvaukset.varasijaltaHyvaksytty());
                                hakemus.setTila(HakemuksenTila.VARASIJALTA_HYVAKSYTTY);
                            } else {
                                hakemus.setTila(HakemuksenTila.HYVAKSYTTY);
                            }
                            voidaanVaihtaa = false;
                            hakemus.setIlmoittautumisTila(valintatulos.getIlmoittautumisTila());
                        } else if (valintatulos.getHyvaksyttyVarasijalta()) {
                            hakemus.setTilanKuvaukset(TilanKuvaukset.varasijaltaHyvaksytty());
                            hakemus.setTila(HakemuksenTila.VARASIJALTA_HYVAKSYTTY);
                            hakemus.setIlmoittautumisTila(valintatulos.getIlmoittautumisTila());
                            voidaanVaihtaa = false;
                        } else if (hakemus.getTila().equals(HakemuksenTila.HYLATTY)) {
                            hakemusWrapper.setTilaVoidaanVaihtaa(false);
                            voidaanVaihtaa = false;
                        }
                        hakemusWrapper.setTilaVoidaanVaihtaa(voidaanVaihtaa);
                        henkiloWrapper.getValintatulos().add(valintatulos);
                    } else if (hakemus.getTila().equals(HakemuksenTila.HYLATTY)) {
                        hakemusWrapper.setTilaVoidaanVaihtaa(false);
                    }
                });

            });
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
        });
        return sijoitteluajoWrapper;
    }

    private HenkiloWrapper getOrCreateHenkilo(Hakemus hakemus, Map<String, HenkiloWrapper> hakemusOidMap) {
        HenkiloWrapper henkiloWrapper = null;
        if (hakemus.getHakemusOid() != null && !hakemus.getHakemusOid().isEmpty()) {
            henkiloWrapper = hakemusOidMap.get(hakemus.getHakemusOid());
        }
        if (henkiloWrapper == null) {
            henkiloWrapper = new HenkiloWrapper();
            henkiloWrapper.setHakemusOid(hakemus.getHakemusOid());
            if (hakemus.getHakemusOid() != null && !hakemus.getHakemusOid().isEmpty()) {
                hakemusOidMap.put(hakemus.getHakemusOid(), henkiloWrapper);
            }
        }
        return henkiloWrapper;
    }

    private Valintatulos getValintatulos(Hakukohde hakukohde, Valintatapajono valintatapajono, Hakemus hakemus, List<Valintatulos> valintatulokset) {
        if (valintatulokset != null) {
            for (Valintatulos vt : valintatulokset) {
                if (vt.getHakukohdeOid().equals(hakukohde.getOid()) && vt.getValintatapajonoOid().equals(valintatapajono.getOid())) {
                    if (vt.getHakemusOid() != null && !vt.getHakemusOid().isEmpty() && vt.getHakemusOid().equals(hakemus.getHakemusOid())) {
                        return vt;
                    }
                }
            }
        }
        return null;
    }


    private HenkiloWrapper getHenkilo(String hakijaOid, Map<String, HenkiloWrapper> hakijaOidMap) {
        return hakijaOidMap.get(hakijaOid);
    }
}
