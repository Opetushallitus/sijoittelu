package fi.vm.sade.sijoittelu.tulos.service.impl;

import fi.vm.sade.sijoittelu.tulos.dao.DAO;
import fi.vm.sade.sijoittelu.tulos.dto.HakijaRaportointiDTO;
import fi.vm.sade.sijoittelu.tulos.service.RaportointiService;
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
    private SijoitteluTulosConverter sijoitteluTulosConverter;


    @Override
    public List<HakijaRaportointiDTO> latestKoulutuspaikalliset(String hakuOid){
        return null;
    }

    @Override
    public List<HakijaRaportointiDTO> koulutuspaikalliset(long sijoitteluajoId){
        return null;
    }

    @Override
    public List<HakijaRaportointiDTO> latestIlmankoulutuspaikkaa(String hakuOid, String hakemusOid){
        return null;
    }

    @Override
    public List<HakijaRaportointiDTO> ilmankoulutuspaikkaa(long sijoitteluajoId){
        return null;
    }

}
