package fi.vm.sade.sijoittelu.laskenta.service.it.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fi.vm.sade.javautils.nio.cas.CasClient;
import fi.vm.sade.sijoittelu.configuration.SijoitteluServiceConfiguration;
import fi.vm.sade.sijoittelu.laskenta.external.resource.HakuV1Resource;
import fi.vm.sade.sijoittelu.laskenta.external.resource.OhjausparametriResource;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.KoutaHaku;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriDTO;
import fi.vm.sade.sijoittelu.laskenta.service.it.Haku;
import fi.vm.sade.sijoittelu.laskenta.service.it.TarjontaIntegrationService;
import fi.vm.sade.sijoittelu.laskenta.util.UrlProperties;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
public class TarjontaIntegrationServiceImpl implements TarjontaIntegrationService{
    private HakuV1Resource hakuV1Resource;
    private OhjausparametriResource ohjausparametriResource;
    private UrlProperties urlProperties;
    private CasClient koutaInternalCasClient;
    private Gson gson;

    @Autowired
    public TarjontaIntegrationServiceImpl(HakuV1Resource hakuV1Resource,
                                          OhjausparametriResource ohjausparametriResource,
                                          UrlProperties urlProperties,
                                          @Qualifier("KoutaInternaCasClient") CasClient koutaInternalCasClient) {
        this.hakuV1Resource = hakuV1Resource;
        this.ohjausparametriResource = ohjausparametriResource;
        this.urlProperties = urlProperties;
        this.koutaInternalCasClient = koutaInternalCasClient;
        this.gson = new GsonBuilder().create();
    }

    @Override
    public Haku getHaku(String hakuOid) {
            ParametriDTO ohjausparametrit = this.gson.fromJson(ohjausparametriResource.haePaivamaara(hakuOid), ParametriDTO.class);

            if (hakuOid.length() > 30 && hakuOid.startsWith("1.2.246.562.29")) {
                try {
                Request request = new RequestBuilder()
                        .setUrl(urlProperties.url("kouta-internal.haku", hakuOid))
                        .setMethod("GET")
                        .addHeader("Accept", "application/json")
                        .addHeader("Caller-Id", SijoitteluServiceConfiguration.CALLER_ID)
                        .setRequestTimeout(Duration.ofMillis(120000))
                        .setReadTimeout(Duration.ofMillis(120000))
                        .build();

                    Response koutaResponse = koutaInternalCasClient.executeBlocking(request);
                    if (koutaResponse.getStatusCode() == 200) {
                        return new Haku(this.gson.fromJson(koutaResponse.getResponseBody(StandardCharsets.UTF_8), KoutaHaku.class), ohjausparametrit, true);
                    } else {
                        throw new RuntimeException(String.format("Haun %s haku koutasta epäonnistui: %s", hakuOid, koutaResponse.getResponseBody()));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(String.format("Haun %s haku koutasta epäonnistui: %s", hakuOid, e));
                }
            } else {
                try {
                    return new Haku(hakuV1Resource.findByOid(hakuOid).getResult(), ohjausparametrit, true);
                } catch (Exception e) {
                    throw new RuntimeException(String.format("Haun %s haku tarjonnasta epäonnistui", hakuOid), e);
                }
            }
    }

    @Override
    public Haku getHaku(String hakuOid, boolean validate) {
        ParametriDTO ohjausparametrit = this.gson.fromJson(ohjausparametriResource.haePaivamaara(hakuOid), ParametriDTO.class);

        if (hakuOid.length() > 30 && hakuOid.startsWith("1.2.246.562.29")) {
            try {
                Request request = new RequestBuilder()
                        .setUrl(urlProperties.url("kouta-internal.haku", hakuOid))
                        .setMethod("GET")
                        .addHeader("Accept", "application/json")
                        .addHeader("Caller-Id", SijoitteluServiceConfiguration.CALLER_ID)
                        .setRequestTimeout(Duration.ofMillis(120000))
                        .setReadTimeout(Duration.ofMillis(120000))
                        .build();

                Response koutaResponse = koutaInternalCasClient.executeBlocking(request);
                if (koutaResponse.getStatusCode() == 200) {
                    return new Haku(this.gson.fromJson(koutaResponse.getResponseBody(StandardCharsets.UTF_8), KoutaHaku.class), ohjausparametrit, validate);
                } else {
                    throw new RuntimeException(String.format("Haun %s haku koutasta epäonnistui: %s", hakuOid, koutaResponse.getResponseBody()));
                }
            } catch (Exception e) {
                throw new RuntimeException(String.format("Haun %s haku koutasta epäonnistui: %s", hakuOid, e));
            }
        } else {
            try {
                return new Haku(hakuV1Resource.findByOid(hakuOid).getResult(), ohjausparametrit, validate);
            } catch (Exception e) {
                throw new RuntimeException(String.format("Haun %s haku tarjonnasta epäonnistui", hakuOid), e);
            }
        }
    }

}
