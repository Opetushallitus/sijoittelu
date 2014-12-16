package fi.vm.sade.sijoittelu.laskenta.resource;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Test;

import tomcatrunner.SharedTomcat;

public class TilaResourceTest {
    @Before
    public void startServer() {
        SharedTomcat.start();
    }

    @Test
    public void smokeTest() throws InterruptedException {
        final String url = "http://localhost:" + SharedTomcat.port + "/sijoittelu-service/resources/tila/1.2.246.562.11.00000441369";
        System.out.println(ClientBuilder.newClient()
            .target(url)
            .request(MediaType.APPLICATION_JSON).get(String.class));
    }
}
