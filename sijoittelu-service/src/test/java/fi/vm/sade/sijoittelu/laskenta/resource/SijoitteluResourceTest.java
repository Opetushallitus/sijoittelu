package fi.vm.sade.sijoittelu.laskenta.resource;

import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import fi.vm.sade.javautils.nio.cas.CasClient;
import fi.vm.sade.javautils.nio.cas.CasConfig;
import fi.vm.sade.service.valintaperusteet.dto.HakijaryhmaValintatapajonoDTO;
import fi.vm.sade.service.valintaperusteet.dto.KoodiDTO;
import fi.vm.sade.service.valintaperusteet.dto.ValintatapajonoDTO;
import fi.vm.sade.service.valintaperusteet.dto.model.Tasapistesaanto;
import fi.vm.sade.sijoittelu.domain.SijoitteluajonTila;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.sijoittelu.laskenta.util.EnumConverter;
import fi.vm.sade.sijoittelu.laskenta.util.UrlProperties;
import fi.vm.sade.util.TestUrlProperties;
import fi.vm.sade.valintalaskenta.domain.dto.HakijaryhmaDTO;
import fi.vm.sade.valintalaskenta.domain.dto.HakukohdeDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintakoe.Tasasijasaanto;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.ValintatietoValinnanvaiheDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.ValintatietoValintatapajonoDTO;
import fi.vm.sade.valintalaskenta.tulos.service.impl.ValintatietoService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Jussi Jartamo
 */
public class SijoitteluResourceTest {

    private final SijoitteluResource sijoitteluResource;
    private final SijoitteluBusinessService sijoitteluBusinessService;
    private final ValintatietoService valintatietoService;
    private final SijoitteluBookkeeperService sijoitteluBookkeeperService = new SijoitteluBookkeeperService();
    private final CasClient sijoitteluCasClient;
    private UrlProperties urlProperties;
    private final MockWebServer mockWebServer;
    private final Gson gson;

    private static final String COOKIENAME = "JSESSIONID";
    private static final String VALID_TICKET = "it-ankan-tiketti";

    @Rule
    public ExpectedException thrown = ExpectedException.none();



    public SijoitteluResourceTest() throws MalformedURLException {
        mockWebServer = new MockWebServer();
        sijoitteluBusinessService = mock(SijoitteluBusinessService.class);
        valintatietoService = mock(ValintatietoService.class);
        sijoitteluCasClient = new CasClient(CasConfig.CasConfig("it-ankka",
                "neverstopthemadness",
                mockWebServer.url("/cas").toString(),
                mockWebServer.url("/") + "test-service",
                "CSRF",
                "Caller-Id",
                COOKIENAME,
                "/j_spring_cas_security_check"));

        urlProperties = new TestUrlProperties(mockWebServer.url("/").toString().substring(7, mockWebServer.url("/").toString().length() - 1));


        GsonBuilder builder = new GsonBuilder();

        builder.registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) -> new Date(json.getAsJsonPrimitive().getAsLong()))
                .registerTypeAdapter(Date.class, (JsonSerializer<Date>) (date, type, jsonSerializationContext) -> new JsonPrimitive(date.getTime()));;


        gson = builder.create();

        sijoitteluResource = new SijoitteluResource(
            sijoitteluBusinessService,
            valintatietoService,
            sijoitteluBookkeeperService,
            sijoitteluCasClient,
            urlProperties
        );
    }

    @After
    public void shutDown() throws IOException {
        this.mockWebServer.shutdown();
    }

