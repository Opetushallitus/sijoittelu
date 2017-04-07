package fi.vm.sade.sijoittelu.tulos.dao;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.RaportointiValintatulos;

@Deprecated
public interface ValintatulosDao {
    Valintatulos loadValintatulos(String hakukohdeOid, String valintatapajonoOid, String hakemusOid);

    void createOrUpdateValintatulos(Valintatulos tulos);

    List<Valintatulos> mergaaValintatulos(List<Hakukohde> kaikkiHakukohteet, List<Valintatulos> sijoittelunTulokset);

    List<Valintatulos> loadValintatulokset(String hakuOid);

    Iterator<Valintatulos> loadValintatuloksetIterator(String hakuOid);

    List<Valintatulos> loadValintatuloksetForHakukohde(String hakukohdeOid);

    Map<String, List<RaportointiValintatulos>> loadValintatuloksetForHakukohteenHakijat(String hakukohdeOid);

    List<Valintatulos> loadValintatulos(String oid);

    List<Valintatulos> loadValintatulokset(String hakukohdeOid, String valintatapajonoOid);

    List<Valintatulos> loadValintatuloksetForHakemus(String hakemusOid);

    List<Valintatulos> loadValintatuloksetForValintatapajono(String valintatapajonoOid);

    Valintatulos loadValintatulosForValintatapajono(String valintatapajonoOid, String hakemusOid);

    void remove(Valintatulos valintatulos);
}
