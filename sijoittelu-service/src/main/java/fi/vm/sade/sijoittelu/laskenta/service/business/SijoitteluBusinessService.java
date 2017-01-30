package fi.vm.sade.sijoittelu.laskenta.service.business;

import akka.actor.ActorRef;
import com.google.common.collect.Sets.SetView;
import fi.vm.sade.auditlog.valintaperusteet.ValintaperusteetOperation;
import fi.vm.sade.authentication.business.service.Authorizer;
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
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriArvoDTO;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriDTO;
import fi.vm.sade.sijoittelu.laskenta.service.exception.HakemustaEiLoytynytException;
import fi.vm.sade.sijoittelu.laskenta.service.exception.ValintatapajonoaEiLoytynytException;
import fi.vm.sade.sijoittelu.laskenta.service.it.TarjontaIntegrationService;
import fi.vm.sade.sijoittelu.tulos.dao.HakukohdeDao;
import fi.vm.sade.sijoittelu.tulos.dao.SijoitteluDao;
import fi.vm.sade.sijoittelu.tulos.dao.ValiSijoitteluDao;
import fi.vm.sade.sijoittelu.tulos.dao.ValintatulosDao;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.roles.SijoitteluRole;
import fi.vm.sade.sijoittelu.tulos.service.RaportointiService;
import fi.vm.sade.sijoittelu.tulos.service.impl.converters.SijoitteluTulosConverter;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
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
import static fi.vm.sade.auditlog.valintaperusteet.LogMessage.builder;
import static fi.vm.sade.sijoittelu.laskenta.util.SijoitteluAudit.AUDIT;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.join;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;

@Service
public class SijoitteluBusinessService {
    private static final Logger LOG = LoggerFactory.getLogger(SijoitteluBusinessService.class);
    public static final String OPH_OID = "1.2.246.562.10.00000000001";
    private HakemusComparator hakemusComparator = new HakemusComparator();
    private final String KK_KOHDEJOUKKO = "haunkohdejoukko_12";


    private final int maxAjoMaara;
    private final int maxErillisAjoMaara;
    private final ValintatulosDao valintatulosDao;
    private final HakukohdeDao hakukohdeDao;
    private final SijoitteluDao sijoitteluDao;
    private final RaportointiService raportointiService;
    private final ValiSijoitteluDao valisijoitteluDao;
    private final Authorizer authorizer;
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

    @Autowired
    public SijoitteluBusinessService(@Value("${sijoittelu.maxAjojenMaara:20}") int maxAjoMaara,
                                     @Value("${sijoittelu.maxErillisAjojenMaara:300}") int maxErillisAjoMaara,
                                     ValintatulosDao valintatulosDao,
                                     HakukohdeDao hakukohdeDao,
                                     SijoitteluDao sijoitteluDao,
                                     RaportointiService raportointiService,
                                     ValiSijoitteluDao valisijoitteluDao,
                                     Authorizer authorizer,
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
        this.raportointiService = raportointiService;
        this.valisijoitteluDao = valisijoitteluDao;
        this.authorizer = authorizer;
        this.sijoitteluTulosConverter = sijoitteluTulosConverter;
        this.actorService = actorService;
        this.tarjontaIntegrationService = tarjontaIntegrationService;
        this.valintaTulosServiceResource = valintaTulosServiceResource;
        this.valintatulosWithVastaanotto = new ValintatulosWithVastaanotto(valintatulosDao, valintaTulosServiceResource);
        this.preSijoitteluProcessors = PreSijoitteluProcessor.defaultPreProcessors();
        this.postSijoitteluProcessors = PostSijoitteluProcessor.defaultPostProcessors();
        this.valintarekisteriService = valintarekisteriService;
    }

    private static Set<String> hakukohteidenJonoOidit(List<Hakukohde> hakukohteet) {
        return unmodifiableSet(hakukohteet.stream()
                .flatMap(hakukohde -> hakukohde.getValintatapajonot().stream().map(Valintatapajono::getOid))
                .collect(toSet()));
    }

