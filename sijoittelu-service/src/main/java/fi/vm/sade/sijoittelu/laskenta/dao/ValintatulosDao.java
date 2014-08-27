package fi.vm.sade.sijoittelu.laskenta.dao;

import java.util.List;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.Valintatulos;

/**
 * 
 * @author Kari Kammonen
 * 
 */
public interface ValintatulosDao {
	Valintatulos loadValintatulos(String hakukohdeOid,
			String valintatapajonoOid, String hakemusOid);

	void createOrUpdateValintatulos(Valintatulos tulos);

	List<Valintatulos> loadValintatulokset(String hakuOid);

	List<Valintatulos> loadValintatuloksetForHakukohde(String hakukohdeOid);

	List<Valintatulos> loadValintatulos(String oid);

	List<Valintatulos> loadValintatulokset(String hakukohdeOid,
			String valintatapajonoOid);
}
