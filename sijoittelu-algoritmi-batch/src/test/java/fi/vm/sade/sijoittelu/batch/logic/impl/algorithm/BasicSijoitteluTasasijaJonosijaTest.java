package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import de.flapdoodle.embed.process.collections.Collections;
import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import junit.framework.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


/**
 *
 * @author Kari Kammonen
 *
 */
public class BasicSijoitteluTasasijaJonosijaTest {

    /**
     * Testaa perustapaus
     *
     * @throws java.io.IOException
     */
    @Test
    public void testSijoittelu() throws IOException {
        // tee sijoittelu
        HakuDTO t = TestHelper.xmlToObjects("testdata/sijoittelu_basic_tasasija_jonosija_case.xml");


        List<Hakukohde> hakukohteet = t.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());

        SijoitteluAlgorithmFactoryImpl h = new SijoitteluAlgorithmFactoryImpl();
        SijoitteluAlgorithm s = h.constructAlgorithm(hakukohteet, Collections.<Valintatulos>newArrayList());
        s.start();

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        Hakemus hakemus1 = getHakemus(hakukohteet, "1.2.246.562.24.00000000001");
        Hakemus hakemus2 = getHakemus(hakukohteet, "1.2.246.562.24.00000000003");
        Hakemus hakemus3 = getHakemus(hakukohteet, "1.2.246.562.24.00000000005");

        Assert.assertTrue(hakemus1.getTasasijaJonosija() > hakemus2.getTasasijaJonosija());
        Assert.assertTrue(hakemus3.getTasasijaJonosija() > hakemus1.getTasasijaJonosija());


    }

    private Hakemus getHakemus(List<Hakukohde> hakukohteet, String oid) {

        Valintatapajono valintatapajono = hakukohteet.get(0).getValintatapajonot().get(0);
        for (Hakemus temp : valintatapajono.getHakemukset()) {
            if(temp.getHakemusOid().equals(oid)) {
                return temp;
            }
        }

        return null;
    }
}