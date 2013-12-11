package fi.vm.sade.sijoittelu.tulos.service;

import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaPaginationObject;

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

    HakijaDTO hakemus(SijoitteluAjo sijoitteluAjo, String hakemusOid);

    HakijaPaginationObject hakemukset(SijoitteluAjo ajo,Boolean hyvaksytyt, Boolean ilmanHyvaksyntaa,Boolean vastaanottaneet, List <String> hakukohdeOid, Integer count, Integer index);
}
