package fi.vm.sade.sijoittelu.laskenta.resource;

import fi.vm.sade.generic.service.exception.NotAuthorizedException;
import fi.vm.sade.integrationtest.tomcat.EmbeddedTomcat;
import fi.vm.sade.integrationtest.tomcat.SharedTomcat;
import fi.vm.sade.sijoittelu.SijoitteluServiceTomcat;
import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.sijoittelu.domain.dto.ErillishaunHakijaDTO;
import fi.vm.sade.sijoittelu.laskenta.external.resource.HakuV1Resource;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.HakuDTO;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ResultHakuDTO;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.sijoittelu.laskenta.service.business.StaleReadException;
import fi.vm.sade.sijoittelu.laskenta.service.exception.HakemustaEiLoytynytException;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.valinta.http.HttpResource;
import junit.framework.Assert;
import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

public class TilaResourceTest {
    String hakuOid = "1.2.246.562.5.2013080813081926341928";
    String hakukohdeOid = "1.2.246.562.5.72607738902";
    String hakemusOid = "1.2.246.562.11.00000441369";
    String tarjoajaOid = "1.2.246.562.10.591352080610";
    String valintatapajonoOid = "14090336922663576781797489829886";
    String hakijaOid = "1.2.246.562.24.14229104472";

    private HakuV1Resource hakuV1Resource;

    @Before
    public void startServer() {
        EmbeddedTomcat tomcat = SijoitteluServiceTomcat.startShared();
        this.hakuV1Resource = (HakuV1Resource)WebApplicationContextUtils.getWebApplicationContext(tomcat.ctx.getServletContext()).getBean("hakuV1Resource");
    }

    @Test
    public void onnistunutMuokkausPalauttaaOk() {
        TilaResource r = new TilaResource();
        r.sijoitteluBusinessService = sijoitteluBusinessServiceMock(new Hakukohde());
        List<Valintatulos> valintatulokset = new ArrayList<>();
        valintatulokset.add(new Valintatulos());

        Response response = r.muutaHakemustenTilaa(hakuOid, hakukohdeOid, valintatulokset, "selite");
        HakukohteenValintatulosUpdateStatuses statuses = (HakukohteenValintatulosUpdateStatuses) response.getEntity();
        assertEquals(200, response.getStatus());
        assertNull("ei viestiä", statuses.message);
        assertTrue("ei virheitä", statuses.statuses.isEmpty());
    }

    @Test
    public void internalServerErrorJosHakukohdettaEiLoydy() {
        TilaResource r = new TilaResource();
        r.sijoitteluBusinessService = sijoitteluBusinessServiceMock(new RuntimeException("Sijoittelua ei löytynyt haulle: " + hakuOid));
        List<Valintatulos> valintatulokset = new ArrayList<>();

        Response response = r.muutaHakemustenTilaa(hakuOid, hakukohdeOid, valintatulokset, "selite");
        HakukohteenValintatulosUpdateStatuses statuses = (HakukohteenValintatulosUpdateStatuses) response.getEntity();
        assertEquals(500, response.getStatus());
        assertEquals("Sijoittelua ei löytynyt haulle: " + hakuOid, statuses.message);
        assertTrue("ei virheitä", statuses.statuses.isEmpty());
    }

    @Test
    public void epaonnistuneidenMuokkaustenVirheetKerataan() {
        TilaResource r = new TilaResource();
        r.sijoitteluBusinessService = new SijoitteluBusinessService(0,null,null,null,null,null,null,null,null,null,null) {
            @Override
            public Hakukohde getHakukohde(String hakuOid, String hakukohdeOid) {
                return new Hakukohde();
            }

            @Override
            public void vaihdaHakemuksenTila(String hakuoid, Hakukohde hakukohde, Valintatulos change, String selite, String muokkaaja) {
                switch (change.getHakemusOid()) {
                    case "1":
                        throw new HakemustaEiLoytynytException(valintatapajonoOid, hakemusOid);
                    case "2":
                        throw new NotAuthorizedException();
                    case "3":
                        throw new StaleReadException(hakuoid, hakukohdeOid, valintatapajonoOid, hakemusOid);
                    case "4":
                        throw new IllegalArgumentException();
                }
            }
        };
        List<Valintatulos> valintatulokset = new ArrayList<>();
        Valintatulos one = new Valintatulos();
        one.setHakemusOid("1", "");
        Valintatulos two = new Valintatulos();
        two.setHakemusOid("2", "");
        Valintatulos three = new Valintatulos();
        three.setHakemusOid("3", "");
        Valintatulos four = new Valintatulos();
        four.setHakemusOid("4", "");
        valintatulokset.add(one);
        valintatulokset.add(two);
        valintatulokset.add(three);
        valintatulokset.add(four);

        Response response = r.muutaHakemustenTilaa(hakuOid, hakukohdeOid, valintatulokset, "selite");
        HakukohteenValintatulosUpdateStatuses statuses = (HakukohteenValintatulosUpdateStatuses) response.getEntity();
        assertEquals(500, response.getStatus());
        assertNull("ei viestiä", statuses.message);
        assertFalse("virheitä", statuses.statuses.isEmpty());
        Map<String, Integer> errors = statuses.statuses.stream()
                .collect(Collectors.toMap(status -> status.hakemusOid, status -> status.status));
        assertEquals(new Integer(404), errors.get("1"));
        assertEquals(new Integer(401), errors.get("2"));
        assertEquals(new Integer(409), errors.get("3"));
        assertEquals(new Integer(400), errors.get("4"));
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
        when(hakuV1Resource.findByOid(hakuOid))
                .thenReturn(new ResultHakuDTO() {{
                    setResult(new HakuDTO() {{
                        setKohdejoukkoUri("haunkohdejoukko_12#1");
                    }});
                }});
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
        assertEquals(200, response.getStatus());
    }

