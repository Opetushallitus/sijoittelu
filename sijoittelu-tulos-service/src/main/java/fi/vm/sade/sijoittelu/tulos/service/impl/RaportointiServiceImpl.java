package fi.vm.sade.sijoittelu.tulos.service.impl;

import java.util.*;

import fi.vm.sade.sijoittelu.tulos.dao.CachingRaportointiDao;
import fi.vm.sade.sijoittelu.tulos.dao.HakukohdeDao;
import fi.vm.sade.sijoittelu.tulos.dto.KevytHakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.*;
import fi.vm.sade.sijoittelu.tulos.service.impl.comparators.KevytHakijaDTOComparator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.tulos.dao.SijoitteluDao;
import fi.vm.sade.sijoittelu.tulos.dao.ValintatulosDao;
import fi.vm.sade.sijoittelu.tulos.dto.HakemuksenTila;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.dto.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.tulos.service.RaportointiService;
import fi.vm.sade.sijoittelu.tulos.service.impl.comparators.HakijaDTOComparator;
import fi.vm.sade.sijoittelu.tulos.service.impl.converters.RaportointiConverter;
import fi.vm.sade.sijoittelu.tulos.service.impl.converters.SijoitteluTulosConverter;

/**
 * Sijoittelun raportointiin liittyvat metodit. Erotettu varsinaisesta
 * tulosservicesta
 */
@Service
public class RaportointiServiceImpl implements RaportointiService {
    @Autowired
    private ValintatulosDao valintatulosDao;

    @Autowired
    private HakukohdeDao hakukohdeDao;

    @Autowired
    private SijoitteluDao sijoitteluDao;

    @Autowired
    private CachingRaportointiDao cachingRaportointiDao;

    @Autowired
    private RaportointiConverter raportointiConverter;

    @Autowired
    private SijoitteluTulosConverter sijoitteluTulosConverter;

    @Override
    public Optional<SijoitteluAjo> getSijoitteluAjo(Long SijoitteluajoId) {
        return sijoitteluDao.getSijoitteluajo(SijoitteluajoId);
    }

    @Override
    public Optional<SijoitteluAjo> latestSijoitteluAjoForHaku(String hakuOid) {
        return sijoitteluDao.getLatestSijoitteluajo(hakuOid);
    }

    @Override
    public Optional<SijoitteluAjo> latestSijoitteluAjoForHakukohde(String hakuOid, String hakukohdeOid) {
        return sijoitteluDao.getLatestSijoitteluajo(hakuOid, hakukohdeOid);
    }

    @Override
    public HakijaDTO hakemus(SijoitteluAjo sijoitteluAjo, String hakemusOid) {
        List<Hakukohde> hakukohteetJoihinHakemusOsallistuu = hakukohdeDao.haeHakukohteetJoihinHakemusOsallistuu(sijoitteluAjo.getSijoitteluajoId(), hakemusOid);
        List<Valintatulos> valintatulokset = valintatulosDao.loadValintatuloksetForHakemus(hakemusOid);
        List<HakukohdeDTO> hakukohdeDTOs = sijoitteluTulosConverter.convert(hakukohteetJoihinHakemusOsallistuu);
        List<HakijaDTO> hakijat = raportointiConverter.convert(hakukohdeDTOs, valintatulokset);
        return filterHakemus(hakijat, hakemusOid);
    }

    /**
     * Unfortunately this has to be done like this, on the positive side, these
     * results can be cached, only valintatulokset needs to be refreshed, EVER!
     */
    @Override
    public HakijaPaginationObject hakemukset(SijoitteluAjo ajo, Boolean hyvaksytyt, Boolean ilmanHyvaksyntaa, Boolean vastaanottaneet, List<String> hakukohdeOids, Integer count, Integer index) {
        List<Valintatulos> valintatulokset = valintatulosDao.loadValintatulokset(ajo.getHakuOid());
        List<Hakukohde> hakukohteet = hakukohdeDao.getHakukohdeForSijoitteluajo(ajo.getSijoitteluajoId());
        return konvertoiHakijat(hyvaksytyt, ilmanHyvaksyntaa, vastaanottaneet, hakukohdeOids, count, index, valintatulokset, hakukohteet);
    }

    @Override
    public List<KevytHakijaDTO> hakemukset(SijoitteluAjo ajo, String hakukohdeOid) {
        Map<String, List<RaportointiValintatulos>> hakukohteenValintatulokset = valintatulosDao.loadValintatuloksetForHakukohteenHakijat(hakukohdeOid);
        Iterator<KevytHakukohdeDTO> hakukohteet = hakukohdeDao.getHakukohdeForSijoitteluajoIterator(ajo.getSijoitteluajoId(), hakukohdeOid);
        Hakukohde hakukohde = hakukohdeDao.getHakukohdeForSijoitteluajo(ajo.getSijoitteluajoId(), hakukohdeOid);
        if (hakukohde == null) {
            return Collections.emptyList();
        }
        return konvertoiHakijat(hakukohde, hakukohteenValintatulokset, hakukohteet);
    }

    @Override
    public List<KevytHakijaDTO> hakemuksetVainHakukohteenTietojenKanssa(SijoitteluAjo ajo, String hakukohdeOid) {
        Hakukohde hakukohde = hakukohdeDao.getHakukohdeForSijoitteluajo(ajo.getSijoitteluajoId(), hakukohdeOid);
        if (hakukohde == null) {
            return Collections.emptyList();
        }
        return konvertoiHakijat(hakukohde, Collections.emptyMap(), Collections.emptyIterator());
    }

