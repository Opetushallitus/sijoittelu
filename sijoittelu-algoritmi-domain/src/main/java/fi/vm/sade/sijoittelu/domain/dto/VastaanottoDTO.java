package fi.vm.sade.sijoittelu.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VastaanottoDTO {
    private String henkiloOid;
    private String hakukohdeOid;
    private ValintatuloksenTila action;

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

    public ValintatuloksenTila getAction() {
        return action;
    }

    public void setAction(ValintatuloksenTila action) {
        this.action = action;
    }

    @Override
    public String toString() {
        return "VastaanottoDTO{" +
                "henkiloOid='" + henkiloOid + '\'' +
                ", hakukohdeOid='" + hakukohdeOid + '\'' +
                ", action='" + action + '\'' +
                '}';
    }
}
