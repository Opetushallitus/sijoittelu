package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import de.flapdoodle.embed.process.collections.Collections;
import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import org.junit.Test;

import java.io.FileWriter;
import java.io.IOException;
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

        SijoitteluAlgorithmFactoryImpl h = new SijoitteluAlgorithmFactoryImpl();
        SijoitteluAlgorithm s = h.constructAlgorithm(hakukohteet, Collections.<Valintatulos>newArrayList());
        s.start();

        // tulosta
        FileWriter fstream = new FileWriter("target/sijoittelu_case_023.sijoitteluresult");
        fstream.write(PrintHelper.tulostaSijoittelu(s));
        fstream.flush();
        fstream.close();

        // lisaa assertointi

    }

}