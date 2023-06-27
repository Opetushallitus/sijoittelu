package fi.vm.sade.sijoittelu.laskenta.external.resource;

import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ResultHakuDTO;

public interface HakuV1Resource {
    ResultHakuDTO findByOid(String oid);
}
