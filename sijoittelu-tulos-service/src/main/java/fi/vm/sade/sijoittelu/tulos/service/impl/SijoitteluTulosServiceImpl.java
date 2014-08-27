package fi.vm.sade.sijoittelu.tulos.service.impl;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
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

/**
 * Created with IntelliJ IDEA. User: kkammone Date: 5.9.2013 Time: 14:24 To
 * change this template use File | Settings | File Templates.
 */
@Service
public class SijoitteluTulosServiceImpl implements SijoitteluTulosService {

    @Autowired
    private ValintatulosDao valintatulosDao;

    @Autowired
    private HakukohdeDao hakukohdeDao;

    @Autowired
    private SijoitteluDao sijoitteluDao;

    @Autowired
    private SijoitteluTulosConverter sijoitteluTulosConverter;

    @Override
    public HakukohdeDTO getHakukohdeBySijoitteluajo(SijoitteluAjo sijoitteluAjo, String hakukohdeOid) {
        Hakukohde a = hakukohdeDao.getHakukohdeForSijoitteluajo(sijoitteluAjo.getSijoitteluajoId(), hakukohdeOid);
        if (a == null) {
            return null;
        }
        HakukohdeDTO b = sijoitteluTulosConverter.convert(a);
        return b;
    }

    @Override
    public SijoitteluajoDTO getSijoitteluajo(SijoitteluAjo sijoitteluAjo) {
        if (sijoitteluAjo == null) {
            return null;
        }
        SijoitteluajoDTO b = sijoitteluTulosConverter.convert(sijoitteluAjo);
        return b;
    }

    @Override
    public SijoitteluDTO getSijoitteluByHakuOid(String hakuOid) {
        Sijoittelu s = sijoitteluDao.getSijoitteluByHakuOid(hakuOid);
        if (s == null) {
            return null;
        }
        return sijoitteluTulosConverter.convert(s);
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

}
