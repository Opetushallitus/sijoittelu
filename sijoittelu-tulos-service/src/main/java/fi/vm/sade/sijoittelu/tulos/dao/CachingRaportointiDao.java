package fi.vm.sade.sijoittelu.tulos.dao;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Valintatulos;

import java.util.List;
import java.util.Optional;

public interface CachingRaportointiDao {
    Optional<List<Hakukohde>> getCachedHakukohdesForSijoitteluajo(Long sijoitteluAjoId);
    Optional<List<Valintatulos>> getCachedValintatulokset(String hakuOid);
}
