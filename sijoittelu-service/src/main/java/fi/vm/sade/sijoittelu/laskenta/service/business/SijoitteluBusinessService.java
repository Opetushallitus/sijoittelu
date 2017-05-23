package fi.vm.sade.sijoittelu.laskenta.service.business;

import akka.actor.ActorRef;
import com.google.common.collect.Sets.SetView;

import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithm;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluajoWrapperFactory;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor.PostSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilaTaulukot;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.sijoittelu.domain.comparator.HakemusComparator;
import fi.vm.sade.sijoittelu.domain.dto.VastaanottoDTO;
import fi.vm.sade.sijoittelu.laskenta.actors.messages.PoistaHakukohteet;
import fi.vm.sade.sijoittelu.laskenta.actors.messages.PoistaVanhatAjotSijoittelulta;
import fi.vm.sade.sijoittelu.laskenta.external.resource.VirkailijaValintaTulosServiceResource;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriDTO;
import fi.vm.sade.sijoittelu.laskenta.service.it.TarjontaIntegrationService;
import fi.vm.sade.sijoittelu.laskenta.util.HakuUtil;
import fi.vm.sade.sijoittelu.tulos.dao.HakukohdeDao;
import fi.vm.sade.sijoittelu.tulos.dao.SijoitteluDao;
import fi.vm.sade.sijoittelu.tulos.dao.ValintatulosDao;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.service.impl.converters.SijoitteluTulosConverter;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintatulosservice.valintarekisteri.domain.NotFoundException;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.intersection;
import static java.util.Arrays.*;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang.StringUtils.join;

import javax.ws.rs.WebApplicationException;

@Service
public class SijoitteluBusinessService {
    private static final Logger LOG = LoggerFactory.getLogger(SijoitteluBusinessService.class);

    private HakemusComparator hakemusComparator = new HakemusComparator();
    private final String KK_KOHDEJOUKKO = "haunkohdejoukko_12";
    private final Set<String> amkopeKohdejoukonTarkenteet = new HashSet<String>() {{
        add("haunkohdejoukontarkenne_2");
        add("haunkohdejoukontarkenne_4");
        add("haunkohdejoukontarkenne_5");
    }};

    private final int maxAjoMaara;
    private final int maxErillisAjoMaara;
    private final ValintatulosDao valintatulosDao;
    private final HakukohdeDao hakukohdeDao;
    private final SijoitteluDao sijoitteluDao;
    private final SijoitteluTulosConverter sijoitteluTulosConverter;
    private final ActorService actorService;
    private final TarjontaIntegrationService tarjontaIntegrationService;
    private final VirkailijaValintaTulosServiceResource valintaTulosServiceResource;
    private final ValintatulosWithVastaanotto valintatulosWithVastaanotto;
    private final Collection<PostSijoitteluProcessor> postSijoitteluProcessors;
    private final Collection<PreSijoitteluProcessor> preSijoitteluProcessors;
    private final ValintarekisteriService valintarekisteriService;

    @Value(value = "${sijoittelu-service.saveSijoitteluToValintarekisteri}")
    private boolean saveSijoitteluToValintarekisteri;

    @Value(value = "${valintalaskenta-ui.read-from-valintarekisteri}")
    private boolean readSijoitteluFromValintarekisteri;

    @Autowired
    public SijoitteluBusinessService(@Value("${sijoittelu.maxAjojenMaara:20}") int maxAjoMaara,
                                     @Value("${sijoittelu.maxErillisAjojenMaara:300}") int maxErillisAjoMaara,
                                     ValintatulosDao valintatulosDao,
                                     HakukohdeDao hakukohdeDao,
                                     SijoitteluDao sijoitteluDao,
                                     SijoitteluTulosConverter sijoitteluTulosConverter,
                                     ActorService actorService,
                                     TarjontaIntegrationService tarjontaIntegrationService,
                                     VirkailijaValintaTulosServiceResource valintaTulosServiceResource,
                                     ValintarekisteriService valintarekisteriService) {
        this.maxAjoMaara = maxAjoMaara;
        this.maxErillisAjoMaara = maxErillisAjoMaara;
        this.valintatulosDao = valintatulosDao;
        this.hakukohdeDao = hakukohdeDao;
        this.sijoitteluDao = sijoitteluDao;
        this.sijoitteluTulosConverter = sijoitteluTulosConverter;
        this.actorService = actorService;
        this.tarjontaIntegrationService = tarjontaIntegrationService;
        this.valintaTulosServiceResource = valintaTulosServiceResource;
        this.valintatulosWithVastaanotto = new ValintatulosWithVastaanotto(valintatulosDao, valintaTulosServiceResource);
        this.preSijoitteluProcessors = PreSijoitteluProcessor.defaultPreProcessors();
        this.postSijoitteluProcessors = PostSijoitteluProcessor.defaultPostProcessors();
        this.valintarekisteriService = valintarekisteriService;
    }

    public void sijoittele(HakuDTO haku, Set<String> eiSijoitteluunMenevatJonot, Set<String> valintaperusteidenValintatapajonot) {
        if( readSijoitteluFromValintarekisteri ) {
            valintarekisteriSijoittelu(haku, eiSijoitteluunMenevatJonot, valintaperusteidenValintatapajonot);
        } else {
            mongoSijoittelu(haku, eiSijoitteluunMenevatJonot, valintaperusteidenValintatapajonot);
        }
    }

