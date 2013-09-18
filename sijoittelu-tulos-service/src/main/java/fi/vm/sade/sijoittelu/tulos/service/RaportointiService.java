package fi.vm.sade.sijoittelu.tulos.service;

import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaDTO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kkammone
 * Date: 16.9.2013
 * Time: 14:50
 * To change this template use File | Settings | File Templates.
 */
public interface RaportointiService {

    List<HakijaDTO> latestKoulutuspaikalliset(String hakuOid);

    List<HakijaDTO> koulutuspaikalliset(long sijoitteluajoId);

    List<HakijaDTO> latestIlmankoulutuspaikkaa(String hakuOid);

    List<HakijaDTO> ilmankoulutuspaikkaa(long sijoitteluajoId);

    List<HakijaDTO> latestHakijat(String oid);

    List<HakijaDTO> hakijat(long l);
}
