package fi.vm.sade.valinta.kooste.external.resource.ohjausparametrit.impl;

import com.google.gson.reflect.TypeToken;
import fi.vm.sade.sijoittelu.laskenta.util.UrlProperties;
import fi.vm.sade.valinta.kooste.external.resource.HttpClient;
import fi.vm.sade.valinta.kooste.external.resource.ohjausparametrit.OhjausparametritAsyncResource;
import fi.vm.sade.valinta.kooste.external.resource.ohjausparametrit.dto.ParametritDTO;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OhjausparametritAsyncResourceImpl implements OhjausparametritAsyncResource {
  private final HttpClient client;
  private final UrlProperties urlProperties;
  private final Duration requestTimeout;

  @Autowired
  public OhjausparametritAsyncResourceImpl(
      @Qualifier("OhjausparametritHttpClient") HttpClient client,
      @Value("${valintalaskentakoostepalvelu.ohjausparametrit.request.timeout.seconds:20}")
          int requestTimeoutSeconds, UrlProperties urlProperties) {
    this.client = client;
    this.urlProperties = urlProperties;
    this.requestTimeout = Duration.ofSeconds(requestTimeoutSeconds);
  }

  @Override
  public CompletableFuture<ParametritDTO> haeHaunOhjausparametrit(String hakuOid) {
    return this.client
        .getResponse(
            this.urlProperties.url("ohjausparametrit-service.parametri", hakuOid),
            this.requestTimeout,
            x -> x)
        .thenApply(
            response -> {
              if (response.statusCode() == 404) {
                return new ParametritDTO();
              }
              return this.client.parseJson(response, new TypeToken<ParametritDTO>() {}.getType());
            });
  }
}
