package fi.vm.sade.sijoittelu.tulos.resource;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.core.LoadStrategyEnum;

import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaDTO;

public class SijoitteluResourceHakemusTest extends SijoitteluResourceTest {
    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    SijoitteluResource sijoitteluResource;

    @Test
    @UsingDataSet(locations = {"sijoittelu-basedata.json", "sijoittelu-tulos-mockdata.json"}, loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void resultForApplication() throws JsonProcessingException {
        String expected = "{\"hakijaOid\":\"1.2.246.562.24.14229104472\",\"hakemusOid\":\"1.2.246.562.11.00000441369\",\"etunimi\":\"Teppo\",\"sukunimi\":\"Testaaja\",\"hakutoiveet\":[{\"hakutoive\":1,\"hakukohdeOid\":\"1.2.246.562.5.72607738902\",\"tarjoajaOid\":\"1.2.246.562.10.591352080610\",\"pistetiedot\":[],\"hakutoiveenValintatapajonot\":[{\"valintatapajonoPrioriteetti\":1,\"valintatapajonoOid\":\"14090336922663576781797489829886\",\"valintatapajonoNimi\":\"Varsinainen jono\",\"jonosija\":1,\"paasyJaSoveltuvuusKokeenTulos\":null,\"varasijanNumero\":null,\"tila\":\"HYVAKSYTTY\",\"tilanKuvaukset\":{},\"vastaanottotieto\":\"ILMOITETTU\",\"ilmoittautumisTila\":\"EI_TEHTY\",\"hyvaksyttyHarkinnanvaraisesti\":false,\"tasasijaJonosija\":1,\"pisteet\":4,\"alinHyvaksyttyPistemaara\":4.0,\"hakeneet\":1,\"hyvaksytty\":1,\"varalla\":0,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"varasijojaKaytetaanAlkaen\":1409069123943,\"varasijojaTaytetaanAsti\":1409069123943,\"tayttojono\":null}],\"kaikkiJonotSijoiteltu\":true}]}";
        HakijaDTO hakemus = sijoitteluResource.hakemus(hakuOid, sijoitteluAjoId, hakemusOid);
        assertEquals("Teppo", hakemus.getEtunimi());
        assertEquals(expected, objectMapper.writeValueAsString(hakemus));
    }
}
