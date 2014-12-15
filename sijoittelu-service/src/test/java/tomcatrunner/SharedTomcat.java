package tomcatrunner;

public class SharedTomcat {
    private final static TomcatRunner tomcat = new TomcatRunner(PortChecker.findFreeLocalPort());

    public final static int port = tomcat.port;

    public void start() {
        tomcat.start();
    }
}
