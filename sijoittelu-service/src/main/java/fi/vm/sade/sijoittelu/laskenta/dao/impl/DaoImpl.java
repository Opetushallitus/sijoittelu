package fi.vm.sade.sijoittelu.laskenta.dao.impl;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.sijoittelu.laskenta.dao.Dao;
import fi.vm.sade.sijoittelu.tulos.dao.exception.SijoitteluEntityNotFoundException;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

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

        /*
        for (HakukohdeItem hki : sijoitteluajo.getHakukohteet()) {
            Hakukohde hk = hki.getHakukohde();
            morphiaDS.save(hk);
        } */
        morphiaDS.save(sijoitteluajo);
    }

    @Override
    public SijoitteluAjo loadSijoitteluajo(Long ajoId) {
        Query<SijoitteluAjo> q = morphiaDS.createQuery(SijoitteluAjo.class);
        q.criteria("sijoitteluajoId").equal(ajoId);
        return q.get();
    }

    @Override
    public Valintatulos loadValintatuloksenTila(String hakukohdeOid, String valintatapajonoOid, String hakemusOid) {
        if(StringUtils.isBlank(hakukohdeOid) || StringUtils.isBlank(hakukohdeOid) || StringUtils.isBlank(hakemusOid))    {
            throw new RuntimeException("Invalid searhch params, fix exception later");
        }

        Query<Valintatulos> q = morphiaDS.createQuery(Valintatulos.class);
        q.criteria("hakukohdeOid").equal(hakukohdeOid);
        q.criteria("valintatapajonoOid").equal(valintatapajonoOid);
        q.criteria("hakemusOid").equal(hakemusOid);
        return q.get();
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
    public void createOrUpdateValintatulos(Valintatulos tulos) {
        morphiaDS.save(tulos);
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
