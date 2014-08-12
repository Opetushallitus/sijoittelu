package fi.vm.sade.sijoittelu.laskenta.dao.impl;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.laskenta.dao.Dao;
import fi.vm.sade.sijoittelu.tulos.dao.SijoitteluCacheDao;

import javax.annotation.PostConstruct;

/**
 * 
 * @author Kari Kammonen
 * 
 */
@Repository
public class DaoImpl implements Dao, SijoitteluCacheDao {

	private final Logger LOG = LoggerFactory.getLogger(DaoImpl.class);
	@Qualifier("datastore")
	@Autowired
	private Datastore morphiaDS;

	private final Cache<String, Sijoittelu> sijoitteluPerHaku = CacheBuilder
			.newBuilder().expireAfterWrite(12, TimeUnit.HOURS).build();

    @PostConstruct
    public void ensureIndexes() {
        morphiaDS.ensureIndexes(Hakukohde.class);
        morphiaDS.ensureIndexes(Valintatulos.class);
    }

	/**
	 * CACHED
	 */
	@Override
	public SijoitteluAjo getLatestSijoitteluajo(String hakuOid) {
		Sijoittelu s = getSijoitteluByHakuOid(hakuOid);
		if (s == null) {
			return null;
		}
		return s.getLatestSijoitteluajo();
	}

	/**
	 * CACHED
	 */
	@Override
	public SijoitteluAjo getSijoitteluajo(Long sijoitteluajoId) {
		if (sijoitteluajoId == null) {
			return null;
		}
		for (Sijoittelu s : sijoitteluPerHaku.asMap().values()) {
			for (SijoitteluAjo a : s.getSijoitteluajot()) {
				if (sijoitteluajoId.equals(a.getSijoitteluajoId())) {
					return a;
				}
			}
		}
		Query<Sijoittelu> query = morphiaDS.createQuery(Sijoittelu.class);
		query.field("sijoitteluajot.sijoitteluajoId").equal(sijoitteluajoId);
		Sijoittelu a = query.get();
		sijoitteluPerHaku.put(a.getHakuOid(), a);
		for (SijoitteluAjo sa : a.getSijoitteluajot()) {
			if (sijoitteluajoId.equals(sa.getSijoitteluajoId())) {
				return sa;
			}
		}
		return null;
	}

	/**
	 * CACHED
	 */
	@Override
	public Sijoittelu getSijoitteluByHakuOid(final String hakuOid) {
		try {
			return sijoitteluPerHaku.get(hakuOid, new Callable<Sijoittelu>() {
				@Override
				public Sijoittelu call() throws Exception {
					Query<Sijoittelu> query = morphiaDS
							.createQuery(Sijoittelu.class);
					query.field("hakuOid").equal(hakuOid);
					return query.get();
				}
			});
		} catch (Exception e) {
			LOG.error("Ei saatu sijoittelua haulle {}: {}", hakuOid,
					Arrays.asList(e.getStackTrace()));
		}
		return null;
	}

	@Override
	public void persistSijoittelu(Sijoittelu sijoittelu) {
		morphiaDS.save(sijoittelu);
		sijoitteluPerHaku.put(sijoittelu.getHakuOid(), sijoittelu);

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
