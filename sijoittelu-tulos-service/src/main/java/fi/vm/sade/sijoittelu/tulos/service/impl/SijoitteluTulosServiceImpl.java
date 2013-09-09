package fi.vm.sade.sijoittelu.tulos.service.impl;

import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.comparator.HakemusDTOComparator;
import fi.vm.sade.sijoittelu.domain.dto.*;
import fi.vm.sade.sijoittelu.tulos.dao.DAO;
import fi.vm.sade.sijoittelu.tulos.service.SijoitteluTulosService;
import fi.vm.sade.sijoittelu.tulos.service.impl.converters.SijoitteluTulosConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA. User: kkammone Date: 5.9.2013 Time: 14:24 To
 * change this template use File | Settings | File Templates.
 */
@Service
public class SijoitteluTulosServiceImpl implements SijoitteluTulosService {

    @Autowired
    private DAO dao;

    @Autowired
    private SijoitteluTulosConverter sijoitteluTulosConverter;



    @Override
    public HakukohdeDTO getHakukohdeBySijoitteluajo(Long sijoitteluajoId, String hakukohdeOid) {
        Hakukohde a = dao.getHakukohdeBySijoitteluajo(sijoitteluajoId, hakukohdeOid);
        HakukohdeDTO b = sijoitteluTulosConverter.convert(a);
        sortHakemukset(b);
        return b;
    }

    @Override
    public SijoitteluajoDTO getSijoitteluajo(Long sijoitteluajoId) {
        SijoitteluAjo a = dao.getSijoitteluajo(sijoitteluajoId);
        SijoitteluajoDTO b = sijoitteluTulosConverter.convert(a);
        return b;
    }

    @Override
    public SijoitteluDTO getSijoitteluByHakuOid(String hakuOid) {
        Sijoittelu s = dao.getSijoitteluByHakuOid(hakuOid);
        return sijoitteluTulosConverter.convert(s);
    }

    @Override
    public SijoitteluajoDTO getLatestSijoitteluajo(String hakuOid) {
        SijoitteluAjo s = dao.getLatestSijoitteluajo(hakuOid);
        return sijoitteluTulosConverter.convert(s);
    }

    @Override
    public HakukohdeDTO getLatestHakukohdeBySijoitteluajo(String hakuOid, String hakukohdeOid) {
        Hakukohde a = dao.getLatestHakukohdeBySijoitteluajo(hakuOid, hakukohdeOid);
        HakukohdeDTO b = sijoitteluTulosConverter.convert(a);
        sortHakemukset(b);
        return b;
    }

    @Override
    public List<HakemusDTO> haeLatestHakukohteetJoihinHakemusOsallistuu(String hakuOid, String hakemusOid) {
        List<Hakukohde> b = dao.haeLatestHakukohteetJoihinHakemusOsallistuu(hakuOid, hakemusOid);
        return getDtos(b, hakemusOid);
    }

    private List<HakemusDTO> getDtos( List<Hakukohde> b, String hakemusOid) {
        List<HakukohdeDTO> a = sijoitteluTulosConverter.convert(b);
        for(HakukohdeDTO hakukohdeDTO : a) {
            sortHakemukset(hakukohdeDTO);
        }
        List<HakemusDTO> hakemukset =new ArrayList<HakemusDTO>();
        for(HakukohdeDTO hakukohdeDTO : a) {
            for(ValintatapajonoDTO vtdto : hakukohdeDTO.getValintatapajonot()) {
                for(HakemusDTO hkdto : vtdto.getHakemukset()) {
                    if(hakemusOid.equals(hkdto.getHakemusOid())) {
                        hakemukset.add(hkdto);
                    }
                }
            }
        }
        return hakemukset;
    }

    @Override
    public List<HakemusDTO> haeHakukohteetJoihinHakemusOsallistuu(Long sijoitteluajoId, String hakemusOid) {
        List<Hakukohde> b = dao.haeHakukohteetJoihinHakemusOsallistuu(sijoitteluajoId, hakemusOid);
        return getDtos(b,hakemusOid);
    }

    private void sortHakemukset(HakukohdeDTO hakukohde) {
        HakemusDTOComparator c = new HakemusDTOComparator();
        //sort hakemukset
        for (ValintatapajonoDTO v : hakukohde.getValintatapajonot()) {
            Collections.sort(v.getHakemukset(), c);
        }
        //apply varalla jonosija
        for(ValintatapajonoDTO v : hakukohde.getValintatapajonot()) {
            applyVarasijaJonosija(v);
            applyAlinHyvaksyttyPistemaara(v);
        }

    }

    private void applyAlinHyvaksyttyPistemaara(ValintatapajonoDTO v) {
        BigDecimal alinHyvaksyttyPistemaara = null;
        for(HakemusDTO hakemusDTO: v.getHakemukset()) {
            if(hakemusDTO.getTila() == HakemuksenTila.HYVAKSYTTY) {
                if(hakemusDTO.getPisteet() != null && (alinHyvaksyttyPistemaara == null || alinHyvaksyttyPistemaara.compareTo(hakemusDTO.getPisteet()) < 0) ) {
                    alinHyvaksyttyPistemaara = hakemusDTO.getPisteet();
                }
            }
        }
        v.setAlinHyvaksyttyPistemaara(alinHyvaksyttyPistemaara);
    }

    /**
     * kutsu vasta sorttauksen jalkeen valintatapajonolle
     * @param v
     */
    private void applyVarasijaJonosija( ValintatapajonoDTO v ) {
        ArrayList<HakemusDTO> hakemukset = v.getHakemukset();
        int paikka = 0;
        for(HakemusDTO hakemusDTO : hakemukset) {
            if(hakemusDTO.getTila() == HakemuksenTila.VARALLA)  {
                paikka++;
                hakemusDTO.setVarasijanNumero(paikka);
            }
        }
    }
}
