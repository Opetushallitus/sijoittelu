package fi.vm.sade.sijoittelu;

import fi.vm.sade.sijoittelu.laskenta.external.resource.ExternalConfiguration;
import org.apache.cxf.jaxrs.swagger.ui.SwaggerUiResourceLocator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class, MongoAutoConfiguration.class })
public class App {

    @Configuration
    @Import({ExternalConfiguration.class})
    @ImportResource({"classpath:spring/context/application-context.xml","classpath:spring/context/application-context-db.xml","classpath:spring/context/application-context-service.xml"})
    public static class TestConfiguration {}
    public static final String CONTEXT_PATH = "/sijoittelu-service";

    public static void main(String[] args) {
        System.setProperty("server.servlet.context-path", CONTEXT_PATH);
        SpringApplication app = new SpringApplication(App.class);
        app.setAllowBeanDefinitionOverriding(true);
        app.run(args);
    }
}
