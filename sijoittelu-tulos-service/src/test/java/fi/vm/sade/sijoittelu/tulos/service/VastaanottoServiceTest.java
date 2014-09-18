package fi.vm.sade.sijoittelu.tulos.service;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import org.joda.time.DateTimeUtils;
import org.joda.time.LocalDate;
import org.junit.After;
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

import fi.vm.sade.sijoittelu.domain.LogEntry;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.tulos.dao.ValintatulosDao;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakemusYhteenvetoDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.YhteenvedonValintaTila;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.YhteenvedonVastaanottotila;
import fi.vm.sade.sijoittelu.tulos.resource.SijoitteluResource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:test-context.xml"})
public class VastaanottoServiceTest {
    @Autowired ApplicationContext applicationContext;

    @Autowired VastaanottoService vastaanottoService;
    @Autowired SijoitteluResource sijoitteluResource;
    @Autowired ValintatulosDao valintatulosDao;

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("sijoittelu");

    final String hakuOid = "1.2.246.562.5.2013080813081926341928";
    final String hakukohdeOid = "1.2.246.562.5.72607738903";
    final String hakemusOid = "1.2.246.562.11.00000441369";
    final String muokkaaja = "Teppo Testi";
    final String selite = "Testimuokkaus";

    @Test(expected = IllegalArgumentException.class)
    @UsingDataSet(locations = {"/fi/vm/sade/sijoittelu/tulos/resource/sijoittelu-basedata.json", "/fi/vm/sade/sijoittelu/tulos/resource/hyvaksytty-ei-valintatulosta.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void uusiValintatulosVastaanotaVäärälläArvolla() {
        assertEquals(YhteenvedonVastaanottotila.KESKEN, getYhteenveto().hakutoiveet.get(0).vastaanottotila);
        vastaanottoService.vastaanota(hakuOid, hakemusOid, hakukohdeOid, ValintatuloksenTila.EI_VASTAANOTETTU_MAARA_AIKANA, muokkaaja, selite); // <- EI_VASTAANOTETTU_MAARA_AIKANA ei ole hyväksytty arvo
    }

    @Test
    @UsingDataSet(locations = {"/fi/vm/sade/sijoittelu/tulos/resource/sijoittelu-basedata.json", "/fi/vm/sade/sijoittelu/tulos/resource/hyvaksytty-ei-valintatulosta.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void uusiValintatulosVastaanota() {
        assertEquals(YhteenvedonVastaanottotila.KESKEN, getYhteenveto().hakutoiveet.get(0).vastaanottotila);
        vastaanottoService.vastaanota(hakuOid, hakemusOid, hakukohdeOid, ValintatuloksenTila.VASTAANOTTANUT, muokkaaja, selite);
        assertEquals(YhteenvedonVastaanottotila.VASTAANOTTANUT, getYhteenveto().hakutoiveet.get(0).vastaanottotila);
    }

    @Test
    @UsingDataSet(locations = {"/fi/vm/sade/sijoittelu/tulos/resource/sijoittelu-basedata.json", "/fi/vm/sade/sijoittelu/tulos/resource/hyvaksytty-ei-valintatulosta.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void valintaTuloksenMuutoslogi() {
        vastaanottoService.vastaanota(hakuOid, hakemusOid, hakukohdeOid, ValintatuloksenTila.VASTAANOTTANUT, muokkaaja, selite);
        Valintatulos valintatulos = valintatulosDao.loadValintatulos(hakukohdeOid, "14090336922663576781797489829887", hakemusOid);
        final List<LogEntry> logEntries = valintatulos.getLogEntries();
        assertEquals(1, logEntries.size());
        final LogEntry logEntry = logEntries.get(0);
        assertEquals("VASTAANOTTANUT", logEntry.getMuutos());
        assertEquals(selite, logEntry.getSelite());
        assertEquals(muokkaaja, logEntry.getMuokkaaja());
        assertEquals(new LocalDate(), new LocalDate(logEntry.getLuotu()));
    }

    @Test
    @UsingDataSet(locations = {"/fi/vm/sade/sijoittelu/tulos/resource/sijoittelu-basedata.json", "/fi/vm/sade/sijoittelu/tulos/resource/hyvaksytty-ei-valintatulosta.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void uusiValintatulosPeru() {
        vastaanottoService.vastaanota(hakuOid, hakemusOid, hakukohdeOid, ValintatuloksenTila.PERUNUT, muokkaaja, selite);
        assertEquals(YhteenvedonVastaanottotila.PERUNUT, getYhteenveto().hakutoiveet.get(0).vastaanottotila);
    }

    @Test(expected = IllegalArgumentException.class)
    @UsingDataSet(locations = {"/fi/vm/sade/sijoittelu/tulos/resource/sijoittelu-basedata.json", "/fi/vm/sade/sijoittelu/tulos/resource/hyvaksytty-ylempi-varalla.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void vastaanotaEhdollisestiKunAikaparametriEiLauennut() throws JsonProcessingException, ParseException {
        vastaanottoService.vastaanota(hakuOid, hakemusOid, hakukohdeOid, ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT, muokkaaja, selite);
    }

    @Test
    @UsingDataSet(locations = {"/fi/vm/sade/sijoittelu/tulos/resource/sijoittelu-basedata.json", "/fi/vm/sade/sijoittelu/tulos/resource/hyvaksytty-ylempi-varalla.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void vastaanotaEhdollisestiKunAikaparametriLauennut() throws JsonProcessingException, ParseException {
        useFixedDate("15.8.2014");
        assertEquals(YhteenvedonValintaTila.VARALLA, getYhteenveto().hakutoiveet.get(0).valintatila);
        assertEquals(YhteenvedonValintaTila.HYVAKSYTTY, getYhteenveto().hakutoiveet.get(1).valintatila);
        vastaanottoService.vastaanota(hakuOid, hakemusOid, hakukohdeOid, ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT, muokkaaja, selite);
        assertEquals(YhteenvedonValintaTila.HYVAKSYTTY, getYhteenveto().hakutoiveet.get(1).valintatila);
        assertEquals(YhteenvedonVastaanottotila.EHDOLLISESTI_VASTAANOTTANUT, getYhteenveto().hakutoiveet.get(1).vastaanottotila);
        assertEquals(YhteenvedonVastaanottotila.KESKEN, getYhteenveto().hakutoiveet.get(0).vastaanottotila);
        assertEquals(YhteenvedonValintaTila.VARALLA, getYhteenveto().hakutoiveet.get(0).valintatila);
    }

    @Test
    @UsingDataSet(locations = {"/fi/vm/sade/sijoittelu/tulos/resource/sijoittelu-basedata.json", "/fi/vm/sade/sijoittelu/tulos/resource/hyvaksytty-ylempi-varalla.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void vastaanotaSitovastiKunAikaparametriLauennut() throws JsonProcessingException, ParseException {
        useFixedDate("15.8.2014");
        vastaanottoService.vastaanota(hakuOid, hakemusOid, hakukohdeOid, ValintatuloksenTila.VASTAANOTTANUT, muokkaaja, selite);
        final HakemusYhteenvetoDTO yhteenveto = getYhteenveto();
        assertEquals(YhteenvedonValintaTila.PERUUNTUNUT, yhteenveto.hakutoiveet.get(0).valintatila);
    }

    private void useFixedDate(final String date) throws ParseException {
        DateTimeUtils.setCurrentMillisFixed(new SimpleDateFormat("d.M.yyyy").parse(date).getTime());
    }

    @After
    public void resetTime() {
        DateTimeUtils.setCurrentMillisSystem();
    }

    @Test(expected = IllegalArgumentException.class)
    @UsingDataSet(locations = {"/fi/vm/sade/sijoittelu/tulos/resource/sijoittelu-basedata.json", "/fi/vm/sade/sijoittelu/tulos/resource/hyvaksytty-vastaanottanut.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void vastaanotaAiemminVastaanotettu() throws JsonProcessingException, ParseException {
        vastaanottoService.vastaanota(hakuOid, hakemusOid, hakukohdeOid, ValintatuloksenTila.VASTAANOTTANUT, muokkaaja, selite);
    }

    @Test(expected = IllegalArgumentException.class)
    @UsingDataSet(locations = {"/fi/vm/sade/sijoittelu/tulos/resource/sijoittelu-basedata.json", "/fi/vm/sade/sijoittelu/tulos/resource/hyvaksytty-ei-valintatulosta.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void hakemustaEiLöydy() {
        vastaanottoService.vastaanota(hakuOid, hakemusOid + 1, hakukohdeOid, ValintatuloksenTila.VASTAANOTTANUT, muokkaaja, selite);
    }

    @Test(expected = IllegalArgumentException.class)
    @UsingDataSet(locations = {"/fi/vm/sade/sijoittelu/tulos/resource/sijoittelu-basedata.json", "/fi/vm/sade/sijoittelu/tulos/resource/hyvaksytty-ei-valintatulosta.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void hakukohdettaEiLöydy() {
        vastaanottoService.vastaanota(hakuOid, hakemusOid, hakukohdeOid + 1, ValintatuloksenTila.VASTAANOTTANUT, muokkaaja, selite);
    }

    @Test
    @UsingDataSet(locations = {"/fi/vm/sade/sijoittelu/tulos/resource/sijoittelu-basedata.json", "/fi/vm/sade/sijoittelu/tulos/resource/hyvaksytty-ilmoitettu.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void vastaanotaIlmoitettu() {
        vastaanottoService.vastaanota(hakuOid, hakemusOid, "1.2.246.562.5.72607738902", ValintatuloksenTila.VASTAANOTTANUT, muokkaaja, selite);
        assertEquals(YhteenvedonVastaanottotila.VASTAANOTTANUT, getYhteenveto().hakutoiveet.get(0).vastaanottotila);
    }

    @Test(expected = IllegalArgumentException.class)
    @UsingDataSet(locations = {"/fi/vm/sade/sijoittelu/tulos/resource/sijoittelu-basedata.json", "/fi/vm/sade/sijoittelu/tulos/resource/hyvaksytty-vastaanottanut-ehdollisesti.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void vastaanotaEhdollisestiVastaanotettu() {
        vastaanottoService.vastaanota(hakuOid, hakemusOid, hakukohdeOid, ValintatuloksenTila.VASTAANOTTANUT, muokkaaja, selite);
    }


    private HakemusYhteenvetoDTO getYhteenveto() {
        return sijoitteluResource.hakemusYhteenveto(hakuOid, "latest", hakemusOid);
    }
}
