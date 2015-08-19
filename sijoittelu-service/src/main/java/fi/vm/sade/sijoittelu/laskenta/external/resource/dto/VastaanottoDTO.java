package fi.vm.sade.sijoittelu.laskenta.external.resource.dto;

import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;

public class VastaanottoDTO {
    public final String hakukohdeOid;
    public final ValintatuloksenTila tila;
    public final String muokkaaja;
    public final String selite;

    public VastaanottoDTO(String hakukohdeOid, ValintatuloksenTila tila, String muokkaaja, String selite) {
        this.hakukohdeOid = hakukohdeOid;
        this.tila = tila;
        this.muokkaaja = muokkaaja;
        this.selite = selite;
    }
}
