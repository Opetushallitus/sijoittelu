package fi.vm.sade.sijoittelu.tulos.dao.impl;

import com.google.code.morphia.Datastore;
import com.google.code.morphia.query.Query;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.tulos.dao.DAO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.LinkedHashSet;
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

    /*
    @Override
    public List<Hakukohde> getHakukohteetForSijoitteluajo(Long sijoitteluAjoId) {
        Query<Hakukohde> query = morphiaDS.createQuery(Hakukohde.class);
        query.field("sijoitteluajoId").equal(sijoitteluAjoId);
        return query.asList();
    }
    @Override
    public List<Hakukohde> getHakukohteetForSijoitteluajo(Long sijoitteluAjoId, String hakukohdeOid) {
        Query<Hakukohde> query = morphiaDS.createQuery(Hakukohde.class);
        query.field("sijoitteluajoId").equal(sijoitteluAjoId);
        query.field("oid").equal(hakukohdeOid);
        return query.asList();
    }
    */

    @Override
    public List<Valintatulos> loadValintatulokset(String hakuOid) {
        if(StringUtils.isBlank(hakuOid) )    {
            throw new RuntimeException("Invalid search params, fix exception later");
        }
        Query<Valintatulos> q = morphiaDS.createQuery(Valintatulos.class);
        q.criteria("hakuOid").equal(hakuOid);
        return q.asList();
    }


    /*
    @Override
    public List<Valintatulos> loadValintatulokset(String hakuOid, String hakukohdeOid,  List<String> vastaanottotieto) {
        if(StringUtils.isBlank(hakuOid))    {
            throw new RuntimeException("Invalid search params, fix exception later");
        }
        Query<Valintatulos> q = morphiaDS.createQuery(Valintatulos.class);
        q.criteria("hakuOid").equal(hakuOid);
        if(StringUtils.isNotBlank(hakukohdeOid)){
            q.criteria("hakukohdeOid").equal(hakukohdeOid);
        }
        if(vastaanottotieto !=null && vastaanottotieto.size() > 0){
            q.criteria("tila").in(vastaanottotieto);
        }
        return q.asList();
    }
      */
    @Override
    public List<Valintatulos> loadValintatuloksetForHakemus(String hakemusOid) {
        if(StringUtils.isBlank(hakemusOid) )    {
            throw new RuntimeException("Invalid search params, fix exception later");
        }
        Query<Valintatulos> q = morphiaDS.createQuery(Valintatulos.class);
        q.criteria("hakemusOid").equal(hakemusOid);
        return q.asList();
    }


    /**
     * This right here is some fucked up shit, but mongoDB & domain
     * @param sijoitteluajoId
     * @param vastaanottotieto
     * @param tila
     * @param hakukohdeOid
     * @param count
     * @param index
     * @return
     */
    @Override
    public List<String> hakukohteet(Long sijoitteluajoId,
                                    List<String> vastaanottotieto,
                                    List<String> tila,
                                    List<String> hakukohdeOid,
                                    Integer count,
                                    Integer index) {

        Query query = morphiaDS.createQuery(Hakukohde.class);
        query.field("sijoitteluajoId").equal(sijoitteluajoId);

        if(tila!=null && tila.size() > 0) {
            query.field("valintatapajonot.hakemukset.tila").in(tila);
        }
        if(hakukohdeOid!=null && hakukohdeOid.size() > 0) {
            query.field("oid").in(hakukohdeOid);
        }


        query.retrievedFields(true, "valintatapajonot.hakemukset.hakemusOid");
        LinkedHashSet<String> set = new LinkedHashSet<String>();
        set.addAll(query.asList());
        ArrayList<String> list = new ArrayList<String>();
        list.addAll(set);

        if(count != null && index != null) {
            return list.subList(index, index+count);
        }   else if(count!=null) {
            return list.subList(0, count);
        }
        return list;

    }


}
