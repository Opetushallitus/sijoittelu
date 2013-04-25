package fi.vm.sade.sijoittelu.esb.bean;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Eetu Blomqvist
 */
@XmlRootElement
public class CallbackData {

    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
