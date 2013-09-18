package fi.vm.sade.sijoittelu.tulos.service.impl;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.tulos.dao.DAO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaDTO;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.service.RaportointiService;
import fi.vm.sade.sijoittelu.tulos.service.impl.converters.RaportointiConverter;
import fi.vm.sade.sijoittelu.tulos.service.impl.converters.SijoitteluTulosConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        List<Hakukohde> hakukohteet=  dao.getHakukohteetForSijoitteluajo(ajo.getSijoitteluajoId());
        List<HakukohdeDTO> hakukohdeDTOs = sijoitteluTulosConverter.convert(hakukohteet);
        List<HakijaDTO> hakijat  =   raportointiConverter.convert(hakukohdeDTOs);
        return hakijat;
    }

    @Override
    public List<HakijaDTO> koulutuspaikalliset(long sijoitteluajoId){
        List<Hakukohde> hakukohteet=  dao.getHakukohteetForSijoitteluajo(sijoitteluajoId);
        List<HakukohdeDTO> hakukohdeDTOs = sijoitteluTulosConverter.convert(hakukohteet);
        List<HakijaDTO> hakijat  =   raportointiConverter.convert(hakukohdeDTOs);
        return hakijat;
    }

    @Override
    public List<HakijaDTO> latestIlmankoulutuspaikkaa(String hakuOid) {
        SijoitteluAjo ajo =  dao.getLatestSijoitteluajo(hakuOid);
        List<Hakukohde> hakukohteet=  dao.getHakukohteetForSijoitteluajo(ajo.getSijoitteluajoId());
        List<HakukohdeDTO> hakukohdeDTOs = sijoitteluTulosConverter.convert(hakukohteet);
        List<HakijaDTO> hakijat  =   raportointiConverter.convert(hakukohdeDTOs);
        return hakijat;
    }

    @Override
    public List<HakijaDTO> ilmankoulutuspaikkaa(long sijoitteluajoId){
        List<Hakukohde> hakukohteet=  dao.getHakukohteetForSijoitteluajo(sijoitteluajoId);
        List<HakukohdeDTO> hakukohdeDTOs = sijoitteluTulosConverter.convert(hakukohteet);
        List<HakijaDTO> hakijat  =   raportointiConverter.convert(hakukohdeDTOs);
        return hakijat;
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
