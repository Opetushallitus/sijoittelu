package fi.vm.sade.sijoittelu.tulos.dto.raportointi;

import fi.vm.sade.sijoittelu.tulos.dto.PistetietoDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Hakemukseen liittyvän hakutoiveen tila sijoittelussa, hakujonoittain.
 */
public class HakutoiveDTO implements Comparable<HakutoiveDTO> {

    private Integer hakutoive;

    private String hakukohdeOid;

    private String tarjoajaOid;

    private List<PistetietoDTO> pistetiedot = new ArrayList<PistetietoDTO>();

    private List<HakutoiveenValintatapajonoDTO> hakutoiveenValintatapajonot = new ArrayList<HakutoiveenValintatapajonoDTO>();

    private boolean kaikkiJonotSijoiteltu = true;

    @Override
    public int compareTo(HakutoiveDTO o) {
        int diff = nullAwareCompare(hakutoive, o.hakutoive);
        if (diff != 0) {
            return diff;
        }
        return nullAwareCompare(hakukohdeOid, o.hakukohdeOid);
    }

    private <T extends Comparable<T>> int nullAwareCompare(T o1, T o2) {
        if (o1 == null) {
            if (o2 == null) {
                return 0;
            }
            return 1;
        }
        if (o2 == null) {
            return -1;
        }
        return o1.compareTo(o2);
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
