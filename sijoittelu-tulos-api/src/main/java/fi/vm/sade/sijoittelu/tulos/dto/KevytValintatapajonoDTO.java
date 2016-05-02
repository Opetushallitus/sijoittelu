package fi.vm.sade.sijoittelu.tulos.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class KevytValintatapajonoDTO {
    private String oid;
    private Boolean eiVarasijatayttoa;
    private Date varasijojaKaytetaanAlkaen;
    private Date varasijojaTaytetaanAsti;
    private List<KevytHakemusDTO> hakemukset = new ArrayList<KevytHakemusDTO>();

    public KevytValintatapajonoDTO() {};

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public Boolean getEiVarasijatayttoa() {
        return eiVarasijatayttoa;
    }

    public void setEiVarasijatayttoa(Boolean eiVarasijatayttoa) {
        this.eiVarasijatayttoa = eiVarasijatayttoa;
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

    public void setVarasijojaTaytetaanAsti(Date varasijojaTaytetaanAsti) {
        this.varasijojaTaytetaanAsti = varasijojaTaytetaanAsti;
    }

    public List<KevytHakemusDTO> getHakemukset() {
        return hakemukset;
    }

    public void setHakemukset(List<KevytHakemusDTO> hakemukset) {
        this.hakemukset = hakemukset;
    }
}
