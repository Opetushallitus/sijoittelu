package fi.vm.sade.sijoittelu.laskenta.service.exception;

public class HakemustaEiLoytynytException extends RuntimeException {

    private static final long serialVersionUID = -2770390894039759626L;

    public HakemustaEiLoytynytException(String valintatapajonoOid, String hakemusOid) {
        super(String.format("Hakemusta ei löytynyt. valintatapajono: %s, hakemus: %s", valintatapajonoOid, hakemusOid));
    }
}
