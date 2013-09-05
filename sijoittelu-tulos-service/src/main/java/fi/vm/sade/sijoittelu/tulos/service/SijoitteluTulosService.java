package fi.vm.sade.sijoittelu.tulos.service;

import fi.vm.sade.sijoittelu.domain.dto.HakemusDTO;
import fi.vm.sade.sijoittelu.domain.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.domain.dto.SijoitteluDTO;
import fi.vm.sade.sijoittelu.domain.dto.SijoitteluajoDTO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kkammone
 * Date: 5.9.2013
 * Time: 14:22
 * To change this template use File | Settings | File Templates.
 */
public interface SijoitteluTulosService {

    List<HakemusDTO> haeHakukohteetJoihinHakemusOsallistuu(String sijoitteluajoId, String hakemusOid);

    HakukohdeDTO getHakukohdeBySijoitteluajo(String sijoitteluajoId, String hakukohdeOid);

    SijoitteluajoDTO getSijoitteluajo(String sijoitteluajoId);

    SijoitteluDTO getSijoitteluByHakuOid(String hakuOid);

    SijoitteluajoDTO getLatestSijoitteluajo(String hakuOid);

    HakukohdeDTO getLatestHakukohdeBySijoitteluajo(String hakuOid, String hakukohdeOid);

    List<HakemusDTO> haeLatestHakukohteetJoihinHakemusOsallistuu(String hakuOid, String hakemusOid);
}
