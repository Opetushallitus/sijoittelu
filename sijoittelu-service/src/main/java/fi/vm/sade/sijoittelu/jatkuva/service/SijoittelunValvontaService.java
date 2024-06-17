package fi.vm.sade.sijoittelu.jatkuva.service;

import fi.vm.sade.sijoittelu.jatkuva.dto.Sijoittelu;

public interface SijoittelunValvontaService {
  Sijoittelu haeAktiivinenSijoitteluHaulle(String hakuOid);
}
