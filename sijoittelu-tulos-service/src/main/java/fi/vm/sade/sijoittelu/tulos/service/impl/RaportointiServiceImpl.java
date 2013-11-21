package fi.vm.sade.sijoittelu.tulos.service.impl;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.tulos.dao.DAO;
import fi.vm.sade.sijoittelu.tulos.dto.HakemuksenTila;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakutoiveDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakutoiveenValintatapajonoDTO;
import fi.vm.sade.sijoittelu.tulos.service.RaportointiService;
import fi.vm.sade.sijoittelu.tulos.service.impl.comparators.HakijaDTOComparator;
import fi.vm.sade.sijoittelu.tulos.service.impl.converters.EnumConverter;
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

    private HakijaDTO filterHakemus(List<HakijaDTO> hakijat, String hakemusOid) {
        for(HakijaDTO hakijaDTO : hakijat) {
            if(hakemusOid.equals(hakijaDTO.getHakemusOid())) {
                return hakijaDTO;
            }
        }
        return null;
    }

    /**
     *  Unfortunately this has to be done like this, on the positive side, these results can be cached, only valintatulokset needs
     * to be refreshed, EVER!
     * @param ajo
     * @param vastaanottotieto
     * @param tila
     * @param hakukohdeOid
     * @param count
     * @param index
     * @return
     */
    @Override
    public List<HakijaDTO> hakemukset(SijoitteluAjo ajo,
                                      List<String> vastaanottotieto,
                                      List<String> tila,
                                      List<String> hakukohdeOid,
                                      Integer count,
                                      Integer index) {

        List<ValintatuloksenTila> vastaanottotietoEnums = EnumConverter.convertStringListToEnum(ValintatuloksenTila.class, vastaanottotieto);
        List<HakemuksenTila> tilaEnums = EnumConverter.convertStringListToEnum(HakemuksenTila.class, tila);

        List<Valintatulos> valintatulokset =  dao.loadValintatulokset(ajo.getHakuOid());
        List<Hakukohde> hakukohteet = dao.getHakukohteetForSijoitteluajo(ajo.getSijoitteluajoId());
        List<HakukohdeDTO> hakukohdeDTOs = sijoitteluTulosConverter.convert(hakukohteet);
        List<HakijaDTO> hakijat  =   raportointiConverter.convert(hakukohdeDTOs,valintatulokset);
        Collections.sort(hakijat, new HakijaDTOComparator());

        //hakijat should be cached & set to non-mutable

        List<HakijaDTO> result = new ArrayList<HakijaDTO>();
        for ( HakijaDTO hakija : hakijat) {
            if(filter(hakija, vastaanottotietoEnums,tilaEnums,hakukohdeOid)) {
                result.add(hakija);
            }
        }
        return applyPagination(result, count, index);
    }

    private boolean filter(HakijaDTO hakija, List<ValintatuloksenTila> vastaanottotieto, List<HakemuksenTila> tila, List<String> hakukohdeOid) {
        boolean vastaanottotietoOK = false;
        boolean tilaOk =false;
        boolean hakukohdeOidOk=false;
        if(vastaanottotieto == null || vastaanottotieto.size() > 0) {
            vastaanottotietoOK = true;
        }
        if(tila == null || tila.size() > 0) {
            tilaOk = true;
        }
        if(hakukohdeOid == null || hakukohdeOid.size() > 0) {
            hakukohdeOidOk = true;
        }
        for(HakutoiveDTO hakutoiveDTO : hakija.getHakutoiveet()) {
            if(hakukohdeOid.contains(hakutoiveDTO.getHakukohdeOid())) {
                hakukohdeOidOk = true;
            }
            for(HakutoiveenValintatapajonoDTO valintatapajono : hakutoiveDTO.getHakutoiveenValintatapajonot()){
                if(tila.contains(valintatapajono.getTila())) {
                    tilaOk = true;
                }
                if(vastaanottotieto.contains(valintatapajono.getVastaanottotieto())) {
                    vastaanottotietoOK = true;
                }
            }
        }
        return vastaanottotietoOK && tilaOk && hakukohdeOidOk;
    }


    private List<HakijaDTO> applyPagination(List<HakijaDTO> result, Integer count, Integer index) {
        return result.subList(index, index+count);
    }

}
