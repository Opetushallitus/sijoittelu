package fi.vm.sade.sijoittelu.tulos.dao.impl;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.mongodb.*;
import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.sijoittelu.tulos.dao.CachingRaportointiDao;
import fi.vm.sade.sijoittelu.tulos.dto.KevytHakemusDTO;
import fi.vm.sade.sijoittelu.tulos.dto.KevytHakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.dto.KevytValintatapajonoDTO;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.mapping.cache.DefaultEntityCache;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import fi.vm.sade.sijoittelu.tulos.dao.HakukohdeDao;
import fi.vm.sade.sijoittelu.tulos.dao.util.MongoMapReduceUtil;

@Repository
public class HakukohdeDaoImpl implements HakukohdeDao {
    @Qualifier("datastore")
    @Autowired
    private Datastore morphiaDS;

    @Autowired
    private CachingRaportointiDao cachingRaportointiDao;

    @Value("${sijoittelu-service.hakukohdeDao.batchSize}")
    private int batchSize;

    @PostConstruct
    public void ensureIndexes() {
        EnsureIndexes.ensureIndexes(morphiaDS, Hakukohde.class);
    }

    @Override
    public void persistHakukohde(Hakukohde hakukohde, String hakuOid) {
        morphiaDS.save(hakukohde);
        cachingRaportointiDao.updateHakukohdeCacheWith(hakukohde, hakuOid);
    }

    @Override
    public List<Hakukohde> findAll() {
        return morphiaDS.find(Hakukohde.class).asList();
    }

