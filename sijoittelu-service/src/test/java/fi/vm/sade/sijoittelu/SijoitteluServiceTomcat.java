package fi.vm.sade.sijoittelu;

import javax.servlet.ServletException;

import org.apache.catalina.LifecycleException;

import fi.vm.sade.valinta.integrationtest.EmbeddedTomcat;
import fi.vm.sade.valinta.integrationtest.ProjectRootFinder;
import fi.vm.sade.valinta.integrationtest.SharedTomcat;

public class SijoitteluServiceTomcat {
    static final String SIJOITTELU_MODULE_ROOT = ProjectRootFinder.findProjectRoot() + "/sijoittelu/sijoittelu-service";
    static final String SIJOITTELU_CONTEXT_PATH = "sijoittelu-service";

    public final static void main(String... args) throws ServletException, LifecycleException {
        new EmbeddedTomcat(Integer.parseInt(System.getProperty("sijoittelu-service.port", "8080")), SIJOITTELU_MODULE_ROOT, SIJOITTELU_CONTEXT_PATH).start().await();
    }

    public static void startShared() {
        SharedTomcat.start(SIJOITTELU_MODULE_ROOT, SIJOITTELU_CONTEXT_PATH);
    }
}