package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import de.flapdoodle.embed.process.collections.Collections;
import fi.vm.sade.service.sijoittelu.types.SijoitteleTyyppi;
import fi.vm.sade.service.valintatiedot.schema.*;
import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.tarjonta.service.types.*;
import fi.vm.sade.tarjonta.service.types.HakukohdeTyyppi;
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
public class BasicSijoitteluTest {

    /**
     * Testaa perustapaus
     *
     * @throws IOException
     */
    @Test
    public void testSijoittelu() throws IOException {
        // tee sijoittelu
        fi.vm.sade.service.sijoittelu.types.SijoitteleTyyppi t = TestHelper.xmlToObjects("testdata/sijoittelu_basic_case.xml");

        List<Hakukohde> hakukohteet = new ArrayList<Hakukohde>() ;
        for(fi.vm.sade.service.valintatiedot.schema.HakukohdeTyyppi hkt : t.getTarjonta().getHakukohde()) {
            Hakukohde hakukohde = DomainConverter.convertToHakukohde(hkt);
            hakukohteet.add(hakukohde);
        }

        SijoitteluAlgorithmFactoryImpl h = new SijoitteluAlgorithmFactoryImpl();
        SijoitteluAlgorithm s = h.constructAlgorithm(hakukohteet, Collections.<Valintatulos>newArrayList());
        s.start();

        // tulosta
        FileWriter fstream = new FileWriter("target/sijoittelu_basic_case.sijoitteluresult");
        fstream.write(PrintHelper.tulostaSijoittelu(s));
        fstream.flush();
        fstream.close();

        // assertoi
        TestHelper.assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot()
                .get(0), "1.2.246.562.24.00000000004");
        TestHelper.assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot()
                .get(1), "1.2.246.562.24.00000000003");
        TestHelper.assertoiAinoastaanValittu(hakukohteet.get(1).getValintatapajonot()
                .get(0), "1.2.246.562.24.00000000002");
        TestHelper.assertoiAinoastaanValittu(hakukohteet.get(2).getValintatapajonot()
                .get(0), "1.2.246.562.24.00000000005", "1.2.246.562.24.00000000006");

    }

}