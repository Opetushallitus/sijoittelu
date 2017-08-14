package fi.vm.sade.sijoittelu.tulos.service.impl;

import com.google.common.collect.Sets;
import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.sijoittelu.tulos.dao.CachingRaportointiDao;
import fi.vm.sade.sijoittelu.tulos.dao.HakukohdeDao;
import fi.vm.sade.sijoittelu.tulos.dao.SijoitteluDao;
import fi.vm.sade.sijoittelu.tulos.dao.ValintatulosDao;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.dto.KevytHakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.dto.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.*;
import fi.vm.sade.sijoittelu.tulos.resource.SijoitteluResource;
import fi.vm.sade.sijoittelu.tulos.service.RaportointiService;
import fi.vm.sade.sijoittelu.tulos.service.impl.comparators.HakijaDTOComparator;
import fi.vm.sade.sijoittelu.tulos.service.impl.comparators.KevytHakijaDTOComparator;
import fi.vm.sade.sijoittelu.tulos.service.impl.converters.RaportointiConverter;
import fi.vm.sade.sijoittelu.tulos.service.impl.converters.SijoitteluTulosConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Collections.*;

/**
 * Sijoittelun raportointiin liittyvat metodit. Erotettu varsinaisesta
 * tulosservicesta
 */
@Service
@Deprecated
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
    public HakijaDTO hakemus(String hakuOid, String sijoitteluajoId, String hakemusOid) {
        Long realSijoitteluAjoId;
        if (SijoitteluResource.LATEST.equals(sijoitteluajoId)) {
            realSijoitteluAjoId = cachingRaportointiDao.getCachedLatestSijoitteluAjo(hakuOid).map(SijoitteluAjo::getSijoitteluajoId).orElse(null);
        } else {
            realSijoitteluAjoId = Long.parseLong(sijoitteluajoId);
        }
        if (realSijoitteluAjoId == null) {
            return null;
        }

        List<Hakukohde> hakukohteetJoihinHakemusOsallistuu = cachingRaportointiDao.getCachedHakukohteetJoihinHakemusOsallistuu(hakuOid, realSijoitteluAjoId, hakemusOid);
        List<Valintatulos> valintatulokset = valintatulosDao.loadValintatuloksetForHakemus(hakemusOid);
        List<HakukohdeDTO> hakukohdeDTOs = sijoitteluTulosConverter.convert(hakukohteetJoihinHakemusOsallistuu);
        List<HakijaDTO> hakijat = raportointiConverter.convert(hakukohdeDTOs, valintatulokset);
        return filterHakemus(hakijat, hakemusOid);
    }

    //Ei toimi, pitää korjata ja testata, jos tarvitaan hyväksymiskirjeitä varten; kts. myös valintarekisteriRaportointiServiceImpl vts:n puolella!!!
    @Deprecated
    private void laskeAlinHyvaksyttyPisteetEnsimmaiselleHakijaryhmalle(List<Hakukohde> hakukohteet) {
        Predicate<Hakemus> isHyvaksytty = hakemus -> HakemuksenTila.HYVAKSYTTY.equals(hakemus.getTila()) || HakemuksenTila.VARASIJALTA_HYVAKSYTTY.equals(hakemus.getTila());
        hakukohteet.stream().forEach(
                h -> {
                    Optional<Hakijaryhma> ensimmainenHakijaryhma = h.getHakijaryhmat().stream().findFirst();
                    Optional<Hakijaryhma> ensimmainenHakijaryhmaJosHyvaksyttyjaHakijoita = ensimmainenHakijaryhma.filter(r -> r.getHakemusOid() != null && !r.getHakemusOid().isEmpty());
                    ensimmainenHakijaryhmaJosHyvaksyttyjaHakijoita.ifPresent(
                            ensisijaistenHakijaryhma -> {
                                Set<String> hyvaksytyt = Sets.newHashSet(ensisijaistenHakijaryhma.getHakemusOid());
                                Stream<Hakemus> hakemuksetKaikissaJonoissa = h.getValintatapajonot()
                                        .stream().flatMap(jono -> Optional.ofNullable(jono.getHakemukset()).orElse(emptyList()).stream());

                                Optional<BigDecimal> minimiPisteet = hakemuksetKaikissaJonoissa.
                                        // hakijalla pisteita
                                                filter(hakemus -> hakemus.getPisteet() != null)
                                        // hakija hyvaksytty
                                        .filter(isHyvaksytty).filter(hyvaksytyt::contains) //Ei toimi, koska filteröidään hakemus-streamia string-setillä!!!
                                        // reduce to pienimmat pisteet
                                        .map(Hakemus::getPisteet).reduce((a, b) -> a.compareTo(b) < 0 ? a : b);

                                ensisijaistenHakijaryhma.setAlinHyvaksyttyPistemaara(minimiPisteet.orElse(null));
                            });
                }
        );
    }

    @Override
    public HakijaPaginationObject cachedHakemukset(SijoitteluAjo ajo, Boolean hyvaksytyt, Boolean ilmanHyvaksyntaa, Boolean vastaanottaneet, List<String> hakukohdeOids, Integer count, Integer index) {
        List<Valintatulos> valintatulokset = cachingRaportointiDao.getCachedValintatulokset(ajo.getHakuOid()).get();
        List<Hakukohde> hakukohteet = cachingRaportointiDao.getCachedHakukohdesForSijoitteluajo(ajo.getSijoitteluajoId()).get();
        laskeAlinHyvaksyttyPisteetEnsimmaiselleHakijaryhmalle(hakukohteet);
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
        sort(hakijat, new HakijaDTOComparator());
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
                if (valintatapajono.getTila() == fi.vm.sade.sijoittelu.tulos.dto.HakemuksenTila.HYVAKSYTTY || valintatapajono.getTila() == fi.vm.sade.sijoittelu.tulos.dto.HakemuksenTila.VARASIJALTA_HYVAKSYTTY) {
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

    @Deprecated //ei käytössä, voi poistaa?
    private List<HakijaDTO> applyPagination(List<HakijaDTO> result,
                                            Integer count, Integer index) {
        if (index != null && count != null) {
            return result.subList(index, Math.min(index + count, result.size()));
        } else if (index != null) {
            return result.subList(index, result.size());
        } else if (count != null) {
            return result.subList(0, Math.min(count, result.size()));
        }
        return result;
    }
}
