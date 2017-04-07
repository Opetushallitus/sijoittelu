package fi.vm.sade.sijoittelu.tulos.dao;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.Valintatulos;

import java.util.List;
import java.util.Optional;

@Deprecated
public interface CachingRaportointiDao {
    Optional<List<Hakukohde>> getCachedHakukohdesForSijoitteluajo(Long sijoitteluAjoId);
    Optional<List<Valintatulos>> getCachedValintatulokset(String hakuOid);
    Optional<SijoitteluAjo> getCachedLatestSijoitteluAjo(String hakuOid);
    Optional<SijoitteluAjo> getCachedLatestSijoitteluAjo(String hakuOid, String hakukohdeOid);
    void updateLatestAjoCacheWith(Sijoittelu sijoittelu);

    List<Hakukohde> getCachedHakukohteetJoihinHakemusOsallistuu(String hakuOid, long sijoitteluajoId, String hakemusOid);
    Hakukohde getCachedHakukohde(SijoitteluAjo sijoitteluAjo, String hakukohdeOid);
    void updateHakukohdeCacheWith(Hakukohde hakukohde, String hakuOid);
}
