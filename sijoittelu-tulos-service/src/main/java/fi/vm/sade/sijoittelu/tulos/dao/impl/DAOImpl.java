package fi.vm.sade.sijoittelu.tulos.dao.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.tulos.dao.DAO;

/**
 * User: tommiha Date: 10/15/12 Time: 2:44 PM
 */
// Siirretty itse busineksen puolelle, liikaa duplikaatteja metodeja.
// poista luokka kun siirto suoritettu luoppuun
@Repository
@Deprecated()
public class DAOImpl implements DAO {
    @Autowired
    private Datastore morphiaDS;

    @Override
    public List<Sijoittelu> getSijoittelu() {
        Query<Sijoittelu> query = morphiaDS.createQuery(Sijoittelu.class);
        return query.asList();
    }

    @Override
    public Sijoittelu getSijoitteluByHakuOid(String hakuOid) {
        Query<Sijoittelu> query = morphiaDS.createQuery(Sijoittelu.class);
        query.field("hakuOid").equal(hakuOid);
        return query.get();
    }

    @Override
    public SijoitteluAjo getSijoitteluajo(Long sijoitteluajoId) {
        Query<Sijoittelu> query = morphiaDS.createQuery(Sijoittelu.class);
        query.field("sijoitteluajot.sijoitteluajoId").equal(sijoitteluajoId);
        Sijoittelu a = query.get();
        for (SijoitteluAjo sa : a.getSijoitteluajot()) {
            if (sijoitteluajoId.equals(sa.getSijoitteluajoId())) {
                return sa;
            }
        }
        return null;
    }

    @Override
    public Hakukohde getHakukohdeBySijoitteluajo(Long sijoitteluajoId, String hakukohdeOid) {
        Query<Hakukohde> query = morphiaDS.createQuery(Hakukohde.class);
        query.field("sijoitteluajoId").equal(sijoitteluajoId);
        query.field("oid").equal(hakukohdeOid);
        return query.get();
    }

    @Override
    public List<Hakukohde> haeHakukohteetJoihinHakemusOsallistuu(Long sijoitteluajoId, String hakemusOid) {
        Query<Hakukohde> query = morphiaDS.createQuery(Hakukohde.class);
        query.field("sijoitteluajoId").equal(sijoitteluajoId);
        query.field("valintatapajonot.hakemukset.hakemusOid").equal(hakemusOid);
        List<Hakukohde> list = query.asList();
        return list;
    }

    @Override
    public SijoitteluAjo getLatestSijoitteluajo(String hakuOid) {
        Query<Sijoittelu> query = morphiaDS.createQuery(Sijoittelu.class);
        query.field("hakuOid").equal(hakuOid);
        Sijoittelu a = query.get();
        return a.getLatestSijoitteluajo(); // this is shit, refactor
    }

    @Override
    public Hakukohde getLatestHakukohdeBySijoitteluajo(String hakuOid, String hakukohdeOid) {

        SijoitteluAjo sa = getLatestSijoitteluajo(hakuOid); // refactor this
                                                            // shit also

        Query<Hakukohde> query = morphiaDS.createQuery(Hakukohde.class);
        query.field("oid").equal(hakukohdeOid);
        query.field("sijoitteluajoId").equal(sa.getSijoitteluajoId());
        return query.get();
    }

    @Override
    public List<Hakukohde> haeLatestHakukohteetJoihinHakemusOsallistuu(String hakuOid, String hakemusOid) {

        SijoitteluAjo sa = getLatestSijoitteluajo(hakuOid); // refactor this
                                                            // shit also

        Query<Hakukohde> query = morphiaDS.createQuery(Hakukohde.class);
        query.field("sijoitteluajoId").equal(sa.getSijoitteluajoId());
        query.field("valintatapajonot.hakemukset.hakemusOid").equal(hakemusOid);
        List<Hakukohde> list = query.asList();
        return list;

    }