    public void sijoittele(HakuDTO sijoitteluTyyppi, Set<String> eiSijoitteluunMenevatJonot, Set<String> valintaperusteidenValintatapajonot) {
        long startTime = System.currentTimeMillis();
        StopWatch stopWatch = new StopWatch("Haun " + sijoitteluTyyppi.getHakuOid() + " sijoittelu");
        String hakuOid = sijoitteluTyyppi.getHakuOid();
        LOG.info("Sijoittelu haulle {} alkaa.", hakuOid);
        stopWatch.start("Päätellään viimeisin sijoitteluajo");
        Sijoittelu sijoittelu = getOrCreateSijoittelu(hakuOid);
        SijoitteluAjo viimeisinSijoitteluajo = sijoittelu.getLatestSijoitteluajo();
        stopWatch.stop();
        stopWatch.start("Päätellään hakukohde- ja valintatapajonotiedot");
        List<Hakukohde> uudetHakukohteet = sijoitteluTyyppi.getHakukohteet().stream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        List<Hakukohde> olemassaolevatHakukohteet = Collections.emptyList();
        if (viimeisinSijoitteluajo != null) {
            olemassaolevatHakukohteet = hakukohdeDao.getHakukohdeForSijoitteluajo(viimeisinSijoitteluajo.getSijoitteluajoId());
            Set<String> joSijoitellutJonot = hakukohteidenJonoOidit(olemassaolevatHakukohteet);
            SetView<String> sijoittelustaPoistetutJonot = difference(joSijoitellutJonot, hakukohteidenJonoOidit(uudetHakukohteet));
            SetView<String> aktiivisetSijoittelustaPoistetutJonot = intersection(eiSijoitteluunMenevatJonot, sijoittelustaPoistetutJonot);
            if (aktiivisetSijoittelustaPoistetutJonot.size() > 0) {
                String msg = "Edellisessä sijoittelussa olleet jonot [" + join(aktiivisetSijoittelustaPoistetutJonot, ", ") +
                        "] puuttuvat sijoittelusta, vaikka ne ovat valintaperusteissa yhä aktiivisina";
                LOG.error(msg);
                stopWatch.stop();
                LOG.info(stopWatch.prettyPrint());
                throw new RuntimeException(msg);
            }
            SetView<String> valintaperusteistaPuuttuvatSijoitellutJonot = difference(joSijoitellutJonot, valintaperusteidenValintatapajonot);
            if(valintaperusteistaPuuttuvatSijoitellutJonot.size() > 0) {
                String msg = "Edellisessä sijoittelussa olleet jonot [" + join(valintaperusteistaPuuttuvatSijoitellutJonot, ", ") +
                        "] ovat kadonneet valintaperusteista";
                LOG.error(msg);
                stopWatch.stop();
                LOG.info(stopWatch.prettyPrint());
                throw new RuntimeException(msg);
            }
        }
        stopWatch.stop();
        SijoitteluAjo uusiSijoitteluajo = createSijoitteluAjo(sijoittelu);
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
        List<Valintatulos> muuttuneetValintatulokset = sijoitteluajoWrapper.getMuuttuneetValintatulokset();
        LOG.info("Ennen mergeä muuttuneita valintatuloksia: " + muuttuneetValintatulokset.size());
        LOG.info("Ennen mergeä muuttuneet valintatulokset: " + muuttuneetValintatulokset);
        stopWatch.start("Mergataan valintatulokset");
        List<Valintatulos> mergatut = valintatulosDao.mergaaValintatulos(kaikkiHakukohteet, muuttuneetValintatulokset);
        stopWatch.stop();
        stopWatch.start("Persistoidaan valintatulokset");
        valintatulosWithVastaanotto.persistValintatulokset(mergatut);

        stopWatch.stop();
        sijoitteluajoWrapper.setMuuttuneetValintatulokset(mergatut);
        List<String> varasijapomput = sijoitteluajoWrapper.getVarasijapomput();
        varasijapomput.forEach(LOG::info);
        LOG.info("Haun {} sijoittelussa muuttui {} kpl valintatuloksia, pomppuja {} kpl", hakuOid, mergatut.size(), varasijapomput.size());
        persistSijoitteluAndSiivoaVanhatAjot(stopWatch, hakuOid, sijoittelu, uusiSijoitteluajo, kaikkiHakukohteet, mergatut, maxAjoMaara);
        LOG.info(stopWatch.prettyPrint());
    }

