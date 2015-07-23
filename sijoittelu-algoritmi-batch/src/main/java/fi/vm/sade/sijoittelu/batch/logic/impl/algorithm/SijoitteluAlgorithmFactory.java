package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor.PostSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor.PostSijoitteluProcessorEhdollisenVastaanotonSiirtyminenYlemmalleHakutoiveelle;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor.PostSijoitteluProcessorMuutostiedonAsetus;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor.PostSijoitteluProcessorPeruuntuneetHakemuksenVastaanotonMuokkaus;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.*;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilanKuvaukset;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.*;
import fi.vm.sade.sijoittelu.domain.*;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.Collections.emptyMap;

@Component
public class SijoitteluAlgorithmFactory {
    private final List<ValintatuloksenTila> hyvaksyttylista = Arrays.asList(ValintatuloksenTila.ILMOITETTU, ValintatuloksenTila.VASTAANOTTANUT, ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI);

    public SijoitteluAlgorithmFactory() {

    }

    public SijoitteluAlgorithm constructAlgorithm(List<Hakukohde> hakukohteet, List<Valintatulos> valintatulokset) {
        List<PreSijoitteluProcessor> preSijoitteluProcessors = new ArrayList<>();
        preSijoitteluProcessors.add(new PreSijoitteluProcessorTasasijaArvonta());
        preSijoitteluProcessors.add(new PreSijoitteluProcessorSort());
        preSijoitteluProcessors.add(new PreSijoitteluProcessorPeruutaAlemmatPeruneetJaHyvaksytyt());
        preSijoitteluProcessors.add(new PreSijoitteluProcessorHylkaaHakijaRyhmaanKuulumattomat());
        preSijoitteluProcessors.add(new PreSijoitteluProcessorLahtotilanteenHash());
        List<PostSijoitteluProcessor> postSijoitteluProcessors = new ArrayList<>();
        //postSijoitteluProcessors.add(new PostSijoitteluProcessorEhdollisenVastaanotonSiirtyminenYlemmalleHakutoiveelle());
        postSijoitteluProcessors.add(new PostSijoitteluProcessorPeruuntuneetHakemuksenVastaanotonMuokkaus());
        postSijoitteluProcessors.add(new PostSijoitteluProcessorMuutostiedonAsetus());
        SijoitteluAlgorithm algorithm = new SijoitteluAlgorithm();
        algorithm.preSijoitteluProcessors = preSijoitteluProcessors;
        algorithm.postSijoitteluProcessors = postSijoitteluProcessors;
        algorithm.sijoitteluAjo = wrapDomain(hakukohteet, indexValintatulokset(valintatulokset));
        return algorithm;
    }

    // hakukohde : valintatapajonot : hakemukset
    private static Map<String, Map<String, Map<String, Valintatulos>>> indexValintatulokset(List<Valintatulos> valintatulokset) {
        Map<String, Map<String, Map<String, Valintatulos>>> hakukohdeIndex = new HashMap<>();

        valintatulokset.stream().filter(vt -> !(vt.getHakemusOid() == null || vt.getHakemusOid().isEmpty())).forEach(vt -> {
            final String hakukohdeOid = vt.getHakukohdeOid();
            final String valintatapajonoOid = vt.getValintatapajonoOid();
            final String hakemusOid = vt.getHakemusOid();

            Map<String, Map<String, Valintatulos>> jonoIndex = hakukohdeIndex.getOrDefault(hakukohdeOid, new HashMap<>());
            Map<String, Valintatulos> hakemusIndex = jonoIndex.getOrDefault(valintatapajonoOid, new HashMap<>());
            hakemusIndex.put(hakemusOid, vt);
            jonoIndex.put(valintatapajonoOid, hakemusIndex);
            hakukohdeIndex.put(hakukohdeOid, jonoIndex);
        });

        return hakukohdeIndex;
    }

    private SijoitteluajoWrapper wrapDomain(List<Hakukohde> hakukohteet, Map<String, Map<String, Map<String, Valintatulos>>> valintatulokset) {
        SijoitteluajoWrapper sijoitteluajoWrapper = new SijoitteluajoWrapper();
        Map<String, HenkiloWrapper> hakemusOidMap = new HashMap<String, HenkiloWrapper>();
        hakukohteet.forEach(hakukohde -> {
            HakukohdeWrapper hakukohdeWrapper = new HakukohdeWrapper();
            hakukohdeWrapper.setHakukohde(hakukohde);
            sijoitteluajoWrapper.getHakukohteet().add(hakukohdeWrapper);
            hakukohdeWrapper.setSijoitteluajoWrapper(sijoitteluajoWrapper);

            Map<String, Map<String, Valintatulos>> jonoIndex = valintatulokset.getOrDefault(hakukohde.getOid(), emptyMap());
            hakukohde.getValintatapajonot().forEach(valintatapajono -> {
                ValintatapajonoWrapper valintatapajonoWrapper = new ValintatapajonoWrapper();
                valintatapajonoWrapper.setValintatapajono(valintatapajono);
                hakukohdeWrapper.getValintatapajonot().add(valintatapajonoWrapper);
                valintatapajonoWrapper.setHakukohdeWrapper(hakukohdeWrapper);

                Map<String, Valintatulos> hakemusIndex = jonoIndex.getOrDefault(valintatapajono.getOid(), emptyMap());
                valintatapajono.getHakemukset().forEach(hakemus -> {
                    HakemusWrapper hakemusWrapper = new HakemusWrapper();
                    hakemusWrapper.setHakemus(hakemus);
                    valintatapajonoWrapper.getHakemukset().add(hakemusWrapper);
                    hakemusWrapper.setValintatapajono(valintatapajonoWrapper);
                    HenkiloWrapper henkiloWrapper = getOrCreateHenkilo(hakemus, hakemusOidMap);
                    henkiloWrapper.getHakemukset().add(hakemusWrapper);
                    hakemusWrapper.setHenkilo(henkiloWrapper);
                    setHakemuksenValintatuloksenTila(hakemus, hakemusWrapper, henkiloWrapper, hakemusIndex.get(hakemus.getHakemusOid()));
                });

            });
            addHakijaRyhmatToHakijaRyhmaWrapper(hakemusOidMap, hakukohde, hakukohdeWrapper);
        });
        return sijoitteluajoWrapper;
    }

