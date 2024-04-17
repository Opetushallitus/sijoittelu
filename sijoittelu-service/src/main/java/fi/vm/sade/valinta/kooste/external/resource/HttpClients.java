package fi.vm.sade.valinta.kooste.external.resource;

import static fi.vm.sade.valinta.sharedutils.http.HttpResource.CSRF_VALUE;

import fi.vm.sade.javautils.nio.cas.CasConfig;
import fi.vm.sade.sijoittelu.laskenta.util.UrlProperties;
import fi.vm.sade.valinta.kooste.external.resource.tarjonta.impl.TarjontaAsyncResourceImpl;
import fi.vm.sade.valinta.kooste.external.resource.viestintapalvelu.RestCasClient;
import fi.vm.sade.valinta.sharedutils.http.DateDeserializer;
import java.net.CookieManager;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration("koosteHttpClients")
public class HttpClients {
  public static final String CALLER_ID = "1.2.246.562.10.00000000001.sijoittelu.sijoittelu-service";

  @Bean
  public CookieManager getCookieManager() {
    return new CookieManager();
  }

  @Bean(name = "TarjontaHttpClient")
  @Autowired
  public HttpClient getTarjontaHttpClient(CookieManager cookieManager) {
    return new HttpClient(
        defaultHttpClientBuilder(cookieManager).build(), TarjontaAsyncResourceImpl.getGson());
  }

  @Profile({"default", "dev"})
  @Bean(name = "KoutaCasClient")
  @Autowired
  public RestCasClient getKoutaCasClient(
      @Value("${sijoittelu-service.kouta-internal.username}") String username,
      @Value("${sijoittelu-service.kouta-internal.password}") String password,
      UrlProperties urlProperties) {
    String ticketsUrl = urlProperties.url("cas.tickets");
    String service = urlProperties.url("kouta-internal.auth.login");
    return new RestCasClient(
        CasConfig.CasConfig(
                username, password, ticketsUrl, service, CSRF_VALUE, CALLER_ID, "session", ""));
  }

  @Bean(name = "OhjausparametritHttpClient")
  @Autowired
  public HttpClient getOhjausparametritHttpClient(CookieManager cookieManager) {
    return new HttpClient(
        defaultHttpClientBuilder(cookieManager).build(), DateDeserializer.gsonBuilder().create());
  }

  @Bean(name = "OrganisaatioHttpClient")
  @Autowired
  public HttpClient getOrganisaatioHttpClient(CookieManager cookieManager) {
    return new HttpClient(
        defaultHttpClientBuilder(cookieManager).build(), DateDeserializer.gsonBuilder().create());
  }

  @Profile({"default", "dev"})
  @Bean(name = "SeurantaCasClient")
  @Autowired
  public RestCasClient getSeurantaCasClient(
      UrlProperties urlProperties,
      @Value("${sijoittelu-service.kouta-internal.username}") String username, // TODO: fix credentials
      @Value("${sijoittelu-service.kouta-internal.password}") String password) {
    String service = urlProperties.url("cas.service.seuranta");
    String ticketsUrl = urlProperties.url("cas.tickets");
    return new RestCasClient(
        CasConfig.CasConfig(
                username, password, ticketsUrl, service, CSRF_VALUE, CALLER_ID, "JSESSIONID", ""));
  }

  public static java.net.http.HttpClient.Builder defaultHttpClientBuilder(
      CookieManager cookieManager) {
    return java.net.http.HttpClient.newBuilder()
        .version(java.net.http.HttpClient.Version.HTTP_1_1)
        .connectTimeout(Duration.ofSeconds(10))
        .cookieHandler(cookieManager);
  }
}
