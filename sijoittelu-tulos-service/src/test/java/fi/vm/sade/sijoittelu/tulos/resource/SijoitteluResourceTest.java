package fi.vm.sade.sijoittelu.tulos.resource;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.core.LoadStrategyEnum;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;

import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaDTO;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-context.xml"})
public class SijoitteluResourceTest {
    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("sijoittelu");

    ObjectMapper objectMapper = new ObjectMapper();

    String hakuOid = "1.2.246.562.5.2013080813081926341928";
    String sijoitteluAjoId = "latest";
    String hakemusOid = "1.2.246.562.11.00000441369";

    @Autowired ApplicationContext applicationContext;

    @Autowired
    SijoitteluResource sijoitteluResource;

    final String hakijaAsString = "{\"hakijaOid\":\"1.2.246.562.24.14229104472\",\"hakemusOid\":\"1.2.246.562.11.00000441369\",\"etunimi\":\"Teppo\",\"sukunimi\":\"Testaaja\",\"hakutoiveet\":[{\"hakutoive\":1,\"hakukohdeOid\":\"1.2.246.562.5.72607738902\",\"tarjoajaOid\":\"1.2.246.562.10.591352080610\",\"vastaanottotieto\":null,\"pistetiedot\":[],\"hakutoiveenValintatapajonot\":[{\"valintatapajonoPrioriteetti\":1,\"valintatapajonoOid\":\"14090336922663576781797489829886\",\"valintatapajonoNimi\":\"Varsinainen jono\",\"eiVarasijatayttoa\":true,\"jonosija\":1,\"paasyJaSoveltuvuusKokeenTulos\":null,\"varasijanNumero\":null,\"tila\":\"HYVAKSYTTY\",\"tilanKuvaukset\":{},\"ilmoittautumisTila\":\"EI_TEHTY\",\"hyvaksyttyHarkinnanvaraisesti\":false,\"tasasijaJonosija\":1,\"pisteet\":4,\"alinHyvaksyttyPistemaara\":4.0,\"hakeneet\":1,\"hyvaksytty\":1,\"varalla\":0,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"varasijojaKaytetaanAlkaen\":1409069123943,\"varasijojaTaytetaanAsti\":1409069123943,\"tayttojono\":null,\"julkaistavissa\":true,\"ehdollisestiHyvaksyttavissa\":false,\"hyvaksyttyVarasijalta\":false,\"valintatuloksenViimeisinMuutos\":1409069123943,\"hakemuksenTilanViimeisinMuutos\":1409065960623}],\"kaikkiJonotSijoiteltu\":true,\"ensikertalaisuusHakijaryhmanAlimmatHyvaksytytPisteet\":null}]}";
    final String hakijatAsString = "[{\"hakijaOid\":\"1.2.246.562.24.14229104472\",\"hakemusOid\":\"1.2.246.562.11.00000441369\",\"etunimi\":\"Teppo\",\"sukunimi\":\"Testaaja\",\"hakutoiveet\":[{\"hakutoive\":1,\"hakukohdeOid\":\"1.2.246.562.5.72607738902\",\"tarjoajaOid\":\"1.2.246.562.10.591352080610\",\"vastaanottotieto\":null,\"pistetiedot\":[],\"hakutoiveenValintatapajonot\":[{\"valintatapajonoPrioriteetti\":1,\"valintatapajonoOid\":\"14090336922663576781797489829886\",\"valintatapajonoNimi\":\"Varsinainen jono\",\"eiVarasijatayttoa\":true,\"jonosija\":1,\"paasyJaSoveltuvuusKokeenTulos\":null,\"varasijanNumero\":null,\"tila\":\"HYVAKSYTTY\",\"tilanKuvaukset\":{},\"ilmoittautumisTila\":\"EI_TEHTY\",\"hyvaksyttyHarkinnanvaraisesti\":false,\"tasasijaJonosija\":1,\"pisteet\":4,\"alinHyvaksyttyPistemaara\":4,\"hakeneet\":1,\"hyvaksytty\":1,\"varalla\":0,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"varasijojaKaytetaanAlkaen\":1409069123943,\"varasijojaTaytetaanAsti\":1409069123943,\"tayttojono\":null,\"julkaistavissa\":true,\"ehdollisestiHyvaksyttavissa\":false,\"hyvaksyttyVarasijalta\":false,\"valintatuloksenViimeisinMuutos\":1409069123943,\"hakemuksenTilanViimeisinMuutos\":1409065960623}],\"kaikkiJonotSijoiteltu\":true,\"ensikertalaisuusHakijaryhmanAlimmatHyvaksytytPisteet\":null}]}]";
    final String hakukohde = "1.2.246.562.5.72607738902";

    @Test
    @UsingDataSet(locations = {"sijoittelu-basedata.json", "hyvaksytty-ilmoitettu.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void resultForApplication() throws JsonProcessingException {
        HakijaDTO hakemus = sijoitteluResource.hakemus(hakuOid, sijoitteluAjoId, hakemusOid);
        assertEquals("Teppo", hakemus.getEtunimi());
        assertEquals(2, (int)hakemus.getHakutoiveet().iterator().next().getHakutoiveenValintatapajonot().iterator().next().getHakeneet());
    }

    @Test
    @UsingDataSet(locations = {"sijoittelu-basedata.json", "hyvaksytty-ilmoitettu.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void hyvaksytytHakukohteeseen() throws JsonProcessingException {
        List<HakijaDTO> results = sijoitteluResource.hyvaksytytHakukohteeseen(hakuOid, hakukohde).getResults();
        assertEquals(1, results.size());
        assertEquals("Teppo", results.iterator().next().getEtunimi());
    }

}
