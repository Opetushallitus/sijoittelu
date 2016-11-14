package fi.vm.sade.sijoittelu.laskenta.service.business;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component
public class ValintarekisteriServiceConfiguration {

    private Properties properties;

    public ValintarekisteriServiceConfiguration(@Value("${valinta-tulos-service.valintarekisteri.db.user}") String  valintarekisteriDbUser,
                                                @Value("${valinta-tulos-service.valintarekisteri.db.password}") String  valintarekisteriDbPassword,
                                                @Value("${valinta-tulos-service.valintarekisteri.db.url}") String valintarekisteriDbUrl,
                                                @Value("${valinta-tulos-service.tarjonta-service.url}") String tarjontaUrl,
                                                @Value("${valinta-tulos-service.parseleniently.tarjonta}") String tarjontaParseLeniently,
                                                @Value("${valinta-tulos-service.koodisto-service.url}") String koodistoUrl) {
        this.properties = new Properties();
        this.properties.setProperty("valinta-tulos-service.valintarekisteri.db.user", valintarekisteriDbUser);
        this.properties.setProperty("valinta-tulos-service.valintarekisteri.db.password", valintarekisteriDbPassword);
        this.properties.setProperty("valinta-tulos-service.valintarekisteri.db.url", valintarekisteriDbUrl);
        this.properties.setProperty("tarjonta-service.url", tarjontaUrl);
        this.properties.setProperty("valinta-tulos-service.parseleniently.tarjonta", tarjontaParseLeniently);
        this.properties.setProperty("koodisto-service.rest.url", koodistoUrl);
    }

    public Properties getProperties() {
        return this.properties;
    }
}
