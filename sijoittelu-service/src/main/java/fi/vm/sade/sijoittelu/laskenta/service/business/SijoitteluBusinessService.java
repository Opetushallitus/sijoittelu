package fi.vm.sade.sijoittelu.laskenta.service.business;

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
import fi.vm.sade.sijoittelu.laskenta.external.resource.VirkailijaValintaTulosServiceResource;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriDTO;
import fi.vm.sade.sijoittelu.laskenta.service.it.TarjontaIntegrationService;
import fi.vm.sade.sijoittelu.laskenta.util.HakuUtil;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.service.impl.converters.SijoitteluTulosConverter;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintatulosservice.valintarekisteri.domain.NotFoundException;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import static fi.vm.sade.sijoittelu.domain.Tasasijasaanto.ARVONTA;
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

    private final SijoitteluTulosConverter sijoitteluTulosConverter;
    private final TarjontaIntegrationService tarjontaIntegrationService;
    private final VirkailijaValintaTulosServiceResource valintaTulosServiceResource;
    private final ValintatulosWithVastaanotto valintatulosWithVastaanotto;
    private final Collection<PostSijoitteluProcessor> postSijoitteluProcessors;
    private final Collection<PreSijoitteluProcessor> preSijoitteluProcessors;
    private final ValintarekisteriService valintarekisteriService;

    @Autowired
    public SijoitteluBusinessService(SijoitteluTulosConverter sijoitteluTulosConverter,
                                     TarjontaIntegrationService tarjontaIntegrationService,
                                     VirkailijaValintaTulosServiceResource valintaTulosServiceResource,
                                     ValintarekisteriService valintarekisteriService) {
        this.sijoitteluTulosConverter = sijoitteluTulosConverter;
        this.tarjontaIntegrationService = tarjontaIntegrationService;
        this.valintaTulosServiceResource = valintaTulosServiceResource;
        this.valintatulosWithVastaanotto = new ValintatulosWithVastaanotto(valintaTulosServiceResource);
        this.preSijoitteluProcessors = PreSijoitteluProcessor.defaultPreProcessors();
        this.postSijoitteluProcessors = PostSijoitteluProcessor.defaultPostProcessors();
        this.valintarekisteriService = valintarekisteriService;
    }

    public void sijoittele(HakuDTO haku, Set<String> eiSijoitteluunMenevatJonot, Set<String> valintaperusteidenValintatapajonot, Long sijoittelunTunniste) {
        //long startTime = System.currentTimeMillis();
        long startTime = sijoittelunTunniste;
        String hakuOid = haku.getHakuOid();
        StopWatch stopWatch = new StopWatch("Haun " + hakuOid + " sijoittelu");
        LOG.info(String.format("Sijoittelu haulle %s alkaa. Luetaan parametrit tarjonnasta esivalidoidaan ne", hakuOid));
        stopWatch.start("Luetaan parametrit tarjonnasta ja esivalidoidaan ne");
        SijoittelunParametrit sijoittelunParametrit = findParametersFromTarjontaAndPerformInitialValidation(hakuOid);
        stopWatch.stop();

        LOG.info("Luetaan sijoittelu valintarekisteristä!", hakuOid);
        stopWatch.start("Luetaan sijoittelu valintarekisteristä");
        SijoitteluAjo viimeisinSijoitteluajo = readSijoitteluajoFromValintarekisteri(haku.getHakuOid());
        stopWatch.stop();

        stopWatch.start("Päätellään hakukohde- ja valintatapajonotiedot");
        List<Hakukohde> uudenSijoitteluajonHakukohteet = haku.getHakukohteet().stream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        List<Hakukohde> edellisenSijoitteluajonTulokset = Collections.emptyList();
        if (viimeisinSijoitteluajo != null) {
            edellisenSijoitteluajonTulokset = valintarekisteriService.getSijoitteluajonHakukohteet(viimeisinSijoitteluajo.getSijoitteluajoId());
            validateSijoittelunJonot(uudenSijoitteluajonHakukohteet, edellisenSijoitteluajonTulokset, eiSijoitteluunMenevatJonot, valintaperusteidenValintatapajonot, stopWatch);
        }
        stopWatch.stop();

        SijoitteluAjo uusiSijoitteluajo = createSijoitteluAjo(hakuOid);
        uusiSijoitteluajo.setSijoitteluajoId(sijoittelunTunniste); //Korvataan sijoitteluajon tunniste (=luontiaika) parametrina saadulla tunnisteella
        stopWatch.start("Mergataan hakukohteet");
        List<Hakukohde> kaikkiHakukohteet = merge(uusiSijoitteluajo, edellisenSijoitteluajonTulokset, uudenSijoitteluajonHakukohteet);
        stopWatch.stop();
        stopWatch.start("Haetaan valintatulokset vastaanottoineen");
        List<Valintatulos> valintatulokset = valintarekisteriService.getValintatulokset(hakuOid);
        stopWatch.stop();
        stopWatch.start("Haetaan kauden aiemmat vastaanotot");
        Map<String, VastaanottoDTO> kaudenAiemmatVastaanotot = aiemmanVastaanotonHakukohdePerHakija(hakuOid);
        stopWatch.stop();
        LOG.info("Haun {} sijoittelun koko: {} olemassaolevaa, {} uutta, {} valintatulosta", hakuOid, edellisenSijoitteluajonTulokset.size(), uudenSijoitteluajonHakukohteet.size(), valintatulokset.size());
        stopWatch.start("Luodaan sijoitteluajoWrapper ja asetetaan parametrit");
        final SijoitteluajoWrapper sijoitteluajoWrapper = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(uusiSijoitteluajo, kaikkiHakukohteet, valintatulokset, kaudenAiemmatVastaanotot);
        asetaSijoittelunParametrit(hakuOid, sijoitteluajoWrapper, sijoittelunParametrit);
        sijoitteluajoWrapper.setEdellisenSijoittelunHakukohteet(edellisenSijoitteluajonTulokset);
        stopWatch.stop();
        suoritaSijoittelu(startTime, stopWatch, hakuOid, uusiSijoitteluajo, sijoitteluajoWrapper);
        stopWatch.start("Kopioidaan edellisen sijoitteluajon tietoja");
        kopioiHakukohteenTiedotVanhaltaSijoitteluajolta(edellisenSijoitteluajonTulokset, kaikkiHakukohteet);
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

    private void validateSijoittelunJonot(List<Hakukohde> uudenSijoitteluajonHakukohteet,
                                          List<Hakukohde> edellisenSijoitteluajonTulokset,
                                          Set<String> eiSijoitteluunMenevatJonot,
                                          Set<String> valintaperusteidenValintatapajonot,
                                          StopWatch stopWatch) {

        Consumer<String> handleError = (msg) -> {
            LOG.error(msg);
            stopWatch.stop();
            LOG.info(stopWatch.prettyPrint());
            throw new RuntimeException(msg);
        };

        Set<String> joSijoitellutJonot = hakukohteidenJonoOidit(edellisenSijoitteluajonTulokset);
        SetView<String> sijoittelustaPoistetutJonot = difference(joSijoitellutJonot, hakukohteidenJonoOidit(uudenSijoitteluajonHakukohteet));
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

    protected SijoittelunParametrit findParametersFromTarjontaAndPerformInitialValidation(String hakuOid) {
        ParametriDTO parametri;
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

        if (kaikkiKohteetSijoittelussa.isPresent() && hakukierrosPaattyy.isBefore(kaikkiKohteetSijoittelussa.get())) {
            throw new IllegalStateException("hakukierros on asetettu päättymään ennen kuin kaikkien kohteiden tulee olla sijoittelussa");
        }
        if (hakukierrosPaattyy.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("hakukierros on päättynyt");
        }
        return new SijoittelunParametrit(parametri);
    }

    protected void asetaSijoittelunParametrit(String hakuOid, SijoitteluajoWrapper sijoitteluAjo, SijoittelunParametrit sijoittelunParametrit) {
        try {
            setOptionalHakuAttributes(hakuOid, sijoitteluAjo);
            setParametersFromTarjonta(sijoitteluAjo, sijoittelunParametrit);
            LOG.info("Sijoittelun ohjausparametrit asetettu haulle {}. onko korkeakouluhaku: {}, kaikki kohteet sijoittelussa: {}, hakukierros päätty: {}, varasijasäännöt astuvat voimaan: {}, varasijasäännöt voimassa: {}, sijoiteltu ilman varasijasääntöjä niiden ollessa voimassa: {}",
                    hakuOid, sijoitteluAjo.isKKHaku(), sijoitteluAjo.getKaikkiKohteetSijoittelussa(), sijoitteluAjo.getHakuKierrosPaattyy(), sijoitteluAjo.getVarasijaSaannotAstuvatVoimaan(), sijoitteluAjo.varasijaSaannotVoimassa(), sijoitteluAjo.onkoKaikkiJonotSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa());
        } catch (IllegalStateException e) {
            throw new RuntimeException("Sijoittelua haulle " + hakuOid + " ei voida suorittaa", e);
        }
    }

    private void setParametersFromTarjonta(SijoitteluajoWrapper sijoitteluAjo, SijoittelunParametrit sijoittelunParametrit) {
        if (sijoitteluAjo.isKKHaku()) {
            if (!sijoittelunParametrit.kaikkiKohteetSijoittelussa.isPresent()) {
                throw new IllegalStateException("kyseessä korkeakouluhaku ja ohjausparametria PH_VTSSV (kaikki kohteet sijoittelussa) ei ole asetettu");
            }
            if (!sijoittelunParametrit.varasijasaannotAstuvatVoimaan.isPresent()) {
                throw new IllegalStateException("kyseessä korkeakouluhaku ja ohjausparametria PH_VSSAV (varasijasäännöt astuvat voimaan) ei ole asetettu");
            }
            if (sijoittelunParametrit.hakukierrosPaattyy.isBefore(sijoittelunParametrit.varasijasaannotAstuvatVoimaan.get())) {
                throw new IllegalStateException("hakukierros on asetettu päättymään ennen kuin varasija säännöt astuvat voimaan");
            }
        }

        sijoitteluAjo.setHakuKierrosPaattyy(sijoittelunParametrit.hakukierrosPaattyy);
        sijoittelunParametrit.kaikkiKohteetSijoittelussa.ifPresent(sijoitteluAjo::setKaikkiKohteetSijoittelussa);
        sijoittelunParametrit.varasijasaannotAstuvatVoimaan.ifPresent(sijoitteluAjo::setVarasijaSaannotAstuvatVoimaan);
        sijoittelunParametrit.varasijatayttoPaattyy.ifPresent(sijoitteluAjo::setVarasijaTayttoPaattyy);
    }

    private void setOptionalHakuAttributes(String hakuOid, SijoitteluajoWrapper sijoitteluAjo) {
        fi.vm.sade.sijoittelu.laskenta.external.resource.dto.HakuDTO hakuDto;
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
        List<Hakukohde> olemassaolevatHakukohteet = Collections.emptyList();
        SijoitteluAjo uusiSijoitteluajo = createValiSijoitteluAjo(sijoittelu);
        List<Hakukohde> kaikkiHakukohteet = merge(uusiSijoitteluajo, olemassaolevatHakukohteet, uudetHakukohteet);
        SijoitteluajoWrapper sijoitteluajoWrapper = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(uusiSijoitteluajo, kaikkiHakukohteet, Collections.emptyList(), Collections.emptyMap());
        stopWatch.stop();

        suoritaSijoittelu(startTime, stopWatch, hakuOid, uusiSijoitteluajo, sijoitteluajoWrapper);

        List<HakukohdeDTO> result = kaikkiHakukohteet.parallelStream().map(h -> sijoitteluTulosConverter.convert(h)).collect(Collectors.toList());
        LOG.info(stopWatch.prettyPrint());
        return result;
    }

    private Hakukohde getErillissijoittelunHakukohde(HakuDTO haku) {
        if( 1 == haku.getHakukohteet().size() ) {
            return DomainConverter.convertToHakukohde(haku.getHakukohteet().get(0));
        }
        LOG.error("Haun {} erillissijoitteluun saatiin {} kpl hakukohteita. Voi olla vain yksi!", haku.getHakuOid(), haku.getHakukohteet().size());
        throw new IllegalStateException("Haun " + haku.getHakuOid() + " erillissijoitteluun saatiin " + haku.getHakukohteet().size() + " kpl hakukohteita. Voi olla vain yksi!");
    }

    public long erillissijoittele(HakuDTO haku) {
        String hakuOid = haku.getHakuOid();
        String hakukohdeOid = haku.getHakukohteet().size() > 0 ? haku.getHakukohteet().get(0).getOid() : "";
        long startTime = System.currentTimeMillis();

        StopWatch stopWatch = new StopWatch("Haun " + hakuOid + " hakukohteen " + hakukohdeOid + " erillissijoittelu");

        LOG.info(String.format("Erillissijoittelu haun %s hakukohteelle %s alkaa. Luetaan parametrit tarjonnasta ja esivalidoidaan ne", hakuOid, hakukohdeOid));
        stopWatch.start("Luetaan parametrit tarjonnasta ja esivalidoidaan ne");
        SijoittelunParametrit sijoittelunParametrit = findParametersFromTarjontaAndPerformInitialValidation(hakuOid);
        stopWatch.stop();

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
        asetaSijoittelunParametrit(hakuOid, sijoitteluajoWrapper, sijoittelunParametrit);
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

        stopWatch.start("Tallennetaan sijoitteluajo, hakukohteet ja valintatulokset Valintarekisteriin");
        tallennaSijoitteluToValintarekisteri(hakuOid, uusiSijoitteluajo, kaikkiTallennettavatHakukohteet, sijoitteluajoWrapper.getMuuttuneetValintatulokset(), stopWatch);
        stopWatch.stop();
        LOG.info(stopWatch.prettyPrint());
        return uusiSijoitteluajo.getSijoitteluajoId();
    }

    private void kopioiHakukohteenTiedotVanhaltaSijoitteluajolta(final List<Hakukohde> edellisenSijoitteluajonHakukohteet, final List<Hakukohde> tamanSijoitteluajonHakukohteet) {
        Map<String, Hakemus> hakemukset = getStringHakemusMap(edellisenSijoitteluajonHakukohteet);
        Map<String, Valintatapajono> valintatapajonot = getStringValintatapajonoMap(edellisenSijoitteluajonHakukohteet);
        tamanSijoitteluajonHakukohteet.parallelStream().forEach(hakukohde ->
            hakukohde.getValintatapajonot().forEach(valintatapajono -> {
                kopioiValintatapajononTiedotVanhaltaSijoitteluajolta(valintatapajono, valintatapajonot.get(valintatapajono.getOid()));
                kopioiHakemustenTiedotVanhaltaSijoitteluajoltaJaAsetaVarasijaNumerot(hakukohde.getOid(), valintatapajono, hakemukset);
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

    private void kopioiHakemustenTiedotVanhaltaSijoitteluajoltaJaAsetaVarasijaNumerot(String hakukohdeOid, Valintatapajono valintatapajono, Map<String, Hakemus> edellisenSijoitteluajonHakemukset) {
        Collections.sort(valintatapajono.getHakemukset(), hakemusComparator);
        int varasija = 0;
        Optional<Hakemus> edellinenVarallaolevaHakemus = Optional.empty();
        for (Hakemus hakemus : valintatapajono.getHakemukset()) {
            if (hakemus.getTila() == HakemuksenTila.VARALLA) {
                varasija++;
                setVarasijaNumero(varasija, hakemus, edellinenVarallaolevaHakemus, valintatapajono.getTasasijasaanto());
                edellinenVarallaolevaHakemus = Optional.of(hakemus);
            } else {
                hakemus.setVarasijanNumero(null);
            }
            Hakemus hakemuksenTilaEdellisestaAjosta = edellisenSijoitteluajonHakemukset.get(hakukohdeOid + valintatapajono.getOid() + hakemus.getHakemusOid());
            if (hakemuksenTilaEdellisestaAjosta != null && TilaTaulukot.kuuluuHyvaksyttyihinTiloihin(hakemus.getTila())) {
                if (hakemus.getSiirtynytToisestaValintatapajonosta() == false) {
                    hakemus.setSiirtynytToisestaValintatapajonosta(hakemuksenTilaEdellisestaAjosta.getSiirtynytToisestaValintatapajonosta());
                }
            }
        }
    }

    private void setVarasijaNumero(int seuraavaVarasijaNumero, Hakemus hakemus, Optional<Hakemus> jononEdellinenVarallaOlevaHakemus, Tasasijasaanto tasasijasaanto) {
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
        talletaTasasijaJonosijatJaEdellisetTilatJaTilanKuvaukset(uudetHakukohteet, kaikkiHakukohteet);
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
                    Triple<String, String, String> id = Triple.of(hakukohdeOid, valintatapajonoOid, hakemusOid);
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

    private void talletaTasasijaJonosijatJaEdellisetTilatJaTilanKuvaukset(List<Hakukohde> uudetHakukohteet, Map<String, Hakukohde> kaikkiHakukohteet) {
        uudetHakukohteet.parallelStream().forEach(hakukohde -> {
            Map<String, Integer> tasasijaHashMap = new ConcurrentHashMap<>();
            Map<String, HakemuksenTila> tilaHashMap = new ConcurrentHashMap<>();
            Map<String, Map<String, String>> tilankuvauksetHashMap = new ConcurrentHashMap<>();
            if (kaikkiHakukohteet.containsKey(hakukohde.getOid())) {
                kaikkiHakukohteet.get(hakukohde.getOid()).getValintatapajonot().parallelStream().forEach(valintatapajono ->
                                valintatapajono.getHakemukset().parallelStream().forEach(h -> {
                                    if (h.getTasasijaJonosija() != null) {
                                        tasasijaHashMap.put(valintatapajono.getOid() + h.getHakemusOid(), h.getTasasijaJonosija());
                                    }
                                    if (h.getTila() != null) {
                                        tilaHashMap.put(valintatapajono.getOid() + h.getHakemusOid(), h.getTila());
                                    }
                                    if (h.getTilanKuvaukset() != null && !h.getTilanKuvaukset().isEmpty()) {
                                        tilankuvauksetHashMap.put(valintatapajono.getOid() + h.getHakemusOid(), h.getTilanKuvaukset());
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
                                    if (tilankuvauksetHashMap.get(valintatapajono.getOid() + hakemus.getHakemusOid()) != null) {
                                        hakemus.setTilanKuvaukset(tilankuvauksetHashMap.get(valintatapajono.getOid() + hakemus.getHakemusOid()));
                                    }
                                })
                );
            }
            kaikkiHakukohteet.put(hakukohde.getOid(), hakukohde);
        });
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

    protected class SijoittelunParametrit {
        private final LocalDateTime hakukierrosPaattyy;
        private final Optional<LocalDateTime> kaikkiKohteetSijoittelussa;
        private final Optional<LocalDateTime> varasijasaannotAstuvatVoimaan;
        private final Optional<LocalDateTime> varasijatayttoPaattyy;

        public SijoittelunParametrit(ParametriDTO tarjontaParametriDto) {
            hakukierrosPaattyy = Optional.ofNullable(tarjontaParametriDto)
                .flatMap(p -> Optional.ofNullable(p.getPH_HKP()))
                .flatMap(p -> Optional.ofNullable(p.getDate()))
                .map(SijoitteluBusinessService.this::fromTimestamp)
                .orElseThrow(() -> new IllegalStateException("ohjausparametria PH_HKP (hakukierros päättyy) parametria ei ole asetettu"));

            kaikkiKohteetSijoittelussa = Optional.ofNullable(tarjontaParametriDto)
                .flatMap(p -> Optional.ofNullable(p.getPH_VTSSV()))
                .flatMap(p -> Optional.ofNullable(p.getDate()))
                .map(SijoitteluBusinessService.this::fromTimestamp);
            varasijasaannotAstuvatVoimaan = Optional.ofNullable(tarjontaParametriDto)
                .flatMap(p -> Optional.ofNullable(p.getPH_VSSAV()))
                .flatMap(p -> Optional.ofNullable(p.getDate()))
                .map(SijoitteluBusinessService.this::fromTimestamp);
            varasijatayttoPaattyy = Optional.ofNullable(tarjontaParametriDto)
                .flatMap(p -> Optional.ofNullable(p.getPH_VSTP()))
                .flatMap(p -> Optional.ofNullable(p.getDate()))
                .map(SijoitteluBusinessService.this::fromTimestamp);
        }
    }
}
