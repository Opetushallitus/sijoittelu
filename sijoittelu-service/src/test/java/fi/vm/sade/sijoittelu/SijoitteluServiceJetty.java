package fi.vm.sade.sijoittelu;

import fi.vm.sade.integrationtest.util.ProjectRootFinder;
import fi.vm.sade.integrationtest.util.SpringProfile;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Optional;

public class SijoitteluServiceJetty {
    private static final Logger LOG = LoggerFactory.getLogger(SijoitteluServiceJetty.class);

    public final static int port;
    private static Server server;

    static {
        try {
            // -Dport=9090
            Integer portFlagOrZero = Integer.parseInt(Optional.ofNullable(System.getProperty("port")).orElse("0"));
            ServerSocket s = new ServerSocket(portFlagOrZero);
            port = s.getLocalPort();
            s.close();
            server = new Server(port);
            LOG.info("Starting server to port {}", port);
        } catch (IOException e) {
            throw new RuntimeException("free port not found");
        }
    }

    public static void main(String[] args) throws Exception{
        startShared();
    }

    public final static String resourcesAddress = "http://localhost:" + port + "/sijoittelu-service/resources";

    private static void setDefaultSystemProperties() {
        setCommonSystemProperties();
        String publicServerUrl = Optional.ofNullable(System.getProperty("public_server")).orElse("http://localhost");
        String vtsServerUrl = Optional.ofNullable(System.getProperty("vts_server")).orElse("http://localhost");
        System.setProperty("valintalaskentakoostepalvelu.valintaperusteet.rest.url", "http://localhost");
        System.setProperty("host.ilb", "http://localhost");
        System.setProperty("valintalaskentakoostepalvelu.parametriservice.rest.url", publicServerUrl);
        System.setProperty("valintalaskentakoostepalvelu.oppijantunnistus.rest.url", publicServerUrl);
        System.setProperty("valintalaskentakoostepalvelu.tarjonta.rest.url", publicServerUrl);
        System.setProperty("valintalaskentakoostepalvelu.valintatulosservice.rest.url", vtsServerUrl + "/valinta-tulos-service");
        System.setProperty("valintalaskentakoostepalvelu.valinta-tulos-service.rest.url", vtsServerUrl + "/valinta-tulos-service");
    }

    private static void setCommonSystemProperties() {
        System.setProperty("root.organisaatio.oid","");
        System.setProperty("cas.callback.sijoittelu-service", "http://localhost");
        System.setProperty("cas.service.sijoittelu-service", "http://localhost");
        System.setProperty("cas.service.organisaatio-service", "http://localhost");
        System.setProperty("valintalaskenta-laskenta-service.mongodb.dbname", "valintalaskentadb");
        System.setProperty("valintalaskenta-laskenta-service.mongodb.uri", Optional.ofNullable(System.getProperty("valintalaskentaMongoUri")).orElse(""));
        System.setProperty("sijoittelu-service.swagger.basepath", resourcesAddress);
        System.setProperty("omatsivut.email.application.modify.link.en", "https://en.test.domain/token/");
        System.setProperty("omatsivut.email.application.modify.link.fi", "https://fi.test.domain/token/");
        System.setProperty("omatsivut.email.application.modify.link.sv", "https://sv.test.domain/token/");
    }

