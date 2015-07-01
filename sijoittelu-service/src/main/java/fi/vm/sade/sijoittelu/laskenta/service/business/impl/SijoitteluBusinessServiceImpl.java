package fi.vm.sade.sijoittelu.laskenta.service.business.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import akka.actor.ActorRef;
import com.google.common.collect.Sets.SetView;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.sijoittelu.domain.comparator.HakemusComparator;
import fi.vm.sade.sijoittelu.laskenta.actors.messages.PoistaHakukohteet;
import fi.vm.sade.sijoittelu.laskenta.actors.messages.PoistaVanhatAjotSijoittelulta;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriDTO;
import fi.vm.sade.sijoittelu.laskenta.service.business.ActorService;
import fi.vm.sade.sijoittelu.laskenta.service.it.TarjontaIntegrationService;
import fi.vm.sade.sijoittelu.tulos.dao.*;
import fi.vm.sade.sijoittelu.laskenta.mapping.SijoitteluModelMapper;
import fi.vm.sade.sijoittelu.laskenta.service.exception.*;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.service.impl.converters.SijoitteluTulosConverter;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fi.vm.sade.authentication.business.service.Authorizer;
import fi.vm.sade.generic.service.exception.NotAuthorizedException;
import fi.vm.sade.security.service.authz.util.AuthorizationUtil;
import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithm;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithmFactory;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.sijoittelu.tulos.roles.SijoitteluRole;

import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.intersection;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang.StringUtils.join;

@Service
public class SijoitteluBusinessServiceImpl implements SijoitteluBusinessService {
    private static final Logger LOG = LoggerFactory.getLogger(SijoitteluBusinessServiceImpl.class);
    private HakemusComparator hakemusComparator = new HakemusComparator();
    private final String KK_KOHDEJOUKKO = "haunkohdejoukko_12";

    @Autowired
    private SijoitteluAlgorithmFactory algorithmFactory;

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
    private SijoitteluModelMapper modelMapper;

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
        SijoitteluAlgorithm sijoitteluAlgorithm = algorithmFactory.constructAlgorithm(hakukohteet, valintatulokset);
        viimeisinSijoitteluajo.setStartMils(System.currentTimeMillis());
        sijoitteluAlgorithm.start();
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

    @Override
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
        SijoitteluAlgorithm sijoitteluAlgorithm = algorithmFactory.constructAlgorithm(kaikkiHakukohteet, valintatulokset);
        asetaSijoittelunParametrit(hakuOid, sijoitteluAlgorithm);
        uusiSijoitteluajo.setStartMils(startTime);
        LOG.info("Suoritetaan sijoittelu haulle {}", hakuOid);
        sijoitteluAlgorithm.start();
        uusiSijoitteluajo.setEndMils(System.currentTimeMillis());
        processOldApplications(olemassaolevatHakukohteet, kaikkiHakukohteet);
        List<Valintatulos> muuttuneetValintatulokset = sijoitteluAlgorithm.getSijoitteluAjo().getMuuttuneetValintatulokset();
        List<Valintatulos> mergatut = valintatulosDao.mergaaValintatulos(kaikkiHakukohteet, muuttuneetValintatulokset);
        mergatut.forEach(valintatulosDao::createOrUpdateValintatulos);
        sijoitteluAlgorithm.getSijoitteluAjo().setMuuttuneetValintatulokset(mergatut);
        List<String> varasijapomput = sijoitteluAlgorithm.getSijoitteluAjo().getVarasijapomput();
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

