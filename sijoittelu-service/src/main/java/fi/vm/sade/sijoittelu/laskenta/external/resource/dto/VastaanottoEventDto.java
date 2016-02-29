package fi.vm.sade.sijoittelu.laskenta.external.resource.dto;

import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;

public class VastaanottoEventDto {
    private String henkiloOid;
    private String hakemusOid;
    private String hakukohdeOid;
    private String hakuOid;
    private ValintatuloksenTila tila;
    private String ilmoittaja;

    public VastaanottoEventDto() {
    }
    public VastaanottoEventDto(String henkiloOid, String hakemusOid, String hakukohdeOid, String hakuOid, ValintatuloksenTila tila, String ilmoittaja) {
        this.henkiloOid = henkiloOid;
        this.hakemusOid = hakemusOid;
        this.hakukohdeOid = hakukohdeOid;
        this.hakuOid = hakuOid;
        this.tila = tila;
        this.ilmoittaja = ilmoittaja;
    }

    public String getHakemusOid() {
        return hakemusOid;
    }

    public String getHakukohdeOid() {
        return hakukohdeOid;
    }

    public String getHakuOid() {
        return hakuOid;
    }

    public String getHenkiloOid() {
        return henkiloOid;
    }

    public String getIlmoittaja() {
        return ilmoittaja;
    }

    public ValintatuloksenTila getTila() {
        return tila;
    }
}
