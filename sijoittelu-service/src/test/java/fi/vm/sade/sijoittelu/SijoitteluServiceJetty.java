package fi.vm.sade.sijoittelu;

import fi.vm.sade.integrationtest.util.PortChecker;
import fi.vm.sade.integrationtest.util.ProjectRootFinder;
import fi.vm.sade.integrationtest.util.SpringProfile;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public static void startShared() {
        SpringProfile.setProfile("test");
        String publicServerUrl = Optional.ofNullable(System.getProperty("public_server")).orElse("http://localhost");
        String vtsServerUrl = Optional.ofNullable(System.getProperty("vts_server")).orElse("http://localhost");
        System.setProperty("root.organisaatio.oid","");
        System.setProperty("valintalaskentakoostepalvelu.valintaperusteet.rest.url", "http://localhost");
        System.setProperty("host.ilb", "http://localhost");
        System.setProperty("valintalaskentakoostepalvelu.parametriservice.rest.url", publicServerUrl);
        System.setProperty("valintalaskentakoostepalvelu.oppijantunnistus.rest.url", publicServerUrl);
        System.setProperty("valintalaskentakoostepalvelu.tarjonta.rest.url", publicServerUrl);
        System.setProperty("valintalaskentakoostepalvelu.valinta-tulos-service.rest.url", vtsServerUrl + "/valinta-tulos-service");
        System.setProperty("sijoittelu-service.swagger.basepath", resourcesAddress);
        System.setProperty("cas.callback.sijoittelu-service", "http://localhost");
        System.setProperty("cas.service.sijoittelu-service", "http://localhost");
        System.setProperty("cas.service.organisaatio-service", "http://localhost");
        System.setProperty("sijoittelu-service.mongodb.dbname", Optional.ofNullable(System.getProperty("sijoitteludbName")).orElse("sijoitteludb"));
        System.setProperty("valintalaskenta-laskenta-service.mongodb.dbname", "valintalaskentadb");
        System.setProperty("valintalaskenta-laskenta-service.mongodb.uri", Optional.ofNullable(System.getProperty("valintalaskentaMongoUri")).orElse(""));
        System.setProperty("sijoittelu-service.mongodb.uri", System.getProperty("sijoitteluMongoUri"));
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
