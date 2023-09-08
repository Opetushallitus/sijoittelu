package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.SijoitteluAlgorithmUtil;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.valintalaskenta.domain.dto.HakukohdeDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BasicSijoitteluTest {
    @Test
    public void testSijoittelu() throws IOException {
        HakuDTO t = TestHelper.readHakuDTOFromJson("testdata/sijoittelu_basic_case.json");


        List<Hakukohde> hakukohteet = new ArrayList<Hakukohde>();
        for (HakukohdeDTO hkt : t.getHakukohteet()) {
            Hakukohde hakukohde = DomainConverter.convertToHakukohde(hkt);
            hakukohteet.add(hakukohde);
        }

        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(hakukohteet, new ArrayList<>(), java.util.Collections.emptyMap());

        PrintHelper.tallennaSijoitteluTiedostoon(s, "target/sijoittelu_basic_case.sijoitteluresult");

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