    @Override
    public void removeHakukohde(Hakukohde hakukohde) {
        morphiaDS.delete(hakukohde);
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
    public Iterator<KevytHakukohdeDTO> getHakukohdeForSijoitteluajoIterator(Long sijoitteluajoId, String hakukohdeOid) {
        List<String> hakemusOids = fetchHakukohteenHakemusOids(sijoitteluajoId, hakukohdeOid);
        Map<String, KevytHakukohdeDTO> hakukohteet = fetchKevytHakukohdeDTOsWithoutHakemukset(sijoitteluajoId, hakemusOids);

        return new Iterator<KevytHakukohdeDTO>() {
            private long skip = 0;
            private long fetched = 0;
            private Iterator<DBObject> i = getHakemusIterator(sijoitteluajoId, hakemusOids, this.skip, batchSize);

            @Override
            public boolean hasNext() {
                boolean done = !i.hasNext();
                if (done && this.fetched < batchSize) {
                    return false;
                }
                if (done) {
                    this.skip += batchSize;
                    this.fetched = 0;
                    this.i = getHakemusIterator(sijoitteluajoId, hakemusOids, this.skip, batchSize);
                }
                return i.hasNext();
            }

            @Override
            public KevytHakukohdeDTO next() {
                DBObject o = i.next();
                this.fetched += 1;
                String hakukohdeOid = (String) o.get("_id");
                KevytHakukohdeDTO hk = hakukohteet.get(hakukohdeOid);
                hakukohteet.remove(hakukohdeOid);
                for (DBObject j : (List<DBObject>) o.get("valintatapajonot")) {
                    String jonoOid = (String) j.get("oid");
                    for (KevytValintatapajonoDTO jono : hk.getValintatapajonot()) {
                        if (jono.getOid().equals(jonoOid)) {
                            for (DBObject h : (List<DBObject>) j.get("hakemukset")) {
                                jono.getHakemukset().add(parseKevytHakemusDTO(h));
                            }
                            break;
                        }
                    }
                }
                return hk;
            }
        };
    }

    private List fetchHakukohteenHakemusOids(Long sijoitteluajoId, String hakukohdeOid) {
        return morphiaDS.getDB().getCollection("Hakukohde")
                .distinct("valintatapajonot.hakemukset.hakemusOid",
                        BasicDBObjectBuilder.start("sijoitteluajoId", sijoitteluajoId).add("oid", hakukohdeOid).append("$snapshot", true).get());
    }

    private Map<String, KevytHakukohdeDTO> fetchKevytHakukohdeDTOsWithoutHakemukset(Long sijoitteluajoId, List<String> hakemusOids) {
        DBCursor hakukohdeIterable = morphiaDS.getDB().getCollection("Hakukohde").find(
                BasicDBObjectBuilder.start("sijoitteluajoId", sijoitteluajoId)
                        .push("valintatapajonot.hakemukset.hakemusOid")
                        .add("$in", hakemusOids)
                        .get(),
                BasicDBObjectBuilder
                        .start("_id", 0)
                        .add("sijoitteluajoId", 0)
                        .add("tila", 0)
                        .add("hakijaryhmat", 0)
                        .add("valintatapajonot.tasasijasaanto", 0)
                        .add("valintatapajonot.tila", 0)
                        .add("valintatapajonot.nimi", 0)
                        .add("valintatapajonot.prioriteetti", 0)
                        .add("valintatapajonot.aloituspaikat", 0)
                        .add("valintatapajonot.kaikkiEhdonTayttavatHyvaksytaan", 0)
                        .add("valintatapajonot.poissaOlevaTaytto", 0)
                        .add("valintatapajonot.varasijat", 0)
                        .add("valintatapajonot.varasijaTayttoPaivat", 0)
                        .add("valintatapajonot.tayttojono", 0)
                        .add("valintatapajonot.hyvaksytty", 0)
                        .add("valintatapajonot.varalla", 0)
                        .add("valintatapajonot.alinHyvaksyttyPistemaara", 0)
                        .add("valintatapajonot.hakemukset", 0)
                        .get()
        ).snapshot();
        Map<String, KevytHakukohdeDTO> hakukohteet = new HashMap<>();
        for (DBObject o : hakukohdeIterable) {
            KevytHakukohdeDTO hk = parseKevytHakukohdeDTO(o);
            hakukohteet.put(hk.getOid(), hk);
        }
        return hakukohteet;
    }

    private KevytHakukohdeDTO parseKevytHakukohdeDTO(DBObject o) {
        KevytHakukohdeDTO hk = new KevytHakukohdeDTO();
        hk.setOid((String) o.get("oid"));
        String tila = (String) o.get("tila");
        hk.setTarjoajaOid((String) o.get("tarjoajaOid"));
        if (null != o.get("kaikkiJonotSijoiteltu")) {
            hk.setKaikkiJonotSijoiteltu((boolean) o.get("kaikkiJonotSijoiteltu"));
        }
        List<DBObject> valintatapajonot = (List<DBObject>) o.get("valintatapajonot");
        for (DBObject j : valintatapajonot == null ? Collections.<DBObject>emptyList() : valintatapajonot) {
            hk.getValintatapajonot().add(parseKevytValintatapajonoDTO(j));
        }
        return hk;
    }

    private KevytValintatapajonoDTO parseKevytValintatapajonoDTO(DBObject j) {
        KevytValintatapajonoDTO jono = new KevytValintatapajonoDTO();
        jono.setOid((String) j.get("oid"));
        jono.setEiVarasijatayttoa((Boolean) j.get("eiVarasijatayttoa"));
        jono.setVarasijojaKaytetaanAlkaen((Date) j.get("varasijojaKaytetaanAlkaen"));
        jono.setVarasijojaTaytetaanAsti((Date) j.get("varasijojaTaytetaanAsti"));
        return jono;
    }

    private Iterator<DBObject> getHakemusIterator(Long sijoitteluajoId, List<String> hakemusOids, long skip, long limit) {
        return morphiaDS.getDB().getCollection("Hakukohde")
                .aggregate(ImmutableList.of(
                        BasicDBObjectBuilder.start()
                                .push("$match")
                                .add("sijoitteluajoId", sijoitteluajoId)
                                .push("valintatapajonot.hakemukset.hakemusOid")
                                .add("$in", hakemusOids)
                                .get(),
                        new BasicDBObject("$unwind", "$valintatapajonot"),
                        new BasicDBObject("$unwind", "$valintatapajonot.hakemukset"),
                        BasicDBObjectBuilder.start()
                                .push("$match")
                                .push("valintatapajonot.hakemukset.hakemusOid")
                                .add("$in", hakemusOids)
                                .get(),
                        BasicDBObjectBuilder.start()
                                .push("$group")
                                .push("_id")
                                .add("oid", "$oid")
                                .add("valintatapajonoOid", "$valintatapajonot.oid")
                                .pop()
                                .push("hakemukset")
                                .add("$push", "$valintatapajonot.hakemukset")
                                .get(),
                        BasicDBObjectBuilder.start()
                                .push("$group")
                                .add("_id", "$_id.oid")
                                .push("valintatapajonot")
                                .push("$push")
                                .add("oid", "$_id.valintatapajonoOid")
                                .add("hakemukset", "$hakemukset")
                                .get(),
                        new BasicDBObject("$skip", skip),
                        new BasicDBObject("$limit", limit)
                )).results().iterator();
    }

    private KevytHakemusDTO parseKevytHakemusDTO(DBObject h) {
        KevytHakemusDTO hakemus = new KevytHakemusDTO();
        hakemus.setHakijaOid((String) h.get("hakijaOid"));
        hakemus.setHakemusOid((String) h.get("hakemusOid"));
        hakemus.setPrioriteetti((Integer) h.get("prioriteetti"));
        hakemus.setJonosija((Integer) h.get("jonosija"));
        String pisteet = (String) h.get("pisteet");
        hakemus.setPisteet(Strings.isNullOrEmpty(pisteet) ? null : new BigDecimal(pisteet));
        hakemus.setTila(fi.vm.sade.sijoittelu.tulos.dto.HakemuksenTila.valueOf((String) h.get("tila")));
        List<DBObject> tilaHistoria = (List<DBObject>) h.get("tilaHistoria");
        Date viimeisenMuutos = tilaHistoria == null ? null : (Date) tilaHistoria.get(tilaHistoria.size() - 1).get("luotu");
        hakemus.setViimeisenHakemuksenTilanMuutos(viimeisenMuutos);
        hakemus.setHyvaksyttyHarkinnanvaraisesti((boolean) h.get("hyvaksyttyHarkinnanvaraisesti"));
        DBObject tilanKuvaukset = (DBObject) h.get("tilanKuvaukset");
        for (String k : tilanKuvaukset == null ? Collections.<String>emptySet() : tilanKuvaukset.keySet()) {
            hakemus.getTilanKuvaukset().put(k, (String) tilanKuvaukset.get(k));
        }
        hakemus.setVarasijanNumero((Integer) h.get("varasijanNumero"));
        return hakemus;
    }

    @Override
    public List<Hakukohde> haeHakukohteetJoihinHakemusOsallistuu(Long sijoitteluajoId, String hakemusOid) {
        final BasicDBObject query = new BasicDBObject("sijoitteluajoId", sijoitteluajoId).append("valintatapajonot.hakemukset.hakemusOid", hakemusOid);
        String map = MongoMapReduceUtil.shallowCloneJs + " copy.valintatapajonot = copy.valintatapajonot.map(function(jono) { jono = shallowClone(jono); jono.hakemukset = (jono.hakemukset || []).filter(function (hakemus) { return hakemus.hakemusOid == '"+hakemusOid+"' }); return jono }); emit(this.oid, copy) }\n";
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

        return StreamSupport.stream(out.results().spliterator(), false)
                .map(dbObject -> new Mapper().fromDBObject(Hakukohde.class, (DBObject) dbObject.get("value"), new DefaultEntityCache()))
                .collect(Collectors.toList());
    }

    @Override
    public Boolean isValintapajonoInUse(Long sijoitteluAjoId, String valintatapajonoOid) {
        Query<Hakukohde> q = morphiaDS.createQuery(Hakukohde.class);
        q.criteria("sijoitteluajoId").equal(sijoitteluAjoId);
        q.criteria("valintatapajonot.oid").equal(valintatapajonoOid);
        return q.countAll() > 0;
    }
}
