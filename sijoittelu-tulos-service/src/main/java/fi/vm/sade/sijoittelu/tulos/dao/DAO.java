package fi.vm.sade.sijoittelu.tulos.dao;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;

import java.util.List;

/**
 * User: tommiha Date: 10/15/12 Time: 2:44 PM
 */
public interface DAO {

    List<Sijoittelu> getSijoittelu();

    Sijoittelu getSijoitteluByHakuOid(String hakuOid);

    SijoitteluAjo getSijoitteluajo(Long sijoitteluajoId);

    Hakukohde getHakukohdeBySijoitteluajo(Long sijoitteluajoId, String hakukohdeOid);

    List<Hakukohde> haeHakukohteetJoihinHakemusOsallistuu(Long sijoitteluajoId, String hakemusOid);

    SijoitteluAjo getLatestSijoitteluajo(String hakuOid);

    Hakukohde getLatestHakukohdeBySijoitteluajo(String hakuOid, String hakukohdeOid);

    List<Hakukohde> haeLatestHakukohteetJoihinHakemusOsallistuu(String hakuOid, String hakemusOid);

    List<Hakukohde> getHakukohteetForSijoitteluajo(Long id);
}
