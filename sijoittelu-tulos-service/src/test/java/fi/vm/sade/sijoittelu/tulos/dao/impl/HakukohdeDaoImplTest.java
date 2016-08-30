package fi.vm.sade.sijoittelu.tulos.dao.impl;

import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.core.LoadStrategyEnum;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import fi.vm.sade.sijoittelu.tulos.dao.HakukohdeDao;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-context.xml"})
public class HakukohdeDaoImplTest {
    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("sijoittelu");

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    HakukohdeDao hakukohdeDao;

    @Test
    @UsingDataSet(locations = {"/fi/vm/sade/sijoittelu/tulos/resource/sijoittelu-basedata.json", "/fi/vm/sade/sijoittelu/tulos/resource/hyvaksytty-ilmoitettu.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testKevytHakukohdeFetch() {

        hakukohdeDao.getHakukohdeForSijoitteluajoIterator(1409055160621L, "1.2.246.562.5.72607738902");
    }
}
