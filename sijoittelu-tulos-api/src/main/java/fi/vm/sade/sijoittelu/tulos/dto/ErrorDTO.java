package fi.vm.sade.sijoittelu.tulos.dto;


/**
 * Created by jukais on 6.2.2014.
 */
public class ErrorDTO {

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
