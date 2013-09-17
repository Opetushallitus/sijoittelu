package fi.vm.sade.sijoittelu.domain.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: kkammone
 * Date: 16.9.2013
 * Time: 14:45
 * To change this template use File | Settings | File Templates.
 */
public class HakijaRaportointiDTO {

    private String hakemusOid;

    private String etunimi;

    private String sukunimi;

    private List<RaportointiHakutoiveDTO> hakutoiveet = new ArrayList<RaportointiHakutoiveDTO>();


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

    public List<RaportointiHakutoiveDTO> getHakutoiveet() {
        return hakutoiveet;
    }

    public void setHakutoiveet(List<RaportointiHakutoiveDTO> hakutoiveet) {
        this.hakutoiveet = hakutoiveet;
    }
}
