package fi.vm.sade.sijoittelu.laskenta.external.resource;

import fi.vm.sade.javautils.nio.cas.CasClient;
import fi.vm.sade.javautils.nio.cas.CasConfig;
import fi.vm.sade.sijoittelu.laskenta.util.UrlProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static fi.vm.sade.valinta.sharedutils.http.HttpResource.CSRF_VALUE;

@Configuration
public class HttpClients {
    public static final String CALLER_ID = "1.2.246.562.10.00000000001.sijoittelu.sijoittelu-service";

    @Bean(name = "KoutaInternaCasClient")
    @Autowired
    public CasClient getKoutaInternalCasClient(
            UrlProperties urlProperties,
            @Value("${sijoittelu-service.kouta-internal.username}") String username,
            @Value("${sijoittelu-service.kouta-internal.password}") String password) {
        String ticketsUrl = urlProperties.url("cas.tickets.url");
        String service = urlProperties.url("kouta-internal.service");
        return new CasClient(CasConfig.CasConfig(
                username,
                password,
                ticketsUrl,
                service,
                CSRF_VALUE,
                CALLER_ID,
                "session",
                "/auth/login"
        )
        );
    }

    @Bean(name = "SijoitteluCasClient")
    @Autowired
    public CasClient getSijoitteluCasClient(
            UrlProperties urlProperties,
            @Value("${sijoittelu-service.username.to.valintaperusteet}") String username,
            @Value("${sijoittelu-service.password.to.valintaperusteet}") String password,
            @Value("${cas.session.valintaperusteet}") String sessionUrl) {
        String ticketsUrl = urlProperties.url("cas.tickets.url");
        String service = urlProperties.url("cas.service.valintaperusteet");
        return new CasClient(new CasConfig(
                username,
                password,
                ticketsUrl,
                service,
                CSRF_VALUE,
                CALLER_ID,
                "JSESSIONID",
                "/j_spring_cas_security_check",
                sessionUrl
        )
        );
    }
}

