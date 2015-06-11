package fi.vm.sade.sijoittelu.tulos.dao.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.tulos.dao.CachingRaportointiDao;
import fi.vm.sade.sijoittelu.tulos.dao.HakukohdeDao;
import fi.vm.sade.sijoittelu.tulos.dao.ValintatulosDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class CachingRaportointiDaoImpl implements CachingRaportointiDao {
    @Autowired
    private ValintatulosDao valintatulosDao;

    @Autowired
    private HakukohdeDao hakukohdeDao;

    private final Cache<Long, List<Hakukohde>> hakukohteetMap = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();
    private final Cache<String, List<Valintatulos>> valintatuloksetMap = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();

    @Override
    public Optional<List<Hakukohde>> getCachedHakukohdesForSijoitteluajo(Long sijoitteluAjoId) {
        try {
            return Optional.ofNullable(hakukohteetMap.get(sijoitteluAjoId,
                    () -> hakukohdeDao.getHakukohdeForSijoitteluajo(sijoitteluAjoId)));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<List<Valintatulos>> getCachedValintatulokset(String hakuOid) {
        try {
            return Optional.ofNullable(valintatuloksetMap.get(hakuOid,
                    () -> valintatulosDao.loadValintatulokset(hakuOid)));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
