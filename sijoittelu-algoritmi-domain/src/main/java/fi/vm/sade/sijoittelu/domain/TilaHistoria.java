package fi.vm.sade.sijoittelu.domain;

/**
 * Created with IntelliJ IDEA.
 * User: jukais
 * Date: 14.8.2013
 * Time: 7.42
 * To change this template use File | Settings | File Templates.
 */

import org.mongodb.morphia.annotations.Embedded;

import java.io.Serializable;
import java.util.Date;

@Embedded("TilaHistoria")
public class TilaHistoria implements Serializable {

    private Date luotu;

    private HakemuksenTila tila;

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
}

