package fi.vm.sade.sijoittelu.tulos.dao.impl;

import java.util.List;

import javax.annotation.PostConstruct;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.tulos.dao.HakukohdeDao;

@Repository
public class HakukohdeDaoImpl implements HakukohdeDao {
    @Qualifier("datastore")
    @Autowired
    private Datastore morphiaDS;

    @PostConstruct
    public void ensureIndexes() {
        morphiaDS.ensureIndexes(Hakukohde.class);
    }

    @Override
    public void persistHakukohde(Hakukohde hakukohde) {
        morphiaDS.save(hakukohde);
    }

    @Override
    public Hakukohde getHakukohdeForSijoitteluajo(Long sijoitteluajoId, String hakukohdeOid) {
        Query<Hakukohde> q = morphiaDS.createQuery(Hakukohde.class);
        q.criteria("sijoitteluajoId").equal(sijoitteluajoId);
        q.criteria("oid").equal(hakukohdeOid);
        return q.get();
    }

    @Override
    public List<Hakukohde> getHakukohdeForSijoitteluajo(Long sijoitteluajoId) {
        Query<Hakukohde> q = morphiaDS.createQuery(Hakukohde.class);
        q.criteria("sijoitteluajoId").equal(sijoitteluajoId);
        return q.asList();
    }

    @Override
    public List<Hakukohde> haeHakukohteetJoihinHakemusOsallistuu(
        Long sijoitteluajoId, String hakemusOid) {
        Query<Hakukohde> query = morphiaDS.createQuery(Hakukohde.class);
        query.field("sijoitteluajoId").equal(sijoitteluajoId);
        query.field("valintatapajonot.hakemukset.hakemusOid").equal(hakemusOid);
        List<Hakukohde> list = query.asList();
        return list;
    }
}