    /*
     * private <T> T getSingleEntity(Class<T> clazz, String fieldName, Object
     * fieldValue) { Query<T> query = morphiaDS.createQuery(clazz); List<T>
     * result = query.field(fieldName).equal(fieldValue).asList(); if
     * (result.size() == 0) { throw new
     * SijoitteluEntityNotFoundException("Entity " + clazz.getSimpleName() +
     * " with " + fieldName + "==" + fieldValue + " does not exist"); } else if
     * (result.size() > 1) { throw new
     * MultipleSijoitteluEntitiesFoundException("Expected single result but found "
     * + result.size() + " results for " + clazz.getSimpleName() + " with " +
     * fieldName + "==" + fieldValue); }
     * 
     * return result.get(0); }
     * 
     * @Override public List<Sijoittelu> getHakus(HaeHautKriteeritTyyppi
     * haeHautKriteerit) { Query<Sijoittelu> query =
     * morphiaDS.createQuery(Sijoittelu.class); if (haeHautKriteerit != null) {
     * if (haeHautKriteerit.getHakuOidLista() != null &&
     * !haeHautKriteerit.getHakuOidLista().isEmpty()) {
     * query.field("hakuOid").hasAnyOf(haeHautKriteerit.getHakuOidLista()); } }
     * 
     * return query.asList(); }
     * 
     * @Override public Sijoittelu getSijoitteluByHakuOid(String hakuOid) {
     * Query<Sijoittelu> query = morphiaDS.createQuery(Sijoittelu.class);
     * query.field("hakuOid").equal(hakuOid); return query.get(); }
     * 
     * private Query<SijoitteluAjo> createSijoitteluajoByHakuOidQuery(String
     * hakuOid) { Query<Sijoittelu> query =
     * morphiaDS.createQuery(Sijoittelu.class);
     * query.field("hakuOid").equal(hakuOid); query.retrievedFields(true,
     * "sijoitteluajot");
     * 
     * Sijoittelu sijoittelu = query.get();
     * 
     * if (sijoittelu == null) { throw new
     * SijoitteluEntityNotFoundException("No sijoittelu found with haku OID " +
     * hakuOid); }
     * 
     * List<ObjectId> saIds = new ArrayList<ObjectId>(); for (SijoitteluAjo ajo
     * : sijoittelu.getSijoitteluajot()) { saIds.add(ajo.getId()); }
     * 
     * Query<SijoitteluAjo> saquery =
     * morphiaDS.createQuery(SijoitteluAjo.class);
     * saquery.field("id").in(saIds); return saquery; }
     * 
     * 
     * @Override public List<SijoitteluAjo> getSijoitteluajoByHakuOid(String
     * hakuOid) { return createSijoitteluajoByHakuOidQuery(hakuOid).asList(); }
     * 
     * @Override public SijoitteluAjo getLatestSijoitteluajoByHakuOid(String
     * hakuOid) { Query<SijoitteluAjo> query =
     * createSijoitteluajoByHakuOidQuery(hakuOid); query.order("-endMils");
     * query.limit(1); return query.get(); }
     * 
     * @Override public SijoitteluAjo
     * getSijoitteluajoByHakuOidAndTimestamp(String hakuOid, Long timestamp) {
     * Query<SijoitteluAjo> query = createSijoitteluajoByHakuOidQuery(hakuOid);
     * query.field("startMils").lessThanOrEq(timestamp);
     * query.order("-endMils"); query.limit(1); return query.get(); }
     * 
     * 
     * @Override public Hakukohde getHakukohdeBySijoitteluajo(Long
     * sijoitteluajoId, String hakukohdeOid) { SijoitteluAjo sijoitteluajo =
     * getSijoitteluajo(sijoitteluajoId);
     * 
     * Query<Hakukohde> query = morphiaDS.createQuery(Hakukohde.class);
     * 
     * List<Object> keys = new ArrayList<Object>(); for (HakukohdeItem hki :
     * sijoitteluajo.getHakukohteet()) {
     * keys.add(morphiaDS.getKey(hki.getHakukohde()).getId()); }
     * 
     * query.field("id").hasAnyOf(keys); query.field("oid").equal(hakukohdeOid);
     * return query.get(); }
     * 
     * @Override public List<SijoitteluAjo>
     * getSijoitteluajos(HaeSijoitteluajotKriteeritTyyppi
     * haeSijoitteluajotKriteerit) { Query<SijoitteluAjo> query =
     * morphiaDS.createQuery(SijoitteluAjo.class); query.retrievedFields(true,
     * "sijoitteluajoId", "startMils", "endMils"); if
     * (haeSijoitteluajotKriteerit != null) { if
     * (haeSijoitteluajotKriteerit.getSijoitteluIdLista() != null &&
     * !haeSijoitteluajotKriteerit.getSijoitteluIdLista().isEmpty()) {
     * query.field
     * ("sijoitteluajoId").hasAnyOf(haeSijoitteluajotKriteerit.getSijoitteluIdLista
     * ()); } }
     * 
     * return query.asList(); }
     * 
     * @Override public SijoitteluAjo getSijoitteluajo(long sijoitteluajoId) {
     * return getSingleEntity(SijoitteluAjo.class, "sijoitteluajoId",
     * sijoitteluajoId); }
     * 
     * @Override public List<Hakukohde> getHakukohdes(long sijoitteluajoId,
     * HaeHakukohteetKriteeritTyyppi haeHakukohteetKriteerit) { SijoitteluAjo
     * sijoitteluajo = getSijoitteluajo(sijoitteluajoId);
     * 
     * Query<Hakukohde> query = morphiaDS.createQuery(Hakukohde.class);
     * 
     * List<Object> keys = new ArrayList<Object>(); for (HakukohdeItem hki :
     * sijoitteluajo.getHakukohteet()) {
     * keys.add(morphiaDS.getKey(hki.getHakukohde()).getId()); }
     * 
     * query.field("id").hasAnyOf(keys); if (haeHakukohteetKriteerit != null &&
     * haeHakukohteetKriteerit.getHakukohdeOidLista() != null &&
     * !haeHakukohteetKriteerit.getHakukohdeOidLista().isEmpty()) {
     * query.field("oid"
     * ).hasAnyOf(haeHakukohteetKriteerit.getHakukohdeOidLista());
     * 
     * }
     * 
     * return query.asList(); }
     */
}
