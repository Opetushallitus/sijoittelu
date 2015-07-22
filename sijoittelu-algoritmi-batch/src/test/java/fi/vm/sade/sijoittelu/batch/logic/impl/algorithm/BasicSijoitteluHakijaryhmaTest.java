package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import com.google.gson.GsonBuilder;
import de.flapdoodle.embed.process.collections.Collections;
import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


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
    //@Ignore
	@Test
	public void testSijoittelu() throws IOException {
		// tee sijoittelu
        HakuDTO t = TestHelper.readHakuDTOFromJson("testdata/sijoittelu_basic_hakijaryhma_case.json");


        List<Hakukohde> hakukohteet = t.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());

        SijoitteluAlgorithmFactoryImpl h = new SijoitteluAlgorithmFactoryImpl();
        SijoitteluAlgorithm s = h.constructAlgorithm(hakukohteet, Collections.<Valintatulos>newArrayList());
        s.start();

		PrintHelper.tallennaSijoitteluTiedostoon(s, "target/sijoittelu_basic_hakijaryhma_case.sijoitteluresult");

		System.err.println(new GsonBuilder().setPrettyPrinting().create().toJson(hakukohteet));
		TestHelper.assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "1.2.246.562.24.00000000001", "1.2.246.562.24.00000000007");
		TestHelper.assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(1), "1.2.246.562.24.00000000004", "1.2.246.562.24.00000000002", "1.2.246.562.24.00000000003", "1.2.246.562.24.00000000008");
	}

	@Test
	public void testSijoitteluEiKaytetaHakijaRyhmaanKuuluvia() throws IOException {
		// tee sijoittelu
		HakuDTO t = TestHelper.readHakuDTOFromJson("testdata/sijoittelu_basic_hakijaryhma_ei_kayteta_ryhmaan_kuuluvia.json");


		List<Hakukohde> hakukohteet = t.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());

		SijoitteluAlgorithmFactoryImpl h = new SijoitteluAlgorithmFactoryImpl();
		SijoitteluAlgorithm s = h.constructAlgorithm(hakukohteet, Collections.<Valintatulos>newArrayList());
		s.start();

		PrintHelper.tallennaSijoitteluTiedostoon(s, "target/sijoittelu_basic_hakijaryhma_ei_ryhmaan_kuuluvia_case.sijoitteluresult");

		System.err.println(new GsonBuilder().setPrettyPrinting().create().toJson(hakukohteet));
		TestHelper.assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "1.2.246.562.24.00000000001", "1.2.246.562.24.00000000007");
		TestHelper.assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(1), "1.2.246.562.24.00000000008", "1.2.246.562.24.00000000002", "1.2.246.562.24.00000000003", "1.2.246.562.24.00000000004");

	}
}