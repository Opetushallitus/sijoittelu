package fi.vm.sade.valintatulosservice.config;

import com.typesafe.config.Config;
import fi.vm.sade.security.ProductionSecurityContext;
import fi.vm.sade.security.SecurityContext;
import fi.vm.sade.valintatulosservice.valintaperusteet.ValintaPerusteetService;
import fi.vm.sade.valintatulosservice.valintaperusteet.ValintaPerusteetServiceImpl;
import scala.collection.immutable.Map;

import java.util.concurrent.TimeUnit;

import static fi.vm.sade.sijoittelu.configuration.SijoitteluServiceConfiguration.CALLER_ID;

public class SijoitteluVtsAppConfig implements VtsAppConfig.VtsAppConfig, VtsAppConfig.ExternalProps, VtsAppConfig.CasSecurity {
  @Override
  public String configFile() {
    return System.getProperty("user.home") + "/oph-configuration/valinta-tulos-service.properties";
  }

  @Override
  public void start() {
  }

  static class CallerIdOverridingVtsAppConfig extends VtsApplicationSettings {
    public CallerIdOverridingVtsAppConfig(final Config config) {
      super(config);
    }

    @Override
    public String callerId() {
      return CALLER_ID;
    }
  }

  @Override
  public VtsApplicationSettings settings() {
    return (CallerIdOverridingVtsAppConfig) ApplicationSettingsLoader.loadSettings(configFile(), VtsApplicationSettings::new);
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
    return new ProductionSecurityContext(settings().securitySettings().casServiceIdentifier(),
        scala.collection.immutable.Set$.MODULE$.empty(),
        scala.concurrent.duration.Duration.apply(30, TimeUnit.SECONDS));
    };

  @Override
  public ValintaPerusteetService valintaPerusteetService() {
    return new ValintaPerusteetServiceImpl(this);
  }
}
