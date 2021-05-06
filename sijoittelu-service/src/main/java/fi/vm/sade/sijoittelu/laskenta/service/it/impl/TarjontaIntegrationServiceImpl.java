package fi.vm.sade.sijoittelu.laskenta.service.it.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fi.vm.sade.javautils.nio.cas.CasClient;
import fi.vm.sade.sijoittelu.laskenta.external.resource.HakuV1Resource;
import fi.vm.sade.sijoittelu.laskenta.external.resource.HttpClients;
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
        try {
            ParametriDTO ohjausparametrit = this.gson.fromJson(ohjausparametriResource.haePaivamaara(hakuOid), ParametriDTO.class);

            Request request = new RequestBuilder()
                    .setUrl(urlProperties.url("kouta-internal.haku", hakuOid))
                    .setMethod("GET")
                    .addHeader("Accept", "application/json")
                    .addHeader("Caller-Id", HttpClients.CALLER_ID)
                    .setRequestTimeout(10000)
                    .build();
            try {
                return new Haku(hakuV1Resource.findByOid(hakuOid).getResult(), ohjausparametrit);
            } catch (Exception tarjontaException) {
                Response koutaResponse = koutaInternalCasClient.executeBlocking(request);
                //TODO fix this!
                if (koutaResponse.getStatusCode() == 200) {
                    return new Haku(this.gson.fromJson(koutaResponse.getResponseBody(StandardCharsets.UTF_8), KoutaHaku.class), ohjausparametrit);
                }
                if (koutaResponse.getStatusCode() == 404) {
                    throw tarjontaException;
                }
                throw new RuntimeException(String.format("Haun %s haku epäonnistui: %s", hakuOid, koutaResponse.getResponseBody()));
            }
        } catch (Exception e) {
            throw new RuntimeException(String.format("Haun %s haku epäonnistui", hakuOid), e);
        }
    }
}
