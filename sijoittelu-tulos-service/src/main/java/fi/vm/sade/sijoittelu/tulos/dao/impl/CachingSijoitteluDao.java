package fi.vm.sade.sijoittelu.tulos.dao.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.tulos.dao.SijoitteluDao;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Repository
public class CachingSijoitteluDao implements SijoitteluDao {

	private final Logger LOG = LoggerFactory
			.getLogger(CachingSijoitteluDao.class);

	@Qualifier("datastore")
	@Autowired
	private Datastore morphiaDS;

	private final Cache<String, Sijoittelu> sijoitteluPerHaku = CacheBuilder
			.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();

	/**
	 * CACHED
	 */
	@Override
	public Optional<SijoitteluAjo> getLatestSijoitteluajo(String hakuOid) {
		Optional<Sijoittelu> sijoittelu = getSijoitteluByHakuOid(hakuOid);
		return sijoittelu.map(
				s -> Optional.ofNullable(s.getLatestSijoitteluajo())).orElse(
				Optional.empty());

	}

	/**
	 * CACHED
	 */
	@Override
	public Optional<SijoitteluAjo> getSijoitteluajo(Long sijoitteluajoId) {
		if (sijoitteluajoId == null) {
			return Optional.empty();
		}

		for (Sijoittelu s : sijoitteluPerHaku.asMap().values()) {
			Optional<SijoitteluAjo> a = s
					.getSijoitteluajot()
					.parallelStream()
					.filter(ajo -> sijoitteluajoId.equals(ajo
							.getSijoitteluajoId())).findFirst();
			if (a.isPresent()) {
				return a;
			}
		}

		Optional<Sijoittelu> a = Optional.ofNullable(morphiaDS
				.find(Sijoittelu.class).field("sijoitteluajot.sijoitteluajoId")
				.equal(sijoitteluajoId).get());

		a.ifPresent(s -> sijoitteluPerHaku.put(s.getHakuOid(), s));

		if (a.isPresent()) {
			Optional<SijoitteluAjo> sa = a
					.get()
					.getSijoitteluajot()
					.parallelStream()
					.filter(ajo -> sijoitteluajoId.equals(ajo
							.getSijoitteluajoId())).findFirst();
			if (sa.isPresent()) {
				return sa;
			}
		}

		return Optional.empty();
	}

	/**
	 * CACHED
	 */
	@Override
	public Optional<Sijoittelu> getSijoitteluByHakuOid(final String hakuOid) {
		try {
			return Optional.ofNullable(sijoitteluPerHaku.get(hakuOid,
					() -> morphiaDS.find(Sijoittelu.class).field("hakuOid")
							.equal(hakuOid).get()));
		} catch (Exception e) {
			LOG.debug("Ei saatu sijoittelua haulle {}: {}", hakuOid,
					e.getMessage());
			return Optional.empty();
		}

	}

	@Override
	public Optional<Sijoittelu> getSijoitteluById(long id) {
		try {
			return Optional.ofNullable(morphiaDS.find(Sijoittelu.class)
					.field("sijoitteluId").equal(id).get());
		} catch (Exception e) {
			LOG.debug("Ei saatu sijoittelua {}: {}", id, e.getMessage());
			return Optional.empty();
		}
	}

	@Override
	public void clearCacheForHaku(String hakuoid) {
		sijoitteluPerHaku.invalidate(hakuoid);
	}

	@Override
	public List<Sijoittelu> findAll() {
		return morphiaDS.find(Sijoittelu.class).asList();
	}

	@Override
	public void persistSijoittelu(Sijoittelu sijoittelu) {
		morphiaDS.save(sijoittelu);
		sijoitteluPerHaku.put(sijoittelu.getHakuOid(), sijoittelu);

	}
}
