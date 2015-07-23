package fi.vm.sade.sijoittelu.laskenta.service.business;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import akka.actor.ActorRef;
import com.google.common.collect.Sets.SetView;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluajoWrapperFactory;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.sijoittelu.domain.comparator.HakemusComparator;
import fi.vm.sade.sijoittelu.laskenta.actors.messages.PoistaHakukohteet;
import fi.vm.sade.sijoittelu.laskenta.actors.messages.PoistaVanhatAjotSijoittelulta;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriDTO;
import fi.vm.sade.sijoittelu.laskenta.service.it.TarjontaIntegrationService;
import fi.vm.sade.sijoittelu.tulos.dao.*;
import fi.vm.sade.sijoittelu.laskenta.service.exception.*;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.service.impl.converters.SijoitteluTulosConverter;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fi.vm.sade.authentication.business.service.Authorizer;
import fi.vm.sade.security.service.authz.util.AuthorizationUtil;
import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithm;
import fi.vm.sade.sijoittelu.tulos.roles.SijoitteluRole;

import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.intersection;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.join;


@Service
public class SijoitteluBusinessService {
    private static final Logger LOG = LoggerFactory.getLogger(SijoitteluBusinessService.class);
    private HakemusComparator hakemusComparator = new HakemusComparator();
    private final String KK_KOHDEJOUKKO = "haunkohdejoukko_12";

    @Autowired
    private ValintatulosDao valintatulosDao;

    @Autowired
    private HakukohdeDao hakukohdeDao;

    @Autowired
    private SijoitteluDao sijoitteluDao;

    @Autowired
    private ValiSijoitteluDao valisijoitteluDao;

    @Autowired
    private ErillisSijoitteluDao erillisSijoitteluDao;

    @Autowired
    private Authorizer authorizer;

    @Autowired
    private SijoitteluTulosConverter sijoitteluTulosConverter;

    @Value("${root.organisaatio.oid}")
    private String rootOrgOid;

    @Value("${sijoittelu.maxAjojenMaara:75}")
    private int maxAjoMaara;

    @Autowired
    ActorService actorService;

    @Autowired
    TarjontaIntegrationService tarjontaIntegrationService;

    /**
     * ei versioi sijoittelua, tekeee uuden sijoittelun olemassaoleville
     * kohteille ei kayteta viela mihinkaan
     */
    public void sijoittele(String hakuOid) {
        Sijoittelu sijoittelu = getOrCreateSijoittelu(hakuOid);
        SijoitteluAjo viimeisinSijoitteluajo = sijoittelu.getLatestSijoitteluajo();
        List<Hakukohde> hakukohteet = hakukohdeDao.getHakukohdeForSijoitteluajo(viimeisinSijoitteluajo.getSijoitteluajoId());
        List<Valintatulos> valintatulokset = valintatulosDao.loadValintatulokset(hakuOid);
        viimeisinSijoitteluajo.setStartMils(System.currentTimeMillis());
        SijoitteluAlgorithm.sijoittele(hakukohteet, valintatulokset);
        viimeisinSijoitteluajo.setEndMils(System.currentTimeMillis());
        // and after
        sijoitteluDao.persistSijoittelu(sijoittelu);
        for (Hakukohde hakukohde : hakukohteet) {
            hakukohdeDao.persistHakukohde(hakukohde);
        }
    }

    private static Set<String> hakukohteidenJonoOidit(List<Hakukohde> hakukohteet) {
        return unmodifiableSet(hakukohteet.stream()
                .flatMap(hakukohde -> hakukohde.getValintatapajonot().stream().map(Valintatapajono::getOid))
                .collect(toSet()));
    }

