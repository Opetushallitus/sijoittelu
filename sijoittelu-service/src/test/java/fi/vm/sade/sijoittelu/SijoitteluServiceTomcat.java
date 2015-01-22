package fi.vm.sade.sijoittelu;

import javax.servlet.ServletException;

import org.apache.catalina.LifecycleException;

import fi.vm.sade.integrationtest.tomcat.EmbeddedTomcat;
import fi.vm.sade.integrationtest.tomcat.SharedTomcat;
import fi.vm.sade.integrationtest.util.ProjectRootFinder;

public class SijoitteluServiceTomcat extends EmbeddedTomcat {
    static final String SIJOITTELU_MODULE_ROOT = ProjectRootFinder.findProjectRoot() + "/sijoittelu/sijoittelu-service";
    static final String SIJOITTELU_CONTEXT_PATH = "sijoittelu-service";

    public final static void main(String... args) throws ServletException, LifecycleException {
        new SijoitteluServiceTomcat(Integer.parseInt(System.getProperty("sijoittelu-service.port", "8095"))).start().await();
    }

    public SijoitteluServiceTomcat(int port) {
        super(port, SIJOITTELU_MODULE_ROOT, SIJOITTELU_CONTEXT_PATH);
    }

    public static void startShared() {
        SharedTomcat.start(SIJOITTELU_MODULE_ROOT, SIJOITTELU_CONTEXT_PATH);
    }
}