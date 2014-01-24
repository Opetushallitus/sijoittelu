package fi.vm.sade.sijoittelu.tulos.service.impl.converters;

import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.tulos.dto.*;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakutoiveDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakutoiveenValintatapajonoDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA. User: kkammone Date: 17.9.2013 Time: 14:49 To
 * change this template use File | Settings | File Templates.
 */
@Component
public class RaportointiConverterImpl implements RaportointiConverter {

    @Override
    public List<HakijaDTO> convert(List<HakukohdeDTO> hakukohteet) {
        HashMap<String, HakijaDTO> hakijat = new HashMap<String, HakijaDTO>();
        for (HakukohdeDTO hakukohde : hakukohteet) {
            for (ValintatapajonoDTO valintatapajono : hakukohde.getValintatapajonot()) {
                for (HakemusDTO hakemusDTO : valintatapajono.getHakemukset()) {
                    HakijaDTO hakijaRaportointiDTO = getOrCreateHakijaRaportointiDTO(hakijat, hakemusDTO);
                    HakutoiveDTO raportointiHakutoiveDTO = getOrCreateHakutoive(hakijaRaportointiDTO, hakemusDTO);
                    HakutoiveenValintatapajonoDTO hakutoiveenValintatapajonoDTO = new HakutoiveenValintatapajonoDTO();
                    raportointiHakutoiveDTO.getHakutoiveenValintatapajonot().add(hakutoiveenValintatapajonoDTO);

                    hakutoiveenValintatapajonoDTO.setHakeneet(valintatapajono.getHakeneet());
                    hakutoiveenValintatapajonoDTO.setAlinHyvaksyttyPistemaara(valintatapajono.getAlinHyvaksyttyPistemaara());
                    hakutoiveenValintatapajonoDTO.setHyvaksytty(valintatapajono.getHyvaksytty());
                    hakutoiveenValintatapajonoDTO.setVaralla(valintatapajono.getVaralla());
                    hakutoiveenValintatapajonoDTO.setHakeneet(valintatapajono.getHakeneet());
                    hakutoiveenValintatapajonoDTO.setValintatapajonoOid(valintatapajono.getOid());
                    hakutoiveenValintatapajonoDTO.setValintatapajonoNimi(valintatapajono.getNimi());

                    hakutoiveenValintatapajonoDTO.setVarasijanNumero(hakemusDTO.getVarasijanNumero());
                    hakutoiveenValintatapajonoDTO.setTasasijaJonosija(hakemusDTO.getTasasijaJonosija());
                    hakutoiveenValintatapajonoDTO.setPisteet(hakemusDTO.getPisteet());
                    hakutoiveenValintatapajonoDTO.setJonosija(hakemusDTO.getJonosija());
                    hakutoiveenValintatapajonoDTO.setTila(EnumConverter.convert(HakemuksenTila.class, hakemusDTO.getTila()));
                    hakutoiveenValintatapajonoDTO.setHyvaksyttyHarkinnanvaraisesti(hakemusDTO.isHyvaksyttyHarkinnanvaraisesti());
                    hakutoiveenValintatapajonoDTO.setPaasyJaSoveltuvuusKokeenTulos(hakemusDTO.getPaasyJaSoveltuvuusKokeenTulos());
                    hakutoiveenValintatapajonoDTO.setValintatapajonoPrioriteetti(hakemusDTO.getPrioriteetti());


                    //hakutoiveenValintatapajonoDTO.setValintatapajonoPrioriteetti(hakemusDTO.getPrioriteetti());
                    hakutoiveenValintatapajonoDTO.setValintatapajonoOid(valintatapajono.getOid());

                    applyPistetiedot(raportointiHakutoiveDTO, hakemusDTO.getPistetiedot());
                }
            }
        }
        return new ArrayList<HakijaDTO>(hakijat.values());
    }

    @Override
    public List<HakijaDTO> convert(List<HakukohdeDTO> hakukohteet, List<Valintatulos> valintatulokset) {
        //convert hakijat
        List<HakijaDTO> hakijat = convert(hakukohteet);
        //apply valintatulos
        Map<String, Valintatulos> valintatulosMap = mapValintatulokset(valintatulokset);
        for(HakijaDTO hakija : hakijat) {
            Valintatulos valintatulos = valintatulosMap.get(hakija.getHakemusOid());
            if(valintatulos!=null) {
                for(HakutoiveDTO hakutoiveDTO : hakija.getHakutoiveet()) {
                    for(HakutoiveenValintatapajonoDTO valintatapajonoDTO : hakutoiveDTO.getHakutoiveenValintatapajonot())  {
                        if(valintatulos.getValintatapajonoOid().equals(valintatapajonoDTO.getValintatapajonoOid())) {
                            valintatapajonoDTO.setVastaanottotieto(EnumConverter.convert(ValintatuloksenTila.class, valintatulos.getTila()));
                        }
                    }
                }
            }
        }
        return hakijat;
    }

    private Map<String, Valintatulos> mapValintatulokset(List<Valintatulos> valintatulokset) {
        Map <String, Valintatulos> map = new HashMap<String, Valintatulos>();
        for(Valintatulos valintatulos : valintatulokset) {
            map.put(valintatulos.getHakemusOid(), valintatulos);
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
                        continue;
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

    private HakutoiveDTO getOrCreateHakutoive(HakijaDTO hakijaDTO, HakemusDTO hakemusDTO) {
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
            hakijaRaportointiDTO.setHakemusOid(hakemus.getHakemusOid());
        }
        hakijat.put(hakemus.getHakemusOid(), hakijaRaportointiDTO);
        return hakijaRaportointiDTO;
    }

}
