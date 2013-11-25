package fi.vm.sade.sijoittelu.tulos.service.impl;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.tulos.dao.DAO;
import fi.vm.sade.sijoittelu.tulos.dto.HakemuksenTila;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.dto.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakutoiveDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakutoiveenValintatapajonoDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.PaginationObject;
import fi.vm.sade.sijoittelu.tulos.service.RaportointiService;
import fi.vm.sade.sijoittelu.tulos.service.impl.comparators.HakijaDTOComparator;
import fi.vm.sade.sijoittelu.tulos.service.impl.converters.RaportointiConverter;
import fi.vm.sade.sijoittelu.tulos.service.impl.converters.SijoitteluTulosConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Sijoittelun raportointiin liittyvat metodit. Erotettu varsinaisesta tulosservicesta
 *
 * Created with IntelliJ IDEA.
 * User: kkammone
 * Date: 16.9.2013
 * Time: 14:51
 * To change this template use File | Settings | File Templates.
 */
@Service
public class RaportointiServiceImpl implements RaportointiService {


    @Autowired
    private DAO dao;

    @Autowired
    private RaportointiConverter raportointiConverter;

    @Autowired
    private SijoitteluTulosConverter sijoitteluTulosConverter;



    @Override
    public SijoitteluAjo getSijoitteluAjo(Long SijoitteluajoId) {
        return dao.getSijoitteluajo(SijoitteluajoId);
    }

    @Override
    public SijoitteluAjo latestSijoitteluAjoForHaku(String hakuOid) {
        return dao.getLatestSijoitteluajo(hakuOid);
    }

    @Override
    public HakijaDTO hakemus(SijoitteluAjo sijoitteluAjo, String hakemusOid) {
        List<Hakukohde> hakukohteetJoihinHakemusOsallistuu =    dao.haeHakukohteetJoihinHakemusOsallistuu(sijoitteluAjo.getSijoitteluajoId(), hakemusOid);
        List<Valintatulos> valintatulokset = dao.loadValintatuloksetForHakemus(hakemusOid);
        List<HakukohdeDTO> hakukohdeDTOs = sijoitteluTulosConverter.convert(hakukohteetJoihinHakemusOsallistuu);
        List<HakijaDTO> hakijat  =   raportointiConverter.convert(hakukohdeDTOs, valintatulokset);
        return filterHakemus(hakijat, hakemusOid);
    }

    /**
     *   Unfortunately this has to be done like this, on the positive side, these results can be cached, only valintatulokset needs
     * to be refreshed, EVER!
     * @param ajo
     * @param hyvaksytyt
     * @param ilmanHyvaksyntaa
     * @param vastaanottaneet
     * @param hakukohdeOid
     * @param count
     * @param index
     * @return
     */
    @Override
    public PaginationObject<HakijaDTO> hakemukset(SijoitteluAjo ajo, Boolean hyvaksytyt, Boolean ilmanHyvaksyntaa, Boolean vastaanottaneet, List<String> hakukohdeOid, Integer count, Integer index) {

        List<Valintatulos> valintatulokset =  dao.loadValintatulokset(ajo.getHakuOid());
        List<Hakukohde> hakukohteet = dao.getHakukohteetForSijoitteluajo(ajo.getSijoitteluajoId());
        List<HakukohdeDTO> hakukohdeDTOs = sijoitteluTulosConverter.convert(hakukohteet);
        List<HakijaDTO> hakijat  =   raportointiConverter.convert(hakukohdeDTOs,valintatulokset);
        Collections.sort(hakijat, new HakijaDTOComparator());

        //hakijat should be cached & set to non-mutable
        //hakijat should be cached & set to non-mutable
        if(hakukohdeOid== null) {
            hakukohdeOid = new ArrayList<String>();
        }

        PaginationObject<HakijaDTO> paginationObject = new PaginationObject<HakijaDTO>();

        List<HakijaDTO> result = new ArrayList<HakijaDTO>();
        for ( HakijaDTO hakija : hakijat) {
            if(filter(hakija, hyvaksytyt,  ilmanHyvaksyntaa,  vastaanottaneet,hakukohdeOid)) {
                result.add(hakija);
            }
        }
        paginationObject.setTotalCount(result.size());
        paginationObject.setResults(applyPagination(result, count, index));
        return paginationObject;
    }


    private boolean filter(HakijaDTO hakija, Boolean hyvaksytyt, Boolean ilmanHyvaksyntaa, Boolean vastaanottaneet, List<String> hakukohdeOid) {
        boolean isPartOfHakukohdeList = false;
        boolean isHyvaksytty = false;
        boolean isVastaanottanut = false;

        for(HakutoiveDTO hakutoiveDTO : hakija.getHakutoiveet()) {
            if(hakukohdeOid.contains(hakutoiveDTO.getHakukohdeOid())) {
                isPartOfHakukohdeList = true;
            }
            for(HakutoiveenValintatapajonoDTO valintatapajono : hakutoiveDTO.getHakutoiveenValintatapajonot()){
                if(valintatapajono.getTila() == HakemuksenTila.HYVAKSYTTY) {
                    isHyvaksytty = true;
                }
                if(valintatapajono.getVastaanottotieto() == ValintatuloksenTila.VASTAANOTTANUT_LASNA || valintatapajono.getVastaanottotieto() ==ValintatuloksenTila.VASTAANOTTANUT_POISSAOLEVA) {
                    isVastaanottanut = true;
                }
            }
        }
        return ((hakukohdeOid == null || hakukohdeOid.size() <=0) || isPartOfHakukohdeList) &&
                (
                        (!Boolean.TRUE.equals(hyvaksytyt) || isHyvaksytty ) &&
                                (!Boolean.TRUE.equals(ilmanHyvaksyntaa) || !isHyvaksytty ) &&
                                (!Boolean.TRUE.equals(vastaanottaneet) || isVastaanottanut )
                );

    }


    private HakijaDTO filterHakemus(List<HakijaDTO> hakijat, String hakemusOid) {
        for(HakijaDTO hakijaDTO : hakijat) {
            if(hakemusOid.equals(hakijaDTO.getHakemusOid())) {
                return hakijaDTO;
            }
        }
        return null;
    }





    private List<HakijaDTO> applyPagination(List<HakijaDTO> result, Integer count, Integer index) {
        if(index != null && count !=null) {
            return result.subList(index, index+count);
        } else if(index != null) {
            return result.subList(index, result.size()-1);
        } else if(count != null) {
            return result.subList(0, count);
        }
        return result;
    }

}
