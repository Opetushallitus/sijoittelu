package fi.vm.sade.sijoittelu.tulos.resource;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import com.mongodb.Mongo;

import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakemusYhteenvetoDTO;
import fi.vm.sade.sijoittelu.tulos.generator.TulosGenerator;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-context-local-mongo.xml"})
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
    public void perfTestWithLocalMongo() {
        perfTest("1.2.246.562.5.2013080813081926341928");
    }

    void perfTest(final String hakuOid) {
        final int kutsuja = 100;
        long started = System.currentTimeMillis();
        for (int i = 0 ; i < kutsuja; i++) {
            final String hakemusOid = TulosGenerator.hakemusOids.randomOid(200);
            final HakemusYhteenvetoDTO yhteenveto = sijoitteluResource.hakemusYhteenveto(hakuOid, "latest", hakemusOid);
            assertEquals(5, yhteenveto.hakutoiveet.size());
        }
        long elapsed = System.currentTimeMillis() - started;
        long msPerCall = elapsed / kutsuja;
        System.out.println("Yhteensä " + kutsuja + "kutsua, keskimääräinen suoritusaika " + msPerCall + " ms");
    }
}
