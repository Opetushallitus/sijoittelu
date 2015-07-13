package fi.vm.sade.sijoittelu.tulos.dao.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fi.vm.sade.sijoittelu.domain.ErillisSijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.ValiSijoittelu;
import fi.vm.sade.sijoittelu.tulos.dao.ErillisSijoitteluDao;
import fi.vm.sade.sijoittelu.tulos.dao.ValiSijoitteluDao;
import org.mongodb.morphia.Datastore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
public class CachingErillisSijoitteluDao implements ErillisSijoitteluDao {
    private final Logger LOG = LoggerFactory.getLogger(CachingErillisSijoitteluDao.class);

    @Qualifier("datastore")
    @Autowired
    private Datastore morphiaDS;

    private final Cache<String, ErillisSijoittelu> sijoitteluPerHaku = CacheBuilder
            .newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();

    /**
     * CACHED
     */
    @Override
    public Optional<SijoitteluAjo> getLatestSijoitteluajo(String hakuOid) {
        Optional<ErillisSijoittelu> sijoittelu = getSijoitteluByHakuOid(hakuOid);
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
        for (ErillisSijoittelu s : sijoitteluPerHaku.asMap().values()) {
            Optional<SijoitteluAjo> a = s
                    .getSijoitteluajot()
                    .parallelStream()
                    .filter(ajo -> sijoitteluajoId.equals(ajo
                            .getSijoitteluajoId())).findFirst();
            if (a.isPresent()) {
                return a;
            }
        }

        Optional<ErillisSijoittelu> a = Optional.ofNullable(morphiaDS
                .find(ErillisSijoittelu.class).field("sijoitteluajot.sijoitteluajoId")
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
    public Optional<ErillisSijoittelu> getSijoitteluByHakuOid(final String hakuOid) {
        try {
            return Optional.ofNullable(sijoitteluPerHaku.get(hakuOid,
                    () -> morphiaDS.find(ErillisSijoittelu.class).field("hakuOid")
                            .equal(hakuOid).get()));
        } catch (Exception e) {
            LOG.error("Ei saatu sijoittelua haulle!"+ hakuOid, e);
            return Optional.empty();
        }
    }

    @Override
    public void persistSijoittelu(ErillisSijoittelu sijoittelu) {
        morphiaDS.save(sijoittelu);
        sijoitteluPerHaku.put(sijoittelu.getHakuOid(), sijoittelu);
    }
}
