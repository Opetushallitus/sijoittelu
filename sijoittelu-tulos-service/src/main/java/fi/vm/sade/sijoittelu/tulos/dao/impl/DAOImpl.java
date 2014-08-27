package fi.vm.sade.sijoittelu.tulos.dao.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.tulos.dao.DAO;
import fi.vm.sade.sijoittelu.tulos.dao.SijoitteluCacheDao;

/**
 * User: tommiha Date: 10/15/12 Time: 2:44 PM
 */
@Repository
public class DAOImpl implements DAO, SijoitteluCacheDao {

    private final Logger LOG = LoggerFactory.getLogger(DAOImpl.class);

    @Qualifier("datastore")
	@Autowired
	private Datastore morphiaDS;

    private final Cache<String, Sijoittelu> sijoitteluPerHaku = CacheBuilder
            .newBuilder().expireAfterWrite(12, TimeUnit.HOURS).build();

    @Override
	public List<Sijoittelu> getSijoittelu() {
		Query<Sijoittelu> query = morphiaDS.createQuery(Sijoittelu.class);
		return query.asList();
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

	/**
	 * This right here is some fucked up shit, but mongoDB & domain
	 * 
	 * @param sijoitteluajoId
	 * @param vastaanottotieto
	 * @param tila
	 * @param hakukohdeOid
	 * @param count
	 * @param index
	 * @return
	 */
	@Override
	public List<String> hakukohteet(Long sijoitteluajoId,
			List<String> vastaanottotieto, List<String> tila,
			List<String> hakukohdeOid, Integer count, Integer index) {

		Query query = morphiaDS.createQuery(Hakukohde.class);
		query.field("sijoitteluajoId").equal(sijoitteluajoId);

		if (tila != null && tila.size() > 0) {
			query.field("valintatapajonot.hakemukset.tila").in(tila);
		}
		if (hakukohdeOid != null && hakukohdeOid.size() > 0) {
			query.field("oid").in(hakukohdeOid);
		}

		query.retrievedFields(true, "valintatapajonot.hakemukset.hakemusOid");
		LinkedHashSet<String> set = new LinkedHashSet<String>();
		set.addAll(query.asList());
		ArrayList<String> list = new ArrayList<String>();
		list.addAll(set);

		if (count != null && index != null) {
			return list.subList(index, index + count);
		} else if (count != null) {
			return list.subList(0, count);
		}
		return list;

	}

}
