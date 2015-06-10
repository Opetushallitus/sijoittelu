package fi.vm.sade.sijoittelu.tulos.service.impl.converters;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.dto.SijoitteluDTO;
import fi.vm.sade.sijoittelu.tulos.dto.SijoitteluajoDTO;
import fi.vm.sade.sijoittelu.tulos.dto.ValintatapajonoDTO;

import java.util.List;

public interface SijoitteluTulosConverter {
    HakukohdeDTO convert(Hakukohde a);

    SijoitteluajoDTO convert(SijoitteluAjo a);

    SijoitteluDTO convert(Sijoittelu s);

    List<HakukohdeDTO> convert(List<Hakukohde> hakukohdeList);

    void sortHakemukset(ValintatapajonoDTO valintatapajonoDTO);
}
