package fi.vm.sade.sijoittelu.tulos.dto;

import java.util.Date;

public class TilaHistoriaDTO {
    private String tila;

    private Date luotu;

    public String getTila() {
        return tila;
    }

    public void setTila(String tila) {
        this.tila = tila;
    }

    public Date getLuotu() {
        return luotu;
    }

    public void setLuotu(Date luotu) {
        this.luotu = luotu;
    }
}
