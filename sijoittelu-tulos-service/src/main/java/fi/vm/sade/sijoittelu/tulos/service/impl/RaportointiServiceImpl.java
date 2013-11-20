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
import fi.vm.sade.sijoittelu.tulos.service.RaportointiService;
import fi.vm.sade.sijoittelu.tulos.service.impl.converters.RaportointiConverter;
import fi.vm.sade.sijoittelu.tulos.service.impl.converters.SijoitteluTulosConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
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
    public List<HakijaDTO> hyvaksytyt(SijoitteluAjo sijoitteluAjoId) {
        return filterHyvakstyt(getHakijat(sijoitteluAjoId));
    }

    @Override
    public Collection<HakijaDTO> hyvaksytyt(SijoitteluAjo sijoitteluAjoId, String hakukohdeOid) {
        return filterHyvakstyt(getHakijat(sijoitteluAjoId, hakukohdeOid));
    }

    @Override
    public List<HakijaDTO> ilmanhyvaksyntaa(SijoitteluAjo sijoitteluAjoId) {
        return filterIlmanhyvaksyntaa(getHakijat(sijoitteluAjoId));
    }

    @Override
    public List<HakijaDTO> ilmanhyvaksyntaa(SijoitteluAjo sijoitteluAjoId, String hakukohdeOid) {
        return filterIlmanhyvaksyntaa(getHakijat(sijoitteluAjoId, hakukohdeOid));
    }

    @Override
    public Collection<HakijaDTO> vastaanottaneet(SijoitteluAjo sijoitteluAjoId) {
        return filterVastanottaneet(getHakijat(sijoitteluAjoId));
    }

    @Override
    public Collection<HakijaDTO> vastaanottaneet(SijoitteluAjo sijoitteluAjoId, String hakukohdeOid) {
        return filterVastanottaneet(getHakijat(sijoitteluAjoId,hakukohdeOid));
    }

    @Override
    public List<HakijaDTO> hakemukset(SijoitteluAjo sijoitteluAjoId) {
        return getHakijat(sijoitteluAjoId);
    }

    @Override
    public HakijaDTO hakemus(SijoitteluAjo sijoitteluAjo, String hakemusOid) {
        List<Hakukohde> hakukohteetJoihinHakemusOsallistuu =    dao.haeHakukohteetJoihinHakemusOsallistuu(sijoitteluAjo.getSijoitteluajoId(), hakemusOid);
        List<Valintatulos> valintatulokset = dao.loadValintatuloksetForHakemus(hakemusOid);
        List<HakukohdeDTO> hakukohdeDTOs = sijoitteluTulosConverter.convert(hakukohteetJoihinHakemusOsallistuu);
        List<HakijaDTO> hakijat  =   raportointiConverter.convert(hakukohdeDTOs, valintatulokset);
        return filterHakemus(hakijat, hakemusOid);

    }



    private List<HakijaDTO> getHakijat(SijoitteluAjo sijoitteluAjo){
        List<Hakukohde> hakukohteet=  dao.getHakukohteetForSijoitteluajo(sijoitteluAjo.getSijoitteluajoId());
        List<Valintatulos> valintatulokset = dao.loadValintatulokset(sijoitteluAjo.getHakuOid());
        List<HakukohdeDTO> hakukohdeDTOs = sijoitteluTulosConverter.convert(hakukohteet);
        List<HakijaDTO> hakijat  =   raportointiConverter.convert(hakukohdeDTOs,valintatulokset);
        return  hakijat;
    }
    private List<HakijaDTO> getHakijat(SijoitteluAjo sijoitteluAjo, String hakukohdeOid){
        List<Hakukohde> hakukohteet=  dao.getHakukohteetForSijoitteluajo(sijoitteluAjo.getSijoitteluajoId(), hakukohdeOid);
        List<Valintatulos> valintatulokset = dao.loadValintatulokset(sijoitteluAjo.getHakuOid(), hakukohdeOid);
        List<HakukohdeDTO> hakukohdeDTOs = sijoitteluTulosConverter.convert(hakukohteet);
        List<HakijaDTO> hakijat  =   raportointiConverter.convert(hakukohdeDTOs,valintatulokset);
        return  hakijat;
    }

    private HakijaDTO filterHakemus(List<HakijaDTO> hakijat, String hakemusOid) {
        for(HakijaDTO hakijaDTO : hakijat) {
            if(hakemusOid.equals(hakijaDTO.getHakemusOid())) {
                return hakijaDTO;
            }
        }
        return null;
    }

    private List<HakijaDTO> filterIlmanhyvaksyntaa(List<HakijaDTO>  kaikkiHakijat){
        List<HakijaDTO> ilmanKoulutuspaikkaa = new ArrayList<HakijaDTO>();
        for(HakijaDTO hakijaDTO : kaikkiHakijat) {
            boolean lisataan = true;
            for(HakutoiveDTO hakutoiveDTO : hakijaDTO.getHakutoiveet())  {
                for(HakutoiveenValintatapajonoDTO vt: hakutoiveDTO.getHakutoiveenValintatapajonot()) {
                    if(vt.getTila() == HakemuksenTila.HYVAKSYTTY) {
                        lisataan = false;
                    }
                }
            }
            if(lisataan) {
                ilmanKoulutuspaikkaa.add(hakijaDTO);
            }
        }
        return ilmanKoulutuspaikkaa;
    }
    private List<HakijaDTO> filterHyvakstyt(List<HakijaDTO>  kaikkiHakijat){
        List<HakijaDTO> ilmanKoulutuspaikkaa = new ArrayList<HakijaDTO>();
        for(HakijaDTO hakijaDTO : kaikkiHakijat) {
            boolean lisataan = false;
            for(HakutoiveDTO hakutoiveDTO : hakijaDTO.getHakutoiveet())  {
                for(HakutoiveenValintatapajonoDTO vt: hakutoiveDTO.getHakutoiveenValintatapajonot()) {
                    if(vt.getTila() == HakemuksenTila.HYVAKSYTTY) {
                        lisataan = true;
                    }
                }
            }
            if(lisataan) {
                ilmanKoulutuspaikkaa.add(hakijaDTO);
            }
        }
        return ilmanKoulutuspaikkaa;
    }
    private List<HakijaDTO> filterVastanottaneet(List<HakijaDTO>  kaikkiHakijat){
        List<HakijaDTO> ilmanKoulutuspaikkaa = new ArrayList<HakijaDTO>();
        for(HakijaDTO hakijaDTO : kaikkiHakijat) {
            boolean lisataan = false;
            for(HakutoiveDTO hakutoiveDTO : hakijaDTO.getHakutoiveet())  {
                for(HakutoiveenValintatapajonoDTO vt: hakutoiveDTO.getHakutoiveenValintatapajonot()) {
                    if(vt.getVastaanottotieto() == ValintatuloksenTila.VASTAANOTTANUT_LASNA || vt.getVastaanottotieto() == ValintatuloksenTila.VASTAANOTTANUT_POISSAOLEVA) {
                        lisataan = true;
                    }
                }
            }
            if(lisataan) {
                ilmanKoulutuspaikkaa.add(hakijaDTO);
            }
        }
        return ilmanKoulutuspaikkaa;
    }

}
