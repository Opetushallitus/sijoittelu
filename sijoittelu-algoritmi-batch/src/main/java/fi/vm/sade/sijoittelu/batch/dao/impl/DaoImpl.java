package fi.vm.sade.sijoittelu.batch.dao.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;

import fi.vm.sade.sijoittelu.batch.dao.Dao;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.HakukohdeItem;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;

/**
 * 
 * @author Kari Kammonen
 * 
 */
@Repository
public class DaoImpl implements Dao {

    @Autowired
    private Datastore morphiaDS;

    @Override
    public void persistSijoitteluAjo(SijoitteluAjo sijoitteluajo) {

        for (HakukohdeItem hki : sijoitteluajo.getHakukohteet()) {
            Hakukohde hk = hki.getHakukohde();
            morphiaDS.save(hk);
        }
        morphiaDS.save(sijoitteluajo);
    }

    @Override
    public SijoitteluAjo loadSijoitteluajo(Long ajoId) {
        Query<SijoitteluAjo> q = morphiaDS.createQuery(SijoitteluAjo.class);
        q.criteria("sijoitteluajoId").equal(ajoId);
        return q.get();
    }

    @Override
    public void persistSijoittelu(Sijoittelu sijoittelu) {
        morphiaDS.save(sijoittelu);

    }

    @Override
    public Sijoittelu loadSijoittelu(String hakuOid) {
        Query<Sijoittelu> q = morphiaDS.createQuery(Sijoittelu.class);
        q.criteria("hakuOid").equal(hakuOid);
        return q.get();
    }

}