    private void poistaValintatapajonokohtaisetHakijaryhmatJoidenJonoaEiSijoiteltu(List<Hakukohde> hakukohteet) {
        hakukohteet.forEach(h -> {
            Set<String> sijoitellutJonot = h.getValintatapajonot().stream()
                    .map(Valintatapajono::getOid)
                    .collect(Collectors.toSet());
            h.setHakijaryhmat(h.getHakijaryhmat().stream()
                    .filter(ryhma -> ryhma.getValintatapajonoOid() == null || sijoitellutJonot.contains(ryhma.getValintatapajonoOid()))
                    .collect(Collectors.toList()));
        });
    }

    private void suoritaSijoittelu(long startTime, StopWatch stopWatch, String hakuOid, SijoitteluAjo uusiSijoitteluajo, SijoitteluajoWrapper sijoitteluajoWrapper) {
        LOG.info("Suoritetaan sijoittelu haulle {}", hakuOid);
        stopWatch.start("Suoritetaan sijoittelu");
        uusiSijoitteluajo.setStartMils(startTime);
        SijoitteluAlgorithm.sijoittele(preSijoitteluProcessors, postSijoitteluProcessors, sijoitteluajoWrapper);
        uusiSijoitteluajo.setEndMils(System.currentTimeMillis());
        stopWatch.stop();
    }

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
                poistaValintatapajonokohtaisetHakijaryhmatJoidenJonoaEiSijoiteltu(hakukohteet);
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

    private void asetaSijoittelunParametrit(String hakuOid, SijoitteluajoWrapper sijoitteluAjo) {
        setOptionalKorkeakouluHakuStatus(hakuOid, sijoitteluAjo);
        setParametersFromTarjonta(hakuOid, sijoitteluAjo);
        if (sijoitteluAjo.getHakuKierrosPaattyy().isBefore(sijoitteluAjo.getKaikkiKohteetSijoittelussa())) {
            throw new RuntimeException("Sijoittelua haulle " + hakuOid + " ei voida suorittaa, koska hakukierros on asetettu päättymään ennen kuin kaikkien kohteiden tulee olla sijoittelussa.");
        }
        if (sijoitteluAjo.isKKHaku() && sijoitteluAjo.getHakuKierrosPaattyy().isBefore(sijoitteluAjo.getVarasijaSaannotAstuvatVoimaan())) {
            throw new RuntimeException("Sijoittelua haulle " + hakuOid + " ei voida suorittaa, koska hakukierros on asetettu päättymään ennen kuin varasija säännöt astuvat voimaan");
        }
        LOG.info("Sijoittelun ohjausparametrit asetettu haulle {}. onko korkeakouluhaku: {}, kaikki kohteet sijoittelussa: {}, hakukierros päätty: {}, varasijasäännöt astuvat voimaan: {}, varasijasäännöt voimassa: {}",
                hakuOid, sijoitteluAjo.isKKHaku(), sijoitteluAjo.getKaikkiKohteetSijoittelussa(), sijoitteluAjo.getHakuKierrosPaattyy(), sijoitteluAjo.getVarasijaSaannotAstuvatVoimaan(), sijoitteluAjo.varasijaSaannotVoimassa());
    }



