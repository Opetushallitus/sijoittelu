package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import de.flapdoodle.embed.process.collections.Collections;
import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.valintalaskenta.domain.dto.HakukohdeDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import org.junit.Test;

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
        HakuDTO t = TestHelper.readHakuDTOFromJson("testdata/sijoittelu_basic_case.json");


        List<Hakukohde> hakukohteet = new ArrayList<Hakukohde>() ;
        for(HakukohdeDTO hkt :t.getHakukohteet()) {
            Hakukohde hakukohde = DomainConverter.convertToHakukohde(hkt);
            hakukohteet.add(hakukohde);
        }

        SijoittelunTila s = SijoitteluAlgorithm.sijoittele(hakukohteet, Collections.<Valintatulos>newArrayList());

        PrintHelper.tallennaSijoitteluTiedostoon(s, "target/sijoittelu_basic_case.sijoitteluresult");

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