package fi.vm.sade.sijoittelu.domain.dto;

import fi.vm.sade.sijoittelu.domain.JsonViews;
import org.codehaus.jackson.map.annotate.JsonView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kkammone
 * Date: 5.9.2013
 * Time: 15:35
 * To change this template use File | Settings | File Templates.
 */
public class SijoitteluDTO implements Serializable {

    @JsonView(JsonViews.Sijoittelu.class)
    private String hakuOid;

    @JsonView(JsonViews.Sijoittelu.class)
    private Long sijoitteluId;

    @JsonView(JsonViews.Sijoittelu.class)
    private Date created;

    @JsonView(JsonViews.Sijoittelu.class)
    private boolean sijoittele = true;

    @JsonView(JsonViews.Sijoittelu.class)
    private List<SijoitteluajoDTO> sijoitteluajot = new ArrayList<SijoitteluajoDTO>();

    public String getHakuOid() {
        return hakuOid;
    }

    public void setHakuOid(String hakuOid) {
        this.hakuOid = hakuOid;
    }

    public Long getSijoitteluId() {
        return sijoitteluId;
    }

    public void setSijoitteluId(Long sijoitteluId) {
        this.sijoitteluId = sijoitteluId;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public boolean isSijoittele() {
        return sijoittele;
    }

    public void setSijoittele(boolean sijoittele) {
        this.sijoittele = sijoittele;
    }

    public List<SijoitteluajoDTO> getSijoitteluajot() {
        return sijoitteluajot;
    }

    public void setSijoitteluajot(List<SijoitteluajoDTO> sijoitteluajot) {
        this.sijoitteluajot = sijoitteluajot;
    }
}
