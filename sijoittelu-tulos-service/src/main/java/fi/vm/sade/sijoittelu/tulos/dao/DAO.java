package fi.vm.sade.sijoittelu.tulos.dao;

import java.util.List;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.Valintatulos;

/**
 * User: tommiha Date: 10/15/12 Time: 2:44 PM
 */
public interface DAO {

	List<Sijoittelu> getSijoittelu();

	Hakukohde getHakukohdeBySijoitteluajo(Long sijoitteluajoId,
			String hakukohdeOid);

	List<Hakukohde> haeHakukohteetJoihinHakemusOsallistuu(Long sijoitteluajoId,
			String hakemusOid);

	List<Hakukohde> getHakukohteetForSijoitteluajo(Long id);

	List<Valintatulos> loadValintatulokset(String hakuOid);

	List<Valintatulos> loadValintatuloksetForHakemus(String hakemusOid);

	List<String> hakukohteet(Long sijoitteluajoId,
			List<String> vastaanottotieto, List<String> tila,
			List<String> hakukohdeOid, Integer count, Integer index);

}
