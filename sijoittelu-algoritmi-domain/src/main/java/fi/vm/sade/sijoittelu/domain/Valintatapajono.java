package fi.vm.sade.sijoittelu.domain;

import com.google.code.morphia.annotations.Embedded;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author Kari Kammonen
 */
@Embedded
public class Valintatapajono implements Serializable {

    private Tasasijasaanto tasasijasaanto;

    private ValintatapajonoTila tila;

    private String oid;

    private Integer prioriteetti;

    private Integer aloituspaikat;

    private Boolean eiVarasijatayttoa;

    //@Embedded
    //private List<Saanto> saannot = new ArrayList<Saanto>();

    @Embedded
    private ArrayList<Hakemus> hakemukset = new ArrayList<Hakemus>();

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

    public int getPrioriteetti() {
        return prioriteetti;
    }

    public void setPrioriteetti(int prioriteetti) {
        this.prioriteetti = prioriteetti;
    }

    public int getAloituspaikat() {
        return aloituspaikat;
    }

    public void setAloituspaikat(int aloituspaikat) {
        this.aloituspaikat = aloituspaikat;
    }

    public ArrayList<Hakemus> getHakemukset() {
        return hakemukset;
    }

    //public List<Saanto> getSaannot() {
//		return saannot;
    //}

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
}
