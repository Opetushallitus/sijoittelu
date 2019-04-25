package fi.vm.sade.sijoittelu.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SijoitteluAjo implements Serializable {

    private Long sijoitteluajoId;

    private String hakuOid;

    private Long startMils;

    private Long endMils;

    private List<HakukohdeItem> hakukohteet = new ArrayList<HakukohdeItem>();

    public Long getSijoitteluajoId() {
        return sijoitteluajoId;
    }

    public void setSijoitteluajoId(Long sijoitteluajoId) {
        this.sijoitteluajoId = sijoitteluajoId;
    }

    public Long getStartMils() {
        return startMils;
    }

    public void setStartMils(Long startMils) {
        this.startMils = startMils;
    }

    public Long getEndMils() {
        return endMils;
    }

    public void setEndMils(Long endMils) {
        this.endMils = endMils;
    }

    public List<HakukohdeItem> getHakukohteet() {
        return hakukohteet;
    }

    public void setHakukohteet(List<HakukohdeItem> hakukohteet) {
        this.hakukohteet = hakukohteet;
    }

    public String getHakuOid() {
        return hakuOid;
    }

    public void setHakuOid(String hakuOid) {
        this.hakuOid = hakuOid;
    }
}
