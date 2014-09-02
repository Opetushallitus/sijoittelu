package fi.vm.sade.sijoittelu.tulos.generator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.mapping.Mapper;
import org.mongodb.morphia.mapping.cache.DefaultEntityCache;

import com.mongodb.DBObject;

import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.tulos.service.MongoMockData;

public class TulosGenerator {
    final static DBObject templateData = MongoMockData.readJson("fi/vm/sade/sijoittelu/tulos/generator/generator-templates.json");

    public final static void main(String... args) {
        List<Hakukohde> hakukohteet = createHakukohteet(10);
        List<Valintatulos> tulokset = createTulokset(hakukohteet, 10);
    }

    public static List<Valintatulos> generateTestData(int hakukohteita, int hakemuksia, Datastore morphiaDS) {
        final List<Hakukohde> hakukohteet = createHakukohteet(hakukohteita);
        final List<Valintatulos> tulokset = createTulokset(hakukohteet, hakemuksia);
        final Sijoittelu sijoittelu = getTemplate("Sijoittelu", Sijoittelu.class);
        morphiaDS.save(sijoittelu);
        morphiaDS.save(hakukohteet);
        morphiaDS.save(tulokset);
        return tulokset;
    }

    public static List<Valintatulos> createTulokset(final List<Hakukohde> hakukohteet, int hakemuksia) {
        return IntStream.range(0, hakemuksia).boxed().flatMap(x -> addHakemus(hakukohteet)).collect(Collectors.toList());
    }

    public static List<Hakukohde> createHakukohteet(final int count) {
        return generate(count, (x) -> createHakukohde());
    }

    // tyhjä hakukohde
    static Hakukohde createHakukohde() {
        final Hakukohde hakukohde = getTemplate("Hakukohde", Hakukohde.class);
        hakukohde.setValintatapajonot(createValintatapaJonot(3));
        hakukohde.setId(null);
        hakukohde.setOid(generateOid());
        return hakukohde;
    }

    static private Stream<Valintatulos> addHakemus(List<Hakukohde> hakukohteet) {
        String oid = generateOid();
        Counter counter = new Counter();
        return hakukohteet.stream().flatMap((hakukohde) -> hakukohde.getValintatapajonot().stream().map(jono -> {
                Hakemus hakemus = getTemplate("Hakemus", Hakemus.class);
                hakemus.setHakemusOid(oid);
                hakemus.setHakijaOid(oid);
                hakemus.setPrioriteetti(counter.next());
                hakemus.setTila(HakemuksenTila.values()[Math.abs(new Random().nextInt()) % HakemuksenTila.values().length]);
                jono.getHakemukset().add(hakemus);
                return createValintatulos(hakukohde, jono, hakemus);
            }));
    }

    static private Valintatulos createValintatulos(final Hakukohde hakukohde, final Valintatapajono jono, final Hakemus hakemus) {
        Valintatulos valintatulos = getTemplate("Valintatulos", Valintatulos.class);
        valintatulos.setHakemusOid(hakemus.getHakemusOid());
        valintatulos.setHakijaOid(hakemus.getHakijaOid());
        valintatulos.setHakukohdeOid(hakukohde.getOid());
        valintatulos.setId(null);
        valintatulos.setValintatapajonoOid(jono.getOid());
        return valintatulos;
    }

    static int counter = 0;
    static private String generateOid() {
        return "1.2.246.262.10" + (++counter);
    }

    static private List<Valintatapajono> createValintatapaJonot(final int count) {
       return generate(count, (x) -> createValintatapaJono());
    }

    static private Valintatapajono createValintatapaJono() {
        final Valintatapajono valintatapajono = getTemplate("Valintatapajono", Valintatapajono.class);
        valintatapajono.setOid(generateOid());
        return valintatapajono;
    }

    static private <T> List<T> generate(int count, IntFunction<T> f) {
        return IntStream.range(0, count).mapToObj(f).collect(Collectors.toList());
    }

    static private <T> T getTemplate(String collection, Class<T> clazz) {
        final DBObject mongoObject = MongoMockData.collectionElements(templateData, collection).get(0);
        return new Mapper().fromDBObject(clazz, mongoObject, new DefaultEntityCache());
    }

    static class Counter {
        int count = 0;
        public int next() {
            return ++count;
        }
    }
}