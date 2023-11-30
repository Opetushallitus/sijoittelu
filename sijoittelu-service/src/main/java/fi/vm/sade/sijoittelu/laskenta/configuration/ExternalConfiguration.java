package fi.vm.sade.sijoittelu.laskenta.configuration;

import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.domain.VastaanotettavuusDTO;
import fi.vm.sade.sijoittelu.domain.dto.VastaanottoDTO;
import fi.vm.sade.sijoittelu.laskenta.external.resource.HakuV1Resource;
import fi.vm.sade.sijoittelu.laskenta.external.resource.OhjausparametriResource;
import fi.vm.sade.sijoittelu.laskenta.external.resource.VirkailijaValintaTulosServiceResource;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ResultHakuDTO;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.VastaanottoEventDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

@Configuration
public class ExternalConfiguration {

    private static <T extends WebClient.RequestHeadersSpec<T>> T withHeaders(WebClient.RequestHeadersSpec<T> spec) {
        return spec
            .header("Caller-Id", SijoitteluServiceConfiguration.CALLER_ID)
            .header("CSRF", "CSRF")
            .header("Cookie", "CSRF=CSRF")
            .accept(MediaType.APPLICATION_JSON)
            .acceptCharset(StandardCharsets.UTF_8);
    }

    private static WebClient webClient;

    static {
        ConnectionProvider provider = ConnectionProvider.builder("external")
            .maxConnections(500)
            .maxIdleTime(Duration.ofSeconds(20))
            .maxLifeTime(Duration.ofSeconds(60))
            .pendingAcquireTimeout(Duration.ofSeconds(60))
            .evictInBackground(Duration.ofSeconds(120)).build();

        webClient = WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(HttpClient.create(provider)))
            .build();
    }


    private static WebClient getWebClient() {
        return webClient;
    }

    @Bean
    public OhjausparametriResource ohjausParametriRestClient(@Value("${valintalaskentakoostepalvelu.parametriservice.rest.url}") String address) {
        return oid -> withHeaders(getWebClient().get().uri(address + "/v1/rest/parametri/" + oid))
            .retrieve()
            .toEntity(String.class)
            .block()
            .getBody();
    }

    @Bean
    public VirkailijaValintaTulosServiceResource virkailijaValintaTulosRestClient(@Value("${valintalaskentakoostepalvelu.valintatulosservice.rest.url}") String address) {
        return new VirkailijaValintaTulosServiceResource() {
            @Override
            public VastaanotettavuusDTO vastaanotettavuus(String hakijaOid, String hakemusOid, String hakukohdeOid) {
                return withHeaders(getWebClient().get().uri(address + "/virkailija/henkilo/" + hakijaOid + "/hakemus/" + hakemusOid+ "/hakukohde/" + hakukohdeOid + "/vastaanotettavuus"))
                    .retrieve()
                    .toEntity(VastaanotettavuusDTO.class)
                    .block()
                    .getBody();
            }

            @Override
            public List<Valintatulos> valintatuloksetValinnantilalla(String hakuOid) {
                return withHeaders(getWebClient().get().uri(address + "/virkailija/valintatulos/haku/" + hakuOid))
                    .retrieve()
                    .toEntityList(Valintatulos.class)
                    .block()
                    .getBody();
            }

            @Override
            public List<VastaanottoDTO> haunKoulutuksenAlkamiskaudenVastaanototYhdenPaikanSaadoksenPiirissa(String hakuOid) {
                return withHeaders(getWebClient().get().uri(address + "/virkailija/vastaanotot/haku/" + hakuOid))
                    .retrieve()
                    .toEntityList(VastaanottoDTO.class)
                    .block()
                    .getBody();
            }

            @Override
            public void valintatuloksetValinnantilalla(List<VastaanottoEventDto> valintatuloses) {
                withHeaders(getWebClient().post().uri(address + "/virkailija/transactional-vastaanotto"))
                    .bodyValue(valintatuloses)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            }
        };
    }

    @Bean
    public HakuV1Resource TarjontaHakuResourceRestClient(@Value("${valintalaskentakoostepalvelu.tarjonta.rest.url}") final String address) {
        return oid -> withHeaders(getWebClient().get().uri(address + "/v1/haku/" + oid))
            .retrieve()
            .toEntity(ResultHakuDTO.class)
            .block()
            .getBody();
    }
}