    public void sijoittele(HakuDTO sijoitteluTyyppi, Set<String> valintaperusteidenJonot) {
        long startTime = System.currentTimeMillis();
        String hakuOid = sijoitteluTyyppi.getHakuOid();
        LOG.info("SijoitteluBusinessServiceImpl:n sijoittelu haulle {} alkaa.", hakuOid);
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
        List<Valintatulos> valintatulokset = valintatulosDao.loadValintatulokset(hakuOid);
        LOG.info("Haun {} sijoittelun koko: {} olemassaolevaa, {} uutta, {} valintatulosta", hakuOid, olemassaolevatHakukohteet.size(), uudetHakukohteet.size(), valintatulokset.size());
        final SijoitteluajoWrapper sijoitteluajoWrapper = SijoitteluajoWrapperFactory.createSijoitteluAjo(kaikkiHakukohteet, valintatulokset);
        asetaSijoittelunParametrit(hakuOid, sijoitteluajoWrapper);
        uusiSijoitteluajo.setStartMils(startTime);
        LOG.info("Suoritetaan sijoittelu haulle {}", hakuOid);
        SijoitteluAlgorithm.sijoittele(sijoitteluajoWrapper);
        uusiSijoitteluajo.setEndMils(System.currentTimeMillis());
        processOldApplications(olemassaolevatHakukohteet, kaikkiHakukohteet);
        List<Valintatulos> muuttuneetValintatulokset = sijoitteluajoWrapper.getMuuttuneetValintatulokset();
        List<Valintatulos> mergatut = valintatulosDao.mergaaValintatulos(kaikkiHakukohteet, muuttuneetValintatulokset);
        mergatut.forEach(valintatulosDao::createOrUpdateValintatulos);
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
        SijoitteluAlgorithm.sijoittele(SijoitteluajoWrapperFactory.createSijoitteluAjo(kaikkiHakukohteet, valintatulokset));
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
        List<Valintatulos> valintatulokset = sijoitteluTyyppi.getHakukohteet()
                .stream()
                .map(h -> valintatulosDao.loadValintatuloksetForHakukohde(h.getOid()))
                .flatMap(list -> list.stream())
                .collect(Collectors.toList());
        uusiSijoitteluajo.setStartMils(startTime);
        SijoitteluAlgorithm.sijoittele(SijoitteluajoWrapperFactory.createSijoitteluAjo(kaikkiHakukohteet, valintatulokset));
        uusiSijoitteluajo.setEndMils(System.currentTimeMillis());
        processOldApplications(olemassaolevatHakukohteet, kaikkiHakukohteet);
        erillisSijoitteluDao.persistSijoittelu(sijoittelu);
        return uusiSijoitteluajo.getSijoitteluajoId();
    }

    private void processOldApplications(final List<Hakukohde> olemassaolevatHakukohteet, final List<Hakukohde> kaikkiHakukohteet) {
        Map<String, Hakemus> hakemusHashMap = getStringHakemusMap(olemassaolevatHakukohteet);
        kaikkiHakukohteet.parallelStream().forEach(hakukohde -> {
                    hakukohde.getValintatapajonot().forEach(processValintatapaJono(hakemusHashMap, hakukohde));
                    hakukohdeDao.persistHakukohde(hakukohde);
                }
        );
    }

