package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Ignore;
import org.junit.Test;

import fi.vm.sade.service.sijoittelu.schema.TarjontaTyyppi;
import fi.vm.sade.service.sijoittelu.types.SijoitteleTyyppi;
import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.tarjonta.service.types.HakuTyyppi;

/**
 * 2011 Syksyn yhteishaku
 * 
 * @author Kari Kammonen
 * 
 */
public class Sijoitteludata_2011S_ALPATTest {

	@Test
	public void testSijoittele() throws IOException {
		SijoitteleTyyppi t = TestHelper.xmlToObjects("testdata/sijoitteludata_2011S_ALPAT.xml");

		SijoitteluAjo ajo = DomainConverter.convertToSijoitteluAjo(t.getTarjonta().getHakukohde());
		SijoitteluAlgorithmFactory f = new SijoitteluAlgorithmFactoryImpl();
		SijoitteluAlgorithm alg = f.constructAlgorithm(ajo);

		long timestart = System.currentTimeMillis();
		long freeMemBefore = Runtime.getRuntime().freeMemory();
		alg.start();
		long timeend = System.currentTimeMillis();
		long freeMemAfter = Runtime.getRuntime().freeMemory();

		String kesto = "Sijoittelu kesti: " + (timeend - timestart) + " milliseconds = " + (((float) (timeend - timestart)) / (((float) 1000))) + " seconds\n";

		String muisti = "Free mem before: " + freeMemBefore + " and after: " + freeMemAfter + " difference: " + ((freeMemBefore - freeMemAfter) / (1024 * 1024)) + "MB\n";

		// tassa pitaisi assertoida jotain, mut odotetaan etta saadaan Sepolta
		// datat.

		// persist result to target
		FileWriter fstream = new FileWriter("target/sijoittelu_2011S_ALPAT.sijoitteluresult");
		fstream.write(PrintHelper.tulostaSijoittelu(alg));
		fstream.write(kesto);
		fstream.write(muisti);
		fstream.flush();
		fstream.close();
	}

	/**
	 * Muista lisata @XmlRootElement annotaatio SijoitteleTyyppi elementtiin.
	 * Ennen kuin ajat taman.
	 * 
	 * @throws IOException
	 */
	@Test
	@Ignore
	public void testToXml() throws IOException {

		SijoitteleTyyppi sijoitteleTyyppi = new SijoitteleTyyppi();
		sijoitteleTyyppi.setTarjonta(new TarjontaTyyppi());
		sijoitteleTyyppi.getTarjonta().setHaku(new HakuTyyppi());
		sijoitteleTyyppi.getTarjonta().getHaku().setSijoittelu(true);
		DomainBuilder builder = new DomainBuilder(sijoitteleTyyppi);

		System.out.println("Tiedosto: sijoitteludata_2011S_ALPAT_vertailu.xml");
		readFile(builder, "testdata/sijoitteludata_2011S_ALPAT_vertailu.xml");

		System.out.println("Tiedosto: sijoitteludata_2011S_ALPAT_vertailu_2.xml");
		readFile(builder, "testdata/sijoitteludata_2011S_ALPAT_vertailu_2.xml");

		System.out.println("Tiedosto: sijoitteludata_2011S_ALPAT_vertailu_3.xml");
		readFile(builder, "testdata/sijoitteludata_2011S_ALPAT_vertailu_3.xml");

		System.out.println("Tiedosto: sijoitteludata_2011S_ALPAT_vertailu_4.xml");
		readFile(builder, "testdata/sijoitteludata_2011S_ALPAT_vertailu_4.xml");

		String s = TestHelper.objectsToXml(sijoitteleTyyppi);
		// System.out.println(s);
		FileWriter fstream = new FileWriter("src/test/resources/testdata/sijoitteludata_2011S_ALPAT.xml");
		fstream.write(s);
		fstream.flush();
		fstream.close();
	}

	private void readFile(DomainBuilder builder, String filename) throws IOException {
		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
		InputStreamReader r = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(r);

		String strLine = br.readLine();// first line, always headers and boring
										// stuff
		while ((strLine = br.readLine()) != null) {
			if (strLine.contains("<rows>")) {
				break;
			}
		}
		while (true) {
			String line = br.readLine(); // skippaa alkurivi
			if (line.contains("</rows>")) {
				break;
			}
			String hakijanro = processRow(br.readLine());
			String prioriteetti = processRow(br.readLine());
			String linja = processRow(br.readLine());
			String hakukohdeId = processRow(br.readLine());
			String aloituspaikat = processRow(br.readLine());
			String pisteet = processRow(br.readLine());
			String valinnanTila = processRow(br.readLine());

			br.readLine(); // skippaa loppurivi

			String tila = null;
			if (!valinnanTila.contains("oleva")) {
				tila = "HYLATTY";
			}

			// System.out.println("RIVI: " + hakijanro+
			// prioriteetti+linja+hakukohdeId+aloituspaikat+pisteet+valinnanTila);
			builder.addRow(hakijanro, stringAsInt(prioriteetti), linja, hakukohdeId, stringAsInt(aloituspaikat), stringAsFloat(pisteet), tila);
		}
	}

	private String processRow(String row) {
		String subString = row.substring(row.indexOf('>') + 1);
		return subString.substring(0, subString.indexOf('<'));
	}

	private Float stringAsFloat(String string) {
		return Float.parseFloat(string.replace(',', '.'));
	}

	private Integer stringAsInt(String string) {
		return Integer.parseInt(string.replace(',', '.'));
	}

}
