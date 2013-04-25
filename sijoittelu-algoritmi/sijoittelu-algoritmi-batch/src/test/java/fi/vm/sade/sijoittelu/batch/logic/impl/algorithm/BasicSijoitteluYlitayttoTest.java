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
 */
public class BasicSijoitteluYlitayttoTest {

	/**
	 * Testaa perustapaus
	 * 
	 * @throws IOException
	 */
	@Test
	public void testSijoittelu() throws IOException {
		// tee sijoittelu
		SijoitteleTyyppi t = TestHelper.xmlToObjects("testdata/sijoittelu_basic_ylitaytto_case.xml");

		SijoitteluAjo sijoitteluAjo = DomainConverter.convertToSijoitteluAjo(t.getTarjonta().getHakukohde());

		SijoitteluAlgorithmFactoryImpl h = new SijoitteluAlgorithmFactoryImpl();
		SijoitteluAlgorithm s = h.constructAlgorithm(sijoitteluAjo);
		s.start();

		// tulosta
		FileWriter fstream = new FileWriter("target/sijoittelu_basic_ylitaytto_case.sijoitteluresult");
		fstream.write(PrintHelper.tulostaSijoittelu(s));
		fstream.flush();
		fstream.close();

		// assertoi

		// kohde1, jono1 ... arvonta, 1 hyvaksytty 3:sta, viimeinen aina
		// varalla.
		TestHelper.assertoiAinoastaanValittu(sijoitteluAjo.getHakukohteet().get(0).getHakukohde().getValintatapajonot().get(0), "1.2.246.562.24.00000000001", "1.2.246.562.24.00000000002");
		TestHelper.assertoiAinoastaanValittu(sijoitteluAjo.getHakukohteet().get(0).getHakukohde().getValintatapajonot().get(1), "1.2.246.562.24.00000000003", "1.2.246.562.24.00000000004", "1.2.246.562.24.00000000005", "1.2.246.562.24.00000000006");

	}
}