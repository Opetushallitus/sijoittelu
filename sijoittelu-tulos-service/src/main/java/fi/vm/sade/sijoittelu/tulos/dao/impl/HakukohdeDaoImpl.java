package fi.vm.sade.sijoittelu.tulos.dao.impl;

import java.lang.instrument.Instrumentation;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;

import com.google.common.base.Strings;
import com.mongodb.*;
import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.sijoittelu.tulos.dto.KevytHakemusDTO;
import fi.vm.sade.sijoittelu.tulos.dto.KevytHakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.dto.KevytValintatapajonoDTO;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.mapping.cache.DefaultEntityCache;
import org.mongodb.morphia.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import fi.vm.sade.sijoittelu.tulos.dao.HakukohdeDao;
import fi.vm.sade.sijoittelu.tulos.dao.util.MongoMapReduceUtil;

@Repository
public class HakukohdeDaoImpl implements HakukohdeDao {
    @Qualifier("datastore")
    @Autowired
    private Datastore morphiaDS;

    @PostConstruct
    public void ensureIndexes() {
        EnsureIndexes.ensureIndexes(morphiaDS, Hakukohde.class);
    }

    @Override
    public void persistHakukohde(Hakukohde hakukohde) {
        morphiaDS.save(hakukohde);
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
    public Iterator<KevytHakukohdeDTO> getHakukohdeForSijoitteluajoIterator(Long sijoitteluajoId) {
        AtomicLong count = new AtomicLong(0);
        Iterator<DBObject> i = morphiaDS.getDB().getCollection("Hakukohde")
                .find(
                        new BasicDBObject("sijoitteluajoId", sijoitteluajoId),
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
                                .add("valintatapajonot.hakemukset.etunimi", 0)
                                .add("valintatapajonot.hakemukset.sukunimi", 0)
                                .add("valintatapajonot.hakemukset.onkoMuuttunutViimeSijoittelussa", 0)
                                .add("valintatapajonot.hakemukset.tasasijaJonosija", 0)
                                .add("valintatapajonot.hakemukset.edellinenTila", 0)
                                .add("valintatapajonot.hakemukset.ilmoittautumisTila", 0)
                                .add("valintatapajonot.hakemukset.hyvaksyttyHakijaryhmasta", 0)
                                .add("valintatapajonot.hakemukset.hakijaryhmaOid", 0)
                                .get()
                ).iterator();
        return new Iterator<KevytHakukohdeDTO>() {
            @Override
            public boolean hasNext() {
                return i.hasNext();
            }

            @Override
            public KevytHakukohdeDTO next() {
                DBObject o = i.next();
                KevytHakukohdeDTO hk = new KevytHakukohdeDTO();
                //hk.setId((ObjectId) o.get("_id"));
                //hk.setSijoitteluajoId((Long) o.get("sijoitteluajoId"));
                hk.setOid((String) o.get("oid"));
                String tila = (String) o.get("tila");
                //hk.setTila(tila == null ? null : HakukohdeTila.valueOf(tila));
                hk.setTarjoajaOid((String) o.get("tarjoajaOid"));
                hk.setKaikkiJonotSijoiteltu((boolean) o.get("kaikkiJonotSijoiteltu"));
                List<DBObject> valintatapajonot = (List<DBObject>) o.get("valintatapajonot");
                for (DBObject j : valintatapajonot == null ? Collections.<DBObject>emptyList() : valintatapajonot) {
                    KevytValintatapajonoDTO jono = new KevytValintatapajonoDTO();
                    //jono.setTasasijasaanto(Tasasijasaanto.valueOf((String) j.get("tasasijasaanto")));
                    //String jonoTila = (String) j.get("tila");
                    //jono.setTila(jonoTila == null ? null : ValintatapajonoTila.valueOf(jonoTila));
                    jono.setOid((String) j.get("oid"));
                    //jono.setNimi((String) j.get("nimi"));
                    //jono.setPrioriteetti((Integer) j.get("prioriteetti"));
                    //jono.setAloituspaikat((Integer) j.get("aloituspaikat"));
                    jono.setEiVarasijatayttoa((Boolean) j.get("eiVarasijatayttoa"));
                    //jono.setKaikkiEhdonTayttavatHyvaksytaan((Boolean) j.get("kaikkiEhdonTayttavatHyvaksytaan"));
                    //jono.setPoissaOlevaTaytto((Boolean) j.get("poissaOlevaTaytto"));
                    //jono.setVarasijat((Integer) j.get("varasijat"));
                    //jono.setVarasijaTayttoPaivat((Integer) j.get("varasijaTayttoPaivat"));
                    jono.setVarasijojaKaytetaanAlkaen((Date) j.get("varasijojaKaytetaanAlkaen"));
                    jono.setVarasijojaTaytetaanAsti((Date) j.get("varasijojaTaytetaanAsti"));
                    //jono.setTayttojono((String) j.get("tayttojono"));
                    //jono.setHyvaksytty((Integer) j.get("hyvaksytty"));
                    //jono.setVaralla((Integer) j.get("varalla"));
                    //String alinHyvaksyttyPistemaara = (String) j.get("alinHyvaksyttyPistemaara");
                    //jono.setAlinHyvaksyttyPistemaara(Strings.isNullOrEmpty(alinHyvaksyttyPistemaara) ? null : new BigDecimal(alinHyvaksyttyPistemaara));
                    for (DBObject h : (List<DBObject>) j.get("hakemukset")) {
                        count.incrementAndGet();
                        KevytHakemusDTO hakemus = new KevytHakemusDTO();
                        hakemus.setHakijaOid((String) h.get("hakijaOid"));
                        hakemus.setHakemusOid((String) h.get("hakemusOid"));
                        //hakemus.setEtunimi((String) h.get("etunimi"));
                        //hakemus.setSukunimi((String) h.get("sukunimi"));
                        hakemus.setPrioriteetti((Integer) h.get("prioriteetti"));
                        hakemus.setJonosija((Integer) h.get("jonosija"));
                        //hakemus.setOnkoMuuttunutViimeSijoittelussa((boolean) h.get("onkoMuuttunutViimeSijoittelussa"));
                        String pisteet = (String) h.get("pisteet");
                        hakemus.setPisteet(Strings.isNullOrEmpty(pisteet) ? null : new BigDecimal(pisteet));
                        //hakemus.setTasasijaJonosija((Integer) h.get("tasasijaJonosija"));
                        hakemus.setTila(fi.vm.sade.sijoittelu.tulos.dto.HakemuksenTila.valueOf((String) h.get("tila")));
                        //hakemus.setEdellinenTila(HakemuksenTila.valueOf((String) h.get("edellinenTila")));
                        //String ilmoittautumisTila = (String) h.get("ilmoittautumisTila");
                        //hakemus.setIlmoittautumisTila(ilmoittautumisTila == null ? null : IlmoittautumisTila.valueOf(ilmoittautumisTila));
                        //hakemus.setTilaHistoria(new ArrayList<>()); // TODO
                        List<DBObject> tilaHistoria = (List<DBObject>) h.get("tilaHistoria");
                        Date viimeisenMuutos = tilaHistoria == null ? null : (Date) tilaHistoria.get(tilaHistoria.size() - 1).get("luotu");
                        hakemus.setViimeisenHakemuksenTilanMuutos(viimeisenMuutos);
                        hakemus.setHyvaksyttyHarkinnanvaraisesti((boolean) h.get("hyvaksyttyHarkinnanvaraisesti"));
                        //hakemus.setPistetiedot(new ArrayList<>()); // TODO
                        DBObject tilanKuvaukset = (DBObject) h.get("tilanKuvaukset");
                        for (String k : tilanKuvaukset == null ? Collections.<String>emptySet() : tilanKuvaukset.keySet()) {
                            hakemus.getTilanKuvaukset().put(k, (String) tilanKuvaukset.get(k));
                        }
                        hakemus.setVarasijanNumero((Integer) h.get("varasijanNumero"));
                        //hakemus.setHyvaksyttyHakijaryhmasta((boolean) h.get("hyvaksyttyHakijaryhmasta"));
                        //hakemus.setHakijaryhmaOid((String) h.get("hakijaryhmaOid"));
                        jono.getHakemukset().add(hakemus);
                    }
                    hk.getValintatapajonot().add(jono);
                }
                System.out.println("count " + count.get());
                return hk;
            }
        };
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
}
