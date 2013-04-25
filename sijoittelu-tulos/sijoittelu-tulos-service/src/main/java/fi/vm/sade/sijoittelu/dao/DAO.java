package fi.vm.sade.sijoittelu.dao;

import java.util.List;

import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.tulos.service.types.HaeHakukohteetKriteeritTyyppi;
import fi.vm.sade.tulos.service.types.HaeHautKriteeritTyyppi;
import fi.vm.sade.tulos.service.types.HaeSijoitteluajotKriteeritTyyppi;

/**
 * User: tommiha Date: 10/15/12 Time: 2:44 PM
 */
public interface DAO {

    List<Hakukohde> getHakukohdes(long sijoitteluajoId, HaeHakukohteetKriteeritTyyppi haeHakukohteetKriteerit);

    SijoitteluAjo getSijoitteluajo(long sijoitteluajoId);

    List<SijoitteluAjo> getSijoitteluajos(HaeSijoitteluajotKriteeritTyyppi haeSijoitteluajotKriteerit);

    List<Sijoittelu> getHakus(HaeHautKriteeritTyyppi haeHautKriteerit);

}
