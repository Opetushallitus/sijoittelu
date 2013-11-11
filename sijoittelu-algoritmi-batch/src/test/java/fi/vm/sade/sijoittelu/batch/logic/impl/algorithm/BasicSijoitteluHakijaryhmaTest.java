package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import de.flapdoodle.embed.process.collections.Collections;
import fi.vm.sade.service.valintatiedot.schema.HakuTyyppi;
import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


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
		HakuTyyppi t = TestHelper.xmlToObjects("testdata/sijoittelu_basic_hakijaryhma_case.xml");


        List<Hakukohde> hakukohteet = new ArrayList<Hakukohde>() ;
        for(fi.vm.sade.service.valintatiedot.schema.HakukohdeTyyppi hkt :t.getHakukohteet()) {
            Hakukohde hakukohde = DomainConverter.convertToHakukohde(hkt);
            hakukohteet.add(hakukohde);
        }

        SijoitteluAlgorithmFactoryImpl h = new SijoitteluAlgorithmFactoryImpl();
        SijoitteluAlgorithm s = h.constructAlgorithm(hakukohteet, Collections.<Valintatulos>newArrayList());
        s.start();

        System.out.println(PrintHelper.tulostaSijoittelu(s));

		// tulosta
		FileWriter fstream = new FileWriter("target/sijoittelu_basic_hakijaryhma_case.sijoitteluresult");
		fstream.write(PrintHelper.tulostaSijoittelu(s));
		fstream.flush();
		fstream.close();

		TestHelper.assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "1.2.246.562.24.00000000001", "1.2.246.562.24.00000000007", "1.2.246.562.24.00000000008");
		TestHelper.assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(1), "1.2.246.562.24.00000000002", "1.2.246.562.24.00000000003", "1.2.246.562.24.00000000004", "1.2.246.562.24.00000000005");

	}

}