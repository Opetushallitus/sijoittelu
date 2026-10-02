package fi.vm.sade.sijoittelu.laskenta.external.resource;

import fi.vm.sade.sijoittelu.domain.dto.VastaanottoDTO;

import java.util.List;

public interface VirkailijaValintaTulosServiceResource {
    List<VastaanottoDTO> haunKoulutuksenAlkamiskaudenVastaanototYhdenPaikanSaadoksenPiirissa(String hakuOid);
}
