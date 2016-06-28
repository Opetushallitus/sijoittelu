package fi.vm.sade.sijoittelu.tulos.dao.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.tulos.dao.CachingRaportointiDao;
import fi.vm.sade.sijoittelu.tulos.dao.HakukohdeDao;
import fi.vm.sade.sijoittelu.tulos.dao.SijoitteluDao;
import fi.vm.sade.sijoittelu.tulos.dao.ValintatulosDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Component
public class CachingRaportointiDaoImpl implements CachingRaportointiDao {
    private static final Logger LOG = LoggerFactory.getLogger(CachingRaportointiDaoImpl.class);

    @Autowired
    private ValintatulosDao valintatulosDao;

    @Autowired
    private HakukohdeDao hakukohdeDao;

    @Autowired
    private SijoitteluDao sijoitteluDao;

    private final Cache<Long, List<Hakukohde>> hakukohteetMap = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();
    private final Cache<String, List<Valintatulos>> valintatuloksetMap = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();
    private final Cache<String, Optional<SijoitteluAjo>> sijoitteluAjoByHaku = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();

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

    @Override
    public Optional<SijoitteluAjo> getCachedLatestSijoitteluAjo(String hakuOid) {
        try {
            return sijoitteluAjoByHaku.get(hakuOid, () -> {
                LOG.info("Ei löytynyt viimeisintä sijoitteluajoa cachesta haulle " + hakuOid + " , ladataan Mongosta.");
                return sijoitteluDao.getLatestSijoitteluajo(hakuOid);
            });
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<SijoitteluAjo> getCachedLatestSijoitteluAjo(String hakuOid, String hakukohdeOid) {
        Optional<SijoitteluAjo> ajoJustByHaku = getCachedLatestSijoitteluAjo(hakuOid);
        if (ajoJustByHaku.isPresent() && containsHakukohde(ajoJustByHaku, hakukohdeOid)) {
            return ajoJustByHaku;
        }
        return sijoitteluDao.getLatestSijoitteluajo(hakuOid, hakukohdeOid);
    }

    @Override
    public void updateLatestAjoCacheWith(Sijoittelu sijoittelu) {
        String hakuOid = sijoittelu.getHakuOid();
        Optional<SijoitteluAjo> latestAjo = Optional.ofNullable(sijoittelu.getLatestSijoitteluajo());
        LOG.info("Päivitetään cacheen haun " + hakuOid + " viimeisin sijoitteluajo " + latestAjo.map(SijoitteluAjo::getSijoitteluajoId));
        sijoitteluAjoByHaku.put(hakuOid, latestAjo);
    }

    private boolean containsHakukohde(Optional<SijoitteluAjo> presentAjoJustByHaku, String hakukohdeOid) {
        return presentAjoJustByHaku.get().getHakukohteet().stream().anyMatch(hakukohdeItem ->
            hakukohdeOid.equals(hakukohdeItem.getOid()));
    }
}
