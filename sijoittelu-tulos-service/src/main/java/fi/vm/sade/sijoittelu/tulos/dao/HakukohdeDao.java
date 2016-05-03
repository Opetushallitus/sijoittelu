package fi.vm.sade.sijoittelu.tulos.dao;

import java.util.Iterator;
import java.util.List;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.tulos.dto.KevytHakukohdeDTO;

public interface HakukohdeDao {
    List<Hakukohde> getHakukohdeForSijoitteluajo(Long sijoitteluajoId);

    Iterator<KevytHakukohdeDTO> getHakukohdeForSijoitteluajoIterator(Long sijoitteluajoId, String hakukohdeOid);

    void persistHakukohde(Hakukohde hakukohde);

    List<Hakukohde> findAll();

    void removeHakukohde(Hakukohde hakukohde);

    Hakukohde getHakukohdeForSijoitteluajo(Long ajoId, String hakukohdeOid);

    List<Hakukohde> haeHakukohteetJoihinHakemusOsallistuu(Long sijoitteluajoId, String hakemusOid);
}
