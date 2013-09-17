package fi.vm.sade.sijoittelu.tulos.service;

import fi.vm.sade.sijoittelu.tulos.dto.HakijaRaportointiDTO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kkammone
 * Date: 16.9.2013
 * Time: 14:50
 * To change this template use File | Settings | File Templates.
 */
public interface RaportointiService {
    List<HakijaRaportointiDTO> latestKoulutuspaikalliset(String hakuOid);

    List<HakijaRaportointiDTO> koulutuspaikalliset(long sijoitteluajoId);

    List<HakijaRaportointiDTO> latestIlmankoulutuspaikkaa(String hakuOid, String hakemusOid);

    List<HakijaRaportointiDTO> ilmankoulutuspaikkaa(long sijoitteluajoId);
}
