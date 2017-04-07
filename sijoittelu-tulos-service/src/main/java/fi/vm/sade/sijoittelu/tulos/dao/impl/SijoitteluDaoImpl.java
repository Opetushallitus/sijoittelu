package fi.vm.sade.sijoittelu.tulos.dao.impl;

import com.google.common.collect.ImmutableList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.tulos.dao.CachingRaportointiDao;
import fi.vm.sade.sijoittelu.tulos.dao.SijoitteluDao;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.mapping.cache.DefaultEntityCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Deprecated
@Repository
public class SijoitteluDaoImpl implements SijoitteluDao {
    private final Logger LOG = LoggerFactory.getLogger(SijoitteluDaoImpl.class);

    @Qualifier("datastore")
    @Autowired
    private Datastore morphiaDS;

    @Autowired
    private CachingRaportointiDao cachingRaportointiDao;

    @Override
    public Optional<SijoitteluAjo> getLatestSijoitteluajo(String hakuOid) {
        DBObject match = BasicDBObjectBuilder.start().push("$match").add("hakuOid", hakuOid).get();
        DBObject unwind = BasicDBObjectBuilder.start("$unwind", "$sijoitteluajot").get();
        DBObject endMilsProject = BasicDBObjectBuilder.start().push("$project")
                .add("sijoitteluajo", "$sijoitteluajot")
                .push("endMils").add("$ifNull", ImmutableList.of("$sijoitteluajot.endMils", -1)).get();
        DBObject sort = BasicDBObjectBuilder.start().push("$sort").add("endMils", -1).get();
        DBObject limit = BasicDBObjectBuilder.start("$limit", 1).get();
        DBObject sijoitteluajoIdProject = BasicDBObjectBuilder.start().push("$project")
                .add("_id", 0)
                .add("sijoitteluajo", 1).get();

        List<DBObject> sijoitteluPipelineSteps = new ArrayList<>(Arrays.asList(match, unwind, endMilsProject, sort, limit, sijoitteluajoIdProject));
        Iterator<DBObject> i = findSijoittelu(sijoitteluPipelineSteps, sijoitteluPipelineSteps);
        if (i.hasNext()) {
            DBObject o = (DBObject) i.next().get("sijoitteluajo");
            return Optional.of(new Mapper().fromDBObject(null, SijoitteluAjo.class, o, new DefaultEntityCache()));
        }
        return Optional.empty();
    }

    private Iterator<DBObject> findSijoittelu(List<DBObject> normaalisijoitteluPipelineSteps, List<DBObject> erillisSijoitteluPipelineSteps) {
        Iterator<DBObject> sijoitteluIterator = morphiaDS.getDB().getCollection("Sijoittelu").aggregate(normaalisijoitteluPipelineSteps).results().iterator();
        if (sijoitteluIterator.hasNext()) {
            return sijoitteluIterator;
        }
        return morphiaDS.getDB().getCollection("ErillisSijoittelu").aggregate(erillisSijoitteluPipelineSteps).results().iterator();
    }

    @Override
    public Optional<SijoitteluAjo> getLatestSijoitteluajo(String hakuOid, String hakukohdeOid) {
        DBObject match = BasicDBObjectBuilder.start().push("$match").add("hakuOid", hakuOid).get();
        DBObject unwind = BasicDBObjectBuilder.start("$unwind", "$sijoitteluajot").get();
        DBObject matchHakukohde = BasicDBObjectBuilder.start().push("$match").add("sijoitteluajot.hakukohteet.oid", hakukohdeOid).get();
        DBObject endMilsProject = BasicDBObjectBuilder.start().push("$project")
                .add("sijoitteluajo", "$sijoitteluajot")
                .push("endMils").add("$ifNull", ImmutableList.of("$sijoitteluajot.endMils", -1)).get();
        DBObject sort = BasicDBObjectBuilder.start().push("$sort").add("endMils", -1).get();
        DBObject limit = BasicDBObjectBuilder.start("$limit", 1).get();
        DBObject sijoitteluajoIdProject = BasicDBObjectBuilder.start().push("$project")
                .add("_id", 0)
                .add("sijoitteluajo", 1).get();

        List<DBObject> normaaliSijoitteluPipelineSteps = new ArrayList<>(Arrays.asList(match, unwind, endMilsProject, sort, limit, sijoitteluajoIdProject));
        List<DBObject> erillisSijoitteluPipelineSteps = new ArrayList<>(Arrays.asList(match, unwind, matchHakukohde, endMilsProject, sort, limit, sijoitteluajoIdProject));
        Iterator<DBObject> i = findSijoittelu(normaaliSijoitteluPipelineSteps, erillisSijoitteluPipelineSteps);

        if (i.hasNext()) {
            DBObject o = (DBObject) i.next().get("sijoitteluajo");
            return Optional.of(new Mapper().fromDBObject(null, SijoitteluAjo.class, o, new DefaultEntityCache()));
        }
        return Optional.empty();
    }

    @Override
    public Optional<SijoitteluAjo> getSijoitteluajo(Long sijoitteluajoId) {
        if (sijoitteluajoId == null) {
            return Optional.empty();
        }

        DBObject o = morphiaDS.getDB().getCollection("Sijoittelu").findOne(
                new BasicDBObject("sijoitteluajot.sijoitteluajoId", sijoitteluajoId),
                BasicDBObjectBuilder.start().push("sijoitteluajot").push("$elemMatch").add("sijoitteluajoId", sijoitteluajoId).get()
        );
        return Optional.of(new Mapper().fromDBObject(null, Sijoittelu.class, o, new DefaultEntityCache()).getSijoitteluajot().get(0));
    }










    @Override
    public Optional<Sijoittelu> getSijoitteluByHakuOid(final String hakuOid) {
        try {
            return Optional.ofNullable(morphiaDS.find(Sijoittelu.class).field("hakuOid").equal(hakuOid).get());
        } catch (Exception e) {
            LOG.debug("Ei saatu sijoittelua haulle" + hakuOid, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Sijoittelu> getSijoitteluById(long id) {
        try {
            return Optional.ofNullable(morphiaDS.find(Sijoittelu.class).field("sijoitteluId").equal(id).get());
        } catch (Exception e) {
            LOG.debug("Ei saatu sijoittelua " + id, e);
            return Optional.empty();
        }
    }

    @Override
    public List<Sijoittelu> findAll() {
        return morphiaDS.find(Sijoittelu.class).asList();
    }

    @Override
    public void persistSijoittelu(Sijoittelu sijoittelu) {
        morphiaDS.save(sijoittelu);
        cachingRaportointiDao.updateLatestAjoCacheWith(sijoittelu);
    }
}
