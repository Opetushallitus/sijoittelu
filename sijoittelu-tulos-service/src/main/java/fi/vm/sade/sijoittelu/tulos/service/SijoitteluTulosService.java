package fi.vm.sade.sijoittelu.tulos.service;

import fi.vm.sade.sijoittelu.domain.dto.HakemusDTO;
import fi.vm.sade.sijoittelu.domain.dto.HakukohdeDTO;
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

    List<HakemusDTO> haeHakukohteetJoihinHakemusOsallistuu(Long id, String oid);

    HakukohdeDTO getHakukohdeBySijoitteluajo(Long id, String oid);

    SijoitteluajoDTO getSijoitteluajo(Long id);
}
