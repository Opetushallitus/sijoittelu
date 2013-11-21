package fi.vm.sade.sijoittelu.tulos.service;

import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
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


    SijoitteluAjo getSijoitteluAjo(Long SijoitteluajoId);

    SijoitteluAjo latestSijoitteluAjoForHaku(String hakuOid);

      /*
    List<HakijaDTO> hyvaksytyt(SijoitteluAjo sijoitteluAjo);

    Collection<HakijaDTO> hyvaksytyt(SijoitteluAjo sijoitteluAjo, String hakukohdeOid);

    List<HakijaDTO> ilmanhyvaksyntaa(SijoitteluAjo sijoitteluAjo);

    List<HakijaDTO> ilmanhyvaksyntaa(SijoitteluAjo sijoitteluAjo, String hakukohdeOid);

    Collection<HakijaDTO> vastaanottaneet(SijoitteluAjo sijoitteluAjo);

    Collection<HakijaDTO> vastaanottaneet(SijoitteluAjo sijoitteluAjo, String hakukohdeOid);
    */


    HakijaDTO hakemus(SijoitteluAjo sijoitteluAjo, String hakemusOid);

    List<String> hakemukset(SijoitteluAjo ajo, List<String> vastaanottotieto, List<String> tila,  List <String> hakukohdeOid, Integer count, Integer index);
}
