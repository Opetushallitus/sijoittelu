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
public class BasicSijoitteluTasasijaTest {

    /**
     * Testaa perustapaus
     *
     * @throws IOException
     */
    @Test
    public void testSijoittelu() throws IOException {
        // tee sijoittelu
        HakuDTO t = TestHelper.readHakuDTOFromJson("testdata/sijoittelu_basic_tasasija_case.json");


        List<Hakukohde> hakukohteet = t.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());

        SijoitteluAlgorithmFactoryImpl h = new SijoitteluAlgorithmFactoryImpl();
        SijoitteluAlgorithm s = h.constructAlgorithm(hakukohteet, Collections.<Valintatulos>newArrayList());
        s.start();

        PrintHelper.tallennaSijoitteluTiedostoon(s, "target/sijoittelu_basic_tasasija_case.sijoitteluresult");

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