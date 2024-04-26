package fi.vm.sade.sijoittelu.jatkuva.external.resource.tarjonta.impl;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import fi.vm.sade.sijoittelu.jatkuva.external.resource.tarjonta.Haku;
import fi.vm.sade.sijoittelu.jatkuva.external.resource.tarjonta.TarjontaAsyncResource;
import fi.vm.sade.sijoittelu.jatkuva.external.resource.tarjonta.dto.KoutaHaku;
import fi.vm.sade.sijoittelu.laskenta.util.UrlProperties;
import fi.vm.sade.tarjonta.service.resources.v1.dto.ErrorV1RDTO;
import fi.vm.sade.tarjonta.service.resources.v1.dto.GenericSearchParamsV1RDTO;
import fi.vm.sade.tarjonta.service.resources.v1.dto.HakuV1RDTO;
import fi.vm.sade.tarjonta.service.resources.v1.dto.ResultV1RDTO;
import fi.vm.sade.tarjonta.service.resources.v1.dto.koulutus.KoulutusV1RDTO;
import fi.vm.sade.sijoittelu.jatkuva.external.resource.HttpClient;
import fi.vm.sade.sijoittelu.jatkuva.external.resource.viestintapalvelu.RestCasClient;
import fi.vm.sade.valinta.sharedutils.http.DateDeserializer;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({"default", "dev"})
public class TarjontaAsyncResourceImpl implements TarjontaAsyncResource {
  private final UrlProperties urlProperties;
  private final HttpClient client;
  private final RestCasClient koutaClient;
  private final Integer KOUTA_OID_LENGTH = 35;

  @Autowired
  public TarjontaAsyncResourceImpl(
      @Qualifier("TarjontaHttpClient") HttpClient client,
      @Qualifier("KoutaCasClient") RestCasClient koutaClient,
      UrlProperties urlProperties) {
    this.client = client;
    this.koutaClient = koutaClient;
    this.urlProperties = urlProperties;
  }

  private CompletableFuture<HakuV1RDTO> getTarjontaHaku(String hakuOid) {
    return this.client
        .<ResultV1RDTO<HakuV1RDTO>>getJson(
            this.urlProperties.url("tarjonta-service.haku.hakuoid", hakuOid),
            Duration.ofMinutes(5),
            new com.google.gson.reflect.TypeToken<ResultV1RDTO<HakuV1RDTO>>() {}.getType())
        .thenApplyAsync(ResultV1RDTO::getResult);
  }

  @Override
  public CompletableFuture<Haku> haeHaku(String hakuOid) {
    if (KOUTA_OID_LENGTH.equals(hakuOid.length())) {
      CompletableFuture<KoutaHaku> koutaF =
          this.koutaClient.get(
              this.urlProperties.url("kouta-internal.haku.hakuoid", hakuOid),
              new TypeToken<KoutaHaku>() {},
              Collections.emptyMap(),
              10 * 1000);
      return koutaF.thenApplyAsync(Haku::new);
    } else {
      return this.getTarjontaHaku(hakuOid).thenApplyAsync(Haku::new);
    }
  }

  public static Gson getGson() {
    return DateDeserializer.gsonBuilder()
        .registerTypeAdapter(
            KoulutusV1RDTO.class,
            (JsonDeserializer<KoulutusV1RDTO>)
                (json, typeOfT, context) -> {
                  JsonObject o = json.getAsJsonObject();
                  String toteutustyyppi = o.getAsJsonPrimitive("toteutustyyppi").getAsString();
                  for (JsonSubTypes.Type type :
                      KoulutusV1RDTO.class.getAnnotation(JsonSubTypes.class).value()) {
                    if (type.name().equals(toteutustyyppi)) {
                      return context.deserialize(o, type.value());
                    }
                  }
                  throw new IllegalStateException(
                      String.format(
                          "Tyyppiä %s olevan koulutuksen jäsentäminen epäonnistui",
                          toteutustyyppi));
                })
        .registerTypeAdapter(
            ResultV1RDTO.class,
            (JsonDeserializer)
                (json, typeOfT, context) -> {
                  Type accessRightsType = new TypeToken<Map<String, Boolean>>() {}.getType();
                  Type errorsType = new TypeToken<List<ErrorV1RDTO>>() {}.getType();
                  Type paramsType = new TypeToken<GenericSearchParamsV1RDTO>() {}.getType();
                  Type resultType = ((ParameterizedType) typeOfT).getActualTypeArguments()[0];
                  Type statusType = new TypeToken<ResultV1RDTO.ResultStatus>() {}.getType();
                  JsonObject o = json.getAsJsonObject();
                  ResultV1RDTO r = new ResultV1RDTO();
                  r.setAccessRights(context.deserialize(o.get("accessRights"), accessRightsType));
                  r.setErrors(context.deserialize(o.get("errors"), errorsType));
                  r.setParams(context.deserialize(o.get("params"), paramsType));
                  r.setResult(context.deserialize(o.get("result"), resultType));
                  r.setStatus(context.deserialize(o.get("status"), statusType));
                  return r;
                })
        .create();
  }
}