    private void valintarekisteriSijoittelu(HakuDTO haku, Set<String> eiSijoitteluunMenevatJonot, Set<String> valintaperusteidenValintatapajonot) {
        long startTime = System.currentTimeMillis();
        String hakuOid = haku.getHakuOid();
        StopWatch stopWatch = new StopWatch("Haun " + hakuOid + " sijoittelu");

        LOG.info("Sijoittelu haulle {} alkaa. Luetaan sijoittelu valintarekisteristä!", hakuOid);
        stopWatch.start("Luetaan sijoittelu valintarekisteristä");
        SijoitteluAjo viimeisinSijoitteluajo = readSijoitteluajoFromValintarekisteri(haku.getHakuOid());
        stopWatch.stop();

        stopWatch.start("Päätellään hakukohde- ja valintatapajonotiedot");
        List<Hakukohde> uudetHakukohteet = haku.getHakukohteet().stream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        List<Hakukohde> olemassaolevatHakukohteet = Collections.emptyList();
        if (viimeisinSijoitteluajo != null) {
            olemassaolevatHakukohteet = valintarekisteriService.getSijoitteluajonHakukohteet(viimeisinSijoitteluajo.getSijoitteluajoId());
            validateSijoittelunJonot(uudetHakukohteet, olemassaolevatHakukohteet, eiSijoitteluunMenevatJonot, valintaperusteidenValintatapajonot, stopWatch);
        }
        stopWatch.stop();

        SijoitteluAjo uusiSijoitteluajo = createSijoitteluAjo(hakuOid);
        stopWatch.start("Mergataan hakukohteet");
        List<Hakukohde> kaikkiHakukohteet = merge(uusiSijoitteluajo, olemassaolevatHakukohteet, uudetHakukohteet);
        stopWatch.stop();
        stopWatch.start("Haetaan valintatulokset vastaanottoineen");
        List<Valintatulos> valintatulokset = valintarekisteriService.getValintatulokset(hakuOid);
        stopWatch.stop();
        stopWatch.start("Haetaan kauden aiemmat vastaanotot");
        Map<String, VastaanottoDTO> kaudenAiemmatVastaanotot = aiemmanVastaanotonHakukohdePerHakija(hakuOid);
        stopWatch.stop();
        LOG.info("Haun {} sijoittelun koko: {} olemassaolevaa, {} uutta, {} valintatulosta", hakuOid, olemassaolevatHakukohteet.size(), uudetHakukohteet.size(), valintatulokset.size());
        stopWatch.start("Luodaan sijoitteluajoWrapper ja asetetaan parametrit");
        final SijoitteluajoWrapper sijoitteluajoWrapper = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(uusiSijoitteluajo, kaikkiHakukohteet, valintatulokset, kaudenAiemmatVastaanotot);
        asetaSijoittelunParametrit(hakuOid, sijoitteluajoWrapper);
        sijoitteluajoWrapper.setEdellisenSijoittelunHakukohteet(olemassaolevatHakukohteet);
        stopWatch.stop();
        suoritaSijoittelu(startTime, stopWatch, hakuOid, uusiSijoitteluajo, sijoitteluajoWrapper);
        stopWatch.start("Kopioidaan edellisen sijoitteluajon tietoja");
        kopioiHakukohteenTiedotVanhaltaSijoitteluajolta(olemassaolevatHakukohteet, kaikkiHakukohteet);
        stopWatch.stop();

        LOG.info("Muuttuneita valintatuloksia: " + sijoitteluajoWrapper.getMuuttuneetValintatulokset().size());
        LOG.info("Muuttuneet valintatulokset: " + sijoitteluajoWrapper.getMuuttuneetValintatulokset());

        stopWatch.start("Tallennetaan vastaanotot");
        valintatulosWithVastaanotto.persistVastaanotot(sijoitteluajoWrapper.getMuuttuneetValintatulokset());
        stopWatch.stop();

        List<String> varasijapomput = sijoitteluajoWrapper.getVarasijapomput();
        varasijapomput.forEach(LOG::info);
        LOG.info("Haun {} sijoittelussa muuttui {} kpl valintatuloksia, pomppuja {} kpl", hakuOid, sijoitteluajoWrapper.getMuuttuneetValintatulokset().size(), varasijapomput.size());

        LOG.warn("Tallennetaan sijoitteluajo ainoastaan Valintarekisteriin!");
        stopWatch.start("Tallennetaan sijoitteluajo, hakukohteet ja valintatulokset Valintarekisteriin");
        tallennaSijoitteluToValintarekisteri(hakuOid, uusiSijoitteluajo, kaikkiHakukohteet, sijoitteluajoWrapper.getMuuttuneetValintatulokset(), stopWatch);
        stopWatch.stop();
        LOG.info(stopWatch.prettyPrint());
    }

    @Deprecated
    private void mongoSijoittelu(HakuDTO haku, Set<String> eiSijoitteluunMenevatJonot, Set<String> valintaperusteidenValintatapajonot) {
        long startTime = System.currentTimeMillis();
        String hakuOid = haku.getHakuOid();
        StopWatch stopWatch = new StopWatch("Haun " + hakuOid + " sijoittelu");

        LOG.info("Sijoittelu haulle {} alkaa.", hakuOid);

        stopWatch.start("Päätellään viimeisin sijoitteluajo");
        Sijoittelu sijoitteluForMongo = getOrCreateSijoittelu(haku.getHakuOid());
        SijoitteluAjo viimeisinSijoitteluajo = sijoitteluForMongo.getLatestSijoitteluajo();
        stopWatch.stop();

        stopWatch.start("Päätellään hakukohde- ja valintatapajonotiedot");
        List<Hakukohde> uudetHakukohteet = haku.getHakukohteet().stream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        List<Hakukohde> olemassaolevatHakukohteet = Collections.emptyList();
        if (viimeisinSijoitteluajo != null) {
            olemassaolevatHakukohteet = hakukohdeDao.getHakukohdeForSijoitteluajo(viimeisinSijoitteluajo.getSijoitteluajoId());
            validateSijoittelunJonot(uudetHakukohteet, olemassaolevatHakukohteet, eiSijoitteluunMenevatJonot, valintaperusteidenValintatapajonot, stopWatch);
        }
        stopWatch.stop();

        SijoitteluAjo uusiSijoitteluajo = createSijoitteluAjo(sijoitteluForMongo);
        stopWatch.start("Mergataan hakukohteet");
        List<Hakukohde> kaikkiHakukohteet = merge(uusiSijoitteluajo, olemassaolevatHakukohteet, uudetHakukohteet);
        stopWatch.stop();
        stopWatch.start("Haetaan valintatulokset vastaanottoineen");
        List<Valintatulos> valintatulokset = valintatulosWithVastaanotto.forHaku(hakuOid);
        stopWatch.stop();
        stopWatch.start("Haetaan kauden aiemmat vastaanotot");
        Map<String, VastaanottoDTO> kaudenAiemmatVastaanotot = aiemmanVastaanotonHakukohdePerHakija(hakuOid);
        stopWatch.stop();
        LOG.info("Haun {} sijoittelun koko: {} olemassaolevaa, {} uutta, {} valintatulosta", hakuOid, olemassaolevatHakukohteet.size(), uudetHakukohteet.size(), valintatulokset.size());
        stopWatch.start("Luodaan sijoitteluajoWrapper ja asetetaan parametrit");
        final SijoitteluajoWrapper sijoitteluajoWrapper = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(uusiSijoitteluajo, kaikkiHakukohteet, valintatulokset, kaudenAiemmatVastaanotot);
        asetaSijoittelunParametrit(hakuOid, sijoitteluajoWrapper);
        sijoitteluajoWrapper.setEdellisenSijoittelunHakukohteet(olemassaolevatHakukohteet);
        stopWatch.stop();
        suoritaSijoittelu(startTime, stopWatch, hakuOid, uusiSijoitteluajo, sijoitteluajoWrapper);
        processOldApplications(stopWatch, olemassaolevatHakukohteet, kaikkiHakukohteet, hakuOid);

        LOG.info("Ennen mergeä muuttuneita valintatuloksia: " + sijoitteluajoWrapper.getMuuttuneetValintatulokset().size());
        LOG.info("Ennen mergeä muuttuneet valintatulokset: " + sijoitteluajoWrapper.getMuuttuneetValintatulokset());

        stopWatch.start("Mergataan valintatulokset");
        List<Valintatulos> mergatut = valintatulosDao.mergaaValintatulos(kaikkiHakukohteet, sijoitteluajoWrapper.getMuuttuneetValintatulokset());
        stopWatch.stop();
        stopWatch.start("Persistoidaan valintatulokset");
        valintatulosWithVastaanotto.persistValintatulokset(mergatut);
        stopWatch.stop();
        sijoitteluajoWrapper.setMuuttuneetValintatulokset(mergatut);

        List<String> varasijapomput = sijoitteluajoWrapper.getVarasijapomput();
        varasijapomput.forEach(LOG::info);
        LOG.info("Haun {} sijoittelussa muuttui {} kpl valintatuloksia, pomppuja {} kpl", hakuOid, sijoitteluajoWrapper.getMuuttuneetValintatulokset().size(), varasijapomput.size());
        persistSijoitteluAndSiivoaVanhatAjot(stopWatch, hakuOid, sijoitteluForMongo, uusiSijoitteluajo, kaikkiHakukohteet, sijoitteluajoWrapper.getMuuttuneetValintatulokset(), maxAjoMaara);
        LOG.info(stopWatch.prettyPrint());
    }

