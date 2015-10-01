package fi.vm.sade.sijoittelu.laskenta.service.business;

public class StaleReadException extends RuntimeException {
    public StaleReadException(String hakuOid, String hakukohdeOid, String valintatapajonoOid, String hakemusOid) {
        super(String.format("Yritettiin muokata muuttunutta valintatulosta. haku: %s, hakukohde: %s, valintatapajono: %s, hakemus: %s",
                hakuOid, hakukohdeOid, valintatapajonoOid, hakemusOid));
    }
}