    private void addHakijaRyhmatToHakijaRyhmaWrapper(Map<String, HenkiloWrapper> hakemusOidMap, Hakukohde hakukohde, HakukohdeWrapper hakukohdeWrapper) {
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

    private void setHakemuksenValintatuloksenTila(Hakemus hakemus, HakemusWrapper hakemusWrapper, HenkiloWrapper henkiloWrapper, Valintatulos valintatulos) {
        if (valintatulos != null && valintatulos.getTila() != null) {
            ValintatuloksenTila tila = valintatulos.getTila();
            boolean voidaanVaihtaa = true;
            if (tila == ValintatuloksenTila.PERUNUT) {
                hakemus.setTila(HakemuksenTila.PERUNUT);
                voidaanVaihtaa = false;
            } else if (tila == ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT) {
                if (hakemus.getEdellinenTila() == HakemuksenTila.VARALLA || hakemus.getEdellinenTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY) {
                    hyvaksyVarasijalta(hakemus);
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
            } else if (isHyvaksyttyValintatuloksenTila(tila)) {
                if (hakemus.getEdellinenTila() == HakemuksenTila.VARALLA || hakemus.getEdellinenTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY) {
                    hyvaksyVarasijalta(hakemus);
                } else {
                    hakemus.setTila(HakemuksenTila.HYVAKSYTTY);
                }
                voidaanVaihtaa = false;
                hakemus.setIlmoittautumisTila(valintatulos.getIlmoittautumisTila());
            } else if (valintatulos.getJulkaistavissa() && hakemus.getEdellinenTila() == HakemuksenTila.HYVAKSYTTY) {
                hakemus.setTila(HakemuksenTila.HYVAKSYTTY);
                hakemus.setTilanKuvaukset(TilanKuvaukset.tyhja);
                voidaanVaihtaa = false;
                hakemus.setIlmoittautumisTila(valintatulos.getIlmoittautumisTila());
            } else if (valintatulos.getJulkaistavissa() && hakemus.getEdellinenTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY) {
                hyvaksyVarasijalta(hakemus);
                voidaanVaihtaa = false;
                hakemus.setIlmoittautumisTila(valintatulos.getIlmoittautumisTila());
            } else if (valintatulos.getHyvaksyttyVarasijalta()) {
                hyvaksyVarasijalta(hakemus);
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
    }

    private boolean isHyvaksyttyValintatuloksenTila(ValintatuloksenTila tila) {
        return hyvaksyttylista.contains(tila);
    }

    private void hyvaksyVarasijalta(Hakemus hakemus) {
        hakemus.setTilanKuvaukset(TilanKuvaukset.varasijaltaHyvaksytty());
        hakemus.setTila(HakemuksenTila.VARASIJALTA_HYVAKSYTTY);
    }

    private HenkiloWrapper getOrCreateHenkilo(Hakemus hakemus, Map<String, HenkiloWrapper> hakemusOidMap) {
        HenkiloWrapper henkiloWrapper = null;
        if (hakemus.getHakemusOid() != null && !hakemus.getHakemusOid().isEmpty()) {
            henkiloWrapper = hakemusOidMap.get(hakemus.getHakemusOid());
        }
        if (henkiloWrapper == null) {
            henkiloWrapper = new HenkiloWrapper();
            henkiloWrapper.setHakemusOid(hakemus.getHakemusOid());
            henkiloWrapper.setHakijaOid(hakemus.getHakijaOid());
            if (hakemus.getHakemusOid() != null && !hakemus.getHakemusOid().isEmpty()) {
                hakemusOidMap.put(hakemus.getHakemusOid(), henkiloWrapper);
            }
        }
        return henkiloWrapper;
    }

    private HenkiloWrapper getHenkilo(String hakijaOid, Map<String, HenkiloWrapper> hakijaOidMap) {
        return hakijaOidMap.get(hakijaOid);
    }
}
