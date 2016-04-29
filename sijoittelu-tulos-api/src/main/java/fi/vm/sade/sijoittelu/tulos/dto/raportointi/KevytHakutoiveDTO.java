package fi.vm.sade.sijoittelu.tulos.dto.raportointi;

import java.util.ArrayList;
import java.util.List;

public class KevytHakutoiveDTO implements Comparable<KevytHakutoiveDTO> {
    private Integer hakutoive;
    private String hakukohdeOid;
    private String tarjoajaOid;

    private List<KevytHakutoiveenValintatapajonoDTO> hakutoiveenValintatapajonot = new ArrayList<KevytHakutoiveenValintatapajonoDTO>();
    private boolean kaikkiJonotSijoiteltu = true;

    public String getHakukohdeOid() {
        return hakukohdeOid;
    }

    public void setHakukohdeOid(String hakukohdeOid) {
        this.hakukohdeOid = hakukohdeOid.intern();
    }

    public List<KevytHakutoiveenValintatapajonoDTO> getHakutoiveenValintatapajonot() {
        return hakutoiveenValintatapajonot;
    }

    public void setHakutoiveenValintatapajonot(List<KevytHakutoiveenValintatapajonoDTO> hakutoiveenValintatapajonot) {
        this.hakutoiveenValintatapajonot = hakutoiveenValintatapajonot;
    }

    public boolean isKaikkiJonotSijoiteltu() {
        return kaikkiJonotSijoiteltu;
    }

    public void setKaikkiJonotSijoiteltu(boolean kaikkiJonotSijoiteltu) {
        this.kaikkiJonotSijoiteltu = kaikkiJonotSijoiteltu;
    }

    public String getTarjoajaOid() {
        return tarjoajaOid;
    }

    public void setTarjoajaOid(String tarjoajaOid) {
        this.tarjoajaOid = tarjoajaOid.intern();
    }

    public Integer getHakutoive() {
        return hakutoive;
    }

    public void setHakutoive(Integer hakutoive) {
        this.hakutoive = hakutoive;
    }

    @Override
    public int compareTo(KevytHakutoiveDTO o) {
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
}
