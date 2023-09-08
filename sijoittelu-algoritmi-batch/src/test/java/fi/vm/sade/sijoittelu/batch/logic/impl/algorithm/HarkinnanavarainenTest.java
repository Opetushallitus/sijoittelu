package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.SijoitteluAlgorithmUtil;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Kari Kammonen
 *
 */
public class HarkinnanavarainenTest {

    /**
     * Testaa perustapaus
     *
     * @throws java.io.IOException
     */
    @Test
    public void testSijoittelu() throws IOException {
        // tee sijoittelu
        HakuDTO t = TestHelper.readHakuDTOFromJson("testdata/sijoittelu_harkinnanvarainen.json");


        List<Hakukohde> hakukohteet = t.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(hakukohteet, new ArrayList(), Collections.emptyMap());

        PrintHelper.tallennaSijoitteluTiedostoon(s, "target/sijoittelu_harkinnanvarainen.sijoitteluresult");

        // assertoi
        TestHelper.assertoi(hakukohteet.get(0).getValintatapajonot()
                .get(0), "1.2.246.562.24.00000000004", HakemuksenTila.HYVAKSYTTY);

        TestHelper.assertoi(hakukohteet.get(1).getValintatapajonot()
                .get(0), "1.2.246.562.24.00000000004", HakemuksenTila.PERUUNTUNUT);

        TestHelper.assertoi(hakukohteet.get(2).getValintatapajonot()
                .get(0), "1.2.246.562.24.00000000004", HakemuksenTila.HYLATTY);

        TestHelper.assertoi(hakukohteet.get(3).getValintatapajonot()
                .get(0), "1.2.246.562.24.00000000004", HakemuksenTila.PERUUNTUNUT);



    }

}
