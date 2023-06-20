package fi.vm.sade.sijoittelu.laskenta.external.resource;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import fi.vm.sade.javautils.cxf.OphRequestHeadersCxfInterceptor;
import fi.vm.sade.sijoittelu.tulos.resource.ObjectMapperProvider;
import org.apache.cxf.jaxrs.client.JAXRSClientFactoryBean;
import org.apache.cxf.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class ExternalConfiguration {

    @Autowired
    private JacksonJsonProvider jacksonJsonProvider;

    @Autowired
    private ObjectMapperProvider objectMapperProvider;

    @Autowired
    private OphRequestHeadersCxfInterceptor<Message> requestHeaders;

    private <T> T createClient(final String address, final Class<T> cls) {
        // Create rest client
        JAXRSClientFactoryBean bean = new JAXRSClientFactoryBean();
        bean.setInheritHeaders(true);
        bean.setAddress(address);
        bean.setProviders(List.of(jacksonJsonProvider, objectMapperProvider));
        bean.setOutInterceptors(List.of(requestHeaders));
        bean.setServiceClass(cls);
        return bean.create(cls);
    }

    @Bean
    public SijoitteluValintaTulosServiceResource sijoitteluValintaTulosRestClient(@Value("${valintalaskentakoostepalvelu.valintatulosservice.rest.url}") String address) {
        return createClient(address, SijoitteluValintaTulosServiceResource.class);
    }

    @Bean
    public OhjausparametriResource ohjausParametriRestClient(@Value("${valintalaskentakoostepalvelu.parametriservice.rest.url}") String address) {
        return createClient(address, OhjausparametriResource.class);
    }

    @Bean
    public VirkailijaValintaTulosServiceResource virkailijaValintaTulosRestClient(@Value("${valintalaskentakoostepalvelu.valintatulosservice.rest.url}") String address) {
        return createClient(address, VirkailijaValintaTulosServiceResource.class);
    }

    @Bean
    public HakuV1Resource TarjontaHakuResourceRestClient(@Value("${valintalaskentakoostepalvelu.tarjonta.rest.url}") final String address) {
        return createClient(address, HakuV1Resource.class);
    }

}
