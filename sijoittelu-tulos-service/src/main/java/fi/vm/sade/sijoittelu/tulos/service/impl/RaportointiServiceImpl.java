package fi.vm.sade.sijoittelu.tulos.service.impl;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import fi.vm.sade.sijoittelu.tulos.dao.CachingRaportointiDao;
import fi.vm.sade.sijoittelu.tulos.dao.HakukohdeDao;
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
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaPaginationObject;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakutoiveDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakutoiveenValintatapajonoDTO;
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
    public HakijaPaginationObject cachedHakemukset(SijoitteluAjo ajo, Boolean hyvaksytyt, Boolean ilmanHyvaksyntaa, Boolean vastaanottaneet, List<String> hakukohdeOids, Integer count, Integer index) {
        List<Valintatulos> valintatulokset = cachingRaportointiDao.getCachedValintatulokset(ajo.getHakuOid()).get();
        List<Hakukohde> hakukohteet = cachingRaportointiDao.getCachedHakukohdesForSijoitteluajo(ajo.getSijoitteluajoId()).get();
        return konvertoiHakijat(hyvaksytyt, ilmanHyvaksyntaa, vastaanottaneet, hakukohdeOids, count, index, valintatulokset, hakukohteet);
    }

    private HakijaPaginationObject konvertoiHakijat(final Boolean hyvaksytyt, final Boolean ilmanHyvaksyntaa, final Boolean vastaanottaneet, final List<String> hakukohdeOids, final Integer count, final Integer index, final List<Valintatulos> valintatulokset, final List<Hakukohde> hakukohteet) {
        List<HakukohdeDTO> hakukohdeDTOs = sijoitteluTulosConverter.convert(hakukohteet);
        List<HakijaDTO> hakijat = raportointiConverter.convert(hakukohdeDTOs, valintatulokset);
        Collections.sort(hakijat, new HakijaDTOComparator());
        HakijaPaginationObject paginationObject = new HakijaPaginationObject();
        List<HakijaDTO> result = new ArrayList<HakijaDTO>();
        for (HakijaDTO hakija : hakijat) {
            if (filter(hakija, hyvaksytyt, ilmanHyvaksyntaa, vastaanottaneet, hakukohdeOids)) {
                result.add(hakija);
            }
        }
        paginationObject.setTotalCount(result.size());
        paginationObject.setResults(applyPagination(result, count, index));
        return paginationObject;
    }

    private boolean filter(HakijaDTO hakija, Boolean hyvaksytyt, Boolean ilmanHyvaksyntaa, Boolean vastaanottaneet, List<String> hakukohdeOids) {
        boolean isPartOfHakukohdeList = false;
        boolean isHyvaksytty = false;
        boolean isVastaanottanut = false;

        for (HakutoiveDTO hakutoiveDTO : hakija.getHakutoiveet()) {
            if (hakukohdeOids != null && hakukohdeOids.contains(hakutoiveDTO.getHakukohdeOid())) {
                isPartOfHakukohdeList = true;
            }
            for (HakutoiveenValintatapajonoDTO valintatapajono : hakutoiveDTO.getHakutoiveenValintatapajonot()) {
                if (valintatapajono.getTila() == HakemuksenTila.HYVAKSYTTY || valintatapajono.getTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY) {
                    isHyvaksytty = true;
                }
                if (valintatapajono.getVastaanottotieto() == ValintatuloksenTila.VASTAANOTTANUT || valintatapajono.getVastaanottotieto() == ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI) {
                    isVastaanottanut = true;
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
