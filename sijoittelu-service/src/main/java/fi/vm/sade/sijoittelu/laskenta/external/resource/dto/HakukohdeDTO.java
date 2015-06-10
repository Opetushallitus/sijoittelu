package fi.vm.sade.sijoittelu.laskenta.external.resource.dto;

import java.util.Set;

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
