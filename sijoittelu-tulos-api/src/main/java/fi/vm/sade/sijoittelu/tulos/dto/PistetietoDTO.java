package fi.vm.sade.sijoittelu.tulos.dto;

import java.io.Serializable;

public class PistetietoDTO implements Serializable {
    private String tunniste;

    private String arvo;

    private String laskennallinenArvo;

    private String osallistuminen;

    private String tyypinKoodiUri;

    private Boolean tilastoidaan;

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

    public String getTyypinKoodiUri() {
        return tyypinKoodiUri;
    }

    public void setTyypinKoodiUri(String tyypinKoodiUri) {
        this.tyypinKoodiUri = tyypinKoodiUri;
    }

    public Boolean isTilastoidaan() {
        return tilastoidaan;
    }

    public void setTilastoidaan(Boolean tilastoidaan) {
        this.tilastoidaan = tilastoidaan;
    }
}
