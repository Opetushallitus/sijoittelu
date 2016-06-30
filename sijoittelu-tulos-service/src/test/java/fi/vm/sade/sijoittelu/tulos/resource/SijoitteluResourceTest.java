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
        assertEquals(hakijaAsString, objectMapper.writeValueAsString(hakemus));
    }

    @Test
    @UsingDataSet(locations = {"sijoittelu-basedata.json", "hyvaksytty-ilmoitettu.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void hyvaksytytHakukohteeseen() throws JsonProcessingException {
        verifyHakemus(sijoitteluResource.hyvaksytytHakukohteeseen(hakuOid, hakukohde).getResults());
    }

    @Test
    @UsingDataSet(locations = {"sijoittelu-basedata.json", "hyvaksytty-ilmoitettu.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void hakemuksetVainHyvaksytytHakukohteelle() throws JsonProcessingException {
        verifyHakemus(sijoitteluResource.hakemukset(hakuOid, "latest", true, null, null, asList(hakukohde), null, null).getResults());
    }

    @Test
    @UsingDataSet(locations = {"sijoittelu-basedata.json", "hyvaksytty-ilmoitettu.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void hakemuksetVainIlmanhyvaksyntaa() throws JsonProcessingException {
        assertEquals(0, sijoitteluResource.hakemukset(hakuOid, "latest", null, true, null, null, null, null).getResults().size());
    }

    @Test
    @UsingDataSet(locations = {"sijoittelu-basedata.json", "hyvaksytty-ilmoitettu.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void hakemuksetKaikilleHakukohteelle() throws JsonProcessingException {
        verifyHakemus(sijoitteluResource.hakemukset(hakuOid, "latest", true, null, null, null, null, null).getResults());
    }

    @Test
    @UsingDataSet(locations = {"sijoittelu-basedata.json", "hyvaksytty-ilmoitettu.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void hakemuksetVäärälleHaulle() throws JsonProcessingException {
        assertEquals(0, sijoitteluResource.hakemukset("???", "latest", true, null, null, null, null, null).getResults().size());
    }

    @Test
    @UsingDataSet(locations = {"sijoittelu-basedata.json", "hyvaksytty-ilmoitettu.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void hakemuksetTietylleSijoitteluajolle() throws JsonProcessingException {
        verifyHakemus(sijoitteluResource.hakemukset(hakuOid, "1409055160621", true, null, null, null, null, null).getResults());
    }

    @Test
    @UsingDataSet(locations = {"sijoittelu-basedata.json", "hyvaksytty-ilmoitettu.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void hakemuksetVäärälleSijoitteluajolle() throws JsonProcessingException {
        assertEquals(0, sijoitteluResource.hakemukset(hakuOid, "1234", true, null, null, null, null, null).getResults().size());
    }

    @Test
    @UsingDataSet(locations = {"sijoittelu-basedata.json", "hyvaksytty-ilmoitettu.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testValintatapajonoInUse() {
        assertEquals(true, sijoitteluResource.isValintapajonoInUse(hakuOid, "14090336922663576781797489829886"));
        assertEquals(false, sijoitteluResource.isValintapajonoInUse(hakuOid, "nonExistingOid"));
    }

    @Test
    @UsingDataSet(locations = {"sijoittelu-basedata.json", "hyvaksytty-ilmoitettu.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testValintatapajonoInUseWhenSijoitteluHasNotBeenExecuted() {
        assertEquals(false, sijoitteluResource.isValintapajonoInUse("nonExistingHaku", "14090336922663576781797489829886"));
    }

    private void verifyHakemus(final List<HakijaDTO> hakijat) throws JsonProcessingException {
        assertEquals(1, hakijat.size());
        assertEquals(hakijatAsString, objectMapper.writeValueAsString(hakijat));
    }

}
