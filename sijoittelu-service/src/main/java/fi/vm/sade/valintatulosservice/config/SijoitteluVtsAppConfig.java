package fi.vm.sade.valintatulosservice.config;

import fi.vm.sade.javautils.nio.cas.CasClient;
import fi.vm.sade.javautils.nio.cas.CasConfig;
import fi.vm.sade.javautils.nio.cas.impl.CasClientImpl;
import fi.vm.sade.javautils.nio.cas.impl.CasSessionFetcher;
import fi.vm.sade.security.SecurityContext;
import fi.vm.sade.valintatulosservice.security.Role;
import fi.vm.sade.valintatulosservice.valintaperusteet.ValintaPerusteetService;
import fi.vm.sade.valintatulosservice.valintaperusteet.ValintaPerusteetServiceImpl;
import org.asynchttpclient.AsyncHttpClient;
import scala.Option;
import scala.Some;
import scala.jdk.javaapi.CollectionConverters;
import scala.collection.immutable.Map;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.asynchttpclient.Dsl.asyncHttpClient;

public class SijoitteluVtsAppConfig implements VtsAppConfig.VtsAppConfig, VtsAppConfig.ExternalProps, VtsAppConfig.CasSecurity {
  @Override
  public String configFile() {
    return System.getProperty("user.home") + "/oph-configuration/valinta-tulos-service.properties";
  }

  @Override
  public void start() {
  }

  @Override
  public VtsApplicationSettings settings() {
    return ApplicationSettingsLoader.loadSettings(configFile(), VtsApplicationSettings::new);
  }

  @Override
  public OphUrlProperties ophUrlProperties() {
    return new ProdOphUrlProperties("/oph-configuration/valinta-tulos-service-oph.properties");
  }

  @Override
  public Map<String, String> properties() {
    return settings().toProperties();
  }

  @Override
  public SecurityContext securityContext() {
    final CasConfig casConfig = new CasConfig.CasConfigBuilder(
        settings().securitySettings().casUsername(),
        settings().securitySettings().casPassword(),
        settings().securitySettings().casUrl(),
        settings().securitySettings().casServiceIdentifier(),
        "CSRF",
        settings().callerId(),
        null
    ).setJsessionName("JSESSIONID").build();
    final AsyncHttpClient httpClient = asyncHttpClient();
    final CasClient casClient = new CasClientImpl(
        casConfig,
        httpClient,
        new CasSessionFetcher(
            casConfig,
            httpClient,
            Duration.of(20, MINUTES).toMillis(),
            Duration.of(2, SECONDS).toMillis()) {

          public CompletableFuture<String> fetchSessionToken() {
            return CompletableFuture.completedFuture("session-token-from-mock-context");
          }

        }
    );
    final Set<Role> roles = CollectionConverters
        .asJavaCollection(settings().securitySettings().requiredRoles())
        .stream()
        .map(Role::new)
        .collect(Collectors.toSet());

    return new SecurityContext() {
      @Override
      public String casServiceIdentifier() {
        return settings().securitySettings().casServiceIdentifier();
      }

      @Override
      public scala.collection.immutable.Set<Role> requiredRoles() {
        return CollectionConverters.asScala(roles).toSet();
      }

      @Override
      public Option<CasClient> javaCasClient() {
        return Some.apply(casClient);
      }

      @Override
      public scala.concurrent.duration.Duration validateServiceTicketTimeout() {
        return settings().securitySettings().casValidateServiceTicketTimeout();
      }
    };
  }

  @Override
  public ValintaPerusteetService valintaPerusteetService() {
    return new ValintaPerusteetServiceImpl(this);
  }
}