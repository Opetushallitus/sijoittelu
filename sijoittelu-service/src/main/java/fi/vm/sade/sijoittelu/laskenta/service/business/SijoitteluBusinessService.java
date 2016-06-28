package fi.vm.sade.sijoittelu.laskenta.service.business;

import akka.actor.ActorRef;
import com.google.common.collect.Sets.SetView;
import fi.vm.sade.auditlog.valintaperusteet.ValintaperusteetOperation;
import fi.vm.sade.authentication.business.service.Authorizer;
import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithm;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluajoWrapperFactory;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor.PostSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilaTaulukot;
import fi.vm.sade.sijoittelu.laskenta.external.resource.ValintaTulosServiceResource;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor.PreSijoitteluProcessor;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.sijoittelu.domain.comparator.HakemusComparator;
import fi.vm.sade.sijoittelu.domain.dto.VastaanottoDTO;
import fi.vm.sade.sijoittelu.laskenta.actors.messages.PoistaHakukohteet;
import fi.vm.sade.sijoittelu.laskenta.actors.messages.PoistaVanhatAjotSijoittelulta;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriArvoDTO;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriDTO;
import fi.vm.sade.sijoittelu.laskenta.service.exception.HakemustaEiLoytynytException;
import fi.vm.sade.sijoittelu.laskenta.service.exception.ValintatapajonoaEiLoytynytException;
import fi.vm.sade.sijoittelu.laskenta.service.it.TarjontaIntegrationService;
import fi.vm.sade.sijoittelu.tulos.dao.*;
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
    private final ValintatulosDao valintatulosDao;
    private final HakukohdeDao hakukohdeDao;
    private final SijoitteluDao sijoitteluDao;
    private final RaportointiService raportointiService;
    private final ValiSijoitteluDao valisijoitteluDao;
    private final ErillisSijoitteluDao erillisSijoitteluDao;
    private final Authorizer authorizer;
    private final SijoitteluTulosConverter sijoitteluTulosConverter;
    private final ActorService actorService;
    private final TarjontaIntegrationService tarjontaIntegrationService;
    private final ValintaTulosServiceResource valintaTulosServiceResource;
    private final ValintatulosWithVastaanotto valintatulosWithVastaanotto;
    private final Collection<PostSijoitteluProcessor> postSijoitteluProcessors;
    private final Collection<PreSijoitteluProcessor> preSijoitteluProcessors;

    @Autowired
    public SijoitteluBusinessService(@Value("${sijoittelu.maxAjojenMaara:75}") int maxAjoMaara,
                                     ValintatulosDao valintatulosDao,
                                     HakukohdeDao hakukohdeDao,
                                     SijoitteluDao sijoitteluDao,
                                     RaportointiService raportointiService,
                                     ValiSijoitteluDao valisijoitteluDao,
                                     ErillisSijoitteluDao erillisSijoitteluDao,
                                     Authorizer authorizer,
                                     SijoitteluTulosConverter sijoitteluTulosConverter,
                                     ActorService actorService,
                                     TarjontaIntegrationService tarjontaIntegrationService,
                                     ValintaTulosServiceResource valintaTulosServiceResource) {
        this.maxAjoMaara = maxAjoMaara;
        this.valintatulosDao = valintatulosDao;
        this.hakukohdeDao = hakukohdeDao;
        this.sijoitteluDao = sijoitteluDao;
        this.raportointiService = raportointiService;
        this.valisijoitteluDao = valisijoitteluDao;
        this.erillisSijoitteluDao = erillisSijoitteluDao;
        this.authorizer = authorizer;
        this.sijoitteluTulosConverter = sijoitteluTulosConverter;
        this.actorService = actorService;
        this.tarjontaIntegrationService = tarjontaIntegrationService;
        this.valintaTulosServiceResource = valintaTulosServiceResource;
        this.valintatulosWithVastaanotto = new ValintatulosWithVastaanotto(valintatulosDao, valintaTulosServiceResource);
        this.preSijoitteluProcessors = PreSijoitteluProcessor.defaultPreProcessors();
        this.postSijoitteluProcessors = PostSijoitteluProcessor.defaultPostProcessors();
    }

    private static Set<String> hakukohteidenJonoOidit(List<Hakukohde> hakukohteet) {
        return unmodifiableSet(hakukohteet.stream()
                .flatMap(hakukohde -> hakukohde.getValintatapajonot().stream().map(Valintatapajono::getOid))
                .collect(toSet()));
    }

    public void sijoittele(HakuDTO sijoitteluTyyppi, Set<String> valintaperusteidenJonot) {
        long startTime = System.currentTimeMillis();
        String hakuOid = sijoitteluTyyppi.getHakuOid();
        LOG.info("Sijoittelu haulle {} alkaa.", hakuOid);
        Sijoittelu sijoittelu = getOrCreateSijoittelu(hakuOid);
        SijoitteluAjo viimeisinSijoitteluajo = sijoittelu.getLatestSijoitteluajo();
        List<Hakukohde> uudetHakukohteet = sijoitteluTyyppi.getHakukohteet().stream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        List<Hakukohde> olemassaolevatHakukohteet = Collections.<Hakukohde>emptyList();
        if (viimeisinSijoitteluajo != null) {
            olemassaolevatHakukohteet = hakukohdeDao.getHakukohdeForSijoitteluajo(viimeisinSijoitteluajo.getSijoitteluajoId());
            SetView<String> poistuneetJonot = difference(hakukohteidenJonoOidit(olemassaolevatHakukohteet), hakukohteidenJonoOidit(uudetHakukohteet));
            SetView<String> valintaperusteidenVaatimat = intersection(valintaperusteidenJonot, poistuneetJonot);
            // Uuden sijoittelun jonot pitaa olla superset paitsi jos ne on poistettu valintaperusteista
            if (poistuneetJonot.size() > 0 && valintaperusteidenVaatimat.size() > 0) {
                String msg = "Edellisessa sijoittelussa olleet jonot [" + join(poistuneetJonot, ", ") + "] puuttuvat vaikka valintaperusteet yha vaativat jonot [" + join(valintaperusteidenVaatimat, ", ") + "]";
                LOG.error(msg);
                throw new RuntimeException(msg);
            }
        }
        SijoitteluAjo uusiSijoitteluajo = createSijoitteluAjo(sijoittelu);
        List<Hakukohde> kaikkiHakukohteet = merge(uusiSijoitteluajo, olemassaolevatHakukohteet, uudetHakukohteet);
        List<Valintatulos> valintatulokset = valintatulosWithVastaanotto.forHaku(hakuOid);
        Map<String, VastaanottoDTO> kaudenAiemmatVastaanotot = aiemmanVastaanotonHakukohdePerHakija(hakuOid);
        LOG.info("Haun {} sijoittelun koko: {} olemassaolevaa, {} uutta, {} valintatulosta", hakuOid, olemassaolevatHakukohteet.size(), uudetHakukohteet.size(), valintatulokset.size());
        final SijoitteluajoWrapper sijoitteluajoWrapper = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(uusiSijoitteluajo, kaikkiHakukohteet, valintatulokset, kaudenAiemmatVastaanotot);
        asetaSijoittelunParametrit(hakuOid, sijoitteluajoWrapper);
        uusiSijoitteluajo.setStartMils(startTime);
        sijoitteluajoWrapper.setEdellisenSijoittelunHakukohteet(olemassaolevatHakukohteet);
        LOG.info("Suoritetaan sijoittelu haulle {}", hakuOid);
        SijoitteluAlgorithm.sijoittele(preSijoitteluProcessors, postSijoitteluProcessors, sijoitteluajoWrapper);
        uusiSijoitteluajo.setEndMils(System.currentTimeMillis());
        processOldApplications(olemassaolevatHakukohteet, kaikkiHakukohteet);
        List<Valintatulos> muuttuneetValintatulokset = sijoitteluajoWrapper.getMuuttuneetValintatulokset();
        LOG.info("Ennen mergeä muuttuneita valintatuloksia: " + muuttuneetValintatulokset.size());
        LOG.info("Ennen mergeä muuttuneet valintatulokset: " + muuttuneetValintatulokset);
        List<Valintatulos> mergatut = valintatulosDao.mergaaValintatulos(kaikkiHakukohteet, muuttuneetValintatulokset);
        valintatulosWithVastaanotto.persistValintatulokset(mergatut);
        sijoitteluajoWrapper.setMuuttuneetValintatulokset(mergatut);
        List<String> varasijapomput = sijoitteluajoWrapper.getVarasijapomput();
        varasijapomput.forEach(LOG::info);
        LOG.info("Haun {} sijoittelussa muuttui {} kpl valintatuloksia, pomppuja {} kpl", hakuOid, mergatut.size(), varasijapomput.size());
        ActorRef siivoaja = actorService.getSiivousActor();
        try {
            sijoitteluDao.persistSijoittelu(sijoittelu);
            LOG.info("Sijoittelu persistoitu haulle {}. Poistetaan vanhoja ajoja. Säästettävien ajojen määrää {}", sijoittelu.getHakuOid(), maxAjoMaara);
            siivoaja.tell(new PoistaVanhatAjotSijoittelulta(sijoittelu.getSijoitteluId(), maxAjoMaara), ActorRef.noSender());
        } catch (Exception e) {
            LOG.error("Sijoittelun persistointi haulle {} epäonnistui. Rollback hakukohteet", sijoittelu.getHakuOid());
            siivoaja.tell(new PoistaHakukohteet(sijoittelu, uusiSijoitteluajo.getSijoitteluajoId()), ActorRef.noSender());
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
        ValiSijoittelu sijoittelu = getOrCreateValiSijoittelu(hakuOid);
        List<Hakukohde> uudetHakukohteet = sijoitteluTyyppi.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        List<Hakukohde> olemassaolevatHakukohteet = Collections.<Hakukohde>emptyList();
        SijoitteluAjo uusiSijoitteluajo = createValiSijoitteluAjo(sijoittelu);
        List<Hakukohde> kaikkiHakukohteet = merge(uusiSijoitteluajo, olemassaolevatHakukohteet, uudetHakukohteet);
        List<Valintatulos> valintatulokset = Collections.emptyList();
        uusiSijoitteluajo.setStartMils(startTime);
        SijoitteluAlgorithm.sijoittele(
                preSijoitteluProcessors,
                postSijoitteluProcessors,
                SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(uusiSijoitteluajo, kaikkiHakukohteet, valintatulokset, Collections.emptyMap())
        );
        uusiSijoitteluajo.setEndMils(System.currentTimeMillis());
        processOldApplications(olemassaolevatHakukohteet, kaikkiHakukohteet);
        valisijoitteluDao.persistSijoittelu(sijoittelu);
        return kaikkiHakukohteet.parallelStream().map(h -> sijoitteluTulosConverter.convert(h)).collect(Collectors.toList());
    }

    public long erillissijoittele(HakuDTO sijoitteluTyyppi) {
        long startTime = System.currentTimeMillis();
        String hakuOid = sijoitteluTyyppi.getHakuOid();
        ErillisSijoittelu sijoittelu = getOrCreateErillisSijoittelu(hakuOid);
        List<Hakukohde> uudetHakukohteet = sijoitteluTyyppi.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        List<Hakukohde> olemassaolevatHakukohteet = Collections.<Hakukohde>emptyList();
        SijoitteluAjo uusiSijoitteluajo = createErillisSijoitteluAjo(sijoittelu);
        List<Hakukohde> kaikkiHakukohteet = merge(uusiSijoitteluajo, olemassaolevatHakukohteet, uudetHakukohteet);
        List<Valintatulos> valintatulokset = valintatulosWithVastaanotto.forHaku(hakuOid);
        Map<String, VastaanottoDTO> kaudenAiemmatVastaanotot = aiemmanVastaanotonHakukohdePerHakija(hakuOid);
        uusiSijoitteluajo.setStartMils(startTime);
        SijoitteluAlgorithm.sijoittele(
                preSijoitteluProcessors,
                postSijoitteluProcessors,
                SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(uusiSijoitteluajo, kaikkiHakukohteet, valintatulokset, kaudenAiemmatVastaanotot)
        );
        uusiSijoitteluajo.setEndMils(System.currentTimeMillis());
        processOldApplications(olemassaolevatHakukohteet, kaikkiHakukohteet);
        erillisSijoitteluDao.persistSijoittelu(sijoittelu);
        return uusiSijoitteluajo.getSijoitteluajoId();
    }

    private void processOldApplications(final List<Hakukohde> olemassaolevatHakukohteet, final List<Hakukohde> kaikkiHakukohteet) {
        Map<String, Hakemus> hakemusHashMap = getStringHakemusMap(olemassaolevatHakukohteet);
        Map<String, Valintatapajono> valintatapajonoHashMap = getStringValintatapajonoMap(olemassaolevatHakukohteet);
        kaikkiHakukohteet.parallelStream().forEach(hakukohde -> {
                    hakukohde.getValintatapajonot().forEach(processValintatapaJono(valintatapajonoHashMap, hakemusHashMap, hakukohde));
                    hakukohdeDao.persistHakukohde(hakukohde);
                }
        );
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
                            if(hakemus.isHyvaksyttyHakijaryhmasta() == false) {
                                hakemus.setHyvaksyttyHakijaryhmasta(edellinen.isHyvaksyttyHakijaryhmasta());
                                hakemus.setHakijaryhmaOid(edellinen.getHakijaryhmaOid());
                            }
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

    private SijoitteluAjo createErillisSijoitteluAjo(ErillisSijoittelu sijoittelu) {
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

    private ErillisSijoittelu getOrCreateErillisSijoittelu(String hakuoid) {
        Optional<ErillisSijoittelu> sijoitteluOpt = erillisSijoitteluDao.getSijoitteluByHakuOid(hakuoid);
        if (sijoitteluOpt.isPresent()) {
            return sijoitteluOpt.get();
        } else {
            ErillisSijoittelu sijoittelu = new ErillisSijoittelu();
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

    public Hakukohde getErillishaunHakukohde(String hakuOid, String hakukohdeOid) {
        return erillisSijoitteluDao.getSijoitteluByHakuOid(hakuOid)
                .map(sijoittelu -> hakukohdeDao.getHakukohdeForSijoitteluajo(sijoittelu.getLatestSijoitteluajo().getSijoitteluajoId(), hakukohdeOid))
                .orElseThrow(() -> new RuntimeException("Erillissijoittelua ei löytynyt haulle: " + hakuOid));
    }

    public Map<String, VastaanottoDTO> aiemmanVastaanotonHakukohdePerHakija(String hakuOid) {
        return valintaTulosServiceResource.haunKoulutuksenAlkamiskaudenVastaanototYhdenPaikanSaadoksenPiirissa(hakuOid)
                .stream().collect(Collectors.toMap(VastaanottoDTO::getHenkiloOid, Function.identity()));
    }

    public void asetaJononValintaesitysHyvaksytyksi(Hakukohde hakukohde, String valintatapajonoOid, boolean hyvaksytty) {
        LOG.info("Asetetaan hakukohteen {} valintatapajonon {} valintaesitys hyväksytyksi: {}", hakukohde.getOid(), valintatapajonoOid, hyvaksytty);
        Valintatapajono valintatapajono = getValintatapajono(valintatapajonoOid, hakukohde);
        valintatapajono.setValintaesitysHyvaksytty(hyvaksytty);
        hakukohdeDao.persistHakukohde(hakukohde);
    }

    public void vaihdaHakemuksenTila(String hakuoid, Hakukohde hakukohde, Valintatulos change, String selite, String muokkaaja) {
        String valintatapajonoOid = change.getValintatapajonoOid();
        String hakemusOid = change.getHakemusOid();
        if (isBlank(hakuoid) || isBlank(valintatapajonoOid) || isBlank(hakemusOid)) {
            throw new IllegalArgumentException(String.format("hakuoid: %s, valintatapajonoOid: %s, hakemusOid: %s", hakuoid, valintatapajonoOid, hakemusOid));
        }
        String tarjoajaOid = hakukohde.getTarjoajaOid();
        if (isBlank(tarjoajaOid)) {
            updateMissingTarjoajaOidFromTarjonta(hakukohde);
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

    private void updateMissingTarjoajaOidFromTarjonta(Hakukohde hakukohde) {
        String oid = tarjontaIntegrationService.getTarjoajaOid(hakukohde.getOid())
                .orElseThrow(() -> new RuntimeException("Hakukohteelle " + hakukohde.getOid() + " ei löytynyt tarjoajaOidia sijoitteluajosta: " + hakukohde.getSijoitteluajoId()));
        hakukohde.setTarjoajaOid(oid);
        hakukohdeDao.persistHakukohde(hakukohde);
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