    @Override
    public HakijaPaginationObject cachedHakemukset(SijoitteluAjo ajo, Boolean hyvaksytyt, Boolean ilmanHyvaksyntaa, Boolean vastaanottaneet, List<String> hakukohdeOids, Integer count, Integer index) {
        List<Valintatulos> valintatulokset = cachingRaportointiDao.getCachedValintatulokset(ajo.getHakuOid()).get();
        List<Hakukohde> hakukohteet = cachingRaportointiDao.getCachedHakukohdesForSijoitteluajo(ajo.getSijoitteluajoId()).get();
        return konvertoiHakijat(hyvaksytyt, ilmanHyvaksyntaa, vastaanottaneet, hakukohdeOids, count, index, valintatulokset, hakukohteet);
    }

    @Override
    public Optional<SijoitteluAjo> cachedLatestSijoitteluAjoForHaku(String hakuOid) {
        return cachingRaportointiDao.getCachedLatestSijoitteluAjo(hakuOid);
    }

    @Override
    public Optional<SijoitteluAjo> cachedLatestSijoitteluAjoForHakukohde(String hakuOid, String hakukohdeOid) {
        return cachingRaportointiDao.getCachedLatestSijoitteluAjo(hakuOid, hakukohdeOid);
    }

    private HakijaPaginationObject konvertoiHakijat(final Boolean hyvaksytyt, final Boolean ilmanHyvaksyntaa, final Boolean vastaanottaneet, final List<String> hakukohdeOids, final Integer count, final Integer index, final List<Valintatulos> valintatulokset, final List<Hakukohde> hakukohteet) {
        List<HakukohdeDTO> hakukohdeDTOs = sijoitteluTulosConverter.convert(hakukohteet);
        List<HakijaDTO> hakijat = raportointiConverter.convert(hakukohdeDTOs, valintatulokset);
        Collections.sort(hakijat, new HakijaDTOComparator());
        HakijaPaginationObject paginationObject = new HakijaPaginationObject();
        List<HakijaDTO> result = new ArrayList<>();
        for (HakijaDTO hakija : hakijat) {
            if (filter(hakija, hyvaksytyt, ilmanHyvaksyntaa, vastaanottaneet, hakukohdeOids)) {
                result.add(hakija);
            }
        }
        paginationObject.setTotalCount(result.size());
        paginationObject.setResults(applyPagination(result, count, index));
        return paginationObject;
    }

    private List<KevytHakijaDTO> konvertoiHakijat(Hakukohde hakukohde, Map<String, List<RaportointiValintatulos>> valintatulokset, Iterator<KevytHakukohdeDTO> hakukohteet) {
        List<KevytHakijaDTO> hakijat = raportointiConverter.convertHakukohde(sijoitteluTulosConverter.convert(hakukohde), hakukohteet, valintatulokset);
        Collections.sort(hakijat, new KevytHakijaDTOComparator());
        return hakijat;
    }

    private boolean filter(HakijaDTO hakija, Boolean hyvaksytyt, Boolean ilmanHyvaksyntaa, Boolean vastaanottaneet, List<String> hakukohdeOids) {
        boolean isPartOfHakukohdeList = false;
        boolean isHyvaksytty = false;
        boolean isVastaanottanut = false;

        for (HakutoiveDTO hakutoiveDTO : hakija.getHakutoiveet()) {
            if (hakukohdeOids != null && hakukohdeOids.contains(hakutoiveDTO.getHakukohdeOid())) {
                isPartOfHakukohdeList = true;
            }
            if (hakutoiveDTO.getVastaanottotieto() == ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI) {
                isVastaanottanut = true;
            }
            for (HakutoiveenValintatapajonoDTO valintatapajono : hakutoiveDTO.getHakutoiveenValintatapajonot()) {
                if (valintatapajono.getTila() == HakemuksenTila.HYVAKSYTTY || valintatapajono.getTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY) {
                    isHyvaksytty = true;
                }
            }
        }
        return ((hakukohdeOids == null || hakukohdeOids.size() <= 0) || isPartOfHakukohdeList)
                && ((!Boolean.TRUE.equals(hyvaksytyt) || isHyvaksytty)
                && (!Boolean.TRUE.equals(ilmanHyvaksyntaa) || !isHyvaksytty) && (!Boolean.TRUE
                .equals(vastaanottaneet) || isVastaanottanut));
    }

    private HakijaDTO filterHakemus(List<HakijaDTO> hakijat, String hakemusOid) {
        for (HakijaDTO hakijaDTO : hakijat) {
            if (hakemusOid.equals(hakijaDTO.getHakemusOid())) {
                return hakijaDTO;
            }
        }
        return null;
    }

    private List<HakijaDTO> applyPagination(List<HakijaDTO> result,
                                            Integer count, Integer index) {
        if (index != null && count != null) {
            return result.subList(index, Math.min(index + count, result.size() - 1));
        } else if (index != null) {
            return result.subList(index, result.size() - 1);
        } else if (count != null) {
            return result.subList(0, Math.min(count, result.size() - 1));
        }
        return result;
    }
}
