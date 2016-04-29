package fi.vm.sade.sijoittelu.tulos.service;

import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaPaginationObject;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.KevytHakijaDTO;

import java.util.List;
import java.util.Optional;

public interface RaportointiService {
    Optional<SijoitteluAjo> getSijoitteluAjo(Long SijoitteluajoId);

    Optional<SijoitteluAjo> latestSijoitteluAjoForHaku(String hakuOid);

    HakijaDTO hakemus(SijoitteluAjo sijoitteluAjo, String hakemusOid);

    HakijaPaginationObject hakemukset(SijoitteluAjo ajo,Boolean hyvaksytyt, Boolean ilmanHyvaksyntaa,Boolean vastaanottaneet, List <String> hakukohdeOid, Integer count, Integer index);

    List<KevytHakijaDTO> hakemukset(SijoitteluAjo ajo, String hakukohdeOid);

    HakijaPaginationObject cachedHakemukset(SijoitteluAjo ajo, Boolean hyvaksytyt, Boolean ilmanHyvaksyntaa, Boolean vastaanottaneet, List<String> hakukohdeOid, Integer count, Integer index);
}
