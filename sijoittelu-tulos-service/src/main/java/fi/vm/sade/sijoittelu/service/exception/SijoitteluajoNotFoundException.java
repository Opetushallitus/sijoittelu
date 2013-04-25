package fi.vm.sade.sijoittelu.service.exception;

import fi.vm.sade.generic.service.exception.SadeBusinessException;

public class SijoitteluajoNotFoundException extends SadeBusinessException {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final String ERROR_KEY = SijoitteluajoNotFoundException.class.getCanonicalName();

    public SijoitteluajoNotFoundException() {
        super();
    }

    public SijoitteluajoNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SijoitteluajoNotFoundException(String message) {
        super(message);
    }

    public SijoitteluajoNotFoundException(Throwable cause) {
        super(cause);
    }

    @Override
    public String getErrorKey() {
        return ERROR_KEY;
    }
}
