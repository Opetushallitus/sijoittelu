package fi.vm.sade.sijoittelu.laskenta.resource;


import java.util.List;

public class Tila {

    private boolean hyvaksy;
    private String tila;
    private List<String> tilanKuvaukset;

    public boolean isHyvaksy() {
        return hyvaksy;
    }

    public void setHyvaksy(boolean hyvaksy) {
        this.hyvaksy = hyvaksy;
    }

    public String getTila() {
        return tila;
    }

    public void setTila(String tila) {
        this.tila = tila;
    }

    public List<String> getTilanKuvaukset() {
        return tilanKuvaukset;
    }

    public void setTilanKuvaukset(List<String> tilanKuvaukset) {
        this.tilanKuvaukset = tilanKuvaukset;
    }
}
