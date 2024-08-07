package fi.vm.sade.sijoittelu.laskenta.resource;

import com.google.common.collect.Sets;
import com.google.gson.*;
import fi.vm.sade.javautils.nio.cas.CasClient;
import fi.vm.sade.javautils.nio.cas.CasClientBuilder;
import fi.vm.sade.javautils.nio.cas.CasConfig;
import fi.vm.sade.service.valintaperusteet.dto.HakijaryhmaValintatapajonoDTO;
import fi.vm.sade.service.valintaperusteet.dto.KoodiDTO;
import fi.vm.sade.service.valintaperusteet.dto.ValintatapajonoDTO;
import fi.vm.sade.service.valintaperusteet.dto.model.Tasapistesaanto;
import fi.vm.sade.sijoittelu.domain.SijoitteluajonTila;
import fi.vm.sade.sijoittelu.laskenta.email.EmailService;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBookkeeperService;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.sijoittelu.laskenta.service.business.ToteutaSijoitteluService;
import fi.vm.sade.sijoittelu.laskenta.service.it.TarjontaIntegrationService;
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
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.*;

/**
 * @author Jussi Jartamo
 */
public class SijoitteluResourceTest {

    private final SijoitteluResource sijoitteluResource;
    private final ToteutaSijoitteluService toteutaSijoitteluService;
    private final SijoitteluBusinessService sijoitteluBusinessService;
    private final ValintatietoService valintatietoService;
    private final SijoitteluBookkeeperService sijoitteluBookkeeperService = new SijoitteluBookkeeperService();
    private final CasClient sijoitteluCasClient;
    private UrlProperties urlProperties;
    private final MockWebServer mockWebServer;
    private final Gson gson;

    private static final String COOKIENAME = "JSESSIONID";
    private static final String VALID_TICKET = "it-ankan-tiketti";

