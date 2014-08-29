package fi.vm.sade.sijoittelu.tulos.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.core.LoadStrategyEnum;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakemusYhteenvetoDTO;
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

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-context.xml"})
public class SijoitteluResourceTest {

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("sijoittelu");

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    SijoitteluResource sijoitteluResource;

    ObjectMapper objectMapper = new ObjectMapper();

    String hakuOid = "1.2.246.562.5.2013080813081926341928";
    String sijoitteluAjoId = "latest";
    String hakemusOid = "1.2.246.562.11.00000441369";

    @Test
    @UsingDataSet(locations = "sijoittelu-tulos-mockdata.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void resultForApplication() throws JsonProcessingException {
        String expected = "{\"hakemusOid\":\"1.2.246.562.11.00000441369\",\"etunimi\":\"Teppo\",\"sukunimi\":\"Testaaja\",\"hakutoiveet\":[{\"hakutoive\":1,\"hakukohdeOid\":\"1.2.246.562.5.72607738902\",\"tarjoajaOid\":\"1.2.246.562.10.591352080610\",\"pistetiedot\":[],\"hakutoiveenValintatapajonot\":[{\"valintatapajonoPrioriteetti\":1,\"valintatapajonoOid\":\"14090336922663576781797489829886\",\"valintatapajonoNimi\":\"Varsinainen jono\",\"jonosija\":1,\"paasyJaSoveltuvuusKokeenTulos\":null,\"varasijanNumero\":null,\"tila\":\"HYVAKSYTTY\",\"tilanKuvaukset\":{},\"vastaanottotieto\":\"ILMOITETTU\",\"ilmoittautumisTila\":null,\"hyvaksyttyHarkinnanvaraisesti\":false,\"tasasijaJonosija\":1,\"pisteet\":4,\"alinHyvaksyttyPistemaara\":4,\"hakeneet\":1,\"hyvaksytty\":1,\"varalla\":0}]}]}";
        HakijaDTO hakemus = sijoitteluResource.hakemus(hakuOid, sijoitteluAjoId, hakemusOid);
        assertEquals("Teppo", hakemus.getEtunimi());
        assertEquals(expected, objectMapper.writeValueAsString(hakemus));
    }

    @Test
    @UsingDataSet(locations = "sijoittelu-tulos-mockdata.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void yhteenveto() throws JsonProcessingException {
        String expectedResponse = "{\"hakemusOid\":\"1.2.246.562.11.00000441369\",\"hakutoiveet\":[{\"hakukohdeOid\":\"1.2.246.562.5.72607738902\",\"tarjoajaOid\":\"1.2.246.562.10.591352080610\",\"tila\":\"HYVAKSYTTY\",\"vastaanottotieto\":\"ILMOITETTU\",\"ilmoittautumisTila\":null,\"jonosija\":1,\"varasijanNumero\":null}]}";
        HakemusYhteenvetoDTO yhteenveto = sijoitteluResource.hakemusYhteenveto(hakuOid, sijoitteluAjoId, hakemusOid);
        assertEquals(expectedResponse, objectMapper.writeValueAsString(yhteenveto));
    }
}
