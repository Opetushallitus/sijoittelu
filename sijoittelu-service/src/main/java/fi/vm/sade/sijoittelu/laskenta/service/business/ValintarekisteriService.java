package fi.vm.sade.sijoittelu.laskenta.service.business;

import fi.vm.sade.valintatulosservice.valintarekisteri.sijoittelu.ValintarekisteriForSijoittelu;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ValintarekisteriService extends ValintarekisteriForSijoittelu  {
    public ValintarekisteriService(@Autowired ValintarekisteriServiceConfiguration configuration) {
        super(configuration.getProperties());
    }
}
