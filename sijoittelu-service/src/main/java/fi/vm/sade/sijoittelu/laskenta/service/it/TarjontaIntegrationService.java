package fi.vm.sade.sijoittelu.laskenta.service.it;

public interface TarjontaIntegrationService {
    Haku getHaku(String hakuOid);
    Haku getHaku(String hakuOid, boolean validate);
}
