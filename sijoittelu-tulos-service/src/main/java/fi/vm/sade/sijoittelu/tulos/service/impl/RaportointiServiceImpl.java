package fi.vm.sade.sijoittelu.tulos.service.impl;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.tulos.dao.DAO;
import fi.vm.sade.sijoittelu.tulos.dto.HakemuksenTila;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakutoiveDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakutoiveenValintatapajonoDTO;
import fi.vm.sade.sijoittelu.tulos.service.RaportointiService;
import fi.vm.sade.sijoittelu.tulos.service.impl.converters.RaportointiConverter;
import fi.vm.sade.sijoittelu.tulos.service.impl.converters.SijoitteluTulosConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Sijoittelun raportointiin liittyvat metodit. Erotettu varsinaisesta tulosservicesta
 *
 * Created with IntelliJ IDEA.
 * User: kkammone
 * Date: 16.9.2013
 * Time: 14:51
 * To change this template use File | Settings | File Templates.
 */
@Service
public class RaportointiServiceImpl implements RaportointiService {


    @Autowired
    private DAO dao;

    @Autowired
    private RaportointiConverter raportointiConverter;

    @Autowired
    private SijoitteluTulosConverter sijoitteluTulosConverter;


    @Override
    public List<HakijaDTO> latestKoulutuspaikalliset(String hakuOid){
        SijoitteluAjo ajo =  dao.getLatestSijoitteluajo(hakuOid);
        if(ajo==null)  {
            return new ArrayList<HakijaDTO>();
        }
        List<Hakukohde> hakukohteet=  dao.getHakukohteetForSijoitteluajo(ajo.getSijoitteluajoId());
        List<HakukohdeDTO> hakukohdeDTOs = sijoitteluTulosConverter.convert(hakukohteet);
        List<HakijaDTO> hakijat  =   raportointiConverter.convert(hakukohdeDTOs);
        return filterkoulutuspaikalliset( hakijat);
    }

    @Override
    public List<HakijaDTO> koulutuspaikalliset(long sijoitteluajoId){
        List<Hakukohde> hakukohteet=  dao.getHakukohteetForSijoitteluajo(sijoitteluajoId);
        List<HakukohdeDTO> hakukohdeDTOs = sijoitteluTulosConverter.convert(hakukohteet);
        List<HakijaDTO> hakijat  =   raportointiConverter.convert(hakukohdeDTOs);
        return filterkoulutuspaikalliset( hakijat);
    }

    @Override
    public List<HakijaDTO> latestIlmankoulutuspaikkaa(String hakuOid) {
        SijoitteluAjo ajo =  dao.getLatestSijoitteluajo(hakuOid);
        if(ajo==null)  {
            return new ArrayList<HakijaDTO>();
        }
        List<Hakukohde> hakukohteet=  dao.getHakukohteetForSijoitteluajo(ajo.getSijoitteluajoId());
        List<HakukohdeDTO> hakukohdeDTOs = sijoitteluTulosConverter.convert(hakukohteet);
        List<HakijaDTO> hakijat  =   raportointiConverter.convert(hakukohdeDTOs);
        return filterIlmanKoulutuspaikkaa(hakijat);
    }

    @Override
    public List<HakijaDTO> ilmankoulutuspaikkaa(long sijoitteluajoId){
        List<Hakukohde> hakukohteet=  dao.getHakukohteetForSijoitteluajo(sijoitteluajoId);
        List<HakukohdeDTO> hakukohdeDTOs = sijoitteluTulosConverter.convert(hakukohteet);
        List<HakijaDTO> hakijat  =   raportointiConverter.convert(hakukohdeDTOs);
        return filterIlmanKoulutuspaikkaa(hakijat);
    }

    private List<HakijaDTO> filterIlmanKoulutuspaikkaa(List<HakijaDTO>  kaikkiHakijat){
        List<HakijaDTO> ilmanKoulutuspaikkaa = new ArrayList<HakijaDTO>();
        for(HakijaDTO hakijaDTO : kaikkiHakijat) {
            boolean lisataan = true;
            for(HakutoiveDTO hakutoiveDTO : hakijaDTO.getHakutoiveet())  {
                for(HakutoiveenValintatapajonoDTO vt: hakutoiveDTO.getHakutoiveenValintatapajonot()) {
                    if(vt.getTila() == HakemuksenTila.HYVAKSYTTY) {
                        lisataan = false;
                    }
                }
            }
            if(lisataan) {
                ilmanKoulutuspaikkaa.add(hakijaDTO);
            }
        }
        return ilmanKoulutuspaikkaa;
    }
    private List<HakijaDTO> filterkoulutuspaikalliset(List<HakijaDTO>  kaikkiHakijat){
        List<HakijaDTO> ilmanKoulutuspaikkaa = new ArrayList<HakijaDTO>();
        for(HakijaDTO hakijaDTO : kaikkiHakijat) {
            boolean lisataan = false;
            for(HakutoiveDTO hakutoiveDTO : hakijaDTO.getHakutoiveet())  {
                for(HakutoiveenValintatapajonoDTO vt: hakutoiveDTO.getHakutoiveenValintatapajonot()) {
                    if(vt.getTila() == HakemuksenTila.HYVAKSYTTY) {
                        lisataan = true;
                    }
                }
            }
            if(lisataan) {
                ilmanKoulutuspaikkaa.add(hakijaDTO);
            }
        }
        return ilmanKoulutuspaikkaa;
    }

    @Override
    public List<HakijaDTO> latestHakijat(String hakuOid) {
        SijoitteluAjo ajo =  dao.getLatestSijoitteluajo(hakuOid);
        List<Hakukohde> hakukohteet=  dao.getHakukohteetForSijoitteluajo(ajo.getSijoitteluajoId());
        List<HakukohdeDTO> hakukohdeDTOs = sijoitteluTulosConverter.convert(hakukohteet);
        List<HakijaDTO> hakijat  =   raportointiConverter.convert(hakukohdeDTOs);
        return hakijat;
    }

    @Override
    public List<HakijaDTO> hakijat(long sijoitteluajoId) {
        List<Hakukohde> hakukohteet=  dao.getHakukohteetForSijoitteluajo(sijoitteluajoId);
        List<HakukohdeDTO> hakukohdeDTOs = sijoitteluTulosConverter.convert(hakukohteet);
        List<HakijaDTO> hakijat  =   raportointiConverter.convert(hakukohdeDTOs);
        return hakijat;
    }

}