    @Test
    public void tulostenPoisto() {
        when(hakuV1Resource.findByOid(hakuOid))
                .thenReturn(new ResultHakuDTO() {{
                    setResult(new HakuDTO() {{
                        setKohdejoukkoUri("haunkohdejoukko_12#1");
                    }});
                }});
        final ErillishaunHakijaDTO hakija = new ErillishaunHakijaDTO();
        hakija.setEtunimi("etunimi");
        hakija.setSukunimi("sukunimi");
        hakija.setHakemuksenTila(HakemuksenTila.HYLATTY);
        hakija.setHakuOid(hakuOid);
        hakija.setHakemusOid("hakemus1");
        hakija.setHakijaOid("hakija1");
        hakija.setHakukohdeOid(hakukohdeOid);
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

        assertEquals(200, response.getStatus());

        assertEquals(1, haeTulokset("hakemus1").size());
        assertEquals("hakemus1", haeHakukohde(hakuOid, hakukohdeOid).getValintatapajonot().stream().filter(j -> j.getOid().equals("jono1")).findFirst().get().getHakemukset().get(0).getHakemusOid());

        hakija.setPoistetaankoTulokset(true);
        hakijat = Arrays.asList(hakija);

        final Response response2 = createClient(url)
                .query("valintatapajononNimi", "varsinainen jono")
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .post(Entity.entity(hakijat, MediaType.APPLICATION_JSON));

        assertEquals(200, response2.getStatus());


        // Tarkistetaan, että poistui kannasta
        assertEquals(0, haeTulokset("hakemus1").size());
        assertEquals(0, haeHakukohde(hakuOid, hakukohdeOid).getValintatapajonot().stream().filter(j -> j.getOid().equals("jono1")).findFirst().get().getHakemukset().size());
    }

    private List<Valintatulos> haeTulokset(String hakemusOid) {
        final String url = "http://localhost:" + SharedTomcat.port + "/sijoittelu-service/resources/tila/" + hakemusOid;
        return createClient(url).accept(MediaType.APPLICATION_JSON).get(new GenericType<List<Valintatulos>>() {
        });
    }

    private HakukohdeDTO haeHakukohde(String hakuOid, String hakukohdeOid) {
        final String url = "http://localhost:" + SharedTomcat.port + "/sijoittelu-service/resources/sijoittelu/" + hakuOid + "/sijoitteluajo/latest/hakukohdedto/"+hakukohdeOid;
        return createClient(url).accept(MediaType.APPLICATION_JSON).get(new GenericType<HakukohdeDTO>() {
        });
    }

    private WebClient createClient(String url) {
        return new HttpResource(url, 15000).getWebClient();
    }

    private static SijoitteluBusinessService sijoitteluBusinessServiceMock(RuntimeException e) {
        return new SijoitteluBusinessService(0,null,null,null,null,null,null,null,null,null,null) {
            @Override
            public Hakukohde getHakukohde(String hakuOid, String hakukohdeOid) {
                throw e;
            }

            @Override
            public void vaihdaHakemuksenTila(String hakuoid, Hakukohde hakukohde, Valintatulos change, String selite, String muokkaaja) {
            }
        };
    }

    private static SijoitteluBusinessService sijoitteluBusinessServiceMock(Hakukohde hakukohde) {
        return new SijoitteluBusinessService(0,null,null,null,null,null,null,null,null,null,null) {
            @Override
            public Hakukohde getHakukohde(String hakuOid, String hakukohdeOid) {
                return hakukohde;
            }

            @Override
            public void vaihdaHakemuksenTila(String hakuoid, Hakukohde hakukohde, Valintatulos change, String selite, String muokkaaja) {
            }
        };
    }
}
