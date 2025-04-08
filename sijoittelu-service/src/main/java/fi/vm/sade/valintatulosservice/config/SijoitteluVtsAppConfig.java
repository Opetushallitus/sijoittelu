package fi.vm.sade.valintatulosservice.config;

import fi.vm.sade.security.ProductionSecurityContext;
import fi.vm.sade.security.SecurityContext;
import fi.vm.sade.utils.cas.CasClient;
import fi.vm.sade.utils.config.ApplicationSettingsLoader;
import fi.vm.sade.valintatulosservice.security.Role;
import fi.vm.sade.valintatulosservice.valintaperusteet.ValintaPerusteetService;
import fi.vm.sade.valintatulosservice.valintaperusteet.ValintaPerusteetServiceImpl;
import org.http4s.client.blaze.BlazeClientConfig;
import org.http4s.client.blaze.SimpleHttp1Client;
import scala.collection.JavaConversions;
import scala.collection.immutable.Map;

import java.util.Set;
import java.util.stream.Collectors;

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
    final CasClient casClient = new CasClient(
        settings().securitySettings().casUrl(),
        SimpleHttp1Client.apply(blazeDefaultConfig()),
        settings().callerId()
    );
    final Set<Role> roles = JavaConversions
        .asJavaCollection(settings().securitySettings().requiredRoles())
        .stream()
        .map(Role::new)
        .collect(Collectors.toSet());
    return new ProductionSecurityContext(
        casClient,
        settings().securitySettings().casServiceIdentifier(),
        JavaConversions.asScalaSet(roles).toSet(),
        settings().securitySettings().casValidateServiceTicketTimeout()
    );
  }

  @Override
  public BlazeClientConfig blazeDefaultConfig() {
    BlazeClientConfig d = BlazeClientConfig.defaultConfig();
    return new BlazeClientConfig(
        settings().blazeResponseHeaderTimeout(),
        settings().blazeIdleTimeout(),
        settings().requestTimeout(),
        d.userAgent(),
        d.sslContext(),
        d.checkEndpointIdentification(),
        d.maxResponseLineSize(),
        d.maxHeaderLength(),
        d.maxChunkSize(),
        d.lenientParser(),
        d.bufferSize(),
        d.customExecutor(),
        d.group()
    );
  }

  @Override
  public ValintaPerusteetService valintaPerusteetService() {
    return new ValintaPerusteetServiceImpl(this);
  }
}