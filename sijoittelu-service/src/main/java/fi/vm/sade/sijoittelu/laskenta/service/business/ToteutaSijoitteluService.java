package fi.vm.sade.sijoittelu.laskenta.service.business;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSyntaxException;
import fi.vm.sade.javautils.nio.cas.CasClient;
import fi.vm.sade.service.valintaperusteet.dto.HakijaryhmaValintatapajonoDTO;
import fi.vm.sade.service.valintaperusteet.dto.ValintatapajonoCreateDTO;
import fi.vm.sade.service.valintaperusteet.dto.ValintatapajonoDTO;
import fi.vm.sade.sijoittelu.domain.SijoitteluajonTila;
import fi.vm.sade.sijoittelu.configuration.SijoitteluServiceConfiguration;
import fi.vm.sade.sijoittelu.laskenta.email.EmailService;
import fi.vm.sade.sijoittelu.laskenta.service.it.Haku;
import fi.vm.sade.sijoittelu.laskenta.service.it.TarjontaIntegrationService;
import fi.vm.sade.sijoittelu.laskenta.util.EnumConverter;
import fi.vm.sade.sijoittelu.laskenta.util.UrlProperties;
import fi.vm.sade.valintalaskenta.domain.dto.HakukohdeDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintakoe.Tasasijasaanto;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.ValintatietoValinnanvaiheDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.ValintatietoValintatapajonoDTO;
import fi.vm.sade.valintalaskenta.tulos.service.impl.ValintatietoService;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;

@Service
public class ToteutaSijoitteluService {
    private final static Logger LOGGER = LoggerFactory.getLogger(ToteutaSijoitteluService.class);

    private static final int MAX_POLL_INTERVALL_IN_SECONDS = 30;
    private static final int ADDED_WAIT_PER_POLL_IN_SECONDS = 3;

