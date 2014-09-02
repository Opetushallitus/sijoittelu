package fi.vm.sade.sijoittelu.tulos.resource;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.junit.Assert.assertEquals;

import java.util.List;

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
    @Qualifier("datastore")

    Datastore morphiaDs;

    @Test
    public void perfTest() {
        final List<Valintatulos> tulokset = TulosGenerator.generateTestData(10, 10, morphiaDs);
        final Valintatulos valintatulos = tulokset.get(0);
        final HakemusYhteenvetoDTO yhteenveto = sijoitteluResource.hakemusYhteenveto(valintatulos.getHakuOid(), "latest", valintatulos.getHakemusOid());
        System.out.println(yhteenveto);
        assertEquals(10, yhteenveto.hakutoiveet.size());
    }
}
