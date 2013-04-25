package fi.vm.sade.sijoittelu.dao.exception;

public class MultipleSijoitteluEntitiesFoundException extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public MultipleSijoitteluEntitiesFoundException() {
        super();
    }

    public MultipleSijoitteluEntitiesFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public MultipleSijoitteluEntitiesFoundException(String message) {
        super(message);
    }

    public MultipleSijoitteluEntitiesFoundException(Throwable cause) {
        super(cause);
    }

}
