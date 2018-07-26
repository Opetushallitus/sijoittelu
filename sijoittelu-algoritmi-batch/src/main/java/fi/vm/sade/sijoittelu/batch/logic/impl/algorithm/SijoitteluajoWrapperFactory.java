package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import static fi.vm.sade.sijoittelu.domain.ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT;
import static fi.vm.sade.sijoittelu.domain.ValintatuloksenTila.KESKEN;
import static fi.vm.sade.sijoittelu.domain.ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilaTaulukot;
import fi.vm.sade.sijoittelu.domain.TilanKuvaukset;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakijaryhmaWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakukohdeWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HenkiloWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.sijoittelu.domain.dto.VastaanottoDTO;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SijoitteluajoWrapperFactory {

    private static final Logger LOG = LoggerFactory.getLogger(SijoitteluajoWrapperFactory.class);

    public static SijoitteluajoWrapper createSijoitteluAjoWrapper(SijoitteluAjo sijoitteluAjo,
                                                                  List<Hakukohde> hakukohteet,
                                                                  List<Valintatulos> valintatulokset,
                                                                  Map<String, VastaanottoDTO> aiemmanVastaanotonHakukohdePerHakija) {
        LOG.info("Luodaan SijoitteluAjoWrapper haulle {}", sijoitteluAjo.getHakuOid());
        final Map<String, Map<String, Map<String, Valintatulos>>> indeksoidutTulokset = indexValintatulokset(valintatulokset);
        SijoitteluajoWrapper sijoitteluajoWrapper = new SijoitteluajoWrapper(sijoitteluAjo);
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
                });
            });
            addHakijaRyhmatToHakijaRyhmaWrapper(hakemusOidMap, hakukohde, hakukohdeWrapper);
        });
        sijoitteluajoWrapper.getHakukohteet().forEach(hakukohdeWrapper -> {
            Map<String, Map<String, Valintatulos>> jonoIndex = indeksoidutTulokset.getOrDefault(hakukohdeWrapper.getHakukohde().getOid(), emptyMap());
            hakukohdeWrapper.getValintatapajonot().forEach(valintatapajonoWrapper -> {
                Map<String, Valintatulos> hakemusIndex = jonoIndex.getOrDefault(valintatapajonoWrapper.getValintatapajono().getOid(), emptyMap());
                valintatapajonoWrapper.getHakemukset().forEach(hakemusWrapper -> {
                    setHakemuksenValintatuloksenTila(
                            hakukohdeWrapper,
                            hakemusWrapper,
                            hakemusIndex.get(hakemusWrapper.getHakemus().getHakemusOid()),
                            Optional.ofNullable(aiemmanVastaanotonHakukohdePerHakija.get(hakemusWrapper.getHenkilo().getHakijaOid()))
                    );
                });
            });
        });
        LOG.info("SijoitteluAjoWrapper luotu haulle {}", sijoitteluAjo.getHakuOid());
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

    private static boolean voiTullaHyvaksytyksi(Hakemus hakemus) {
        if (hakemus.getTila() == HakemuksenTila.HYLATTY) {
            return false;
        }
        if (hakemus.getTila() == HakemuksenTila.VARALLA) {
            return true;
        }
        throw new RuntimeException(String.format("Hakemuksen tilan piti olla HYLATTY tai VARALLA, oli %s. Hakemus %s",
                hakemus.getTila(), hakemus));
    }

    private static boolean vastaanotonTilaSaaMuuttaaHakemuksenTilaa(Hakemus hakemus) {
        return TilaTaulukot.kuuluuVastaanotonMuokattavissaTiloihin(hakemus.getEdellinenTila());
    }

    private static void setHakemuksenValintatuloksenTila(HakukohdeWrapper hakukohdeWrapper,
                                                         HakemusWrapper hakemusWrapper,
                                                         Valintatulos valintatulos,
                                                         Optional<VastaanottoDTO> aiempiVastaanottoSamalleKaudelle) {
        Hakemus hakemus = hakemusWrapper.getHakemus();
        if (estaaVastaanotonYhdenPaikanSaannoksenTakia(aiempiVastaanottoSamalleKaudelle, hakemusWrapper)) {
             if (valintatulos != null && valintatulos.getTila() == ValintatuloksenTila.PERUNUT) {
                hakemus.setTila(HakemuksenTila.PERUNUT);
            } else if (voiTullaHyvaksytyksi(hakemus) || hakemus.getEdellinenTila() == HakemuksenTila.PERUUNTUNUT) {
                hakemus.setTila(HakemuksenTila.PERUUNTUNUT);
                if (hakemus.getEdellinenTila() != HakemuksenTila.PERUUNTUNUT || hakemus.getTilanKuvaukset() == null || hakemus.getTilanKuvaukset().isEmpty()) {
                    hakemus.setTilanKuvaukset(TilanKuvaukset.peruuntunutVastaanottanutToisenOpiskelupaikanYhdenPaikanSaannonPiirissa());
                }
            }
            hakemusWrapper.setTilaVoidaanVaihtaa(false);
        } else if (valintatulos != null && valintatulos.getTila() != null) {
            if (!vastaanotonTilaSaaMuuttaaHakemuksenTilaa(hakemus)) {
                // Don't write a log entry
                valintatulos.setTila(ValintatuloksenTila.KESKEN, ValintatuloksenTila.KESKEN, "", "");
            }
            LOG.debug("Hakukohde: {}, valintatapajono: {}, hakemus: {}, hakemuksen tila: {}, hakemuksen edellinen tila: {}, vastaanoton tila: {}",
                    hakemusWrapper.getValintatapajono().getHakukohdeWrapper().getHakukohde().getOid(),
                    hakemusWrapper.getValintatapajono().getValintatapajono().getOid(),
                    hakemus.getHakemusOid(),
                    hakemus.getTila(),
                    hakemus.getEdellinenTila(),
                    valintatulos.getTila());
            ValintatuloksenTila tila = valintatulos.getTila();
            boolean voidaanVaihtaa = false;
            if (tila == ValintatuloksenTila.PERUNUT) {
                hakemus.setTila(HakemuksenTila.PERUNUT);
            } else if (asList(VASTAANOTTANUT_SITOVASTI, EHDOLLISESTI_VASTAANOTTANUT).contains(tila)) {
                if (viimeisinHyvaksyttyJono(hakemusWrapper)) {
                    if (hakemus.getEdellinenTila() == HakemuksenTila.VARALLA || hakemus.getEdellinenTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY) {
                        hyvaksyVarasijalta(hakemus, valintatulos);
                    } else {
                        hyvaksy(hakemus, valintatulos);
                    }
                } else {
                    valintatulos.setTila(KESKEN, "Peitetään vastaanottotieto ei-hyväksytyltä jonolta"); // See ValintatulosWithVastaanotto.persistValintatulokset check
                    voidaanVaihtaa = false;
                }
            } else if (tila == ValintatuloksenTila.EI_VASTAANOTETTU_MAARA_AIKANA) {
                hakemus.setTila(HakemuksenTila.PERUNUT);
                hakemus.setTilanKuvaukset(TilanKuvaukset.peruuntunutEiVastaanottanutMaaraaikana());
            } else if (tila == ValintatuloksenTila.PERUUTETTU) {
                hakemus.setTila(HakemuksenTila.PERUUTETTU);
                hakemus.setTilankuvauksenTarkenne(TilankuvauksenTarkenne.EI_TILANKUVAUKSEN_TARKENNETTA);
            } else if (tila == ValintatuloksenTila.KESKEN) {
                // tila == KESKEN
                if (valintatulos.getJulkaistavissa() && hakemus.getEdellinenTila() == HakemuksenTila.HYVAKSYTTY) {
                    hyvaksy(hakemus, valintatulos);
                } else if (valintatulos.getJulkaistavissa() && hakemus.getEdellinenTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY) {
                    hyvaksyVarasijalta(hakemus, valintatulos);
                } else if (valintatulos.getHyvaksyttyVarasijalta()) {
                    if (hasHigherJulkaistuHyvaksytty(hakukohdeWrapper, hakemusWrapper)) {
                        voidaanVaihtaa = true;
                        logDidNotHyvaksy(hakemusWrapper, tila, "hyvaksyttyVarasijalta");
                    } else {
                        hyvaksyVarasijalta(hakemus, valintatulos);
                    }
                } else if (valintatulos.getHyvaksyPeruuntunut()) {
                    if (hasHigherJulkaistuHyvaksytty(hakukohdeWrapper, hakemusWrapper)) {
                        voidaanVaihtaa = true;
                        logDidNotHyvaksy(hakemusWrapper, tila, "hyvaksyPeruuntunut");
                    } else {
                        hyvaksy(hakemus, valintatulos);
                        hakemusWrapper.hyvaksyPeruuntunut();
                    }
                } else if (HakemuksenTila.HYLATTY == hakemus.getTila()) {
                    voidaanVaihtaa = false;
                } else {
                    voidaanVaihtaa = true;
                }
            } else {
                throw new IllegalStateException(String.format("Valintatulos %s ja hakemus %s olivat tuntemattomassa tilassa", valintatulos, hakemus));
            }
            hakemusWrapper.setTilaVoidaanVaihtaa(voidaanVaihtaa);
            hakemusWrapper.getHenkilo().getValintatulos().add(valintatulos);
        } else if (hakemus.getTila().equals(HakemuksenTila.HYLATTY)) {
            hakemusWrapper.setTilaVoidaanVaihtaa(false);
        } else if (aiempiVastaanottoSamalleKaudelle.isPresent()) {
            LOG.warn("Ei muutettu hakemuksen tilaa aiemman saman kauden vastaanoton perusteella. " +
                    "Hakukohde: {}, valintatapajono: {}, hakemus: {}, hakemuksen tila: {}, " +
                    "hakemuksen edellinen tila: {}, vastaanoton tila: {}, aiemmin vastaanotettu hakukohde: {} {},",
                    hakemusWrapper.getValintatapajono().getHakukohdeWrapper().getHakukohde().getOid(),
                    hakemusWrapper.getValintatapajono().getValintatapajono().getOid(),
                    hakemus.getHakemusOid(),
                    hakemus.getTila(),
                    hakemus.getEdellinenTila(),
                    valintatulos != null ? valintatulos.getTila() : "- (ei valintatulosta)",
                    aiempiVastaanottoSamalleKaudelle.get().getAction(),
                    aiempiVastaanottoSamalleKaudelle.get().getHakukohdeOid());
        }
    }

    private static void logDidNotHyvaksy(HakemusWrapper hakemusWrapper, ValintatuloksenTila tila, String flagName) {
        Hakemus hakemus = hakemusWrapper.getHakemus();
        LOG.info("Ei hyväksytä jonosta vaikka {} päällä, koska korkeamman prioriteetin jonossa on jo julkaistu hyväksytty: " +
                        "Hakukohde: {}, valintatapajono: {}, hakemus: {}, hakemuksen tila: {}, " +
                        "hakemuksen edellinen tila: {}, vastaanoton tila: {}",
                flagName,
                hakemusWrapper.getValintatapajono().getHakukohdeWrapper().getHakukohde().getOid(),
                hakemusWrapper.getValintatapajono().getValintatapajono().getOid(),
                hakemus.getHakemusOid(),
                hakemus.getTila(),
                hakemus.getEdellinenTila(),
                tila);
    }

    private static boolean hasHigherJulkaistuHyvaksytty(HakukohdeWrapper hakukohdeWrapper,
                                                        HakemusWrapper hakemusWrapper) {
        Valintatapajono currentJono = hakemusWrapper.getValintatapajono().getValintatapajono();
        for (ValintatapajonoWrapper toinenJono : hakukohdeWrapper.getValintatapajonot()) {
            if (toinenJono.getValintatapajono().getPrioriteetti() < currentJono.getPrioriteetti()) {
                for (HakemusWrapper hakemus : toinenJono.getHakemukset()) {
                    if (hakemus.getHakemus().getHakemusOid().equals(hakemusWrapper.getHakemus().getHakemusOid())) {
                        if (hakemus.getValintatulos().map(v -> v.getJulkaistavissa()).orElse(false)
                                && TilaTaulukot.kuuluuHyvaksyttyihinTiloihin(hakemus.getHakemus().getEdellinenTila())) {
                            return true;
                        }
                        break;
                    }
                }
            }
        }
        return false;
    }

    private static boolean estaaVastaanotonYhdenPaikanSaannoksenTakia(Optional<VastaanottoDTO> aiempiVastaanottoOptional, HakemusWrapper hakemusWrapper) {
        if (!aiempiVastaanottoOptional.isPresent() || hakemusWrapper.getHakukohdeOid().equals(aiempiVastaanottoOptional.get().getHakukohdeOid())) {
            return false;
        }
        VastaanottoDTO aiempiVastaanotto = aiempiVastaanottoOptional.get();
        if (aiempiVastaanotto.getAction().sitova) {
            return true;
        }
        return onEriHaun(aiempiVastaanotto, hakemusWrapper);
    }

    private static boolean onEriHaun(VastaanottoDTO aiempiVastaanotto, HakemusWrapper hakemusWrapper) {
        return hakemusWrapper.getHenkilo().getHakemukset().stream().noneMatch(h ->
            h.getHakukohdeOid().equals(aiempiVastaanotto.getHakukohdeOid()));
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

    private static boolean viimeisinHyvaksyttyJono(HakemusWrapper hakemusWrapper) {

        if(TilaTaulukot.kuuluuHyvaksyttyihinTiloihin(hakemusWrapper.getHakemus().getEdellinenTila())) return true;

        final String viimeisinHyvaksyttyJonoOid = hakemusWrapper.getHenkilo().getHakemukset().stream()
                .filter(hw -> hw.getHakemus().getTilaHistoria().stream().anyMatch(th -> TilaTaulukot.kuuluuHyvaksyttyihinTiloihin(th.getTila())) == true)
                .map(hw -> {
                    final TilaHistoria tilaHistoria = hw.getHakemus().getTilaHistoria().stream()
                            .filter(th -> TilaTaulukot.kuuluuHyvaksyttyihinTiloihin(th.getTila()))
                            .max(Comparator.comparingLong(th -> th.getLuotu().getTime()))
                            .get();
                    org.apache.commons.lang3.tuple.Pair<String, Date> pair = Pair.of(hw.getValintatapajono().getValintatapajono().getOid(), tilaHistoria.getLuotu());

                    return pair;

                }).collect(Collectors.toList()).stream()
                .max(Comparator.comparingLong(pair -> pair.getRight().getTime())).orElse(Pair.of("", null)).getLeft();

        return viimeisinHyvaksyttyJonoOid.equals(hakemusWrapper.getValintatapajono().getValintatapajono().getOid());


    }
}
