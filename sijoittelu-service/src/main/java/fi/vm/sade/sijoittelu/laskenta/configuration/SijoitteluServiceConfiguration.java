package fi.vm.sade.sijoittelu.laskenta.configuration;

import fi.vm.sade.sijoittelu.laskenta.mapping.SijoitteluModelMapper;
import fi.vm.sade.valintalaskenta.tulos.logging.LaskentaAuditLogImpl;
import fi.vm.sade.valintalaskenta.tulos.mapping.ValintalaskentaModelMapper;
import org.springframework.context.annotation.*;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
@ComponentScan(basePackages = {
    "fi.vm.sade.valintalaskenta.tulos.dao",
    "fi.vm.sade.valintalaskenta.tulos.service.impl",
    "fi.vm.sade.valintalaskenta.tulos.service.impl.converters"
})
public class SijoitteluServiceConfiguration {

    public static final String CALLER_ID = "1.2.246.562.10.00000000001.sijoittelu.sijoittelu-service";

    @Bean
    public LaskentaAuditLogImpl laskentaAuditLog() {
        return new LaskentaAuditLogImpl();
    }

    @Bean
    public ValintalaskentaModelMapper valintalaskentaModelMapper() {
        return new ValintalaskentaModelMapper();
    }

    @Bean
    public SijoitteluModelMapper sijoitteluModelMapper() {
        return new SijoitteluModelMapper();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/").allowedOrigins("*");
            }
        };
    }
}