package fi.vm.sade.sijoittelu.tulos.dao;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.Valintatulos;

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

    List<Hakukohde> getHakukohteetForSijoitteluajo(Long id);

    List<Valintatulos> loadValintatulokset(String hakuOid);

    List<Valintatulos> loadValintatulokset(String hakuOid, String hakukohdeOid);

    List<Valintatulos> loadValintatuloksetForHakemus(String hakemusOid);

    List<Hakukohde> getHakukohteetForSijoitteluajo(Long sijoitteluAjoId, String hakukohdeOid);


}
