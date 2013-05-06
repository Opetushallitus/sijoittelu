package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers;

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

    public List<HakemusWrapper> getHakemukset() {
        return hakemukset;
    }

    public String getHenkiloOid() {
        return henkiloOid;
    }

    public void setHenkiloOid(String henkiloOid) {
        this.henkiloOid = henkiloOid;
    }

}
