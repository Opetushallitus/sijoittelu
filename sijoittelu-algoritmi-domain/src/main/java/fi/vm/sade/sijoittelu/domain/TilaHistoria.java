package fi.vm.sade.sijoittelu.domain;

/**
 * Created with IntelliJ IDEA.
 * User: jukais
 * Date: 14.8.2013
 * Time: 7.42
 * To change this template use File | Settings | File Templates.
 */

import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Id;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.annotate.JsonView;

import java.util.Date;

@Embedded("TilaHistoria")
public class TilaHistoria {
    @Id
    private ObjectId id;

    @JsonView({JsonViews.Tila.class, JsonViews.MonenHakemuksenTila.class})
    private Date luotu;
    @JsonView({JsonViews.Tila.class, JsonViews.MonenHakemuksenTila.class})
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

