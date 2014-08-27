package fi.vm.sade.sijoittelu.laskenta.dao.impl;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.laskenta.dao.HakukohdeDao;
import fi.vm.sade.sijoittelu.laskenta.dao.ValintatulosDao;

import javax.annotation.PostConstruct;

/**
 * 
 * @author Kari Kammonen
 * 
 */
@Repository
public class ValintatulosDaoImpl implements ValintatulosDao, HakukohdeDao {

	@Qualifier("datastore")
	@Autowired
	private Datastore morphiaDS;

    @PostConstruct
    public void ensureIndexes() {
        morphiaDS.ensureIndexes(Hakukohde.class);
        morphiaDS.ensureIndexes(Valintatulos.class);
    }

	@Override
	public void persistHakukohde(Hakukohde hakukohde) {
		morphiaDS.save(hakukohde);
	}

	@Override
	public List<Valintatulos> loadValintatulos(String hakemusOid) {
		if (StringUtils.isBlank(hakemusOid)) {
			throw new RuntimeException(
					"Invalid search params, fix exception later");
		}
		Query<Valintatulos> q = morphiaDS.createQuery(Valintatulos.class);
		q.criteria("hakemusOid").equal(hakemusOid);
		return q.asList();
	}

	@Override
	public Valintatulos loadValintatulos(String hakukohdeOid,
			String valintatapajonoOid, String hakemusOid) {
		if (StringUtils.isBlank(hakukohdeOid)
				|| StringUtils.isBlank(hakukohdeOid)
				|| StringUtils.isBlank(hakemusOid)) {
			throw new RuntimeException(
					"Invalid search params, fix exception later");
		}
		Query<Valintatulos> q = morphiaDS.createQuery(Valintatulos.class);
		q.criteria("hakukohdeOid").equal(hakukohdeOid);
		q.criteria("valintatapajonoOid").equal(valintatapajonoOid);
		q.criteria("hakemusOid").equal(hakemusOid);
		return q.get();
	}

	@Override
	public List<Valintatulos> loadValintatulokset(String hakukohdeOid,
			String valintatapajonoOid) {
		if (StringUtils.isBlank(hakukohdeOid)
				|| StringUtils.isBlank(valintatapajonoOid)) {
			throw new RuntimeException(
					"Invalid search params, fix exception later");
		}
		Query<Valintatulos> q = morphiaDS.createQuery(Valintatulos.class);
		q.and(
		//
		q.criteria("hakukohdeOid").equal(hakukohdeOid),
		//
				q.criteria("valintatapajonoOid").equal(valintatapajonoOid));

		return q.asList();
	}

	@Override
	public List<Valintatulos> loadValintatuloksetForHakukohde(
			String hakukohdeOid) {
		if (StringUtils.isBlank(hakukohdeOid)) {
			throw new RuntimeException(
					"Invalid search params, fix exception later");
		}
		Query<Valintatulos> q = morphiaDS.createQuery(Valintatulos.class);
		q.or(q.criteria("hakukohdeOid").equal(hakukohdeOid));
		return q.asList();
	}

	@Override
	public List<Valintatulos> loadValintatulokset(String hakuOid) {
		if (StringUtils.isBlank(hakuOid)) {
			throw new RuntimeException(
					"Invalid search params, fix exception later");
		}
		Query<Valintatulos> q = morphiaDS.createQuery(Valintatulos.class);
		q.criteria("hakuOid").equal(hakuOid);
		return q.asList();
	}

	@Override
	public Hakukohde getHakukohdeForSijoitteluajo(Long sijoitteluajoId,
			String hakukohdeOid) {
		Query<Hakukohde> q = morphiaDS.createQuery(Hakukohde.class);
		q.criteria("sijoitteluajoId").equal(sijoitteluajoId);
		q.criteria("oid").equal(hakukohdeOid);
		return q.get();
	}

	@Override
	public List<Hakukohde> getHakukohdeForSijoitteluajo(Long sijoitteluajoId) {
		Query<Hakukohde> q = morphiaDS.createQuery(Hakukohde.class);
		q.criteria("sijoitteluajoId").equal(sijoitteluajoId);
		return q.asList();
	}

	@Override
	public void createOrUpdateValintatulos(Valintatulos tulos) {
		morphiaDS.save(tulos);
	}

}
