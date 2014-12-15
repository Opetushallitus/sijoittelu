package tomcatrunner;

import javax.servlet.ServletException;

import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

public class TomcatRunner {
    public final static void main(String... args) throws ServletException, LifecycleException {
        Tomcat tomcat = new Tomcat();
        String webappDirLocation = ProjectRootFinder.findProjectRoot() + "/sijoittelu/sijoittelu-service/src/main/webapp/";

        Context webContext = tomcat.addWebapp("/sijoittelu-service", webappDirLocation);

        // use it-profile-web.xml instead of web.xml
        webContext.getServletContext().setAttribute(Globals.ALT_DD_ATTR, ProjectRootFinder.findProjectRoot() + "/sijoittelu/sijoittelu-service/src/test/resources/it-profile-web.xml");

        tomcat.setPort(8080);

        tomcat.start();
        tomcat.getServer().await();
    }
}