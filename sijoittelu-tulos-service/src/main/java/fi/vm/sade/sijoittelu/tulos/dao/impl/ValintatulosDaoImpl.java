package fi.vm.sade.sijoittelu.tulos.dao.impl;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import fi.vm.sade.sijoittelu.domain.*;
import org.apache.commons.lang.StringUtils;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import fi.vm.sade.sijoittelu.tulos.dao.ValintatulosDao;

@Repository
public class ValintatulosDaoImpl implements ValintatulosDao {
    private static final Logger LOG = LoggerFactory.getLogger(ValintatulosDaoImpl.class);

    @Qualifier("datastore")
    @Autowired
    private Datastore morphiaDS;

    @PostConstruct
    public void ensureIndexes() {
        EnsureIndexes.ensureIndexes(morphiaDS, Valintatulos.class);
    }

    @Override
    public List<Valintatulos> loadValintatulos(String hakemusOid) {
        if (StringUtils.isBlank(hakemusOid)) {
            throw new RuntimeException("Invalid search params, fix exception later");
        }
        Query<Valintatulos> q = morphiaDS.createQuery(Valintatulos.class);
        q.criteria("hakemusOid").equal(hakemusOid);
        return q.asList();
    }

    @Override
    public Valintatulos loadValintatulos(String hakukohdeOid,
                                         String valintatapajonoOid, String hakemusOid) {
        if (StringUtils.isBlank(hakukohdeOid)
            || StringUtils.isBlank(hakukohdeOid)
            || StringUtils.isBlank(hakemusOid)) {
            throw new RuntimeException("Invalid search params, fix exception later");
        }
        Query<Valintatulos> q = morphiaDS.createQuery(Valintatulos.class);
        q.criteria("hakukohdeOid").equal(hakukohdeOid);
        q.criteria("valintatapajonoOid").equal(valintatapajonoOid);
        q.criteria("hakemusOid").equal(hakemusOid);
        return q.get();
    }

    @Override
    public List<Valintatulos> loadValintatulokset(String hakukohdeOid,
                                                  String valintatapajonoOid) {
        if (StringUtils.isBlank(hakukohdeOid)
            || StringUtils.isBlank(valintatapajonoOid)) {
            throw new RuntimeException("Invalid search params, fix exception later");
        }
        Query<Valintatulos> q = morphiaDS.createQuery(Valintatulos.class);
        q.and(
                //
                q.criteria("hakukohdeOid").equal(hakukohdeOid),
                //
                q.criteria("valintatapajonoOid").equal(valintatapajonoOid));

        return q.asList();
    }

    @Override
    public List<Valintatulos> loadValintatuloksetForHakukohde(
        String hakukohdeOid) {
        if (StringUtils.isBlank(hakukohdeOid)) {
            throw new RuntimeException("Invalid search params, fix exception later");
        }
        Query<Valintatulos> q = morphiaDS.createQuery(Valintatulos.class);
        q.or(q.criteria("hakukohdeOid").equal(hakukohdeOid));
        return q.asList();
    }

    @Override
    public List<Valintatulos> loadValintatulokset(String hakuOid) {
        if (StringUtils.isBlank(hakuOid)) {
            throw new RuntimeException("Invalid search params, fix exception later");
        }
        Query<Valintatulos> q = morphiaDS.createQuery(Valintatulos.class);
        q.criteria("hakuOid").equal(hakuOid);
        return q.asList();
    }

    @Override
    public List<Valintatulos> loadValintatuloksetForHakemus(String hakemusOid) {
        if (StringUtils.isBlank(hakemusOid)) {
            throw new RuntimeException("Invalid search params, fix exception later");
        }
        Query<Valintatulos> q = morphiaDS.createQuery(Valintatulos.class);
        q.criteria("hakemusOid").equal(hakemusOid);
        return q.asList();
    }

    @Override
    public void remove(Valintatulos valintatulos) {
        morphiaDS.delete(valintatulos);
    }

