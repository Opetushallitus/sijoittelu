package fi.vm.sade.sijoittelu.tulos.service;

import java.util.List;

import fi.vm.sade.sijoittelu.tulos.dto.HakemusDTO;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.dto.SijoitteluDTO;
import fi.vm.sade.sijoittelu.tulos.dto.SijoitteluajoDTO;

/**
 * Created with IntelliJ IDEA. User: kkammone Date: 5.9.2013 Time: 14:22 To
 * change this template use File | Settings | File Templates.
 */
public interface SijoitteluTulosService {

    List<HakemusDTO> haeHakukohteetJoihinHakemusOsallistuu(Long sijoitteluajoId, String hakemusOid);

    HakukohdeDTO getHakukohdeBySijoitteluajo(Long sijoitteluajoId, String hakukohdeOid);

    SijoitteluajoDTO getSijoitteluajo(Long sijoitteluajoId);

    SijoitteluDTO getSijoitteluByHakuOid(String hakuOid);

    SijoitteluajoDTO getLatestSijoitteluajo(String hakuOid);

    HakukohdeDTO getLatestHakukohdeBySijoitteluajo(String hakuOid, String hakukohdeOid);

    List<HakemusDTO> haeLatestHakukohteetJoihinHakemusOsallistuu(String hakuOid, String hakemusOid);
}
