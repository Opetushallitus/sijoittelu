package fi.vm.sade.sijoittelu.laskenta.service.it.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fi.vm.sade.sijoittelu.laskenta.external.resource.HakuV1Resource;
import fi.vm.sade.sijoittelu.laskenta.external.resource.HttpClients;
import fi.vm.sade.sijoittelu.laskenta.external.resource.OhjausparametriResource;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.KoutaHaku;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriDTO;
import fi.vm.sade.sijoittelu.laskenta.service.it.Haku;
import fi.vm.sade.sijoittelu.laskenta.service.it.TarjontaIntegrationService;
import fi.vm.sade.sijoittelu.laskenta.util.UrlProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
public class TarjontaIntegrationServiceImpl implements TarjontaIntegrationService{
    private HakuV1Resource hakuV1Resource;
    private OhjausparametriResource ohjausparametriResource;
    private UrlProperties urlProperties;
    private HttpClient koutaInternalHttpClient;
    private Gson gson;

    @Autowired
    public TarjontaIntegrationServiceImpl(HakuV1Resource hakuV1Resource,
                                          OhjausparametriResource ohjausparametriResource,
                                          UrlProperties urlProperties,
                                          @Qualifier("KoutaInternalHttpClient") HttpClient koutaInternalHttpClient) {
        this.hakuV1Resource = hakuV1Resource;
        this.ohjausparametriResource = ohjausparametriResource;
        this.urlProperties = urlProperties;
        this.koutaInternalHttpClient = koutaInternalHttpClient;
        this.gson = new GsonBuilder().create();
    }

    @Override
    public Haku getHaku(String hakuOid) {
        try {
            ParametriDTO ohjausparametrit = this.gson.fromJson(ohjausparametriResource.haePaivamaara(hakuOid), ParametriDTO.class);

            CompletableFuture<HttpResponse<String>> koutaResponseF = koutaInternalHttpClient.sendAsync(
                    HttpRequest.newBuilder(URI.create(urlProperties.url("kouta-internal.haku", hakuOid)))
                            .header("Accept", "application/json")
                            .GET()
                            .timeout(Duration.ofSeconds(10))
                            .header("Caller-Id", HttpClients.CALLER_ID)
                            .build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8)
            );

            try {
                return new Haku(hakuV1Resource.findByOid(hakuOid).getResult(), ohjausparametrit);
            } catch (Exception tarjontaException) {
                HttpResponse<String> koutaResponse = koutaResponseF.get(10, TimeUnit.SECONDS);
                if (koutaResponse.statusCode() == 200) {
                    return new Haku(this.gson.fromJson(koutaResponse.body(), KoutaHaku.class), ohjausparametrit);
                }
                if (koutaResponse.statusCode() == 404) {
                    throw tarjontaException;
                }
                throw new RuntimeException(String.format("Haun %s haku epäonnistui: %s", hakuOid, koutaResponse.body()));
            }
        } catch (Exception e) {
            throw new RuntimeException(String.format("Haun %s haku epäonnistui", hakuOid), e);
        }
    }
}
