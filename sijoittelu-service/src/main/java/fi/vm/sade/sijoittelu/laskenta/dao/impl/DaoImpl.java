package fi.vm.sade.sijoittelu.laskenta.dao.impl;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.laskenta.dao.Dao;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

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
    public void persistSijoittelu(Sijoittelu sijoittelu) {
        morphiaDS.save(sijoittelu);

    }
    @Override
    public void persistHakukohde(Hakukohde hakukohde) {
        morphiaDS.save(hakukohde);
    }

    @Override
    public Sijoittelu loadSijoittelu(String hakuOid) {
        Query<Sijoittelu> q = morphiaDS.createQuery(Sijoittelu.class);
        q.criteria("hakuOid").equal(hakuOid);
        return q.get();
    }

    @Override
    public List<Valintatulos> loadValintatulos(String hakemusOid) {
        if(StringUtils.isBlank(hakemusOid))    {
            throw new RuntimeException("Invalid search params, fix exception later");
        }
        Query<Valintatulos> q = morphiaDS.createQuery(Valintatulos.class);
        q.criteria("hakemusOid").equal(hakemusOid);
        return q.asList();
    }

    @Override
    public Valintatulos loadValintatulos(String hakukohdeOid, String valintatapajonoOid, String hakemusOid) {
        if(StringUtils.isBlank(hakukohdeOid) || StringUtils.isBlank(hakukohdeOid) || StringUtils.isBlank(hakemusOid))    {
            throw new RuntimeException("Invalid search params, fix exception later");
        }
        Query<Valintatulos> q = morphiaDS.createQuery(Valintatulos.class);
        q.criteria("hakukohdeOid").equal(hakukohdeOid);
        q.criteria("valintatapajonoOid").equal(valintatapajonoOid);
        q.criteria("hakemusOid").equal(hakemusOid);
        return q.get();
    }

    @Override
    public List<Valintatulos> loadValintatulokset(String hakuOid) {
        if(StringUtils.isBlank(hakuOid) )    {
            throw new RuntimeException("Invalid search params, fix exception later");
        }
        Query<Valintatulos> q = morphiaDS.createQuery(Valintatulos.class);
        q.criteria("hakuOid").equal(hakuOid);
        return q.asList();
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
    public void createOrUpdateValintatulos(Valintatulos tulos) {
        morphiaDS.save(tulos);
    }

}
