package fi.vm.sade.sijoittelu.laskenta.service.business;

import javax.ws.rs.WebApplicationException;

public class PriorAcceptanceException extends IllegalVTSRequestException {
    public PriorAcceptanceException(WebApplicationException e) {
        super(e);
    }
}
