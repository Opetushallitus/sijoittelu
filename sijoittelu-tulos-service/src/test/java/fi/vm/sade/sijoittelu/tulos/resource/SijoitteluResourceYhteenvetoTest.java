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

public class SijoitteluResourceYhteenvetoTest extends SijoitteluResourceTest {
    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    SijoitteluResource sijoitteluResource;

    ObjectMapper objectMapper = new ObjectMapper();

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