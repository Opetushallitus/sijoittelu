package fi.vm.sade.sijoittelu.laskenta.service.business;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class ValintarekisteriServiceConfiguration {

    private Properties properties;

    public ValintarekisteriServiceConfiguration(@Value("${valintarekisteri.db.user}") String  valintarekisteriDbUser,
                                                @Value("${valintarekisteri.db.password}") String  valintarekisteriDbPassword,
                                                @Value("${valintarekisteri.db.url}") String valintarekisteriDbUrl,
                                                @Value("${valintarekisteri.tarjonta-service.url}") String tarjontaUrl,
                                                @Value("${valintarekisteri.organisaatio-service.url") String organisaatioUrl,
                                                @Value("${valintarekisteri.parseleniently.tarjonta}") String tarjontaParseLeniently,
                                                @Value("${valintarekisteri.koodisto-service.url}") String koodistoUrl,
                                                @Value("${valintarekisteri.cas.username}") String casUsername,
                                                @Value("${valintarekisteri.cas.password}") String casPassword,
                                                @Value("${valintarekisteri.blaze.response-header-timeout}") String blazeResponseHeaderTimeout,
                                                @Value("${valintarekisteri.blaze.idle-timeout}") String blazeIdleTimeout,
                                                @Value("${valintarekisteri.blaze.request-timeout}") String blazeRequestTimeout) {
        this.properties = new Properties();
        this.properties.setProperty("valinta-tulos-service.valintarekisteri.db.user", valintarekisteriDbUser);
        this.properties.setProperty("valinta-tulos-service.valintarekisteri.db.password", valintarekisteriDbPassword);
        this.properties.setProperty("valinta-tulos-service.valintarekisteri.db.url", valintarekisteriDbUrl);
        this.properties.setProperty("tarjonta-service.url", tarjontaUrl);
        this.properties.setProperty("organisaatio-service.url", organisaatioUrl);
        this.properties.setProperty("valinta-tulos-service.parseleniently.tarjonta", tarjontaParseLeniently);
        this.properties.setProperty("koodisto-service.rest.url", koodistoUrl);
        this.properties.setProperty("valinta-tulos-service.cas.username", casUsername);
        this.properties.setProperty("valinta-tulos-service.cas.password", casPassword);
        this.properties.setProperty("valinta-tulos-service.blaze.response-header-timeout", blazeResponseHeaderTimeout);
        this.properties.setProperty("valinta-tulos-service.blaze.idle-timeout", blazeIdleTimeout);
        this.properties.setProperty("valinta-tulos-service.blaze.request-timeout", blazeRequestTimeout);
    }

    public Properties getProperties() {
        return this.properties;
    }
}
