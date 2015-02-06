package fi.vm.sade.sijoittelu.tulos.dto;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: kkammone
 * Date: 27.9.2013
 * Time: 10:29
 * To change this template use File | Settings | File Templates.
 */
public class PistetietoDTO implements Serializable {

    private String tunniste;

    private String arvo;

    private String laskennallinenArvo;

    private String osallistuminen;

    public String getOsallistuminen() {
        return osallistuminen;
    }

    public void setOsallistuminen(String osallistuminen) {
        this.osallistuminen = osallistuminen;
    }

    public String getLaskennallinenArvo() {
        return laskennallinenArvo;
    }

    public void setLaskennallinenArvo(String laskennallinenArvo) {
        this.laskennallinenArvo = laskennallinenArvo;
    }

    public String getTunniste() {
        return tunniste;
    }

    public void setTunniste(String tunniste) {
        this.tunniste = tunniste;
    }

    public String getArvo() {
        return arvo;
    }

    public void setArvo(String arvo) {
        this.arvo = arvo;
    }
}
