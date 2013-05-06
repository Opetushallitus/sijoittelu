package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers;

import fi.vm.sade.sijoittelu.domain.Hakemus;

/**
 * 
 * @author Kari Kammonen
 * 
 */
public class HakemusWrapper {

    private Hakemus hakemus;

    private ValintatapajonoWrapper valintatapajono;

    private HenkiloWrapper henkilo;

    public HenkiloWrapper getHenkilo() {
        return henkilo;
    }

    public void setHenkilo(HenkiloWrapper henkilo) {
        this.henkilo = henkilo;
    }

    public Hakemus getHakemus() {
        return hakemus;
    }

    public void setHakemus(Hakemus hakemus) {
        this.hakemus = hakemus;
    }

    public ValintatapajonoWrapper getValintatapajono() {
        return valintatapajono;
    }

    public void setValintatapajono(ValintatapajonoWrapper valintatapajono) {
        this.valintatapajono = valintatapajono;
    }
}