    public SijoitteluResourceTest() throws MalformedURLException {
        mockWebServer = new MockWebServer();
        sijoitteluBusinessService = mock(SijoitteluBusinessService.class);
        valintatietoService = mock(ValintatietoService.class);
        sijoitteluCasClient = CasClientBuilder.build(new CasConfig.CasConfigBuilder("it-ankka",
                "neverstopthemadness",
                mockWebServer.url("/cas").toString(),
                mockWebServer.url("/") + "test-service",
                "CSRF",
                "Caller-Id",
                "/j_spring_cas_security_check").setJsessionName(COOKIENAME).build());

        urlProperties = new TestUrlProperties(mockWebServer.url("/").toString().substring(7, mockWebServer.url("/").toString().length() - 1));
        toteutaSijoitteluService = new ToteutaSijoitteluService(
            sijoitteluBusinessService,
            valintatietoService,
            sijoitteluBookkeeperService,
            sijoitteluCasClient,
            urlProperties,
            mock(TarjontaIntegrationService.class),
            mock(EmailService.class)
        );

        this.gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) ->
                        new Date(json.getAsJsonPrimitive().getAsLong()))
                .registerTypeAdapter(Date.class, (JsonSerializer<Date>) (date, type, jsonSerializationContext) ->
                        new JsonPrimitive(date.getTime()))
                .create();
        sijoitteluResource = new SijoitteluResource(
            toteutaSijoitteluService,
            sijoitteluBusinessService,
            valintatietoService,
            sijoitteluBookkeeperService,
            sijoitteluCasClient,
            urlProperties,
            mock(TarjontaIntegrationService.class),
            mock(EmailService.class)
        );
    }

    @AfterEach
    public void shutDown() throws IOException {
        this.mockWebServer.shutdown();
    }

    @Test
    public void testaaSijoittelunLuonti() {
        String haku1 = "1.2.3.4444";

        Long id = sijoitteluResource.sijoittele(haku1);
        Long id2 = sijoitteluResource.sijoittele(haku1);

        String tila = sijoitteluResource.sijoittelunTila(id);
        String tila2 = sijoitteluResource.sijoittelunTila(id2);

        Assertions.assertEquals(Long.valueOf(-1), id2);
        MatcherAssert.assertThat(id, Matchers.greaterThan(0L));
        Assertions.assertEquals(SijoitteluajonTila.KESKEN.toString(), tila);
        Assertions.assertEquals(SijoitteluajonTila.EI_LOYTYNYT.toString(), tila2);
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
                        .addHeader("Location", mockWebServer.url("/") + "cas/v1/tickets/TGT-1")
                        .setResponseCode(201));
                mockWebServer.enqueue(new MockResponse()
                        .setBody(VALID_TICKET)
                        .setResponseCode(200));
                mockWebServer.enqueue(new MockResponse()
                        .setHeader("Set-Cookie", "JSESSIONID=XYZ")
                        .setBody(VALID_TICKET)
                        .setResponseCode(200));
                mockWebServer.enqueue(new MockResponse()
                        .setBody(this.gson.toJson(asList(hakijaryhmavalintaperusteista)))
                        .setResponseCode(200));
                mockWebServer.enqueue(new MockResponse()
                        .setBody(this.gson.toJson(asList(valintatapajononHakijaryhmavalintaperusteista)))
                        .setResponseCode(200));

                final HashMap<String, List<ValintatapajonoDTO>> vpMap = new HashMap<>();
                vpMap.put(hakukohdeOid, Arrays.asList(valintaperusteista));

                mockWebServer.enqueue(new MockResponse().setBody(this.gson.toJson(vpMap)));

                try {
                    toteutaSijoitteluService.toteutaSijoittelu(EMPTY, 12345L);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }

            verify(sijoitteluBusinessService, times(1)).sijoittele(eq(haku), any(), eq(Sets.newHashSet(valintatapajonoOid)), eq(12345L), any());

            HakukohdeDTO hakukohde = haku.getHakukohteet().iterator().next();
            /// ASSERTOIDAAN ETTA JONON TIEDOT PAIVITTYY
            {
                ValintatietoValintatapajonoDTO jono = hakukohde.getValinnanvaihe().iterator().next().getValintatapajonot().iterator().next();
                ValintatietoValintatapajonoDTO alkuperainen = createValintatietoValinnanvaiheDTO().getValintatapajonot().iterator().next();
                MatcherAssert.assertThat(jono.getAloituspaikat(), is(valintaperusteista.getAloituspaikat()));
                Assertions.assertNotSame(jono.getAloituspaikat(), alkuperainen.getAloituspaikat());

                MatcherAssert.assertThat(jono.getEiVarasijatayttoa(), is(valintaperusteista.getEiVarasijatayttoa()));
                Assertions.assertNotSame(jono.getEiVarasijatayttoa(), alkuperainen.getEiVarasijatayttoa());

                MatcherAssert.assertThat(jono.getNimi(), is(valintaperusteista.getNimi()));
                Assertions.assertNotSame(jono.getNimi(), alkuperainen.getNimi());

                MatcherAssert.assertThat(jono.getTasasijasaanto(), is(EnumConverter
                    .convert(
                        Tasasijasaanto.class, valintaperusteista.getTasapistesaanto())));
                Assertions.assertNotSame(jono.getTasasijasaanto(), alkuperainen.getTasasijasaanto());
            }
            /// ASSERTOIDAAN ETTA HAKIJARYHMAN TIEDOT PAIVITTYY
            {
                HakijaryhmaDTO hakijaryhma = hakukohde.getHakijaryhma().iterator().next();
                HakijaryhmaDTO alkuperainen = createHakijaryhmaDTO(hakijaryhmaOid);

                Assertions.assertNotSame(hakijaryhma.isKaytaKaikki(), alkuperainen.isKaytaKaikki());
                Assertions.assertNotSame(hakijaryhma.isKaytetaanRyhmaanKuuluvia(), alkuperainen.isKaytetaanRyhmaanKuuluvia());
                Assertions.assertNotSame(hakijaryhma.getKiintio(), alkuperainen.getKiintio());
                Assertions.assertNotSame(hakijaryhma.getKuvaus(), alkuperainen.getKuvaus());
                Assertions.assertNotSame(hakijaryhma.getNimi(), alkuperainen.getNimi());
                Assertions.assertNotSame(hakijaryhma.isTarkkaKiintio(), alkuperainen.isTarkkaKiintio());
                Assertions.assertNotSame(hakijaryhma.getHakijaryhmatyyppikoodiUri(), alkuperainen.getHakijaryhmatyyppikoodiUri());

                MatcherAssert.assertThat(hakijaryhma.isKaytaKaikki(), is(hakijaryhmavalintaperusteista.isKaytaKaikki()));
                MatcherAssert.assertThat(hakijaryhma.isKaytetaanRyhmaanKuuluvia(), is(hakijaryhmavalintaperusteista.isKaytetaanRyhmaanKuuluvia()));
                MatcherAssert.assertThat(hakijaryhma.getKiintio(), is(hakijaryhmavalintaperusteista.getKiintio()));
                MatcherAssert.assertThat(hakijaryhma.getKuvaus(), is(hakijaryhmavalintaperusteista.getKuvaus()));
                MatcherAssert.assertThat(hakijaryhma.getNimi(), is(hakijaryhmavalintaperusteista.getNimi()));
                MatcherAssert.assertThat(hakijaryhma.isTarkkaKiintio(), is(hakijaryhmavalintaperusteista.isTarkkaKiintio()));
                MatcherAssert.assertThat(hakijaryhma.getHakijaryhmatyyppikoodiUri(), is(hakijaryhmavalintaperusteista.getHakijaryhmatyyppikoodi().getUri()));

                hakijaryhma = hakukohde.getHakijaryhma().iterator().next();
                alkuperainen = createHakijaryhmaDTO(valintatapajononHakijaryhmaOid);

                Assertions.assertNotSame(hakijaryhma.isKaytaKaikki(), alkuperainen.isKaytaKaikki());
                Assertions.assertNotSame(hakijaryhma.isKaytetaanRyhmaanKuuluvia(), alkuperainen.isKaytetaanRyhmaanKuuluvia());
                Assertions.assertNotSame(hakijaryhma.getKiintio(), alkuperainen.getKiintio());
                Assertions.assertNotSame(hakijaryhma.getKuvaus(), alkuperainen.getKuvaus());
                Assertions.assertNotSame(hakijaryhma.getNimi(), alkuperainen.getNimi());
                Assertions.assertNotSame(hakijaryhma.isTarkkaKiintio(), alkuperainen.isTarkkaKiintio());
                Assertions.assertNotSame(hakijaryhma.getHakijaryhmatyyppikoodiUri(), alkuperainen.getHakijaryhmatyyppikoodiUri());

                MatcherAssert.assertThat(hakijaryhma.isKaytaKaikki(), is(hakijaryhmavalintaperusteista.isKaytaKaikki()));
                MatcherAssert.assertThat(hakijaryhma.isKaytetaanRyhmaanKuuluvia(), is(hakijaryhmavalintaperusteista.isKaytetaanRyhmaanKuuluvia()));
                MatcherAssert.assertThat(hakijaryhma.getKiintio(), is(hakijaryhmavalintaperusteista.getKiintio()));
                MatcherAssert.assertThat(hakijaryhma.getKuvaus(), is(hakijaryhmavalintaperusteista.getKuvaus()));
                MatcherAssert.assertThat(hakijaryhma.getNimi(), is(hakijaryhmavalintaperusteista.getNimi()));
                MatcherAssert.assertThat(hakijaryhma.isTarkkaKiintio(), is(hakijaryhmavalintaperusteista.isTarkkaKiintio()));
                MatcherAssert.assertThat(hakijaryhma.getHakijaryhmatyyppikoodiUri(), is(hakijaryhmavalintaperusteista.getHakijaryhmatyyppikoodi().getUri()));
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
                    .addHeader("Location", mockWebServer.url("/") + "cas/v1/tickets/TGT-1")
                    .setResponseCode(201));
            mockWebServer.enqueue(new MockResponse()
                    .setBody(VALID_TICKET)
                    .setResponseCode(200));
            mockWebServer.enqueue(new MockResponse()
                    .setHeader("Set-Cookie", "JSESSIONID=XYZ")
                    .setBody(VALID_TICKET)
                    .setResponseCode(200));
            mockWebServer.enqueue(new MockResponse()
                    .setHeader("Content-type", "application/json")
                    .setBody(this.gson.toJson(Collections.singletonList(hakijaryhmavalintaperusteista)))
                    .setResponseCode(200));
            mockWebServer.enqueue(new MockResponse()
                    .setHeader("Content-type", "application/json")
                    .setBody(this.gson.toJson(Collections.singletonList(valintatapajononHakijaryhmavalintaperusteista)))
                    .setResponseCode(200));

            final HashMap<String, List<ValintatapajonoDTO>> vpMap = new HashMap<>();
            vpMap.put(hakukohdeOid, Collections.singletonList(valintaperusteista));
            mockWebServer.enqueue(new MockResponse()
                    .setHeader("Content-type", "application/json")
                    .setBody(this.gson.toJson(vpMap))
                    .setResponseCode(200));

            Exception exception = Assertions.assertThrows(RuntimeException.class, () -> toteutaSijoitteluService.toteutaSijoittelu(hakuOid, 12345L));
            Assertions.assertEquals("java.lang.IllegalStateException: Haun hakuOid sijoittelu : " +
                    "Laskennan tuloksista löytyvien jonojen tietoja on kadonnut valintaperusteista: " +
                    "[Hakukohde hakukohdeOid , jono \"Varsinainen testivalinta\" (valintatapaJonoOid , prio 0)]", exception.getMessage());
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