    private void setParametersFromTarjonta(String hakuOid, SijoitteluajoWrapper sijoitteluAjo) {
        try {
            ParametriDTO parametri = tarjontaIntegrationService.getHaunParametrit(hakuOid);
            if (parametri == null || parametri.getPH_HKP() == null || parametri.getPH_HKP().getDate() == null) {
                throw new RuntimeException("Sijoittelua haulle " + hakuOid + " ei voida suorittaa, koska hakukierroksen päättymispäivä parametria ei saatu.");
            } else {
                sijoitteluAjo.setHakuKierrosPaattyy(fromTimestamp(parametri.getPH_HKP().getDate()));
            }
            if (sijoitteluAjo.isKKHaku()
                    && (parametri.getPH_VTSSV() == null
                    || parametri.getPH_VTSSV().getDate() == null
                    || parametri.getPH_VSSAV() == null
                    || parametri.getPH_VSSAV().getDate() == null)) {
                throw new RuntimeException("Sijoittelua haulle " + hakuOid + " ei voida suorittaa, koska kyseessä on korkeakouluhaku ja vaadittavia ohjausparametrejä (PH_VTSSV, PH_VSSAV) ei saatu haettua tai asetettua.");
            } else if (sijoitteluAjo.isKKHaku()) {
                LOG.info("Saadut ohjausparametrit haulle {}: kaikkikohteet sijoittelussa-> {}, varasijasäännöt astuvat voimaan-> {}", hakuOid, fromTimestamp(parametri.getPH_VTSSV().getDate()), fromTimestamp(parametri.getPH_VSSAV().getDate()));
            }
            if (parametri.getPH_VTSSV() != null && parametri.getPH_VTSSV().getDate() != null) {
                sijoitteluAjo.setKaikkiKohteetSijoittelussa(fromTimestamp(parametri.getPH_VTSSV().getDate()));
            }
            if (parametri.getPH_VSSAV() != null && parametri.getPH_VSSAV().getDate() != null) {
                sijoitteluAjo.setVarasijaSaannotAstuvatVoimaan(fromTimestamp(parametri.getPH_VSSAV().getDate()));
            }
            if (parametri.getPH_VSTP() != null && parametri.getPH_VSTP().getDate() != null) {
                sijoitteluAjo.setVarasijaTayttoPaattyy(fromTimestamp(parametri.getPH_VSTP().getDate()));
            }
        } catch (Exception e) {
            LOG.error("############## Ohjausparametrin muuntaminen LocalDateksi epäonnistui ##############", e);
            if (sijoitteluAjo.isKKHaku()) {
                throw new RuntimeException("Sijoittelua haulle " + hakuOid + " ei voida suorittaa, koska kyseessä on korkeakouluhaku ja vaadittavia ohjausparametrejä (PH_VTSSV, PH_VSSAV) ei saatu haettua tai asetettua.");
            } else {
                throw new RuntimeException("Sijoittelua haulle " + hakuOid + " ei voida suorittaa, koska hakukierroksen päättymispäivää ei saatu haettua tai asetettua.");
            }
        }
    }

    private void setOptionalKorkeakouluHakuStatus(String hakuOid, SijoitteluajoWrapper sijoitteluAjo) {
        try {
            Optional<String> kohdejoukko = tarjontaIntegrationService.getHaunKohdejoukko(hakuOid);
            if (kohdejoukko.isPresent()) {
                if (kohdejoukko.get().equals(KK_KOHDEJOUKKO)) {
                    sijoitteluAjo.setKKHaku(true);
                }
            } else {
                throw new RuntimeException("Sijoittelua haulle " + hakuOid + " ei voida suorittaa, koska tarjonnasta ei saatu haun tietoja");
            }
        } catch (Exception e) {
            LOG.error("############## Haun hakeminen tarjonnasta epäonnistui ##############", e);
            throw new RuntimeException("Sijoittelua haulle " + hakuOid + " ei voida suorittaa, koska tarjonnasta ei saatu haun tietoja");
        }
    }

    private LocalDateTime fromTimestamp(Long timestamp) {
        return LocalDateTime.ofInstant(new Date(timestamp).toInstant(), ZoneId.systemDefault());
    }

    public List<HakukohdeDTO> valisijoittele(HakuDTO sijoitteluTyyppi) {
        long startTime = System.currentTimeMillis();
        String hakuOid = sijoitteluTyyppi.getHakuOid();
        StopWatch stopWatch = new StopWatch("Haun " + hakuOid + " välisijoittelu");

        stopWatch.start("Alustetaan uusi välisijoittelu ja haetaan hakukohteet");
        ValiSijoittelu sijoittelu = getOrCreateValiSijoittelu(hakuOid);
        List<Hakukohde> uudetHakukohteet = sijoitteluTyyppi.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        List<Hakukohde> olemassaolevatHakukohteet = Collections.<Hakukohde>emptyList();
        SijoitteluAjo uusiSijoitteluajo = createValiSijoitteluAjo(sijoittelu);
        List<Hakukohde> kaikkiHakukohteet = merge(uusiSijoitteluajo, olemassaolevatHakukohteet, uudetHakukohteet);
        SijoitteluajoWrapper sijoitteluajoWrapper = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(uusiSijoitteluajo, kaikkiHakukohteet, Collections.emptyList(), Collections.emptyMap());
        stopWatch.stop();

        suoritaSijoittelu(startTime, stopWatch, hakuOid, uusiSijoitteluajo, sijoitteluajoWrapper);
        processOldApplications(stopWatch, olemassaolevatHakukohteet, kaikkiHakukohteet, hakuOid);

        stopWatch.start("Persistoidaan välisijoittelu");
        valisijoitteluDao.persistSijoittelu(sijoittelu);
        stopWatch.stop();

        List<HakukohdeDTO> result = kaikkiHakukohteet.parallelStream().map(h -> sijoitteluTulosConverter.convert(h)).collect(Collectors.toList());
        LOG.info(stopWatch.prettyPrint());
        return result;
    }

