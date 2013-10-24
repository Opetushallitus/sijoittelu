package fi.vm.sade.sijoittelu.tulos.dto.raportointi;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonView;

import fi.vm.sade.sijoittelu.tulos.dto.JsonViews;
import fi.vm.sade.sijoittelu.tulos.dto.PistetietoDTO;

/**
 * Created with IntelliJ IDEA. User: kkammone Date: 17.9.2013 Time: 9:47 To
 * change this template use File | Settings | File Templates.
 */
public class HakutoiveDTO implements Comparable<HakutoiveDTO> {

    @JsonView(JsonViews.Hakija.class)
    private Integer hakutoive;

    @JsonView(JsonViews.Hakija.class)
    private String hakukohdeOid;

    @JsonView(JsonViews.Hakija.class)
    private String tarjoajaOid;

    @JsonView(JsonViews.Hakija.class)
    private List<PistetietoDTO> pistetiedot = new ArrayList<PistetietoDTO>();

    @JsonView(JsonViews.Hakija.class)
    private List<HakutoiveenValintatapajonoDTO> hakutoiveenValintatapajonot = new ArrayList<HakutoiveenValintatapajonoDTO>();

    @Override
    public int compareTo(HakutoiveDTO o) {
        return hakutoive.compareTo(o.hakutoive);
    }

    public String getHakukohdeOid() {
        return hakukohdeOid;
    }

    public void setTarjoajaOid(String tarjoajaOid) {
        this.tarjoajaOid = tarjoajaOid;
    }

    public String getTarjoajaOid() {
        return tarjoajaOid;
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

    public List<PistetietoDTO> getPistetiedot() {
        return pistetiedot;
    }

    public void setPistetiedot(List<PistetietoDTO> pistetiedot) {
        this.pistetiedot = pistetiedot;
    }
}
