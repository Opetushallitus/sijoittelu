package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import java.io.FileWriter;
import java.io.IOException;

import org.junit.Test;

import fi.vm.sade.service.sijoittelu.types.SijoitteleTyyppi;
import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;

/**
 * 
 * @author Kari Kammonen
 * 
 *         Note! this test will yield different result on every run because of
 *         randomization
 * 
 */
public class BasicSijoitteluHakijaryhmaTest {

	/**
	 * Testaa perustapaus
	 * 
	 * @throws IOException
	 */
	@Test
	public void testSijoittelu() throws IOException {
		// tee sijoittelu
		SijoitteleTyyppi t = TestHelper.xmlToObjects("testdata/sijoittelu_basic_hakijaryhma_case.xml");

		SijoitteluAjo sijoitteluAjo = DomainConverter.convertToSijoitteluAjo(t.getTarjonta().getHakukohde());

		SijoitteluAlgorithmFactoryImpl h = new SijoitteluAlgorithmFactoryImpl();
		SijoitteluAlgorithm s = h.constructAlgorithm(sijoitteluAjo);
		s.start();

		System.out.println(PrintHelper.tulostaSijoittelu(s));

		// tulosta
		FileWriter fstream = new FileWriter("target/sijoittelu_basic_hakijaryhma_case.sijoitteluresult");
		fstream.write(PrintHelper.tulostaSijoittelu(s));
		fstream.flush();
		fstream.close();

		TestHelper.assertoiAinoastaanValittu(sijoitteluAjo.getHakukohteet().get(0).getHakukohde().getValintatapajonot().get(0), "1.2.246.562.24.00000000001", "1.2.246.562.24.00000000007", "1.2.246.562.24.00000000008");
		TestHelper.assertoiAinoastaanValittu(sijoitteluAjo.getHakukohteet().get(0).getHakukohde().getValintatapajonot().get(1), "1.2.246.562.24.00000000002", "1.2.246.562.24.00000000003", "1.2.246.562.24.00000000004", "1.2.246.562.24.00000000005");

	}

}