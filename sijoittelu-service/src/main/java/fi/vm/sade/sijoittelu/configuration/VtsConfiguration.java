package fi.vm.sade.sijoittelu.configuration;

import fi.vm.sade.sijoittelu.laskenta.service.business.WrappedVastaanottoService;
import fi.vm.sade.valintatulosservice.ValintatulosService;
import fi.vm.sade.valintatulosservice.VastaanottoService;
import fi.vm.sade.valintatulosservice.config.*;
import fi.vm.sade.valintatulosservice.hakemus.AtaruHakemusEnricher;
import fi.vm.sade.valintatulosservice.hakemus.AtaruHakemusRepository;
import fi.vm.sade.valintatulosservice.hakemus.HakemusRepository;
import fi.vm.sade.valintatulosservice.hakemus.HakuAppRepository;
import fi.vm.sade.valintatulosservice.koodisto.CachedKoodistoService;
import fi.vm.sade.valintatulosservice.koodisto.RemoteKoodistoService;
import fi.vm.sade.valintatulosservice.ohjausparametrit.CachedOhjausparametritService;
import fi.vm.sade.valintatulosservice.ohjausparametrit.OhjausparametritService;
import fi.vm.sade.valintatulosservice.ohjausparametrit.RemoteOhjausparametritService;
import fi.vm.sade.valintatulosservice.oppijanumerorekisteri.OppijanumerorekisteriService;
import fi.vm.sade.valintatulosservice.organisaatio.CachedOrganisaatioService;
import fi.vm.sade.valintatulosservice.organisaatio.RealOrganisaatioService;
import fi.vm.sade.valintatulosservice.sijoittelu.*;
import fi.vm.sade.valintatulosservice.tarjonta.*;
import fi.vm.sade.valintatulosservice.valintarekisteri.db.impl.DbConfig;
import fi.vm.sade.valintatulosservice.valintarekisteri.db.impl.ValintarekisteriDb;
import fi.vm.sade.valintatulosservice.valintarekisteri.hakukohde.HakukohdeRecordService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Profile("!test")
@Configuration
public class VtsConfiguration {
  @Bean
  public VtsAppConfig.VtsAppConfig vtsAppConfig() {
    return new SijoitteluVtsAppConfig();
  }

  @Bean
  public WrappedVastaanottoService vastaanottoService(final VtsAppConfig.VtsAppConfig appConfig) {
    final DbConfig dbConfig = appConfig.settings().valintaRekisteriDbConfig();
    final ValintarekisteriDb valintarekisteriDb = new ValintarekisteriDb(dbConfig, false);
    final OhjausparametritService ohjausparametritService = new CachedOhjausparametritService(
        appConfig,
        new RemoteOhjausparametritService(appConfig)
    );
    final CachedOrganisaatioService organisaatioService = new CachedOrganisaatioService(new RealOrganisaatioService(appConfig));
    final CachedKoodistoService koodistoService = new CachedKoodistoService(new RemoteKoodistoService(appConfig));
    final HakuService hakuService = new CachedHakuService(
        new TarjontaHakuService(appConfig),
        new KoutaHakuService(
            appConfig,
            ohjausparametritService,
            organisaatioService,
            koodistoService
        ),
        appConfig
    );
    final HakukohdeRecordService hakukohdeRecordService = new HakukohdeRecordService(
        hakuService,
        valintarekisteriDb,
        appConfig.settings().lenientTarjontaDataParsing()
    );
    final HakemusRepository hakemusRepository = new HakemusRepository(
        new HakuAppRepository(appConfig),
        new AtaruHakemusRepository(appConfig),
        new AtaruHakemusEnricher(appConfig,
            hakuService,
            new OppijanumerorekisteriService(appConfig)
        ),
        appConfig
    );
    final SijoittelutulosService sijoittelutulosService = new SijoittelutulosService(
        new ValintarekisteriRaportointiServiceImpl(
            valintarekisteriDb,
            new ValintarekisteriValintatulosDaoImpl(valintarekisteriDb)
        ),
        ohjausparametritService,
        valintarekisteriDb,
        new ValintarekisteriSijoittelunTulosClientImpl(valintarekisteriDb)
    );
    return new WrappedVastaanottoService(
        new VastaanottoService(
            hakuService,
            hakukohdeRecordService,
            new ValintatulosService(
                valintarekisteriDb,
                sijoittelutulosService,
                hakemusRepository,
                valintarekisteriDb,
                ohjausparametritService,
                hakuService,
                valintarekisteriDb,
                hakukohdeRecordService,
                new ValintarekisteriValintatulosDaoImpl(valintarekisteriDb),
                koodistoService,
                appConfig
            ),
            valintarekisteriDb,
            ohjausparametritService,
            sijoittelutulosService,
            hakemusRepository,
            valintarekisteriDb
        )
    );
  }
}
