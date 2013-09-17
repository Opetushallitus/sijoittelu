package fi.vm.sade.sijoittelu.tulos.dao.impl;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.tulos.dao.DAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * User: tommiha Date: 10/15/12 Time: 2:44 PM
 */
@Repository
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
    //TODO REFACTOR
    public SijoitteluAjo getLatestSijoitteluajo(String hakuOid) {
        Query<Sijoittelu> query = morphiaDS.createQuery(Sijoittelu.class);
        query.field("hakuOid").equal(hakuOid);
        Sijoittelu a = query.get();
        SijoitteluAjo ajo = null;
        if(a != null) {
            ajo =  a.getLatestSijoitteluajo();
        }
        return ajo;
    }

    @Override
    //TODO REFACTOR
    public Hakukohde getLatestHakukohdeBySijoitteluajo(String hakuOid, String hakukohdeOid) {

        SijoitteluAjo sa = getLatestSijoitteluajo(hakuOid);

        if(sa == null) {
            return null;
        }

        Query<Hakukohde> query = morphiaDS.createQuery(Hakukohde.class);
        query.field("oid").equal(hakukohdeOid);
        query.field("sijoitteluajoId").equal(sa.getSijoitteluajoId());
        return query.get();
    }

    @Override
    //TODO REFACTOR
    public List<Hakukohde> haeLatestHakukohteetJoihinHakemusOsallistuu(String hakuOid, String hakemusOid) {

        SijoitteluAjo sa = getLatestSijoitteluajo(hakuOid);

        if(sa == null) {
            return null;
        }
        Query<Hakukohde> query = morphiaDS.createQuery(Hakukohde.class);
        query.field("sijoitteluajoId").equal(sa.getSijoitteluajoId());
        query.field("valintatapajonot.hakemukset.hakemusOid").equal(hakemusOid);
        List<Hakukohde> list = query.asList();
        return list;

    }

    @Override
    public List<Hakukohde> getHakukohteetForSijoitteluajo(Long id) {
        Query<Hakukohde> query = morphiaDS.createQuery(Hakukohde.class);
        query.field("sijoitteluajoId").equal(id);
        return query.asList();
    }

}
