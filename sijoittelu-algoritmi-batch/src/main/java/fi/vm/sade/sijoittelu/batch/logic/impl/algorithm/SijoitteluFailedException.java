package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

public class SijoitteluFailedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SijoitteluFailedException() {
        super();
    }

    public SijoitteluFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public SijoitteluFailedException(String message) {
        super(message);
    }

    public SijoitteluFailedException(Throwable cause) {
        super(cause);
    }

}
