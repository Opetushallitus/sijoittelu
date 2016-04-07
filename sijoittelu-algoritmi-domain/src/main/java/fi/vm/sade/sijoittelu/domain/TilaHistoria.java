package fi.vm.sade.sijoittelu.domain;

import org.mongodb.morphia.annotations.Embedded;

import java.io.Serializable;
import java.util.Date;

@Embedded("TilaHistoria")
public class TilaHistoria implements Serializable {

    private Date luotu;

    private HakemuksenTila tila;

    public TilaHistoria() {
        this.luotu = new Date();
    }

    public TilaHistoria(HakemuksenTila tila) {
        this.luotu = new Date();
        this.tila = tila;
    }

    public Date getLuotu() {
        return luotu;
    }

    public void setLuotu(Date luotu) {
        this.luotu = luotu;
    }

    public HakemuksenTila getTila() {
        return tila;
    }

    public void setTila(HakemuksenTila tila) {
        this.tila = tila;
    }

    @Override
    public String toString() {
        return "TilaHistoria{" +
                "luotu=" + luotu +
                ", tila=" + tila +
                '}';
    }
}

