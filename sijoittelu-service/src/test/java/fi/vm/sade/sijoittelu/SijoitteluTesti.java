package fi.vm.sade.sijoittelu;

import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.core.LoadStrategyEnum;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.PrintHelper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithm;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithmFactory;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.sijoittelu.laskenta.dao.Dao;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;

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

    @Autowired
    private SijoitteluAlgorithmFactory algorithmFactory;

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("sijoittelu");

    @Test
    @UsingDataSet(locations = "sijoittelutestidata.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void syksy2013test() {
        List<Hakukohde> hakukohteet = dao.getHakukohdeForSijoitteluajo(1383916417363L);

        for(Hakukohde hk : hakukohteet) {
            for(Valintatapajono vt :  hk.getValintatapajonot()) {
                for(Hakemus h : vt.getHakemukset()) {
                    if(h.getTila() != HakemuksenTila.HYLATTY || h.getTila()!=HakemuksenTila.PERUNUT) {
                        h.setTila(null);
                    }
                }
            }
        }

        SijoitteluAlgorithm sijoitteluAlgorithm = algorithmFactory.constructAlgorithm(hakukohteet, null);
        System.out.println("START");
        sijoitteluAlgorithm.start();
        System.out.println("END");
        System.out.println( PrintHelper.tulostaSijoittelu(sijoitteluAlgorithm));
    }


}
