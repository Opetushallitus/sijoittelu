package fi.vm.sade.sijoittelu.laskenta.service.business;

import javax.ws.rs.WebApplicationException;
import java.util.Map;

public class IllegalVTSRequestException extends RuntimeException {
    public IllegalVTSRequestException(WebApplicationException e) {
        this(e.getResponse().readEntity(Map.class));
    }

    private IllegalVTSRequestException(Map<?, ?> response) {
        super(response.get("error") != null ? response.get("error").toString() : "");
    }
}
