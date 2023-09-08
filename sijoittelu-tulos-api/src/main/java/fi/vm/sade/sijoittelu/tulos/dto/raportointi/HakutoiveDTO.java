package fi.vm.sade.sijoittelu.tulos.dto.raportointi;

import fi.vm.sade.sijoittelu.tulos.dto.ValintatuloksenTila;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Hakemukseen liittyvän hakutoiveen tila sijoittelussa, hakujonoittain.
 */

@Schema(description = "Hakutoive")
public class HakutoiveDTO implements Comparable<HakutoiveDTO> {

    private Integer hakutoive;

    private String hakukohdeOid;

    private String tarjoajaOid;

    private ValintatuloksenTila vastaanottotieto = ValintatuloksenTila.KESKEN;

    private List<HakutoiveenValintatapajonoDTO> hakutoiveenValintatapajonot = new ArrayList<HakutoiveenValintatapajonoDTO>();

    private List<HakijaryhmaDTO> hakijaryhmat = new ArrayList<HakijaryhmaDTO>();

    private boolean kaikkiJonotSijoiteltu = true;

    private BigDecimal ensikertalaisuusHakijaryhmanAlimmatHyvaksytytPisteet;

    public BigDecimal getEnsikertalaisuusHakijaryhmanAlimmatHyvaksytytPisteet() {
        return ensikertalaisuusHakijaryhmanAlimmatHyvaksytytPisteet;
    }

    public void setEnsikertalaisuusHakijaryhmanAlimmatHyvaksytytPisteet(BigDecimal ensikertalaisuusHakijaryhmanAlimmatHyvaksytytPisteet) {
        this.ensikertalaisuusHakijaryhmanAlimmatHyvaksytytPisteet = ensikertalaisuusHakijaryhmanAlimmatHyvaksytytPisteet;
    }

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

    public boolean isKaikkiJonotSijoiteltu() {
        return kaikkiJonotSijoiteltu;
    }

    public void setKaikkiJonotSijoiteltu(boolean kaikkiJonotSijoiteltu) {
        this.kaikkiJonotSijoiteltu = kaikkiJonotSijoiteltu;
    }

    public ValintatuloksenTila getVastaanottotieto() {
        return vastaanottotieto;
    }

    public void setVastaanottotieto(ValintatuloksenTila vastaanottotieto) {
        this.vastaanottotieto = vastaanottotieto;
    }

    public List<HakijaryhmaDTO> getHakijaryhmat() {
        return hakijaryhmat;
    }

    public void setHakijaryhmat(List<HakijaryhmaDTO> hakijaryhmat) {
        this.hakijaryhmat = hakijaryhmat;
    }
}
