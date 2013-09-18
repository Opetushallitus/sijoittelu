package fi.vm.sade.sijoittelu.tulos.service.impl;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.tulos.dao.DAO;
import fi.vm.sade.sijoittelu.tulos.dto.*;
import fi.vm.sade.sijoittelu.tulos.service.SijoitteluTulosService;
import fi.vm.sade.sijoittelu.tulos.service.impl.converters.SijoitteluTulosConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        if (a == null) {
            return null;
        }
        HakukohdeDTO b = sijoitteluTulosConverter.convert(a);
        return b;
    }

    @Override
    public SijoitteluajoDTO getSijoitteluajo(Long sijoitteluajoId) {
        SijoitteluAjo a = dao.getSijoitteluajo(sijoitteluajoId);
        if (a == null) {
            return null;
        }
        SijoitteluajoDTO b = sijoitteluTulosConverter.convert(a);
        return b;
    }

    @Override
    public SijoitteluDTO getSijoitteluByHakuOid(String hakuOid) {
        Sijoittelu s = dao.getSijoitteluByHakuOid(hakuOid);
        if (s == null) {
            return null;
        }
        return sijoitteluTulosConverter.convert(s);
    }

    @Override
    public SijoitteluajoDTO getLatestSijoitteluajo(String hakuOid) {
        SijoitteluAjo s = dao.getLatestSijoitteluajo(hakuOid);
        if (s == null) {
            return null;
        }
        return sijoitteluTulosConverter.convert(s);
    }

    @Override
    public HakukohdeDTO getLatestHakukohdeBySijoitteluajo(String hakuOid, String hakukohdeOid) {
        Hakukohde a = dao.getLatestHakukohdeBySijoitteluajo(hakuOid, hakukohdeOid);
        if (a == null) {
            return null;
        }
        HakukohdeDTO b = sijoitteluTulosConverter.convert(a);
        return b;
    }

    @Override
    public List<HakemusDTO> haeLatestHakukohteetJoihinHakemusOsallistuu(String hakuOid, String hakemusOid) {
        List<Hakukohde> b = dao.haeLatestHakukohteetJoihinHakemusOsallistuu(hakuOid, hakemusOid);
        if (b == null) {
            return null;
        }
        List<HakukohdeDTO> a = sijoitteluTulosConverter.convert(b);
        return getDtos(a, hakemusOid);
    }

    private List<HakemusDTO> getDtos(List<HakukohdeDTO> b, String hakemusOid) {
        List<HakemusDTO> hakemukset = new ArrayList<HakemusDTO>();
        for (HakukohdeDTO hakukohdeDTO : b) {
            for (ValintatapajonoDTO vtdto : hakukohdeDTO.getValintatapajonot()) {
                for (HakemusDTO hkdto : vtdto.getHakemukset()) {
                    if (hakemusOid.equals(hkdto.getHakemusOid())) {
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
        if (b == null) {
            return null;
        }
        List<HakukohdeDTO> a = sijoitteluTulosConverter.convert(b);
        return getDtos(a, hakemusOid);
    }


}
