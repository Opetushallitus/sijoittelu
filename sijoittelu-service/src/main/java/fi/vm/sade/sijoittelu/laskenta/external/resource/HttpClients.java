package fi.vm.sade.sijoittelu.laskenta.external.resource;

import fi.vm.sade.javautils.cas.ApplicationSession;
import fi.vm.sade.javautils.cas.CasSession;
import fi.vm.sade.sijoittelu.laskenta.util.UrlProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class HttpClients {
    public static final String CALLER_ID = "1.2.246.562.10.00000000001.sijoittelu.sijoittelu-service";

    private static HttpClient.Builder defaultHttpClientBuilder(CookieManager cookieManager) {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(10))
                .cookieHandler(cookieManager);
    }

    @Bean
    public CookieManager getCookieManager() {
        return new CookieManager();
    }

    @Bean(name = "CasHttpClient")
    @Autowired
    public HttpClient getCasHttpClient(CookieManager cookieManager) {
        return defaultHttpClientBuilder(cookieManager).build();
    }

    @Bean(name = "KoutaInternalHttpClient")
    @Autowired
    public HttpClient getKoutaInternalApplicationSession(
            UrlProperties urlProperties,
            @Qualifier("CasHttpClient") HttpClient casHttpClient,
            CookieManager cookieManager,
            @Value("${sijoittelu-service.kouta-internal.username}") String username,
            @Value("${sijoittelu-service.kouta-internal.password}") String password) {
        String ticketsUrl = urlProperties.url("cas.tickets");
        String service = urlProperties.url("kouta-internal.auth.login");
        HttpClient client = defaultHttpClientBuilder(cookieManager).build();
        return new CasAuthenticatingHttpClient(
                client,
                new ApplicationSession(
                        client,
                        cookieManager,
                        CALLER_ID,
                        Duration.ofSeconds(10),
                        new CasSession(
                                casHttpClient,
                                Duration.ofSeconds(10),
                                CALLER_ID,
                                URI.create(ticketsUrl),
                                username,
                                password),
                        service,
                        "session")
        );
    }

}
