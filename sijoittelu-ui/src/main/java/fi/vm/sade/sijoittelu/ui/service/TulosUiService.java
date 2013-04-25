package fi.vm.sade.sijoittelu.ui.service;

import java.util.List;

import fi.vm.sade.tulos.service.types.tulos.HakuTyyppi;
import fi.vm.sade.tulos.service.types.tulos.HakukohdeTyyppi;
import fi.vm.sade.tulos.service.types.tulos.SijoitteluajoTyyppi;

public interface TulosUiService {
    public List<HakuTyyppi> listHakus();

    public List<SijoitteluajoTyyppi> getSijoitteluajos(List<Long> sijoitteluajoIds);

    public HakuTyyppi getHaku(String hakuOid);

    public SijoitteluajoTyyppi getSijoitteluajo(Long sijoitteluajoId);

    public List<HakukohdeTyyppi> getHakukohdes(Long sijoitteluajoId, List<String> hakukohdeOids);

    public HakukohdeTyyppi getHakukohde(Long sijoitteluajoId, String hakukohdeOid);

}
