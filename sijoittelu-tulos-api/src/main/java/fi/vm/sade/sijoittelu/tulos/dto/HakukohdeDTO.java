package fi.vm.sade.sijoittelu.tulos.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HakukohdeDTO implements Serializable {
    private Long sijoitteluajoId;

    private String oid;

    private HakukohdeTila tila;

    private String tarjoajaOid;

    private List<ValintatapajonoDTO> valintatapajonot = new ArrayList<ValintatapajonoDTO>();

    private List<HakijaryhmaDTO> hakijaryhmat = new ArrayList<HakijaryhmaDTO>();

    private boolean kaikkiJonotSijoiteltu = true;

    public Long getSijoitteluajoId() {
        return sijoitteluajoId;
    }

    public void setSijoitteluajoId(Long sijoitteluajoId) {
        this.sijoitteluajoId = sijoitteluajoId;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public HakukohdeTila getTila() {
        return tila;
    }

    public void setTila(HakukohdeTila tila) {
        this.tila = tila;
    }

    public String getTarjoajaOid() {
        return tarjoajaOid;
    }

    public void setTarjoajaOid(String tarjoajaOid) {
        this.tarjoajaOid = tarjoajaOid;
    }

    public List<ValintatapajonoDTO> getValintatapajonot() {
        return valintatapajonot;
    }

    public void setValintatapajonot(List<ValintatapajonoDTO> valintatapajonot) {
        this.valintatapajonot = valintatapajonot;
    }

    public List<HakijaryhmaDTO> getHakijaryhmat() {
        return hakijaryhmat;
    }

    public void setHakijaryhmat(List<HakijaryhmaDTO> hakijaryhmat) {
        this.hakijaryhmat = hakijaryhmat;
    }

    public boolean isKaikkiJonotSijoiteltu() {
        return kaikkiJonotSijoiteltu;
    }

    public void setKaikkiJonotSijoiteltu(boolean kaikkiJonotSijoiteltu) {
        this.kaikkiJonotSijoiteltu = kaikkiJonotSijoiteltu;
    }
}
