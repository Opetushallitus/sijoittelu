package fi.vm.sade.sijoittelu.batch.logic.impl;

import java.util.Random;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fi.vm.sade.service.sijoittelu.types.SijoitteleTyyppi;
import fi.vm.sade.sijoittelu.batch.dao.Dao;
import fi.vm.sade.sijoittelu.batch.logic.SijoitteluBusinessService;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.TestHelper;
import fi.vm.sade.sijoittelu.batch.logic.impl.itutil.FlapdoodleMongoDbTest;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.tarjonta.service.types.HakuTyyppi;

/**
 *
 * @author Kari Kammonen
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-sijoittelu-batch-mongo.xml" })
@ActiveProfiles()
public class SijoitteleAndPersistIntegrationTest extends FlapdoodleMongoDbTest {

	@Autowired
	private SijoitteluBusinessService sijoitteluService;

	@Autowired
	private Dao dao;

	@Test
	@Ignore
	public void testSimple() {
		SijoitteleTyyppi t = TestHelper
				.xmlToObjects("testdata/sijoittelu_basic_case.xml");
		t.setSijoitteluId(String.valueOf(222L));
		sijoitteluService.sijoittele(t);
		SijoitteluAjo ajo = dao.loadSijoitteluajo(222L);
		Assert.assertTrue(ajo.getHakukohteet().get(1).getHakukohde()
				.getValintatapajonot().get(0).getHakemukset().get(2).getTila() == HakemuksenTila.HYVAKSYTTY);
	}

	@Test
	@Ignore
	public void testLargeFile() {
		Random r = new Random();
		Long longValue = r.nextLong();

		SijoitteleTyyppi st = TestHelper
				.xmlToObjects("testdata/sijoitteludata_2011S_ALPAT.xml");
		HakuTyyppi ht = new HakuTyyppi();
		ht.setSijoittelu(true);
		ht.setOid("sijoitteludata_2011S_ALPAT.xml");
		st.setSijoitteluId(String.valueOf(longValue));
		st.getTarjonta().setHaku(ht);
		sijoitteluService.sijoittele(st);

		// assert results
		SijoitteluAjo result = dao.loadSijoitteluajo(longValue);
		Assert.assertEquals(3214, result.getHakukohteet().size());
	}

}
