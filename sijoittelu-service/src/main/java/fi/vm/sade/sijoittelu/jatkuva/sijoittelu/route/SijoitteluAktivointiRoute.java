package fi.vm.sade.sijoittelu.jatkuva.sijoittelu.route;

import fi.vm.sade.sijoittelu.jatkuva.sijoittelu.dto.Sijoittelu;

public interface SijoitteluAktivointiRoute {

  void aktivoiSijoittelu(Sijoittelu sijoittelu);
}
