package fi.vm.sade.sijoittelu.tulos.dto.raportointi;

import fi.vm.sade.sijoittelu.tulos.dto.JsonViews;
import org.codehaus.jackson.map.annotate.JsonView;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

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
    private SortedSet<HakutoiveDTO> hakutoiveet = new TreeSet<HakutoiveDTO>();

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

    public Set<HakutoiveDTO> getHakutoiveet() { //fucked up, but otherwise swagger wont work
        return hakutoiveet;
    }

    public void setHakutoiveet(SortedSet<HakutoiveDTO> hakutoiveet) {
        this.hakutoiveet = hakutoiveet;
    }
}
