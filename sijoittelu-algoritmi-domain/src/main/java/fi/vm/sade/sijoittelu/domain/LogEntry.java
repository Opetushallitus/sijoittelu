package fi.vm.sade.sijoittelu.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;

import java.io.Serializable;
import java.util.Date;

public class LogEntry implements Serializable {

    private Date luotu;

    private String muokkaaja;

    private String muutos;

    private String selite;

    public LogEntry() {}

    public LogEntry(String muokkaaja, String muutos, String selite) {
        this.luotu = new Date();
        this.muokkaaja = muokkaaja;
        this.muutos = muutos;
        this.selite = selite;
    }

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

    public String getMuutos() {
        return muutos;
    }

    public void setMuutos(String muutos) {
        this.muutos = muutos;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this,obj);
    }
}

