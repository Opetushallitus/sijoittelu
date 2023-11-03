package fi.vm.sade.sijoittelu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class App {

    public static final String CONTEXT_PATH = "/sijoittelu-service";

    public static void main(String[] args) {
        System.setProperty("server.servlet.context-path", CONTEXT_PATH);
        SpringApplication app = new SpringApplication(App.class);
        app.setAllowBeanDefinitionOverriding(true);
        app.run(args);
    }
}
