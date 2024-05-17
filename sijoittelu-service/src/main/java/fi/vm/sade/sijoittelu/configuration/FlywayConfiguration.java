package fi.vm.sade.sijoittelu.configuration;

import fi.vm.sade.sijoittelu.flyway.V20240419000001__LueJatkuvatSijoittelutSeurannasta;
import fi.vm.sade.sijoittelu.jatkuva.external.resource.viestintapalvelu.RestCasClient;
import fi.vm.sade.sijoittelu.laskenta.util.UrlProperties;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.Arrays;

@Configuration
public class FlywayConfiguration {

    @Autowired
    @Qualifier("sijoitteluDataSource")
    DataSource sijoitteluDataSource;

    @Autowired(required = false)
    @Qualifier("SeurantaCasClient")
    RestCasClient seurantaCasClient;

    @Autowired
    UrlProperties urlProperties;

    @Autowired
    Environment environment;

    public static class FlywayMigrationDone {}

    private boolean isTest() {
        return Arrays.stream(environment.getActiveProfiles()).filter(p -> p.equals("test")).findFirst().isPresent();
    }

    @Bean
    public FlywayMigrationDone doFlywayMigration() {
        V20240419000001__LueJatkuvatSijoittelutSeurannasta.setDependencies(this.urlProperties, this.seurantaCasClient, isTest());

        Flyway flyway = Flyway.configure()
            .schemas("public")
            .dataSource(this.sijoitteluDataSource)
            .locations("/db/migration", "fi/vm/sade/sijoittelu/flyway")
            .load();
        flyway.migrate();

        return new FlywayMigrationDone();
    }
}
