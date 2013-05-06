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
public class BasicSijoitteluTasasijaTest {

    /**
     * Testaa perustapaus
     * 
     * @throws IOException
     */
    @Test
    public void testSijoittelu() throws IOException {
        // tee sijoittelu
        SijoitteleTyyppi t = TestHelper.xmlToObjects("testdata/sijoittelu_basic_tasasija_case.xml");

        SijoitteluAjo sijoitteluAjo = DomainConverter.convertToSijoitteluAjo(t.getTarjonta().getHakukohde());

        SijoitteluAlgorithmFactoryImpl h = new SijoitteluAlgorithmFactoryImpl();
        SijoitteluAlgorithm s = h.constructAlgorithm(sijoitteluAjo);
        s.start();

        // tulosta
        FileWriter fstream = new FileWriter("target/sijoittelu_basic_tasasija_case.sijoitteluresult");
        fstream.write(PrintHelper.tulostaSijoittelu(s));
        fstream.flush();
        fstream.close();

        // assertoi

        // kohde1, jono1 ... arvonta, 1 hyvaksytty 3:sta, viimeinen aina
        // varalla.
        TestHelper.assertoiVainYksiJoukostaValittu(sijoitteluAjo.getHakukohteet().get(0).getHakukohde()
                .getValintatapajonot().get(0), "1.2.246.562.24.00000000001", "1.2.246.562.24.00000000002",
                "1.2.246.562.24.00000000003");

        // kohde1, jono2 ... ylitaytto, kaikki valittu
        TestHelper.assertoiAinoastaanValittu(sijoitteluAjo.getHakukohteet().get(0).getHakukohde().getValintatapajonot()
                .get(1), "1.2.246.562.24.00000000005", "1.2.246.562.24.00000000006", "1.2.246.562.24.00000000007");

        // kohde2, jono1 ... alitaytto, kukaan ei hyvaksytty
        TestHelper.assertoiKukaanEiValittu(sijoitteluAjo.getHakukohteet().get(1).getHakukohde().getValintatapajonot()
                .get(0));

    }
}