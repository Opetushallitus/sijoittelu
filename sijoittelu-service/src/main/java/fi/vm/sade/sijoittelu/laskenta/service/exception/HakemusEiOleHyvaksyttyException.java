package fi.vm.sade.sijoittelu.laskenta.service.exception;

public class HakemusEiOleHyvaksyttyException extends RuntimeException {

    private static final long serialVersionUID = -8486542263016135600L;

    public HakemusEiOleHyvaksyttyException(String s) {
        super(s);
    }
}
