package fi.vm.sade.sijoittelu;

import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.core.LoadStrategyEnum;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import fi.vm.sade.sijoittelu.laskenta.dao.Dao;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.junit.Assert.assertTrue;

/**
 * User: wuoti
 * Date: 11.11.2013
 * Time: 15.13
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-sijoittelu-batch-mongo.xml"})
public class SijoitteluTesti {
    @Autowired
    private Dao dao;

    @Autowired
    private ApplicationContext applicationContext;

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("sijoittelu");

    @Test
    @UsingDataSet(locations = "sijoittelutestidata.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void haeValintakoeOsallistumisetByOidTest() {
        assertTrue(dao.getHakukohdeForSijoitteluajo(1383905904107L).size() > 0);
    }


}
