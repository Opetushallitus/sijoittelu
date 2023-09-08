package fi.vm.sade.sijoittelu.laskenta.external.resource;

import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.domain.VastaanotettavuusDTO;
import fi.vm.sade.sijoittelu.domain.dto.VastaanottoDTO;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.VastaanottoEventDto;

import java.util.List;

public interface VirkailijaValintaTulosServiceResource {

    VastaanotettavuusDTO vastaanotettavuus(String hakijaOid, String hakemusOid, String hakukohdeOid);

    List<Valintatulos> valintatuloksetValinnantilalla(String hakuOid);

    List<VastaanottoDTO> haunKoulutuksenAlkamiskaudenVastaanototYhdenPaikanSaadoksenPiirissa(String hakuOid);

    void valintatuloksetValinnantilalla(List<VastaanottoEventDto> valintatuloses);
}
