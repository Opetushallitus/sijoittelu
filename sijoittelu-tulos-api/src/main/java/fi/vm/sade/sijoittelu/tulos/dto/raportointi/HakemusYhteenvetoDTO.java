package fi.vm.sade.sijoittelu.tulos.dto.raportointi;

import java.util.List;

/**
 *  Hakemuksen sijoittelutilan yhteenveto. Ei hakujonokohtaista tietoa, vaan yhteenveto hakutoiveittain
 */
public class HakemusYhteenvetoDTO {

    public final String hakemusOid;
    public final List<HakutoiveYhteenvetoDTO> hakutoiveet;

    public HakemusYhteenvetoDTO(String hakemusOid, List<HakutoiveYhteenvetoDTO> hakutoiveet) {
        this.hakemusOid = hakemusOid;
        this.hakutoiveet = hakutoiveet;
    }
}
