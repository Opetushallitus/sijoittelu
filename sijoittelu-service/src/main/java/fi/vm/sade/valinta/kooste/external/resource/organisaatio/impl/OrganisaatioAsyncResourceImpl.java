package fi.vm.sade.valinta.kooste.external.resource.organisaatio.impl;

import fi.vm.sade.sijoittelu.laskenta.util.UrlProperties;
import fi.vm.sade.valinta.kooste.external.resource.HttpClient;
import fi.vm.sade.valinta.kooste.external.resource.organisaatio.OrganisaatioAsyncResource;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class OrganisaatioAsyncResourceImpl implements OrganisaatioAsyncResource {
  private final HttpClient client;
  private final UrlProperties urlProperties;

  @Autowired
  public OrganisaatioAsyncResourceImpl(@Qualifier("OrganisaatioHttpClient") HttpClient client, UrlProperties urlProperties) {
    this.client = client;
    this.urlProperties = urlProperties;
  }

  @Override
  public CompletableFuture<String> parentoids(String organisaatioId) {
    return this.client
        .getResponse(
            this.urlProperties.url("valintalaskentakoostepalvelu.organisaatioService.rest.url")
                + "/organisaatio/"
                + organisaatioId
                + "/parentoids",
            Duration.ofMinutes(1),
            x -> x)
        .thenApply(
            r -> {
              try {
                return IOUtils.toString(r.body(), "UTF-8");
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });
  }
}
