package fi.vm.sade.sijoittelu.laskenta.service.business;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.hakukohteet.LisapaikkaTapa;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriDTO;
import fi.vm.sade.sijoittelu.laskenta.service.it.TarjontaIntegrationService;
import fi.vm.sade.sijoittelu.laskenta.util.HakuUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
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


    @Autowired
    public SijoitteluajoResourcesLoader(TarjontaIntegrationService tarjontaIntegrationService) {
        this.tarjontaIntegrationService = tarjontaIntegrationService;
    }

    SijoittelunParametrit findParametersFromTarjontaAndPerformInitialValidation(String hakuOid, StopWatch stopWatch) {
        stopWatch.start("Luetaan parametrit tarjonnasta ja esivalidoidaan ne");
        SijoittelunParametrit sijoittelunParametrit = findParametersFromTarjontaAndPerformInitialValidation(hakuOid);
        stopWatch.stop();
        return sijoittelunParametrit;
    }

    public void asetaSijoittelunParametrit(String hakuOid, SijoitteluajoWrapper sijoitteluAjo, SijoittelunParametrit sijoittelunParametrit) {
        try {
            setOptionalHakuAttributes(hakuOid, sijoitteluAjo);
            setParametersFromTarjonta(sijoitteluAjo, sijoittelunParametrit);
            sijoitteluAjo.setLisapaikkaTapa(LisapaikkaTapa.TAPA1);
            LOG.info("Sijoittelun ohjausparametrit asetettu haulle {}. onko korkeakouluhaku: {}, " +
                    "kaikki kohteet sijoittelussa: {}, hakukierros päätty: {}, varasijasäännöt astuvat voimaan: {}, " +
                    "varasijasäännöt voimassa: {}, sijoiteltu ilman varasijasääntöjä niiden ollessa voimassa: {}, käytettävä lisäpaikkatapa: {}",
                hakuOid,
                sijoitteluAjo.isKKHaku(),
                sijoitteluAjo.getKaikkiKohteetSijoittelussa(),
                sijoitteluAjo.getHakuKierrosPaattyy(),
                sijoitteluAjo.getVarasijaSaannotAstuvatVoimaan(),
                sijoitteluAjo.varasijaSaannotVoimassa(),
                sijoitteluAjo.onkoKaikkiJonotSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa(),
                sijoitteluAjo.getLisapaikkaTapa());
        } catch (IllegalStateException e) {
            throw new RuntimeException(String.format("Sijoittelua haulle %s ei voida suorittaa", hakuOid), e);
        }
    }

    public SijoittelunParametrit findParametersFromTarjontaAndPerformInitialValidation(String hakuOid) {
        ParametriDTO parametri;
        try {
            parametri = tarjontaIntegrationService.getHaunParametrit(hakuOid);
        } catch (Exception e) {
            throw new IllegalStateException("tarjonnasta ei saatu haun ohjausparametreja");
        }
        LocalDateTime hakukierrosPaattyy = Optional.ofNullable(parametri)
            .flatMap(p -> Optional.ofNullable(p.getPH_HKP()))
            .flatMap(p -> Optional.ofNullable(p.getDate()))
            .map(SijoitteluajoResourcesLoader::fromTimestamp)
            .orElseThrow(() ->
                new IllegalStateException("ohjausparametria PH_HKP (hakukierros päättyy) parametria ei ole asetettu"));

        Optional<LocalDateTime> kaikkiKohteetSijoittelussa = Optional.ofNullable(parametri)
            .flatMap(p -> Optional.ofNullable(p.getPH_VTSSV()))
            .flatMap(p -> Optional.ofNullable(p.getDate()))
            .map(SijoitteluajoResourcesLoader::fromTimestamp);

        if (kaikkiKohteetSijoittelussa.isPresent() && hakukierrosPaattyy.isBefore(kaikkiKohteetSijoittelussa.get())) {
            throw new IllegalStateException("hakukierros on asetettu päättymään " + hakukierrosPaattyy +
                " ennen kuin kaikkien kohteiden tulee olla sijoittelussa " + kaikkiKohteetSijoittelussa.get());
        }
        if (hakukierrosPaattyy.isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("hakukierros on päättynyt");
        }
        return new SijoittelunParametrit(parametri);
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
        sijoitteluAjo.setAmkopeHaku(isKKHaku && kohdejoukonTarkenne.map(SijoitteluajoResourcesLoader::isAmmatillinenOpettajakoulutus).orElse(false));
    }


    private static void setParametersFromTarjonta(SijoitteluajoWrapper sijoitteluAjo, SijoittelunParametrit sijoittelunParametrit) {
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

    private static boolean isAmmatillinenOpettajakoulutus(String haunKohdeJoukonTarkenne) {
        return amkopeKohdejoukonTarkenteet.contains(haunKohdeJoukonTarkenne);
    }

    private static LocalDateTime fromTimestamp(Long timestamp) {
        return LocalDateTime.ofInstant(new Date(timestamp).toInstant(), ZoneId.systemDefault());
    }

    static class SijoittelunParametrit {
        final LocalDateTime hakukierrosPaattyy;
        final Optional<LocalDateTime> kaikkiKohteetSijoittelussa;
        final Optional<LocalDateTime> varasijasaannotAstuvatVoimaan;
        final Optional<LocalDateTime> varasijatayttoPaattyy;

        private SijoittelunParametrit(ParametriDTO tarjontaParametriDto) {
            hakukierrosPaattyy = Optional.ofNullable(tarjontaParametriDto)
                .flatMap(p -> Optional.ofNullable(p.getPH_HKP()))
                .flatMap(p -> Optional.ofNullable(p.getDate()))
                .map(SijoitteluajoResourcesLoader::fromTimestamp)
                .orElseThrow(() -> new IllegalStateException("ohjausparametria PH_HKP (hakukierros päättyy) parametria ei ole asetettu"));

            kaikkiKohteetSijoittelussa = Optional.ofNullable(tarjontaParametriDto)
                .flatMap(p -> Optional.ofNullable(p.getPH_VTSSV()))
                .flatMap(p -> Optional.ofNullable(p.getDate()))
                .map(SijoitteluajoResourcesLoader::fromTimestamp);
            varasijasaannotAstuvatVoimaan = Optional.ofNullable(tarjontaParametriDto)
                .flatMap(p -> Optional.ofNullable(p.getPH_VSSAV()))
                .flatMap(p -> Optional.ofNullable(p.getDate()))
                .map(SijoitteluajoResourcesLoader::fromTimestamp);
            varasijatayttoPaattyy = Optional.ofNullable(tarjontaParametriDto)
                .flatMap(p -> Optional.ofNullable(p.getPH_VSTP()))
                .flatMap(p -> Optional.ofNullable(p.getDate()))
                .map(SijoitteluajoResourcesLoader::fromTimestamp);
        }
    }

}
