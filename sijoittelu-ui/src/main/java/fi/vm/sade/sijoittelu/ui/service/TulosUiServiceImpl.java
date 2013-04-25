package fi.vm.sade.sijoittelu.ui.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fi.vm.sade.tulos.service.TulosService;
import fi.vm.sade.tulos.service.types.HaeHakukohteetKriteeritTyyppi;
import fi.vm.sade.tulos.service.types.HaeHautKriteeritTyyppi;
import fi.vm.sade.tulos.service.types.HaeSijoitteluajotKriteeritTyyppi;
import fi.vm.sade.tulos.service.types.tulos.HakuTyyppi;
import fi.vm.sade.tulos.service.types.tulos.HakukohdeTyyppi;
import fi.vm.sade.tulos.service.types.tulos.SijoitteluajoTyyppi;

@Service
public class TulosUiServiceImpl implements TulosUiService {

    @Autowired
    private TulosService tulosService;

    @Override
    public List<HakuTyyppi> listHakus() {
        return tulosService.haeHaut(null);
    }

    @Override
    public List<SijoitteluajoTyyppi> getSijoitteluajos(List<Long> sijoitteluajoIds) {
        HaeSijoitteluajotKriteeritTyyppi kriteerit = new HaeSijoitteluajotKriteeritTyyppi();
        kriteerit.getSijoitteluIdLista().addAll(sijoitteluajoIds);

        return tulosService.haeSijoitteluajot(kriteerit);
    }

    @Override
    public HakuTyyppi getHaku(String hakuOid) {
        HaeHautKriteeritTyyppi kriteerit = new HaeHautKriteeritTyyppi();
        kriteerit.getHakuOidLista().add(hakuOid);

        List<HakuTyyppi> hakus = tulosService.haeHaut(kriteerit);
        if (hakus == null || hakus.isEmpty()) {
            throw new RuntimeException("Could not find haku for oid " + hakuOid);
        }

        return hakus.get(0);
    }

    @Override
    public SijoitteluajoTyyppi getSijoitteluajo(Long sijoitteluajoId) {
        HaeSijoitteluajotKriteeritTyyppi kriteerit = new HaeSijoitteluajotKriteeritTyyppi();
        kriteerit.getSijoitteluIdLista().add(sijoitteluajoId);

        List<SijoitteluajoTyyppi> sijoitteluajos = tulosService.haeSijoitteluajot(kriteerit);
        if (sijoitteluajos == null || sijoitteluajos.isEmpty()) {
            throw new RuntimeException("Could not find sijoitteluajo for id " + sijoitteluajoId);
        }

        return sijoitteluajos.get(0);
    }

    @Override
    public List<HakukohdeTyyppi> getHakukohdes(Long sijoitteluajoId, List<String> hakukohdeOids) {
        HaeHakukohteetKriteeritTyyppi kriteerit = new HaeHakukohteetKriteeritTyyppi();
        kriteerit.getHakukohdeOidLista().addAll(hakukohdeOids);

        return tulosService.haeHakukohteet(sijoitteluajoId, kriteerit);
    }

    @Override
    public HakukohdeTyyppi getHakukohde(Long sijoitteluajoId, String hakukohdeOid) {
        HaeHakukohteetKriteeritTyyppi kriteerit = new HaeHakukohteetKriteeritTyyppi();
        kriteerit.getHakukohdeOidLista().add(hakukohdeOid);

        List<HakukohdeTyyppi> hakukohdes = tulosService.haeHakukohteet(sijoitteluajoId, kriteerit);
        if (hakukohdes == null || hakukohdes.isEmpty()) {
            throw new RuntimeException("Could not find hakukohde for oid " + hakukohdeOid);
        }

        return hakukohdes.get(0);
    }
}
