package fi.vm.sade.sijoittelu.tulos.dao.exception;

public class SijoitteluEntityNotFoundException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public SijoitteluEntityNotFoundException() {
        super();
    }

    public SijoitteluEntityNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SijoitteluEntityNotFoundException(String message) {
        super(message);
    }

    public SijoitteluEntityNotFoundException(Throwable cause) {
        super(cause);
    }

}
