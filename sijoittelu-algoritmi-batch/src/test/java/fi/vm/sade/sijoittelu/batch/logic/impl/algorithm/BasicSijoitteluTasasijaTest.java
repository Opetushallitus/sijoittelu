package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import de.flapdoodle.embed.process.collections.Collections;
import fi.vm.sade.service.sijoittelu.types.SijoitteleTyyppi;
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

        List<Hakukohde> hakukohteet = new ArrayList<Hakukohde>() ;
        for(fi.vm.sade.service.valintatiedot.schema.HakukohdeTyyppi hkt :t.getTarjonta().getHakukohde()) {
            Hakukohde hakukohde = DomainConverter.convertToHakukohde(hkt);
            hakukohteet.add(hakukohde);
        }


        SijoitteluAlgorithmFactoryImpl h = new SijoitteluAlgorithmFactoryImpl();
        SijoitteluAlgorithm s = h.constructAlgorithm(hakukohteet, Collections.<Valintatulos>newArrayList());
        s.start();

        // tulosta
        FileWriter fstream = new FileWriter("target/sijoittelu_basic_tasasija_case.sijoitteluresult");
        fstream.write(PrintHelper.tulostaSijoittelu(s));
        fstream.flush();
        fstream.close();

        // assertoi

        // kohde1, jono1 ... arvonta, 1 hyvaksytty 3:sta, viimeinen aina
        // varalla.
        TestHelper.assertoiVainYksiJoukostaValittu(hakukohteet.get(0).getValintatapajonot().get(0), "1.2.246.562.24.00000000001", "1.2.246.562.24.00000000002",
                "1.2.246.562.24.00000000003");

        // kohde1, jono2 ... ylitaytto, kaikki valittu
        TestHelper.assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot()
                .get(1), "1.2.246.562.24.00000000005", "1.2.246.562.24.00000000006", "1.2.246.562.24.00000000007");

        // kohde2, jono1 ... alitaytto, kukaan ei hyvaksytty
        TestHelper.assertoiKukaanEiValittu(hakukohteet.get(1).getValintatapajonot()
                .get(0));

    }
}