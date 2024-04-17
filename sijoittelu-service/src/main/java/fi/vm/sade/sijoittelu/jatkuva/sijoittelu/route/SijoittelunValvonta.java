package fi.vm.sade.sijoittelu.jatkuva.sijoittelu.route;

import fi.vm.sade.sijoittelu.jatkuva.sijoittelu.dto.Sijoittelu;

public interface SijoittelunValvonta {
  Sijoittelu haeAktiivinenSijoitteluHaulle(String hakuOid);
}