    private SijoitteluAjo readSijoitteluajoFromValintarekisteri(String hakuOid) {
        try {
            LOG.info("Luetaan sijoittelu valintarekisteristä");
            SijoitteluAjo viimeisinSijoitteluajo = valintarekisteriService.getLatestSijoitteluajo(hakuOid);
            return viimeisinSijoitteluajo;
        } catch (NotFoundException iae) {
            LOG.info("Viimeisintä sijoitteluajoa haulle {} ei löydy valintarekisteristä.", hakuOid);
            LOG.warn(iae.getMessage());
            return null;
        }
    }

    private void validateSijoittelunJonot(List<Hakukohde> uudetHakukohteet,
                                          List<Hakukohde> olemassaolevatHakukohteet,
                                          Set<String> eiSijoitteluunMenevatJonot,
                                          Set<String> valintaperusteidenValintatapajonot,
                                          StopWatch stopWatch) {

        Consumer<String> handleError = (msg) -> {
            LOG.error(msg);
            stopWatch.stop();
            LOG.info(stopWatch.prettyPrint());
            throw new RuntimeException(msg);
        };

        Set<String> joSijoitellutJonot = hakukohteidenJonoOidit(olemassaolevatHakukohteet);
        SetView<String> sijoittelustaPoistetutJonot = difference(joSijoitellutJonot, hakukohteidenJonoOidit(uudetHakukohteet));
        SetView<String> aktiivisetSijoittelustaPoistetutJonot = intersection(eiSijoitteluunMenevatJonot, sijoittelustaPoistetutJonot);
        if (aktiivisetSijoittelustaPoistetutJonot.size() > 0) {
            handleError.accept("Edellisessä sijoittelussa olleet jonot [" + join(aktiivisetSijoittelustaPoistetutJonot, ", ") +
                    "] puuttuvat sijoittelusta, vaikka ne ovat valintaperusteissa yhä aktiivisina");
        }
        SetView<String> valintaperusteistaPuuttuvatSijoitellutJonot = difference(joSijoitellutJonot, valintaperusteidenValintatapajonot);
        if(valintaperusteistaPuuttuvatSijoitellutJonot.size() > 0) {
            handleError.accept("Edellisessä sijoittelussa olleet jonot [" + join(valintaperusteistaPuuttuvatSijoitellutJonot, ", ") +
                    "] ovat kadonneet valintaperusteista");
        }
    }

    private Set<String> hakukohteidenJonoOidit(List<Hakukohde> hakukohteet) {
        return unmodifiableSet(hakukohteet.stream()
                .flatMap(hakukohde -> hakukohde.getValintatapajonot().stream().map(Valintatapajono::getOid))
                .collect(toSet()));
    }

    private void suoritaSijoittelu(long startTime, StopWatch stopWatch, String hakuOid, SijoitteluAjo uusiSijoitteluajo, SijoitteluajoWrapper sijoitteluajoWrapper) {
        LOG.info("Suoritetaan sijoittelu haulle {}", hakuOid);
        stopWatch.start("Suoritetaan sijoittelu");
        uusiSijoitteluajo.setStartMils(startTime);
        SijoitteluAlgorithm.sijoittele(preSijoitteluProcessors, postSijoitteluProcessors, sijoitteluajoWrapper);
        uusiSijoitteluajo.setEndMils(System.currentTimeMillis());
        stopWatch.stop();
    }

