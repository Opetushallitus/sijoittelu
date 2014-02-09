package fi.vm.sade.sijoittelu.tulos.dto;

import org.codehaus.jackson.map.annotate.JsonView;

/**
 * Created by jukais on 6.2.2014.
 */
public class ErrorDTO {
    @JsonView({ JsonViews.Hakemus.class, JsonViews.Hakukohde.class })
    private String message;

    public ErrorDTO(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
