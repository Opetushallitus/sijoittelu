package fi.vm.sade.sijoittelu;

import fi.vm.sade.util.TempDockerDB;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Profile;

@Profile("dev")
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class DevApp {


    public static final String CONTEXT_PATH = "/sijoittelu-service";
    private static final String ENVIRONMENT = "hahtuva";

    public static void main(String[] args) {
        // ssl-konfiguraatio
        System.setProperty("server.ssl.key-store-type", "PKCS12");
        System.setProperty("server.ssl.key-store", "classpath:sijoittelu.p12");
        System.setProperty("server.ssl.key-store-password", "password");
        System.setProperty("server.ssl.key-alias", "sijoittelu");
        System.setProperty("server.ssl.enabled", "true");
        System.setProperty("server.port", "8443");

        // cas-configuraatio
        System.setProperty("cas-service.service", String.format("https://virkailija.%sopintopolku.fi/sijoittelu-service", ENVIRONMENT));
        System.setProperty("cas-service.sendRenew", "false");
        System.setProperty("cas-service.key", "sijoittelu-service");
        System.setProperty("web.url.cas", String.format("https://virkailija.%sopintopolku.fi/cas", ENVIRONMENT));

        // postgres
        System.setProperty("valintarekisteri.db.url", "jdbc:postgresql://localhost:5433/valintarekisteri");
        System.setProperty("valintarekisteri.db.user", "oph");
        System.setProperty("valintarekisteri.db.password", "oph");

        System.setProperty("sijoittelu.valintalaskenta.postgresql.url", "jdbc:postgresql://localhost:25446/valintalaskenta");
        System.setProperty("sijoittelu.valintalaskenta.postgresql.driver", "org.postgresql.Driver");
        System.setProperty("sijoittelu.valintalaskenta.postgresql.maxlifetimemillis", "60000");
        System.setProperty("sijoittelu.valintalaskenta.postgresql.readonly", "true");

        System.setProperty("sijoittelu.sijoittelu.postgresql.url", "jdbc:postgresql://localhost:5433/sijoittelu");
        System.setProperty("sijoittelu.sijoittelu.postgresql.username", "oph");
        System.setProperty("sijoittelu.sijoittelu.postgresql.password", "oph");
        System.setProperty("sijoittelu.sijoittelu.postgresql.driver", "");

        // ulkoiset palvelut
        final String hostVirkailija = String.format("virkailija.%sopintopolku.fi", ENVIRONMENT);
        System.setProperty("host.virkailija", hostVirkailija);
        System.setProperty("kayttooikeus-service.userDetails.byUsername", String.format("http://alb.%sopintopolku.fi:8888/kayttooikeus-service/userDetails/$1", ENVIRONMENT));
        System.setProperty("organisaatio-service.organisaatio.hae.oid", String.format("%s/organisaatio-service/api/hierarkia/hae?aktiiviset=true&suunnitellut=true&lakkautetut=true&oid=$1", hostVirkailija));
        System.setProperty("valintalaskentakoostepalvelu.valintatulosservice.rest.url", String.format("http://alb.%sopintopolku.fi:8888/valinta-tulos-service", ENVIRONMENT));
        System.setProperty("valintalaskentakoostepalvelu.parametriservice.rest.url", String.format("https://%s/ohjausparametrit-service/api", hostVirkailija));
        System.setProperty("valintalaskentakoostepalvelu.tarjonta.rest.url", String.format("https://%s/tarjonta-service/rest", hostVirkailija));
        System.setProperty("valintarekisteri.tarjonta-service.url", String.format("https://%s/tarjonta-service", hostVirkailija));
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

        // uusia seurantaa varten
        System.setProperty("valintalaskentakoostepalvelu.seuranta.rest.url", String.format("https://%s/seuranta-service/resources", hostVirkailija));

        // vts:ää varten (vastaaottojen tallennus)
        System.setProperty("cas.url", String.format("https://%s/cas", hostVirkailija));
        System.setProperty("cas.service.organisaatio-service", String.format("https://%s/organisaatio-service", hostVirkailija));
        System.setProperty("cas.service.valintaperusteet-service", String.format("https://%s/valintaperusteet-service", hostVirkailija));
        System.setProperty("hakemus.mongodb.dbname", "hakulomake");
        System.setProperty("hakemus.mongodb.uri", "mongodb://hakulomakeuser:hakulomake@docdb.db.hahtuvaopintopolku.fi:27017");
        System.setProperty("omatsivut.fi", "not_configured");
        System.setProperty("omatsivut.en", "not_configured");
        System.setProperty("omatsivut.sv", "not_configured");
        System.setProperty("omatsivut.oili.hetutonUrl", "not_configured");
        System.setProperty("oppijan-tunnistus-service.url", String.format("https://%s/oppijan-tunnistus/api/v1/only_token", hostVirkailija));
        System.setProperty("root.organisaatio.oid", "1.2.246.562.10.00000000001");
        System.setProperty("tarjonta-service.url", String.format("https://%s/tarjonta-service", hostVirkailija));
        System.setProperty("valinta-tulos-service.ataru-hakemus-enricher-hakukohde-cache.ttl.seconds", "3600");
        System.setProperty("valinta-tulos-service.blaze.response-header-timeout", "10");
        System.setProperty("valinta-tulos-service.blaze.idle-timeout", "60");
        System.setProperty("valinta-tulos-service.blaze.request-timeout", "3600");
        System.setProperty("valinta-tulos-service.cas.service", String.format("https://%s/valinta-tulos-service", hostVirkailija));
        System.setProperty("valinta-tulos-service.cas.username", "not_configured");
        System.setProperty("valinta-tulos-service.cas.password", "not_configured");
        System.setProperty("valinta-tulos-service.cas.kela.username", "not_configured");
        System.setProperty("valinta-tulos-service.cas.kela.password", "not_configured");
        System.setProperty("valinta-tulos-service.cas.validate-service-ticket.timeout.seconds", "1");
        System.setProperty("valinta-tulos-service.emailer.cron.string", "0 05 7,19 * * ?");
        System.setProperty("valinta-tulos-service.header.last.modified", "X-Last-Modified");
        System.setProperty("valinta-tulos-service.header.if.unmodified.since", "X-If-Unmodified-Since");
        System.setProperty("valinta-tulos-service.ilmoittautuminen.enabled", "true");
        System.setProperty("valinta-tulos-service.kela.vastaanotot.testihetu", "");
        System.setProperty("valinta-tulos-service.kela.url", "https://asiointi.kela.fi/go_app/EArtApplication?reitti=oph");
        System.setProperty("valinta-tulos-service.kohdejoukot.korkeakoulu", "'haunkohdejoukko_12'");
        System.setProperty("valinta-tulos-service.kohdejoukot.toinen-aste", "haunkohdejoukko_11,haunkohdejoukko_17,haunkohdejoukko_20,haunkohdejoukko_23,haunkohdejoukko_24");
        System.setProperty("valinta-tulos-service.kohdejoukon-tarkenteet.amkope", "haunkohdejoukontarkenne_2,haunkohdejoukontarkenne_4,haunkohdejoukontarkenne_5,haunkohdejoukontarkenne_6");
        System.setProperty("valinta-tulos-service.mail-poller.concurrency", "1");
        System.setProperty("valinta-tulos-service.mail-poller.hakemus.recheck.hours", "24");
        System.setProperty("valinta-tulos-service.mail-poller.resultless.hakukohde.hours", "23");
        System.setProperty("valinta-tulos-service.parseleniently.sijoitteluajontulos", "true");
        System.setProperty("valinta-tulos-service.parseleniently.tarjonta", "false");
        System.setProperty("valinta-tulos-service.read-from-valintarekisteri", "true");
        System.setProperty("valinta-tulos-service.scheduled-migration.start-hour", "17");
        System.setProperty("valinta-tulos-service.scheduled-migration.end-hour", "7");
        System.setProperty("valinta-tulos-service.scheduled-delete-sijoitteluajo.start-hour", "23");
        System.setProperty("valinta-tulos-service.scheduled-delete-sijoitteluajo.limit", "20");
        System.setProperty("valinta-tulos-service.siirtotiedosto.aws-region", "eu-west-1");
        System.setProperty("valinta-tulos-service.siirtotiedosto.hakukohde_group_size", "500");
        System.setProperty("valinta-tulos-service.siirtotiedosto.hyvaksytytjulkaistuthakutoiveet_page_size", "10000");
        System.setProperty("valinta-tulos-service.siirtotiedosto.ilmoittautumiset_page_size", "10000");
        System.setProperty("valinta-tulos-service.siirtotiedosto.jonosijat_page_size", "10000");
        System.setProperty("valinta-tulos-service.siirtotiedosto.lukuvuosimaksut_page_size", "10000");
        System.setProperty("valinta-tulos-service.siirtotiedosto.s3.target-role-arn", "no-role-set");
        System.setProperty("valinta-tulos-service.siirtotiedosto.s3-bucket", "not-a-real-bucket");
        System.setProperty("valinta-tulos-service.siirtotiedosto.valintatapajonot_page_size", "10000");
        System.setProperty("valinta-tulos-service.siirtotiedosto.vastaanotot_page_size", "10000");
        System.setProperty("valinta-tulos-service.streaming.hakukohde.concurrency", "10");
        System.setProperty("valinta-tulos-service.streaming.lock.queue.limit", "10");
        System.setProperty("valinta-tulos-service.streaming.lock.timeout.seconds", "600");
        System.setProperty("valinta-tulos-service.swagger", String.format("https\\://%s/valinta-tulos-service/swagger/swagger.json", hostVirkailija));
        System.setProperty("valinta-tulos-service.valintarekisteri.db.url", "jdbc:postgresql://localhost:5433/valintarekisteri");
        System.setProperty("valinta-tulos-service.valintarekisteri.db.user", "oph");
        System.setProperty("valinta-tulos-service.valintarekisteri.db.password", "oph");
        System.setProperty("valinta-tulos-service.valintarekisteri.db.numThreads", "10");
        System.setProperty("valinta-tulos-service.valintarekisteri.db.maxConnections", "50");
        System.setProperty("valinta-tulos-service.valintarekisteri.db.minConnections", "10");
        System.setProperty("valinta-tulos-service.valintarekisteri.db.queueSize", "10000");
        System.setProperty("valinta-tulos-service.valintarekisteri.db.registerMbeans", "true");
        System.setProperty("valinta-tulos-service.valintarekisteri.db.initializationFailFast", "1000");
        System.setProperty("valinta-tulos-service.valintarekisteri.db.leakDetectionThresholdMillis", "1800000");
        System.setProperty("valinta-tulos-service.valintarekisteri.db.flyway.disabled", "false");
        System.setProperty("valinta-tulos-service.valintarekisteri.ensikertalaisuus.max.henkilo.oids", "1000000");
        System.setProperty("valinta-tulos-service.valintarekisteri.use-sijoittelu-mongo", "true");

        TempDockerDB.start();
        System.setProperty("spring-boot.run.jvmArguments", "-Xms2048m -Xmx4096m");
        System.setProperty("server.servlet.context-path", CONTEXT_PATH);
        App.main(args);
    }

}
