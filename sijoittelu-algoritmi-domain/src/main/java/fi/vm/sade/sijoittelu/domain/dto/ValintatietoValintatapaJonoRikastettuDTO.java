package fi.vm.sade.sijoittelu.domain.dto;

import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.ValintatietoValintatapajonoDTO;

public class ValintatietoValintatapaJonoRikastettuDTO extends ValintatietoValintatapajonoDTO {

    private Boolean merkitseMyohAuto;

    public Boolean getMerkitseMyohAuto() {
        return merkitseMyohAuto;
    }

    public void setMerkitseMyohAuto(Boolean merkitseMyohAuto) {
        this.merkitseMyohAuto = merkitseMyohAuto;
    }
}
