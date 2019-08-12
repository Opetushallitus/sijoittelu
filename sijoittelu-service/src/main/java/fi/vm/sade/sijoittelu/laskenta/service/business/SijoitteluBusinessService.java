package fi.vm.sade.sijoittelu.laskenta.service.business;

import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.intersection;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.HYLATTY;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.HYVAKSYTTY;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.VARALLA;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.VARASIJALTA_HYVAKSYTTY;
import static fi.vm.sade.sijoittelu.domain.Tasasijasaanto.ARVONTA;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;
import com.google.common.collect.Sets.SetView;

import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithm;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluConfiguration;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluajoWrapperFactory;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.hakukohteet.LisapaikkaTapa;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor.PostSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilaTaulukot;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.HakukohdeItem;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.Tasasijasaanto;
import fi.vm.sade.sijoittelu.domain.ValiSijoittelu;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.domain.comparator.HakemusComparator;
import fi.vm.sade.sijoittelu.domain.dto.VastaanottoDTO;
import fi.vm.sade.sijoittelu.laskenta.external.resource.VirkailijaValintaTulosServiceResource;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluajoResourcesLoader.SijoittelunParametrit;
import fi.vm.sade.sijoittelu.laskenta.service.it.TarjontaIntegrationService;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.service.impl.converters.SijoitteluTulosConverter;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintatulosservice.valintarekisteri.domain.NotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import javax.ws.rs.WebApplicationException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class SijoitteluBusinessService {
    private static final Logger LOG = LoggerFactory.getLogger(SijoitteluBusinessService.class);

    private HakemusComparator hakemusComparator = new HakemusComparator();

    private final SijoitteluTulosConverter sijoitteluTulosConverter;
    private final VirkailijaValintaTulosServiceResource valintaTulosServiceResource;
    private final ValintatulosWithVastaanotto valintatulosWithVastaanotto;
    protected final SijoitteluajoResourcesLoader sijoitteluajoResourcesLoader;
    private final Collection<PostSijoitteluProcessor> postSijoitteluProcessors;
    private final Collection<PreSijoitteluProcessor> preSijoitteluProcessors;
    private final ValintarekisteriService valintarekisteriService;
    private final SijoitteluConfiguration sijoitteluConfiguration;

    @Autowired
    public SijoitteluBusinessService(SijoitteluTulosConverter sijoitteluTulosConverter,
                                     VirkailijaValintaTulosServiceResource valintaTulosServiceResource,
                                     ValintarekisteriService valintarekisteriService,
                                     SijoitteluConfiguration sijoitteluConfiguration,
                                     SijoitteluajoResourcesLoader sijoitteluajoResourcesLoader) {
        this.sijoitteluTulosConverter = sijoitteluTulosConverter;
        this.valintaTulosServiceResource = valintaTulosServiceResource;
        this.valintatulosWithVastaanotto = new ValintatulosWithVastaanotto(valintaTulosServiceResource);
        this.sijoitteluajoResourcesLoader = sijoitteluajoResourcesLoader;
        this.preSijoitteluProcessors = PreSijoitteluProcessor.defaultPreProcessors();
        this.postSijoitteluProcessors = PostSijoitteluProcessor.defaultPostProcessors();
        this.valintarekisteriService = valintarekisteriService;
        this.sijoitteluConfiguration = sijoitteluConfiguration;
    }

    public void sijoittele(HakuDTO haku,
                           Set<String> eiSijoitteluunMenevatJonot,
                           Set<String> laskennanTuloksistaJaValintaperusteistaLoytyvatJonot,
                           Long sijoittelunTunniste) {
        long startTime = sijoittelunTunniste;
        String hakuOid = haku.getHakuOid();
        StopWatch stopWatch = new StopWatch("Haun " + hakuOid + " sijoittelu");
        LOG.info(String.format("Sijoittelu haulle %s alkaa. Luetaan parametrit tarjonnasta ja esivalidoidaan ne", hakuOid));
        SijoittelunParametrit sijoittelunParametrit = sijoitteluajoResourcesLoader.findParametersFromTarjontaAndPerformInitialValidation(hakuOid, stopWatch);

        LOG.info("Luetaan sijoittelu valintarekisteristä!", hakuOid);
        stopWatch.start("Luetaan sijoittelu valintarekisteristä");
        SijoitteluAjo viimeisinSijoitteluajo = readSijoitteluajoFromValintarekisteri(haku.getHakuOid());
        stopWatch.stop();

        stopWatch.start("Päätellään hakukohde- ja valintatapajonotiedot");
        List<Hakukohde> uudenSijoitteluajonHakukohteet = haku.getHakukohteet().stream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        List<Hakukohde> edellisenSijoitteluajonTulokset = Collections.emptyList();
        if (viimeisinSijoitteluajo != null) {
            edellisenSijoitteluajonTulokset =
                valintarekisteriService.getSijoitteluajonHakukohteet(viimeisinSijoitteluajo.getSijoitteluajoId());
            validateSijoittelunJonot(uudenSijoitteluajonHakukohteet,
                edellisenSijoitteluajonTulokset,
                eiSijoitteluunMenevatJonot,
                laskennanTuloksistaJaValintaperusteistaLoytyvatJonot,
                stopWatch);
        }
        stopWatch.stop();

        SijoitteluAjo uusiSijoitteluajo = createSijoitteluAjo(hakuOid);
        uusiSijoitteluajo.setSijoitteluajoId(sijoittelunTunniste); //Korvataan sijoitteluajon tunniste (=luontiaika) parametrina saadulla tunnisteella
        stopWatch.start("Mergataan hakukohteet");
        Pair<List<Hakukohde>, Set<Pair<String, String>>> mergeResult = merge(uusiSijoitteluajo, edellisenSijoitteluajonTulokset, uudenSijoitteluajonHakukohteet);
        List<Hakukohde> kaikkiHakukohteet = mergeResult.getLeft();
        stopWatch.stop();

        poistaTarpeettomatValinnantulokset(stopWatch, mergeResult);

        stopWatch.start("Haetaan valintatulokset vastaanottoineen");
        List<Valintatulos> valintatulokset = valintarekisteriService.getValintatulokset(hakuOid);
        stopWatch.stop();

        stopWatch.start("Haetaan kauden aiemmat vastaanotot");
        Map<String, VastaanottoDTO> kaudenAiemmatVastaanotot = aiemmanVastaanotonHakukohdePerHakija(hakuOid);
        stopWatch.stop();
        LOG.info("Haun {} sijoittelun koko: {} olemassaolevaa, {} uutta, {} valintatulosta",
            hakuOid, edellisenSijoitteluajonTulokset.size(), uudenSijoitteluajonHakukohteet.size(), valintatulokset.size());

        stopWatch.start("Luodaan sijoitteluajoWrapper ja asetetaan parametrit");
        final SijoitteluajoWrapper sijoitteluajoWrapper = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(
            sijoitteluConfiguration, uusiSijoitteluajo, kaikkiHakukohteet, valintatulokset, kaudenAiemmatVastaanotot);
        sijoitteluajoResourcesLoader.asetaSijoittelunParametrit(hakuOid, sijoitteluajoWrapper, sijoittelunParametrit);
        sijoitteluajoWrapper.setEdellisenSijoittelunHakukohteet(edellisenSijoitteluajonTulokset);
        stopWatch.stop();

        if(!sijoitteluajoWrapper.getLisapaikkaTapa().equals(LisapaikkaTapa.EI_KAYTOSSA)) {
            LOG.warn("HRS HUOM: Sijoitteluajossa {} käytetään lisäpaikkoja hakijaryhmäylitäyttötilanteissa (OK-223). Lisäpaikkojen laskentatapa: {}", sijoitteluajoWrapper.getSijoitteluajo(), sijoitteluajoWrapper.getLisapaikkaTapa());
        }
        suoritaSijoittelu(startTime, stopWatch, hakuOid, uusiSijoitteluajo, sijoitteluajoWrapper);
        stopWatch.start("Kopioidaan edellisen sijoitteluajon tietoja");
        kopioiHakukohteenTiedotVanhaltaSijoitteluajolta(edellisenSijoitteluajonTulokset, kaikkiHakukohteet);
        stopWatch.stop();

        LOG.info(String.format("Haun %s sijoitteluajossa muuttui %d valintatulosta.", hakuOid, sijoitteluajoWrapper.getMuuttuneetValintatulokset().size()));
        LOG.info(String.format("Haun %s sijoitteluajossa muuttuneet valintatulokset: %s", hakuOid, sijoitteluajoWrapper.getMuuttuneetValintatulokset()));

        stopWatch.start("Tallennetaan vastaanotot");
        valintatulosWithVastaanotto.persistVastaanotot(sijoitteluajoWrapper.getMuuttuneetValintatulokset());
        stopWatch.stop();

        LOG.info("Tallennetaan sijoitteluajo Valintarekisteriin");
        stopWatch.start("Tallennetaan sijoitteluajo, hakukohteet ja valintatulokset Valintarekisteriin");
        tallennaSijoitteluToValintarekisteri(hakuOid,
            uusiSijoitteluajo,
            kaikkiHakukohteet,
            sijoitteluajoWrapper.getMuuttuneetValintatulokset(),
            stopWatch);
        stopWatch.stop();
        LOG.info(stopWatch.prettyPrint());
    }

    private void poistaTarpeettomatValinnantulokset(StopWatch stopWatch, Pair<List<Hakukohde>, Set<Pair<String, String>>> tarpeettomatValinnantulokset) {
        stopWatch.start("Poistetaan tarpeettomat valinnantulokset");
        tarpeettomatValinnantulokset.getRight().forEach(p -> {
            String hakemusOid = p.getLeft();
            String hakukohdeOid = p.getRight();
            LOG.warn(String.format("Poistetaan hakemuksen %s valinnantulokset hakukohteeseen %s, sillä hakemus ei ole enää mukana hakukohteen valintatapajonoissa", hakemusOid, hakukohdeOid));
            valintarekisteriService.cleanRedundantSijoitteluTuloksesForHakemusInHakukohde(hakemusOid, hakukohdeOid);
        });
        stopWatch.stop();
    }

    public void sijoitteleIlmanPriorisointia(HakuDTO haku,
                           Set<String> eiSijoitteluunMenevatJonot,
                           Set<String> laskennanTuloksistaJaValintaperusteistaLoytyvatJonot,
                           Long sijoittelunTunniste) {
        long startTime = sijoittelunTunniste;
        String hakuOid = haku.getHakuOid();
        StopWatch stopWatch = new StopWatch("Haun " + hakuOid + " sijoittelu ilman hakutoiveiden priorisointia");
        LOG.info(String.format("Sijoittelu ilman priorisointia haulle %s alkaa. Luetaan parametrit tarjonnasta ja esivalidoidaan ne", hakuOid));
        SijoittelunParametrit sijoittelunParametrit = sijoitteluajoResourcesLoader.findParametersFromTarjontaAndPerformInitialValidation(hakuOid, stopWatch);

        LOG.info("Luetaan sijoittelu valintarekisteristä!", hakuOid);
        stopWatch.start("Luetaan sijoittelu valintarekisteristä");
        SijoitteluAjo viimeisinSijoitteluajo = readSijoitteluajoFromValintarekisteri(haku.getHakuOid());
        stopWatch.stop();

        stopWatch.start("Päätellään hakukohde- ja valintatapajonotiedot");
        List<Hakukohde> uudenSijoitteluajonHakukohteet = haku.getHakukohteet().stream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        List<Hakukohde> edellisenSijoitteluajonTulokset = Collections.emptyList();
        if (viimeisinSijoitteluajo != null) {
            edellisenSijoitteluajonTulokset =
                valintarekisteriService.getSijoitteluajonHakukohteet(viimeisinSijoitteluajo.getSijoitteluajoId());
            validateSijoittelunJonot(uudenSijoitteluajonHakukohteet,
                edellisenSijoitteluajonTulokset,
                eiSijoitteluunMenevatJonot,
                laskennanTuloksistaJaValintaperusteistaLoytyvatJonot,
                stopWatch);
        }
        stopWatch.stop();

        SijoitteluAjo uusiSijoitteluajo = createSijoitteluAjo(hakuOid);
        uusiSijoitteluajo.setSijoitteluajoId(sijoittelunTunniste); //Korvataan sijoitteluajon tunniste (=luontiaika) parametrina saadulla tunnisteella
        stopWatch.start("Mergataan hakukohteet");
        Pair<List<Hakukohde>, Set<Pair<String, String>>> mergeResult = merge(uusiSijoitteluajo, edellisenSijoitteluajonTulokset, uudenSijoitteluajonHakukohteet);
        List<Hakukohde> kaikkiHakukohteet = mergeResult.getLeft();
        stopWatch.stop();

        poistaTarpeettomatValinnantulokset(stopWatch, mergeResult);

        stopWatch.start("Haetaan valintatulokset vastaanottoineen");
        List<Valintatulos> valintatulokset = valintarekisteriService.getValintatulokset(hakuOid);
        stopWatch.stop();

        stopWatch.start("Haetaan kauden aiemmat vastaanotot");
        Map<String, VastaanottoDTO> kaudenAiemmatVastaanotot = aiemmanVastaanotonHakukohdePerHakija(hakuOid);
        stopWatch.stop();
        LOG.info("Haun {} sijoittelun koko: {} olemassaolevaa, {} uutta, {} valintatulosta",
            hakuOid, edellisenSijoitteluajonTulokset.size(), uudenSijoitteluajonHakukohteet.size(), valintatulokset.size());

        stopWatch.start("Luodaan sijoitteluajoWrapper ja asetetaan parametrit");
        final SijoitteluajoWrapper kokoSijoitteluajoWrapper = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(
            sijoitteluConfiguration, uusiSijoitteluajo, kaikkiHakukohteet, valintatulokset, kaudenAiemmatVastaanotot);
        sijoitteluajoResourcesLoader.asetaSijoittelunParametrit(hakuOid, kokoSijoitteluajoWrapper, sijoittelunParametrit);
        kokoSijoitteluajoWrapper.setEdellisenSijoittelunHakukohteet(edellisenSijoitteluajonTulokset);
        stopWatch.stop();

        if(!kokoSijoitteluajoWrapper.getLisapaikkaTapa().equals(LisapaikkaTapa.EI_KAYTOSSA)) {
            LOG.warn("HRS HUOM: Sijoitteluajossa {} käytetään lisäpaikkoja hakijaryhmäylitäyttötilanteissa (OK-223). Lisäpaikkojen laskentatapa: {}", kokoSijoitteluajoWrapper.getSijoitteluajo(), kokoSijoitteluajoWrapper.getLisapaikkaTapa());
        }

        List<Valintatulos> kaikkiMuuttuneetValintatulokset = new LinkedList<>();

        for (Hakukohde hakukohde : kaikkiHakukohteet) {
            stopWatch.start(String.format("Sijoitellaan hakukohde %s ilman priorisointia", hakukohde.getOid()));
            final SijoitteluajoWrapper yhdenHakukohteenSijoitteluajoWrapper = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(
                sijoitteluConfiguration, uusiSijoitteluajo, Collections.singletonList(hakukohde), valintatulokset, kaudenAiemmatVastaanotot);
            sijoitteluajoResourcesLoader.asetaSijoittelunParametrit(hakuOid, yhdenHakukohteenSijoitteluajoWrapper, sijoittelunParametrit);
            yhdenHakukohteenSijoitteluajoWrapper.setEdellisenSijoittelunHakukohteet(edellisenSijoitteluajonTulokset.stream().filter(h -> h.getOid().equals(hakukohde.getOid())).collect(Collectors.toList()));
            StopWatch hakukohteenStopWatch = new StopWatch(String.format("Haun %s hakukohteen %s sijoittelu ilman hakutoiveiden priorisointia", hakuOid, hakukohde.getOid()));;
            suoritaSijoittelu(startTime, hakukohteenStopWatch, hakuOid, uusiSijoitteluajo, yhdenHakukohteenSijoitteluajoWrapper);
            kaikkiMuuttuneetValintatulokset.addAll(yhdenHakukohteenSijoitteluajoWrapper.getMuuttuneetValintatulokset());
            LOG.info(hakukohteenStopWatch.prettyPrint());
            stopWatch.stop();
        }

        stopWatch.start("Kopioidaan edellisen sijoitteluajon tietoja");
        kopioiHakukohteenTiedotVanhaltaSijoitteluajolta(edellisenSijoitteluajonTulokset, kaikkiHakukohteet);
        stopWatch.stop();

        LOG.info(String.format("Haun %s sijoitteluajossa muuttui %d valintatulosta.", hakuOid, kaikkiMuuttuneetValintatulokset.size()));
        LOG.info(String.format("Haun %s sijoitteluajossa muuttuneet valintatulokset: %s", hakuOid, kaikkiMuuttuneetValintatulokset));

        stopWatch.start("Tallennetaan vastaanotot");
        valintatulosWithVastaanotto.persistVastaanotot(kaikkiMuuttuneetValintatulokset);
        stopWatch.stop();

        LOG.info("Tallennetaan sijoitteluajo Valintarekisteriin");
        stopWatch.start("Tallennetaan sijoitteluajo, hakukohteet ja valintatulokset Valintarekisteriin");
        tallennaSijoitteluToValintarekisteri(hakuOid,
            uusiSijoitteluajo,
            kaikkiHakukohteet,
            kaikkiMuuttuneetValintatulokset,
            stopWatch);
        stopWatch.stop();
        LOG.info(stopWatch.prettyPrint());
    }

    private SijoitteluAjo readSijoitteluajoFromValintarekisteri(String hakuOid) {
        try {
            LOG.info("Luetaan sijoittelu valintarekisteristä");
            return valintarekisteriService.getLatestSijoitteluajo(hakuOid);
        } catch (NotFoundException iae) {
            LOG.info("Viimeisintä sijoitteluajoa haulle {} ei löydy valintarekisteristä.", hakuOid);
            LOG.warn(iae.getMessage());
            return null;
        }
    }

    private void validateSijoittelunJonot(List<Hakukohde> uudenSijoitteluajonHakukohteet,
                                          List<Hakukohde> edellisenSijoitteluajonTulokset,
                                          Set<String> eiSijoitteluunMenevatJonot,
                                          Set<String> laskennanTuloksistaJaValintaperusteistaLoytyvatJonot,
                                          StopWatch stopWatch) {
        Consumer<String> handleError = (msg) -> {
            LOG.error(msg);
            stopWatch.stop();
            LOG.info(stopWatch.prettyPrint());
            throw new RuntimeException(msg);
        };

        LOG.info(String.format("Valintalaskennasta on löytynyt laskennan tuloksia %d hakukohteelle, " +
                "edellisestä sijoitteluajosta löytyy sijoittelun tuloksia %d hakukohteelle.",
            uudenSijoitteluajonHakukohteet.size(), edellisenSijoitteluajonTulokset.size()));

        Set<String> joSijoitellutJonot = hakukohteidenJonoOidit(edellisenSijoitteluajonTulokset);
        Set<String> uudenSijoitteluajonJonoOidit = hakukohteidenJonoOidit(uudenSijoitteluajonHakukohteet);
        SetView<String> sijoittelustaPoistetutJonot = difference(joSijoitellutJonot, uudenSijoitteluajonJonoOidit);
        SetView<String> aktiivisetSijoittelustaPoistetutJonot = intersection(eiSijoitteluunMenevatJonot, sijoittelustaPoistetutJonot);
        if (aktiivisetSijoittelustaPoistetutJonot.size() > 0) {
            handleError.accept("Edellisessä sijoittelussa olleet jonot puuttuvat sijoittelusta, vaikka ne ovat " +
                "valintaperusteissa yhä aktiivisina: " +
                listaaPoistuneidenJonojenTiedot(edellisenSijoitteluajonTulokset, aktiivisetSijoittelustaPoistetutJonot));
        }

        SetView<String> valintaperusteistaTaiLaskennanTuloksistaPuuttuvatJonot = difference(joSijoitellutJonot,
            laskennanTuloksistaJaValintaperusteistaLoytyvatJonot);
        if (valintaperusteistaTaiLaskennanTuloksistaPuuttuvatJonot.size() > 0) {
            SetView<String> sijoittelustaPoistuneidenJonojenOidit = difference(joSijoitellutJonot,
                difference(uudenSijoitteluajonJonoOidit, eiSijoitteluunMenevatJonot));
            if (!sijoittelustaPoistuneidenJonojenOidit.isEmpty()) {
                handleError.accept("Edellisessä sijoittelussa olleet jonot puuttuvat laskennan tuloksista: " +
                    listaaPoistuneidenJonojenTiedot(edellisenSijoitteluajonTulokset, sijoittelustaPoistuneidenJonojenOidit));
            } else {
                handleError.accept("Edellisessä sijoittelussa olleet jonot ovat kadonneet valintaperusteista, minkä olisi " +
                    "pitänyt ilmetä jo ladatessa tietoja SijoitteluResourcessa. Toisaalta tämän validoinnin ei pitäisi " +
                    "voida triggeröityä, koska jonojen puuttumisen valintaperusteista pitäisi aiheuttaa se, etteivät " +
                    "laskennan tuloksetkaan Tule tänne asti. Vaikuttaa siis bugilta: " +
                    listaaPoistuneidenJonojenTiedot(edellisenSijoitteluajonTulokset, valintaperusteistaTaiLaskennanTuloksistaPuuttuvatJonot));
            }
        }
    }

    private List<String> listaaPoistuneidenJonojenTiedot(List<Hakukohde> edellisenSijoitteluajonTulokset, Collection<String> poistuneidenJonojenOidit) {
        return poistuneidenJonojenOidit.stream().sorted().map(jonoOid -> {
            Predicate<Valintatapajono> withJonoOid = j -> j.getOid().equals(jonoOid);
            Hakukohde poistuneenJononHakukohde = edellisenSijoitteluajonTulokset.stream()
                .filter(h -> h.getValintatapajonot().stream().anyMatch(withJonoOid)).findFirst().get();
            Valintatapajono poistunutJono = poistuneenJononHakukohde.getValintatapajonot().stream().filter(withJonoOid).findFirst().get();
            return String.format("Hakukohde %s , jono \"%s\" (%s , prio %d)", poistuneenJononHakukohde.getOid(),
                poistunutJono.getNimi(), poistunutJono.getOid(), poistunutJono.getPrioriteetti());
        }).collect(Collectors.toList());
    }

    private Set<String> hakukohteidenJonoOidit(List<Hakukohde> hakukohteet) {
        return unmodifiableSet(hakukohteet.stream()
                .flatMap(hakukohde -> hakukohde.getValintatapajonot().stream().map(Valintatapajono::getOid))
                .collect(toSet()));
    }

    private void suoritaSijoittelu(long startTime,
                                   StopWatch stopWatch,
                                   String hakuOid,
                                   SijoitteluAjo uusiSijoitteluajo,
                                   SijoitteluajoWrapper sijoitteluajoWrapper) {
        LOG.info("Suoritetaan sijoittelu haulle {}", hakuOid);
        stopWatch.start("Suoritetaan sijoittelu");
        uusiSijoitteluajo.setStartMils(startTime);
        SijoitteluAlgorithm.sijoittele(preSijoitteluProcessors, postSijoitteluProcessors, sijoitteluajoWrapper);
        uusiSijoitteluajo.setEndMils(System.currentTimeMillis());
        stopWatch.stop();
    }

    private void tallennaSijoitteluToValintarekisteri(String hakuOid,
                                                      SijoitteluAjo uusiSijoitteluajo,
                                                      List<Hakukohde> hakukohteet,
                                                      List<Valintatulos> valintatulokset,
                                                      StopWatch stopWatch) {
        LOG.info("Tallennetaan haun {} sijoittelu valintarekisteriin", hakuOid);
        try {
            valintarekisteriService.tallennaSijoittelu(uusiSijoitteluajo, hakukohteet, valintatulokset);
        } catch (Exception e) {
            LOG.error(String.format(
                    "Sijoittelujon %s tallennus valintarekisteriin epäonnistui haulle %s.",
                    uusiSijoitteluajo.getSijoitteluajoId(), hakuOid
            ), e);
            stopWatch.stop();
            LOG.info(stopWatch.prettyPrint());
            throw e;
        }
        //TODO: vanhojen sijoitteluajojen siivous
    }




    public List<HakukohdeDTO> valisijoittele(HakuDTO sijoitteluTyyppi) {
        long startTime = System.currentTimeMillis();
        String hakuOid = sijoitteluTyyppi.getHakuOid();
        StopWatch stopWatch = new StopWatch("Haun " + hakuOid + " välisijoittelu");

        stopWatch.start("Alustetaan uusi välisijoittelu ja haetaan hakukohteet");
        ValiSijoittelu sijoittelu = createValiSijoittelu(hakuOid);
        List<Hakukohde> uudetHakukohteet = sijoitteluTyyppi.getHakukohteet().parallelStream()
            .map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        List<Hakukohde> olemassaolevatHakukohteet = Collections.emptyList();
        SijoitteluAjo uusiSijoitteluajo = createValiSijoitteluAjo(sijoittelu);
        List<Hakukohde> kaikkiHakukohteet = merge(uusiSijoitteluajo, olemassaolevatHakukohteet, uudetHakukohteet).getLeft();
        SijoitteluajoWrapper sijoitteluajoWrapper =
            SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(sijoitteluConfiguration, uusiSijoitteluajo,
                kaikkiHakukohteet, Collections.emptyList(), Collections.emptyMap());
        stopWatch.stop();

        suoritaSijoittelu(startTime, stopWatch, hakuOid, uusiSijoitteluajo, sijoitteluajoWrapper);

        List<HakukohdeDTO> result = kaikkiHakukohteet.parallelStream()
            .map(sijoitteluTulosConverter::convert)
            .collect(Collectors.toList());
        LOG.info(stopWatch.prettyPrint());
        return result;
    }

    private Hakukohde getErillissijoittelunHakukohde(HakuDTO haku) {
        if( 1 == haku.getHakukohteet().size() ) {
            return DomainConverter.convertToHakukohde(haku.getHakukohteet().get(0));
        }
        LOG.error("Haun {} erillissijoitteluun saatiin {} kpl hakukohteita. Voi olla vain yksi!",
            haku.getHakuOid(), haku.getHakukohteet().size());
        throw new IllegalStateException(String.format("Haun %s erillissijoitteluun saatiin %d kpl hakukohteita. " +
            "Voi olla vain yksi!", haku.getHakuOid(), haku.getHakukohteet().size()));
    }

    public long erillissijoittele(HakuDTO haku) {
        String hakuOid = haku.getHakuOid();
        String hakukohdeOid = haku.getHakukohteet().size() > 0 ? haku.getHakukohteet().get(0).getOid() : "";
        long startTime = System.currentTimeMillis();

        StopWatch stopWatch = new StopWatch(String.format("Haun %s hakukohteen %s erillissijoittelu",
            hakuOid, hakukohdeOid));

        String ajonKuvaus = String.format("Erillissijoittelu haun %s hakukohteelle %s", hakuOid, hakukohdeOid);
        LOG.info(ajonKuvaus + " alkaa. Luetaan parametrit tarjonnasta ja esivalidoidaan ne");
        SijoittelunParametrit sijoittelunParametrit = sijoitteluajoResourcesLoader.findParametersFromTarjontaAndPerformInitialValidation(hakuOid, stopWatch);

        LOG.info(String.format("Luetaan sijoittelu valintarekisteristä haun %s hakukohteelle %s .", hakuOid, hakukohdeOid));

        stopWatch.start("Luetaan viimeisin erillissijoitteluajo valintarekisteristä");
        SijoitteluAjo viimeisinSijoitteluajo = readSijoitteluajoFromValintarekisteri(haku.getHakuOid());
        stopWatch.stop();

        stopWatch.start("Haetaan erillissijoittelun hakukohde");
        Hakukohde hakukohdeValintalaskennassa = getErillissijoittelunHakukohde(haku);
        String sijoiteltavanHakukohteenOid = hakukohdeValintalaskennassa.getOid();

        List<Hakukohde> viimeisimmanSijoitteluajonHakukohteet = Collections.emptyList();
        List<Hakukohde> hakukohdeViimeisimmassaSijoitteluajossa = Collections.emptyList();

        if (null != viimeisinSijoitteluajo) {
            viimeisimmanSijoitteluajonHakukohteet = valintarekisteriService.getSijoitteluajonHakukohteet(viimeisinSijoitteluajo.getSijoitteluajoId());
            hakukohdeViimeisimmassaSijoitteluajossa = viimeisimmanSijoitteluajonHakukohteet.stream()
                .filter(hk -> hk.getOid().equals(sijoiteltavanHakukohteenOid))
                .findFirst()
                .map(Arrays::asList)
                .orElse(Collections.emptyList());
        }

        SijoitteluAjo uusiSijoitteluajo = createSijoitteluAjo(hakuOid);
        Pair<List<Hakukohde>, Set<Pair<String, String>>> mergeResult = merge(uusiSijoitteluajo,
                hakukohdeViimeisimmassaSijoitteluajossa,
                Collections.singletonList(hakukohdeValintalaskennassa));
        List<Hakukohde> hakukohdeTassaSijoittelussa = mergeResult.getLeft();
        stopWatch.stop();

        poistaTarpeettomatValinnantulokset(stopWatch, mergeResult);

        stopWatch.start("Haetaan valintatulokset vastaanottoineen");
        List<Valintatulos> valintatulokset = valintarekisteriService.getValintatulokset(hakuOid);
        stopWatch.stop();
        stopWatch.start("Haetaan kauden aiemmat vastaanotot");
        Map<String, VastaanottoDTO> kaudenAiemmatVastaanotot = aiemmanVastaanotonHakukohdePerHakija(hakuOid);
        stopWatch.stop();

        stopWatch.start("Luodaan sijoitteluajoWrapper");
        final SijoitteluajoWrapper sijoitteluajoWrapper =
            SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(
                sijoitteluConfiguration, uusiSijoitteluajo, hakukohdeTassaSijoittelussa, valintatulokset, kaudenAiemmatVastaanotot);
        sijoitteluajoResourcesLoader.asetaSijoittelunParametrit(hakuOid, sijoitteluajoWrapper, sijoittelunParametrit);
        stopWatch.stop();

        suoritaSijoittelu(startTime, stopWatch, hakuOid, uusiSijoitteluajo, sijoitteluajoWrapper);

        stopWatch.start("Kopioidaan edellisen sijoitteluajon tietoja");
        kopioiHakukohteenTiedotVanhaltaSijoitteluajolta(hakukohdeViimeisimmassaSijoitteluajossa, hakukohdeTassaSijoittelussa);
        List<Hakukohde> kaikkiTallennettavatHakukohteet = new ArrayList<>();
        kaikkiTallennettavatHakukohteet.addAll(hakukohdeTassaSijoittelussa);
        kaikkiTallennettavatHakukohteet.addAll(viimeisimmanSijoitteluajonHakukohteet.stream()
            .filter(hk -> !hk.getOid().equals(sijoiteltavanHakukohteenOid))
            .map(hakukohde -> {
                hakukohde.setSijoitteluajoId(uusiSijoitteluajo.getSijoitteluajoId());
                HakukohdeItem item = new HakukohdeItem();
                item.setOid(hakukohde.getOid());
                uusiSijoitteluajo.getHakukohteet().add(item);
                return hakukohde;
            }).collect(Collectors.toList()));
        stopWatch.stop();

        stopWatch.start("Tallennetaan sijoitteluajo, hakukohteet ja valintatulokset Valintarekisteriin");
        tallennaSijoitteluToValintarekisteri(hakuOid,
            uusiSijoitteluajo,
            kaikkiTallennettavatHakukohteet,
            sijoitteluajoWrapper.getMuuttuneetValintatulokset(),
            stopWatch);
        stopWatch.stop();
        LOG.info(stopWatch.prettyPrint());
        return uusiSijoitteluajo.getSijoitteluajoId();
    }

    private void kopioiHakukohteenTiedotVanhaltaSijoitteluajolta(final List<Hakukohde> edellisenSijoitteluajonHakukohteet,
                                                                 final List<Hakukohde> tamanSijoitteluajonHakukohteet) {
        Map<String, Hakemus> hakemukset = getStringHakemusMap(edellisenSijoitteluajonHakukohteet);
        Map<String, Valintatapajono> valintatapajonot = getStringValintatapajonoMap(edellisenSijoitteluajonHakukohteet);
        tamanSijoitteluajonHakukohteet.parallelStream().forEach(hakukohde ->
            hakukohde.getValintatapajonot().forEach(valintatapajono -> {
                kopioiValintatapajononTiedotVanhaltaSijoitteluajolta(valintatapajono, valintatapajonot.get(valintatapajono.getOid()));
                kopioiHakemustenTiedotVanhaltaSijoitteluajoltaJaAsetaVarasijaNumerot(hakukohde.getOid(), valintatapajono, hakemukset);
            })
        );
    }

    private void kopioiValintatapajononTiedotVanhaltaSijoitteluajolta(Valintatapajono valintatapajono,
                                                                      Valintatapajono edellisenSijoitteluajonValintatapajono) {
        valintatapajono.setAlinHyvaksyttyPistemaara(alinHyvaksyttyPistemaara(valintatapajono.getHakemukset()).orElse(null));
        valintatapajono.setHyvaksytty(getMaara(valintatapajono.getHakemukset(), asList(HYVAKSYTTY, VARASIJALTA_HYVAKSYTTY)));
        valintatapajono.setVaralla(getMaara(valintatapajono.getHakemukset(), Collections.singletonList(VARALLA)));

        if(null != edellisenSijoitteluajonValintatapajono) {
            valintatapajono.setValintaesitysHyvaksytty(edellisenSijoitteluajonValintatapajono.getValintaesitysHyvaksytty());
        }
    }

    private void kopioiHakemustenTiedotVanhaltaSijoitteluajoltaJaAsetaVarasijaNumerot(String hakukohdeOid,
                                                                                      Valintatapajono valintatapajono,
                                                                                      Map<String, Hakemus> edellisenSijoitteluajonHakemukset) {
        Collections.sort(valintatapajono.getHakemukset(), hakemusComparator);
        int varasija = 0;
        Optional<Hakemus> edellinenVarallaolevaHakemus = Optional.empty();
        for (Hakemus hakemus : valintatapajono.getHakemukset()) {
            if (hakemus.getTila() == VARALLA) {
                varasija++;
                setVarasijaNumero(varasija, hakemus, edellinenVarallaolevaHakemus, valintatapajono.getTasasijasaanto());
                edellinenVarallaolevaHakemus = Optional.of(hakemus);
            } else {
                hakemus.setVarasijanNumero(null);
            }
            Hakemus hakemuksenTilaEdellisestaAjosta = edellisenSijoitteluajonHakemukset.get(hakukohdeOid + valintatapajono.getOid() + hakemus.getHakemusOid());
            if (hakemuksenTilaEdellisestaAjosta != null && TilaTaulukot.kuuluuHyvaksyttyihinTiloihin(hakemus.getTila())) {
                if (!hakemus.getSiirtynytToisestaValintatapajonosta()) {
                    hakemus.setSiirtynytToisestaValintatapajonosta(hakemuksenTilaEdellisestaAjosta.getSiirtynytToisestaValintatapajonosta());
                }
            }
        }
    }

    private void setVarasijaNumero(int seuraavaVarasijaNumero,
                                   Hakemus hakemus,
                                   Optional<Hakemus> jononEdellinenVarallaOlevaHakemus,
                                   Tasasijasaanto tasasijasaanto) {
        boolean hakemusOnSamallaJonosijallaKuinJononEdellinenVarallaolija = jononEdellinenVarallaOlevaHakemus
            .map(edellinen -> edellinen.getJonosija().equals(hakemus.getJonosija()))
            .orElse(false);
        if (!tasasijasaanto.equals(ARVONTA) && hakemusOnSamallaJonosijallaKuinJononEdellinenVarallaolija) {
            hakemus.setVarasijanNumero(jononEdellinenVarallaOlevaHakemus.get().getVarasijanNumero());
        } else {
            hakemus.setVarasijanNumero(seuraavaVarasijaNumero);
        }
    }

    private Map<String, Hakemus> getStringHakemusMap(List<Hakukohde> olemassaolevatHakukohteet) {
        Map<String, Hakemus> hakemusHashMap = new ConcurrentHashMap<>();
        olemassaolevatHakukohteet.parallelStream().forEach(hakukohde ->
            hakukohde.getValintatapajonot().parallelStream().forEach(valintatapajono ->
                valintatapajono.getHakemukset().parallelStream().forEach(hakemus ->
                    hakemusHashMap.put(hakukohde.getOid() + valintatapajono.getOid() + hakemus.getHakemusOid(), hakemus)
                )
            )
        );
        return hakemusHashMap;
    }

    private Map<String, Valintatapajono> getStringValintatapajonoMap(List<Hakukohde> olemassaolevatHakukohteet) {
        Map<String, Valintatapajono> valintatapajonoHashMap = new ConcurrentHashMap<>();
        olemassaolevatHakukohteet.parallelStream().forEach(hakukohde ->
            hakukohde.getValintatapajonot().parallelStream().forEach(valintatapajono -> {
                valintatapajonoHashMap.put(valintatapajono.getOid(), valintatapajono);
            })
        );
        return valintatapajonoHashMap;
    }

    private int getMaara(List<Hakemus> hakemukset, List<HakemuksenTila> tilat) {
        return hakemukset.parallelStream().filter(h -> tilat.indexOf(h.getTila()) != -1)
                .reduce(0, (sum, b) -> sum + 1, Integer::sum);
    }

    private Optional<BigDecimal> alinHyvaksyttyPistemaara(List<Hakemus> hakemukset) {
        return hakemukset.parallelStream()
                .filter(h -> (h.getTila() == HYVAKSYTTY || h.getTila() == VARASIJALTA_HYVAKSYTTY) && !h.isHyvaksyttyHarkinnanvaraisesti())
                .filter(h -> h.getPisteet() != null)
                .map(Hakemus::getPisteet)
                .min(BigDecimal::compareTo);
    }

    private Pair<List<Hakukohde>, Set<Pair<String, String>>> merge(SijoitteluAjo uusiSijoitteluajo, List<Hakukohde> olemassaolevatHakukohteet, List<Hakukohde> uudetHakukohteet) {
        Set<Pair<String, String>> poistettavatValinnantulokset = new HashSet<>();
        Map<String, Hakukohde> sijoiteltavatHakukohteet = uudetHakukohteet.stream().collect(Collectors.toMap(Hakukohde::getOid, h -> h));
        olemassaolevatHakukohteet.forEach(vanhaHakukohde -> {
            Hakukohde sijoiteltavaHakukohde = sijoiteltavatHakukohteet.get(vanhaHakukohde.getOid());
            if (sijoiteltavaHakukohde == null) {
                sijoiteltavatHakukohteet.put(vanhaHakukohde.getOid(), vanhaHakukohde);
            } else {
                Map<String, Valintatapajono> sijoiteltavatValintatapajonot = sijoiteltavaHakukohde.getValintatapajonot().stream().collect(Collectors.toMap(Valintatapajono::getOid, v -> v));
                vanhaHakukohde.getValintatapajonot().forEach(vanhaValintatapajono -> {
                    Valintatapajono sijoiteltavaValintatapajono = sijoiteltavatValintatapajonot.get(vanhaValintatapajono.getOid());
                    if (sijoiteltavaValintatapajono != null) {
                        sijoiteltavaValintatapajono.setSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa(vanhaValintatapajono.getSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa());
                        sijoiteltavaValintatapajono.setSivssnovSijoittelunVarasijataytonRajoitus(vanhaValintatapajono.getSivssnovSijoittelunVarasijataytonRajoitus());

                        Map<String, Hakemus> sijoiteltavatHakemukset = sijoiteltavaValintatapajono.getHakemukset().stream().collect(Collectors.toMap(Hakemus::getHakemusOid, h -> h));
                        vanhaValintatapajono.getHakemukset().forEach(vanhaHakemus -> {
                            Hakemus sijoiteltavaHakemus = sijoiteltavatHakemukset.get(vanhaHakemus.getHakemusOid());
                            if (sijoiteltavaHakemus == null) {
                                poistettavatValinnantulokset.add(Pair.of(vanhaHakemus.getHakemusOid(), vanhaHakukohde.getOid()));
                            } else {
                                sijoiteltavaHakemus.setEdellinenTila(vanhaHakemus.getTila());
                                sijoiteltavaHakemus.setTilaHistoria(vanhaHakemus.getTilaHistoria());
                                sijoiteltavaHakemus.setTasasijaJonosija(vanhaHakemus.getTasasijaJonosija());
                                sijoiteltavaHakemus.setVarasijanNumero(vanhaHakemus.getVarasijanNumero());
                                sijoiteltavaHakemus.setIlmoittautumisTila(vanhaHakemus.getIlmoittautumisTila());
                                if (sijoiteltavaHakemus.getTila() != HYLATTY) {
                                    sijoiteltavaHakemus.setTilankuvauksenTarkenne(
                                            vanhaHakemus.getTilankuvauksenTarkenne(),
                                            vanhaHakemus.getTilanKuvaukset()
                                    );
                                }
                            }
                        });
                    }
                });
            }
        });
        sijoiteltavatHakukohteet.values().forEach(hakukohde -> {
            HakukohdeItem hki = new HakukohdeItem();
            hki.setOid(hakukohde.getOid());
            uusiSijoitteluajo.getHakukohteet().add(hki);
            hakukohde.setSijoitteluajoId(uusiSijoitteluajo.getSijoitteluajoId());
        });
        return Pair.of(new ArrayList<>(sijoiteltavatHakukohteet.values()), poistettavatValinnantulokset);
    }

    private SijoitteluAjo createSijoitteluAjo(String hakuOid) {
        SijoitteluAjo sijoitteluAjo = new SijoitteluAjo();
        Long now = System.currentTimeMillis();
        sijoitteluAjo.setSijoitteluajoId(now);
        sijoitteluAjo.setHakuOid(hakuOid);
        return sijoitteluAjo;
    }

    private SijoitteluAjo createValiSijoitteluAjo(ValiSijoittelu sijoittelu) {
        SijoitteluAjo sijoitteluAjo = createSijoitteluAjo(sijoittelu.getHakuOid());
        sijoittelu.getSijoitteluajot().add(sijoitteluAjo);
        return sijoitteluAjo;
    }

    private ValiSijoittelu createValiSijoittelu(String hakuoid) {
        ValiSijoittelu sijoittelu = new ValiSijoittelu();
        sijoittelu.setCreated(new Date());
        sijoittelu.setSijoitteluId(System.currentTimeMillis());
        sijoittelu.setHakuOid(hakuoid);
        return sijoittelu;
    }

    private Map<String, VastaanottoDTO> aiemmanVastaanotonHakukohdePerHakija(String hakuOid) {
        try {
            return valintaTulosServiceResource.haunKoulutuksenAlkamiskaudenVastaanototYhdenPaikanSaadoksenPiirissa(hakuOid)
                    .stream().collect(Collectors.toMap(VastaanottoDTO::getHenkiloOid, Function.identity()));
        } catch (WebApplicationException e) {
            String responseContent = e.getResponse().readEntity(String.class);
            LOG.error("Virhe haettassa haunKoulutuksenAlkamiskaudenVastaanototYhdenPaikanSaadoksenPiirissa(" +
                hakuOid + ") ; response: " + responseContent, e);
            throw e;
        }
    }

}
