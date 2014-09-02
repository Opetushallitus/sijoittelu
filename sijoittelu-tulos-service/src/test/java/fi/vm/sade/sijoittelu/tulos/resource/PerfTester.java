package fi.vm.sade.sijoittelu.tulos.resource;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.databene.contiperf.PerfTest;
import org.databene.contiperf.Required;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mongodb.morphia.Datastore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import com.mongodb.Mongo;

import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakemusYhteenvetoDTO;
import fi.vm.sade.sijoittelu.tulos.generator.TulosGenerator;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-context.xml"})
public class PerfTester {
    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("sijoittelu");

    @Autowired ApplicationContext applicationContext;

    @Autowired
    SijoitteluResource sijoitteluResource;

    @Autowired
    @Qualifier("mongo")
    Mongo mongo;

    @Test
    public void perfTest() {
        final int hakukohteita = 50;
        final int hakemuksia = 10000;
        final int kutsuja = 10;
        final List<Valintatulos> tulokset = TulosGenerator.generateTestData(hakukohteita, hakemuksia, mongo.getDB("sijoittelu"));
        Valintatulos valintatulos = tulokset.get(0);

        long started = System.currentTimeMillis();

        for (int i = 0 ; i < kutsuja; i++) {
            final HakemusYhteenvetoDTO yhteenveto = sijoitteluResource.hakemusYhteenveto(valintatulos.getHakuOid(), "latest", valintatulos.getHakemusOid());
            assertEquals(5, yhteenveto.hakutoiveet.size());
        }

        long elapsed = System.currentTimeMillis() - started;

        long msPerCall = elapsed / kutsuja;

        System.out.println("Hakukohteita " + hakukohteita + ", hakemuksia " + hakemuksia);
        System.out.println("Yhteensä " + kutsuja + "kutsua, keskimääräinen suoritusaika " + msPerCall + " ms");

    }
}
