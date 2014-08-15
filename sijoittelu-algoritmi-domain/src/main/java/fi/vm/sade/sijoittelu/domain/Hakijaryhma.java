package fi.vm.sade.sijoittelu.domain;

import org.mongodb.morphia.annotations.Embedded;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Kari Kammonen
 *
 */
@Embedded
public class Hakijaryhma implements Serializable {

    private Integer prioriteetti;

    private int paikat;

    private String oid;

    private String nimi;

    private List<String> hakemusOid = new ArrayList<String>();

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
                ", hakemusOid=" + hakemusOid +
                '}';
    }
}
