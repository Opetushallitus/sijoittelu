package fi.vm.sade.sijoittelu.tulos.service;

import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaPaginationObject;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.KevytHakijaDTO;

import java.util.List;
import java.util.Optional;

@Deprecated
public interface RaportointiService {
    Optional<SijoitteluAjo> getSijoitteluAjo(Long SijoitteluajoId);

    HakijaDTO hakemus(String hakuOid, String sijoitteluajoId, String hakemusOid);

    HakijaPaginationObject cachedHakemukset(SijoitteluAjo ajo, Boolean hyvaksytyt, Boolean ilmanHyvaksyntaa, Boolean vastaanottaneet, List<String> hakukohdeOid, Integer count, Integer index);

    Optional<SijoitteluAjo> cachedLatestSijoitteluAjoForHaku(String hakuOid);

    Optional<SijoitteluAjo> cachedLatestSijoitteluAjoForHakukohde(String hakuOid, String hakukohdeOid);

}
