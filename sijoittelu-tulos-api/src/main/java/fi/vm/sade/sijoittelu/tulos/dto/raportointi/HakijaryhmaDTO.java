package fi.vm.sade.sijoittelu.tulos.dto.raportointi;

import java.io.Serializable;

public class HakijaryhmaDTO implements Serializable {

    private String          oid;
    private String          nimi;
    private int             kiintio;
    private String          hakijaryhmatyyppikoodiUri;
    private String          valintatapajonoOid;

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getNimi() {
        return nimi;
    }

    public void setNimi(String nimi) {
        this.nimi = nimi;
    }

    public int getKiintio() {
        return kiintio;
    }

    public void setKiintio(int kiintio) {
        this.kiintio = kiintio;
    }

    public String getValintatapajonoOid() {
        return valintatapajonoOid;
    }

    public void setValintatapajonoOid(String valintatapajonoOid) {
        this.valintatapajonoOid = valintatapajonoOid;
    }

    public String getHakijaryhmatyyppikoodiUri() {
        return hakijaryhmatyyppikoodiUri;
    }

    public void setHakijaryhmatyyppikoodiUri(String hakijaryhmatyyppikoodiUri) {
        this.hakijaryhmatyyppikoodiUri = hakijaryhmatyyppikoodiUri;
    }
}
