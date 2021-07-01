package fi.vm.sade.util;


import fi.vm.sade.properties.OphProperties;
import fi.vm.sade.sijoittelu.laskenta.util.UrlProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TestUrlProperties extends UrlProperties {
    private final OphProperties ophProperties;

    public TestUrlProperties(@Value("${host.virkailija}") String hostVirkailija) {
        super(hostVirkailija);
        this.ophProperties = new OphProperties("/sijoittelu-service-oph.properties");
        this.ophProperties.addDefault("host.virkailija", hostVirkailija);
    }

    @Override
    public String url(String key, Object... params) {
        return ophProperties.url(key, params).replace("https", "http");
    }
}
