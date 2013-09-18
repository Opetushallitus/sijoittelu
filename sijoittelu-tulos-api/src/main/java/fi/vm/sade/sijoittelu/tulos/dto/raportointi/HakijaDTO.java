package fi.vm.sade.sijoittelu.tulos.dto.raportointi;

import fi.vm.sade.sijoittelu.tulos.dto.JsonViews;
import org.codehaus.jackson.map.annotate.JsonView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA. User: kkammone Date: 16.9.2013 Time: 14:45 To
 * change this template use File | Settings | File Templates.
 */
public class HakijaDTO {

    @JsonView(JsonViews.Hakija.class)
    private String hakemusOid;

    @JsonView(JsonViews.Hakija.class)
    private String etunimi;

    @JsonView(JsonViews.Hakija.class)
    private String sukunimi;

    @JsonView(JsonViews.Hakija.class)
    private List<HakutoiveDTO> hakutoiveet = new ArrayList<HakutoiveDTO>();

    public String getHakemusOid() {
        return hakemusOid;
    }

    public void setHakemusOid(String hakemusOid) {
        this.hakemusOid = hakemusOid;
    }

    public String getEtunimi() {
        return etunimi;
    }

    public void setEtunimi(String etunimi) {
        this.etunimi = etunimi;
    }

    public String getSukunimi() {
        return sukunimi;
    }

    public void setSukunimi(String sukunimi) {
        this.sukunimi = sukunimi;
    }

    public List<HakutoiveDTO> getHakutoiveet() {
        return hakutoiveet;
    }

    public void setHakutoiveet(List<HakutoiveDTO> hakutoiveet) {
        this.hakutoiveet = hakutoiveet;
    }
}
