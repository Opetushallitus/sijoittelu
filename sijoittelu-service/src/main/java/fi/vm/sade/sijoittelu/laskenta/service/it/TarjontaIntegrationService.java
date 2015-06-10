package fi.vm.sade.sijoittelu.laskenta.service.it;

import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriDTO;

import java.util.Optional;

public interface TarjontaIntegrationService {
    Optional<String> getTarjoajaOid(String hakukohdeOid);

    Optional<String> getHaunKohdejoukko(String hakuOid);

    ParametriDTO getHaunParametrit(String hakuOid);
}
