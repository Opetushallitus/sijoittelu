package fi.vm.sade.sijoittelu.tulos.dto.raportointi;

import fi.vm.sade.sijoittelu.tulos.dto.JsonViews;
import fi.vm.sade.sijoittelu.tulos.dto.PistetietoDTO;
import org.codehaus.jackson.map.annotate.JsonView;

import java.util.ArrayList;
import java.util.List;

/**
 *  Hakemukseen liittyvän hakutoiveen tila sijoittelussa, hakujonoittain.
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

    private boolean kaikkiJonotSijoiteltu = true;

    @Override
    public int compareTo(HakutoiveDTO o) {
        if (hakutoive == null) {
            return 0;
        }
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

    public boolean isKaikkiJonotSijoiteltu() {
        return kaikkiJonotSijoiteltu;
    }

    public void setKaikkiJonotSijoiteltu(boolean kaikkiJonotSijoiteltu) {
        this.kaikkiJonotSijoiteltu = kaikkiJonotSijoiteltu;
    }
}
