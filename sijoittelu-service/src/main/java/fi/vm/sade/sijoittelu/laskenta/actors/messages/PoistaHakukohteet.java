package fi.vm.sade.sijoittelu.laskenta.actors.messages;

import fi.vm.sade.sijoittelu.domain.Sijoittelu;

public class PoistaHakukohteet {
    private final Sijoittelu sijoittelu;

    private final Long ajoId;

    public PoistaHakukohteet(Sijoittelu sijoittelu, Long ajoId) {
        this.sijoittelu = sijoittelu;
        this.ajoId = ajoId;
    }

    public Sijoittelu getSijoittelu() {
        return sijoittelu;
    }

    public Long getAjoId() {
        return ajoId;
    }
}
