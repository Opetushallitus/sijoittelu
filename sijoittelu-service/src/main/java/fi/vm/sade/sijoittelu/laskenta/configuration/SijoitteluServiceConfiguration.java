package fi.vm.sade.sijoittelu.laskenta.configuration;

import fi.vm.sade.auditlog.ApplicationType;
import fi.vm.sade.auditlog.Audit;
import fi.vm.sade.sijoittelu.laskenta.mapping.SijoitteluModelMapper;
import fi.vm.sade.valinta.sharedutils.AuditLogger;
import fi.vm.sade.valintalaskenta.tulos.logging.LaskentaAuditLog;
import fi.vm.sade.valintalaskenta.tulos.logging.LaskentaAuditLogImpl;
import fi.vm.sade.valintalaskenta.tulos.mapping.ValintalaskentaModelMapper;
import fi.vm.sade.valintalaskenta.tulos.service.impl.JarjestyskriteerihistoriaServiceImpl;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
@ComponentScan(basePackages = {
    "fi.vm.sade.valintalaskenta.tulos.dao",
    "fi.vm.sade.valintalaskenta.tulos.service.impl",
    "fi.vm.sade.valintalaskenta.tulos.service.impl.converters"
}, excludeFilters = {
  @ComponentScan.Filter(type=FilterType.ASSIGNABLE_TYPE, value=JarjestyskriteerihistoriaServiceImpl.class)})
@Profile("!test")
public class SijoitteluServiceConfiguration implements InitializingBean {

    public static final String CALLER_ID = "1.2.246.562.10.00000000001.sijoittelu.sijoittelu-service";

    @Autowired
    @Qualifier("sijoitteluJdbcTemplate")
    JdbcTemplate jdbcTemplate;

    @Bean
    public Audit audit() { return new Audit(new AuditLogger(), "sijoittelu", ApplicationType.VIRKAILIJA); }

    @Bean("LaskentaAuditLog")
    public LaskentaAuditLog laskentaAuditLog() {
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

    @Override
    public void afterPropertiesSet() throws Exception {
        Flyway flyway = Flyway.configure()
            .schemas("public")
            .dataSource(jdbcTemplate.getDataSource())
            .table("schema_version")
            .locations("/db/migration")
            .load();
        flyway.migrate();
    }
}
