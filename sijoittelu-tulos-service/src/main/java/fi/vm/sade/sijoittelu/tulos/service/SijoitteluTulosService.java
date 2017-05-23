package fi.vm.sade.sijoittelu.tulos.service;

import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.dto.SijoitteluDTO;
import fi.vm.sade.sijoittelu.tulos.dto.SijoitteluajoDTO;

@Deprecated
public interface SijoitteluTulosService {
    HakukohdeDTO getHakukohdeBySijoitteluajo(SijoitteluAjo sijoitteluAjo, String hakukohdeOid);

    SijoitteluajoDTO getSijoitteluajo(SijoitteluAjo sijoitteluAjo);

    SijoitteluDTO getSijoitteluByHakuOid(String hakuOid);
}
