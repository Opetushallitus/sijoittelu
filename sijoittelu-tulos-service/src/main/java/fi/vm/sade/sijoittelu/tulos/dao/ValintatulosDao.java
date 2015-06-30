package fi.vm.sade.sijoittelu.tulos.dao;

import java.util.List;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Valintatulos;

public interface ValintatulosDao {
    Valintatulos loadValintatulos(String hakukohdeOid, String valintatapajonoOid, String hakemusOid);

    void createOrUpdateValintatulos(Valintatulos tulos);

    List<Valintatulos> mergaaValintatulos(List<Hakukohde> kaikkiHakukohteet, List<Valintatulos> sijoittelunTulokset);

    List<Valintatulos> loadValintatulokset(String hakuOid);

    List<Valintatulos> loadValintatuloksetForHakukohde(String hakukohdeOid);

    List<Valintatulos> loadValintatulos(String oid);

    List<Valintatulos> loadValintatulokset(String hakukohdeOid, String valintatapajonoOid);

    List<Valintatulos> loadValintatuloksetForHakemus(String hakemusOid);

    void remove(Valintatulos valintatulos);
}
