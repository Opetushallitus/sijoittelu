package fi.vm.sade.sijoittelu;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Terminated;
import akka.testkit.JavaTestKit;
import com.google.gson.GsonBuilder;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.core.LoadStrategyEnum;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.sijoittelu.laskenta.actors.messages.PoistaVanhatAjotSijoittelulta;
import fi.vm.sade.sijoittelu.tulos.dao.HakukohdeDao;
import fi.vm.sade.sijoittelu.tulos.dao.SijoitteluDao;
import org.junit.*;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.junit.Assert.assertEquals;
import static fi.vm.sade.sijoittelu.laskenta.actors.creators.SpringExtension.SpringExtProvider;

/**
 * User: wuoti Date: 11.11.2013 Time: 15.13
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-sijoittelu-batch-mongo.xml" })
public class SijoitteluSiivousTest {

    private static final Logger LOG = LoggerFactory.getLogger(SijoitteluSiivousTest.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    SijoitteluDao sijoitteluDao;

    @Autowired
    HakukohdeDao hakukohdeDao;

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("sijoittelu");

    private ActorSystem system;

    @Before
    public void setup() {
        system = ActorSystem.create();
        SpringExtProvider.get(system).initialize(applicationContext);
    }

    @After
    public void teardown() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    @UsingDataSet(locations = "sijoittelu-siivous-test-data.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void poistaVanhatAjotTest() {

        new JavaTestKit(system) {{

            final ActorRef target = system.actorOf(
                    SpringExtProvider.get(system).props("SijoitteluSiivousActor"),
                    "testActor");

            final JavaTestKit probe = new JavaTestKit(system);
            probe.watch(target);

            final Long sijoitteluId = new Long("1407160499958");
            final int maara = 10;

            Sijoittelu original = sijoitteluDao.getSijoitteluById(sijoitteluId).get();
            final Long latest = original.getLatestSijoitteluajo().getSijoitteluajoId();

            target.tell(new PoistaVanhatAjotSijoittelulta(sijoitteluId, maara, "hakuoid-tahan"), ActorRef.noSender());

            final Terminated msg = probe.expectMsgClass(Terminated.class);

            Sijoittelu sijoittelu = sijoitteluDao.getSijoitteluById(sijoitteluId).get();

            System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(sijoittelu));
            assertEquals(msg.getActor(), target);
            assertEquals(sijoittelu.getSijoitteluajot().size(), 10);
            assertEquals(sijoittelu.getLatestSijoitteluajo().getSijoitteluajoId().longValue(), latest.longValue());
        }};

    }

}
