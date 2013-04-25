package fi.vm.sade.sijoittelu.domain;

import java.util.ArrayList;
import java.util.List;

import org.bson.types.ObjectId;

import com.google.code.morphia.annotations.Embedded;
import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Id;

/**
 * 
 * @author Kari Kammonen
 * 
 */
@Entity("SijoitteluAjo")
public class SijoitteluAjo {

    @SuppressWarnings("unused")
    @Id
    private ObjectId id;
    
    private Long sijoitteluajoId;

    private Long startMils;

    private Long endMils;
    
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
