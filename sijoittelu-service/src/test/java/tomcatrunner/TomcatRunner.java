package tomcatrunner;

import javax.servlet.ServletException;

import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Server;
import org.apache.catalina.startup.Tomcat;

public class TomcatRunner {
    public final int port;
    private final Tomcat tomcat;

    public final static void main(String... args) throws ServletException, LifecycleException {
        new TomcatRunner(Integer.parseInt(System.getProperty("sijoittelu-service.port", "8080"))).start().await();
    }

    public TomcatRunner(final int port) {
        this.port = port;
        this.tomcat = new Tomcat();
        String webappDirLocation = ProjectRootFinder.findProjectRoot() + "/sijoittelu/sijoittelu-service/src/main/webapp/";
        Context webContext = null;
        try {
            webContext = tomcat.addWebapp("/sijoittelu-service", webappDirLocation);

            if (SpringProfile.activeProfile().equals("it")) {
                // use it-profile-web.xml instead of web.xml
                webContext.getServletContext().setAttribute(Globals.ALT_DD_ATTR, ProjectRootFinder.findProjectRoot() + "/sijoittelu/sijoittelu-service/src/test/resources/it-profile-web.xml");
            }

            tomcat.setPort(port);

        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
    }

    public Server start() {
        try {
            tomcat.start();
            return tomcat.getServer();
        } catch (LifecycleException e) {
            throw new RuntimeException(e);
        }
    }
}