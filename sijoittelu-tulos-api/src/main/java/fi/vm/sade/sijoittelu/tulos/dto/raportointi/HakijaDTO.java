package fi.vm.sade.sijoittelu.tulos.dto.raportointi;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Hakemuksen tilaa sijoittelussa, sisältäen kunkin hakutoiveen osalta hakujonokohtaisen tilan.
 */
public class HakijaDTO {

    private String hakijaOid;

    private String hakemusOid;

    private String etunimi;

    private String sukunimi;

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

    public String getHakijaOid() {
        return hakijaOid;
    }

    public void setHakijaOid(final String hakijaOid) {
        this.hakijaOid = hakijaOid;
    }
}
