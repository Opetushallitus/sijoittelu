package fi.vm.sade.sijoittelu.domain;

import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;
import org.bson.types.ObjectId;
import org.codehaus.jackson.map.annotate.JsonView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Kari Kammonen
 * 
 */
//@Entity("SijoitteluAjo")
@Embedded
public class SijoitteluAjo implements Serializable {

    @SuppressWarnings("unused")
    @Id
    private ObjectId id;

    @JsonView(JsonViews.Basic.class)
    private Long sijoitteluajoId;

    @JsonView(JsonViews.Basic.class)
    private Long startMils;

    @JsonView(JsonViews.Basic.class)
    private Long endMils;

    @JsonView(JsonViews.Basic.class)
    @Embedded
    private List<HakukohdeItem> hakukohteet = new ArrayList<HakukohdeItem>();

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

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

}
