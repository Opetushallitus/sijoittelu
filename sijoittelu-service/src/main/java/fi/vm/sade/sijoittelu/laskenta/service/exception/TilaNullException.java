package fi.vm.sade.sijoittelu.laskenta.service.exception;

public class TilaNullException extends RuntimeException {

    private static final long serialVersionUID = 767882937355707100L;

    public TilaNullException(String s) {
        super(s);
    }
}
