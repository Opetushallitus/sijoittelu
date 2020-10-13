package fi.vm.sade.sijoittelu.laskenta.service.business;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.hakukohteet.LisapaikkaTapa;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.laskenta.service.it.Haku;
import fi.vm.sade.sijoittelu.laskenta.service.it.TarjontaIntegrationService;
import fi.vm.sade.sijoittelu.laskenta.util.HakuUtil;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintatulosservice.valintarekisteri.domain.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class SijoitteluajoResourcesLoader {
    private static final Logger LOG = LoggerFactory.getLogger(SijoitteluajoResourcesLoader.class);

    private static final String KK_KOHDEJOUKKO = "haunkohdejoukko_12";
    private static final Set<String> amkopeKohdejoukonTarkenteet = new HashSet<>() {{
        add("haunkohdejoukontarkenne_2");
        add("haunkohdejoukontarkenne_4");
        add("haunkohdejoukontarkenne_5");
    }};

    private final TarjontaIntegrationService tarjontaIntegrationService;
    private final ValintarekisteriService valintarekisteriService;

    @Autowired
    public SijoitteluajoResourcesLoader(TarjontaIntegrationService tarjontaIntegrationService,
                                        ValintarekisteriService valintarekisteriService) {
        this.tarjontaIntegrationService = tarjontaIntegrationService;
        this.valintarekisteriService = valintarekisteriService;
    }

    Haku findParametersFromTarjontaAndPerformInitialValidation(String hakuOid, StopWatch stopWatch, String ajonKuvaus) {
        LOG.info(String.format("%s alkaa. Luetaan parametrit tarjonnasta ja esivalidoidaan ne", ajonKuvaus));
        stopWatch.start("Luetaan parametrit tarjonnasta ja esivalidoidaan ne");
        Haku sijoittelunParametrit = findParametersFromTarjontaAndPerformInitialValidation(hakuOid);
        stopWatch.stop();
        return sijoittelunParametrit;
    }

    public void asetaSijoittelunParametrit(String hakuOid, SijoitteluajoWrapper sijoitteluAjo, Haku sijoittelunParametrit) {
        populateHakuAttributesFromTarjonta(sijoitteluAjo, sijoittelunParametrit);
        setParametersFromTarjonta(sijoitteluAjo, sijoittelunParametrit);
        sijoitteluAjo.setLisapaikkaTapa(LisapaikkaTapa.TAPA1);
        LOG.info("Sijoittelun ohjausparametrit asetettu haulle {}. onko korkeakouluhaku: {}, " +
                        "kaikki kohteet sijoittelussa: {}, hakukierros päätty: {}, varasijasäännöt astuvat voimaan: {}, " +
                        "varasijasäännöt voimassa: {}, sijoiteltu ilman varasijasääntöjä niiden ollessa voimassa: {}, käytettävä lisäpaikkatapa: {}",
                hakuOid,
                sijoitteluAjo.isKKHaku(),
                sijoittelunParametrit.valintatuloksetSiirrettavaSijoitteluunViimeistaan,
                sijoittelunParametrit.hakukierrosPaattyy,
                sijoittelunParametrit.varasijasaannotAstuvatVoimaan,
                sijoitteluAjo.varasijaSaannotVoimassa(),
                sijoitteluAjo.onkoKaikkiJonotSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa(),
                sijoitteluAjo.getLisapaikkaTapa());
    }

    public Haku findParametersFromTarjontaAndPerformInitialValidation(String hakuOid) {
        return tarjontaIntegrationService.getHaku(hakuOid);
    }

    SijoitteluAjo readSijoitteluFromValintarekisteri(HakuDTO haku, String ajonKuvaus, StopWatch stopWatch) {
        LOG.info(String.format("%s : luetaan sijoittelu valintarekisteristä!", ajonKuvaus));
        stopWatch.start(String.format("%s : luetaan sijoittelu valintarekisteristä", ajonKuvaus));
        SijoitteluAjo viimeisinSijoitteluajo = readSijoitteluajoFromValintarekisteri(haku.getHakuOid());
        stopWatch.stop();
        return viimeisinSijoitteluajo;
    }

    private static void populateHakuAttributesFromTarjonta(SijoitteluajoWrapper sijoitteluAjo, Haku haku) {
        boolean isKKHaku = haku.haunkohdejoukkoUri.startsWith(KK_KOHDEJOUKKO + "#");
        sijoitteluAjo.setKKHaku(isKKHaku);
        sijoitteluAjo.setHakutoiveidenPriorisointi(haku.jarjestetytHakutoiveet);

        Optional<String> kohdejoukonTarkenne = HakuUtil.gethaunKohdejoukonTarkenne(haku);
        sijoitteluAjo.setAmkopeHaku(isKKHaku && kohdejoukonTarkenne.map(SijoitteluajoResourcesLoader::isAmmatillinenOpettajakoulutus).orElse(false));
    }


    private static void setParametersFromTarjonta(SijoitteluajoWrapper sijoitteluAjo, Haku haku) {
        if (haku.hakukierrosPaattyy != null) {
            sijoitteluAjo.setHakuKierrosPaattyy(LocalDateTime.ofInstant(haku.hakukierrosPaattyy, ZoneId.of("Europe/Helsinki")));
        }
        if (haku.valintatuloksetSiirrettavaSijoitteluunViimeistaan != null) {
            sijoitteluAjo.setKaikkiKohteetSijoittelussa(LocalDateTime.ofInstant(haku.valintatuloksetSiirrettavaSijoitteluunViimeistaan, ZoneId.of("Europe/Helsinki")));
        }
        if (haku.varasijasaannotAstuvatVoimaan != null) {
            sijoitteluAjo.setVarasijaSaannotAstuvatVoimaan(LocalDateTime.ofInstant(haku.varasijasaannotAstuvatVoimaan, ZoneId.of("Europe/Helsinki")));
        }
        if (haku.varasijatayttoPaattyy != null) {
            sijoitteluAjo.setVarasijaTayttoPaattyy(LocalDateTime.ofInstant(haku.varasijatayttoPaattyy, ZoneId.of("Europe/Helsinki")));
        }
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

    private static boolean isAmmatillinenOpettajakoulutus(String haunKohdeJoukonTarkenne) {
        return amkopeKohdejoukonTarkenteet.contains(haunKohdeJoukonTarkenne);
    }
}
