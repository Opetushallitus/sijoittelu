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
                                                @Value("${valintarekisteri.koodisto-service.url}") String koodistoUrl) {
        this.properties = new Properties();
        this.properties.setProperty("valinta-tulos-service.valintarekisteri.db.user", valintarekisteriDbUser);
        this.properties.setProperty("valinta-tulos-service.valintarekisteri.db.password", valintarekisteriDbPassword);
        this.properties.setProperty("valinta-tulos-service.valintarekisteri.db.url", valintarekisteriDbUrl);
        this.properties.setProperty("tarjonta-service.url", tarjontaUrl);
        this.properties.setProperty("organisaatio-service.url", organisaatioUrl);
        this.properties.setProperty("valinta-tulos-service.parseleniently.tarjonta", tarjontaParseLeniently);
        this.properties.setProperty("koodisto-service.rest.url", koodistoUrl);
    }

    public Properties getProperties() {
        return this.properties;
    }
}
