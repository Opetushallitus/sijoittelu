package fi.vm.sade.sijoittelu.tulos.service.impl.converters;

import com.google.common.collect.Lists;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.tulos.dto.*;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.*;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RaportointiConverterImpl implements RaportointiConverter {
    @Override
    public List<HakijaDTO> convert(List<HakukohdeDTO> hakukohteet) {
        HashMap<String, HakijaDTO> hakijat = new HashMap<>();
        for (HakukohdeDTO hakukohde : hakukohteet) {
            for (ValintatapajonoDTO valintatapajono : hakukohde.getValintatapajonot()) {
                for (HakemusDTO hakemusDTO : valintatapajono.getHakemukset()) {
                    HakijaDTO hakijaRaportointiDTO = getOrCreateHakijaRaportointiDTO(hakijat, hakemusDTO);
                    populateHakija(hakukohde, valintatapajono, hakemusDTO, hakijaRaportointiDTO);
                }
            }
        }
        return new ArrayList<>(hakijat.values());
    }

    private void populateHakija(HakukohdeDTO hakukohde, ValintatapajonoDTO valintatapajono, HakemusDTO hakemusDTO, HakijaDTO hakijaRaportointiDTO) {
        HakutoiveDTO raportointiHakutoiveDTO = getOrCreateHakutoive(hakijaRaportointiDTO, hakemusDTO, hakukohde);
        HakutoiveenValintatapajonoDTO hakutoiveenValintatapajonoDTO = new HakutoiveenValintatapajonoDTO();
        raportointiHakutoiveDTO.getHakutoiveenValintatapajonot().add(hakutoiveenValintatapajonoDTO);
        hakutoiveenValintatapajonoDTO.setHakeneet(valintatapajono.getHakeneet());
        hakutoiveenValintatapajonoDTO.setAlinHyvaksyttyPistemaara(valintatapajono.getAlinHyvaksyttyPistemaara());
        hakutoiveenValintatapajonoDTO.setHyvaksytty(valintatapajono.getHyvaksytty());
        hakutoiveenValintatapajonoDTO.setVaralla(valintatapajono.getVaralla());
        hakutoiveenValintatapajonoDTO.setHakeneet(valintatapajono.getHakeneet());
        hakutoiveenValintatapajonoDTO.setValintatapajonoOid(valintatapajono.getOid());
        hakutoiveenValintatapajonoDTO.setTayttojono(valintatapajono.getTayttojono());
        hakutoiveenValintatapajonoDTO.setEiVarasijatayttoa(Optional.ofNullable(valintatapajono.getEiVarasijatayttoa()).orElse(false));
        hakutoiveenValintatapajonoDTO.setVarasijat(valintatapajono.getVarasijat());
        hakutoiveenValintatapajonoDTO.setVarasijaTayttoPaivat(valintatapajono.getVarasijaTayttoPaivat());
        hakutoiveenValintatapajonoDTO.setVarasijojaKaytetaanAlkaen(valintatapajono.getVarasijojaKaytetaanAlkaen());
        hakutoiveenValintatapajonoDTO.setVarasijojaTaytetaanAsti(valintatapajono.getVarasijojaTaytetaanAsti());
        hakutoiveenValintatapajonoDTO.setValintatapajonoNimi(valintatapajono.getNimi());
        hakutoiveenValintatapajonoDTO.setVarasijanNumero(hakemusDTO.getVarasijanNumero());
        hakutoiveenValintatapajonoDTO.setTasasijaJonosija(hakemusDTO.getTasasijaJonosija());
        hakutoiveenValintatapajonoDTO.setPisteet(hakemusDTO.getPisteet());
        hakutoiveenValintatapajonoDTO.setJonosija(hakemusDTO.getJonosija());
        hakutoiveenValintatapajonoDTO.setTila(hakemusDTO.getTila());
        hakutoiveenValintatapajonoDTO.setTilanKuvaukset(hakemusDTO.getTilanKuvaukset());
        hakutoiveenValintatapajonoDTO.setHyvaksyttyHarkinnanvaraisesti(hakemusDTO.isHyvaksyttyHarkinnanvaraisesti());
        hakutoiveenValintatapajonoDTO.setPaasyJaSoveltuvuusKokeenTulos(hakemusDTO.getPaasyJaSoveltuvuusKokeenTulos());
        hakutoiveenValintatapajonoDTO.setValintatapajonoPrioriteetti(hakemusDTO.getPrioriteetti());
        hakutoiveenValintatapajonoDTO.setValintatapajonoOid(valintatapajono.getOid());
        hakutoiveenValintatapajonoDTO.setHakemuksenTilanViimeisinMuutos(viimeisinHakemuksenTilanMuutos(hakemusDTO));
        applyPistetiedot(raportointiHakutoiveDTO, hakemusDTO.getPistetiedot());
    }

    private void kevytPopulateHakija(KevytHakukohdeDTO hakukohde, KevytValintatapajonoDTO valintatapajono, KevytHakemusDTO hakemusDTO, KevytHakijaDTO hakijaRaportointiDTO) {
        KevytHakutoiveDTO raportointiHakutoiveDTO = getOrCreateHakutoive(hakijaRaportointiDTO, hakemusDTO, hakukohde);
        KevytHakutoiveenValintatapajonoDTO hakutoiveenValintatapajonoDTO = new KevytHakutoiveenValintatapajonoDTO();
        raportointiHakutoiveDTO.getHakutoiveenValintatapajonot().add(hakutoiveenValintatapajonoDTO);
        hakutoiveenValintatapajonoDTO.setValintatapajonoOid(valintatapajono.getOid());
        hakutoiveenValintatapajonoDTO.setEiVarasijatayttoa(Optional.ofNullable(valintatapajono.getEiVarasijatayttoa()).orElse(false));
        hakutoiveenValintatapajonoDTO.setVarasijojaKaytetaanAlkaen(valintatapajono.getVarasijojaKaytetaanAlkaen());
        hakutoiveenValintatapajonoDTO.setVarasijojaTaytetaanAsti(valintatapajono.getVarasijojaTaytetaanAsti());
        hakutoiveenValintatapajonoDTO.setVarasijanNumero(hakemusDTO.getVarasijanNumero());
        hakutoiveenValintatapajonoDTO.setPisteet(hakemusDTO.getPisteet());
        hakutoiveenValintatapajonoDTO.setJonosija(hakemusDTO.getJonosija());
        hakutoiveenValintatapajonoDTO.setTila(hakemusDTO.getTila());
        hakutoiveenValintatapajonoDTO.setTilanKuvaukset(hakemusDTO.getTilanKuvaukset());
        hakutoiveenValintatapajonoDTO.setHyvaksyttyHarkinnanvaraisesti(hakemusDTO.isHyvaksyttyHarkinnanvaraisesti());
        hakutoiveenValintatapajonoDTO.setValintatapajonoOid(valintatapajono.getOid());
        hakutoiveenValintatapajonoDTO.setHakemuksenTilanViimeisinMuutos(hakemusDTO.getViimeisenHakemuksenTilanMuutos());
    }

    private List<KevytHakijaDTO> convertHakukohteenHakijat(HakukohdeDTO hakukohde, Iterator<KevytHakukohdeDTO> hakukohteet) {
        HashMap<String, KevytHakijaDTO> hakijat = new HashMap<>();
        for (ValintatapajonoDTO valintatapajono : hakukohde.getValintatapajonot()) {
            for (HakemusDTO hakemusDTO : valintatapajono.getHakemukset()) {
                if (!hakijat.containsKey(hakemusDTO.getHakemusOid())) {
                    KevytHakijaDTO h = new KevytHakijaDTO();
                    h.setHakijaOid(hakemusDTO.getHakijaOid());
                    h.setHakemusOid(hakemusDTO.getHakemusOid());
                    hakijat.put(hakemusDTO.getHakemusOid(), h);
                }
                KevytHakijaDTO hakijaRaportointiDTO = hakijat.get(hakemusDTO.getHakemusOid());
                KevytHakutoiveDTO raportointiHakutoiveDTO = null;
                for (KevytHakutoiveDTO hd : hakijaRaportointiDTO.getHakutoiveet()) {
                    if (hd.getHakukohdeOid().equals(hakemusDTO.getHakukohdeOid())) {
                        raportointiHakutoiveDTO = hd;
                    }
                }
                if (raportointiHakutoiveDTO == null) {
                    raportointiHakutoiveDTO = new KevytHakutoiveDTO();
                    raportointiHakutoiveDTO.setTarjoajaOid(hakemusDTO.getTarjoajaOid());
                    raportointiHakutoiveDTO.setHakukohdeOid(hakemusDTO.getHakukohdeOid());
                    raportointiHakutoiveDTO.setKaikkiJonotSijoiteltu(hakukohde.isKaikkiJonotSijoiteltu());
                    raportointiHakutoiveDTO.setHakutoive(hakemusDTO.getPrioriteetti());
                    hakijaRaportointiDTO.getHakutoiveet().add(raportointiHakutoiveDTO);
                }

                KevytHakutoiveenValintatapajonoDTO hakutoiveenValintatapajonoDTO = new KevytHakutoiveenValintatapajonoDTO();
                raportointiHakutoiveDTO.getHakutoiveenValintatapajonot().add(hakutoiveenValintatapajonoDTO);
                hakutoiveenValintatapajonoDTO.setValintatapajonoOid(valintatapajono.getOid());
                hakutoiveenValintatapajonoDTO.setEiVarasijatayttoa(Optional.ofNullable(valintatapajono.getEiVarasijatayttoa()).orElse(false));
                hakutoiveenValintatapajonoDTO.setVarasijojaKaytetaanAlkaen(valintatapajono.getVarasijojaKaytetaanAlkaen());
                hakutoiveenValintatapajonoDTO.setVarasijojaTaytetaanAsti(valintatapajono.getVarasijojaTaytetaanAsti());
                hakutoiveenValintatapajonoDTO.setVarasijanNumero(hakemusDTO.getVarasijanNumero());
                hakutoiveenValintatapajonoDTO.setPisteet(hakemusDTO.getPisteet());
                hakutoiveenValintatapajonoDTO.setJonosija(hakemusDTO.getJonosija());
                hakutoiveenValintatapajonoDTO.setTila(hakemusDTO.getTila());
                hakutoiveenValintatapajonoDTO.setTilanKuvaukset(hakemusDTO.getTilanKuvaukset());
                hakutoiveenValintatapajonoDTO.setHyvaksyttyHarkinnanvaraisesti(hakemusDTO.isHyvaksyttyHarkinnanvaraisesti());
                hakutoiveenValintatapajonoDTO.setValintatapajonoOid(valintatapajono.getOid());
                hakutoiveenValintatapajonoDTO.setHakemuksenTilanViimeisinMuutos(viimeisinHakemuksenTilanMuutos(hakemusDTO));
            }
        }
        while (hakukohteet.hasNext()) {
            KevytHakukohdeDTO hk = hakukohteet.next();
            if (!Objects.equals(hk.getOid(), hakukohde.getOid())) {
                for (KevytValintatapajonoDTO valintatapajono : hk.getValintatapajonot()) {
                    for (KevytHakemusDTO hakemusDTO : valintatapajono.getHakemukset()) {
                        if (hakijat.containsKey(hakemusDTO.getHakemusOid())) {
                            kevytPopulateHakija(hk, valintatapajono, hakemusDTO, hakijat.get(hakemusDTO.getHakemusOid()));
                        }
                    }
                }
            }
        }
        return new ArrayList<>(hakijat.values());
    }

    @Override
    public List<HakijaDTO> convert(List<HakukohdeDTO> hakukohteet, List<Valintatulos> kaikkienValintatulokset) {
        List<HakijaDTO> hakijat = convert(hakukohteet);
        return applyValintatuloksenTiedot(kaikkienValintatulokset, hakijat);
    }

    @Override
    public List<KevytHakijaDTO> convertHakukohde(HakukohdeDTO hakukohde, Iterator<KevytHakukohdeDTO> hakukohteet, Map<String, List<RaportointiValintatulos>> valintatulokset) {
        List<KevytHakijaDTO> hakijat = convertHakukohteenHakijat(hakukohde, hakukohteet);
        return kevytApplyValintatuloksenTiedot(valintatulokset, hakijat);
    }

    private List<HakijaDTO> applyValintatuloksenTiedot(List<Valintatulos> kaikkienValintatulokset, List<HakijaDTO> hakijat) {
        Map<String, List<Valintatulos>> valintatulosMap = mapValintatulokset(kaikkienValintatulokset);
        for (HakijaDTO hakija : hakijat) {
            List<Valintatulos> valintatulokset = valintatulosMap.get(hakija.getHakemusOid());
            if (valintatulokset != null && !valintatulokset.isEmpty()) {
                for (HakutoiveDTO hakutoiveDTO : hakija.getHakutoiveet()) {
                    valintatulokset.stream().filter(v -> v.getHakukohdeOid().equals(hakutoiveDTO.getHakukohdeOid())).forEach(v ->
                        hakutoiveDTO.setVastaanottotieto(EnumConverter.convert(ValintatuloksenTila.class, v.getTila())));
                    for (HakutoiveenValintatapajonoDTO valintatapajonoDTO : hakutoiveDTO.getHakutoiveenValintatapajonot()) {
                        if (valintatapajonoDTO == null) {
                            continue;
                        }
                        for (Valintatulos valintatulos : valintatulokset) {
                            if (valintatulos == null) {
                                continue;
                            }
                            if (valintatulos.getValintatapajonoOid().equals(valintatapajonoDTO.getValintatapajonoOid())) {
                                valintatapajonoDTO.setJulkaistavissa(valintatulos.getJulkaistavissa());
                                valintatapajonoDTO.setHyvaksyttyVarasijalta(valintatulos.getHyvaksyttyVarasijalta());
                                valintatapajonoDTO.setValintatuloksenViimeisinMuutos(viimeisinValintatuloksenMuutos(valintatulos));
                                valintatapajonoDTO.setIlmoittautumisTila(EnumConverter.convert(IlmoittautumisTila.class, valintatulos.getIlmoittautumisTila()));
                            }
                        }
                    }
                }
            }
        }
        return hakijat;
    }

    private List<KevytHakijaDTO> kevytApplyValintatuloksenTiedot(Map<String, List<RaportointiValintatulos>> hakijoidenValintatulokset, List<KevytHakijaDTO> hakijat) {
        for (KevytHakijaDTO hakija : hakijat) {
            List<RaportointiValintatulos> valintatulokset = hakijoidenValintatulokset.get(hakija.getHakemusOid());
            if (valintatulokset != null && !valintatulokset.isEmpty()) {
                for (KevytHakutoiveDTO hakutoiveDTO : hakija.getHakutoiveet()) {
                    for (KevytHakutoiveenValintatapajonoDTO valintatapajonoDTO : hakutoiveDTO.getHakutoiveenValintatapajonot()) {
                        if (valintatapajonoDTO == null) {
                            continue;
                        }
                        for (RaportointiValintatulos valintatulos : valintatulokset) {
                            if (valintatulos != null && valintatulos.valintatapajonoOid.equals(valintatapajonoDTO.getValintatapajonoOid())) {
                                valintatapajonoDTO.setJulkaistavissa(valintatulos.julkaistavissa);
                                valintatapajonoDTO.setHyvaksyttyVarasijalta(valintatulos.hyvaksyttyVarasijalta);
                                valintatapajonoDTO.setValintatuloksenViimeisinMuutos(valintatulos.viimeisinValintatuloksenMuutos);
                                valintatapajonoDTO.setIlmoittautumisTila(valintatulos.ilmoittautumisTila);
                            }
                        }
                    }
                }
            }
        }
        return hakijat;
    }

    private static Date viimeisinHakemuksenTilanMuutos(HakemusDTO hakemus) {
        if (hakemus != null && !hakemus.getTilaHistoria().isEmpty()) {
            return hakemus.getTilaHistoria().get(hakemus.getTilaHistoria().size() - 1).getLuotu();
        }
        return null;
    }

    private static Date viimeisinValintatuloksenMuutos(Valintatulos valintatulos) {
        if (valintatulos != null && !valintatulos.getLogEntries().isEmpty()) {
            return valintatulos.getLogEntries().get(valintatulos.getLogEntries().size() - 1).getLuotu();
        }
        return null;
    }

    private Map<String, List<Valintatulos>> mapValintatulokset(List<Valintatulos> valintatulokset) {
        Map<String, List<Valintatulos>> map = new HashMap<>();
        for (Valintatulos valintatulos : valintatulokset) {
            if (map.containsKey(valintatulos.getHakemusOid())) {
                map.get(valintatulos.getHakemusOid()).add(valintatulos);
            } else {
                List<Valintatulos> v = Lists.newArrayListWithExpectedSize(2);
                v.add(valintatulos);
                map.put(valintatulos.getHakemusOid(), v);
            }
        }
        return map;
    }

    private void applyPistetiedot(HakutoiveDTO dto, List<PistetietoDTO> pistetiedot) {
        for (PistetietoDTO pistetieto : pistetiedot) {
            if (pistetieto.getTunniste() != null) {
                PistetietoDTO pt = null;
                for (PistetietoDTO lpto : dto.getPistetiedot()) {
                    if (pistetieto.getTunniste().equals(lpto.getTunniste())) {
                        pt = lpto;
                    }
                }
                if (pt == null) {
                    pt = new PistetietoDTO();
                    dto.getPistetiedot().add(pt);
                }
                pt.setArvo(pistetieto.getArvo());
                pt.setLaskennallinenArvo(pistetieto.getLaskennallinenArvo());
                pt.setOsallistuminen(pistetieto.getOsallistuminen());
                pt.setTunniste(pistetieto.getTunniste());
            }
        }
    }

    private HakutoiveDTO getOrCreateHakutoive(HakijaDTO hakijaDTO, HakemusDTO hakemusDTO, HakukohdeDTO hakukohde) {
        HakutoiveDTO hakutoiveDTO = null;
        for (HakutoiveDTO hd : hakijaDTO.getHakutoiveet()) {
            if (hd.getHakukohdeOid().equals(hakemusDTO.getHakukohdeOid())) {
                hakutoiveDTO = hd;
            }
        }
        if (hakutoiveDTO == null) {
            hakutoiveDTO = new HakutoiveDTO();
            hakutoiveDTO.setTarjoajaOid(hakemusDTO.getTarjoajaOid());
            hakutoiveDTO.setHakukohdeOid(hakemusDTO.getHakukohdeOid());
            hakutoiveDTO.setKaikkiJonotSijoiteltu(hakukohde.isKaikkiJonotSijoiteltu());
            hakutoiveDTO.setHakutoive(hakemusDTO.getPrioriteetti());
            hakijaDTO.getHakutoiveet().add(hakutoiveDTO);
        }
        return hakutoiveDTO;
    }

    private KevytHakutoiveDTO getOrCreateHakutoive(KevytHakijaDTO hakijaDTO, KevytHakemusDTO hakemusDTO, KevytHakukohdeDTO hakukohde) {
        KevytHakutoiveDTO hakutoiveDTO = null;
        for (KevytHakutoiveDTO hd : hakijaDTO.getHakutoiveet()) {
            if (hd.getHakukohdeOid().equals(hakukohde.getOid())) {
                hakutoiveDTO = hd;
            }
        }
        if (hakutoiveDTO == null) {
            hakutoiveDTO = new KevytHakutoiveDTO();
            hakutoiveDTO.setTarjoajaOid(hakukohde.getTarjoajaOid());
            hakutoiveDTO.setHakukohdeOid(hakukohde.getOid());
            hakutoiveDTO.setKaikkiJonotSijoiteltu(hakukohde.isKaikkiJonotSijoiteltu());
            hakutoiveDTO.setHakutoive(hakemusDTO.getPrioriteetti());
            hakijaDTO.getHakutoiveet().add(hakutoiveDTO);
        }
        return hakutoiveDTO;
    }

    private HakijaDTO getOrCreateHakijaRaportointiDTO(HashMap<String, HakijaDTO> hakijat, HakemusDTO hakemus) {
        HakijaDTO hakijaRaportointiDTO = hakijat.get(hakemus.getHakemusOid());
        if (hakijaRaportointiDTO == null) {
            hakijaRaportointiDTO = new HakijaDTO();
            hakijaRaportointiDTO.setEtunimi(hakemus.getEtunimi());
            hakijaRaportointiDTO.setSukunimi(hakemus.getSukunimi());
            hakijaRaportointiDTO.setHakijaOid(hakemus.getHakijaOid());
            hakijaRaportointiDTO.setHakemusOid(hakemus.getHakemusOid());
            hakijat.put(hakemus.getHakemusOid(), hakijaRaportointiDTO);
        }
        return hakijaRaportointiDTO;
    }
}
