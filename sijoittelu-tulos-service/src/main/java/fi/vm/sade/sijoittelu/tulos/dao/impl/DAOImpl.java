package fi.vm.sade.sijoittelu.tulos.dao.impl;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.tulos.dao.DAO;
import org.apache.commons.lang.StringUtils;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * User: tommiha Date: 10/15/12 Time: 2:44 PM
 */
@Repository
public class DAOImpl implements DAO {

    @Qualifier("datastore")
	@Autowired
	private Datastore morphiaDS;

    @Override
	public List<Sijoittelu> getSijoittelu() {
		Query<Sijoittelu> query = morphiaDS.createQuery(Sijoittelu.class);
		return query.asList();
	}

    @Override
	public Hakukohde getHakukohdeBySijoitteluajo(Long sijoitteluajoId,
			String hakukohdeOid) {
		Query<Hakukohde> query = morphiaDS.createQuery(Hakukohde.class);
		query.field("sijoitteluajoId").equal(sijoitteluajoId);
		query.field("oid").equal(hakukohdeOid);
		return query.get();
	}

	@Override
	public List<Hakukohde> haeHakukohteetJoihinHakemusOsallistuu(
			Long sijoitteluajoId, String hakemusOid) {
		Query<Hakukohde> query = morphiaDS.createQuery(Hakukohde.class);
		query.field("sijoitteluajoId").equal(sijoitteluajoId);
		query.field("valintatapajonot.hakemukset.hakemusOid").equal(hakemusOid);
		List<Hakukohde> list = query.asList();
		return list;
	}

	@Override
	public List<Hakukohde> getHakukohteetForSijoitteluajo(Long sijoitteluAjoId) {
		Query<Hakukohde> query = morphiaDS.createQuery(Hakukohde.class);
		query.field("sijoitteluajoId").equal(sijoitteluAjoId);
		return query.asList();
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
	public List<Valintatulos> loadValintatuloksetForHakemus(String hakemusOid) {
		if (StringUtils.isBlank(hakemusOid)) {
			throw new RuntimeException(
					"Invalid search params, fix exception later");
		}
		Query<Valintatulos> q = morphiaDS.createQuery(Valintatulos.class);
		q.criteria("hakemusOid").equal(hakemusOid);
		return q.asList();
	}
}
