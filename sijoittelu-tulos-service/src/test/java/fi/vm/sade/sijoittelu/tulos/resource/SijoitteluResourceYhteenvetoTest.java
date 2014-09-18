package fi.vm.sade.sijoittelu.tulos.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.core.LoadStrategyEnum;

import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakemusYhteenvetoDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakutoiveYhteenvetoDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.Vastaanotettavuustila;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.YhteenvedonValintaTila;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.YhteenvedonVastaanottotila;

public class SijoitteluResourceYhteenvetoTest extends SijoitteluResourceTest {
    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    SijoitteluResource sijoitteluResource;

    @Test
    @UsingDataSet(locations = {"sijoittelu-basedata.json", "hyvaksytty-ilmoitettu.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void hyvaksytty() throws JsonProcessingException {
        String expectedResponse = "{\"hakemusOid\":\"1.2.246.562.11.00000441369\",\"hakutoiveet\":[{\"hakukohdeOid\":\"1.2.246.562.5.72607738902\",\"tarjoajaOid\":\"1.2.246.562.10.591352080610\",\"valintatila\":\"HYVAKSYTTY\",\"vastaanottotila\":\"KESKEN\",\"ilmoittautumistila\":\"EI_TEHTY\",\"vastaanotettavuustila\":\"VASTAANOTETTAVISSA_SITOVASTI\",\"jonosija\":1,\"varasijojaKaytetaanAlkaen\":1409069123943,\"varasijojaTaytetaanAsti\":1409069123943,\"varasijanumero\":null,\"julkaistavissa\":true}]}";
        HakemusYhteenvetoDTO yhteenveto = getYhteenveto();
        assertEquals(expectedResponse, objectMapper.writeValueAsString(yhteenveto));
        checkHakutoiveState(yhteenveto.hakutoiveet.get(0), YhteenvedonValintaTila.HYVAKSYTTY, YhteenvedonVastaanottotila.KESKEN, Vastaanotettavuustila.VASTAANOTETTAVISSA_SITOVASTI, true);
    }

    @Test
    @UsingDataSet(locations = {"sijoittelu-basedata.json", "hyvaksytty-ei-valintatulosta.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void hyvaksyttyEiValintatulosta() throws JsonProcessingException {
        HakemusYhteenvetoDTO yhteenveto = getYhteenveto();
        checkHakutoiveState(yhteenveto.hakutoiveet.get(0), YhteenvedonValintaTila.HYVAKSYTTY, YhteenvedonVastaanottotila.KESKEN, Vastaanotettavuustila.VASTAANOTETTAVISSA_SITOVASTI, false);
        assertFalse(yhteenveto.hakutoiveet.get(0).julkaistavissa);
    }

    @Test
    @UsingDataSet(locations = {"sijoittelu-basedata.json", "hyvaksytty-ylempi-sijoittelematon.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void hyvaksyttyYlempiSijoittelematon() throws JsonProcessingException {
        HakemusYhteenvetoDTO yhteenveto = getYhteenveto();
        checkHakutoiveState(yhteenveto.hakutoiveet.get(0), YhteenvedonValintaTila.KESKEN, YhteenvedonVastaanottotila.KESKEN, Vastaanotettavuustila.EI_VASTAANOTETTAVISSA, false);
    }

    @Test
    @UsingDataSet(locations = {"sijoittelu-basedata.json", "hyvaksytty-valintatulos-peruutettu.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void hyvaksyttyValintatulosPeruutettu() throws JsonProcessingException {
        HakemusYhteenvetoDTO yhteenveto = getYhteenveto();
        checkHakutoiveState(yhteenveto.hakutoiveet.get(0), YhteenvedonValintaTila.PERUUTETTU, YhteenvedonVastaanottotila.PERUUTETTU, Vastaanotettavuustila.EI_VASTAANOTETTAVISSA, true);
    }

    @Test
    @UsingDataSet(locations = {"sijoittelu-basedata.json", "hyvaksytty-valintatulos-perunut.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void hyvaksyttyValintatulosPerunut() throws JsonProcessingException {
        HakemusYhteenvetoDTO yhteenveto = getYhteenveto();
        checkHakutoiveState(yhteenveto.hakutoiveet.get(0), YhteenvedonValintaTila.PERUNUT, YhteenvedonVastaanottotila.PERUNUT, Vastaanotettavuustila.EI_VASTAANOTETTAVISSA, true);
    }

    @Test
    @UsingDataSet(locations = {"sijoittelu-basedata.json", "hyvaksytty-ylempi-sijoiteltu.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void hyvaksyttyYlemmatSijoiteltu() throws JsonProcessingException {
        HakemusYhteenvetoDTO yhteenveto = getYhteenveto();
        checkHakutoiveState(yhteenveto.hakutoiveet.get(0), YhteenvedonValintaTila.HYLATTY, YhteenvedonVastaanottotila.KESKEN, Vastaanotettavuustila.EI_VASTAANOTETTAVISSA, false);
        checkHakutoiveState(yhteenveto.hakutoiveet.get(1), YhteenvedonValintaTila.HYVAKSYTTY, YhteenvedonVastaanottotila.KESKEN, Vastaanotettavuustila.VASTAANOTETTAVISSA_SITOVASTI, false);
    }

    @Test
    @UsingDataSet(locations = {"sijoittelu-basedata.json", "hyvaksytty-vastaanottanut.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void hyvaksyttyVastaanottanut() throws JsonProcessingException {
        HakemusYhteenvetoDTO yhteenveto = getYhteenveto();
        checkHakutoiveState(yhteenveto.hakutoiveet.get(0), YhteenvedonValintaTila.HYVAKSYTTY, YhteenvedonVastaanottotila.VASTAANOTTANUT, Vastaanotettavuustila.EI_VASTAANOTETTAVISSA, true);
    }

    @Test
    @UsingDataSet(locations = {"sijoittelu-basedata.json", "hyvaksytty-vastaanottanut-ehdollisesti.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void hyvaksyttyVastaanottanutEhdollisesti() throws JsonProcessingException {
        HakemusYhteenvetoDTO yhteenveto = getYhteenveto();
        checkHakutoiveState(yhteenveto.hakutoiveet.get(0), YhteenvedonValintaTila.HYVAKSYTTY, YhteenvedonVastaanottotila.EHDOLLISESTI_VASTAANOTTANUT, Vastaanotettavuustila.EI_VASTAANOTETTAVISSA, true);
    }


    @Test
    @UsingDataSet(locations = {"sijoittelu-basedata.json", "hyvaksytty-ylempi-varalla.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void hyvaksyttyYlempiVaralla() throws JsonProcessingException {
        HakemusYhteenvetoDTO yhteenveto = getYhteenveto();
        checkHakutoiveState(yhteenveto.hakutoiveet.get(0), YhteenvedonValintaTila.VARALLA, YhteenvedonVastaanottotila.KESKEN, Vastaanotettavuustila.EI_VASTAANOTETTAVISSA, false);
        checkHakutoiveState(yhteenveto.hakutoiveet.get(1), YhteenvedonValintaTila.HYVAKSYTTY, YhteenvedonVastaanottotila.KESKEN, Vastaanotettavuustila.EI_VASTAANOTETTAVISSA, false);
    }

    @Test
    @UsingDataSet(locations = {"sijoittelu-basedata.json", "hyvaksytty-ylempi-varalla.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void varallaKäytetäänParastaVarasijaa() throws JsonProcessingException {
        HakemusYhteenvetoDTO yhteenveto = getYhteenveto();
        assertEquals(new Integer(2), yhteenveto.hakutoiveet.get(0).varasijanumero);
    }

    @Test
    @UsingDataSet(locations = {"sijoittelu-basedata.json", "hyvaksytty-ylempi-varalla.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void varasijojenKasittelypaivamaaratNaytetaan() throws JsonProcessingException {
        HakemusYhteenvetoDTO yhteenveto = getYhteenveto();
        assertEquals(new DateTime("2014-08-01T16:00:00.000Z").toDate(), yhteenveto.hakutoiveet.get(1).varasijojaKaytetaanAlkaen);
        assertEquals(new DateTime("2014-08-31T16:00:00.000Z").toDate(), yhteenveto.hakutoiveet.get(1).varasijojaTaytetaanAsti);
    }

    @Test
    @UsingDataSet(locations = {"sijoittelu-basedata.json", "hyvaksytty-ylempi-varalla.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void hyvaksyttyYlempiVarallaAikaparametriLauennut() throws JsonProcessingException, ParseException {
        DateTimeUtils.setCurrentMillisFixed(new SimpleDateFormat("d.M.yyyy").parse("15.8.2014").getTime());
        try {
            HakemusYhteenvetoDTO yhteenveto = getYhteenveto();
            checkHakutoiveState(yhteenveto.hakutoiveet.get(0), YhteenvedonValintaTila.VARALLA, YhteenvedonVastaanottotila.KESKEN, Vastaanotettavuustila.EI_VASTAANOTETTAVISSA, false);
            checkHakutoiveState(yhteenveto.hakutoiveet.get(1), YhteenvedonValintaTila.HYVAKSYTTY, YhteenvedonVastaanottotila.KESKEN, Vastaanotettavuustila.VASTAANOTETTAVISSA_EHDOLLISESTI, false);
        } finally {
            DateTimeUtils.setCurrentMillisSystem();
        }
    }

    @Test
    @UsingDataSet(locations = {"sijoittelu-basedata.json", "varalla-valintatulos-ilmoitettu.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void varallaValintatulosIlmoitettu() throws JsonProcessingException {  // <- legacy-tila ILMOITETTU
        HakemusYhteenvetoDTO yhteenveto = getYhteenveto();
        checkHakutoiveState(yhteenveto.hakutoiveet.get(0), YhteenvedonValintaTila.HYVAKSYTTY, YhteenvedonVastaanottotila.KESKEN, Vastaanotettavuustila.EI_VASTAANOTETTAVISSA, true);
    }


    @Test
    @UsingDataSet(locations = {"sijoittelu-basedata.json", "hylatty-jonoja-kesken.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void hakutoiveHylattyKunSijoitteluKesken() {
        HakutoiveYhteenvetoDTO hakuToive = getHakuToive();
        checkHakutoiveState(hakuToive, YhteenvedonValintaTila.KESKEN, YhteenvedonVastaanottotila.KESKEN, Vastaanotettavuustila.EI_VASTAANOTETTAVISSA, false);
    }

    @Test
    @UsingDataSet(locations = {"sijoittelu-basedata.json", "hylatty-jonot-valmiit.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void hakutoiveHylattyKunSijoitteluValmis() {
        HakutoiveYhteenvetoDTO hakuToive = getHakuToive();
        checkHakutoiveState(hakuToive, YhteenvedonValintaTila.HYLATTY, YhteenvedonVastaanottotila.KESKEN, Vastaanotettavuustila.EI_VASTAANOTETTAVISSA, false);
    }

    private void checkHakutoiveState(HakutoiveYhteenvetoDTO hakuToive, YhteenvedonValintaTila expectedTila, YhteenvedonVastaanottotila vastaanottoTila, Vastaanotettavuustila vastaanotettavuustila, final boolean julkaistavissa) {
        assertEquals(expectedTila, hakuToive.valintatila);
        assertEquals(vastaanottoTila, hakuToive.vastaanottotila);
        assertEquals(vastaanotettavuustila, hakuToive.vastaanotettavuustila);
        assertEquals(julkaistavissa, hakuToive.julkaistavissa);
    }

    private HakutoiveYhteenvetoDTO getHakuToive() {
        HakemusYhteenvetoDTO yhteenveto = getYhteenveto();
        return yhteenveto.hakutoiveet.get(0);
    }

    private HakemusYhteenvetoDTO getYhteenveto() {
        return sijoitteluResource.hakemusYhteenveto(hakuOid, sijoitteluAjoId, hakemusOid);
    }
}