    private void asetaSijoittelunParametrit(String hakuOid, SijoitteluAlgorithm sijoitteluAlgorithm) {
        SijoitteluajoWrapper sijoitteluAjo = sijoitteluAlgorithm.getSijoitteluAjo();
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

    @Override
    public List<HakukohdeDTO> valisijoittele(HakuDTO sijoitteluTyyppi) {
        long startTime = System.currentTimeMillis();
        String hakuOid = sijoitteluTyyppi.getHakuOid();
        ValiSijoittelu sijoittelu = getOrCreateValiSijoittelu(hakuOid);
        List<Hakukohde> uudetHakukohteet = sijoitteluTyyppi.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        List<Hakukohde> olemassaolevatHakukohteet = Collections.<Hakukohde>emptyList();
        SijoitteluAjo uusiSijoitteluajo = createValiSijoitteluAjo(sijoittelu);
        List<Hakukohde> kaikkiHakukohteet = merge(uusiSijoitteluajo, olemassaolevatHakukohteet, uudetHakukohteet);
        List<Valintatulos> valintatulokset = Collections.emptyList();
        SijoitteluAlgorithm sijoitteluAlgorithm = algorithmFactory.constructAlgorithm(kaikkiHakukohteet, valintatulokset);
        uusiSijoitteluajo.setStartMils(startTime);
        sijoitteluAlgorithm.start();
        uusiSijoitteluajo.setEndMils(System.currentTimeMillis());
        processOldApplications(olemassaolevatHakukohteet, kaikkiHakukohteet);
        valisijoitteluDao.persistSijoittelu(sijoittelu);
        return kaikkiHakukohteet.parallelStream().map(h -> sijoitteluTulosConverter.convert(h)).collect(Collectors.toList());
    }

    @Override
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
        SijoitteluAlgorithm sijoitteluAlgorithm = algorithmFactory.constructAlgorithm(kaikkiHakukohteet, valintatulokset);
        uusiSijoitteluajo.setStartMils(startTime);
        sijoitteluAlgorithm.start();
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

    @Override
    public Valintatulos haeHakemuksenTila(String hakuoid, String hakukohdeOid, String valintatapajonoOid, String hakemusOid) {
        if (StringUtils.isBlank(hakukohdeOid) || StringUtils.isBlank(hakemusOid)) {
            throw new RuntimeException("Invalid search params, fix exception later");
        }
        return valintatulosDao.loadValintatulos(hakukohdeOid, valintatapajonoOid, hakemusOid);
    }

    @Override
    public List<Valintatulos> haeHakemustenTilat(String hakukohdeOid, String valintatapajonoOid) {
        if (StringUtils.isBlank(hakukohdeOid)) {
            throw new RuntimeException("Invalid search params, fix exception later");
        }
        return valintatulosDao.loadValintatulokset(hakukohdeOid, valintatapajonoOid);
    }

    @Override
    public List<Valintatulos> haeHakukohteenTilat(String hakukohdeOid) {
        if (StringUtils.isBlank(hakukohdeOid)) {
            throw new RuntimeException("Invalid search params, fix exception later");
        }
        return valintatulosDao.loadValintatuloksetForHakukohde(hakukohdeOid);
    }

    @Override
    public boolean muutoksetOvatAjantasaisia(String hakukohdeOid, List<Valintatulos> valintatulokset) {
        return !valintatulokset.stream()
                .anyMatch(valintatulos -> {
                    Valintatulos saved = valintatulosDao.loadValintatulos(hakukohdeOid, valintatulos.getValintatapajonoOid(), valintatulos.getHakemusOid());
                    Interval interval = new Interval(new DateTime(valintatulos.getRead()), DateTime.now());
                    return saved.getLogEntries() != null &&
                            saved.getLogEntries().stream().anyMatch(entry -> interval.contains(entry.getLuotu().getTime()));
                });
    }

    @Override
    public List<Valintatulos> haeHakemuksenTila(String hakemusOid) {
        if (StringUtils.isBlank(hakemusOid)) {
            throw new RuntimeException("Invalid search params, fix exception later");
        }
        return valintatulosDao.loadValintatulos(hakemusOid);
    }

    @Override
    public Hakukohde getHakukohde(String hakuOid, String hakukohdeOid) {
        return sijoitteluDao.getSijoitteluByHakuOid(hakuOid)
                .map(sijoittelu -> hakukohdeDao.getHakukohdeForSijoitteluajo(sijoittelu.getLatestSijoitteluajo().getSijoitteluajoId(), hakukohdeOid))
                .orElseThrow(() -> new RuntimeException("Sijoittelua ei löytynyt haulle: " + hakuOid));
    }

    @Override
    public void vaihdaHakemuksenTila(String hakuoid, Hakukohde hakukohde,
                                     String valintatapajonoOid, String hakemusOid,
                                     ValintatuloksenTila tila, String selite,
                                     IlmoittautumisTila ilmoittautumisTila,
                                     boolean julkaistavissa,
                                     boolean hyvaksyttyVarasijalta) {
        if (StringUtils.isBlank(hakuoid) || StringUtils.isBlank(valintatapajonoOid) || StringUtils.isBlank(hakemusOid)) {
            throw new RuntimeException("Osa parametreista puuttuu. hakuoid: " + hakuoid + " - valintatapajonoOid: " + valintatapajonoOid + " - hakemusOid: " + hakemusOid);
        }
        final Long ajoId = hakukohde.getSijoitteluajoId();
        final String hakukohdeOid = hakukohde.getOid();
        Valintatapajono valintatapajono = getValintatapajono(valintatapajonoOid, hakukohde);
        if (valintatapajono == null) {
            throw new ValintatapajonoaEiLoytynytException("Valintatapajonoa " + valintatapajonoOid + "ei löytynyt hakukohteelle " + hakukohdeOid + " haussa " + hakuoid);
        }
        Hakemus hakemus = getHakemus(hakemusOid, valintatapajono);
        // Oph-admin voi muokata aina
        // organisaatio updater voi muokata, jos hyväksytty
        String tarjoajaOid = hakukohde.getTarjoajaOid();
        if (tarjoajaOid == null || StringUtils.isBlank(tarjoajaOid)) {
            updateMissingTarjoajaOidFromTarjonta(hakukohdeOid, ajoId, hakukohde);

        }
        authorizer.checkOrganisationAccess(tarjoajaOid, SijoitteluRole.UPDATE_ROLE, SijoitteluRole.CRUD_ROLE);
        Valintatulos v = valintatulosDao.loadValintatulos(hakukohdeOid, valintatapajonoOid, hakemusOid);
        if (v != null && v.getTila().equals(tila) && v.getIlmoittautumisTila().equals(ilmoittautumisTila)
                && v.getJulkaistavissa() == julkaistavissa && v.getHyvaksyttyVarasijalta() == hyvaksyttyVarasijalta) {
            return;
        }
        if (v == null) {
            v = new Valintatulos();
            v.setHakemusOid(hakemus.getHakemusOid());
            v.setValintatapajonoOid(valintatapajono.getOid());
            v.setHakukohdeOid(hakukohde.getOid());
            v.setHakijaOid(hakemus.getHakijaOid());
            v.setHakutoive(hakemus.getPrioriteetti());
            v.setHakuOid(hakuoid);
        }
        v.setTila(tila);
        v.setIlmoittautumisTila(ilmoittautumisTila);
        v.setJulkaistavissa(julkaistavissa);
        v.setHyvaksyttyVarasijalta(hyvaksyttyVarasijalta);
        LOG.info("Asetetaan valintatuloksen tila - hakukohdeoid {}, valintatapajonooid {}, hakemusoid {}", new Object[]{hakukohdeOid, valintatapajonoOid, hakemusOid});
        LOG.info("Valintatuloksen uusi tila {}", tila);
        LogEntry logEntry = createLogEntry(tila, selite);
        v.getLogEntries().add(logEntry);
        valintatulosDao.createOrUpdateValintatulos(v);
    }

    private LogEntry createLogEntry(ValintatuloksenTila tila, String selite) {
        LogEntry logEntry = new LogEntry();
        logEntry.setLuotu(new Date());
        logEntry.setMuokkaaja(AuthorizationUtil.getCurrentUser());
        logEntry.setSelite(selite);
        if (tila == null) {
            logEntry.setMuutos("");
        } else {
            logEntry.setMuutos(tila.name());
        }
        return logEntry;
    }

    private void updateMissingTarjoajaOidFromTarjonta(String hakukohdeOid, Long ajoId, Hakukohde hakukohde) {
        try {
            Optional<String> tOid = tarjontaIntegrationService.getTarjoajaOid(hakukohde.getOid());
            if (tOid.isPresent()) {
                hakukohde.setTarjoajaOid(tOid.get());
                hakukohdeDao.persistHakukohde(hakukohde);
            } else {
                throw new RuntimeException("Hakukohteelle " + hakukohdeOid + " ei löytynyt tarjoajaOidia sijoitteluajosta: " + ajoId);
            }
        } catch (Exception e) {
            LOG.error("Tarjoajaoidin päivitys tarjonnasta epäonnistui", e);
            throw new RuntimeException("Hakukohteelle " + hakukohdeOid + " ei löytynyt tarjoajaOidia sijoitteluajosta: " + ajoId);
        }
    }

    private static Valintatapajono getValintatapajono(String valintatapajonoOid, Hakukohde hakukohde) {
        Valintatapajono valintatapajono = null;
        for (Valintatapajono v : hakukohde.getValintatapajonot()) {
            if (valintatapajonoOid.equals(v.getOid())) {
                valintatapajono = v;
                break;
            }
        }
        return valintatapajono;
    }

    private boolean checkIfOphAdmin(final Hakemus hakemus) {
        boolean ophAdmin = false;
        try {
            authorizer.checkOrganisationAccess(rootOrgOid, SijoitteluRole.CRUD_ROLE);
            ophAdmin = true;
        } catch (NotAuthorizedException nae) {
            LOG.info("Ei ophadmin");
        }
        return ophAdmin;
    }

    private static Hakemus getHakemus(final String hakemusOid, final Valintatapajono valintatapajono) {
        Hakemus hakemus = null;
        for (Hakemus h : valintatapajono.getHakemukset()) {
            if (hakemusOid.equals(h.getHakemusOid())) {
                hakemus = h;
            }
        }
        if (hakemus == null) {
            throw new HakemustaEiLoytynytException("Hakemusta ei löytynyt.");
        }
        return hakemus;
    }
}
