package fi.vm.sade.testing;

import fi.vm.sade.javautils.nio.cas.CasClient;
import fi.vm.sade.javautils.opintopolku_spring_security.Authorizer;
import fi.vm.sade.service.valintaperusteet.resource.ValintaperusteetResource;
import fi.vm.sade.service.valintaperusteet.resource.ValintaperusteetResourceV2;
import fi.vm.sade.sijoittelu.laskenta.external.resource.*;
import fi.vm.sade.sijoittelu.laskenta.mapping.SijoitteluModelMapper;
import fi.vm.sade.sijoittelu.laskenta.service.business.ValintarekisteriService;
import fi.vm.sade.valintalaskenta.tulos.logging.LaskentaAuditLog;
import fi.vm.sade.valintalaskenta.tulos.logging.LaskentaAuditLogImpl;
import fi.vm.sade.valintalaskenta.tulos.mapping.ValintalaskentaModelMapper;
import org.flywaydb.core.Flyway;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;

@TestConfiguration
@ComponentScan(basePackages = {
  "fi.vm.sade.valintalaskenta.tulos.dao",
  "fi.vm.sade.valintalaskenta.tulos.service.impl",
  "fi.vm.sade.valintalaskenta.tulos.service.impl.converters"
})
@Profile("test")
public class TestConfigurationWithMocks {

    @Bean
    public ValintaperusteetResource valintaperusteetResource() { return Mockito.mock(ValintaperusteetResource.class); }

    @Bean
    public ValintaperusteetResourceV2 valintaperusteetResourceV2() { return Mockito.mock(ValintaperusteetResourceV2.class); }

    @Bean
    public OhjausparametriResource ohjausparametriResource() { return Mockito.mock(OhjausparametriResource.class); }

    @Bean
    public HakuV1Resource hakuV1Resource() { return Mockito.mock(HakuV1Resource.class); }

    @Bean(name="KoutaInternaCasClient")
    public CasClient koutaInternalCasClient() { return Mockito.mock(CasClient.class); }

    @Bean("SijoitteluCasClient")
    public CasClient sijoitteluCasClient() { return Mockito.mock(CasClient.class); }

    @Bean
    public VirkailijaValintaTulosServiceResource virkailijaValintaTulosServiceResource() { return Mockito.mock(VirkailijaValintaTulosServiceResource.class); }

    @Bean
    public Authorizer authorizer() { return Mockito.mock(Authorizer.class); }

    @Bean
    public ValintarekisteriService valintarekisteriService() { return Mockito.mock(ValintarekisteriService.class); }

    @Bean("LaskentaAuditLog")
    public LaskentaAuditLog laskentaAuditLog() {
        return new LaskentaAuditLogImpl();
    }

    @Bean
    public ValintalaskentaModelMapper valintalaskentaModelMapper() {
        return new ValintalaskentaModelMapper();
    }

    @Bean
    public SijoitteluModelMapper sijoitteluModelMapper() {
        return new SijoitteluModelMapper();
    }

    @Bean
    public ApplicationContextGetter applicationContextGetter() {
        return new ApplicationContextGetter();
    }

}