    private static void setLuokkaSystemProperties() {
        setCommonSystemProperties();
        System.setProperty("host.ilb", "https://itest-virkailija.oph.ware.fi");
        System.setProperty("valintalaskentakoostepalvelu.valintaperusteService.url", "https://itest-virkailija.oph.ware.fi/valintaperusteet-service/services/ws/valintaperusteService");
        System.setProperty("valintalaskentakoostepalvelu.valintalaskentaService.url", "https://itest-virkailija.oph.ware.fi/valintalaskenta-laskenta-service/services/valintalaskentaService");
        System.setProperty("valintalaskentakoostepalvelu.valintaTulosService.url", "https://itest-virkailija.oph.ware.fi/valintalaskenta-laskenta-service/services/valintatietoService");
        System.setProperty("valintalaskentakoostepalvelu.sijoitteluService.url", "https://itest-virkailija.oph.ware.fi/sijoittelu-service/services/sijoitteluService");
        System.setProperty("valintalaskentakoostepalvelu.viestintapalvelu.url", "https://itest-virkailija.oph.ware.fi/viestintapalvelu");
        System.setProperty("valintalaskentakoostepalvelu.tarjontaService.url", "https://itest-virkailija.oph.ware.fi/tarjonta-service/services/tarjontaPublicService");
        System.setProperty("valintalaskentakoostepalvelu.organisaatioService.url", "https://itest-virkailija.oph.ware.fi/organisaatio-service/services/organisaatioService");
        System.setProperty("valintalaskentakoostepalvelu.organisaatioService.rest.url", "https://itest-virkailija.oph.ware.fi/organisaatio-service/rest");
        System.setProperty("valintalaskentakoostepalvelu.hakemusService.url", "https://itest-virkailija.oph.ware.fi/haku-app/services/ws/hakemusService");
        System.setProperty("valintalaskentakoostepalvelu.tarjonta.rest.url", "https://itest-virkailija.oph.ware.fi/tarjonta-service/rest");
        System.setProperty("valintalaskentakoostepalvelu.oppijantunnistus.rest.url", "https://itest-virkailija.oph.ware.fi/oppijan-tunnistus");
        System.setProperty("valintalaskentakoostepalvelu.hakemus.rest.url", "https://itest-virkailija.oph.ware.fi/haku-app");
        System.setProperty("valintalaskentakoostepalvelu.authentication.rest.url", "https://itest-virkailija.oph.ware.fi/authentication-service");
        System.setProperty("valintalaskentakoostepalvelu.koodisto.rest.url", "https://itest-virkailija.oph.ware.fi/koodisto-service/rest");
        System.setProperty("valintalaskentakoostepalvelu.sijoittelu.rest.url", "https://itest-virkailija.oph.ware.fi/sijoittelu-service/resources");
        System.setProperty("valintalaskentakoostepalvelu.valintalaskenta.rest.url", "https://itest-virkailija.oph.ware.fi/valintalaskenta-laskenta-service/resources");
        System.setProperty("valintalaskentakoostepalvelu.koodiService.url", "https://itest-virkailija.oph.ware.fi/koodisto-service/services/koodiService");
        System.setProperty("valintalaskentakoostepalvelu.dokumenttipalvelu.rest.url", "https://itest-virkailija.oph.ware.fi/dokumenttipalvelu-service/resources");
        System.setProperty("valintalaskentakoostepalvelu.swagger.basepath", "/valintalaskentakoostepalvelu/resources");
        System.setProperty("valintalaskentakoostepalvelu.valintaperusteet.rest.url", "https://itest-virkailija.oph.ware.fi/valintaperusteet-service/resources");
        System.setProperty("valintalaskentakoostepalvelu.parametriservice.rest.url", "https://itest-virkailija.oph.ware.fi/ohjausparametrit-service/api");
        System.setProperty("valintalaskentakoostepalvelu.valintaperusteet.ilb.url", "https://itest-virkailija.oph.ware.fi/valintaperusteet-service/resources");
        System.setProperty("valintalaskenta-laskenta-service.mongodb.uri", Optional.ofNullable(System.getProperty("valintalaskentaMongoUri")).orElse(""));

        System.setProperty("valintarekisteri.db.user", "oph");
        System.setProperty("valintarekisteri.db.password", "oph");
        System.setProperty("valintarekisteri.db.url", "jdbc:postgresql://localhost:37242/valintarekisteri");
        System.setProperty("valintarekisteri.tarjonta-service.url", "https://itest-virkailija.oph.ware.fi/tarjonta-service");
        System.setProperty("valintarekisteri.parseleniently.tarjonta", "true");
        System.setProperty("valintarekisteri.koodisto-service.url", "https://itest-virkailija.oph.ware.fi/koodisto-service/");

        System.setProperty("valintalaskentakoostepalvelu.valinta-tulos-service.rest.url", "http://localhost:8097/valinta-tulos-service");
        System.setProperty("valintalaskentakoostepalvelu.valintatulosservice.rest.url", "http://localhost:8097/valinta-tulos-service");
    }

    public static void startShared() {
        SpringProfile.setProfile("test");

        Optional<String> useLuokka = Optional.ofNullable(System.getProperty("useLuokka"));
        if(useLuokka.isPresent() && "TRUE".equalsIgnoreCase(useLuokka.get())) {
            setLuokkaSystemProperties();
        } else {
            setDefaultSystemProperties();
        }

        Optional<String> debugProperties = Optional.ofNullable(System.getProperty("debugProperties"));
        if(debugProperties.isPresent() && "TRUE".equalsIgnoreCase(debugProperties.get())) {
            System.getProperties().keySet().stream().forEach(k -> System.out.println(k + "=" + System.getProperty((String)k)));
        }

        try {
            if (server.isStopped()) {
                String root =  ProjectRootFinder.findProjectRoot() + "/sijoittelu/sijoittelu-service";
                LOG.info("Project root {}", root);
                WebAppContext wac = new WebAppContext();
                wac.setDescriptor(SijoitteluServiceJetty.class.getResource("/test-web.xml").toString());
                wac.setResourceBase(root + "/src/main/webapp");
                wac.setContextPath("/sijoittelu-service");
                wac.setParentLoaderPriority(true);
                server.setHandler(wac);
                server.setStopAtShutdown(true);
                server.start();
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

}
