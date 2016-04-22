package fi.vm.sade.sijoittelu.laskenta.external.resource.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VastaanottoDTO {
    private String henkiloOid;
    private String hakukohdeOid;

    public VastaanottoDTO() { }

    public String getHenkiloOid() {
        return henkiloOid;
    }

    public void setHenkiloOid(String henkiloOid) {
        this.henkiloOid = henkiloOid;
    }

    public String getHakukohdeOid() {
        return hakukohdeOid;
    }

    public void setHakukohdeOid(String hakukohdeOid) {
        this.hakukohdeOid = hakukohdeOid;
    }
}
