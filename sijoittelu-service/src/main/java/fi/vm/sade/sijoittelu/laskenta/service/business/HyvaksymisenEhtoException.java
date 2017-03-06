package fi.vm.sade.sijoittelu.laskenta.service.business;

public class HyvaksymisenEhtoException extends RuntimeException {

    public HyvaksymisenEhtoException(String hakuOid, String hakukohdeOid, String valintatapajonoOid, String hakemusOid) {
        super(String.format("Puuttuvia kenttiä. haku: %s, hakukohde: %s, valintatapajono: %s, hakemus: %s ",
                hakuOid, hakukohdeOid, valintatapajonoOid, hakemusOid));
    }
}
