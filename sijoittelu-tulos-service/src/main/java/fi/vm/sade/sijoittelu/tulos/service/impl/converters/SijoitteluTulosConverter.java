package fi.vm.sade.sijoittelu.tulos.service.impl.converters;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.dto.SijoitteluDTO;
import fi.vm.sade.sijoittelu.tulos.dto.SijoitteluajoDTO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kkammone
 * Date: 5.9.2013
 * Time: 14:27
 * To change this template use File | Settings | File Templates.
 */
public interface SijoitteluTulosConverter {

    HakukohdeDTO convert(Hakukohde a);

    SijoitteluajoDTO convert(SijoitteluAjo a);

    SijoitteluDTO convert(Sijoittelu s);

    List<HakukohdeDTO> convert(List<Hakukohde> hakukohdeList);
}
