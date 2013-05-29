package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers;

import fi.vm.sade.sijoittelu.domain.Valintatulos;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Kari Kammonen
 * 
 */
public class HenkiloWrapper {

    private List<HakemusWrapper> hakemukset = new ArrayList<HakemusWrapper>();

    private String hakijaOid;

    private String hakemusOid;

    //henkilolla voi poikkeustapauksissa olla useampi valintatulos
    private List<Valintatulos> valintatulos = new ArrayList<Valintatulos>();;

    public List<HakemusWrapper> getHakemukset() {
        return hakemukset;
    }

    public String getHakijaOid() {
        return hakijaOid;
    }

    public void setHakijaOid(String henkiloOid) {
        this.hakijaOid = henkiloOid;
    }

    public String getHakemusOid() {
        return hakemusOid;
    }


    public void setHakemusOid(String hakemusOid) {
        this.hakemusOid = hakemusOid;
    }

    public List<Valintatulos> getValintatulos() {
        return valintatulos;
    }

    public void setValintatulos(List<Valintatulos> valintatulos) {
        this.valintatulos = valintatulos;
    }
}
