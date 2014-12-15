package tomcatrunner;

public class SpringProfile {
    public final static String activeProfile() {
        return System.getProperty("spring.profiles.active", "default");
    }
}
