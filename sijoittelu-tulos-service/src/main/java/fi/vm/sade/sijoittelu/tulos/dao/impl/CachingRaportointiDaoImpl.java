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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
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

    private final List<String> hakukohdeCachettavienHakujenOidit = ImmutableList.of(
        "1.2.246.562.29.75203638285", // Korkeakoulujen yhteishaku kevät 2016
        "1.2.246.562.29.14662042044"); // Yhteishaku ammatilliseen ja lukioon, kevät 2016
    private final List<CachedHaunSijoitteluAjonHakukohteet> hakukohdeCachet = new ArrayList<>();

    private final Cache<Long, List<Hakukohde>> hakukohteetMap = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();
    private final Cache<String, List<Valintatulos>> valintatuloksetMap = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).build();
    private final Cache<String, Optional<SijoitteluAjo>> sijoitteluAjoByHaku = CacheBuilder.newBuilder().build();

    @PostConstruct
    public void populateHakukohdeCache() {
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
                    markHakukohdeCacheAsFullyPopulated(sijoitteluajoId);
                } else {
                    LOG.warn("No latest sijoitteluajo found for haku " + hakuOid);
                }
            });
            LOG.info("Populating hakukohdeCache took " + (System.currentTimeMillis() - start) + " ms");
        }).start();
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
            markHakukohdeCacheAsFullyPopulated(sijoitteluajoId);
            purgeHakukohteetOfOlderSijoitteluAjos(hakuOid, sijoitteluajoId);
        }
    }

    private void markHakukohdeCacheAsFullyPopulated(Long sijoitteluajoId) {
        for (CachedHaunSijoitteluAjonHakukohteet c : hakukohdeCachet) {
            if (c.sijoitteluAjoId == sijoitteluajoId) {
                LOG.info(String.format("Marking cache of haku / sijoitteluajo %s / %s as fully populated with %s items",
                    c.hakuOid, c.sijoitteluAjoId, c.hakukohteet.size()));
                c.fullyPopulated = true;
            }
        }
    }

    private void purgeHakukohteetOfOlderSijoitteluAjos(String hakuOid, Long latestSijoitteluAjoId) {
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

    @Override
    public List<Hakukohde> getCachedHakukohteetJoihinHakemusOsallistuu(String hakuOid, long sijoitteluAjoId, String hakemusOid) {
        if (hakukohdeCachettavienHakujenOidit.contains(hakuOid)) {
            for (CachedHaunSijoitteluAjonHakukohteet c : hakukohdeCachet) {
                if (c.hakuOid.equals(hakuOid) && c.sijoitteluAjoId == sijoitteluAjoId) {
                    if (c.fullyPopulated) {
                        List<Hakukohde> kaikkiKohteet = c.hakukohteet;
                        return filtteroidytKopiotHakemuksenHakukohteista(hakemusOid, kaikkiKohteet);
                    } else {
                        LOG.warn(String.format("Cache not fully populated for haku / sijoitteluajo %s / %s " +
                            "when fetching for hakemus %s . Cache size is %s",
                            hakuOid, sijoitteluAjoId, hakemusOid, c.hakukohteet.size()));
                    }
                }
            }
            LOG.warn(String.format("Could not find cache entry for haku / sijoitteluajo %s / %s when fetching for hakemus %s . " +
                "Total cache list size is %s", hakuOid, sijoitteluAjoId, hakemusOid, hakukohdeCachet.size()));
        }

        return hakukohdeDao.haeHakukohteetJoihinHakemusOsallistuu(sijoitteluAjoId, hakemusOid);
    }

    @Override
    public Hakukohde getCachedHakukohde(SijoitteluAjo sijoitteluAjo, String hakukohdeOid) {
        String hakuOid = sijoitteluAjo.getHakuOid();
        Long sijoitteluAjoId = sijoitteluAjo.getSijoitteluajoId();
        if (hakukohdeCachettavienHakujenOidit.contains(hakuOid)) {
            for (CachedHaunSijoitteluAjonHakukohteet c : hakukohdeCachet) {
                if (c.sijoitteluAjoId == sijoitteluAjoId && c.hakuOid.equals(hakuOid)) {
                    for (Hakukohde hakukohde : c.hakukohteet) {
                        if (hakukohde.getOid().equals(hakukohdeOid)) {
                            return hakukohde;
                        }
                    }
                }
            }
        }
        return hakukohdeDao.getHakukohdeForSijoitteluajo(sijoitteluAjoId, hakukohdeOid);
    }

    @Override
    public void updateHakukohdeCacheWith(Hakukohde hakukohde, String hakuOid) {
        if (!hakukohdeCachettavienHakujenOidit.contains(hakuOid)) {
            return;
        }

        LOG.info("Updating hakukohde cache of haku " + hakuOid + " with hakukohde " + hakukohde.getOid());
        boolean foundBySijoitteluajoId = false;
        for (CachedHaunSijoitteluAjonHakukohteet c : hakukohdeCachet) {
            foundBySijoitteluajoId = c.updateWith(hakukohde, foundBySijoitteluajoId);
        }
        if (!foundBySijoitteluajoId) {
            List<Hakukohde> hakukohteet = new ArrayList<>();
            hakukohteet.add(hakukohde);
            hakukohdeCachet.add(new CachedHaunSijoitteluAjonHakukohteet(hakuOid, hakukohde.getSijoitteluajoId(), hakukohteet));
        }
    }

    private List<Hakukohde> filtteroidytKopiotHakemuksenHakukohteista(String hakemusOid, List<Hakukohde> kaikkiKohteet) {
        List<Hakukohde> vainHakemuksenHakukohteetVainHakemuksenTietojenKanssa = new ArrayList<>();
        for (Hakukohde hakukohde : kaikkiKohteet) {
            Hakukohde filtteroityKohde = null;
            for (Valintatapajono jono : hakukohde.getValintatapajonot()) {
                for (Hakemus hakemus: jono.getHakemukset()) {
                    if (hakemus.getHakemusOid().equals(hakemusOid)) {
                        if (filtteroityKohde == null) {
                            filtteroityKohde = new Hakukohde();
                            vainHakemuksenHakukohteetVainHakemuksenTietojenKanssa.add(filtteroityKohde);
                            List<Valintatapajono> filtteroidytJonot = new ArrayList<>(hakukohde.getValintatapajonot().size());
                            filtteroityKohde.setValintatapajonot(filtteroidytJonot);
                            filtteroityKohde.setHakijaryhmat(hakukohde.getHakijaryhmat());
                            filtteroityKohde.setOid(hakukohde.getOid());
                            filtteroityKohde.setId(hakukohde.getId());
                            filtteroityKohde.setKaikkiJonotSijoiteltu(hakukohde.isKaikkiJonotSijoiteltu());
                            filtteroityKohde.setTarjoajaOid(hakukohde.getTarjoajaOid());
                            filtteroityKohde.setTila(hakukohde.getTila());
                            filtteroityKohde.setSijoitteluajoId(hakukohde.getSijoitteluajoId());
                        }

                        Valintatapajono filtteroityJono = new Valintatapajono();
                        filtteroityJono.setHakemukset(Collections.singletonList(hakemus));

                        filtteroityJono.setTasasijasaanto(jono.getTasasijasaanto());
                        filtteroityJono.setPrioriteetti(jono.getPrioriteetti());
                        filtteroityJono.setAloituspaikat(jono.getAloituspaikat());
                        filtteroityJono.setAlinHyvaksyttyPistemaara(jono.getAlinHyvaksyttyPistemaara());
                        filtteroityJono.setAlkuperaisetAloituspaikat(jono.getAlkuperaisetAloituspaikat());
                        filtteroityJono.setEiVarasijatayttoa(jono.getEiVarasijatayttoa());
                        filtteroityJono.setHyvaksytty(jono.getHyvaksytty());
                        filtteroityJono.setKaikkiEhdonTayttavatHyvaksytaan(jono.getKaikkiEhdonTayttavatHyvaksytaan());
                        filtteroityJono.setNimi(jono.getNimi());
                        filtteroityJono.setOid(jono.getOid());
                        filtteroityJono.setPoissaOlevaTaytto(jono.getPoissaOlevaTaytto());
                        filtteroityJono.setTayttojono(jono.getTayttojono());
                        filtteroityJono.setValintaesitysHyvaksytty(jono.getValintaesitysHyvaksytty());
                        filtteroityJono.setVarasijojaTaytetaanAsti(jono.getVarasijojaTaytetaanAsti());
                        filtteroityJono.setVarasijojaKaytetaanAlkaen(jono.getVarasijojaKaytetaanAlkaen());
                        filtteroityJono.setVarasijaTayttoPaivat(jono.getVarasijaTayttoPaivat());
                        filtteroityJono.setVarasijat(jono.getVarasijat());
                        filtteroityJono.setVaralla(jono.getVaralla());
                        filtteroityJono.setTila(jono.getTila());
                        filtteroityKohde.getValintatapajonot().add(filtteroityJono);
                    }
                }
            }
        }
        return vainHakemuksenHakukohteetVainHakemuksenTietojenKanssa;
    }

    private boolean containsHakukohde(Optional<SijoitteluAjo> presentAjoJustByHaku, String hakukohdeOid) {
        return presentAjoJustByHaku.get().getHakukohteet().stream().anyMatch(hakukohdeItem ->
            hakukohdeOid.equals(hakukohdeItem.getOid()));
    }

    private static class CachedHaunSijoitteluAjonHakukohteet {
        public final String hakuOid;
        public final long sijoitteluAjoId;
        public final List<Hakukohde> hakukohteet;
        public boolean fullyPopulated = false;

        private CachedHaunSijoitteluAjonHakukohteet(String hakuOid, long sijoitteluAjoId, List<Hakukohde> hakukohteet) {
            this.hakuOid = hakuOid;
            this.sijoitteluAjoId = sijoitteluAjoId;
            this.hakukohteet = hakukohteet;
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
            }
            return foundBySijoitteluajoId;
        }
    }
}
