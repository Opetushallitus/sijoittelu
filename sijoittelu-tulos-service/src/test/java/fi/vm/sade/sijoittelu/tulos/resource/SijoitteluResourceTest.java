package fi.vm.sade.sijoittelu.tulos.resource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.core.LoadStrategyEnum;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;

import fi.vm.sade.sijoittelu.tulos.dto.raportointi.*;

import org.joda.time.DateTimeUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;

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
    @UsingDataSet(locations = {"sijoittelu-basedata.json", "sijoittelu-tulos-mockdata.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void resultForApplication() throws JsonProcessingException {
        String expected = "{\"hakemusOid\":\"1.2.246.562.11.00000441369\",\"etunimi\":\"Teppo\",\"sukunimi\":\"Testaaja\",\"hakutoiveet\":[{\"hakutoive\":1,\"hakukohdeOid\":\"1.2.246.562.5.72607738902\",\"tarjoajaOid\":\"1.2.246.562.10.591352080610\",\"pistetiedot\":[],\"hakutoiveenValintatapajonot\":[{\"valintatapajonoPrioriteetti\":1,\"valintatapajonoOid\":\"14090336922663576781797489829886\",\"valintatapajonoNimi\":\"Varsinainen jono\",\"jonosija\":1,\"paasyJaSoveltuvuusKokeenTulos\":null,\"varasijanNumero\":null,\"tila\":\"HYVAKSYTTY\",\"tilanKuvaukset\":{},\"vastaanottotieto\":\"ILMOITETTU\",\"ilmoittautumisTila\":\"EI_TEHTY\",\"hyvaksyttyHarkinnanvaraisesti\":false,\"tasasijaJonosija\":1,\"pisteet\":4,\"alinHyvaksyttyPistemaara\":4.0,\"hakeneet\":1,\"hyvaksytty\":1,\"varalla\":0,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"varasijojaKaytetaanAlkaen\":1409069123943,\"varasijojaTaytetaanAsti\":1409069123943,\"tayttojono\":null}],\"kaikkiJonotSijoiteltu\":true}]}";
        HakijaDTO hakemus = sijoitteluResource.hakemus(hakuOid, sijoitteluAjoId, hakemusOid);
        assertEquals("Teppo", hakemus.getEtunimi());
        assertEquals(expected, objectMapper.writeValueAsString(hakemus));
    }

    @Test
    @UsingDataSet(locations = {"sijoittelu-basedata.json", "sijoittelu-tulos-mockdata.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void hyvaksytty() throws JsonProcessingException {
        String expectedResponse = "{\"hakemusOid\":\"1.2.246.562.11.00000441369\",\"hakutoiveet\":[{\"hakukohdeOid\":\"1.2.246.562.5.72607738902\",\"tarjoajaOid\":\"1.2.246.562.10.591352080610\",\"valintatila\":\"HYVAKSYTTY\",\"vastaanottotila\":\"ILMOITETTU\",\"ilmoittautumistila\":\"EI_TEHTY\",\"vastaanotettavuustila\":\"VASTAANOTETTAVISSA_SITOVASTI\",\"jonosija\":1,\"varasijanumero\":null}]}";
        HakemusYhteenvetoDTO yhteenveto = getYhteenveto();
        assertEquals(expectedResponse, objectMapper.writeValueAsString(yhteenveto));
        checkHakutoiveState(yhteenveto.hakutoiveet.get(0), YhteenvedonTila.HYVAKSYTTY, Vastaanotettavuustila.VASTAANOTETTAVISSA_SITOVASTI);
    }

    @Test
    @UsingDataSet(locations = {"sijoittelu-basedata.json", "hyvaksytty-ylempi-sijoittelematon.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void hyvaksyttyYlempiSijoittelematon() throws JsonProcessingException {
        HakemusYhteenvetoDTO yhteenveto = getYhteenveto();
        checkHakutoiveState(yhteenveto.hakutoiveet.get(0), YhteenvedonTila.KESKEN, Vastaanotettavuustila.EI_VASTAANOTETTAVISSA);
        checkHakutoiveState(yhteenveto.hakutoiveet.get(1), YhteenvedonTila.KESKEN, Vastaanotettavuustila.EI_VASTAANOTETTAVISSA);
    }

    @Test
    @UsingDataSet(locations = {"sijoittelu-basedata.json", "hyvaksytty-ylempi-sijoiteltu.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void hyvaksyttyYlemmatSijoiteltu() throws JsonProcessingException {
        HakemusYhteenvetoDTO yhteenveto = getYhteenveto();
        checkHakutoiveState(yhteenveto.hakutoiveet.get(0), YhteenvedonTila.HYLATTY, Vastaanotettavuustila.EI_VASTAANOTETTAVISSA);
        checkHakutoiveState(yhteenveto.hakutoiveet.get(1), YhteenvedonTila.HYVAKSYTTY, Vastaanotettavuustila.VASTAANOTETTAVISSA_SITOVASTI);
    }

    @Test
    @UsingDataSet(locations = {"sijoittelu-basedata.json", "hyvaksytty-ylempi-varalla.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void hyvaksyttyYlempiVaralla() throws JsonProcessingException {
        HakemusYhteenvetoDTO yhteenveto = getYhteenveto();
        checkHakutoiveState(yhteenveto.hakutoiveet.get(0), YhteenvedonTila.VARALLA, Vastaanotettavuustila.EI_VASTAANOTETTAVISSA);
        checkHakutoiveState(yhteenveto.hakutoiveet.get(1), YhteenvedonTila.HYVAKSYTTY, Vastaanotettavuustila.EI_VASTAANOTETTAVISSA);
    }

    @Test
    @UsingDataSet(locations = {"sijoittelu-basedata.json", "hyvaksytty-ylempi-varalla.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void varallaKäytetäänParastaVarasijaa() throws JsonProcessingException {
        HakemusYhteenvetoDTO yhteenveto = getYhteenveto();
        assertEquals(new Integer(2), yhteenveto.hakutoiveet.get(0).varasijanumero);
    }


    @Test
    @UsingDataSet(locations = {"sijoittelu-basedata.json", "hyvaksytty-ylempi-varalla.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void hyvaksyttyYlempiVarallaAikaparametriLauennut() throws JsonProcessingException, ParseException {
        DateTimeUtils.setCurrentMillisFixed(new SimpleDateFormat("d.M.yyyy").parse("15.8.2014").getTime());
        try {
            HakemusYhteenvetoDTO yhteenveto = getYhteenveto();
            checkHakutoiveState(yhteenveto.hakutoiveet.get(0), YhteenvedonTila.VARALLA, Vastaanotettavuustila.EI_VASTAANOTETTAVISSA);
            checkHakutoiveState(yhteenveto.hakutoiveet.get(1), YhteenvedonTila.HYVAKSYTTY, Vastaanotettavuustila.VASTAANOTETTAVISSA_EHDOLLISESTI);
        } finally {
            DateTimeUtils.setCurrentMillisSystem();
        }
    }

    @Test
    @UsingDataSet(locations = {"sijoittelu-basedata.json", "hylatty-jonoja-kesken.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void hakutoiveHylattyKunSijoitteluKesken() {
        HakutoiveYhteenvetoDTO hakuToive = getHakuToive();
        checkHakutoiveState(hakuToive, YhteenvedonTila.KESKEN, Vastaanotettavuustila.EI_VASTAANOTETTAVISSA);
    }

    @Test
    @UsingDataSet(locations = {"sijoittelu-basedata.json", "hylatty-jonot-valmiit.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void hakutoiveHylattyKunSijoitteluValmis() {
        HakutoiveYhteenvetoDTO hakuToive = getHakuToive();
        checkHakutoiveState(hakuToive, YhteenvedonTila.HYLATTY, Vastaanotettavuustila.EI_VASTAANOTETTAVISSA);
    }

    private void checkHakutoiveState(HakutoiveYhteenvetoDTO hakuToive, YhteenvedonTila expectedTila, Vastaanotettavuustila vastaanotettavuustila) {
        assertEquals(expectedTila, hakuToive.valintatila);
        assertEquals(vastaanotettavuustila, hakuToive.vastaanotettavuustila);
    }

    private HakutoiveYhteenvetoDTO getHakuToive() {
        HakemusYhteenvetoDTO yhteenveto = getYhteenveto();
        return yhteenveto.hakutoiveet.get(0);
    }

    private HakemusYhteenvetoDTO getYhteenveto() {
        return sijoitteluResource.hakemusYhteenveto(hakuOid, sijoitteluAjoId, hakemusOid);
    }
}