    private final SijoitteluBusinessService sijoitteluBusinessService;
    private final ValintatietoService valintatietoService;
    private final SijoitteluBookkeeperService sijoitteluBookkeeperService;
    private final CasClient sijoitteluCasClient;
    private final UrlProperties urlProperties;
    private final TarjontaIntegrationService tarjontaIntegrationService;
    private final EmailService emailService;
    private final Gson gson;
    @Autowired
    public ToteutaSijoitteluService(SijoitteluBusinessService sijoitteluBusinessService,
                                    ValintatietoService valintatietoService,
                                    SijoitteluBookkeeperService sijoitteluBookkeeperService,
                                    @Qualifier("SijoitteluCasClient") CasClient sijoitteluCasClient,
                                    UrlProperties urlProperties,
                                    TarjontaIntegrationService tarjontaIntegrationService,
                                    EmailService emailService) {
        this.sijoitteluBusinessService = sijoitteluBusinessService;
        this.valintatietoService = valintatietoService;
        this.sijoitteluCasClient = sijoitteluCasClient;
        this.sijoitteluBookkeeperService = sijoitteluBookkeeperService;
        this.urlProperties = urlProperties;
        this.tarjontaIntegrationService = tarjontaIntegrationService;
        this.emailService = emailService;

        this.gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, (JsonDeserializer<Date>) (json, typeOfT, context) ->
                        new Date(json.getAsJsonPrimitive().getAsLong()))
                .create();
    }

    public void toteutaSijoitteluAsync(
        String hakuOid, Consumer<String> callback, Consumer<Throwable> failureCallback) {
        LOGGER.info("Sijoitellaan pollaten haulle {}", hakuOid);
        int secondsUntilNextPoll = 3;
        String status = "";
        AtomicReference<Boolean> done = new AtomicReference<>(false);
        Long sijoitteluajoId = -1L;

        // Luodaan sijoitteluajo, saadaan palautusarvona sen id, jota käytetään pollattaessa toista
        // rajapintaa.
        String luontiUrl = this.urlProperties.url("sijoittelu-service.sijoittele", hakuOid);
        try {
            sijoitteluajoId = this.toteutaSijoittelu(hakuOid);
        } catch (Exception e) {
            LOGGER.error(String.format("(Haku %s) sijoittelun rajapintakutsu epäonnistui", hakuOid), e);
        }
        // Jos rajapinta palauttaa -1 tai kutsu epäonnistuu, uutta sijoitteluajoa ei luotu. Ei aloiteta
        // pollausta.
        if (sijoitteluajoId == -1) {
            String msg =
                String.format(
                    "Uuden sijoittelun luonti haulle %s epäonnistui: luontirajapinta palautti -1",
                    hakuOid);
            LOGGER.error(msg);
            failureCallback.accept(new Exception(msg));
            return;
        }

        LOGGER.info(
            "(Haku: {}) Sijoittelu on käynnistynyt id:llä {}. Pollataan kunnes se on päättynyt.",
            hakuOid,
            sijoitteluajoId);
        String pollingUrl =
            this.urlProperties.url("sijoittelu-service.sijoittele.ajontila", sijoitteluajoId);
        while (!done.get()) {
            try {
                TimeUnit.SECONDS.sleep(secondsUntilNextPoll);
            } catch (Exception e) {
                throw new RuntimeException();
            }
            if (secondsUntilNextPoll < MAX_POLL_INTERVALL_IN_SECONDS) {
                secondsUntilNextPoll += ADDED_WAIT_PER_POLL_IN_SECONDS;
            }
            try {
                status = this.sijoitteluBookkeeperService.getSijoitteluAjonTila(sijoitteluajoId);

                LOGGER.info("Saatiin ajontila-rajapinnalta palautusarvo {}", status);
                if (SijoitteluajonTila.VALMIS.toString().equals(status)) {
                    LOGGER.info("#### Sijoittelu {} haulle {} on valmistunut", sijoitteluajoId, hakuOid);
                    callback.accept(status);
                    done.set(true);
                    return;
                }
                if (SijoitteluajonTila.VIRHE.toString().equals(status)) {
                    LOGGER.error("#### Sijoittelu {} haulle {} päättyi virheeseen", sijoitteluajoId, hakuOid);
                    failureCallback.accept(new Exception());
                    done.set(true);
                    return;
                }
            } catch (Exception e) {
                LOGGER.error(
                    String.format("Sijoittelussa %s haulle %s tapahtui virhe", sijoitteluajoId, hakuOid),
                    e);
                failureCallback.accept(e);
                return;
            }
        }
    }

    public Long toteutaSijoittelu(String hakuOid) {

        Long sijoitteluAjonTunniste = System.currentTimeMillis();
        if (!sijoitteluBookkeeperService.luoUusiSijoitteluAjo(hakuOid, sijoitteluAjonTunniste)) {
            LOGGER.warn("Uuden sijoittelun luominen haulle {} ei onnistunut, luultavasti siksi että edellinen oli vielä KESKEN", hakuOid);
            return -1L;
        } else {
            LOGGER.info("Luodaan ja käynnistetään uusi sijoittelu haulle {}", hakuOid);
            try {
                Runnable kaynnistaSijoittelu = () -> {
                    try {
                        toteutaSijoittelu(hakuOid, sijoitteluAjonTunniste);
                    } catch (ExecutionException e) {
                        LOGGER.error("Virhe sijoittelun suorituksessa, ", e);
                    }
                };
                Thread sijoittelu = new Thread(kaynnistaSijoittelu);
                sijoittelu.start();
                return sijoitteluAjonTunniste;
            } catch (Exception e) {
                LOGGER.error("Virhe sijoittelun suorituksessa, ", e);
                throw e;
            }
        }
    }

    private static Set<String> flatMapJonoOids(Map<String, Map<String, ValintatapajonoDTO>> mapToMapOfObjects) {
        return unmodifiableSet(mapToMapOfObjects.values().stream()
                .flatMap(map -> map.values().stream().map(ValintatapajonoDTO::getOid))
                .collect(toSet()));
    }

    private Set<String> updateValintatapajonotAndReturnAmountUsed(Map<String, ValintatapajonoDTO> valintatapajonoByOid,
                                                          ValintatietoValinnanvaiheDTO vaihe) {
        List<ValintatietoValintatapajonoDTO> konvertoidut = new ArrayList<>();
        vaihe.getValintatapajonot().forEach(jono -> {
            if (valintatapajonoByOid.containsKey(jono.getOid())
                    && jono.getValmisSijoiteltavaksi()
                    && jono.getAktiivinen()) {
                ValintatapajonoDTO perusteJono = valintatapajonoByOid.get(jono.getOid());
                jono.setAloituspaikat(perusteJono.getAloituspaikat());
                jono.setEiVarasijatayttoa(perusteJono.getEiVarasijatayttoa());
                jono.setPoissaOlevaTaytto(perusteJono.getPoissaOlevaTaytto());
                jono.setTasasijasaanto(EnumConverter.convert(Tasasijasaanto.class, perusteJono.getTasapistesaanto()));
                jono.setTayttojono(perusteJono.getTayttojono());
                jono.setVarasijat(perusteJono.getVarasijat());
                jono.setVarasijaTayttoPaivat(perusteJono.getVarasijaTayttoPaivat());
                jono.setVarasijojaKaytetaanAlkaen(perusteJono.getVarasijojaKaytetaanAlkaen());
                jono.setVarasijojaTaytetaanAsti(perusteJono.getVarasijojaTaytetaanAsti());
                jono.setAktiivinen(perusteJono.getAktiivinen());
                jono.setKaikkiEhdonTayttavatHyvaksytaan(perusteJono.getKaikkiEhdonTayttavatHyvaksytaan());
                jono.setNimi(perusteJono.getNimi());
                jono.setPrioriteetti(perusteJono.getPrioriteetti());
                konvertoidut.add(jono);
            }
        });
        vaihe.setValintatapajonot(konvertoidut);
        return konvertoidut.stream().map(ValintatietoValintatapajonoDTO::getOid).collect(toSet());
    }

    private void updateHakijaRyhmat(Map<String, HakijaryhmaValintatapajonoDTO> hakijaryhmaByOid, HakukohdeDTO hakukohde) {
        ofNullable(hakukohde.getHakijaryhma()).orElse(emptyList()).forEach(hakijaryhma -> {
                    if (hakijaryhma != null && hakijaryhmaByOid.containsKey(hakijaryhma.getHakijaryhmaOid())) {
                        HakijaryhmaValintatapajonoDTO h = hakijaryhmaByOid.get(hakijaryhma.getHakijaryhmaOid());
                        hakijaryhma.setKaytaKaikki(h.isKaytaKaikki());
                        hakijaryhma.setKaytetaanRyhmaanKuuluvia(h.isKaytetaanRyhmaanKuuluvia());
                        hakijaryhma.setKiintio(h.getKiintio());
                        hakijaryhma.setKuvaus(h.getKuvaus());
                        hakijaryhma.setNimi(h.getNimi());
                        hakijaryhma.setTarkkaKiintio(h.isTarkkaKiintio());
                        hakijaryhma.setPrioriteetti(h.getPrioriteetti());
                        hakijaryhma.setHakijaryhmatyyppikoodiUri(h.getHakijaryhmatyyppikoodi() != null ? h.getHakijaryhmatyyppikoodi().getUri() : null);
                    }
                }
        );
    }

    private Map<String, HakijaryhmaValintatapajonoDTO> haeMahdollisestiMuuttuneetHakijaryhmat(HakuDTO haku) throws ExecutionException {
        Set<String> hakukohdeOidsWithHakijaryhma = haku.getHakukohteet().stream()
            .filter(hakukohde -> hakukohde.getHakijaryhma() != null && !hakukohde.getHakijaryhma().isEmpty())
            .map(hakukohde -> hakukohde.getOid()).collect(toSet());
        Set<String> valintatapajonoOidsWithHakijaryhma = haku.getHakukohteet().stream()
            .filter(hakukohde -> hakukohde.getHakijaryhma() != null && !hakukohde.getHakijaryhma().isEmpty())
            .flatMap(hakukohde -> hakukohde.getValinnanvaihe().stream())
            .flatMap(valinnanvaihe -> valinnanvaihe.getValintatapajonot().stream())
            .map(jono -> jono.getOid()).collect(toSet());

        Map<String, HakijaryhmaValintatapajonoDTO> hakijaryhmaByOid = Collections.emptyMap();
        TypeToken<List<HakijaryhmaValintatapajonoDTO>> token = new TypeToken<List<HakijaryhmaValintatapajonoDTO>>() {};
        if (!hakukohdeOidsWithHakijaryhma.isEmpty()) {
            LOGGER.info("Haetaan hakijaryhmät sijoittelua varten");
            LOGGER.info("Haetaan hakukohdekohtaiset hakijaryhmät sijoittelua varten");
            Request hakuRequest = new RequestBuilder()
                .setUrl(urlProperties.url("valintaperusteet.haku.rest.url"))
                .setMethod("POST")
                .setBody(this.gson.toJson(Lists.newArrayList(hakukohdeOidsWithHakijaryhma)))
                .addHeader("Accept", "application/json")
                .addHeader("Content-type", "application/json")
                .addHeader("Caller-Id", SijoitteluServiceConfiguration.CALLER_ID)
                .setRequestTimeout(120000)
                .setReadTimeout(120000)
                .build();
            try {
                Response hakuResponse = sijoitteluCasClient.executeAndRetryWithCleanSessionOnStatusCodesBlocking(hakuRequest, Set.of(302, 401, 403));

                if (hakuResponse.getStatusCode() == 200) {
                    try {
                        List<HakijaryhmaValintatapajonoDTO> hakijaryhmaByOids = gson.fromJson(hakuResponse.getResponseBody(), token.getType());
                        hakijaryhmaByOid = hakijaryhmaByOids
                            .stream()
                            // Valintaperusteet pitaisi palauttaa vain aktiivisia mutta filtteroidaan varmuuden vuoksi
                            .filter(v -> TRUE.equals(v.getAktiivinen()))
                            .collect(Collectors.toMap(v -> v.getOid(), v -> v));
                    } catch (JsonSyntaxException e) {
                        throw new RuntimeException(String.format("Failed to parse response from JSON %s", hakuResponse.getResponseBody()), e);
                    }
                } else {
                    throw new RuntimeException(String.format("Failed to fetch haku %s from %s. Response status: %s", hakukohdeOidsWithHakijaryhma, hakuResponse.getUri().toString(), hakuResponse.getStatusCode()));
                }
            } catch (InterruptedException ie) {
                LOGGER.error("Hakijaryhmien hakeminen epäonnistui virheeseen!", ie);
                throw new RuntimeException(ie);
            }
            try {
                LOGGER.info("Haetaan valintatapajonokohtaiset hakijaryhmät sijoittelua varten");
                Request hakijaryhmaRequest = new RequestBuilder()
                    .setUrl(urlProperties.url("valintaperusteet.hakijaryhmat.rest.url"))
                    .setMethod("POST")
                    .setBody(this.gson.toJson(Lists.newArrayList(valintatapajonoOidsWithHakijaryhma)))
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-type", "application/json")
                    .addHeader("Caller-Id", SijoitteluServiceConfiguration.CALLER_ID)
                    .setRequestTimeout(120000)
                    .setReadTimeout(120000)
                    .build();
                //Response hakijaryhmaResponse = sijoitteluCasClient.executeWithServiceTicketBlocking(hakijaryhmaRequest);
                try {
                    Response hakijaryhmaResponse = sijoitteluCasClient.executeAndRetryWithCleanSessionOnStatusCodesBlocking(hakijaryhmaRequest, Set.of(302, 401, 403));
                    if (hakijaryhmaResponse.getStatusCode() == 200) {
                        try {
                            List<HakijaryhmaValintatapajonoDTO> valintatapajonojenHakijaryhmaByOids = gson.fromJson(hakijaryhmaResponse.getResponseBody(), token.getType());

                            Map<String, HakijaryhmaValintatapajonoDTO> valintatapajonojenHakijaryhmaByOid =
                                valintatapajonojenHakijaryhmaByOids
                                    .stream()
                                    // Valintaperusteet pitaisi palauttaa vain aktiivisia mutta filtteroidaan varmuuden vuoksi
                                    .filter(v -> TRUE.equals(v.getAktiivinen()))
                                    .collect(Collectors.toMap(v -> v.getOid(), v -> v));

                            hakijaryhmaByOid.putAll(valintatapajonojenHakijaryhmaByOid);
                        } catch (JsonSyntaxException e) {
                            throw new RuntimeException(String.format("Failed to parse response from JSON %s", hakijaryhmaResponse.getResponseBody()));
                        }
                    } else {
                        throw new RuntimeException(String.format("Failed to fetch hakijaryhmät %s from %s. Response status: %s", hakukohdeOidsWithHakijaryhma, hakijaryhmaResponse.getUri().toString(), hakijaryhmaResponse.getStatusCode()));
                    }
                } catch (ExecutionException e) {
                    LOGGER.error("Hakijaryhmien hakeminen epäonnistui virheeseen!", e);
                    throw e;
                }
                LOGGER.info("Saatiin haun {} {}:lle hakukohteelle yhteensä {} aktiivista hakijaryhmää",
                    haku.getHakuOid(), haku.getHakukohteet().size(), hakijaryhmaByOid.size());
            } catch (InterruptedException ie) {
                LOGGER.error("Hakijaryhmien hakeminen epäonnistui virheeseen!", ie);
                throw new RuntimeException(ie);
            }
        }
        return hakijaryhmaByOid;
    }

        private Map<String, List<ValintatapajonoDTO>> haeValintatapajonotSijoittelulle (HakuDTO haku) throws ExecutionException {
            final Set<String> aktiivisiaJonojaSisaltavienKohteidenOidit = haku.filtteroiOiditHakukohteilleJoillaOnAktiivisiaJonoja();
            Map<String, List<ValintatapajonoDTO>> valintaperusteidenJonotHakukohteittain;
            if (!aktiivisiaJonojaSisaltavienKohteidenOidit.isEmpty()) {
                LOGGER.info(String.format("Haetaan valintaperusteista valintatapajonoja sijoittelua varten haun " +
                        "%s %s:lle hakukohteelle, jotka löytyvät laskennan tuloksista",
                    haku.getHakuOid(), aktiivisiaJonojaSisaltavienKohteidenOidit.size()));
                TypeToken<Map<String, List<ValintatapajonoDTO>>> token = new TypeToken<Map<String, List<ValintatapajonoDTO>>>() {};
                try {
                    LOGGER.info("Haetaan valintatapajonokohtaiset hakijaryhmät sijoittelua varten");
                    Request valintatapajonoRequest = new RequestBuilder()
                        .setUrl(urlProperties.url("valintaperusteet.valintatapajono.rest.url"))
                        .setMethod("POST")
                        .setBody(this.gson.toJson(new ArrayList<>(aktiivisiaJonojaSisaltavienKohteidenOidit)))
                        .addHeader("Accept", "application/json")
                        .addHeader("Content-type", "application/json")
                        .addHeader("Caller-Id", SijoitteluServiceConfiguration.CALLER_ID)
                        .setRequestTimeout(120000)
                        .setReadTimeout(120000)
                        .build();
                    Response valintatapajonoResponse = sijoitteluCasClient.executeAndRetryWithCleanSessionOnStatusCodesBlocking(valintatapajonoRequest, Set.of(302, 401, 403));

                    if (valintatapajonoResponse.getStatusCode() == 200) {
                        try {
                            valintaperusteidenJonotHakukohteittain = gson.fromJson(valintatapajonoResponse.getResponseBody(), token.getType());
                            assertKaikkiLaskennanTuloksistaLoytyvatSijoitteluunValmiitJonotLoytyvatValintaPerusteista(haku,
                                valintaperusteidenJonotHakukohteittain);
                        } catch (JsonSyntaxException e) {
                            throw new RuntimeException(String.format("Failed to parse response from JSON %s", valintatapajonoResponse.getResponseBody()));
                        }
                    } else {
                        throw new RuntimeException(String.format("Failed to fetch valintatapajonot from %s. Response status: %s", valintatapajonoResponse.getUri().toString(), valintatapajonoResponse.getStatusCode()));
                    }
                } catch (Exception e) {
                    LOGGER.error("Valintatapajonojen hakeminen epäonnistui virheeseen!", e);
                    throw new RuntimeException(e);
                }
                return valintaperusteidenJonotHakukohteittain;
            } else {
                return Collections.emptyMap();
            }
        }

        private void assertKaikkiLaskennanTuloksistaLoytyvatSijoitteluunValmiitJonotLoytyvatValintaPerusteista (HakuDTO
        hakuDTO, Map < String, List < ValintatapajonoDTO >> valintaperusteidenJonotHakukohteittain){
            Set<String> laskennanTulostenJonoOidit = hakuDTO.getHakukohteet().stream().flatMap(hk ->
                    hk.getValinnanvaihe().stream().flatMap(vv ->
                            vv.getValintatapajonot().stream()
                                    .filter(fi.vm.sade.valintalaskenta.domain.dto.ValintatapajonoDTO::getValmisSijoiteltavaksi)
                                    .map(fi.vm.sade.valintalaskenta.domain.dto.ValintatapajonoDTO::getOid))).collect(toSet());
            Set<String> valintaperusteidenJonoOidit = valintaperusteidenJonotHakukohteittain.values().stream().flatMap(js ->
                    js.stream().map(ValintatapajonoDTO::getOid)).collect(toSet());
            Sets.SetView<String> valintaperusteistaKadonneetJonoOidit = Sets.difference(laskennanTulostenJonoOidit, valintaperusteidenJonoOidit);

            if (valintaperusteistaKadonneetJonoOidit.isEmpty()) {
                return;
            }

            List<String> poistuneidenJonojenTiedot = valintaperusteistaKadonneetJonoOidit.stream().map(jonoOid -> {
                Predicate<ValintatietoValintatapajonoDTO> withJonoOid = j -> j.getOid().equals(jonoOid);
                HakukohdeDTO poistuneenJononHakukohde = (hakuDTO.getHakukohteet()).stream()
                        .filter(h -> h.getValinnanvaihe().stream()
                                .anyMatch(vv -> vv.getValintatapajonot().stream()
                                        .anyMatch(withJonoOid))).findFirst().get();
                ValintatietoValintatapajonoDTO poistunutJono = poistuneenJononHakukohde.getValinnanvaihe().stream()
                        .flatMap(vv -> vv.getValintatapajonot().stream()
                                .filter(withJonoOid)).findFirst().get();
                return String.format("Hakukohde %s , jono \"%s\" (%s , prio %d)", poistuneenJononHakukohde.getOid(),
                        poistunutJono.getNimi(), poistunutJono.getOid(), poistunutJono.getPrioriteetti());
            }).collect(Collectors.toList());
            throw new IllegalStateException(String.format("Haun %s sijoittelu : " +
                            "Laskennan tuloksista löytyvien jonojen tietoja on kadonnut valintaperusteista: %s",
                    hakuDTO.getHakuOid(), poistuneidenJonojenTiedot));
        }

        private Map<String, Map<String, ValintatapajonoDTO>> filtteroiAktiivisetJonotMappiinOideittain (
                Map < String, List < ValintatapajonoDTO >> valintatapajonotSijoittelulle){
            return valintatapajonotSijoittelulle
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().stream()
                            .filter(ValintatapajonoCreateDTO::getAktiivinen)
                            .collect(Collectors.toMap(ValintatapajonoDTO::getOid, v0 -> v0))));
        }

        public void toteutaSijoittelu (String hakuOid, Long sijoitteluAjonTunniste) throws ExecutionException {
            try {
                LOGGER.info("Valintatietoja valmistetaan haulle {}!", hakuOid);
                HakuDTO haku = valintatietoService.haeValintatiedot(hakuOid);
                LOGGER.info("Valintatiedot haettu serviceltä {}!", hakuOid);
                LOGGER.info("Asetetaan valintaperusteet {}!", hakuOid);

                final Map<String, HakijaryhmaValintatapajonoDTO> hakijaryhmaByOid = haeMahdollisestiMuuttuneetHakijaryhmat(haku);
                final Map<String, List<ValintatapajonoDTO>> jonotSijoittelulleHakukohdeOideittain = haeValintatapajonotSijoittelulle(haku);
                final Map<String, Map<String, ValintatapajonoDTO>> hakukohdeMapToValintatapajonoByOid = Maps.newHashMap(filtteroiAktiivisetJonotMappiinOideittain(jonotSijoittelulleHakukohdeOideittain));
                final Set<String> laskennanTuloksistaJaValintaperusteistaLoytyvatJonot = jonotSijoittelulleHakukohdeOideittain.values().stream()
                        .flatMap(Collection::stream)
                        .map(ValintatapajonoDTO::getOid)
                        .collect(Collectors.toSet());

                haku.getHakukohteet().forEach(hakukohde -> {
                    updateHakijaRyhmat(hakijaryhmaByOid, hakukohde);
                    Map<String, ValintatapajonoDTO> valintatapajonoByOid =
                            hakukohdeMapToValintatapajonoByOid.getOrDefault(hakukohde.getOid(), new HashMap<>());
                    Set<String> usedOids = new HashSet<>();
                    hakukohde.getValinnanvaihe().forEach(vaihe ->
                        usedOids.addAll(updateValintatapajonotAndReturnAmountUsed(valintatapajonoByOid, vaihe)));
                    if (!usedOids.containsAll(valintatapajonoByOid.keySet())
                            || usedOids.size() != valintatapajonoByOid.size() ) {
                        LOGGER.warn("Kaikkia jonoja ei ole sijoiteltu {} hakukohteessa {}: {}",
                                hakuOid, hakukohde.getOid(), valintatapajonoByOid.keySet());
                        hakukohde.setKaikkiJonotSijoiteltu(false);
                    }
                });
                LOGGER.info("Valintaperusteet asetettu {}!", hakuOid);

                sijoitteluBusinessService.sijoittele(haku,
                        flatMapJonoOids(hakukohdeMapToValintatapajonoByOid),
                        laskennanTuloksistaJaValintaperusteistaLoytyvatJonot,
                        sijoitteluAjonTunniste,
                        hakukohdeMapToValintatapajonoByOid);
                LOGGER.info("Sijoitteluajo {} suoritettu onnistuneesti haulle {}", sijoitteluAjonTunniste, hakuOid);
                sijoitteluBookkeeperService.merkitseSijoitteluAjonTila(hakuOid,
                        sijoitteluAjonTunniste,
                        SijoitteluajonTila.VALMIS);
            } catch (Throwable t) {
                LOGGER.error(String.format("Sijoitteluajo (tunniste %d) epäonnistui haulle %s : %s",
                        sijoitteluAjonTunniste, hakuOid, t.getMessage()), t);
                try {
                    Haku haku = tarjontaIntegrationService.getHaku(hakuOid, false);
                    if(haku != null && haku.isYhteishaku()) {
                        if(haku.isKk()) {
                            LOGGER.error("Korkeakoulujen yhteishaun sijoitteluajo epäonnistui: {}", hakuOid);
                            emailService.sendKkErrorEmail(haku, t);
                        } else {
                            LOGGER.error("2. asteen yhteishaun sijoitteluajo epäonnistui: {}", hakuOid);
                            emailService.sendToinenAsteErrorEmail(haku, t);
                        }
                    }
                } catch (Throwable t1) {
                    LOGGER.error("Sijoitteluajon virhesähköpostin lähettäminen epäonnistui", t1);
                }
                sijoitteluBookkeeperService.merkitseSijoitteluAjonTila(hakuOid,
                        sijoitteluAjonTunniste,
                        SijoitteluajonTila.VIRHE);
                throw t;
            }
        }
    }
