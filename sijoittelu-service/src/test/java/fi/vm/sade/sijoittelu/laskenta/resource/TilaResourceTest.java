package fi.vm.sade.sijoittelu.laskenta.resource;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.IlmoittautumisTila;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.domain.dto.ErillishaunHakijaDTO;
import tomcatrunner.SharedTomcat;

public class TilaResourceTest {
    String hakuOid = "1.2.246.562.5.2013080813081926341928";
    String hakukohdeOid = "1.2.246.562.5.72607738902";
    final String hakemusOid = "1.2.246.562.11.00000441369";


    @Before
    public void startServer() {
        SharedTomcat.start();
    }

    @Test
    public void smokeTest() {
        final List<Valintatulos> tulokset = haeTulokset();
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
        hakija.setHakijaOid("1.2.246.562.24.14229104472");
        hakija.setHakukohdeOid(hakukohdeOid);
        hakija.setIlmoittautumisTila(IlmoittautumisTila.EI_ILMOITTAUTUNUT);
        hakija.setJulkaistavissa(true);
        hakija.setTarjoajaOid("1.2.246.562.10.591352080610");
        hakija.setValintatapajonoOid("14090336922663576781797489829886");
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
        assertEquals(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT, haeTulokset().get(0).getTila());
    }

    org.apache.cxf.jaxrs.client.WebClient createClient(String url) {
        JAXRSClientFactoryBean bean = new JAXRSClientFactoryBean();
        bean.setAddress(url);
        bean.setThreadSafe(true);
        List<Object> providers = Lists.newArrayList();
        providers.add(new com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider());
        bean.setProviders(providers);
        return bean.createWebClient();
    }

    private List<Valintatulos> haeTulokset() {
        final String url = "http://localhost:" + SharedTomcat.port + "/sijoittelu-service/resources/tila/" + hakemusOid;
        return createClient(url).accept(MediaType.APPLICATION_JSON).get(new GenericType<List<Valintatulos>>() { });
    }

}
