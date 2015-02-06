package fi.vm.sade.sijoittelu.laskenta.external.resource.dto;

import java.util.Set;

/**
 * Created by kjsaila on 06/02/15.
 */
public class HakukohdeDTO {
    private Set<String> tarjoajaOids;

    public HakukohdeDTO(){}

    public Set<String> getTarjoajaOids() {
        return tarjoajaOids;
    }

    public void setTarjoajaOids(Set<String> tarjoajaOids) {
        this.tarjoajaOids = tarjoajaOids;
    }
}
