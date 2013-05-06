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
public class SijoitteluCase023Test {

    /**
     * 
     * 
     * @throws IOException
     */
    @Test
    public void testSijoittelu() throws IOException {
        // tee sijoittelu
        SijoitteleTyyppi t = TestHelper.xmlToObjects("testdata/sijoittelu_case_023.xml");

        SijoitteluAjo sijoitteluAjo = DomainConverter.convertToSijoitteluAjo(t.getTarjonta().getHakukohde());

        SijoitteluAlgorithmFactoryImpl h = new SijoitteluAlgorithmFactoryImpl();
        SijoitteluAlgorithm s = h.constructAlgorithm(sijoitteluAjo);
        s.start();

        // tulosta
        FileWriter fstream = new FileWriter("target/sijoittelu_case_023.sijoitteluresult");
        fstream.write(PrintHelper.tulostaSijoittelu(s));
        fstream.flush();
        fstream.close();

        // lisaa assertointi

    }

}