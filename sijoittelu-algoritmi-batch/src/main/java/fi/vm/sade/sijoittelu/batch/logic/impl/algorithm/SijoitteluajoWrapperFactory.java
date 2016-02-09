package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import static fi.vm.sade.sijoittelu.domain.ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT;
import static fi.vm.sade.sijoittelu.domain.ValintatuloksenTila.KESKEN;
import static fi.vm.sade.sijoittelu.domain.ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

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
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;

public class SijoitteluajoWrapperFactory {
    public static SijoitteluajoWrapper createSijoitteluAjoWrapper(SijoitteluAjo sijoitteluAjo, List<Hakukohde> hakukohteet, List<Valintatulos> valintatulokset) {
        final Map<String, Map<String, Map<String, Valintatulos>>> indeksoidutTulokset = indexValintatulokset(valintatulokset);
        SijoitteluajoWrapper sijoitteluajoWrapper = new SijoitteluajoWrapper(sijoitteluAjo);
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
            } else if (asList(VASTAANOTTANUT_SITOVASTI, EHDOLLISESTI_VASTAANOTTANUT).contains(tila)) {
                if (hakemus.getEdellinenTila() == HakemuksenTila.VARALLA || hakemus.getEdellinenTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY) {
                    hyvaksyVarasijalta(hakemus, valintatulos);
                } else {
                    hyvaksy(hakemus, valintatulos);
                }
            } else if (tila == ValintatuloksenTila.EI_VASTAANOTETTU_MAARA_AIKANA) {
                hakemus.setTila(HakemuksenTila.PERUNUT);
                hakemus.setTilanKuvaukset(TilanKuvaukset.peruuntunutEiVastaanottanutMaaraaikana());
            } else if (tila == ValintatuloksenTila.PERUUTETTU) {
                hakemus.setTila(HakemuksenTila.PERUUTETTU);
            } else {
                // tila == KESKEN
                if (valintatulos.getJulkaistavissa() && hakemus.getEdellinenTila() == HakemuksenTila.HYVAKSYTTY) {
                    hyvaksy(hakemus, valintatulos);
                } else if (valintatulos.getJulkaistavissa() && hakemus.getEdellinenTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY) {
                    hyvaksyVarasijalta(hakemus, valintatulos);
                } else if (valintatulos.getHyvaksyttyVarasijalta()) {
                    hyvaksyVarasijalta(hakemus, valintatulos);
                } else if (HakemuksenTila.PERUUNTUNUT == hakemus.getEdellinenTila() && valintatulos.getHyvaksyPeruuntunut()) {
                    hyvaksy(hakemus, valintatulos);
                    hakemusWrapper.hyvaksyPeruuntunut();
                } else if (HakemuksenTila.HYLATTY == hakemus.getTila()) {
                    // NOOP
                } else {
                    voidaanVaihtaa = true;
                }
            }
            hakemusWrapper.setTilaVoidaanVaihtaa(voidaanVaihtaa);
            henkiloWrapper.getValintatulos().add(valintatulos);
        } else if (hakemus.getTila().equals(HakemuksenTila.HYLATTY)) {
            hakemusWrapper.setTilaVoidaanVaihtaa(false);
        }
    }

    private static void hyvaksyVarasijalta(final Hakemus hakemus, final Valintatulos valintatulos) {
        hakemus.setTilanKuvaukset(TilanKuvaukset.varasijaltaHyvaksytty());
        hakemus.setTila(HakemuksenTila.VARASIJALTA_HYVAKSYTTY);
        hakemus.setIlmoittautumisTila(valintatulos.getIlmoittautumisTila());
    }

    private static void hyvaksy(final Hakemus hakemus, final Valintatulos valintatulos) {
        hakemus.setTila(HakemuksenTila.HYVAKSYTTY);
        hakemus.setTilanKuvaukset(TilanKuvaukset.tyhja);
        hakemus.setIlmoittautumisTila(valintatulos.getIlmoittautumisTila());
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
