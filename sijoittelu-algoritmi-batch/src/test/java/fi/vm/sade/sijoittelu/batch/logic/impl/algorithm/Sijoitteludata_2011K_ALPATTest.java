package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import de.flapdoodle.embed.process.collections.Collections;
import fi.vm.sade.service.valintatiedot.schema.HakuTyyppi;
import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;


/**
 * 2011 KEVAT 2 asteen sijoittelun data.
 * 
 * Testaa sijoittelu sijoitteludata_2011K_ALPAT.xls:tä generoitua csv tiedostoa
 * vasten
 * 
 * @author Kari Kammonen
 * 
 */
public class Sijoitteludata_2011K_ALPATTest {

	/**
	 * Testaa 2011 syksyn sijoittelu Tsekkaa etta 1279425 sijoittuu oikein
	 * 
	 * @throws IOException
	 * 
	 * */
	@Test     	@Ignore
	public void testSijoittele() throws IOException {
		// tee sijoittelu
		HakuTyyppi t = csvToDomain("testdata/sijoitteludata_2011K_ALPAT.csv");
		DomainConverter f = new DomainConverter();
        List<Hakukohde> hakukohteet = new ArrayList<Hakukohde>() ;
        for(fi.vm.sade.service.valintatiedot.schema.HakukohdeTyyppi hkt : t.getHakukohteet()) {
            Hakukohde hakukohde = DomainConverter.convertToHakukohde(hkt);
            hakukohteet.add(hakukohde);
        }

        SijoitteluAlgorithmFactoryImpl h = new SijoitteluAlgorithmFactoryImpl();
        SijoitteluAlgorithm sa = h.constructAlgorithm(hakukohteet, Collections.<Valintatulos>newArrayList());

        long timestart = System.currentTimeMillis();
		sa.start();
		long timeend = System.currentTimeMillis();

		String kesto = "Sijoittelu kesti: " + (timeend - timestart) + " milliseconds = " + (((float) (timeend - timestart)) / (((float) 1000))) + " seconds";

		// tassa pitaisi assertoida jotain, mut odotetaan etta saadaan Sepolta
		// datat.

		// persist result to target
		FileWriter fstream = new FileWriter("target/sijoittelu_2011K_ALPAT.sijoitteluresult");
		fstream.write(PrintHelper.tulostaSijoittelu(sa));
		fstream.write(kesto);
		fstream.flush();
		fstream.close();
	}

	/**
	 * Luo sijoittelutyyppi sijoitteludataexcel211 tiedostosta
	 * 
	 * @param filename
	 * @return
	 */
	private HakuTyyppi csvToDomain(String filename) {
		try {
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
			InputStreamReader r = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(r);

            HakuTyyppi sijoitteleTyyppi = new HakuTyyppi();
			DomainBuilder builder = new DomainBuilder(sijoitteleTyyppi);

			String strLine = br.readLine();// first line, always headers and
											// boring stuff
			while ((strLine = br.readLine()) != null) {
				// break comma separated line using ";"
				StringTokenizer st = new StringTokenizer(strLine, ";");
				String hakijanro = st.nextToken();
				int prioriteetti = Integer.parseInt(st.nextToken());
				String linjannimi = st.nextToken(); // tätä ei tarvita
				String hakukohdeId = st.nextToken();
				String aloituspaikatStr = st.nextToken();
				String pisteStr = st.nextToken();

				pisteStr = pisteStr.replace(',', '.'); // make sure decimal
														// separator is a dot
				float pisteet = Float.parseFloat(pisteStr);
				int aloituspaikat = Integer.parseInt(aloituspaikatStr);

				builder.addRow(hakijanro, prioriteetti, linjannimi, hakukohdeId, aloituspaikat, pisteet, null);

			}
			return sijoitteleTyyppi;

		} catch (IOException e) {
			throw new RuntimeException(e);
		} // lets skip
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
		HakuTyyppi st = this.csvToDomain("testdata/sijoitteludata_2011K_ALPAT.csv");
		String s = TestHelper.objectsToXml(st);
		FileWriter fstream = new FileWriter("src/test/resources/testdata/sijoitteludata_2011K_ALPAT.xml");
		fstream.write(s);
		fstream.flush();
		fstream.close();
	}

}