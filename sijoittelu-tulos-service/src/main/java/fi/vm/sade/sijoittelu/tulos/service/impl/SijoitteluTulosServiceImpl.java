package fi.vm.sade.sijoittelu.tulos.service.impl;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.tulos.dao.CachingRaportointiDao;
import fi.vm.sade.sijoittelu.tulos.dao.ValintatulosDao;
import fi.vm.sade.sijoittelu.tulos.dao.HakukohdeDao;
import fi.vm.sade.sijoittelu.tulos.dao.SijoitteluDao;
import fi.vm.sade.sijoittelu.tulos.dto.*;
import fi.vm.sade.sijoittelu.tulos.service.SijoitteluTulosService;
import fi.vm.sade.sijoittelu.tulos.service.impl.converters.SijoitteluTulosConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Service
public class SijoitteluTulosServiceImpl implements SijoitteluTulosService {
    @Autowired
    private HakukohdeDao hakukohdeDao;

    @Autowired
    private CachingRaportointiDao cachingRaportointiDao;

    @Autowired
    private SijoitteluDao sijoitteluDao;

    @Autowired
    private SijoitteluTulosConverter sijoitteluTulosConverter;

    @Override
    public HakukohdeDTO getHakukohdeBySijoitteluajo(SijoitteluAjo sijoitteluAjo, String hakukohdeOid) {
        Hakukohde a = cachingRaportointiDao.getCachedHakukohde(sijoitteluAjo, hakukohdeOid);
        if (a == null) {
            return null;
        }
        return sijoitteluTulosConverter.convert(a);
    }

    @Override
    public SijoitteluajoDTO getSijoitteluajo(SijoitteluAjo sijoitteluAjo) {
        if (sijoitteluAjo == null) {
            return null;
        }
        return sijoitteluTulosConverter.convert(sijoitteluAjo);
    }

    @Override
    public SijoitteluDTO getSijoitteluByHakuOid(String hakuOid) {
        Optional<Sijoittelu> s = sijoitteluDao.getSijoitteluByHakuOid(hakuOid);

        return s.map(sijoitteluTulosConverter::convert).orElse(new SijoitteluDTO());

    }
}
