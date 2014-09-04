package fi.vm.sade.sijoittelu.tulos.generator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.mongodb.morphia.mapping.Mapper;

import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.util.JSON;

import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.sijoittelu.domain.Valintatulos;

public class TulosGenerator {
    private static final Random random = new Random();
    public static OidGen hakemusOids = new OidGen("1.2.246.260.10");
    final static OidGen hakukohdeOids = new OidGen("1.2.246.261.10");
    final static OidGen valintatapajonoOids = new OidGen("1.2.246.262.10");
    final static ObjectTemplate template = new ObjectTemplate("fi/vm/sade/sijoittelu/tulos/generator/generator-templates.json");

    public final static void main(String... args) {
        List<Hakukohde> hakukohteet = createHakukohteet(10);
        List<Valintatulos> tulokset = createTulokset(hakukohteet, 10);
    }

    public static List<Valintatulos> generateTestData(int hakukohteita, int hakemuksia, DB db) {
        final List<Hakukohde> hakukohteet = createHakukohteet(hakukohteita);
        final List<Valintatulos> tulokset = createTulokset(hakukohteet, hakemuksia);
        final Sijoittelu sijoittelu = template.getTemplate("Sijoittelu", Sijoittelu.class);
        saveAll("Sijoittelu", Arrays.asList(sijoittelu), db);
        saveAll("Hakukohde", hakukohteet, db);
        saveAll("Valintatulos", tulokset, db);
        return tulokset;
    }

    private static <T> void saveAll(String collection, List<T> objects, DB db) {
        final Mapper mapper = new Mapper();
        final List<DBObject> dbObjects = objects.stream().map(object -> {
            return mapper.toDBObject(object);
        }).collect(Collectors.toList());
        db.getCollection(collection).insert(dbObjects);
    }

    public static List<Valintatulos> createTulokset(final List<Hakukohde> hakukohteet, int hakemuksia) {
        return IntStream.range(0, hakemuksia).boxed().flatMap(x -> {
            Collections.shuffle(hakukohteet);
            return addHakemus(hakukohteet.stream().limit(5));
        }).collect(Collectors.toList());
    }

    public static List<Hakukohde> createHakukohteet(final int count) {
        return generate(count, (x) -> createHakukohde());
    }

    // tyhjä hakukohde
    static Hakukohde createHakukohde() {
        final Hakukohde hakukohde = template.getTemplate("Hakukohde", Hakukohde.class);
        hakukohde.setValintatapajonot(createValintatapaJonot(3));
        hakukohde.setId(null);
        hakukohde.setOid(hakukohdeOids.nextOid());
        return hakukohde;
    }

    static private Stream<Valintatulos> addHakemus(Stream<Hakukohde> hakukohteet) {
        String oid = hakemusOids.nextOid();
        Counter counter = new Counter();
        return hakukohteet.flatMap((hakukohde) -> hakukohde.getValintatapajonot().stream().map(jono -> {
            Hakemus hakemus = template.getTemplate("Hakemus", Hakemus.class);
            hakemus.setHakemusOid(oid);
            hakemus.setHakijaOid(oid);
            hakemus.setPrioriteetti(counter.next());
            hakemus.setTila(HakemuksenTila.values()[Math.abs(new Random().nextInt()) % HakemuksenTila.values().length]);
            jono.getHakemukset().add(hakemus);
            return createValintatulos(hakukohde, jono, hakemus);
        }));
    }

    static private Valintatulos createValintatulos(final Hakukohde hakukohde, final Valintatapajono jono, final Hakemus hakemus) {
        Valintatulos valintatulos = template.getTemplate("Valintatulos", Valintatulos.class);
        valintatulos.setHakemusOid(hakemus.getHakemusOid());
        valintatulos.setHakijaOid(hakemus.getHakijaOid());
        valintatulos.setHakukohdeOid(hakukohde.getOid());
        valintatulos.setId(null);
        valintatulos.setValintatapajonoOid(jono.getOid());
        return valintatulos;
    }

    static private List<Valintatapajono> createValintatapaJonot(final int count) {
       return generate(count, (x) -> createValintatapaJono());
    }

    static private Valintatapajono createValintatapaJono() {
        final Valintatapajono valintatapajono = template.getTemplate("Valintatapajono", Valintatapajono.class);
        valintatapajono.setOid(valintatapajonoOids.nextOid());
        return valintatapajono;
    }

    static private <T> List<T> generate(int count, IntFunction<T> f) {
        return IntStream.range(0, count).mapToObj(f).collect(Collectors.toList());
    }

    static class Counter {
        int count = 0;
        public int next() {
            return ++count;
        }
    }

    public static class OidGen {
        private final String prefix;
        int counter = 0;
        public OidGen(String prefix) {
            this.prefix = prefix;
        }
        public String nextOid() {
            return getOid(++counter);
        }

        private String getOid(final int num) {
            return prefix + "." + (num);
        }

        public String randomOid(int count) {
            return getOid(random.nextInt(count) + 1);
        }
    }
}