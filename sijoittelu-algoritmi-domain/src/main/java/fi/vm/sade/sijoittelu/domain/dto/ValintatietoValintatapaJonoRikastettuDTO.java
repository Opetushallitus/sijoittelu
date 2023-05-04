package fi.vm.sade.sijoittelu.domain.dto;

import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.ValintatietoValintatapajonoDTO;
import org.modelmapper.ModelMapper;

public class ValintatietoValintatapaJonoRikastettuDTO extends ValintatietoValintatapajonoDTO {

    private static ModelMapper mapper = new ModelMapper();

    private Boolean merkitseMyohAuto;

    public Boolean getMerkitseMyohAuto() {
        return merkitseMyohAuto;
    }

    public void setMerkitseMyohAuto(Boolean merkitseMyohAuto) {
        this.merkitseMyohAuto = merkitseMyohAuto;
    }

    public static ValintatietoValintatapaJonoRikastettuDTO convert(ValintatietoValintatapajonoDTO jono) {
        return mapper.map(jono, ValintatietoValintatapaJonoRikastettuDTO.class);
    }
}
