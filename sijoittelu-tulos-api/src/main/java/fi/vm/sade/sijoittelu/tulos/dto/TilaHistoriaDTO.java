package fi.vm.sade.sijoittelu.tulos.dto;

import org.codehaus.jackson.map.annotate.JsonView;

import java.util.Date;

/**
 * Created by jukais on 11.2.2014.
 */
public class TilaHistoriaDTO {
    @JsonView({ JsonViews.Hakemus.class, JsonViews.Hakukohde.class })
    private String tila;

    @JsonView({ JsonViews.Hakemus.class, JsonViews.Hakukohde.class })
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
