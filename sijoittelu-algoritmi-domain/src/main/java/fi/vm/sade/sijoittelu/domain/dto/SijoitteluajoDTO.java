package fi.vm.sade.sijoittelu.domain.dto;


import fi.vm.sade.sijoittelu.domain.JsonViews;
import org.codehaus.jackson.map.annotate.JsonView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kkammone
 * Date: 5.9.2013
 * Time: 14:32
 * To change this template use File | Settings | File Templates.
 */
public class SijoitteluajoDTO implements Serializable {

    @JsonView(JsonViews.Sijoitteluajo.class)
    private Long sijoitteluajoId;

    @JsonView(JsonViews.Sijoitteluajo.class)
    private String hakuOid;

    @JsonView(JsonViews.Sijoitteluajo.class)
    private Long startMils;

    @JsonView(JsonViews.Sijoitteluajo.class)
    private Long endMils;

    @JsonView(JsonViews.Sijoitteluajo.class)
    private List<HakukohdeDTO> hakukohteet = new ArrayList<HakukohdeDTO>();

    public Long getSijoitteluajoId() {
        return sijoitteluajoId;
    }

    public void setSijoitteluajoId(Long sijoitteluajoId) {
        this.sijoitteluajoId = sijoitteluajoId;
    }

    public String getHakuOid() {
        return hakuOid;
    }

    public void setHakuOid(String hakuOid) {
        this.hakuOid = hakuOid;
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

    public List<HakukohdeDTO> getHakukohteet() {
        return hakukohteet;
    }

    public void setHakukohteet(List<HakukohdeDTO> hakukohteet) {
        this.hakukohteet = hakukohteet;
    }
}
