package fi.vm.sade.sijoittelu.tulos.service;

import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.dto.SijoitteluDTO;
import fi.vm.sade.sijoittelu.tulos.dto.SijoitteluajoDTO;

/**
 * Created with IntelliJ IDEA. User: kkammone Date: 5.9.2013 Time: 14:22 To
 * change this template use File | Settings | File Templates.
 */
public interface SijoitteluTulosService {

    HakukohdeDTO getHakukohdeBySijoitteluajo(SijoitteluAjo sijoitteluAjo, String hakukohdeOid);

    SijoitteluajoDTO getSijoitteluajo(SijoitteluAjo sijoitteluAjo);

    SijoitteluDTO getSijoitteluByHakuOid(String hakuOid);
}
