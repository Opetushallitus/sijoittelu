package fi.vm.sade.sijoittelu;

import fi.vm.sade.util.TempDockerDB;

public class DevApp extends App {

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
        System.setProperty("web.url.cas", "https://virkailija.hahtuvaopintopolku.fi/cas");

        // postgres
        System.setProperty("valintarekisteri.db.url", "jdbc:postgresql://localhost:5433/sijoittelu");
        System.setProperty("valintarekisteri.db.user", "oph");
        System.setProperty("valintarekisteri.db.password", "oph");

        // ulkoiset palvelut
        System.setProperty("host.virkailija", "virkailija.hahtuvaopintopolku.fi");
        System.setProperty("kayttooikeus-service.userDetails.byUsername", "https://virkailija.hahtuvaopintopolku.fi/kayttooikeus-service/userDetails/$1");
        System.setProperty("organisaatio-service.organisaatio.hae.oid", "${url-virkailija}/organisaatio-service/api/hierarkia/hae?aktiiviset=true&suunnitellut=true&lakkautetut=true&oid=$1");
        System.setProperty("valintalaskentakoostepalvelu.valintatulosservice.rest.url", "https://virkailija.hahtuvaopintopolku.fi/valinta-tulos-service");
        System.setProperty("valintalaskentakoostepalvelu.parametriservice.rest.url", "https://virkailija.hahtuvaopintopolku.fi/ohjausparametrit-service/api");
        System.setProperty("valintalaskentakoostepalvelu.tarjonta.rest.url", "https://${host.virkailija}/tarjonta-service/rest");
        System.setProperty("valintarekisteri.tarjonta-service.url", "https://virkailija.hahtuvaopintopolku.fi/tarjonta-service");
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

        TempDockerDB.start();

        App.main(args);
    }
}
