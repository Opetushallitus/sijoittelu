package fi.vm.sade.sijoittelu.domain;

import fi.vm.sade.sijoittelu.domain.converter.BigDecimalConverter;
import org.mongodb.morphia.annotations.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Embedded
@Converters(BigDecimalConverter.class)
public class Valintatapajono implements Serializable {

    private Tasasijasaanto tasasijasaanto;

    private ValintatapajonoTila tila;

    @Indexed
    private String oid;

    private String nimi;

    private Integer prioriteetti;

    private Integer aloituspaikat;

    private Integer alkuperaisetAloituspaikat;

    private Boolean eiVarasijatayttoa;

    private Boolean kaikkiEhdonTayttavatHyvaksytaan;

    private Boolean poissaOlevaTaytto;

    private Integer varasijat = 0;

    private Integer varasijaTayttoPaivat = 0;

    private Date varasijojaKaytetaanAlkaen;

    private Date varasijojaTaytetaanAsti;

    private String tayttojono;

    private Integer hyvaksytty;

    private Integer varalla;

    private BigDecimal alinHyvaksyttyPistemaara;

    private Boolean valintaesitysHyvaksytty;

    private Integer hakemustenMaara;

    private boolean sijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa;

    @Embedded
    private List<Hakemus> hakemukset = new ArrayList<Hakemus>();

    @PreLoad
    public void setSivssnovIfMissing() {
        this.setSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa(false);
    }

    public Integer getHakemustenMaara() {
        return hakemustenMaara;
    }

    public void setHakemustenMaara(Integer hakemustenMaara) {
        this.hakemustenMaara = hakemustenMaara;
    }

    public ValintatapajonoTila getTila() {
        return tila;
    }

    public void setTila(ValintatapajonoTila tila) {
        this.tila = tila;
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

    public Integer getPrioriteetti() {
        return prioriteetti;
    }

    public void setPrioriteetti(int prioriteetti) {
        this.prioriteetti = prioriteetti;
    }

    public Integer getAloituspaikat() {
        return aloituspaikat;
    }

    public void setAloituspaikat(int aloituspaikat) {
        this.aloituspaikat = aloituspaikat;
    }

    public List<Hakemus> getHakemukset() {
        return hakemukset;
    }

    public void setHakemukset(List<Hakemus> hakemukset) {
        this.hakemukset = hakemukset;
    }

    public Tasasijasaanto getTasasijasaanto() {
        return tasasijasaanto;
    }

    public void setTasasijasaanto(Tasasijasaanto tasasijasaanto) {
        this.tasasijasaanto = tasasijasaanto;
    }

    @Override
    public String toString() {
        return "Valintatapajono{" +
                "tasasijasaanto=" + tasasijasaanto +
                ", tila=" + tila +
                ", nimi=" + nimi +
                ", oid='" + oid + '\'' +
                ", prioriteetti=" + prioriteetti +
                ", aloituspaikat=" + aloituspaikat +
                ", hakemukset=" + hakemukset +
                '}';
    }

    public Boolean getEiVarasijatayttoa() {
        return eiVarasijatayttoa;
    }

    public void setEiVarasijatayttoa(Boolean eiVarasijatayttoa) {
        this.eiVarasijatayttoa = eiVarasijatayttoa;
    }

    public Boolean getKaikkiEhdonTayttavatHyvaksytaan() {
        return kaikkiEhdonTayttavatHyvaksytaan;
    }

    public void setKaikkiEhdonTayttavatHyvaksytaan(Boolean kaikkiEhdonTayttavatHyvaksytaan) {
        this.kaikkiEhdonTayttavatHyvaksytaan = kaikkiEhdonTayttavatHyvaksytaan;
    }

    public Boolean getPoissaOlevaTaytto() {
        return poissaOlevaTaytto;
    }

    public void setPoissaOlevaTaytto(Boolean poissaOlevaTaytto) {
        this.poissaOlevaTaytto = poissaOlevaTaytto;
    }

    public Integer getVarasijat() {
        return varasijat;
    }

    public void setVarasijat(Integer varasijat) {
        this.varasijat = varasijat;
    }

    public Integer getVarasijaTayttoPaivat() {
        return varasijaTayttoPaivat;
    }

    public void setVarasijaTayttoPaivat(Integer varasijaTayttoPaivat) {
        this.varasijaTayttoPaivat = varasijaTayttoPaivat;
    }

    public Date getVarasijojaKaytetaanAlkaen() {
        return varasijojaKaytetaanAlkaen;
    }

    public void setVarasijojaKaytetaanAlkaen(Date varasijojaKaytetaanAlkaen) {
        this.varasijojaKaytetaanAlkaen = varasijojaKaytetaanAlkaen;
    }

    public Date getVarasijojaTaytetaanAsti() {
        return varasijojaTaytetaanAsti;
    }

    public String getTayttojono() {
        return tayttojono;
    }

    public void setTayttojono(String tayttojono) {
        this.tayttojono = tayttojono;
    }

    public void setVarasijojaTaytetaanAsti(Date varasijojaTaytetaanAsti) {
        this.varasijojaTaytetaanAsti = varasijojaTaytetaanAsti;
    }

    public Integer getHyvaksytty() {
        return hyvaksytty;
    }

    public void setHyvaksytty(Integer hyvaksytty) {
        this.hyvaksytty = hyvaksytty;
    }

    public Integer getVaralla() {
        return varalla;
    }

    public void setVaralla(Integer varalla) {
        this.varalla = varalla;
    }

    public BigDecimal getAlinHyvaksyttyPistemaara() {
        return alinHyvaksyttyPistemaara;
    }

    public void setAlinHyvaksyttyPistemaara(BigDecimal alinHyvaksyttyPistemaara) {
        this.alinHyvaksyttyPistemaara = alinHyvaksyttyPistemaara;
    }

    public Integer getAlkuperaisetAloituspaikat() {
        return alkuperaisetAloituspaikat;
    }

    public void setAlkuperaisetAloituspaikat(Integer alkuperaisetAloituspaikat) {
        this.alkuperaisetAloituspaikat = alkuperaisetAloituspaikat;
    }

    public Boolean getValintaesitysHyvaksytty() {
        return valintaesitysHyvaksytty;
    }

    public void setValintaesitysHyvaksytty(Boolean valintaesitysHyvaksytty) {
        this.valintaesitysHyvaksytty = valintaesitysHyvaksytty;
    }

    public Boolean getSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa() {
        return sijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa;
    }

    public void setSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa(boolean sijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa) {
        this.sijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa = sijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa;
    }
}
