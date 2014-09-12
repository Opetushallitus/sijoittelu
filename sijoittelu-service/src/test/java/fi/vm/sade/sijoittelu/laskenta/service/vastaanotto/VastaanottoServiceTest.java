package fi.vm.sade.sijoittelu.laskenta.service.vastaanotto;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.core.LoadStrategyEnum;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;

import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.ValintatulosPerustiedot;
import fi.vm.sade.sijoittelu.tulos.resource.SijoitteluResource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-sijoittelu-batch-mongo.xml" })
public class VastaanottoServiceTest {
    @Autowired ApplicationContext applicationContext;

    @Autowired VastaanottoService vastaanottoService;

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("sijoittelu");

    @Test(expected = IllegalArgumentException.class)
    @UsingDataSet(locations = {"/fi/vm/sade/sijoittelu/tulos/resource/sijoittelu-basedata.json", "/fi/vm/sade/sijoittelu/tulos/resource/hyvaksytty-ei-valintatulosta.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void uusiValintatulosVääräTila() {
        final String hakuOid = "1.2.246.562.5.2013080813081926341928";
        final String hakukohdeOid = "1.2.246.562.5.72607738903";
        final String valintatapajonoId = "14090336922663576781797489829887";
        final String hakemusOid = "1.2.246.562.11.00000441369";
        final String hakijaOid = "1.2.246.562.24.14229104472";
        final ValintatulosPerustiedot perustiedot = new ValintatulosPerustiedot(hakuOid, hakukohdeOid, valintatapajonoId, hakemusOid, hakijaOid, 1);
        vastaanottoService.vastaanota(perustiedot, ValintatuloksenTila.EI_VASTAANOTETTU_MAARA_AIKANA);
    }
}
