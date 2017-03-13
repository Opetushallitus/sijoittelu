package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import de.flapdoodle.embed.process.collections.Collections;
import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.SijoitteluAlgorithmUtil;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class BasicSijoitteluHakijaryhmaTasasijaTest {


    private static final Logger LOG = LoggerFactory.getLogger(BasicSijoitteluHakijaryhmaTasasijaTest.class);

    /**
     * Testaa perustapaus
     *
     * @throws IOException
     */

    @Test
    public void testSijoittelu() throws IOException {
        // tee sijoittelu
        HakuDTO t = TestHelper.readHakuDTOFromJson("testdata/sijoittelu_basic_hakijaryhma_tasasija_case.json");

        List<Hakukohde> hakukohteet = t.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());

        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(hakukohteet, Collections.<Valintatulos>newArrayList(), java.util.Collections.emptyMap());
        PrintHelper.tallennaSijoitteluTiedostoon(s, "target/sijoittelu_basic_hakijaryhma_tasasija_case.sijoitteluresult");
        // Pitäiskö olla näin???
        //TestHelper.assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "1.2.246.562.24.00000000001", "1.2.246.562.24.00000000002");


        TestHelper.assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "1.2.246.562.24.00000000001");
        TestHelper.assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(1), "1.2.246.562.24.00000000002", "1.2.246.562.24.00000000003", "1.2.246.562.24.00000000004", "1.2.246.562.24.00000000005","1.2.246.562.24.00000000007", "1.2.246.562.24.00000000008");

    }
}
