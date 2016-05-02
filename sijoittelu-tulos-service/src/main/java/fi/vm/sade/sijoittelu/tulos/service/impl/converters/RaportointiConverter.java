package fi.vm.sade.sijoittelu.tulos.service.impl.converters;

import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.KevytHakijaDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.RaportointiValintatulos;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public interface RaportointiConverter {
    List<HakijaDTO> convert(List<HakukohdeDTO> hakukohteet);

    List<HakijaDTO> convert(List<HakukohdeDTO> hakukohteet, List<Valintatulos> valintatulokset);

    List<KevytHakijaDTO> convertHakukohde(HakukohdeDTO hakukohde, Iterator<HakukohdeDTO> hakukohteet, Map<String, List<RaportointiValintatulos>> valintatulokset);
}
