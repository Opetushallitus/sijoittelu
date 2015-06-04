package fi.vm.sade.sijoittelu.batch.logic.impl;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.core.LoadStrategyEnum;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import de.flapdoodle.embed.process.collections.Collections;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.PrintHelper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithm;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithmFactoryImpl;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.laskenta.external.resource.HakuV1Resource;
import fi.vm.sade.sijoittelu.laskenta.external.resource.OhjausparametriResource;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriDTO;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.sijoittelu.laskenta.service.it.TarjontaIntegrationService;
import fi.vm.sade.sijoittelu.tulos.dao.HakukohdeDao;
import fi.vm.sade.sijoittelu.tulos.dao.SijoitteluDao;
import fi.vm.sade.sijoittelu.tulos.dao.ValiSijoitteluDao;
import fi.vm.sade.sijoittelu.tulos.dao.ValintatulosDao;
import fi.vm.sade.tarjonta.service.resources.v1.dto.HakuV1RDTO;
import fi.vm.sade.tarjonta.service.resources.v1.dto.ResultV1RDTO;
import fi.vm.sade.valintalaskenta.domain.dto.HakukohdeDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintalaskenta.tulos.service.impl.ValintatietoService;
import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Kari Kammonen
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-sijoittelu-batch-mongo.xml" })
@ActiveProfiles()
public class MorphiaIntegrationTest {

	@Autowired
	private ValintatulosDao valintatulosDao;

    @Autowired
    private SijoitteluDao sijoitteluDao;

    @Autowired
    private HakukohdeDao hakukohdeDao;

    @Autowired
    private ValiSijoitteluDao valisijoitteluDao;

	@Autowired
	private SijoitteluBusinessService sijoitteluService;


    @Autowired
    private TarjontaIntegrationService tarjontaIntegrationService;

    @Autowired
    private ValintatietoService valintatietoService;

    @Autowired
    private ApplicationContext applicationContext;

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("test");

	@Test
	public void testSijoitteluService() {

        tarjontaIntegrationService = mock(TarjontaIntegrationService.class);

        ReflectionTestUtils.setField(sijoitteluService,
                "tarjontaIntegrationService",
                tarjontaIntegrationService);


        String hakuJson = "{\n" +
                "    \"hakukausiUri\": \"kausi_s#1\", \n" +
                "    \"hakukausiVuosi\": 2014, \n" +
                "    \"hakutapaUri\": \"hakutapa_01#1\", \n" +
                "    \"hakutyyppiUri\": \"hakutyyppi_01#1\", \n" +
                "    \"kohdejoukkoUri\": \"haunkohdejoukko_11#1\", \n" +
                "    \"modifiedBy\": \"1.2.246.562.24.15473337696\", \n" +
                "    \"oid\": \"1.2.246.562.29.92175749016\"\n" +
                "  }";
        HakuV1RDTO haku = new GsonBuilder().create().fromJson(hakuJson,  new TypeToken<HakuV1RDTO>() {
        }.getType());

        ResultV1RDTO<HakuV1RDTO> dto = new ResultV1RDTO<>();
        dto.setResult(haku);

        when(tarjontaIntegrationService.getHaunKohdejoukko(anyString())).thenReturn(Optional.of("haunkohdejoukko_11"));

        String json = "{ \"target\": \"1.2.246.562.29.173465377510\", \"__modified__\": 1416309364472, \"__modifiedBy__\": \"1.2.246.562.24.47840234552\", \"PH_TJT\": {\"date\": null}, \"PH_HKLPT\": {\"date\": null}, \"PH_HKMT\": {\"date\": null}, \"PH_KKM\": { \"dateStart\": null, \"dateEnd\": null }, \"PH_HVVPTP\": {\"date\": null}, \"PH_KTT\": { \"dateStart\": null, \"dateEnd\": null }, \"PH_OLVVPKE\": { \"dateStart\": null, \"dateEnd\": null }, \"PH_VLS\": { \"dateStart\": null, \"dateEnd\": null }, \"PH_SS\": { \"dateStart\": null, \"dateEnd\": null }, \"PH_JKLIP\": {\"date\": null}, \"PH_HKP\": {\"date\": 1416866395389}, \"PH_VTSSV\": {\"date\": 1416866395389}, \"PH_VSSAV\": {\"date\": 1416866458888}, \"PH_VTJH\": { \"dateStart\": null, \"dateEnd\": null }, \"PH_EVR\": {\"date\": null}, \"PH_OPVP\": {\"date\": null}, \"PH_HPVOA\": {\"date\": null}, \"PH_IP\": {\"date\": null} }";

        when(tarjontaIntegrationService.getHaunParametrit(anyString())).thenReturn(new GsonBuilder().create().fromJson(json, ParametriDTO.class));

		HakuDTO st = new HakuDTO();

		st.setHakuOid("testihakuoidi");

		sijoitteluService.sijoittele(st, new HashSet<>());
		Sijoittelu sijoittelu = sijoitteluDao.getSijoitteluByHakuOid("testihakuoidi").get();
		Assert.assertNotNull(sijoittelu);
		Assert.assertEquals(sijoittelu.getHakuOid(), "testihakuoidi");

	}

    @Configuration
    @ComponentScan("fi.vm.sade.sijoittelu.laskenta.service.business.impl")
    static class someConfig {

        @Bean
        PropertyPlaceholderConfigurer propConfig() {
            PropertyPlaceholderConfigurer ppc =  new PropertyPlaceholderConfigurer();
            ppc.setLocation(new ClassPathResource("common.properties"));
            return ppc;
        }
    }

}