    private Consumer<Valintatapajono> processValintatapaJono(Map<String, Hakemus> hakemusHashMap, Hakukohde hakukohde) {
        return valintatapajono -> {
                    valintatapajono.setAlinHyvaksyttyPistemaara(alinHyvaksyttyPistemaara(valintatapajono.getHakemukset()).orElse(null));
                    valintatapajono.setHyvaksytty(getMaara(valintatapajono.getHakemukset(), Arrays.asList(HakemuksenTila.HYVAKSYTTY, HakemuksenTila.VARASIJALTA_HYVAKSYTTY)));
                    valintatapajono.setVaralla(getMaara(valintatapajono.getHakemukset(), Arrays.asList(HakemuksenTila.VARALLA)));
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
        kaikkiHakukohteet.values().forEach(hakukohde -> {
                    HakukohdeItem hki = new HakukohdeItem();
                    hki.setOid(hakukohde.getOid());
                    uusiSijoitteluajo.getHakukohteet().add(hki);
                    hakukohde.setSijoitteluajoId(uusiSijoitteluajo.getSijoitteluajoId());
                }
        );
        return new ArrayList<>(kaikkiHakukohteet.values());
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

    public boolean muutoksetOvatAjantasaisia(String hakukohdeOid, List<Valintatulos> valintatulokset) {
        return !valintatulokset.stream()
                .anyMatch(valintatulos -> {
                    Valintatulos saved = valintatulosDao.loadValintatulos(hakukohdeOid, valintatulos.getValintatapajonoOid(), valintatulos.getHakemusOid());
                    Interval interval = new Interval(new DateTime(valintatulos.getRead()), DateTime.now());
                    return saved != null && saved.getLogEntries() != null &&
                            saved.getLogEntries().stream().anyMatch(entry -> interval.contains(entry.getLuotu().getTime()));
                });
    }

    public List<Valintatulos> haeHakemuksenTila(String hakemusOid) {
        if (isBlank(hakemusOid)) {
            throw new RuntimeException("Invalid search params, fix exception later");
        }
        return valintatulosDao.loadValintatulos(hakemusOid);
    }

    public Hakukohde getHakukohde(String hakuOid, String hakukohdeOid) {
        return sijoitteluDao.getSijoitteluByHakuOid(hakuOid)
                .map(sijoittelu -> hakukohdeDao.getHakukohdeForSijoitteluajo(sijoittelu.getLatestSijoitteluajo().getSijoitteluajoId(), hakukohdeOid))
                .orElseThrow(() -> new RuntimeException("Sijoittelua ei löytynyt haulle: " + hakuOid));
    }

    public Hakukohde getErillishaunHakukohde(String hakuOid, String hakukohdeOid) {
        return erillisSijoitteluDao.getSijoitteluByHakuOid(hakuOid)
                .map(sijoittelu -> hakukohdeDao.getHakukohdeForSijoitteluajo(sijoittelu.getLatestSijoitteluajo().getSijoitteluajoId(), hakukohdeOid))
                .orElseThrow(() -> new RuntimeException("Erillissijoittelua ei löytynyt haulle: " + hakuOid));
    }

    public void vaihdaHakemuksenTila(String hakuoid,
                                     Hakukohde hakukohde,
                                     String valintatapajonoOid,
                                     String hakemusOid,
                                     ValintatuloksenTila tila,
                                     String selite,
                                     IlmoittautumisTila ilmoittautumisTila,
                                     boolean julkaistavissa,
                                     boolean hyvaksyttyVarasijalta,
                                     boolean hyvaksyPeruuntunut) {
        if (tila == null || isBlank(hakuoid) || isBlank(valintatapajonoOid) || isBlank(hakemusOid)) {
            throw new IllegalArgumentException(String.format("tila: %s, hakuoid: %s, valintatapajonoOid: %s, hakemusOid: %s", tila, hakuoid, valintatapajonoOid, hakemusOid));
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
                .orElse(buildValintatulos(hakuoid, hakukohde, valintatapajono, hakemus));
        if (notModifying(tila, ilmoittautumisTila, julkaistavissa, hyvaksyttyVarasijalta, hyvaksyPeruuntunut, v)) {
            return;
        }

        authorizeHyvaksyPeruuntunutModification(tarjoajaOid, hyvaksyPeruuntunut, v);

        String muutos = muutos(v, tila, ilmoittautumisTila, julkaistavissa, hyvaksyttyVarasijalta, hyvaksyPeruuntunut);
        v.getLogEntries().add(createLogEntry(selite, muutos));
        v.setTila(tila);
        v.setIlmoittautumisTila(ilmoittautumisTila);
        v.setJulkaistavissa(julkaistavissa);
        v.setHyvaksyttyVarasijalta(hyvaksyttyVarasijalta);
        v.setHyvaksyPeruuntunut(hyvaksyPeruuntunut);
        LOG.info("Muutetaan valintatulosta hakukohdeoid {}, valintatapajonooid {}, hakemusoid {}: {}", hakukohdeOid, valintatapajonoOid, hakemusOid, muutos);
        valintatulosDao.createOrUpdateValintatulos(v);
    }

    private void authorizeHyvaksyPeruuntunutModification(String tarjoajaOid, boolean hyvaksyPeruuntunut, Valintatulos v) {
        if (v.getHyvaksyPeruuntunut() != hyvaksyPeruuntunut) {
            authorizer.checkOrganisationAccess(tarjoajaOid, SijoitteluRole.PERUUNTUNEIDEN_HYVAKSYNTA);
        }
    }

    private static Valintatulos buildValintatulos(String hakuoid, Hakukohde hakukohde, Valintatapajono valintatapajono, Hakemus hakemus) {
        Valintatulos v = new Valintatulos();
        v.setHakemusOid(hakemus.getHakemusOid());
        v.setValintatapajonoOid(valintatapajono.getOid());
        v.setHakukohdeOid(hakukohde.getOid());
        v.setHakijaOid(hakemus.getHakijaOid());
        v.setHakutoive(hakemus.getPrioriteetti());
        v.setHakuOid(hakuoid);
        return v;
    }

    private static String muutos(Valintatulos v,
                                 ValintatuloksenTila tila,
                                 IlmoittautumisTila ilmoittautumisTila,
                                 boolean julkaistavissa,
                                 boolean hyvaksyttyVarasijalta,
                                 boolean hyvaksyPeruuntunut) {
        List<String> muutos = new ArrayList<>();
        if (tila != v.getTila()) {
            muutos.add(Optional.ofNullable(v.getTila()).map(Enum::name).orElse("") + " -> " + tila.name());
        }
        if (ilmoittautumisTila != v.getIlmoittautumisTila()) {
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
        return muutos.stream().collect(Collectors.joining(", "));
    }

    private static boolean notModifying(ValintatuloksenTila tila,
                                        IlmoittautumisTila ilmoittautumisTila,
                                        boolean julkaistavissa,
                                        boolean hyvaksyttyVarasijalta,
                                        boolean hyvaksyPeruuntunut,
                                        Valintatulos v) {
        return v.getTila() == tila &&
                v.getIlmoittautumisTila() == ilmoittautumisTila &&
                v.getJulkaistavissa() == julkaistavissa &&
                v.getHyvaksyttyVarasijalta() == hyvaksyttyVarasijalta &&
                v.getHyvaksyPeruuntunut() == hyvaksyPeruuntunut;
    }

    private LogEntry createLogEntry(String selite, String muutos) {
        LogEntry logEntry = new LogEntry();
        logEntry.setLuotu(new Date());
        logEntry.setMuokkaaja(AuthorizationUtil.getCurrentUser());
        logEntry.setSelite(selite);
        logEntry.setMuutos(muutos);
        return logEntry;
    }

    private void updateMissingTarjoajaOidFromTarjonta(Hakukohde hakukohde) {
        String oid = tarjontaIntegrationService.getTarjoajaOid(hakukohde.getOid())
                .orElseThrow(() -> new RuntimeException("Hakukohteelle " + hakukohde.getOid() + " ei löytynyt tarjoajaOidia sijoitteluajosta: " + hakukohde.getSijoitteluajoId()));
        hakukohde.setTarjoajaOid(oid);
        hakukohdeDao.persistHakukohde(hakukohde);
    }

    private static Valintatapajono getValintatapajono(String valintatapajonoOid, Hakukohde hakukohde) {
        return hakukohde.getValintatapajonot().stream()
                .filter(v -> valintatapajonoOid.equals(v.getOid()))
                .findFirst()
                .orElseThrow(() -> new ValintatapajonoaEiLoytynytException(String.format("Valintatapajonoa %sei löytynyt hakukohteelle %s", valintatapajonoOid, hakukohde.getOid())));
    }

    private static Hakemus getHakemus(final String hakemusOid, final Valintatapajono valintatapajono) {
        return valintatapajono.getHakemukset().stream()
                .filter(h -> hakemusOid.equals(h.getHakemusOid()))
                .findFirst()
                .orElseThrow(() -> new HakemustaEiLoytynytException("Hakemusta ei löytynyt."));
    }
}
