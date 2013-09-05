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

@Embedded("LogEntry")
public class    LogEntry {
    @Id
    private ObjectId id;

    private Date luotu;
    private String muokkaaja;
    private String muutos;
    private String selite;

    public Date getLuotu() {
        return luotu;
    }

    public void setLuotu(Date luotu) {
        this.luotu = luotu;
    }

    public String getMuokkaaja() {
        return muokkaaja;
    }

    public void setMuokkaaja(String muokkaaja) {
        this.muokkaaja = muokkaaja;
    }

    public String getSelite() {
        return selite;
    }

    public void setSelite(String selite) {
        this.selite = selite;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getMuutos() {
        return muutos;
    }

    public void setMuutos(String muutos) {
        this.muutos = muutos;
    }
}

