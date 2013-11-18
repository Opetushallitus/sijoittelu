package fi.vm.sade.sijoittelu.tulos.service.impl.converters;

import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaDTO;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kkammone
 * Date: 17.9.2013
 * Time: 14:49
 * To change this template use File | Settings | File Templates.
 */
public interface RaportointiConverter {
    List<HakijaDTO> convert(List<HakukohdeDTO> hakukohteet);

    List<HakijaDTO> convert(List<HakukohdeDTO> hakukohteet, List<Valintatulos> valintatulokset);
}
