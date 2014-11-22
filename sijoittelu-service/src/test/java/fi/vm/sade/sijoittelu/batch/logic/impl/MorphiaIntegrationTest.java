package fi.vm.sade.sijoittelu.batch.logic.impl;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.sijoittelu.tulos.dao.SijoitteluDao;
import fi.vm.sade.sijoittelu.tulos.dao.ValiSijoitteluDao;
import fi.vm.sade.sijoittelu.tulos.dao.ValintatulosDao;
import fi.vm.sade.tarjonta.service.resources.v1.HakuV1Resource;
import fi.vm.sade.tarjonta.service.resources.v1.dto.HakuV1RDTO;
import fi.vm.sade.tarjonta.service.resources.v1.dto.ResultV1RDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import org.springframework.test.util.ReflectionTestUtils;

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
    private ValiSijoitteluDao valisijoitteluDao;

	@Autowired
	private SijoitteluBusinessService sijoitteluService;

    @Autowired
    private HakuV1Resource hakuV1Resource;

	@Test
	public void testSijoitteluService() {

        hakuV1Resource = mock(HakuV1Resource.class);

        ReflectionTestUtils.setField(sijoitteluService,
                "hakuV1Resource",
                hakuV1Resource);

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

        when(hakuV1Resource.findByOid(anyString())).thenReturn(dto);

		HakuDTO st = new HakuDTO();

		st.setHakuOid("testihakuoidi");

		sijoitteluService.sijoittele(st);
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
