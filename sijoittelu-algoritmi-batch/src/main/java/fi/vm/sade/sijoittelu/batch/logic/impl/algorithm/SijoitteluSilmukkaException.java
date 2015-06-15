package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

public class SijoitteluSilmukkaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SijoitteluSilmukkaException() {
        super();
    }

    public SijoitteluSilmukkaException(String message, Throwable cause) {
        super(message, cause);
    }

    public SijoitteluSilmukkaException(String message) {
        super(message);
    }

    public SijoitteluSilmukkaException(Throwable cause) {
        super(cause);
    }

}