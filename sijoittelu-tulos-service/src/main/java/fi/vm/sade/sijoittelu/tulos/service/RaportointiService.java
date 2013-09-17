package fi.vm.sade.sijoittelu.tulos.service;

import fi.vm.sade.sijoittelu.domain.dto.HakijaRaportointiDTO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kkammone
 * Date: 16.9.2013
 * Time: 14:50
 * To change this template use File | Settings | File Templates.
 */
public interface RaportointiService {
    List<HakijaRaportointiDTO> latestKoulutuspaikalliset(String oid);

    List<HakijaRaportointiDTO> Koulutuspaikalliset(long l);

    List<HakijaRaportointiDTO> latestIlmankoulutuspaikkaa(String oid, String oid1);

    List<HakijaRaportointiDTO> ilmankoulutuspaikkaa(long l);
}
