package fi.vm.sade.sijoittelu.laskenta.configuration;

import ch.qos.logback.access.tomcat.LogbackValve;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccessLogConfiguration {

  @Bean
  @ConditionalOnProperty(name = "logback.access")
  public WebServerFactoryCustomizer containerCustomizer(@Value("${logback.access:}") final String path) {
    return container -> {
      if (container instanceof TomcatServletWebServerFactory) {
        ((TomcatServletWebServerFactory)container).addContextCustomizers(context -> {
          LogbackValve logbackValve = new LogbackValve();
          logbackValve.setFilename(path);
          context.getPipeline().addValve(logbackValve);
        });
      }
    };
  }
}
