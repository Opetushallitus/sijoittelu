package fi.vm.sade.sijoittelu.laskenta.dao;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.Valintatulos;

/**
 * 
 * @author Kari Kammonen
 * 
 */
public interface Dao {

    void persistSijoittelu(Sijoittelu sijoittelu);

    void persistSijoitteluAjo(SijoitteluAjo sijoitteluAjo);

    Sijoittelu loadSijoittelu(String hakuOid);

    SijoitteluAjo loadSijoitteluajo(Long ajoId);

    Valintatulos loadValintatuloksenTila(String hakukohdeOid, String valintatapajonoOid, String hakemusOid);

    void createOrUpdateValintatulos(Valintatulos tulos);

    void persistHakukohde(Hakukohde hakukohde);

    Hakukohde getHakukohdeForSijoitteluajo(Long ajoId, String hakukohdeOid);
}
