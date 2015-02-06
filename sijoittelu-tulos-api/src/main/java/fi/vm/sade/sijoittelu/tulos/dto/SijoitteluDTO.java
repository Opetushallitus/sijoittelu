package fi.vm.sade.sijoittelu.tulos.dto;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA. User: kkammone Date: 5.9.2013 Time: 15:35 To
 * change this template use File | Settings | File Templates.
 */
public class SijoitteluDTO implements Serializable {

    private String hakuOid;

    private Long sijoitteluId;

    private Date created;

    private boolean sijoittele = true;

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
