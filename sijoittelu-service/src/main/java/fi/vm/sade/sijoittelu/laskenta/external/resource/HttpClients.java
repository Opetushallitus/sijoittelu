package fi.vm.sade.sijoittelu.laskenta.external.resource;

import fi.vm.sade.javautils.nio.cas.CasClient;
import fi.vm.sade.javautils.nio.cas.CasClientBuilder;
import fi.vm.sade.javautils.nio.cas.CasConfig;
import fi.vm.sade.sijoittelu.laskenta.util.UrlProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static fi.vm.sade.sijoittelu.configuration.SijoitteluServiceConfiguration.CALLER_ID;
import static fi.vm.sade.valinta.sharedutils.http.HttpResource.CSRF_VALUE;

@Configuration
public class HttpClients {
    @Bean(name = "KoutaInternaCasClient")
    @Autowired
    public CasClient getKoutaInternalCasClient(
            UrlProperties urlProperties,
            @Value("${sijoittelu-service.kouta-internal.username}") String username,
            @Value("${sijoittelu-service.kouta-internal.password}") String password) {
        String ticketsUrl = urlProperties.url("cas.tickets.url");
        String service = urlProperties.url("kouta-internal.service");
        CasConfig casConfig = new CasConfig.CasConfigBuilder(
                username,
                password,
                ticketsUrl,
                service,
                CSRF_VALUE,
                CALLER_ID,
                "/auth/login"
        )
        .setJsessionName("session").build();
        return CasClientBuilder.build(casConfig);
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
        CasConfig casConfig = new CasConfig.CasConfigBuilder(
                username,
                password,
                ticketsUrl,
                service,
                CSRF_VALUE,
                CALLER_ID,
                "/j_spring_cas_security_check"
        )
        .setJsessionName("JSESSIONID").build();
        return CasClientBuilder.build(casConfig);
    }
}

