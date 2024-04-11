package fi.vm.sade.sijoittelu;

import fi.vm.sade.util.TempDockerDB;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Profile;

@Profile("dev")
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class DevApp {


    public static final String CONTEXT_PATH = "/sijoittelu-service";
    private static final String ENVIRONMENT = "untuva";

    public static void main(String[] args) {
        // ssl-konfiguraatio
        System.setProperty("server.ssl.key-store-type", "PKCS12");
        System.setProperty("server.ssl.key-store", "classpath:sijoittelu.p12");
        System.setProperty("server.ssl.key-store-password", "password");
        System.setProperty("server.ssl.key-alias", "sijoittelu");
        System.setProperty("server.ssl.enabled", "true");
        System.setProperty("server.port", "8443");

        // cas-configuraatio
        System.setProperty("cas-service.service", "https://localhost:8443/sijoittelu-service");
        System.setProperty("cas-service.sendRenew", "false");
        System.setProperty("cas-service.key", "sijoittelu-service");
        System.setProperty("web.url.cas", String.format("https://virkailija.%sopintopolku.fi/cas", ENVIRONMENT));

        // postgres
        System.setProperty("valintarekisteri.db.url", "jdbc:postgresql://localhost:5433/sijoittelu");
        System.setProperty("valintarekisteri.db.user", "oph");
        System.setProperty("valintarekisteri.db.password", "oph");

        System.setProperty("sijoittelu.valintalaskenta.postgresql.maxlifetimemillis", "60000");
        System.setProperty("sijoittelu.valintalaskenta.postgresql.readonly", "true");

        // ulkoiset palvelut
        System.setProperty("host.virkailija", String.format("virkailija.%sopintopolku.fi", ENVIRONMENT));
        System.setProperty("kayttooikeus-service.userDetails.byUsername", String.format("https://virkailija.%sopintopolku.fi/kayttooikeus-service/userDetails/$1", ENVIRONMENT));
        System.setProperty("organisaatio-service.organisaatio.hae.oid", "${url-virkailija}/organisaatio-service/api/hierarkia/hae?aktiiviset=true&suunnitellut=true&lakkautetut=true&oid=$1");
        System.setProperty("valintalaskentakoostepalvelu.valintatulosservice.rest.url", String.format("https://virkailija.%sopintopolku.fi/valinta-tulos-service", ENVIRONMENT));
        System.setProperty("valintalaskentakoostepalvelu.parametriservice.rest.url", String.format("https://virkailija.%sopintopolku.fi/ohjausparametrit-service/api", ENVIRONMENT));
        System.setProperty("valintalaskentakoostepalvelu.tarjonta.rest.url", "https://${host.virkailija}/tarjonta-service/rest");
        System.setProperty("valintarekisteri.tarjonta-service.url", String.format("https://virkailija.%sopintopolku.fi/tarjonta-service", ENVIRONMENT));
        System.setProperty("valintarekisteri.parseleniently.tarjonta", "false");

        // propertyt jotka tarvitaan jotta sovellus käynnistyy, mutta joita ei tarvita ainakaan normaalissa sijoittelussa
        System.setProperty("root.organisaatio.oid", "NOT_DEFINED");

        System.setProperty("cas.session.valintaperusteet", "NOT_DEFINED");

        System.setProperty("valintarekisteri.koodisto-service.url", "NOT_DEFINED");

        System.setProperty("valintarekisteri.kohdejoukot.korkeakoulu", "NOT_DEFINED");
        System.setProperty("valintarekisteri.kohdejoukot.toinen-aste", "NOT_DEFINED");
        System.setProperty("valintarekisteri.kohdejoukon-tarkenteet.amkope", "NOT_DEFINED");
        System.setProperty("valintarekisteri.blaze.response-header-timeout", "60");
        System.setProperty("valintarekisteri.blaze.idle-timeout", "60");
        System.setProperty("valintarekisteri.blaze.request-timeout", "60");

        System.setProperty("spring.profiles.active", "dev");
        //System.setProperty("logging.level.root", "debug");

        // uusia seurantaa varten
        System.setProperty("valintalaskentakoostepalvelu.seuranta.rest.url", "https://${host.virkailija}/seuranta-service/resources");

        TempDockerDB.start();
        System.setProperty("spring-boot.run.jvmArguments", "-Xms2048m -Xmx4096m");
        System.setProperty("server.servlet.context-path", CONTEXT_PATH);
        App.main(args);
    }

}
