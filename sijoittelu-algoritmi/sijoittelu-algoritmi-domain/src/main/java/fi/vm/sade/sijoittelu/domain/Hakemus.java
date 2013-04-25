package fi.vm.sade.sijoittelu.domain;

import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Transient;

/**
 * 
 * @author Kari Kammonen
 * 
 */
@Embedded
public class Hakemus {

    private String hakijaOid;

    /**
     * defaulttina tosi epakorkea
     */
    private Integer prioriteetti;

    private Integer jonosija;
    
    private Integer tasasijaJonosija;

    private HakemuksenTila tila; 


     public int getPrioriteetti() {
        return prioriteetti;
    }

    public void setPrioriteetti(int prioriteetti) {
        this.prioriteetti = prioriteetti;
    }

    public int getJonosija() {
        return jonosija;
    }

    public void setJonosija(int jonosija) {
        this.jonosija = jonosija;
    }

    public HakemuksenTila getTila() {
        return tila;
    }

    public void setTila(HakemuksenTila tila) {
        this.tila = tila;
    }

    public String getHakijaOid() {
        return hakijaOid;
    }

    public void setHakijaOid(String hakijaOid) {
        this.hakijaOid = hakijaOid;
    }

    public Integer getTasasijaJonosija() {
        return tasasijaJonosija;
    }

    public void setTasasijaJonosija(Integer tasasijaJonosija) {
        this.tasasijaJonosija = tasasijaJonosija;
    }

}
