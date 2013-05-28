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

    private String henkiloOid;

    private String hakemusOid;

    private Valintatulos valintatulos;

    public List<HakemusWrapper> getHakemukset() {
        return hakemukset;
    }

    public String getHenkiloOid() {
        return henkiloOid;
    }

    public void setHenkiloOid(String henkiloOid) {
        this.henkiloOid = henkiloOid;
    }

    public String getHakemusOid() {
        return hakemusOid;
    }

    public Valintatulos getValintatulos() {
        return valintatulos;
    }

    public void setValintatulos(Valintatulos valintatulos) {
        this.valintatulos = valintatulos;
    }

    public void setHakemusOid(String hakemusOid) {
        this.hakemusOid = hakemusOid;
    }
}
