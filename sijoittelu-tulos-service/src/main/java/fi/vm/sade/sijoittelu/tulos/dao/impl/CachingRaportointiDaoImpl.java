package fi.vm.sade.sijoittelu.tulos.dao.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;

import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.tulos.dao.CachingRaportointiDao;
import fi.vm.sade.sijoittelu.tulos.dao.HakukohdeDao;
import fi.vm.sade.sijoittelu.tulos.dao.SijoitteluDao;
import fi.vm.sade.sijoittelu.tulos.dao.ValintatulosDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

    @Value(value = "${sijoittelu-service.hakukohdeCache.populate:true}")
    private boolean populateHakukohdeCache;

    private final List<String> hakukohdeCachettavienHakujenOidit = ImmutableList.of(
        "1.2.246.562.29.75203638285", // Korkeakoulujen yhteishaku kevät 2016
        "1.2.246.562.29.14662042044", // Yhteishaku ammatilliseen ja lukioon, kevät 2016
        "1.2.246.562.29.94318919571", // Perusopetuksen jälkeisen valmistavan koulutuksen kevään 2016 haku
        "1.2.246.562.29.669559278110" // Haku erityisopetuksena järjestettävään ammatilliseen koulutukseen, kevät 2016
    );

    private final HakukohdeCache hakukohdeCache = new HakukohdeCache();

    private final Cache<Long, List<Hakukohde>> hakukohteetMap = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();
    private final Cache<String, List<Valintatulos>> valintatuloksetMap = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();
    private final Cache<String, Optional<SijoitteluAjo>> sijoitteluAjoByHaku = CacheBuilder.newBuilder().build();

    @PostConstruct
    public void populateHakukohdeCache() {
        if (populateHakukohdeCache) {
            LOG.info("Starting hakukohdeCache populating thread for haku oids: " + hakukohdeCachettavienHakujenOidit);
            new Thread(() -> {
                LOG.info("Populating hakukohdeCache for haku oids: " + hakukohdeCachettavienHakujenOidit);
                long start = System.currentTimeMillis();
                hakukohdeCachettavienHakujenOidit.stream().forEach(hakuOid -> {
                    Optional<SijoitteluAjo> latestSijoitteluAjo = getCachedLatestSijoitteluAjo(hakuOid);
                    if (latestSijoitteluAjo.isPresent()) {
                        Long sijoitteluajoId = latestSijoitteluAjo.get().getSijoitteluajoId();
                        List<Hakukohde> hakukohteet = hakukohdeDao.getHakukohdeForSijoitteluajo(sijoitteluajoId);
                        hakukohteet.forEach(hakukohde -> updateHakukohdeCacheWith(hakukohde, hakuOid));
                        hakukohdeCache.markAsFullyPopulated(sijoitteluajoId);
                    } else {
                        LOG.warn("No latest sijoitteluajo found for haku " + hakuOid);
                    }
                });
                LOG.info("Populating hakukohdeCache took " + (System.currentTimeMillis() - start) + " ms");
            }).start();
        } else {
            LOG.info("Not populating hakukohdeCache");
        }
    }

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
        if (latestAjo.isPresent() && hakukohdeCachettavienHakujenOidit.contains(hakuOid)) {
            Long sijoitteluajoId = latestAjo.get().getSijoitteluajoId();
            hakukohdeCache.markAsFullyPopulated(sijoitteluajoId);
            hakukohdeCache.purgeHakukohteetOfOlderSijoitteluAjos(hakuOid, sijoitteluajoId);
        }
    }

    @Override
    public List<Hakukohde> getCachedHakukohteetJoihinHakemusOsallistuu(String hakuOid, long sijoitteluAjoId, String hakemusOid) {
        if (hakukohdeCachettavienHakujenOidit.contains(hakuOid)) {
            Optional<List<Hakukohde>> hakukohteet = hakukohdeCache.findHakukohteetOfHakemus(hakuOid, sijoitteluAjoId, hakemusOid);
            if (hakukohteet.isPresent()) {
                return hakukohteet.get();
            }
        }

        return hakukohdeDao.haeHakukohteetJoihinHakemusOsallistuu(sijoitteluAjoId, hakemusOid);
    }

    @Override
    public Hakukohde getCachedHakukohde(SijoitteluAjo sijoitteluAjo, String hakukohdeOid) {
        String hakuOid = sijoitteluAjo.getHakuOid();
        Long sijoitteluAjoId = sijoitteluAjo.getSijoitteluajoId();
        if (hakukohdeCachettavienHakujenOidit.contains(hakuOid)) {
            Optional<Hakukohde> hakukohde = hakukohdeCache.findHakukohde(hakuOid, sijoitteluAjoId, hakukohdeOid);
            if (hakukohde.isPresent()) {
                return hakukohde.get();
            }
        }
        return hakukohdeDao.getHakukohdeForSijoitteluajo(sijoitteluAjoId, hakukohdeOid);
    }

    @Override
    public void updateHakukohdeCacheWith(Hakukohde hakukohde, String hakuOid) {
        if (!hakukohdeCachettavienHakujenOidit.contains(hakuOid)) {
            return;
        }
        hakukohdeCache.updateWith(hakuOid, hakukohde);
    }

    private boolean containsHakukohde(Optional<SijoitteluAjo> presentAjoJustByHaku, String hakukohdeOid) {
        return presentAjoJustByHaku.get().getHakukohteet().stream().anyMatch(hakukohdeItem ->
            hakukohdeOid.equals(hakukohdeItem.getOid()));
    }

    private static class CachedHaunSijoitteluAjonHakukohteet {
        public final String hakuOid;
        public final long sijoitteluAjoId;
        public final List<Hakukohde> hakukohteet = new ArrayList<>();
        public final Map<String, Set<HakemusCacheObject>> hakemusItems = new HashMap<>();
        public boolean fullyPopulated = false;

        private CachedHaunSijoitteluAjonHakukohteet(String hakuOid, long sijoitteluAjoId) {
            this.hakuOid = hakuOid;
            this.sijoitteluAjoId = sijoitteluAjoId;
        }

        private synchronized boolean updateWith(Hakukohde hakukohde, boolean foundBySijoitteluajoId) {
            if (sijoitteluAjoId == hakukohde.getSijoitteluajoId()) {
                foundBySijoitteluajoId = true;
                int indexOfHakukohdeToReplace = -1;
                int i = 0;
                Iterator<Hakukohde> hakukohdeIterator = hakukohteet.iterator();
                while (hakukohdeIterator.hasNext() && indexOfHakukohdeToReplace == -1) {
                    if (hakukohdeIterator.next().getOid().equals(hakukohde.getOid())) {
                        indexOfHakukohdeToReplace = i;
                    }
                    i = i + 1;
                }
                if (indexOfHakukohdeToReplace == -1) {
                    hakukohteet.add(hakukohde);
                } else {
                    hakukohteet.set(indexOfHakukohdeToReplace, hakukohde);
                }
                updateHakemusCacheWith(hakukohde);
            }
            return foundBySijoitteluajoId;
        }

        private void updateHakemusCacheWith(Hakukohde newKohde) {
            for (Valintatapajono newJono : newKohde.getValintatapajonot()) {
                for (Hakemus newHakemus : newJono.getHakemukset()) {
                    hakemusItems.compute(newHakemus.getHakemusOid(), (k, hakemusCacheObjects) -> {
                        if (hakemusCacheObjects == null) {
                            hakemusCacheObjects = new HashSet<>();
                        }
                        HakemusCacheObject toRemove = null;
                        for (HakemusCacheObject h : hakemusCacheObjects) {
                            if (h.hakukohde.getOid().equals(newKohde.getOid()) && h.valintatapajono.getOid().equals(newJono.getOid())) {
                                toRemove = h;
                            }
                        }
                        if (toRemove != null) {
                            hakemusCacheObjects.remove(toRemove);
                        }
                        hakemusCacheObjects.add(new HakemusCacheObject(newKohde, newJono, newHakemus));
                        return hakemusCacheObjects;
                    });
                }
            }
        }
    }

    private static class HakukohdeCache {
        private final List<CachedHaunSijoitteluAjonHakukohteet> hakukohdeCachet = new ArrayList<>();

        private synchronized void updateWith(String hakuOid, Hakukohde hakukohde) {
            LOG.debug("Updating hakukohde cache of haku " + hakuOid + " with hakukohde " + hakukohde.getOid());
            boolean foundBySijoitteluajoId = false;
            for (CachedHaunSijoitteluAjonHakukohteet c : hakukohdeCachet) {
                foundBySijoitteluajoId = c.updateWith(hakukohde, foundBySijoitteluajoId);
            }
            if (!foundBySijoitteluajoId) {
                CachedHaunSijoitteluAjonHakukohteet newCacheItem = new CachedHaunSijoitteluAjonHakukohteet(hakuOid, hakukohde.getSijoitteluajoId());
                newCacheItem.updateWith(hakukohde, false);
                hakukohdeCachet.add(newCacheItem);
            }
        }

        private synchronized void purgeHakukohteetOfOlderSijoitteluAjos(String hakuOid, Long latestSijoitteluAjoId) {
            List<CachedHaunSijoitteluAjonHakukohteet> cacheEntriesToRemove = new ArrayList<>();
            for (CachedHaunSijoitteluAjonHakukohteet c : hakukohdeCachet) {
                if (c.hakuOid.equals(hakuOid)) {
                    if (c.sijoitteluAjoId < latestSijoitteluAjoId) {
                        LOG.info(String.format("Purging old hakukohde cache of haku / sijoitteluajo %s / %s with %s items, because latest sijoitteluajo is %s",
                                c.hakuOid, c.sijoitteluAjoId, c.hakukohteet.size(), latestSijoitteluAjoId));
                        c.fullyPopulated = false;
                        cacheEntriesToRemove.add(c);
                    }
                }
            }
            LOG.info(String.format("Removing %d entries of haku %s", cacheEntriesToRemove.size(), hakuOid));
            hakukohdeCachet.removeAll(cacheEntriesToRemove);
        }

        private synchronized Optional<List<Hakukohde>> findHakukohteetOfHakemus(String hakuOid, long sijoitteluAjoId, String hakemusOid) {
            for (CachedHaunSijoitteluAjonHakukohteet c : hakukohdeCachet) {
                if (c.hakuOid.equals(hakuOid) && c.sijoitteluAjoId == sijoitteluAjoId) {
                    if (c.fullyPopulated) {
                        long start = System.currentTimeMillis();
                        List<Hakukohde> foundFilteredKohteet = filtteroidytKopiotHakemuksenHakukohteista(c.hakemusItems.get(hakemusOid));
                        long elapsed = System.currentTimeMillis() - start;
                        if (elapsed > 100) {
                            LOG.warn(String.format("Retrieving hakuOid / sijoitteluAjoId / hakemusOid %s / %s / %s took %s milliseconds",
                                hakuOid, sijoitteluAjoId, hakemusOid, elapsed));
                        }
                        return Optional.of(foundFilteredKohteet);
                    } else {
                        LOG.warn(String.format("Cache not fully populated for haku / sijoitteluajo %s / %s " +
                                        "when fetching for hakemus %s . Cache size is %s",
                                hakuOid, sijoitteluAjoId, hakemusOid, c.hakukohteet.size()));
                    }
                }
            }
            LOG.warn(String.format("Could not find cache entry for haku / sijoitteluajo %s / %s when fetching for hakemus %s . " +
                    "Total cache list size is %s", hakuOid, sijoitteluAjoId, hakemusOid, hakukohdeCachet.size()));
            return Optional.empty();
        }

        private synchronized Optional<Hakukohde> findHakukohde(String hakuOid, long sijoitteluAjoId, String hakukohdeOid) {
            for (CachedHaunSijoitteluAjonHakukohteet c : hakukohdeCachet) {
                if (c.sijoitteluAjoId == sijoitteluAjoId && c.hakuOid.equals(hakuOid)) {
                    if (c.fullyPopulated) {
                        for (Hakukohde hakukohde : c.hakukohteet) {
                            if (hakukohde.getOid().equals(hakukohdeOid)) {
                                return Optional.of(hakukohde);
                            }
                        }
                    } else {
                        LOG.warn(String.format("Cache not fully populated for haku / sijoitteluajo %s / %s " +
                                        "when fetching hakukohde %s . Cache size is %s",
                                hakuOid, sijoitteluAjoId, hakukohdeOid, c.hakukohteet.size()));
                    }
                }
            }
            LOG.warn(String.format("Could not find cache entry for haku / sijoitteluajo %s / %s when fetching hakukohde %s . " +
                    "Total cache list size is %s", hakuOid, sijoitteluAjoId, hakukohdeOid, hakukohdeCachet.size()));
            return Optional.empty();
        }

        private synchronized void markAsFullyPopulated(Long sijoitteluajoId) {
            for (CachedHaunSijoitteluAjonHakukohteet c : hakukohdeCachet) {
                if (c.sijoitteluAjoId == sijoitteluajoId) {
                    LOG.info(String.format("Marking cache of haku / sijoitteluajo %s / %s as fully populated with %s items",
                            c.hakuOid, c.sijoitteluAjoId, c.hakukohteet.size()));
                    c.fullyPopulated = true;
                }
            }
        }

        private List<Hakukohde> filtteroidytKopiotHakemuksenHakukohteista(Set<HakemusCacheObject> hakemusCacheObjects) {
            if (hakemusCacheObjects == null) {
                return new ArrayList<>();
            }
            Map<String, Hakukohde> filteredHakukohteetByOid = new HashMap<>();
            for (HakemusCacheObject cacheObject : hakemusCacheObjects) {
                Hakukohde filteredHakuKohde = filteredHakukohteetByOid.compute(cacheObject.hakukohde.getOid(), (k, kohde) -> {
                    if (kohde == null) {
                        kohde = new Hakukohde();
                        BeanUtils.copyProperties(cacheObject.hakukohde, kohde);
                        kohde.setValintatapajonot(new ArrayList<>());
                    }
                    return kohde;
                });

                Valintatapajono filteredJono = new Valintatapajono();
                BeanUtils.copyProperties(cacheObject.valintatapajono, filteredJono);
                filteredJono.setHakemukset(Collections.singletonList(cacheObject.hakemus));
                filteredHakuKohde.getValintatapajonot().add(filteredJono);
            }
            return new ArrayList<>(filteredHakukohteetByOid.values());
        }
    }
}
