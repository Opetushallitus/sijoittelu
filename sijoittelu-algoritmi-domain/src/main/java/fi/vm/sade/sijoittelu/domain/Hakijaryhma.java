package fi.vm.sade.sijoittelu.domain;

import com.google.code.morphia.annotations.Embedded;
import org.codehaus.jackson.map.annotate.JsonView;

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

    @JsonView(JsonViews.Basic.class)
    private Integer prioriteetti;

    @JsonView(JsonViews.Basic.class)
    private int paikat;

    @JsonView(JsonViews.Basic.class)
    private String oid;

    @JsonView(JsonViews.Basic.class)
    private String nimi;

    @JsonView(JsonViews.Basic.class)
    private List<String> hakijaOid = new ArrayList<String>();

    public List<String> getHakijaOid() {
        return hakijaOid;
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
                ", hakijaOid=" + hakijaOid +
                '}';
    }
}
