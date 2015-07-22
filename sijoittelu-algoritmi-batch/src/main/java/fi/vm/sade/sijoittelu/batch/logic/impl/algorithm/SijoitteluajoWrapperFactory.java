package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import static java.util.Collections.emptyMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilanKuvaukset;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakijaryhmaWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakukohdeWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HenkiloWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakijaryhma;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;

public class SijoitteluajoWrapperFactory {
    private final static List<ValintatuloksenTila> hyvaksyttylista = Arrays.asList(ValintatuloksenTila.ILMOITETTU, ValintatuloksenTila.VASTAANOTTANUT, ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI);

    public static SijoitteluajoWrapper createSijoitteluAjo(List<Hakukohde> hakukohteet, List<Valintatulos> valintatulokset) {
        final Map<String, Map<String, Map<String, Valintatulos>>> indeksoidutTulokset = indexValintatulokset(valintatulokset);
        SijoitteluajoWrapper sijoitteluajoWrapper = new SijoitteluajoWrapper();
        Map<String, HenkiloWrapper> hakemusOidMap = new HashMap<String, HenkiloWrapper>();
        hakukohteet.forEach(hakukohde -> {
            HakukohdeWrapper hakukohdeWrapper = new HakukohdeWrapper();
            hakukohdeWrapper.setHakukohde(hakukohde);
            sijoitteluajoWrapper.getHakukohteet().add(hakukohdeWrapper);
            hakukohdeWrapper.setSijoitteluajoWrapper(sijoitteluajoWrapper);
            Map<String, Map<String, Valintatulos>> jonoIndex = indeksoidutTulokset.getOrDefault(hakukohde.getOid(), emptyMap());
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
    private static void addHakijaRyhmatToHakijaRyhmaWrapper(Map<String, HenkiloWrapper> hakemusOidMap, Hakukohde hakukohde, HakukohdeWrapper hakukohdeWrapper) {
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

    private static void setHakemuksenValintatuloksenTila(Hakemus hakemus, HakemusWrapper hakemusWrapper, HenkiloWrapper henkiloWrapper, Valintatulos valintatulos) {
        if (valintatulos != null && valintatulos.getTila() != null) {
            ValintatuloksenTila tila = valintatulos.getTila();
            boolean voidaanVaihtaa = false;
            if (tila == ValintatuloksenTila.PERUNUT) {
                hakemus.setTila(HakemuksenTila.PERUNUT);
            } else if (tila == ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT) {
                if (hakemus.getEdellinenTila() == HakemuksenTila.VARALLA || hakemus.getEdellinenTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY) {
                    hyvaksyVarasijalta(hakemus);
                } else {
                    hakemus.setTila(HakemuksenTila.HYVAKSYTTY);
                }
                hakemus.setIlmoittautumisTila(valintatulos.getIlmoittautumisTila());
            } else if (tila == ValintatuloksenTila.EI_VASTAANOTETTU_MAARA_AIKANA) {
                hakemus.setTila(HakemuksenTila.PERUNUT);
                hakemus.setTilanKuvaukset(TilanKuvaukset.peruuntunutEiVastaanottanutMaaraaikana());
            } else if (tila == ValintatuloksenTila.PERUUTETTU) {
                hakemus.setTila(HakemuksenTila.PERUUTETTU);
            } else if (isHyvaksyttyValintatuloksenTila(tila)) {
                if (hakemus.getEdellinenTila() == HakemuksenTila.VARALLA || hakemus.getEdellinenTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY) {
                    hyvaksyVarasijalta(hakemus);
                } else {
                    hakemus.setTila(HakemuksenTila.HYVAKSYTTY);
                }
                hakemus.setIlmoittautumisTila(valintatulos.getIlmoittautumisTila());
            } else if (valintatulos.getJulkaistavissa() && hakemus.getEdellinenTila() == HakemuksenTila.HYVAKSYTTY) {
                hakemus.setTila(HakemuksenTila.HYVAKSYTTY);
                hakemus.setTilanKuvaukset(TilanKuvaukset.tyhja);
                hakemus.setIlmoittautumisTila(valintatulos.getIlmoittautumisTila());
            } else if (valintatulos.getJulkaistavissa() && hakemus.getEdellinenTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY) {
                hyvaksyVarasijalta(hakemus);
                hakemus.setIlmoittautumisTila(valintatulos.getIlmoittautumisTila());
            } else if (valintatulos.getHyvaksyttyVarasijalta()) {
                hyvaksyVarasijalta(hakemus);
                hakemus.setIlmoittautumisTila(valintatulos.getIlmoittautumisTila());
            } else if (HakemuksenTila.PERUUNTUNUT == hakemus.getEdellinenTila() && valintatulos.getHyvaksyPeruuntunut()) {
                hakemus.setTila(HakemuksenTila.HYVAKSYTTY);
                hakemus.setIlmoittautumisTila(valintatulos.getIlmoittautumisTila());
            } else if (HakemuksenTila.HYLATTY == hakemus.getTila()) {
                voidaanVaihtaa = false; // NOOP
            } else {
                voidaanVaihtaa = true;
            }
            hakemusWrapper.setTilaVoidaanVaihtaa(voidaanVaihtaa);
            henkiloWrapper.getValintatulos().add(valintatulos);
        } else if (hakemus.getTila().equals(HakemuksenTila.HYLATTY)) {
            hakemusWrapper.setTilaVoidaanVaihtaa(false);
        }
    }

    private static boolean isHyvaksyttyValintatuloksenTila(ValintatuloksenTila tila) {
        return hyvaksyttylista.contains(tila);
    }

    private static void hyvaksyVarasijalta(Hakemus hakemus) {
        hakemus.setTilanKuvaukset(TilanKuvaukset.varasijaltaHyvaksytty());
        hakemus.setTila(HakemuksenTila.VARASIJALTA_HYVAKSYTTY);
    }

    private static HenkiloWrapper getOrCreateHenkilo(Hakemus hakemus, Map<String, HenkiloWrapper> hakemusOidMap) {
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

    private static HenkiloWrapper getHenkilo(String hakijaOid, Map<String, HenkiloWrapper> hakijaOidMap) {
        return hakijaOidMap.get(hakijaOid);
    }

}
