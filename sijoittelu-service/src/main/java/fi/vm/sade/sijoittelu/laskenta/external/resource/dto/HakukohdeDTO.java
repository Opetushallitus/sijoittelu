package fi.vm.sade.sijoittelu.laskenta.external.resource.dto;

import io.swagger.annotations.ApiModel;

import java.util.Set;

@ApiModel(value = "sijoittelu.laskenta.external.resource.dto.Hakukohde", description = "Hakukohde")
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