    @Override
    public void createOrUpdateValintatulos(Valintatulos tulos) {
        if(tulos.getId() == null) {
            morphiaDS.save(tulos);
        } else {
            UpdateOperations<Valintatulos> update = morphiaDS.createUpdateOperations(Valintatulos.class)
                    .set("ilmoittautumisTila",tulos.getIlmoittautumisTila())
                    .set("julkaistavissa", tulos.getJulkaistavissa())
                    .set("hyvaksyttyVarasijalta", tulos.getHyvaksyttyVarasijalta())
                    .set("hyvaksyPeruuntunut", tulos.getHyvaksyPeruuntunut());
            List<LogEntry> diff = new ArrayList<>(tulos.getLogEntries());
            diff.removeAll(tulos.getOriginalLogEntries());
            if(!diff.isEmpty()) {
                update.addAll("logEntries", diff, false);
            }
            morphiaDS.update(morphiaDS.createQuery(Valintatulos.class).field("_id").equal(tulos.getId()),update);
        }
    }

    @Override
    public List<Valintatulos> mergaaValintatulos(List<Hakukohde> kaikkiHakukohteet, List<Valintatulos> sijoittelunTulokset) {
        final Map<String, List<Valintatulos>> hakukohteidenValintatuloksetMongo = hakukohteidenValintatuloksetMongo(kaikkiHakukohteet);
        final Set<String> peruuntuneetHakemukset = kaikkiHakukohteet.stream()
                .flatMap(s -> s.getValintatapajonot().stream())
                .flatMap(j -> j.getHakemukset().stream().filter(h -> h.getTila().equals(HakemuksenTila.PERUUNTUNUT)))
                .map(Hakemus::getHakemusOid)
                .collect(Collectors.toSet());

        return sijoittelunTulokset.stream().map(valintatulos -> {
            if (!peruuntuneetHakemukset.contains(valintatulos.getHakemusOid())) {
                final Optional<Valintatulos> valintatulosMongosta = haeOlemassaolevaValintatulosMongosta(hakukohteidenValintatuloksetMongo, valintatulos);
                if (valintatulosMongosta.isPresent()) {
                    Valintatulos mongoTulos = valintatulosMongosta.get();
                    if (mongoTulos.getIlmoittautumisTila() != valintatulos.getIlmoittautumisTila()) {
                        valintatulos.setIlmoittautumisTila(mongoTulos.getIlmoittautumisTila(), "Korvataan sijoittelun ulkopuolella muuttuneella tiedolla");
                    }
                    if (mongoTulos.getJulkaistavissa() != valintatulos.getJulkaistavissa()) {
                        valintatulos.setJulkaistavissa(mongoTulos.getJulkaistavissa(), "Korvataan sijoittelun ulkopuolella muuttuneella tiedolla");
                    }
                    if (mongoTulos.getTila() != ValintatuloksenTila.KESKEN) {
                        LOG.info("Ohitetaan sijoittelun ulkopuolella muutettu valintatulos: hakija {}, hakemus {}, hakutoive {}",
                                valintatulos.getHakijaOid(), valintatulos.getHakemusOid(), valintatulos.getHakutoive());
                        valintatulos.setTila(mongoTulos.getTila(), "Korvataan sijoittelun ulkopuolella muuttuneella tiedolla");
                    }
                }
            }
            return valintatulos;
        }).collect(Collectors.toList());
    }

    private Optional<Valintatulos> haeOlemassaolevaValintatulosMongosta(Map<String, List<Valintatulos>> hakukohteidenValintatuloksetMongo, Valintatulos valintatulos) {
        return hakukohteidenValintatuloksetMongo.get(valintatulos.getHakukohdeOid()).stream()
                .filter(v -> v.getHakemusOid().equals(valintatulos.getHakemusOid()) && v.getValintatapajonoOid().equals(valintatulos.getValintatapajonoOid()))
                .findFirst();
    }

    private Map<String, List<Valintatulos>> hakukohteidenValintatuloksetMongo(List<Hakukohde> kaikkiHakukohteet) {
        return kaikkiHakukohteet.stream().collect(Collectors.toMap(Hakukohde::getOid, h -> loadValintatuloksetForHakukohde(h.getOid())));
    }
}
