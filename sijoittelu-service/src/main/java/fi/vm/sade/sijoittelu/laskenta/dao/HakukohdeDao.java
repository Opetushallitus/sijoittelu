package fi.vm.sade.sijoittelu.laskenta.dao;

import java.util.List;

import fi.vm.sade.sijoittelu.domain.Hakukohde;

public interface HakukohdeDao {

    List<Hakukohde> getHakukohdeForSijoitteluajo(Long sijoitteluajoId);

    void persistHakukohde(Hakukohde hakukohde);

    Hakukohde getHakukohdeForSijoitteluajo(Long ajoId, String hakukohdeOid);

}
