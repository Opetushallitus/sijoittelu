package fi.vm.sade.sijoittelu.laskenta.service.business;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.hakukohteet.LisapaikkaTapa;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.laskenta.service.it.Haku;
import fi.vm.sade.sijoittelu.laskenta.service.it.TarjontaIntegrationService;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintatulosservice.valintarekisteri.domain.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class SijoitteluajoResourcesLoader {
    private static final Logger LOG = LoggerFactory.getLogger(SijoitteluajoResourcesLoader.class);

    private final TarjontaIntegrationService tarjontaIntegrationService;
    private final ValintarekisteriService valintarekisteriService;

    @Autowired
    public SijoitteluajoResourcesLoader(TarjontaIntegrationService tarjontaIntegrationService,
                                        ValintarekisteriService valintarekisteriService) {
        this.tarjontaIntegrationService = tarjontaIntegrationService;
        this.valintarekisteriService = valintarekisteriService;
    }

    public Haku findParametersFromTarjontaAndPerformInitialValidation(String hakuOid, StopWatch stopWatch, String ajonKuvaus) {
        LOG.info(String.format("%s alkaa. Luetaan parametrit tarjonnasta ja esivalidoidaan ne", ajonKuvaus));
        stopWatch.start("Luetaan parametrit tarjonnasta ja esivalidoidaan ne");
        Haku sijoittelunParametrit = tarjontaIntegrationService.getHaku(hakuOid);
        stopWatch.stop();
        return sijoittelunParametrit;
    }

    public void asetaSijoittelunParametrit(String hakuOid, SijoitteluajoWrapper sijoitteluAjo, Haku sijoittelunParametrit) {
        sijoitteluAjo.setKKHaku(sijoittelunParametrit.isKk());
        sijoitteluAjo.setHakutoiveidenPriorisointi(sijoittelunParametrit.jarjestetytHakutoiveet);
        sijoitteluAjo.setAmkopeHaku(sijoittelunParametrit.isAmkOpe());
        if (sijoittelunParametrit.hakukierrosPaattyy != null) {
            sijoitteluAjo.setHakuKierrosPaattyy(LocalDateTime.ofInstant(sijoittelunParametrit.hakukierrosPaattyy, ZoneId.of("Europe/Helsinki")));
        }
        if (sijoittelunParametrit.valintatuloksetSiirrettavaSijoitteluunViimeistaan != null) {
            sijoitteluAjo.setKaikkiKohteetSijoittelussa(LocalDateTime.ofInstant(sijoittelunParametrit.valintatuloksetSiirrettavaSijoitteluunViimeistaan, ZoneId.of("Europe/Helsinki")));
        }
        if (sijoittelunParametrit.varasijasaannotAstuvatVoimaan != null) {
            sijoitteluAjo.setVarasijaSaannotAstuvatVoimaan(LocalDateTime.ofInstant(sijoittelunParametrit.varasijasaannotAstuvatVoimaan, ZoneId.of("Europe/Helsinki")));
        }
        if (sijoittelunParametrit.varasijatayttoPaattyy != null) {
            sijoitteluAjo.setVarasijaTayttoPaattyy(LocalDateTime.ofInstant(sijoittelunParametrit.varasijatayttoPaattyy, ZoneId.of("Europe/Helsinki")));
        }
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

    SijoitteluAjo readSijoitteluFromValintarekisteri(HakuDTO haku, String ajonKuvaus, StopWatch stopWatch) {
        LOG.info(String.format("%s : luetaan sijoittelu valintarekisteristä!", ajonKuvaus));
        stopWatch.start(String.format("%s : luetaan sijoittelu valintarekisteristä", ajonKuvaus));
        SijoitteluAjo viimeisinSijoitteluajo = readSijoitteluajoFromValintarekisteri(haku.getHakuOid());
        stopWatch.stop();
        return viimeisinSijoitteluajo;
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
}
