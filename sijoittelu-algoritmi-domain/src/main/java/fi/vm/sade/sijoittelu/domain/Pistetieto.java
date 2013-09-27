package fi.vm.sade.sijoittelu.domain;

import com.google.code.morphia.annotations.Converters;
import com.google.code.morphia.annotations.Embedded;
import fi.vm.sade.sijoittelu.domain.converter.BigDecimalConverter;

/**
 * Created with IntelliJ IDEA.
 * User: kkammone
 * Date: 27.9.2013
 * Time: 10:11
 * To change this template use File | Settings | File Templates.
 */
@Embedded
@Converters(BigDecimalConverter.class)
public class Pistetieto {

    private String tunniste;

    private String arvo;

    private String laskennallinenArvo;

    private String osallistuminen;

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

    public String getLaskennallinenArvo() {
        return laskennallinenArvo;
    }

    public void setLaskennallinenArvo(String laskennallinenArvo) {
        this.laskennallinenArvo = laskennallinenArvo;
    }

    public String getOsallistuminen() {
        return osallistuminen;
    }

    public void setOsallistuminen(String osallistuminen) {
        this.osallistuminen = osallistuminen;
    }
}