    @Deprecated
    private void persistSijoitteluAndSiivoaVanhatAjot(StopWatch stopWatch,
                                                      String hakuOid,
                                                      Sijoittelu sijoittelu,
                                                      SijoitteluAjo uusiSijoitteluajo,
                                                      List<Hakukohde> hakukohteet,
                                                      List<Valintatulos> valintatulokset,
                                                      int sailytettavaAjoMaara) {
        try {
            stopWatch.start("Persistoidaan sijoittelu");
            sijoitteluDao.persistSijoittelu(sijoittelu);
            stopWatch.stop();
            LOG.info("Sijoittelu persistoitu haulle {}. Poistetaan vanhoja ajoja. Säästettävien ajojen määrää {}", sijoittelu.getHakuOid(), sailytettavaAjoMaara);
            stopWatch.start("Käynnistetään vanhojen sijoitteluajojen siivouksen taustaprosessi");
            siivoaVanhatAjotSijoittelulta(hakuOid, sijoittelu, sailytettavaAjoMaara);
            stopWatch.stop();
            if (saveSijoitteluToValintarekisteri) {
                LOG.info("Tallennetaan haun {} sijoittelu valintarekisteriin", hakuOid);
                stopWatch.start("Tallennetaan sijoitteluajo, hakukohteet ja valintatulokset Valintarekisteriin");
                try {
                    valintarekisteriService.tallennaSijoittelu(uusiSijoitteluajo, hakukohteet, valintatulokset);
                } catch (Exception e) {
                    LOG.warn(String.format(
                            "Sijoittelujon %s tallennus valintarekisteriin epäonnistui haulle %s.",
                            uusiSijoitteluajo.getSijoitteluajoId(), hakuOid
                    ), e);
                }
                stopWatch.stop();
            } else {
                LOG.info("Ohitetaan haun {} sijoittelun tallennus valintarekisteriin", hakuOid);
            }
        } catch (Exception e) {
            LOG.error("Sijoittelun persistointi haulle {} epäonnistui. Rollback hakukohteet", sijoittelu.getHakuOid());
            actorService.getSiivousActor().tell(new PoistaHakukohteet(sijoittelu, uusiSijoitteluajo.getSijoitteluajoId()), ActorRef.noSender());
            stopWatch.stop();
            LOG.info(stopWatch.prettyPrint());
            throw e;
        }
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

    private void asetaSijoittelunParametrit(String hakuOid, SijoitteluajoWrapper sijoitteluAjo) {
        try {
            setOptionalHakuAttributes(hakuOid, sijoitteluAjo);
            setParametersFromTarjonta(hakuOid, sijoitteluAjo);
            LOG.info("Sijoittelun ohjausparametrit asetettu haulle {}. onko korkeakouluhaku: {}, kaikki kohteet sijoittelussa: {}, hakukierros päätty: {}, varasijasäännöt astuvat voimaan: {}, varasijasäännöt voimassa: {}",
                    hakuOid, sijoitteluAjo.isKKHaku(), sijoitteluAjo.getKaikkiKohteetSijoittelussa(), sijoitteluAjo.getHakuKierrosPaattyy(), sijoitteluAjo.getVarasijaSaannotAstuvatVoimaan(), sijoitteluAjo.varasijaSaannotVoimassa());
        } catch (IllegalStateException e) {
            throw new RuntimeException("Sijoittelua haulle " + hakuOid + " ei voida suorittaa", e);
        }
    }

    private void setParametersFromTarjonta(String hakuOid, SijoitteluajoWrapper sijoitteluAjo) {
        ParametriDTO parametri = null;
        try {
            parametri = tarjontaIntegrationService.getHaunParametrit(hakuOid);
        } catch (Exception e) {
            throw new IllegalStateException("tarjonnasta ei saatu haun ohjausparametreja");
        }

        LocalDateTime hakukierrosPaattyy = Optional.ofNullable(parametri)
                .flatMap(p -> Optional.ofNullable(p.getPH_HKP()))
                .flatMap(p -> Optional.ofNullable(p.getDate()))
                .map(this::fromTimestamp)
                .orElseThrow(() -> new IllegalStateException("ohjausparametria PH_HKP (hakukierros päättyy) parametria ei ole asetettu"));

        Optional<LocalDateTime> kaikkiKohteetSijoittelussa = Optional.ofNullable(parametri)
                .flatMap(p -> Optional.ofNullable(p.getPH_VTSSV()))
                .flatMap(p -> Optional.ofNullable(p.getDate()))
                .map(this::fromTimestamp);
        Optional<LocalDateTime> varasijasaannotAstuvatVoimaan = Optional.ofNullable(parametri)
                .flatMap(p -> Optional.ofNullable(p.getPH_VSSAV()))
                .flatMap(p -> Optional.ofNullable(p.getDate()))
                .map(this::fromTimestamp);
        Optional<LocalDateTime> varasijatayttoPaattyy = Optional.ofNullable(parametri)
                .flatMap(p -> Optional.ofNullable(p.getPH_VSTP()))
                .flatMap(p -> Optional.ofNullable(p.getDate()))
                .map(this::fromTimestamp);

        if (sijoitteluAjo.isKKHaku()) {
            if (!kaikkiKohteetSijoittelussa.isPresent()) {
                throw new IllegalStateException("kyseessä korkeakouluhaku ja ohjausparametria PH_VTSSV (kaikki kohteet sijoittelussa) ei ole asetettu");
            }
            if (!varasijasaannotAstuvatVoimaan.isPresent()) {
                throw new IllegalStateException("kyseessä korkeakouluhaku ja ohjausparametria PH_VSSAV (varasijasäännöt astuvat voimaan) ei ole asetettu");
            }
            if (hakukierrosPaattyy.isBefore(varasijasaannotAstuvatVoimaan.get())) {
                throw new IllegalStateException("hakukierros on asetettu päättymään ennen kuin varasija säännöt astuvat voimaan");
            }
        }
        if (kaikkiKohteetSijoittelussa.isPresent() && hakukierrosPaattyy.isBefore(kaikkiKohteetSijoittelussa.get())) {
            throw new IllegalStateException("hakukierros on asetettu päättymään ennen kuin kaikkien kohteiden tulee olla sijoittelussa");
        }
        if (hakukierrosPaattyy.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("hakukierros on päättynyt");
        }

        sijoitteluAjo.setHakuKierrosPaattyy(hakukierrosPaattyy);
        kaikkiKohteetSijoittelussa.ifPresent(sijoitteluAjo::setKaikkiKohteetSijoittelussa);
        varasijasaannotAstuvatVoimaan.ifPresent(sijoitteluAjo::setVarasijaSaannotAstuvatVoimaan);
        varasijatayttoPaattyy.ifPresent(sijoitteluAjo::setVarasijaTayttoPaattyy);
    }

    private void setOptionalHakuAttributes(String hakuOid, SijoitteluajoWrapper sijoitteluAjo) {
        fi.vm.sade.sijoittelu.laskenta.external.resource.dto.HakuDTO hakuDto = null;
        try {
            hakuDto = tarjontaIntegrationService.getHakuByHakuOid(hakuOid);
        } catch (Exception e) {
            throw new IllegalStateException("tarjonnasta ei saatu haun tietoja", e);
        }

        String kohdejoukko = HakuUtil.getHaunKohdejoukko(hakuDto).orElseThrow(() ->
                new IllegalStateException("tarjonnasta ei saatu haun kohdejoukkoa"));

        boolean isKKHaku = kohdejoukko.equals(KK_KOHDEJOUKKO);
        sijoitteluAjo.setKKHaku(isKKHaku);

        Optional<String> kohdejoukonTarkenne = HakuUtil.gethaunKohdejoukonTarkenne(hakuDto);
        sijoitteluAjo.setAmkopeHaku(isKKHaku && kohdejoukonTarkenne.map(this::isAmmatillinenOpettajakoulutus).orElse(false));
    }

    private boolean isAmmatillinenOpettajakoulutus(String haunKohdeJoukonTarkenne) {
        return amkopeKohdejoukonTarkenteet.contains(haunKohdeJoukonTarkenne);
    }

    private LocalDateTime fromTimestamp(Long timestamp) {
        return LocalDateTime.ofInstant(new Date(timestamp).toInstant(), ZoneId.systemDefault());
    }

    public List<HakukohdeDTO> valisijoittele(HakuDTO sijoitteluTyyppi) {
        long startTime = System.currentTimeMillis();
        String hakuOid = sijoitteluTyyppi.getHakuOid();
        StopWatch stopWatch = new StopWatch("Haun " + hakuOid + " välisijoittelu");

        stopWatch.start("Alustetaan uusi välisijoittelu ja haetaan hakukohteet");
        ValiSijoittelu sijoittelu = createValiSijoittelu(hakuOid);
        List<Hakukohde> uudetHakukohteet = sijoitteluTyyppi.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        List<Hakukohde> olemassaolevatHakukohteet = Collections.<Hakukohde>emptyList();
        SijoitteluAjo uusiSijoitteluajo = createValiSijoitteluAjo(sijoittelu);
        List<Hakukohde> kaikkiHakukohteet = merge(uusiSijoitteluajo, olemassaolevatHakukohteet, uudetHakukohteet);
        SijoitteluajoWrapper sijoitteluajoWrapper = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(uusiSijoitteluajo, kaikkiHakukohteet, Collections.emptyList(), Collections.emptyMap());
        stopWatch.stop();

        suoritaSijoittelu(startTime, stopWatch, hakuOid, uusiSijoitteluajo, sijoitteluajoWrapper);
        processOldApplications(stopWatch, olemassaolevatHakukohteet, kaikkiHakukohteet, hakuOid);

        List<HakukohdeDTO> result = kaikkiHakukohteet.parallelStream().map(h -> sijoitteluTulosConverter.convert(h)).collect(Collectors.toList());
        LOG.info(stopWatch.prettyPrint());
        return result;
    }

    private class HakukohdeOidAndSijoitteluAjoId {
        private final String hakukohdeOid;
        private final Long sijoitteluAjoId;
        public HakukohdeOidAndSijoitteluAjoId(String hakukohdeOid, Long sijoitteluAjoId) {
            this.hakukohdeOid = hakukohdeOid;
            this.sijoitteluAjoId = sijoitteluAjoId;
        }

        public Long getSijoitteluAjoId() {
            return sijoitteluAjoId;
        }

        public String getHakukohdeOid() {
            return hakukohdeOid;
        }
    }

    @Deprecated
    private Map<String,Long> hakukohdeToLastSijoitteluAjoId(final Sijoittelu sijoittelu) {
        return sijoittelu.getSijoitteluajot().stream()
                .flatMap(s -> s.getHakukohteet().stream().map(h -> new HakukohdeOidAndSijoitteluAjoId(h.getOid(),s.getSijoitteluajoId())))
                .collect(Collectors.toMap(
                        HakukohdeOidAndSijoitteluAjoId::getHakukohdeOid,
                        s -> s.getSijoitteluAjoId(),
                        (sijoitteluAjoId1, sijoitteluAjoId2) -> Math.max(sijoitteluAjoId1, sijoitteluAjoId2)
                ));
    }

    public long erillissijoittele(HakuDTO hakuDTO) {
        if(readSijoitteluFromValintarekisteri) {
            return valintarekisteriErillissijoittelu(hakuDTO);
        } else {
            return mongoErillissijoittelu(hakuDTO);
        }
    }

    private Hakukohde getErillissijoittelunHakukohde(HakuDTO haku) {
        if( 1 == haku.getHakukohteet().size() ) {
            return DomainConverter.convertToHakukohde(haku.getHakukohteet().get(0));
        }
        LOG.error("Haun {} erillissijoitteluun saatiin {} kpl hakukohteita. Voi olla vain yksi!", haku.getHakuOid(), haku.getHakukohteet().size());
        throw new IllegalStateException("Haun " + haku.getHakuOid() + " erillissijoitteluun saatiin " + haku.getHakukohteet().size() + " kpl hakukohteita. Voi olla vain yksi!");
    }

    private long valintarekisteriErillissijoittelu(HakuDTO haku) {
        long startTime = System.currentTimeMillis();
        String hakuOid = haku.getHakuOid();

        StopWatch stopWatch = new StopWatch("Haun " + hakuOid + " erillissijoittelu");
        LOG.info("Erillissijoittelu haulle {} alkaa. Luetaan sijoittelu valintarekisteristä!", hakuOid);

        stopWatch.start("Luetaan viimeisin erillissijoitteluajo valintarekisteristä");
        SijoitteluAjo viimeisinSijoitteluajo = readSijoitteluajoFromValintarekisteri(haku.getHakuOid());
        stopWatch.stop();

        stopWatch.start("Haetaan erillissijoittelun hakukohde");
        Hakukohde hakukohdeValintalaskennassa = getErillissijoittelunHakukohde(haku);
        String sijoiteltavanHakukohteenOid = hakukohdeValintalaskennassa.getOid();

        List<Hakukohde> viimeisimmanSijoitteluajonHakukohteet = Collections.emptyList();
        List<Hakukohde> hakukohdeViimeisimmassaSijoitteluajossa = Collections.emptyList();

        if( null != viimeisinSijoitteluajo ) {
            viimeisimmanSijoitteluajonHakukohteet = valintarekisteriService.getSijoitteluajonHakukohteet(viimeisinSijoitteluajo.getSijoitteluajoId());
            hakukohdeViimeisimmassaSijoitteluajossa = viimeisimmanSijoitteluajonHakukohteet.stream().filter(hk -> hk.getOid().equals(sijoiteltavanHakukohteenOid)).findFirst().map(hk -> Arrays.asList(hk)).orElse(Collections.emptyList());
        }

        SijoitteluAjo uusiSijoitteluajo = createSijoitteluAjo(hakuOid);
        List<Hakukohde> hakukohdeTassaSijoittelussa = merge(uusiSijoitteluajo, hakukohdeViimeisimmassaSijoitteluajossa, Arrays.asList(hakukohdeValintalaskennassa));
        stopWatch.stop();

        stopWatch.start("Haetaan valintatulokset vastaanottoineen");
        List<Valintatulos> valintatulokset = valintarekisteriService.getValintatulokset(hakuOid);
        stopWatch.stop();
        stopWatch.start("Haetaan kauden aiemmat vastaanotot");
        Map<String, VastaanottoDTO> kaudenAiemmatVastaanotot = aiemmanVastaanotonHakukohdePerHakija(hakuOid);
        stopWatch.stop();

        stopWatch.start("Luodaan sijoitteluajoWrapper");
        final SijoitteluajoWrapper sijoitteluajoWrapper = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(uusiSijoitteluajo, hakukohdeTassaSijoittelussa, valintatulokset, kaudenAiemmatVastaanotot);
        asetaSijoittelunParametrit(hakuOid, sijoitteluajoWrapper);
        stopWatch.stop();

        suoritaSijoittelu(startTime, stopWatch, hakuOid, uusiSijoitteluajo, sijoitteluajoWrapper);

        stopWatch.start("Kopioidaan edellisen sijoitteluajon tietoja");
        kopioiHakukohteenTiedotVanhaltaSijoitteluajolta(hakukohdeViimeisimmassaSijoitteluajossa, hakukohdeTassaSijoittelussa);
        List<Hakukohde> kaikkiTallennettavatHakukohteet = new ArrayList<>();
        kaikkiTallennettavatHakukohteet.addAll(hakukohdeTassaSijoittelussa);
        kaikkiTallennettavatHakukohteet.addAll(viimeisimmanSijoitteluajonHakukohteet.stream().filter(hk -> !hk.getOid().equals(sijoiteltavanHakukohteenOid)).map(hakukohde -> {
            hakukohde.setId(null);
            hakukohde.setSijoitteluajoId(uusiSijoitteluajo.getSijoitteluajoId());
            HakukohdeItem item = new HakukohdeItem();
            item.setOid(hakukohde.getOid());
            uusiSijoitteluajo.getHakukohteet().add(item);
            return hakukohde;
        }).collect(Collectors.toList()));
        stopWatch.stop();

        LOG.warn("Tallennetaan sijoitteluajo ainoastaan Valintarekisteriin!");
        stopWatch.start("Tallennetaan sijoitteluajo, hakukohteet ja valintatulokset Valintarekisteriin");
        tallennaSijoitteluToValintarekisteri(hakuOid, uusiSijoitteluajo, kaikkiTallennettavatHakukohteet, sijoitteluajoWrapper.getMuuttuneetValintatulokset(), stopWatch);
        stopWatch.stop();
        LOG.info(stopWatch.prettyPrint());

        return uusiSijoitteluajo.getSijoitteluajoId();
    }



    @Deprecated
    private long mongoErillissijoittelu(HakuDTO sijoitteluTyyppi) {
        long startTime = System.currentTimeMillis();
        StopWatch stopWatch = new StopWatch("Haun " + sijoitteluTyyppi.getHakuOid() + " erillissijoittelu");
        String hakuOid = sijoitteluTyyppi.getHakuOid();
        LOG.info("Sijoittelu haulle {} alkaa.", hakuOid);

        stopWatch.start("Alustetaan uusi erillissijoitteluajo ja haetaan sen hakukohteet");
        final Sijoittelu sijoittelu = getOrCreateErillisSijoittelu(hakuOid);
        List<Hakukohde> uudetHakukohteet = sijoitteluTyyppi.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        List<Hakukohde> olemassaolevatHakukohteet = new ArrayList<>();
        SijoitteluAjo viimeisinSijoitteluajo = sijoittelu.getLatestSijoitteluajo();
        if (viimeisinSijoitteluajo != null) {
            for (Hakukohde hakukohde: uudetHakukohteet) {
                Hakukohde vanhaTulos = hakukohdeDao.getHakukohdeForSijoitteluajo(viimeisinSijoitteluajo.getSijoitteluajoId(), hakukohde.getOid());
                if(vanhaTulos != null) {
                    olemassaolevatHakukohteet.add(vanhaTulos);
                }
            }
        }
        SijoitteluAjo uusiSijoitteluajo = createSijoitteluAjo(sijoittelu);
        List<Hakukohde> tamanSijoittelunHakukohteet = merge(uusiSijoitteluajo, olemassaolevatHakukohteet, uudetHakukohteet);
        stopWatch.stop();

        stopWatch.start("Haetaan valintatulokset vastaanottoineen");
        List<Valintatulos> valintatulokset = valintatulosWithVastaanotto.forHaku(hakuOid);
        stopWatch.stop();
        stopWatch.start("Haetaan kauden aiemmat vastaanotot");
        Map<String, VastaanottoDTO> kaudenAiemmatVastaanotot = aiemmanVastaanotonHakukohdePerHakija(hakuOid);
        stopWatch.stop();

        stopWatch.start("Luodaan sijoitteluajoWrapper");
        final SijoitteluajoWrapper sijoitteluajoWrapper = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(uusiSijoitteluajo, tamanSijoittelunHakukohteet, valintatulokset, kaudenAiemmatVastaanotot);
        asetaSijoittelunParametrit(hakuOid, sijoitteluajoWrapper);
        stopWatch.stop();

        suoritaSijoittelu(startTime, stopWatch, hakuOid, uusiSijoitteluajo, sijoitteluajoWrapper);
        processOldApplications(stopWatch, olemassaolevatHakukohteet, tamanSijoittelunHakukohteet, hakuOid);

        stopWatch.start("Lisätään muut kuin tässä sijoittelussa mukana olleet hakukohteet");
        Map<String, Long> kaikkiHakukohteetJotkaOnJoskusSijoiteltuToLastSijoitteluAjoId = hakukohdeToLastSijoitteluAjoId(sijoittelu);
        final Set<String> tamanSijoittelunHakukohdeOids = tamanSijoittelunHakukohteet.stream().map(Hakukohde::getOid).collect(Collectors.toSet());
        List<Hakukohde> kaikkiHakukohteet = new LinkedList<>(tamanSijoittelunHakukohteet);
        // Clone previous hakukohdes
        kaikkiHakukohteetJotkaOnJoskusSijoiteltuToLastSijoitteluAjoId.forEach(
                (hakukohdeOid, latestSijoitteluForHakukohde) -> {
                    if(!tamanSijoittelunHakukohdeOids.contains(hakukohdeOid)) {
                        Hakukohde h = hakukohdeDao.getHakukohdeForSijoitteluajo(latestSijoitteluForHakukohde, hakukohdeOid);
                        if(h == null) {
                            // hakukohde has been removed
                        } else {
                            h.setId(null);
                            h.setSijoitteluajoId(uusiSijoitteluajo.getSijoitteluajoId());
                            hakukohdeDao.persistHakukohde(h, hakuOid);
                            HakukohdeItem item = new HakukohdeItem();
                            item.setOid(hakukohdeOid);
                            sijoittelu.getLatestSijoitteluajo().getHakukohteet().add(item);
                            kaikkiHakukohteet.add(h);
                        }
                    }
                }
        );
        stopWatch.stop();

        persistSijoitteluAndSiivoaVanhatAjot(stopWatch, hakuOid, sijoittelu, uusiSijoitteluajo, kaikkiHakukohteet, valintatulokset, maxErillisAjoMaara);
        LOG.info(stopWatch.prettyPrint());
        return uusiSijoitteluajo.getSijoitteluajoId();
    }


    private void kopioiHakukohteenTiedotVanhaltaSijoitteluajolta(final List<Hakukohde> edellisenSijoitteluajonHakukohteet, final List<Hakukohde> tamanSijoitteluajonHakukohteet) {
        Map<String, Hakemus> hakemukset = getStringHakemusMap(edellisenSijoitteluajonHakukohteet);
        Map<String, Valintatapajono> valintatapajonot = getStringValintatapajonoMap(edellisenSijoitteluajonHakukohteet);
        tamanSijoitteluajonHakukohteet.parallelStream().forEach(hakukohde ->
            hakukohde.getValintatapajonot().forEach(valintatapajono -> {
                kopioiValintatapajononTiedotVanhaltaSijoitteluajolta(valintatapajono, valintatapajonot.get(valintatapajono.getOid()));
                kopioiHakemustenTiedotVanhaltaSijoitteluajolta(hakukohde.getOid(), valintatapajono, hakemukset);
            })
        );
    }

    private void kopioiValintatapajononTiedotVanhaltaSijoitteluajolta(Valintatapajono valintatapajono, Valintatapajono edellisenSijoitteluajonValintatapajono) {
        valintatapajono.setAlinHyvaksyttyPistemaara(alinHyvaksyttyPistemaara(valintatapajono.getHakemukset()).orElse(null));
        valintatapajono.setHyvaksytty(getMaara(valintatapajono.getHakemukset(), asList(HakemuksenTila.HYVAKSYTTY, HakemuksenTila.VARASIJALTA_HYVAKSYTTY)));
        valintatapajono.setVaralla(getMaara(valintatapajono.getHakemukset(), asList(HakemuksenTila.VARALLA)));

        if(null != edellisenSijoitteluajonValintatapajono) {
            valintatapajono.setValintaesitysHyvaksytty(edellisenSijoitteluajonValintatapajono.getValintaesitysHyvaksytty());
        }
    }

    private void kopioiHakemustenTiedotVanhaltaSijoitteluajolta(String hakukohdeOid, Valintatapajono valintatapajono, Map<String, Hakemus> edellisenSijoitteluajonHakemukset) {
        Collections.sort(valintatapajono.getHakemukset(), hakemusComparator);
        int varasija = 0;
        for (Hakemus hakemus : valintatapajono.getHakemukset()) {
            Hakemus edellinen = edellisenSijoitteluajonHakemukset.get(hakukohdeOid + valintatapajono.getOid() + hakemus.getHakemusOid());
            if (hakemus.getTila() == HakemuksenTila.VARALLA) {
                varasija++;
                hakemus.setVarasijanNumero(varasija);
            } else {
                hakemus.setVarasijanNumero(null);
            }
            if(edellinen != null && TilaTaulukot.kuuluuHyvaksyttyihinTiloihin(hakemus.getTila())) {
                if(hakemus.getSiirtynytToisestaValintatapajonosta() == false) {
                    hakemus.setSiirtynytToisestaValintatapajonosta(edellinen.getSiirtynytToisestaValintatapajonosta());
                }
            }
        }
    }



    @Deprecated
    private void processOldApplications(StopWatch stopWatch, final List<Hakukohde> olemassaolevatHakukohteet, final List<Hakukohde> kaikkiHakukohteet, String hakuOid) {
        stopWatch.start("processOldApplications");
        Map<String, Hakemus> hakemusHashMap = getStringHakemusMap(olemassaolevatHakukohteet);
        Map<String, Valintatapajono> valintatapajonoHashMap = getStringValintatapajonoMap(olemassaolevatHakukohteet);
        kaikkiHakukohteet.parallelStream().forEach(hakukohde -> {
                    hakukohde.getValintatapajonot().forEach(processValintatapaJono(valintatapajonoHashMap, hakemusHashMap, hakukohde));
                    hakukohdeDao.persistHakukohde(hakukohde, hakuOid);
                }
        );
        stopWatch.stop();
    }

    @Deprecated
    private Consumer<Valintatapajono> processValintatapaJono(Map<String, Valintatapajono> valintatapajonoHashMap, Map<String, Hakemus> hakemusHashMap, Hakukohde hakukohde) {
        return valintatapajono -> {
                    valintatapajono.setAlinHyvaksyttyPistemaara(alinHyvaksyttyPistemaara(valintatapajono.getHakemukset()).orElse(null));
                    valintatapajono.setHyvaksytty(getMaara(valintatapajono.getHakemukset(), asList(HakemuksenTila.HYVAKSYTTY, HakemuksenTila.VARASIJALTA_HYVAKSYTTY)));
                    valintatapajono.setVaralla(getMaara(valintatapajono.getHakemukset(), asList(HakemuksenTila.VARALLA)));

                    Valintatapajono vanhaValintatapajono = valintatapajonoHashMap.get(valintatapajono.getOid());
                    if(vanhaValintatapajono != null) {
                        valintatapajono.setValintaesitysHyvaksytty(vanhaValintatapajono.getValintaesitysHyvaksytty());
                    }
            
                    Collections.sort(valintatapajono.getHakemukset(), hakemusComparator);
                    int varasija = 0;
                    for (Hakemus hakemus : valintatapajono.getHakemukset()) {
                        Hakemus edellinen = hakemusHashMap.get(hakukohde.getOid() + valintatapajono.getOid() + hakemus.getHakemusOid());
                        if (edellinen != null && edellinen.getTilaHistoria() != null && !edellinen.getTilaHistoria().isEmpty()) {
                            hakemus.setTilaHistoria(edellinen.getTilaHistoria());
                            if (hakemus.getTila() != edellinen.getTila()) {
                                TilaHistoria th = new TilaHistoria();
                                th.setLuotu(new Date());
                                th.setTila(hakemus.getTila());
                                hakemus.getTilaHistoria().add(th);
                            }
                        } else {
                            TilaHistoria th = new TilaHistoria();
                            th.setLuotu(new Date());
                            th.setTila(hakemus.getTila());
                            hakemus.getTilaHistoria().add(th);
                        }
                        if (hakemus.getTila() == HakemuksenTila.VARALLA) {
                            varasija++;
                            hakemus.setVarasijanNumero(varasija);
                        } else {
                            hakemus.setVarasijanNumero(null);
                        }
                        if(edellinen != null && TilaTaulukot.kuuluuHyvaksyttyihinTiloihin(hakemus.getTila())) {
                            if(hakemus.getSiirtynytToisestaValintatapajonosta() == false) {
                                hakemus.setSiirtynytToisestaValintatapajonosta(edellinen.getSiirtynytToisestaValintatapajonosta());
                            }
                        }

                    }
                };
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
        return (int) hakemukset.parallelStream().filter(h -> tilat.indexOf(h.getTila()) != -1)
                .reduce(0, (sum, b) -> sum + 1, Integer::sum);
    }

    private Optional<BigDecimal> alinHyvaksyttyPistemaara(List<Hakemus> hakemukset) {
        return hakemukset.parallelStream()
                .filter(h -> (h.getTila() == HakemuksenTila.HYVAKSYTTY || h.getTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY) && !h.isHyvaksyttyHarkinnanvaraisesti())
                .filter(h -> h.getPisteet() != null)
                .map(Hakemus::getPisteet)
                .min(BigDecimal::compareTo);
    }

    // nykyisellaan vain korvaa hakukohteet, mietittava toiminta tarkemmin
    private List<Hakukohde> merge(SijoitteluAjo uusiSijoitteluajo, List<Hakukohde> olemassaolevatHakukohteet, List<Hakukohde> uudetHakukohteet) {
        Map<String, Hakukohde> kaikkiHakukohteet = new ConcurrentHashMap<>();
        olemassaolevatHakukohteet.parallelStream().forEach(hakukohde -> {
            // poista id vanhoilta hakukohteilta, niin etta ne voidaan peristoida uusina dokumentteina
            hakukohde.setId(null);
            kaikkiHakukohteet.put(hakukohde.getOid(), hakukohde);
        });
        talletaTasasijajonosijatJaEdellisenSijoittelunTilat(uudetHakukohteet, kaikkiHakukohteet);
        siirraSivssnov(olemassaolevatHakukohteet, kaikkiHakukohteet);
        kopioiHakemuksenTietoja(olemassaolevatHakukohteet, kaikkiHakukohteet);
        kaikkiHakukohteet.values().forEach(hakukohde -> {
                    HakukohdeItem hki = new HakukohdeItem();
                    hki.setOid(hakukohde.getOid());
                    uusiSijoitteluajo.getHakukohteet().add(hki);
                    hakukohde.setSijoitteluajoId(uusiSijoitteluajo.getSijoitteluajoId());
                }
        );
        return new ArrayList<>(kaikkiHakukohteet.values());
    }

    private void siirraSivssnov(List<Hakukohde> olemassaolevatHakukohteet, Map<String, Hakukohde> kaikkiHakukohteet) {
        olemassaolevatHakukohteet.forEach(h -> {
            h.getValintatapajonot().forEach(olemassaolevaValintatapajono -> {
                kaikkiHakukohteet.get(h.getOid()).getValintatapajonot().forEach(v -> {
                    v.setSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa(olemassaolevaValintatapajono.getSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa());
                });
            });
        });
    }

    private void kopioiHakemuksenTietoja(List<Hakukohde> olemassaolevatHakukohteet, Map<String, Hakukohde> kaikkiHakukohteet) {

        Map<Triple<String, String, String>, Hakemus> hakemusIndex = new HashMap<>();
        olemassaolevatHakukohteet.forEach(hk -> {
            String hakukohdeOid = hk.getOid();
            hk.getValintatapajonot().forEach(j -> {
                String valintatapajonoOid = j.getOid();
                j.getHakemukset().forEach(h -> {
                    String hakemusOid = h.getHakemusOid();
                    Triple id = Triple.of(hakukohdeOid, valintatapajonoOid, hakemusOid);
                    hakemusIndex.put(id, h);
                });
            });
        });

        kaikkiHakukohteet.values().forEach(hk -> {
            String hakukohdeOid = hk.getOid();
            hk.getValintatapajonot().forEach(j -> {
                String valintatapajonoOid = j.getOid();
                j.getHakemukset().forEach(h -> {
                    String hakemusOid = h.getHakemusOid();
                    Triple id = Triple.of(hakukohdeOid, valintatapajonoOid, hakemusOid);
                    if(hakemusIndex.containsKey(id)) {
                        Hakemus alkuperainen = hakemusIndex.get(id);
                        h.setTilaHistoria(alkuperainen.getTilaHistoria());
                        h.setVarasijanNumero(alkuperainen.getVarasijanNumero());
                        h.setIlmoittautumisTila(alkuperainen.getIlmoittautumisTila());
                        //h.setEdellinenTila(alkuperainen.getEdellinenTila());
                    }
                });
            });
        });
    }

    private void talletaTasasijajonosijatJaEdellisenSijoittelunTilat(List<Hakukohde> uudetHakukohteet, Map<String, Hakukohde> kaikkiHakukohteet) {
        uudetHakukohteet.parallelStream().forEach(hakukohde -> {
            Map<String, Integer> tasasijaHashMap = new ConcurrentHashMap<>();
            Map<String, HakemuksenTila> tilaHashMap = new ConcurrentHashMap<>();
            if (kaikkiHakukohteet.containsKey(hakukohde.getOid())) {
                kaikkiHakukohteet.get(hakukohde.getOid()).getValintatapajonot().parallelStream().forEach(valintatapajono ->
                                valintatapajono.getHakemukset().parallelStream().forEach(h -> {
                                    if (h.getTasasijaJonosija() != null) {
                                        tasasijaHashMap.put(valintatapajono.getOid() + h.getHakemusOid(), h.getTasasijaJonosija());
                                    }
                                    if (h.getTila() != null) {
                                        tilaHashMap.put(valintatapajono.getOid() + h.getHakemusOid(), h.getTila());
                                    }
                                })
                );
                hakukohde.getValintatapajonot().forEach(valintatapajono ->
                                valintatapajono.getHakemukset().forEach(hakemus -> {
                                    if (tasasijaHashMap.get(valintatapajono.getOid() + hakemus.getHakemusOid()) != null) {
                                        hakemus.setTasasijaJonosija(tasasijaHashMap.get(valintatapajono.getOid() + hakemus.getHakemusOid()));
                                    }
                                    if (tilaHashMap.get(valintatapajono.getOid() + hakemus.getHakemusOid()) != null) {
                                        hakemus.setEdellinenTila(tilaHashMap.get(valintatapajono.getOid() + hakemus.getHakemusOid()));
                                    }
                                })
                );
            }
            kaikkiHakukohteet.put(hakukohde.getOid(), hakukohde);
        });
    }

    @Deprecated
    private SijoitteluAjo createSijoitteluAjo(Sijoittelu sijoittelu) {
        SijoitteluAjo sijoitteluAjo = createSijoitteluAjo(sijoittelu.getHakuOid());
        sijoittelu.getSijoitteluajot().add(sijoitteluAjo);
        return sijoitteluAjo;
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

    @Deprecated
    private Sijoittelu getOrCreateSijoittelu(String hakuoid) {
        Optional<Sijoittelu> sijoitteluOpt = sijoitteluDao.getSijoitteluByHakuOid(hakuoid);
        if (sijoitteluOpt.isPresent()) {
            return sijoitteluOpt.get();
        } else {
            Sijoittelu sijoittelu = new Sijoittelu();
            sijoittelu.setCreated(new Date());
            sijoittelu.setSijoitteluId(System.currentTimeMillis());
            sijoittelu.setHakuOid(hakuoid);
            return sijoittelu;
        }
    }

    private ValiSijoittelu createValiSijoittelu(String hakuoid) {
        ValiSijoittelu sijoittelu = new ValiSijoittelu();
        sijoittelu.setCreated(new Date());
        sijoittelu.setSijoitteluId(System.currentTimeMillis());
        sijoittelu.setHakuOid(hakuoid);
        return sijoittelu;
    }

    @Deprecated
    private Sijoittelu getOrCreateErillisSijoittelu(String hakuoid) {
        Optional<Sijoittelu> sijoitteluOpt = sijoitteluDao.getSijoitteluByHakuOid(hakuoid);
        if (sijoitteluOpt.isPresent()) {
            return sijoitteluOpt.get();
        } else {
            Sijoittelu sijoittelu = new Sijoittelu();
            sijoittelu.setSijoitteluType(Sijoittelu.SijoitteluType.ERILLISSIJOITTELU_TYPE);
            sijoittelu.setCreated(new Date());
            sijoittelu.setSijoitteluId(System.currentTimeMillis());
            sijoittelu.setHakuOid(hakuoid);
            return sijoittelu;
        }
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

    public void siivoaVanhatAjotSijoittelulta(String hakuOid, Sijoittelu sijoittelu, int ajojaSaastetaan) {
        ActorRef siivoaja = actorService.getSiivousActor();
        siivoaja.tell(new PoistaVanhatAjotSijoittelulta(sijoittelu.getSijoitteluId(), ajojaSaastetaan, hakuOid), ActorRef.noSender());
    }
}
