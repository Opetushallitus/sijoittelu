package fi.vm.sade.sijoittelu.tulos.service.impl;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.sijoittelu.domain.comparator.HakemusComparator;
import fi.vm.sade.sijoittelu.domain.dto.HakemusDTO;
import fi.vm.sade.sijoittelu.domain.dto.HakukohdeDTO;
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
    public List<HakemusDTO> haeHakukohteetJoihinHakemusOsallistuu(Long id, String oid) {
        List<Hakukohde> b = dao.haeHakukohteetJoihinHakemusOsallistuu(id, oid);
        List<HakemusDTO> a = sijoitteluTulosConverter.extractHakemukset(b, oid);
        return a;
    }

    @Override
    public HakukohdeDTO getHakukohdeBySijoitteluajo(Long id, String oid) {
        Hakukohde a = dao.getHakukohdeBySijoitteluajo(id, oid);
        sortHakemukset(a);
        HakukohdeDTO b =  sijoitteluTulosConverter.convert(a);
        return b;
    }

    private void sortHakemukset(Hakukohde hakukohde)    {
        HakemusComparator c = new HakemusComparator();
        for(Valintatapajono v : hakukohde.getValintatapajonot()) {
            Collections.sort(v.getHakemukset(), c);
        }
    }
    @Override
    public SijoitteluajoDTO getSijoitteluajo(Long id) {
        SijoitteluAjo a = dao.getSijoitteluajo(id);
        SijoitteluajoDTO b = sijoitteluTulosConverter.convert(a);
        return b;
    }
}
