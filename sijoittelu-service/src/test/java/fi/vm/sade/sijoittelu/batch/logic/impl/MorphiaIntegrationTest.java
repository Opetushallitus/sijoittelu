package fi.vm.sade.sijoittelu.batch.logic.impl;

import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.sijoittelu.laskenta.service.business.impl.SijoitteluBusinessServiceImpl;
import fi.vm.sade.sijoittelu.tulos.dao.SijoitteluDao;
import fi.vm.sade.sijoittelu.tulos.dao.ValintatulosDao;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import junit.framework.Assert;

import org.junit.Before;
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
	private SijoitteluBusinessService sijoitteluService;

	@Test
	public void testSijoitteluService() {

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

        // because @PropertySource doesnt work in annotation only land
        @Bean
        PropertyPlaceholderConfigurer propConfig() {
            PropertyPlaceholderConfigurer ppc =  new PropertyPlaceholderConfigurer();
            ppc.setLocation(new ClassPathResource("common.properties"));
            return ppc;
        }
    }

}
