package fi.vm.sade.sijoittelu.domain.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VastaanottoDTO {
    private String henkiloOid;
    private String hakukohdeOid;
    private String action;

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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public VastaanottoType typeOfVastaanotto() {
        return VastaanottoType.valueOf(action);
    }

    public enum VastaanottoType {
        VastaanotaSitovasti(true),
        VastaanotaEhdollisesti(false);

        public final boolean sitova;

        VastaanottoType(boolean sitova) {
            this.sitova = sitova;
        }
    }
}
