package fi.vm.sade.sijoittelu.tulos.dto;

import java.util.ArrayList;
import java.util.List;

public class KevytHakukohdeDTO {
    private String oid;
    private String tarjoajaOid;
    private boolean kaikkiJonotSijoiteltu = true;
    private List<KevytValintatapajonoDTO> valintatapajonot = new ArrayList<KevytValintatapajonoDTO>();

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getTarjoajaOid() {
        return tarjoajaOid;
    }

    public void setTarjoajaOid(String tarjoajaOid) {
        this.tarjoajaOid = tarjoajaOid;
    }

    public boolean isKaikkiJonotSijoiteltu() {
        return kaikkiJonotSijoiteltu;
    }

    public void setKaikkiJonotSijoiteltu(boolean kaikkiJonotSijoiteltu) {
        this.kaikkiJonotSijoiteltu = kaikkiJonotSijoiteltu;
    }

    public List<KevytValintatapajonoDTO> getValintatapajonot() {
        return valintatapajonot;
    }

    public void setValintatapajonot(List<KevytValintatapajonoDTO> valintatapajonot) {
        this.valintatapajonot = valintatapajonot;
    }
}
