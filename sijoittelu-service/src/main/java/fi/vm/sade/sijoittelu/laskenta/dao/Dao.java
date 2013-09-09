package fi.vm.sade.sijoittelu.laskenta.dao;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.Valintatulos;

import java.util.List;

/**
 * 
 * @author Kari Kammonen
 * 
 */
public interface Dao {

    void persistSijoittelu(Sijoittelu sijoittelu);

    Sijoittelu loadSijoittelu(String hakuOid);

    Valintatulos loadValintatulos(String hakukohdeOid, String valintatapajonoOid, String hakemusOid);

    void createOrUpdateValintatulos(Valintatulos tulos);

    void persistHakukohde(Hakukohde hakukohde);

    Hakukohde getHakukohdeForSijoitteluajo(Long ajoId, String hakukohdeOid);

    List<Valintatulos> loadValintatulokset(String hakuOid);

    List<Hakukohde> getHakukohdeForSijoitteluajo(Long sijoitteluajoId);

    List<Valintatulos> loadValintatulos(String oid);

    List<Valintatulos> loadValintatulokset(String hakukohdeOid, String valintatapajonoOid);
}
