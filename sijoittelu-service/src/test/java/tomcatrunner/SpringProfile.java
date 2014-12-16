package tomcatrunner;

public class SpringProfile {
    public final static String activeProfile() {
        return System.getProperty("spring.profiles.active", "default");
    }

    public static void setProfile(final String profile) {
        System.setProperty("spring.profiles.active", profile);
    }
}

