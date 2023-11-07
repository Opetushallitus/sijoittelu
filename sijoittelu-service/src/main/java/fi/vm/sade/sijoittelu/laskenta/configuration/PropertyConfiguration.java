package fi.vm.sade.sijoittelu.laskenta.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Profile("!test")
@Configuration
@PropertySource(value = {
    "file:///${user.home:''}/oph-configuration/common.properties",
    "file:///${user.home:''}/oph-configuration/sijoittelu-service.properties"
})
public class PropertyConfiguration {
}
