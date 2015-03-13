package fi.vm.sade.sijoittelu.laskenta.resource;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import junit.framework.Assert;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.IlmoittautumisTila;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.domain.dto.ErillishaunHakijaDTO;
import fi.vm.sade.valinta.http.HttpResource;
import fi.vm.sade.integrationtest.tomcat.SharedTomcat;
import fi.vm.sade.sijoittelu.SijoitteluServiceTomcat;

public class TilaResourceTest {
    String hakuOid = "1.2.246.562.5.2013080813081926341928";
    String hakukohdeOid = "1.2.246.562.5.72607738902";
    String hakemusOid = "1.2.246.562.11.00000441369";
    String tarjoajaOid = "1.2.246.562.10.591352080610";
    String valintatapajonoOid = "14090336922663576781797489829886";
    String hakijaOid = "1.2.246.562.24.14229104472";


    @Before
    public void startServer() {
        SijoitteluServiceTomcat.startShared();
    }

    @Test
    public void ensureCorsIsSet() throws Exception{
        final String url = "http://localhost:" + SharedTomcat.port + "/sijoittelu-service/resources/tila/" + hakemusOid;
        Response response = createClient(url).accept(MediaType.APPLICATION_JSON).get();
        if(response.getStatus() == 200) {
            MultivaluedMap headers = response.getHeaders();
            Assert.assertTrue("*".equals(headers.getFirst("Access-Control-Allow-Origin")));
        } else {
            // smokeTest hajoo ja joku muu ongelma kyseessa
        }
    }
    @Test
    public void smokeTest() throws Exception{
        final List<Valintatulos> tulokset = haeTulokset(hakemusOid);
        assertEquals(1, tulokset.size());
    }

    @Test
    public void erillishaunHakijoidenTuonti() {
        final ErillishaunHakijaDTO hakija = new ErillishaunHakijaDTO();
        hakija.setEtunimi("etu");
        hakija.setSukunimi("suku");
        hakija.setHakemuksenTila(HakemuksenTila.HYLATTY);
        hakija.setHakuOid(hakuOid);
        hakija.setHakemusOid(hakemusOid);
        hakija.setHakijaOid(hakijaOid);
        hakija.setHakukohdeOid(hakukohdeOid);
        hakija.setIlmoittautumisTila(IlmoittautumisTila.EI_ILMOITTAUTUNUT);
        hakija.setJulkaistavissa(true);
        hakija.setTarjoajaOid(tarjoajaOid);
        hakija.setValintatapajonoOid(valintatapajonoOid);
        hakija.setValintatuloksenTila(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT);
        List<ErillishaunHakijaDTO> hakijat = Arrays.asList(hakija);
        final String url = "http://localhost:" + SharedTomcat.port + "/sijoittelu-service/resources/tila/erillishaku/"+hakuOid+"/hakukohde/"+hakukohdeOid;
        final Response response = createClient(url)
            .query("valintatapajononNimi", "varsinainen jono")
            .type(MediaType.APPLICATION_JSON_TYPE)
            .accept(MediaType.APPLICATION_JSON_TYPE)
            .post(Entity.entity(hakijat, MediaType.APPLICATION_JSON));
        assertEquals(202, response.getStatus());

        // Tarkistetaan, että tila meni kantaan
        assertEquals(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT, haeTulokset(hakemusOid).get(0).getTila());
    }

    @Test
    public void tulostenPoisto() {
        final ErillishaunHakijaDTO hakija = new ErillishaunHakijaDTO();
        hakija.setEtunimi("etunimi");
        hakija.setSukunimi("sukunimi");
        hakija.setHakemuksenTila(HakemuksenTila.HYLATTY);
        hakija.setHakuOid("haku1");
        hakija.setHakemusOid("hakemus1");
        hakija.setHakijaOid("hakija1");
        hakija.setHakukohdeOid("hakukohde1");
        hakija.setIlmoittautumisTila(IlmoittautumisTila.EI_ILMOITTAUTUNUT);
        hakija.setJulkaistavissa(true);
        hakija.setTarjoajaOid("tarjoaja1");
        hakija.setValintatapajonoOid("jono1");
        hakija.setValintatuloksenTila(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT);
        List<ErillishaunHakijaDTO> hakijat = Arrays.asList(hakija);
        final String url = "http://localhost:" + SharedTomcat.port + "/sijoittelu-service/resources/tila/erillishaku/"+hakuOid+"/hakukohde/"+hakukohdeOid;
        final Response response = createClient(url)
                .query("valintatapajononNimi", "varsinainen jono")
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(hakijat, MediaType.APPLICATION_JSON));

        assertEquals(202, response.getStatus());

        assertEquals(1, haeTulokset("hakemus1").size());
        assertEquals("hakemus1", haeHakukohde("haku1", "hakukohde1").getValintatapajonot().stream().filter(j -> j.getOid().equals("jono1")).findFirst().get().getHakemukset().get(0).getHakemusOid());

        hakija.setPoistetaankoTulokset(true);
        hakijat = Arrays.asList(hakija);

        final Response response2 = createClient(url)
                .query("valintatapajononNimi", "varsinainen jono")
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(hakijat, MediaType.APPLICATION_JSON));

        assertEquals(202, response2.getStatus());


        // Tarkistetaan, että poistui kannasta
        assertEquals(0, haeTulokset("hakemus1").size());
        assertEquals(0, haeHakukohde("haku1", "hakukohde1").getValintatapajonot().stream().filter(j -> j.getOid().equals("jono1")).findFirst().get().getHakemukset().size());
    }

    private List<Valintatulos> haeTulokset(String hakemusOid) {
        final String url = "http://localhost:" + SharedTomcat.port + "/sijoittelu-service/resources/tila/" + hakemusOid;
        return createClient(url).accept(MediaType.APPLICATION_JSON).get(new GenericType<List<Valintatulos>>() { });
    }

    private HakukohdeDTO haeHakukohde(String hakuOid, String hakukohdeOid) {
        final String url = "http://localhost:" + SharedTomcat.port + "/sijoittelu-service/resources/sijoittelu/" + hakuOid + "/sijoitteluajo/latest/hakukohdedto/"+hakukohdeOid;
        return createClient(url).accept(MediaType.APPLICATION_JSON).get(new GenericType<HakukohdeDTO>() { });
    }

    private WebClient createClient(String url) {
        return new HttpResource(url, 1000).getWebClient();
    }
}
