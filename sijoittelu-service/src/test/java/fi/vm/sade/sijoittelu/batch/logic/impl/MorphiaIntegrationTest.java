package fi.vm.sade.sijoittelu.batch.logic.impl;

import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.sijoittelu.tulos.dao.SijoitteluDao;
import fi.vm.sade.sijoittelu.tulos.dao.ValintatulosDao;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fi.vm.sade.sijoittelu.domain.Sijoittelu;

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
		Sijoittelu sijoittelu = sijoitteluDao.getSijoitteluByHakuOid("testihakuoidi");
		Assert.assertNotNull(sijoittelu);
		Assert.assertEquals(sijoittelu.getHakuOid(), "testihakuoidi");
	}

}
