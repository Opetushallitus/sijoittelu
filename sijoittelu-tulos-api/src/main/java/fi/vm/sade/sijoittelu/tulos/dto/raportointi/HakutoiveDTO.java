package fi.vm.sade.sijoittelu.tulos.dto.raportointi;

import fi.vm.sade.sijoittelu.tulos.dto.JsonViews;
import org.codehaus.jackson.map.annotate.JsonView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kkammone
 * Date: 17.9.2013
 * Time: 9:47
 * To change this template use File | Settings | File Templates.
 */
public class HakutoiveDTO {

    @JsonView(JsonViews.Hakija.class)
    private Integer hakutoive ;

    @JsonView(JsonViews.Hakija.class)
    private String hakukohdeOid;

    @JsonView(JsonViews.Hakija.class)
    private List<HakutoiveenValintatapajonoDTO> hakutoiveenValintatapajonot = new ArrayList<HakutoiveenValintatapajonoDTO>();

    public String getHakukohdeOid() {
        return hakukohdeOid;
    }

    public void setHakukohdeOid(String hakukohdeOid) {
        this.hakukohdeOid = hakukohdeOid;
    }

    public Integer getHakutoive() {
        return hakutoive;
    }

    public void setHakutoive(Integer hakutoive) {
        this.hakutoive = hakutoive;
    }

    public List<HakutoiveenValintatapajonoDTO> getHakutoiveenValintatapajonot() {
        return hakutoiveenValintatapajonot;
    }

    public void setHakutoiveenValintatapajonot(List<HakutoiveenValintatapajonoDTO> hakutoiveenValintatapajonot) {
        this.hakutoiveenValintatapajonot = hakutoiveenValintatapajonot;
    }
}
