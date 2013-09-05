package fi.vm.sade.sijoittelu.domain.dto;

import fi.vm.sade.sijoittelu.domain.JsonViews;
import fi.vm.sade.sijoittelu.domain.Tasasijasaanto;
import fi.vm.sade.sijoittelu.domain.ValintatapajonoTila;
import org.codehaus.jackson.map.annotate.JsonView;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created with IntelliJ IDEA.
 * User: kkammone
 * Date: 5.9.2013
 * Time: 12:47
 * To change this template use File | Settings | File Templates.
 */
public class ValintatapajonoDTO  implements Serializable {

    @JsonView(JsonViews.Hakukohde.class)
    private Tasasijasaanto tasasijasaanto;

    @JsonView(JsonViews.Hakukohde.class)
    private ValintatapajonoTila tila;

    @JsonView(JsonViews.Hakukohde.class)
    private String oid;

    @JsonView(JsonViews.Hakukohde.class)
    private Integer prioriteetti;

    @JsonView(JsonViews.Hakukohde.class)
    private Integer aloituspaikat;

    @JsonView(JsonViews.Hakukohde.class)
    private Boolean eiVarasijatayttoa;

    @JsonView(JsonViews.Hakukohde.class)
    private ArrayList<HakemusDTO> hakemukset = new ArrayList<HakemusDTO>();

    public Tasasijasaanto getTasasijasaanto() {
        return tasasijasaanto;
    }

    public void setTasasijasaanto(Tasasijasaanto tasasijasaanto) {
        this.tasasijasaanto = tasasijasaanto;
    }

    public ValintatapajonoTila getTila() {
        return tila;
    }

    public void setTila(ValintatapajonoTila tila) {
        this.tila = tila;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public Integer getPrioriteetti() {
        return prioriteetti;
    }

    public void setPrioriteetti(Integer prioriteetti) {
        this.prioriteetti = prioriteetti;
    }

    public Integer getAloituspaikat() {
        return aloituspaikat;
    }

    public void setAloituspaikat(Integer aloituspaikat) {
        this.aloituspaikat = aloituspaikat;
    }

    public Boolean getEiVarasijatayttoa() {
        return eiVarasijatayttoa;
    }

    public void setEiVarasijatayttoa(Boolean eiVarasijatayttoa) {
        this.eiVarasijatayttoa = eiVarasijatayttoa;
    }

    public ArrayList<HakemusDTO> getHakemukset() {
        return hakemukset;
    }

    public void setHakemukset(ArrayList<HakemusDTO> hakemukset) {
        this.hakemukset = hakemukset;
    }
}
