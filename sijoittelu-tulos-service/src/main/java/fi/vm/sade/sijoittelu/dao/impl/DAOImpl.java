package fi.vm.sade.sijoittelu.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;

import fi.vm.sade.sijoittelu.dao.DAO;
import fi.vm.sade.sijoittelu.dao.exception.MultipleSijoitteluEntitiesFoundException;
import fi.vm.sade.sijoittelu.dao.exception.SijoitteluEntityNotFoundException;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.HakukohdeItem;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.tulos.service.types.HaeHakukohteetKriteeritTyyppi;
import fi.vm.sade.tulos.service.types.HaeHautKriteeritTyyppi;
import fi.vm.sade.tulos.service.types.HaeSijoitteluajotKriteeritTyyppi;

/**
 * User: tommiha Date: 10/15/12 Time: 2:44 PM
 */
@Repository
public class DAOImpl implements DAO {
    @Autowired
    private Datastore morphiaDS;

    private <T> T getSingleEntity(Class<T> clazz, String fieldName, Object fieldValue) {
        Query<T> query = morphiaDS.createQuery(clazz);
        List<T> result = query.field(fieldName).equal(fieldValue).asList();
        if (result.size() == 0) {
            throw new SijoitteluEntityNotFoundException("Entity " + clazz.getSimpleName() + " with " + fieldName + "==" + fieldValue + " does not exist");
        } else if (result.size() > 1) {
            throw new MultipleSijoitteluEntitiesFoundException("Expected single result but found " + result.size() + " results for " + clazz.getSimpleName()
                    + " with " + fieldName + "==" + fieldValue);
        }

        return result.get(0);
    }

    @Override
    public List<Sijoittelu> getHakus(HaeHautKriteeritTyyppi haeHautKriteerit) {
        Query<Sijoittelu> query = morphiaDS.createQuery(Sijoittelu.class);
        if (haeHautKriteerit != null) {
            if (haeHautKriteerit.getHakuOidLista() != null && !haeHautKriteerit.getHakuOidLista().isEmpty()) {
                query.field("haku.oid").hasAnyOf(haeHautKriteerit.getHakuOidLista());
            }
        }

        return query.asList();
    }

    @Override
    public Sijoittelu getSijoitteluByHakuOid(String hakuOid) {
        Query<Sijoittelu> query = morphiaDS.createQuery(Sijoittelu.class);
        query.field("haku.oid").equal(hakuOid);
        return query.get();
    }

    @Override
    public List<SijoitteluAjo> getSijoitteluajos(HaeSijoitteluajotKriteeritTyyppi haeSijoitteluajotKriteerit) {
        Query<SijoitteluAjo> query = morphiaDS.createQuery(SijoitteluAjo.class);
        query.retrievedFields(true, "sijoitteluajoId", "startMils", "endMils");
        if (haeSijoitteluajotKriteerit != null) {
            if (haeSijoitteluajotKriteerit.getSijoitteluIdLista() != null && !haeSijoitteluajotKriteerit.getSijoitteluIdLista().isEmpty()) {
                query.field("sijoitteluajoId").hasAnyOf(haeSijoitteluajotKriteerit.getSijoitteluIdLista());
            }
        }

        return query.asList();
    }

    @Override
    public SijoitteluAjo getSijoitteluajo(long sijoitteluajoId) {
        return getSingleEntity(SijoitteluAjo.class, "sijoitteluajoId", sijoitteluajoId);
    }

    @Override
    public List<Hakukohde> getHakukohdes(long sijoitteluajoId, HaeHakukohteetKriteeritTyyppi haeHakukohteetKriteerit) {
        SijoitteluAjo sijoitteluajo = getSijoitteluajo(sijoitteluajoId);

        Query<Hakukohde> query = morphiaDS.createQuery(Hakukohde.class);

        List<Object> keys = new ArrayList<Object>();
        for (HakukohdeItem hki : sijoitteluajo.getHakukohteet()) {
            keys.add(morphiaDS.getKey(hki.getHakukohde()).getId());
        }

        query.field("id").hasAnyOf(keys);
        if (haeHakukohteetKriteerit != null && haeHakukohteetKriteerit.getHakukohdeOidLista() != null
                && !haeHakukohteetKriteerit.getHakukohdeOidLista().isEmpty()) {
            query.field("oid").hasAnyOf(haeHakukohteetKriteerit.getHakukohdeOidLista());

        }

        return query.asList();
    }
}
