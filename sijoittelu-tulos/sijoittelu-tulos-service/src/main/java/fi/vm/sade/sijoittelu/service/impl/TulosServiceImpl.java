package fi.vm.sade.sijoittelu.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.vm.sade.generic.service.conversion.SadeConversionService;
import fi.vm.sade.sijoittelu.dao.DAO;
import fi.vm.sade.sijoittelu.dao.exception.SijoitteluEntityNotFoundException;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.service.exception.SijoitteluajoNotFoundException;
import fi.vm.sade.tulos.service.TulosService;
import fi.vm.sade.tulos.service.types.HaeHakukohteetKriteeritTyyppi;
import fi.vm.sade.tulos.service.types.HaeHautKriteeritTyyppi;
import fi.vm.sade.tulos.service.types.HaeSijoitteluajotKriteeritTyyppi;
import fi.vm.sade.tulos.service.types.tulos.HakuTyyppi;
import fi.vm.sade.tulos.service.types.tulos.HakukohdeTyyppi;
import fi.vm.sade.tulos.service.types.tulos.SijoitteluajoTyyppi;

/**
 * User: tommiha Date: 10/15/12 Time: 3:51 PM
 */
@Service("tulosService")
public class TulosServiceImpl implements TulosService {

    @Autowired
    private DAO dao;

    @Autowired
    private SadeConversionService conversionService;

    @Override
    public List<SijoitteluajoTyyppi> haeSijoitteluajot(HaeSijoitteluajotKriteeritTyyppi haeSijoitteluajotKriteerit) {
        List<SijoitteluAjo> sijoitteluajos = dao.getSijoitteluajos(haeSijoitteluajotKriteerit);
        return conversionService.convertAll(sijoitteluajos, SijoitteluajoTyyppi.class);
    }

    @Override
    public List<HakuTyyppi> haeHaut(HaeHautKriteeritTyyppi haeHautKriteerit) {
        List<Sijoittelu> hakus = dao.getHakus(haeHautKriteerit);
        return conversionService.convertAll(hakus, HakuTyyppi.class);
    }

    @Override
    public List<HakukohdeTyyppi> haeHakukohteet(long sijoitteluajoId, HaeHakukohteetKriteeritTyyppi haeHakukohteetKriteerit) {
        try {
            List<Hakukohde> hakukohdes = dao.getHakukohdes(sijoitteluajoId, haeHakukohteetKriteerit);
            return conversionService.convertAll(hakukohdes, HakukohdeTyyppi.class);
        } catch (SijoitteluEntityNotFoundException e) {
            throw new SijoitteluajoNotFoundException("Could not find sijoitteluajo for id " + sijoitteluajoId);
        }
    }
}
