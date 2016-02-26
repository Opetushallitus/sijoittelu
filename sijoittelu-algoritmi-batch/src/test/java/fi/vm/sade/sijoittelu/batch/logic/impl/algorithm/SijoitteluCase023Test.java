package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.SijoitteluAlgorithmUtil;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        HakuDTO t = TestHelper.readHakuDTOFromJson("testdata/sijoittelu_case_023.json");


        List<Hakukohde> hakukohteet = t.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(new SijoitteluAjo(), hakukohteet, new ArrayList());

        PrintHelper.tallennaSijoitteluTiedostoon(s, "target/sijoittelu_case_023.sijoitteluresult");

        // lisaa assertointi
    }

}