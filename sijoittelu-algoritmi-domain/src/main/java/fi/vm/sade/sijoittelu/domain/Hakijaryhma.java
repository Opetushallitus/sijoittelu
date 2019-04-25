package fi.vm.sade.sijoittelu.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class Hakijaryhma implements Serializable {

    private Integer prioriteetti;

    private int paikat;

    private String oid;

    private String nimi;

    private String hakukohdeOid;

    private int kiintio;

    private boolean kaytaKaikki;

    private boolean tarkkaKiintio;

    private boolean kaytetaanRyhmaanKuuluvia;

    private String hakijaryhmatyyppikoodiUri;

    private String valintatapajonoOid;

    private BigDecimal alinHyvaksyttyPistemaara;

    private List<String> hakemusOid = new ArrayList<String>();

    public BigDecimal getAlinHyvaksyttyPistemaara() {
        return alinHyvaksyttyPistemaara;
    }

    public void setAlinHyvaksyttyPistemaara(BigDecimal alinHyvaksyttyPistemaara) {
        this.alinHyvaksyttyPistemaara = alinHyvaksyttyPistemaara;
    }

    public List<String> getHakemusOid() {
        return hakemusOid;
    }
 
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

    public int getPaikat() {
        return paikat;
    }

    public void setPaikat(int paikat) {
        this.paikat = paikat;
    }

    public Integer getPrioriteetti() {
        return prioriteetti;
    }

    public void setPrioriteetti(Integer prioriteetti) {
        this.prioriteetti = prioriteetti;
    }

    @Override
    public String toString() {
        return "Hakijaryhma{" +
                "prioriteetti=" + prioriteetti +
                ", paikat=" + paikat +
                ", oid='" + oid + '\'' +
                ", nimi='" + nimi + '\'' +
                ", hakukohdeOid='" + hakukohdeOid + '\'' +
                ", kiintio=" + kiintio +
                ", kaytaKaikki=" + kaytaKaikki +
                ", tarkkaKiintio=" + tarkkaKiintio +
                ", kaytetaanRyhmaanKuuluvia=" + kaytetaanRyhmaanKuuluvia +
                ", hakijaryhmatyyppikoodiUri='" + hakijaryhmatyyppikoodiUri + '\'' +
                ", valintatapajonoOid='" + valintatapajonoOid + '\'' +
                ", alinHyvaksyttyPistemaara=" + alinHyvaksyttyPistemaara +
                '}';
    }

    public String getHakukohdeOid() {
        return hakukohdeOid;
    }

    public void setHakukohdeOid(String hakukohdeOid) {
        this.hakukohdeOid = hakukohdeOid;
    }

    public int getKiintio() {
        return kiintio;
    }

    public void setKiintio(int kiintio) {
        this.kiintio = kiintio;
    }

    public boolean isKaytaKaikki() {
        return kaytaKaikki;
    }

    public void setKaytaKaikki(boolean kaytaKaikki) {
        this.kaytaKaikki = kaytaKaikki;
    }

    public boolean isTarkkaKiintio() {
        return tarkkaKiintio;
    }

    public void setTarkkaKiintio(boolean tarkkaKiintio) {
        this.tarkkaKiintio = tarkkaKiintio;
    }

    public boolean isKaytetaanRyhmaanKuuluvia() {
        return kaytetaanRyhmaanKuuluvia;
    }

    public void setKaytetaanRyhmaanKuuluvia(boolean kaytetaanRyhmaanKuuluvia) {
        this.kaytetaanRyhmaanKuuluvia = kaytetaanRyhmaanKuuluvia;
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
