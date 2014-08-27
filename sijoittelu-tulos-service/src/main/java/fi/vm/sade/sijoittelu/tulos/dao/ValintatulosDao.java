package fi.vm.sade.sijoittelu.tulos.dao;

import java.util.List;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.Valintatulos;

/**
 * User: tommiha Date: 10/15/12 Time: 2:44 PM
 */
public interface ValintatulosDao {
    Valintatulos loadValintatulos(String hakukohdeOid, String valintatapajonoOid, String hakemusOid);

    void createOrUpdateValintatulos(Valintatulos tulos);

    List<Valintatulos> loadValintatulokset(String hakuOid);

    List<Valintatulos> loadValintatuloksetForHakukohde(String hakukohdeOid);

    List<Valintatulos> loadValintatulos(String oid);

    List<Valintatulos> loadValintatulokset(String hakukohdeOid, String valintatapajonoOid);

    List<Valintatulos> loadValintatuloksetForHakemus(String hakemusOid);
}