@Ignore
    @Test
    public void testing() {
            String responseBody = "{\"1.2.246.562.20.85076696401\":[{\"aloituspaikat\":29,\"nimi\":\"Todistusvalinta (YO)\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_tv\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"YLITAYTTO\",\"aktiivinen\":true,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":true,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":true,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":1603141200000,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":\"16000630989123225075305367726935\",\"oid\":\"16000630989123993019540197112205\",\"inheritance\":true,\"prioriteetti\":0},{\"aloituspaikat\":15,\"nimi\":\"Todistusvalinta (AMM)\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_tv\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"YLITAYTTO\",\"aktiivinen\":true,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":true,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":true,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":1603141200000,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":\"16000630989123225075305367726935\",\"oid\":\"16000630989122580573570181467271\",\"inheritance\":true,\"prioriteetti\":1},{\"aloituspaikat\":30,\"nimi\":\"Valintakoevalinta\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_kp\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"YLITAYTTO\",\"aktiivinen\":true,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":true,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":true,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":null,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":null,\"oid\":\"16000630989123225075305367726935\",\"inheritance\":true,\"prioriteetti\":2}],\"1.2.246.562.20.48070545164\":[{\"aloituspaikat\":27,\"nimi\":\"Todistusvalinta (YO)\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_tv\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"YLITAYTTO\",\"aktiivinen\":true,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":true,\"eiVarasijatayttoa\":true,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":false,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":1603141200000,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":\"15991278522614761474767401073795\",\"oid\":\"15991278522612907138318070210847\",\"inheritance\":true,\"prioriteetti\":0},{\"aloituspaikat\":27,\"nimi\":\"Todistusvalinta (AMM)\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_tv\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"YLITAYTTO\",\"aktiivinen\":true,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":true,\"eiVarasijatayttoa\":true,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":false,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":1603141200000,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":\"15991278522613509362396880142258\",\"oid\":\"15991278522614761474767401073795\",\"inheritance\":true,\"prioriteetti\":1},{\"aloituspaikat\":6,\"nimi\":\"Valintakoevalinta\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_kp\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"YLITAYTTO\",\"aktiivinen\":true,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":true,\"eiVarasijatayttoa\":true,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":false,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":null,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":null,\"oid\":\"15991278522613509362396880142258\",\"inheritance\":true,\"prioriteetti\":2}],\"1.2.246.562.20.398726019910\":[{\"aloituspaikat\":42,\"nimi\":\"Todistusvalinta (YO)\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_tv\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"ARVONTA\",\"aktiivinen\":true,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":true,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":true,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":1603141200000,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":null,\"oid\":\"15983532846712166613549122301712\",\"inheritance\":true,\"prioriteetti\":0},{\"aloituspaikat\":5,\"nimi\":\"Todistusvalinta (AMM)\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_tv\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"ARVONTA\",\"aktiivinen\":true,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":true,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":true,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":1603141200000,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":null,\"oid\":\"1598353284671-2989019967526601780\",\"inheritance\":true,\"prioriteetti\":1},{\"aloituspaikat\":31,\"nimi\":\"Valintakoevalinta\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_kp\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"ARVONTA\",\"aktiivinen\":true,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":true,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":true,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":null,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":null,\"oid\":\"1598353284671-4219724595940692775\",\"inheritance\":true,\"prioriteetti\":2}],\"1.2.246.562.20.90887234673\":[{\"aloituspaikat\":11,\"nimi\":\"Todistusvalinta (YO)\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_tv\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"ARVONTA\",\"aktiivinen\":true,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":true,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":true,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":1603141200000,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":null,\"oid\":\"15967145796756192415742255156775\",\"inheritance\":true,\"prioriteetti\":0},{\"aloituspaikat\":4,\"nimi\":\"Todistusvalinta (AMM)\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_tv\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"ARVONTA\",\"aktiivinen\":true,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":true,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":true,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":1603141200000,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":null,\"oid\":\"1596714579675-1321343671060208046\",\"inheritance\":true,\"prioriteetti\":1},{\"aloituspaikat\":18,\"nimi\":\"Valintakoevalinta\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_kp\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"ARVONTA\",\"aktiivinen\":true,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":true,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":true,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":null,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":null,\"oid\":\"1596714579675-4679967825167896342\",\"inheritance\":true,\"prioriteetti\":2}],\"1.2.246.562.20.30060712855\":[{\"aloituspaikat\":40,\"nimi\":\"Yhteispisteet jono\",\"kuvaus\":\"Valintakurssin ja ryhmätehtävän yhteispisteet\",\"tyyppi\":\"valintatapajono_yp\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"ARVONTA\",\"aktiivinen\":true,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":true,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":true,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":null,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":null,\"oid\":\"1599717690661-1154508905353914393\",\"inheritance\":true,\"prioriteetti\":0},{\"aloituspaikat\":0,\"nimi\":\"Todistusvalinta (YO)\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_tv\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"YLITAYTTO\",\"aktiivinen\":false,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":false,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":false,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":null,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":null,\"oid\":\"15996372231238708475746281410943\",\"inheritance\":true,\"prioriteetti\":1},{\"aloituspaikat\":0,\"nimi\":\"Todistusvalinta (AMM)\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_tv\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"YLITAYTTO\",\"aktiivinen\":false,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":false,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":false,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":null,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":null,\"oid\":\"1599637223123215940169783768723\",\"inheritance\":true,\"prioriteetti\":2},{\"aloituspaikat\":0,\"nimi\":\"Valintakoevalinta\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_kp\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"YLITAYTTO\",\"aktiivinen\":false,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":false,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":false,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":null,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":null,\"oid\":\"15996372231232253633776810333039\",\"inheritance\":true,\"prioriteetti\":3}],\"1.2.246.562.20.25652228419\":[{\"aloituspaikat\":20,\"nimi\":\"Valintakoevalinta\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_kp\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"ARVONTA\",\"aktiivinen\":true,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":true,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":true,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":1607464800000,\"eiLasketaPaivamaaranJalkeen\":null,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":null,\"oid\":\"15984351065881676427354209244774\",\"inheritance\":true,\"prioriteetti\":0}],\"1.2.246.562.20.64331782058\":[{\"aloituspaikat\":7,\"nimi\":\"Todistusvalinta (YO)\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_tv\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"ARVONTA\",\"aktiivinen\":true,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":true,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":true,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":1603141200000,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":null,\"oid\":\"15954873893112231315345948367245\",\"inheritance\":true,\"prioriteetti\":0},{\"aloituspaikat\":7,\"nimi\":\"Todistusvalinta (AMM)\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_tv\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"ARVONTA\",\"aktiivinen\":true,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":true,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":true,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":1603141200000,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":null,\"oid\":\"1595487389311-2252236464859404790\",\"inheritance\":true,\"prioriteetti\":1},{\"aloituspaikat\":10,\"nimi\":\"Valintakoevalinta\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_kp\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"ARVONTA\",\"aktiivinen\":true,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":true,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":true,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":null,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":null,\"oid\":\"1595487389311-4521065734020287932\",\"inheritance\":true,\"prioriteetti\":2}],\"1.2.246.562.20.18622071621\":[{\"aloituspaikat\":30,\"nimi\":\"Yhteispisteet jono\",\"kuvaus\":\"Valintakurssin ja ryhmätehtävän yhteispisteet\",\"tyyppi\":\"valintatapajono_yp\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"ARVONTA\",\"aktiivinen\":true,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":true,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":true,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":null,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":null,\"oid\":\"1599717690617-6389549514882627552\",\"inheritance\":true,\"prioriteetti\":0},{\"aloituspaikat\":0,\"nimi\":\"Todistusvalinta (YO)\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_tv\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"YLITAYTTO\",\"aktiivinen\":false,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":false,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":false,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":null,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":null,\"oid\":\"1599637141466-5580054662220977745\",\"inheritance\":true,\"prioriteetti\":1},{\"aloituspaikat\":0,\"nimi\":\"Todistusvalinta (AMM)\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_tv\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"YLITAYTTO\",\"aktiivinen\":false,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":false,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":false,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":null,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":null,\"oid\":\"1599637141466-7639407564410478293\",\"inheritance\":true,\"prioriteetti\":2},{\"aloituspaikat\":0,\"nimi\":\"Valintakoevalinta\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_kp\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"YLITAYTTO\",\"aktiivinen\":false,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":false,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":false,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":null,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":null,\"oid\":\"1599637141466-5666637200970334610\",\"inheritance\":true,\"prioriteetti\":3}],\"1.2.246.562.20.10347993502\":[{\"aloituspaikat\":43,\"nimi\":\"Valintakurssi\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_m\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"YLITAYTTO\",\"aktiivinen\":true,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":true,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":true,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":null,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":null,\"oid\":\"16021417143344574877863930128068\",\"inheritance\":true,\"prioriteetti\":0},{\"aloituspaikat\":0,\"nimi\":\"Todistusvalinta (YO)\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_tv\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"YLITAYTTO\",\"aktiivinen\":false,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":false,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":false,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":null,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":null,\"oid\":\"1600155635265-2771559489647712535\",\"inheritance\":true,\"prioriteetti\":1},{\"aloituspaikat\":0,\"nimi\":\"Todistusvalinta (AMM)\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_tv\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"YLITAYTTO\",\"aktiivinen\":false,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":false,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":false,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":null,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":null,\"oid\":\"1600155635265-1230246106513274190\",\"inheritance\":true,\"prioriteetti\":2},{\"aloituspaikat\":0,\"nimi\":\"Valintakoevalinta\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_kp\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"YLITAYTTO\",\"aktiivinen\":false,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":false,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":false,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":null,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":null,\"oid\":\"1600155635265-4907186014450547839\",\"inheritance\":true,\"prioriteetti\":3}],\"1.2.246.562.20.43464871367\":[{\"aloituspaikat\":22,\"nimi\":\"Valintakurssi\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_m\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"ARVONTA\",\"aktiivinen\":true,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":true,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":true,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":null,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":null,\"oid\":\"16021421811517998127438312025768\",\"inheritance\":true,\"prioriteetti\":0},{\"aloituspaikat\":0,\"nimi\":\"Todistusvalinta (YO)\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_tv\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"YLITAYTTO\",\"aktiivinen\":false,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":false,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":false,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":null,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":null,\"oid\":\"16001551751771891712041895682402\",\"inheritance\":true,\"prioriteetti\":1},{\"aloituspaikat\":0,\"nimi\":\"Todistusvalinta (AMM)\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_tv\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"YLITAYTTO\",\"aktiivinen\":false,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":false,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":false,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":null,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":null,\"oid\":\"1600155175177-3520055960965364020\",\"inheritance\":true,\"prioriteetti\":2},{\"aloituspaikat\":0,\"nimi\":\"Valintakoevalinta\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_kp\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"YLITAYTTO\",\"aktiivinen\":false,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":false,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":false,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":null,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":null,\"oid\":\"1600155175177-8293493591841363572\",\"inheritance\":true,\"prioriteetti\":3}],\"1.2.246.562.20.58482684998\":[{\"aloituspaikat\":14,\"nimi\":\"Todistusvalinta (YO)\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_tv\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"ARVONTA\",\"aktiivinen\":true,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":true,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":true,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":1604181600000,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":null,\"oid\":\"1598960319697-3218611278595405375\",\"inheritance\":true,\"prioriteetti\":0},{\"aloituspaikat\":4,\"nimi\":\"Todistusvalinta (AMM)\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_tv\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"ARVONTA\",\"aktiivinen\":true,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":true,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":true,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":1604181600000,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":null,\"oid\":\"1598960319697945777970232562433\",\"inheritance\":true,\"prioriteetti\":1},{\"aloituspaikat\":17,\"nimi\":\"Valintakoevalinta\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_kp\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"ARVONTA\",\"aktiivinen\":true,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":true,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":true,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":null,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":null,\"oid\":\"1598960319697-6396322149503611352\",\"inheritance\":true,\"prioriteetti\":2}],\"1.2.246.562.20.89811705006\":[{\"aloituspaikat\":30,\"nimi\":\"Yhteispisteet jono\",\"kuvaus\":\"Valintakurssin ja haastattelun yhteispisteet\",\"tyyppi\":\"valintatapajono_yp\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"ARVONTA\",\"aktiivinen\":true,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":true,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":true,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":null,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":null,\"oid\":\"15997176907498155369408465874782\",\"inheritance\":true,\"prioriteetti\":0},{\"aloituspaikat\":0,\"nimi\":\"Todistusvalinta (YO)\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_tv\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"YLITAYTTO\",\"aktiivinen\":false,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":false,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":false,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":null,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":null,\"oid\":\"1599637185966-4084999312469011931\",\"inheritance\":true,\"prioriteetti\":1},{\"aloituspaikat\":0,\"nimi\":\"Todistusvalinta (AMM)\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_tv\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"YLITAYTTO\",\"aktiivinen\":false,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":false,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":false,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":null,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":null,\"oid\":\"1599637185966-1466513565462764322\",\"inheritance\":true,\"prioriteetti\":2},{\"aloituspaikat\":0,\"nimi\":\"Valintakoevalinta\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_kp\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"YLITAYTTO\",\"aktiivinen\":false,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":false,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":false,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":null,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":null,\"oid\":\"15996371859667004053163134697649\",\"inheritance\":true,\"prioriteetti\":3}],\"1.2.246.562.20.33509227444\":[{\"aloituspaikat\":14,\"nimi\":\"Todistusvalinta (YO)\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_tv\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"ARVONTA\",\"aktiivinen\":true,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":true,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":true,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":null,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":\"1598445116049-9168825185358978216\",\"oid\":\"15984451160492427449733671455814\",\"inheritance\":true,\"prioriteetti\":0},{\"aloituspaikat\":14,\"nimi\":\"Todistusvalinta (AMM)\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_tv\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"ARVONTA\",\"aktiivinen\":true,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":true,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":true,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":null,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":\"1598445116049-9168825185358978216\",\"oid\":\"1598445116049-8820061587020511388\",\"inheritance\":true,\"prioriteetti\":1},{\"aloituspaikat\":7,\"nimi\":\"Valintakoevalinta\",\"kuvaus\":null,\"tyyppi\":\"valintatapajono_kp\",\"siirretaanSijoitteluun\":true,\"tasapistesaanto\":\"ARVONTA\",\"aktiivinen\":true,\"valisijoittelu\":false,\"automaattinenSijoitteluunSiirto\":true,\"eiVarasijatayttoa\":false,\"kaikkiEhdonTayttavatHyvaksytaan\":false,\"varasijat\":0,\"varasijaTayttoPaivat\":0,\"poissaOlevaTaytto\":true,\"poistetaankoHylatyt\":false,\"varasijojaKaytetaanAlkaen\":null,\"varasijojaTaytetaanAsti\":null,\"eiLasketaPaivamaaranJalkeen\":null,\"kaytetaanValintalaskentaa\":true,\"tayttojono\":null,\"oid\":\"1598445116049-9168825185358978216\",\"inheritance\":true,\"prioriteetti\":2}]}\n";


            Type token = new TypeToken<Map<String, List<ValintatapajonoDTO>>>(){}.getType();
            Map<String, List<ValintatapajonoDTO>> valintaperusteet = new HashMap<String, List<ValintatapajonoDTO>>();
            try {
                valintaperusteet = this.gson.fromJson(responseBody, token);
            } catch (JsonSyntaxException e) {
                System.out.println("FAILED");
                e.printStackTrace();
            }

            System.out.println(valintaperusteet);

        }

    @Test
    public void testaaSijoittelunLuonti() {
        String haku1 = "1.2.3.4444";

        Long id = sijoitteluResource.sijoittele(haku1);
        Long id2 = sijoitteluResource.sijoittele(haku1);

        String tila = sijoitteluResource.sijoittelunTila(id);
        String tila2 = sijoitteluResource.sijoittelunTila(id2);

        assertEquals(Long.valueOf(-1), id2);
        assertThat(id, Matchers.greaterThan(0L));
        assertEquals(SijoitteluajonTila.KESKEN.toString(), tila);
        assertEquals(SijoitteluajonTila.EI_LOYTYNYT.toString(), tila2);
    }

    @Test
    public void testaaSijoitteluReitti() {
        final String hakukohdeOid = UUID.randomUUID().toString();
        final String hakijaryhmaOid = UUID.randomUUID().toString();
        final String valintatapajononHakijaryhmaOid = UUID.randomUUID().toString();
        try {
            ValintatietoValinnanvaiheDTO laskennasta = createValintatietoValinnanvaiheDTO();
            ValintatapajonoDTO valintaperusteista = createValintatapajonoDTO();
            HakijaryhmaValintatapajonoDTO hakijaryhmavalintaperusteista =
                createHakijaryhmaValintatapajonoDTO(hakijaryhmaOid);
            HakijaryhmaValintatapajonoDTO valintatapajononHakijaryhmavalintaperusteista =
                createHakijaryhmaValintatapajonoDTO(valintatapajononHakijaryhmaOid);
            final String valintatapajonoOid = laskennasta.getValintatapajonot().iterator().next().getOid();
            valintaperusteista.setOid(valintatapajonoOid);
            HakuDTO haku = createHakuDTO(
                EMPTY,
                hakukohdeOid,
                createHakijaryhmaDTO(hakijaryhmaOid),
                createHakijaryhmaDTO(valintatapajononHakijaryhmaOid),
                laskennasta);
            {
                when(valintatietoService.haeValintatiedot(anyString())).thenReturn(haku);
                mockWebServer.enqueue(new MockResponse()
                        .addHeader("Location", mockWebServer.url("/") + "cas/tickets")
                        .setResponseCode(201));
                mockWebServer.enqueue(new MockResponse()
                        .setBody(VALID_TICKET)
                        .setResponseCode(200));
                mockWebServer.enqueue(new MockResponse()
                        .setBody(this.gson.toJson(asList(hakijaryhmavalintaperusteista)))
                        .setResponseCode(200));
                mockWebServer.enqueue(new MockResponse()
                        .addHeader("Location", mockWebServer.url("/") + "cas/tickets")
                        .setResponseCode(201));
                mockWebServer.enqueue(new MockResponse()
                        .setBody(VALID_TICKET)
                        .setResponseCode(200));
                mockWebServer.enqueue(new MockResponse()
                        .setBody(this.gson.toJson(asList(valintatapajononHakijaryhmavalintaperusteista)))
                        .setResponseCode(200));

                final HashMap<String, List<ValintatapajonoDTO>> vpMap = new HashMap<>();
                vpMap.put(hakukohdeOid, Arrays.asList(valintaperusteista));

                mockWebServer.enqueue(new MockResponse()
                        .addHeader("Location", mockWebServer.url("/") + "cas/tickets")
                        .setResponseCode(201));
                mockWebServer.enqueue(new MockResponse()
                        .setBody(VALID_TICKET)
                        .setResponseCode(200));
                mockWebServer.enqueue(new MockResponse().setBody(this.gson.toJson(vpMap)));

                try {
                    sijoitteluResource.toteutaSijoittelu(EMPTY, 12345L);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }

            verify(sijoitteluBusinessService, times(1)).sijoittele(haku, new HashSet<>(), Sets.newHashSet(valintatapajonoOid), 12345L);

            HakukohdeDTO hakukohde = haku.getHakukohteet().iterator().next();
            /// ASSERTOIDAAN ETTA JONON TIEDOT PAIVITTYY
            {
                ValintatietoValintatapajonoDTO jono = hakukohde.getValinnanvaihe().iterator().next().getValintatapajonot().iterator().next();
                ValintatietoValintatapajonoDTO alkuperainen = createValintatietoValinnanvaiheDTO().getValintatapajonot().iterator().next();
                assertThat(jono.getAloituspaikat(), is(valintaperusteista.getAloituspaikat()));
                assertNotSame(jono.getAloituspaikat(), alkuperainen.getAloituspaikat());

                assertThat(jono.getEiVarasijatayttoa(), is(valintaperusteista.getEiVarasijatayttoa()));
                assertNotSame(jono.getEiVarasijatayttoa(), alkuperainen.getEiVarasijatayttoa());

                assertThat(jono.getNimi(), is(valintaperusteista.getNimi()));
                assertNotSame(jono.getNimi(), alkuperainen.getNimi());

                assertThat(jono.getTasasijasaanto(), is(EnumConverter
                    .convert(
                        Tasasijasaanto.class, valintaperusteista.getTasapistesaanto())));
                assertNotSame(jono.getTasasijasaanto(), alkuperainen.getTasasijasaanto());
            }
            /// ASSERTOIDAAN ETTA HAKIJARYHMAN TIEDOT PAIVITTYY
            {
                HakijaryhmaDTO hakijaryhma = hakukohde.getHakijaryhma().iterator().next();
                HakijaryhmaDTO alkuperainen = createHakijaryhmaDTO(hakijaryhmaOid);

                assertNotSame(hakijaryhma.isKaytaKaikki(), alkuperainen.isKaytaKaikki());
                assertNotSame(hakijaryhma.isKaytetaanRyhmaanKuuluvia(), alkuperainen.isKaytetaanRyhmaanKuuluvia());
                assertNotSame(hakijaryhma.getKiintio(), alkuperainen.getKiintio());
                assertNotSame(hakijaryhma.getKuvaus(), alkuperainen.getKuvaus());
                assertNotSame(hakijaryhma.getNimi(), alkuperainen.getNimi());
                assertNotSame(hakijaryhma.isTarkkaKiintio(), alkuperainen.isTarkkaKiintio());
                assertNotSame(hakijaryhma.getHakijaryhmatyyppikoodiUri(), alkuperainen.getHakijaryhmatyyppikoodiUri());

                assertThat(hakijaryhma.isKaytaKaikki(), is(hakijaryhmavalintaperusteista.isKaytaKaikki()));
                assertThat(hakijaryhma.isKaytetaanRyhmaanKuuluvia(), is(hakijaryhmavalintaperusteista.isKaytetaanRyhmaanKuuluvia()));
                assertThat(hakijaryhma.getKiintio(), is(hakijaryhmavalintaperusteista.getKiintio()));
                assertThat(hakijaryhma.getKuvaus(), is(hakijaryhmavalintaperusteista.getKuvaus()));
                assertThat(hakijaryhma.getNimi(), is(hakijaryhmavalintaperusteista.getNimi()));
                assertThat(hakijaryhma.isTarkkaKiintio(), is(hakijaryhmavalintaperusteista.isTarkkaKiintio()));
                assertThat(hakijaryhma.getHakijaryhmatyyppikoodiUri(), is(hakijaryhmavalintaperusteista.getHakijaryhmatyyppikoodi().getUri()));

                hakijaryhma = hakukohde.getHakijaryhma().iterator().next();
                alkuperainen = createHakijaryhmaDTO(valintatapajononHakijaryhmaOid);

                assertNotSame(hakijaryhma.isKaytaKaikki(), alkuperainen.isKaytaKaikki());
                assertNotSame(hakijaryhma.isKaytetaanRyhmaanKuuluvia(), alkuperainen.isKaytetaanRyhmaanKuuluvia());
                assertNotSame(hakijaryhma.getKiintio(), alkuperainen.getKiintio());
                assertNotSame(hakijaryhma.getKuvaus(), alkuperainen.getKuvaus());
                assertNotSame(hakijaryhma.getNimi(), alkuperainen.getNimi());
                assertNotSame(hakijaryhma.isTarkkaKiintio(), alkuperainen.isTarkkaKiintio());
                assertNotSame(hakijaryhma.getHakijaryhmatyyppikoodiUri(), alkuperainen.getHakijaryhmatyyppikoodiUri());

                assertThat(hakijaryhma.isKaytaKaikki(), is(hakijaryhmavalintaperusteista.isKaytaKaikki()));
                assertThat(hakijaryhma.isKaytetaanRyhmaanKuuluvia(), is(hakijaryhmavalintaperusteista.isKaytetaanRyhmaanKuuluvia()));
                assertThat(hakijaryhma.getKiintio(), is(hakijaryhmavalintaperusteista.getKiintio()));
                assertThat(hakijaryhma.getKuvaus(), is(hakijaryhmavalintaperusteista.getKuvaus()));
                assertThat(hakijaryhma.getNimi(), is(hakijaryhmavalintaperusteista.getNimi()));
                assertThat(hakijaryhma.isTarkkaKiintio(), is(hakijaryhmavalintaperusteista.isTarkkaKiintio()));
                assertThat(hakijaryhma.getHakijaryhmatyyppikoodiUri(), is(hakijaryhmavalintaperusteista.getHakijaryhmatyyppikoodi().getUri()));
            }
        } finally {
            reset(sijoitteluBusinessService, valintatietoService);
        }
    }

    @Test
    public void valintaperusteistaKadonneetJonotHuomataanJoTietojaLadatessa() {
        String hakuOid = "hakuOid";
        final String hakukohdeOid = "hakukohdeOid";
        final String hakijaryhmaOid = UUID.randomUUID().toString();
        final String valintatapajononHakijaryhmaOid = UUID.randomUUID().toString();
        try {
            ValintatietoValinnanvaiheDTO laskennasta = createValintatietoValinnanvaiheDTO();
            ValintatapajonoDTO valintaperusteista = createValintatapajonoDTO();
            HakijaryhmaValintatapajonoDTO hakijaryhmavalintaperusteista = createHakijaryhmaValintatapajonoDTO(hakijaryhmaOid);
            HakijaryhmaValintatapajonoDTO valintatapajononHakijaryhmavalintaperusteista =
                createHakijaryhmaValintatapajonoDTO(valintatapajononHakijaryhmaOid);
            ValintatietoValintatapajonoDTO laskennanValintatapajono = laskennasta.getValintatapajonot().iterator().next();
            final String valintatapajonoOid = "valintatapaJonoOid";
            laskennanValintatapajono.setOid(valintatapajonoOid);
            laskennanValintatapajono.setSiirretaanSijoitteluun(true);
            laskennanValintatapajono.setNimi("Varsinainen testivalinta");
            valintaperusteista.setOid(valintatapajonoOid + "-broken");
            HakuDTO haku = createHakuDTO(
                hakuOid,
                hakukohdeOid,
                createHakijaryhmaDTO(hakijaryhmaOid),
                createHakijaryhmaDTO(valintatapajononHakijaryhmaOid),
                laskennasta);
            when(valintatietoService.haeValintatiedot(hakuOid)).thenReturn(haku);
            mockWebServer.enqueue(new MockResponse()
                    .addHeader("Location", mockWebServer.url("/") + "cas/tickets")
                    .setResponseCode(201));
            mockWebServer.enqueue(new MockResponse()
                    .setBody(VALID_TICKET)
                    .setResponseCode(200));
            mockWebServer.enqueue(new MockResponse()
                    .setBody(this.gson.toJson(Collections.singletonList(hakijaryhmavalintaperusteista)))
                    .setResponseCode(200));
            mockWebServer.enqueue(new MockResponse()
                    .addHeader("Location", mockWebServer.url("/") + "cas/tickets")
                    .setResponseCode(201));
            mockWebServer.enqueue(new MockResponse()
                    .setBody(VALID_TICKET)
                    .setResponseCode(200));
            mockWebServer.enqueue(new MockResponse()
                    .setBody(this.gson.toJson(Collections.singletonList(valintatapajononHakijaryhmavalintaperusteista)))
                    .setResponseCode(200));

            final HashMap<String, List<ValintatapajonoDTO>> vpMap = new HashMap<>();
            vpMap.put(hakukohdeOid, Collections.singletonList(valintaperusteista));

            mockWebServer.enqueue(new MockResponse()
                    .addHeader("Location", mockWebServer.url("/") + "cas/tickets")
                    .setResponseCode(201));
            mockWebServer.enqueue(new MockResponse()
                    .setBody(VALID_TICKET)
                    .setResponseCode(200));
            mockWebServer.enqueue(new MockResponse()
                    .setBody(this.gson.toJson(vpMap))
                    .setResponseCode(200));
            thrown.expect(IllegalStateException.class);
            thrown.expectMessage("Haun hakuOid sijoittelu : " +
                "Laskennan tuloksista löytyvien jonojen tietoja on kadonnut valintaperusteista: " +
                "[Hakukohde hakukohdeOid , jono \"Varsinainen testivalinta\" (valintatapaJonoOid , prio 0)]");
            try {
                sijoitteluResource.toteutaSijoittelu(hakuOid, 12345L);
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        } finally{
            reset(sijoitteluBusinessService, valintatietoService);
        }
    }

    private ValintatapajonoDTO createValintatapajonoDTO() {
        ValintatapajonoDTO valintatapajonoDTO = new ValintatapajonoDTO();
        valintatapajonoDTO.setOid(UUID.randomUUID().toString());
        valintatapajonoDTO.setAktiivinen(true);
        valintatapajonoDTO.setAloituspaikat(Integer.MIN_VALUE);
        valintatapajonoDTO.setautomaattinenSijoitteluunSiirto(true);
        valintatapajonoDTO.setEiVarasijatayttoa(false);
        valintatapajonoDTO.setKaikkiEhdonTayttavatHyvaksytaan(false);
        valintatapajonoDTO.setKaytetaanValintalaskentaa(false);
        valintatapajonoDTO.setKuvaus(UUID.randomUUID().toString());
        valintatapajonoDTO.setNimi(UUID.randomUUID().toString());
        valintatapajonoDTO.setPoissaOlevaTaytto(false);
        valintatapajonoDTO.setSiirretaanSijoitteluun(false);
        valintatapajonoDTO.setTasapistesaanto(Tasapistesaanto.ARVONTA);
        valintatapajonoDTO.setTayttojono(UUID.randomUUID().toString());
        valintatapajonoDTO.setValisijoittelu(false);
        valintatapajonoDTO.setVarasijat(Integer.MIN_VALUE);
        valintatapajonoDTO.setVarasijaTayttoPaivat(Integer.MIN_VALUE);
        valintatapajonoDTO.setVarasijojaKaytetaanAlkaen(new Date());
        valintatapajonoDTO.setVarasijojaTaytetaanAsti(new Date());
        return valintatapajonoDTO;
    }

    private ValintatietoValinnanvaiheDTO createValintatietoValinnanvaiheDTO() {
        ValintatietoValinnanvaiheDTO valintatietoValinnanvaiheDTO = new ValintatietoValinnanvaiheDTO();
        ValintatietoValintatapajonoDTO valintatietoValintatapajonoDTO = new ValintatietoValintatapajonoDTO();
        valintatietoValintatapajonoDTO.setOid(UUID.randomUUID().toString());
        valintatietoValintatapajonoDTO.setAloituspaikat(Integer.MAX_VALUE);
        valintatietoValintatapajonoDTO.setEiVarasijatayttoa(true);
        valintatietoValintatapajonoDTO.setPoissaOlevaTaytto(true);
        valintatietoValintatapajonoDTO.setTasasijasaanto(Tasasijasaanto.ALITAYTTO);
        valintatietoValintatapajonoDTO.setTayttojono(UUID.randomUUID().toString());
        valintatietoValintatapajonoDTO.setVarasijat(Integer.MAX_VALUE);
        valintatietoValintatapajonoDTO.setVarasijaTayttoPaivat(Integer.MAX_VALUE);
        valintatietoValintatapajonoDTO.setVarasijojaKaytetaanAlkaen(new Date());
        valintatietoValintatapajonoDTO.setVarasijojaTaytetaanAsti(new Date());
        valintatietoValintatapajonoDTO.setAktiivinen(true);
        valintatietoValintatapajonoDTO.setKaikkiEhdonTayttavatHyvaksytaan(true);
        valintatietoValintatapajonoDTO.setNimi(UUID.randomUUID().toString());
        valintatietoValinnanvaiheDTO.setValintatapajonot(asList(valintatietoValintatapajonoDTO));
        return valintatietoValinnanvaiheDTO;
    }

    private HakuDTO createHakuDTO(String hakuOid,
                                  String hakukohdeOid,
                                  HakijaryhmaDTO hakijaryhma,
                                  HakijaryhmaDTO valintatapajononHakijaryhma,
                                  ValintatietoValinnanvaiheDTO valinnanvaihe) {
        HakuDTO haku = new HakuDTO();
        haku.setHakuOid(hakuOid);
        HakukohdeDTO hakukohde = new HakukohdeDTO();
        hakukohde.setOid(hakukohdeOid);
        hakukohde.setHakijaryhma(asList(hakijaryhma, valintatapajononHakijaryhma));
        hakukohde.setValinnanvaihe(asList(valinnanvaihe));
        haku.setHakukohteet(asList(hakukohde));
        return haku;
    }

    private HakijaryhmaDTO createHakijaryhmaDTO(String hakijaryhmaOid) {
        HakijaryhmaDTO hakijaryhma = new HakijaryhmaDTO();
        hakijaryhma.setHakijaryhmaOid(hakijaryhmaOid);
        hakijaryhma.setKiintio(0);
        hakijaryhma.setKaytetaanRyhmaanKuuluvia(false);
        hakijaryhma.setKaytaKaikki(false);
        hakijaryhma.setKuvaus(EMPTY);
        hakijaryhma.setNimi(EMPTY);
        hakijaryhma.setTarkkaKiintio(false);
        hakijaryhma.setHakijaryhmatyyppikoodiUri(EMPTY);
        return hakijaryhma;
    }
    private HakijaryhmaValintatapajonoDTO createHakijaryhmaValintatapajonoDTO(String hakijaryhmaOid) {
        HakijaryhmaValintatapajonoDTO hakijaryhmaValintatapajonoDTO = new HakijaryhmaValintatapajonoDTO();
        hakijaryhmaValintatapajonoDTO.setOid(hakijaryhmaOid);
        hakijaryhmaValintatapajonoDTO.setAktiivinen(true);
        hakijaryhmaValintatapajonoDTO.setKaytaKaikki(true);
        hakijaryhmaValintatapajonoDTO.setKaytetaanRyhmaanKuuluvia(true);
        hakijaryhmaValintatapajonoDTO.setKiintio(Integer.MAX_VALUE);
        hakijaryhmaValintatapajonoDTO.setKuvaus(UUID.randomUUID().toString());
        hakijaryhmaValintatapajonoDTO.setNimi(UUID.randomUUID().toString());
        hakijaryhmaValintatapajonoDTO.setTarkkaKiintio(true);
        KoodiDTO koodi = new KoodiDTO();
        koodi.setUri("koodi_uri");
        hakijaryhmaValintatapajonoDTO.setHakijaryhmatyyppikoodi(koodi);
        return hakijaryhmaValintatapajonoDTO;
    }
}
