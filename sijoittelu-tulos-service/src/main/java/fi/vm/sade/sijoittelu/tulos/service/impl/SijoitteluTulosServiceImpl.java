package fi.vm.sade.sijoittelu.tulos.service.impl;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.sijoittelu.domain.comparator.HakemusComparator;
import fi.vm.sade.sijoittelu.domain.dto.HakemusDTO;
import fi.vm.sade.sijoittelu.domain.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.domain.dto.SijoitteluDTO;
import fi.vm.sade.sijoittelu.domain.dto.SijoitteluajoDTO;
import fi.vm.sade.sijoittelu.tulos.dao.DAO;
import fi.vm.sade.sijoittelu.tulos.service.SijoitteluTulosService;
import fi.vm.sade.sijoittelu.tulos.service.impl.converters.SijoitteluTulosConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kkammone
 * Date: 5.9.2013
 * Time: 14:24
 * To change this template use File | Settings | File Templates.
 */
@Service
public class SijoitteluTulosServiceImpl implements SijoitteluTulosService{

    @Autowired
    private DAO dao;

    @Autowired
    private SijoitteluTulosConverter sijoitteluTulosConverter;

    @Override
    public List<HakemusDTO> haeHakukohteetJoihinHakemusOsallistuu(String sijoitteluajoId, String hakemusOid) {
        List<Hakukohde> b = dao.haeHakukohteetJoihinHakemusOsallistuu(sijoitteluajoId, hakemusOid);
        List<HakemusDTO> a = sijoitteluTulosConverter.extractHakemukset(b, hakemusOid);
        return a;
    }

    @Override
    public HakukohdeDTO getHakukohdeBySijoitteluajo(String sijoitteluajoId, String hakukohdeOid) {
        Hakukohde a = dao.getHakukohdeBySijoitteluajo(sijoitteluajoId, hakukohdeOid);
        sortHakemukset(a);
        HakukohdeDTO b =  sijoitteluTulosConverter.convert(a);
        return b;
    }

    @Override
    public SijoitteluajoDTO getSijoitteluajo(String sijoitteluajoId) {
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
        sortHakemukset(a);
        HakukohdeDTO b =  sijoitteluTulosConverter.convert(a);
        return b;
    }

    @Override
    public List<HakemusDTO> haeLatestHakukohteetJoihinHakemusOsallistuu(String hakuOid, String hakemusOid) {
        List<Hakukohde> b = dao.haeLatestHakukohteetJoihinHakemusOsallistuu(hakuOid, hakemusOid);
        List<HakemusDTO> a = sijoitteluTulosConverter.extractHakemukset(b, hakemusOid);
        return a;
    }

    private void sortHakemukset(Hakukohde hakukohde) {
        HakemusComparator c = new HakemusComparator();
        for(Valintatapajono v : hakukohde.getValintatapajonot()) {
            Collections.sort(v.getHakemukset(), c);
        }
    }
}