    private static class HakukohdeOidAndSijoitteluAjoId {
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

    private Map<String,Long> hakukohdeToLastSijoitteluAjoId(final Sijoittelu sijoittelu) {
        return sijoittelu.getSijoitteluajot().stream()
                .flatMap(s -> s.getHakukohteet().stream().map(h -> new HakukohdeOidAndSijoitteluAjoId(h.getOid(),s.getSijoitteluajoId())))
                .collect(Collectors.toMap(
                        HakukohdeOidAndSijoitteluAjoId::getHakukohdeOid,
                        s -> s.getSijoitteluAjoId(),
                        (sijoitteluAjoId1, sijoitteluAjoId2) -> Math.max(sijoitteluAjoId1, sijoitteluAjoId2)
                ));
    }

    public long erillissijoittele(HakuDTO sijoitteluTyyppi) {
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
        SijoitteluAjo uusiSijoitteluajo = createErillisSijoitteluAjo(sijoittelu);
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

    private Consumer<Valintatapajono> processValintatapaJono(Map<String, Valintatapajono> valintatapajonoHashMap, Map<String, Hakemus> hakemusHashMap, Hakukohde hakukohde) {
        return valintatapajono -> {
                    valintatapajono.setAlinHyvaksyttyPistemaara(alinHyvaksyttyPistemaara(valintatapajono.getHakemukset()).orElse(null));
                    valintatapajono.setHyvaksytty(getMaara(valintatapajono.getHakemukset(), Arrays.asList(HakemuksenTila.HYVAKSYTTY, HakemuksenTila.VARASIJALTA_HYVAKSYTTY)));
                    valintatapajono.setVaralla(getMaara(valintatapajono.getHakemukset(), Arrays.asList(HakemuksenTila.VARALLA)));

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

    private SijoitteluAjo createSijoitteluAjo(Sijoittelu sijoittelu) {
        SijoitteluAjo sijoitteluAjo = new SijoitteluAjo();
        Long now = System.currentTimeMillis();
        sijoitteluAjo.setSijoitteluajoId(now);
        sijoitteluAjo.setHakuOid(sijoittelu.getHakuOid());
        sijoittelu.getSijoitteluajot().add(sijoitteluAjo);
        return sijoitteluAjo;
    }

    private SijoitteluAjo createValiSijoitteluAjo(ValiSijoittelu sijoittelu) {
        SijoitteluAjo sijoitteluAjo = new SijoitteluAjo();
        Long now = System.currentTimeMillis();
        sijoitteluAjo.setSijoitteluajoId(now);
        sijoitteluAjo.setHakuOid(sijoittelu.getHakuOid());
        sijoittelu.getSijoitteluajot().add(sijoitteluAjo);
        return sijoitteluAjo;
    }

    private SijoitteluAjo createErillisSijoitteluAjo(Sijoittelu sijoittelu) {
        SijoitteluAjo sijoitteluAjo = new SijoitteluAjo();
        Long now = System.currentTimeMillis();
        sijoitteluAjo.setSijoitteluajoId(now);
        sijoitteluAjo.setHakuOid(sijoittelu.getHakuOid());
        sijoittelu.getSijoitteluajot().add(sijoitteluAjo);
        return sijoitteluAjo;
    }

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

    private ValiSijoittelu getOrCreateValiSijoittelu(String hakuoid) {
        Optional<ValiSijoittelu> sijoitteluOpt = valisijoitteluDao.getSijoitteluByHakuOid(hakuoid);
        if (sijoitteluOpt.isPresent()) {
            return sijoitteluOpt.get();
        } else {
            ValiSijoittelu sijoittelu = new ValiSijoittelu();
            sijoittelu.setCreated(new Date());
            sijoittelu.setSijoitteluId(System.currentTimeMillis());
            sijoittelu.setHakuOid(hakuoid);
            return sijoittelu;
        }
    }

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

    public Valintatulos haeHakemuksenTila(String hakuoid, String hakukohdeOid, String valintatapajonoOid, String hakemusOid) {
        if (isBlank(hakukohdeOid) || isBlank(hakemusOid)) {
            throw new RuntimeException("Invalid search params, fix exception later");
        }
        return valintatulosDao.loadValintatulos(hakukohdeOid, valintatapajonoOid, hakemusOid);
    }

    public List<Valintatulos> haeHakemustenTilat(String hakukohdeOid, String valintatapajonoOid) {
        if (isBlank(hakukohdeOid)) {
            throw new RuntimeException("Invalid search params, fix exception later");
        }
        return valintatulosDao.loadValintatulokset(hakukohdeOid, valintatapajonoOid);
    }

    public List<Valintatulos> haeHakukohteenTilat(String hakukohdeOid) {
        if (isBlank(hakukohdeOid)) {
            throw new RuntimeException("Invalid search params, fix exception later");
        }
        return valintatulosDao.loadValintatuloksetForHakukohde(hakukohdeOid);
    }

    public List<Valintatulos> haeHakemuksenTila(String hakemusOid) {
        if (isBlank(hakemusOid)) {
            throw new RuntimeException("Invalid search params, fix exception later");
        }
        return valintatulosDao.loadValintatulos(hakemusOid);
    }

    public Hakukohde getHakukohde(String hakuOid, String hakukohdeOid) {
        return raportointiService.cachedLatestSijoitteluAjoForHakukohde(hakuOid, hakukohdeOid)
                .map(sijoitteluAjo -> hakukohdeDao.getHakukohdeForSijoitteluajo(sijoitteluAjo.getSijoitteluajoId(), hakukohdeOid))
                .orElseThrow(() -> new RuntimeException("Sijoittelua ei löytynyt haulle: " + hakuOid));
    }

    public Map<String, VastaanottoDTO> aiemmanVastaanotonHakukohdePerHakija(String hakuOid) {
        return valintaTulosServiceResource.haunKoulutuksenAlkamiskaudenVastaanototYhdenPaikanSaadoksenPiirissa(hakuOid)
                .stream().collect(Collectors.toMap(VastaanottoDTO::getHenkiloOid, Function.identity()));
    }

    public void asetaJononValintaesitysHyvaksytyksi(Hakukohde hakukohde, String valintatapajonoOid, boolean hyvaksytty, String hakuOid) {
        LOG.info("Asetetaan hakukohteen {} valintatapajonon {} valintaesitys hyväksytyksi: {}", hakukohde.getOid(), valintatapajonoOid, hyvaksytty);
        Valintatapajono valintatapajono = getValintatapajono(valintatapajonoOid, hakukohde);
        valintatapajono.setValintaesitysHyvaksytty(hyvaksytty);
        hakukohdeDao.persistHakukohde(hakukohde, hakuOid);
    }

    public void vaihdaHakemuksenTila(String hakuoid, Hakukohde hakukohde, Valintatulos change, String selite, String muokkaaja) {
        String valintatapajonoOid = change.getValintatapajonoOid();
        String hakemusOid = change.getHakemusOid();
        if (isBlank(hakuoid) || isBlank(valintatapajonoOid) || isBlank(hakemusOid)) {
            throw new IllegalArgumentException(String.format("hakuoid: %s, valintatapajonoOid: %s, hakemusOid: %s", hakuoid, valintatapajonoOid, hakemusOid));
        }
        String tarjoajaOid = hakukohde.getTarjoajaOid();
        if (isBlank(tarjoajaOid)) {
            updateMissingTarjoajaOidFromTarjonta(hakukohde, hakuoid);
        }
        // Oph-admin voi muokata aina
        // organisaatio updater voi muokata, jos hyväksytty
        authorizer.checkOrganisationAccess(tarjoajaOid, SijoitteluRole.UPDATE_ROLE, SijoitteluRole.CRUD_ROLE);

        Valintatapajono valintatapajono = getValintatapajono(valintatapajonoOid, hakukohde);
        Hakemus hakemus = getHakemus(hakemusOid, valintatapajono);
        String hakukohdeOid = hakukohde.getOid();
        Valintatulos v = Optional.ofNullable(valintatulosDao.loadValintatulos(hakukohdeOid, valintatapajonoOid, hakemusOid))
                .orElse(new Valintatulos(
                        valintatapajono.getOid(),
                        hakemus.getHakemusOid(),
                        hakukohde.getOid(),
                        hakemus.getHakijaOid(),
                        hakuoid,
                        hakemus.getPrioriteetti()));
        if (notModifying(change, v)) {
            return;
        }
        if (v.getViimeinenMuutos() != null && v.getViimeinenMuutos().after(change.getRead())) {
            throw new StaleReadException(hakuoid, hakukohdeOid, valintatapajonoOid, hakemusOid, v.getViimeinenMuutos(), change.getRead());
        }

        authorizeJulkaistavissa(hakuoid, v.getJulkaistavissa(), change.getJulkaistavissa());
        authorizeHyvaksyPeruuntunutModification(tarjoajaOid, change.getHyvaksyPeruuntunut(), v);

        LOG.info("Muutetaan valintatulosta hakukohdeoid {}, valintatapajonooid {}, hakemusoid {}: {}",
                hakukohdeOid, valintatapajonoOid, hakemusOid, muutos(v, change));
        if(change.getIlmoittautumisTila() != null) {
            v.setIlmoittautumisTila(change.getIlmoittautumisTila(), selite, muokkaaja);
        }
        v.setJulkaistavissa(change.getJulkaistavissa(), selite, muokkaaja);
        v.setEhdollisestiHyvaksyttavissa(change.getEhdollisestiHyvaksyttavissa(), selite, muokkaaja);
        v.setHyvaksyttyVarasijalta(change.getHyvaksyttyVarasijalta(), selite, muokkaaja);
        v.setHyvaksyPeruuntunut(change.getHyvaksyPeruuntunut(), selite, muokkaaja);
        v.setHyvaksymiskirjeLahetetty(change.getHyvaksymiskirjeLahetetty(), selite, muokkaaja);
        valintatulosDao.createOrUpdateValintatulos(v);

        AUDIT.log(builder()
                .id(muokkaaja)
                .hakuOid(hakuoid)
                .hakukohdeOid(hakukohde.getOid())
                .hakemusOid(v.getHakemusOid())
                .valintatapajonoOid(v.getValintatapajonoOid())
                .add("ilmoittautumistila", v.getIlmoittautumisTila())
                .add("julkaistavissa", v.getJulkaistavissa())
                .add("hyvaksymiskirjeLahetetty", v.getHyvaksymiskirjeLahetetty())
                .add("hyvaksyttyvarasijalta", v.getHyvaksyttyVarasijalta())
                .add("hyvaksyperuuntunut", v.getHyvaksyPeruuntunut())
                .add("ehdollisestihyvaksyttavissa", v.getEhdollisestiHyvaksyttavissa())
                .add("selite", selite)
                .setOperaatio(ValintaperusteetOperation.HAKEMUS_TILAMUUTOS)
                .build());
    }

    public void siivoaVanhatAjotSijoittelulta(String hakuOid, Sijoittelu sijoittelu, int ajojaSaastetaan) {
        ActorRef siivoaja = actorService.getSiivousActor();
        siivoaja.tell(new PoistaVanhatAjotSijoittelulta(sijoittelu.getSijoitteluId(), ajojaSaastetaan, hakuOid), ActorRef.noSender());
    }

    public int getMaxAjoMaara() {
        return maxAjoMaara;
    }

    private void authorizeJulkaistavissa(String hakuOid, boolean v, boolean change) {
        if (!v && change && !KK_KOHDEJOUKKO.equals(tarjontaIntegrationService.getHaunKohdejoukko(hakuOid).orElse(""))) {
            ParametriArvoDTO d = tarjontaIntegrationService.getHaunParametrit(hakuOid).getPH_VEH();
            if (d.getDate() == null || d.getDate() > new Date().getTime()) {
                authorizer.checkOrganisationAccess(OPH_OID, SijoitteluRole.CRUD_ROLE);
            }
        }
    }

    private void authorizeHyvaksyPeruuntunutModification(String tarjoajaOid, boolean hyvaksyPeruuntunut, Valintatulos v) {
        if (v.getHyvaksyPeruuntunut() != hyvaksyPeruuntunut) {
            String hyvaksymisTila = hyvaksyPeruuntunut ? " hyvaksyi peruuntuneen " : " perui hyvaksynnan peruuntuneelta ";
            LOG.info(getUsernameFromSession() + hyvaksymisTila + v.getHakijaOid() + " hakemuksen " + v.getHakemusOid());
            authorizer.checkOrganisationAccess(tarjoajaOid, SijoitteluRole.PERUUNTUNEIDEN_HYVAKSYNTA);
        }
    }

    private String getUsernameFromSession() {
        Authentication authentication = getContext().getAuthentication();
        return authentication == null ? "[No user defined in session]" : authentication.getName();
    }

    private static String muutos(Valintatulos v, Valintatulos change) {
        IlmoittautumisTila ilmoittautumisTila = change.getIlmoittautumisTila();
        boolean julkaistavissa = change.getJulkaistavissa();
        boolean hyvaksyttyVarasijalta = change.getHyvaksyttyVarasijalta();
        boolean hyvaksyPeruuntunut = change.getHyvaksyPeruuntunut();
        boolean ehdollinenHyvaksynta = change.getEhdollisestiHyvaksyttavissa();
        List<String> muutos = new ArrayList<>();
        if (ilmoittautumisTila != null && ilmoittautumisTila != v.getIlmoittautumisTila()) {
            muutos.add(Optional.ofNullable(v.getIlmoittautumisTila()).map(Enum::name).orElse("") + " -> " + ilmoittautumisTila.name());
        }
        if (julkaistavissa != v.getJulkaistavissa()) {
            muutos.add((v.getJulkaistavissa() ? "JULKAISTAVISSA" : "EI JULKAISTAVISSA") + " -> " + (julkaistavissa ? "JULKAISTAVISSA" : "EI JULKAISTAVISSA"));
        }
        if (hyvaksyttyVarasijalta != v.getHyvaksyttyVarasijalta()) {
            muutos.add((v.getHyvaksyttyVarasijalta() ? "HYVÄKSYTTY VARASIJALTA" : "") + " -> " + (hyvaksyttyVarasijalta ? "HYVÄKSYTTY VARASIJALTA" : ""));
        }
        if (hyvaksyPeruuntunut != v.getHyvaksyPeruuntunut()) {
            muutos.add((v.getHyvaksyPeruuntunut() ? "HYVÄKSYTTY PERUUNTUNUT" : "") + " -> " + (hyvaksyPeruuntunut ? "HYVÄKSYTTY PERUUNTUNUT" : ""));
        }
        if (ehdollinenHyvaksynta != v.getEhdollisestiHyvaksyttavissa()) {
            muutos.add((v.getEhdollisestiHyvaksyttavissa() ? "EHDOLLINEN HYVÄKSYNTÄ" : "") + " -> " + (ehdollinenHyvaksynta ? "EHDOLLINEN HYVÄKSYNTÄ" : ""));
        }
        return muutos.stream().collect(Collectors.joining(", "));
    }

    private static boolean notModifying(Valintatulos change, Valintatulos v) {
        return v.getTila() == change.getTila() &&
                v.getIlmoittautumisTila() == change.getIlmoittautumisTila() &&
                v.getJulkaistavissa() == change.getJulkaistavissa() &&
                v.getHyvaksyttyVarasijalta() == change.getHyvaksyttyVarasijalta() &&
                v.getHyvaksyPeruuntunut() == change.getHyvaksyPeruuntunut();
    }

    private void updateMissingTarjoajaOidFromTarjonta(Hakukohde hakukohde, String hakuOid) {
        String oid = tarjontaIntegrationService.getTarjoajaOid(hakukohde.getOid())
                .orElseThrow(() -> new RuntimeException("Hakukohteelle " + hakukohde.getOid() + " ei löytynyt tarjoajaOidia sijoitteluajosta: " + hakukohde.getSijoitteluajoId()));
        hakukohde.setTarjoajaOid(oid);
        hakukohdeDao.persistHakukohde(hakukohde, hakuOid);
    }

    public static Valintatapajono getValintatapajono(String valintatapajonoOid, Hakukohde hakukohde) {
        return hakukohde.getValintatapajonot().stream()
                .filter(v -> valintatapajonoOid.equals(v.getOid()))
                .findFirst()
                .orElseThrow(() -> new ValintatapajonoaEiLoytynytException(String.format("Valintatapajonoa %sei löytynyt hakukohteelle %s", valintatapajonoOid, hakukohde.getOid())));
    }

    private static Hakemus getHakemus(final String hakemusOid, final Valintatapajono valintatapajono) {
        return valintatapajono.getHakemukset().stream()
                .filter(h -> hakemusOid.equals(h.getHakemusOid()))
                .findFirst()
                .orElseThrow(() -> new HakemustaEiLoytynytException(valintatapajono.getOid(), hakemusOid));
    }
}
