package fi.vm.sade.sijoittelu.tulos.dao.impl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.MapreduceType;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.mapping.cache.DefaultEntityCache;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Repository;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MapReduceCommand;
import com.mongodb.MapReduceOutput;

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
    public List<Hakukohde> haeHakukohteetJoihinHakemusOsallistuu(Long sijoitteluajoId, String hakemusOid) {
        final BasicDBObject query = new BasicDBObject("sijoitteluajoId", sijoitteluajoId).append("valintatapajonot.hakemukset.hakemusOid", hakemusOid);

        String map = "function() { emit(this.oid, {className:this.className, sijoitteluajoId: this.sijoitteluajoId, oid:this.oid, tarjoajaOid: this.tarjoajaOid, kaikkiJonotSijoiteltu: this.kaikkiJonotSijoiteltu, valintatapajonot: this.valintatapajonot.map(function(jono) { return {tasasijasaanto: jono.tasasijasaanto, oid:jono.oid, nimi: jono.nimi, prioriteetti: jono.prioriteetti, aloituspaikat:jono.aloituspaikat, eiVarasijatayttoa:jono.eiVarasijatayttoa, kaikkiEhdonTayttavatHyvaksytaan: jono.kaikkiEhdonTayttavatHyvaksytaan, poissaOlevaTaytto:jono.poissaOlevaTaytto, varasijat:jono.varasijat, varasijaTayttoPaivat: jono.varasijaTayttoPaivat, hyvaksytty: jono.hyvaksytty, varalla: jono.varalla, alinHyvaksyttyPistemaara: jono.alinHyvaksyttyPistemaara, hakemukset:jono.hakemukset.filter(function(hakemus) { return hakemus.hakemusOid == '"+hakemusOid+"' })}})})}";
        String reduce = "function(key, values) { return values[0] }";

        DBCollection collection = morphiaDS.getCollection(Hakukohde.class);
        MapReduceCommand cmd = new MapReduceCommand(
            collection,
            map,
            reduce,
            null,
            MapReduceCommand.OutputType.INLINE,
            query);
        MapReduceOutput out = collection.mapReduce(cmd);

        return StreamSupport.stream(out.results().spliterator(), false).map(dbObject -> {
            return new Mapper().fromDBObject(Hakukohde.class, (DBObject) dbObject.get("value"), new DefaultEntityCache());
        }).collect(Collectors.toList());
    }
}
