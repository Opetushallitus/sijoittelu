package fi.vm.sade.sijoittelu.tulos.dao.impl;

import com.google.common.collect.ImmutableList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.tulos.dao.SijoitteluDao;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.mapping.cache.DefaultEntityCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Repository
public class SijoitteluDaoImpl implements SijoitteluDao {
    private final Logger LOG = LoggerFactory.getLogger(SijoitteluDaoImpl.class);

    @Qualifier("datastore")
    @Autowired
    private Datastore morphiaDS;

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

        Iterator<DBObject> i = morphiaDS.getDB().getCollection("Sijoittelu").aggregate(ImmutableList.of(
                match, unwind, endMilsProject, sort, limit, sijoitteluajoIdProject
        )).results().iterator();

        if (i.hasNext()) {
            DBObject o = (DBObject) i.next().get("sijoitteluajo");
            return Optional.of(new Mapper().fromDBObject(SijoitteluAjo.class, o, new DefaultEntityCache()));
        }
        return Optional.empty();
    }

    @Override
    public Optional<SijoitteluAjo> getSijoitteluajo(Long sijoitteluajoId) {
        if (sijoitteluajoId == null) {
            return Optional.empty();
        }

        Optional<Sijoittelu> a = Optional.ofNullable(morphiaDS
                .find(Sijoittelu.class).field("sijoitteluajot.sijoitteluajoId")
                .equal(sijoitteluajoId).get());

        if (a.isPresent()) {
            Optional<SijoitteluAjo> sa = a
                    .get()
                    .getSijoitteluajot()
                    .parallelStream()
                    .filter(ajo -> sijoitteluajoId.equals(ajo
                            .getSijoitteluajoId())).findFirst();
            if (sa.isPresent()) {
                return sa;
            }
        }

        return Optional.empty();
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
    }
}
