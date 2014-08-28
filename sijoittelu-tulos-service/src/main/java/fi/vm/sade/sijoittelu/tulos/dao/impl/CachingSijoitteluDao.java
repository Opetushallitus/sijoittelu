package fi.vm.sade.sijoittelu.tulos.dao.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
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
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Repository
public class CachingSijoitteluDao implements SijoitteluDao {

    private final Logger LOG = LoggerFactory.getLogger(CachingSijoitteluDao.class);

    @Qualifier("datastore")
    @Autowired
    private Datastore morphiaDS;

    private final Cache<String, Sijoittelu> sijoitteluPerHaku = CacheBuilder
            .newBuilder().expireAfterWrite(12, TimeUnit.HOURS).build();

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
}
