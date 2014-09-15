package fi.vm.sade.sijoittelu.laskenta.service.vastaanotto;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.joda.time.DateTimeUtils;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.core.LoadStrategyEnum;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;

import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakemusYhteenvetoDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.Vastaanotettavuustila;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.YhteenvedonValintaTila;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.YhteenvedonVastaanottotila;
import fi.vm.sade.sijoittelu.tulos.resource.SijoitteluResource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:vastaanotto-service-test-context.xml"})
public class VastaanottoServiceTest {
    @Autowired ApplicationContext applicationContext;

    @Autowired VastaanottoService vastaanottoService;
    @Autowired SijoitteluResource sijoitteluResource;

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("sijoittelu");

    final String hakuOid = "1.2.246.562.5.2013080813081926341928";
    final String hakukohdeOid = "1.2.246.562.5.72607738903";
    final String hakemusOid = "1.2.246.562.11.00000441369";

    @Test(expected = IllegalArgumentException.class)
    @UsingDataSet(locations = {"/fi/vm/sade/sijoittelu/tulos/resource/sijoittelu-basedata.json", "/fi/vm/sade/sijoittelu/tulos/resource/hyvaksytty-ei-valintatulosta.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void uusiValintatulosVastaanotaVäärälläArvolla() {
        assertEquals(YhteenvedonVastaanottotila.KESKEN, getYhteenveto().hakutoiveet.get(0).vastaanottotila);
        vastaanottoService.vastaanota(hakuOid, hakemusOid, hakukohdeOid, ValintatuloksenTila.EI_VASTAANOTETTU_MAARA_AIKANA); // <- EI_VASTAANOTETTU_MAARA_AIKANA ei ole hyväksytty arvo
    }

    @Test
    @UsingDataSet(locations = {"/fi/vm/sade/sijoittelu/tulos/resource/sijoittelu-basedata.json", "/fi/vm/sade/sijoittelu/tulos/resource/hyvaksytty-ei-valintatulosta.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void uusiValintatulosVastaanota() {
        assertEquals(YhteenvedonVastaanottotila.KESKEN, getYhteenveto().hakutoiveet.get(0).vastaanottotila);
        vastaanottoService.vastaanota(hakuOid, hakemusOid, hakukohdeOid, ValintatuloksenTila.VASTAANOTTANUT);
        assertEquals(YhteenvedonVastaanottotila.VASTAANOTTANUT, getYhteenveto().hakutoiveet.get(0).vastaanottotila);
    }

    @Test
    @UsingDataSet(locations = {"/fi/vm/sade/sijoittelu/tulos/resource/sijoittelu-basedata.json", "/fi/vm/sade/sijoittelu/tulos/resource/hyvaksytty-ei-valintatulosta.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void uusiValintatulosPeru() {
        vastaanottoService.vastaanota(hakuOid, hakemusOid, hakukohdeOid, ValintatuloksenTila.PERUNUT);
        assertEquals(YhteenvedonVastaanottotila.PERUNUT, getYhteenveto().hakutoiveet.get(0).vastaanottotila);
    }

    @Test(expected = IllegalArgumentException.class)
    @UsingDataSet(locations = {"/fi/vm/sade/sijoittelu/tulos/resource/sijoittelu-basedata.json", "/fi/vm/sade/sijoittelu/tulos/resource/hyvaksytty-ylempi-varalla.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void vastaanotaEhdollisestiKunAikaparametriEiLauennut() throws JsonProcessingException, ParseException {
        vastaanottoService.vastaanota(hakuOid, hakemusOid, hakukohdeOid, ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT);
    }

    @Test
    @UsingDataSet(locations = {"/fi/vm/sade/sijoittelu/tulos/resource/sijoittelu-basedata.json", "/fi/vm/sade/sijoittelu/tulos/resource/hyvaksytty-ylempi-varalla.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void vastaanotaEhdollisestiKunAikaparametriLauennut() throws JsonProcessingException, ParseException {
        DateTimeUtils.setCurrentMillisFixed(new SimpleDateFormat("d.M.yyyy").parse("15.8.2014").getTime());
        try {
            vastaanottoService.vastaanota(hakuOid, hakemusOid, hakukohdeOid, ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT);
        } finally {
            DateTimeUtils.setCurrentMillisSystem();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    @UsingDataSet(locations = {"/fi/vm/sade/sijoittelu/tulos/resource/sijoittelu-basedata.json", "/fi/vm/sade/sijoittelu/tulos/resource/hyvaksytty-vastaanottanut.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void vastaanotaAiemminVastaanotettu() throws JsonProcessingException, ParseException {
        vastaanottoService.vastaanota(hakuOid, hakemusOid, hakukohdeOid, ValintatuloksenTila.VASTAANOTTANUT);
    }

    @Test(expected = IllegalArgumentException.class)
    @UsingDataSet(locations = {"/fi/vm/sade/sijoittelu/tulos/resource/sijoittelu-basedata.json", "/fi/vm/sade/sijoittelu/tulos/resource/hyvaksytty-ei-valintatulosta.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void hakemustaEiLöydy() {
        vastaanottoService.vastaanota(hakuOid, hakemusOid + 1, hakukohdeOid, ValintatuloksenTila.VASTAANOTTANUT);
    }

    @Test(expected = IllegalArgumentException.class)
    @UsingDataSet(locations = {"/fi/vm/sade/sijoittelu/tulos/resource/sijoittelu-basedata.json", "/fi/vm/sade/sijoittelu/tulos/resource/hyvaksytty-ei-valintatulosta.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void hakukohdettaEiLöydy() {
        vastaanottoService.vastaanota(hakuOid, hakemusOid, hakukohdeOid + 1, ValintatuloksenTila.VASTAANOTTANUT);
    }

    // TODO: logEntries-testit ja toteutus

    private HakemusYhteenvetoDTO getYhteenveto() {
        return sijoitteluResource.hakemusYhteenveto(hakuOid, "latest", hakemusOid);
    }
}
