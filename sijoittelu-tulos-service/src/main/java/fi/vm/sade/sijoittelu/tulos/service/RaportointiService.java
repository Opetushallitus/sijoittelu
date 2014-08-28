package fi.vm.sade.sijoittelu.tulos.service;

import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaPaginationObject;

import java.util.List;
import java.util.Optional;

/**
 * Created with IntelliJ IDEA.
 * User: kkammone
 * Date: 16.9.2013
 * Time: 14:50
 * To change this template use File | Settings | File Templates.
 */
public interface RaportointiService {


    Optional<SijoitteluAjo> getSijoitteluAjo(Long SijoitteluajoId);

    Optional<SijoitteluAjo> latestSijoitteluAjoForHaku(String hakuOid);

    HakijaDTO hakemus(SijoitteluAjo sijoitteluAjo, String hakemusOid);

    HakijaPaginationObject hakemukset(SijoitteluAjo ajo,Boolean hyvaksytyt, Boolean ilmanHyvaksyntaa,Boolean vastaanottaneet, List <String> hakukohdeOid, Integer count, Integer index);
}
