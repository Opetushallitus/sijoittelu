package fi.vm.sade.sijoittelu.tulos.dto.raportointi;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class KevytHakijaDTO {
    private String hakijaOid;
    private String hakemusOid;
    private SortedSet<KevytHakutoiveDTO> hakutoiveet = new TreeSet<KevytHakutoiveDTO>();


    public Set<KevytHakutoiveDTO> getHakutoiveet() {
        return hakutoiveet;
    }

    public void setHakutoiveet(SortedSet<KevytHakutoiveDTO> hakutoiveet) {
        this.hakutoiveet = hakutoiveet;
    }

    public String getHakijaOid() {
        return hakijaOid;
    }

    public void setHakijaOid(String hakijaOid) {
        this.hakijaOid = hakijaOid;
    }

    public String getHakemusOid() {
        return hakemusOid;
    }

    public void setHakemusOid(String hakemusOid) {
        this.hakemusOid = hakemusOid;
    }
}
