package fi.vm.sade.sijoittelu.tulos.resource;

import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.core.LoadStrategyEnum;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaDTO;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.junit.Assert.assertEquals;

/**
 * @author blomqvie
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-context.xml"})
public class SijoitteluResourceTest {
    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("sijoittelu");

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    SijoitteluResource sijoitteluResource;

    @Test
    @UsingDataSet(locations = "sijoittelu-tulos-testdata.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void asdf() {
        String hakuOid = "1.2.246.562.29.92478804245";
        String sijoitteluAjoId = "latest";
        String hakemusOid = "1.2.246.562.11.00000878229";
        HakijaDTO hakemus = sijoitteluResource.hakemus(hakuOid, sijoitteluAjoId, hakemusOid);

        assertEquals("Teppo", hakemus.getEtunimi());
    }
}
