package fi.vm.sade.sijoittelu.jatkuva.dto;

import java.util.Date;

public class JatkuvaSijoittelu {

    private final String hakuOid;
    private final boolean ajossa;
    private final Date viimeksiAjettu;
    private final String virhe;
    private Date aloitusajankohta;
    private Integer ajotiheys;

    public JatkuvaSijoittelu() {
        this.hakuOid = null;
        this.ajossa = false;
        this.viimeksiAjettu = null;
        this.virhe = null;
        this.aloitusajankohta = null;
        this.ajotiheys = null;
    }

    public JatkuvaSijoittelu(String hakuOid, boolean ajossa, Date viimeksiAjettu, String virhe, Date aloitusajankohta, Integer ajotiheys) {
        this.hakuOid = hakuOid;
        this.ajossa = ajossa;
        this.viimeksiAjettu = viimeksiAjettu;
        this.virhe = virhe;
        this.aloitusajankohta = aloitusajankohta;
        this.ajotiheys = ajotiheys;
    }

    public String getVirhe() { return virhe; }

    public String getHakuOid() {
        return hakuOid;
    }

    public Date getViimeksiAjettu() {
        return viimeksiAjettu;
    }

    public boolean isAjossa() {
        return ajossa;
    }

    public Date getAloitusajankohta() { return aloitusajankohta; }

    public Integer getAjotiheys() {
        return ajotiheys;
    }
}
