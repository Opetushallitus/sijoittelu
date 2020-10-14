package fi.vm.sade.sijoittelu.laskenta.util;

import fi.vm.sade.properties.OphProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UrlProperties {
    private final OphProperties ophProperties;

    public UrlProperties(@Value("${host.virkailija}") String hostVirkailija) {
        this.ophProperties = new OphProperties("/sijoittelu-service-oph.properties");
        this.ophProperties.addDefault("host.virkailija", hostVirkailija);
    }

    public String url(String key, Object... params) {
        return ophProperties.url(key, params);
    }
}
