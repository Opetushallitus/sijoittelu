package fi.vm.sade.sijoittelu.laskenta.configuration;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import fi.vm.sade.sijoittelu.laskenta.mapping.SijoitteluModelMapper;
import fi.vm.sade.valintalaskenta.tulos.logging.LaskentaAuditLogImpl;
import fi.vm.sade.valintalaskenta.tulos.mapping.ValintalaskentaModelMapper;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import org.springframework.beans.factory.annotation.Value;
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

    @Bean(name="datastore2")
    public Datastore datastore(@Value("${valintalaskenta-laskenta-service.mongodb.uri}") String mongoUri, @Value("${valintalaskenta-laskenta-service.mongodb.dbname}") String dbName) {
        return new Morphia().createDatastore(new MongoClient(new MongoClientURI(mongoUri)), dbName);
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
