package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
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
public class VastaanottotietoTest {

    /**
     * Testaa perustapaus
     *
     * @throws java.io.IOException
     */
    @Test
    public void testVastaanottotilaPeruutettu() throws IOException {
        // tee sijoittelu
        HakuDTO t = TestHelper.xmlToObjects("testdata/sijoittelu_vastaanottotieto_case.xml");


        List<Hakukohde> hakukohteet = t.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohdeRest).collect(Collectors.toList());

        ArrayList<Valintatulos> valintatuloses = new ArrayList<Valintatulos>();
        Valintatulos valintatulos = new Valintatulos();

        valintatulos.setHakemusOid("1.2.246.562.24.00000000004");
        valintatulos.setHakijaOid("oid");
        valintatulos.setHakukohdeOid("1.2.246.562.11.00000000007");
        valintatulos.setHakuOid(t.getHakuOid());
        valintatulos.setTila(ValintatuloksenTila.PERUUTETTU);
        valintatulos.setValintatapajonoOid("tkk_jono_1");

        valintatuloses.add(valintatulos);

        valintatulos = new Valintatulos();

        valintatulos.setHakemusOid("1.2.246.562.24.00000000003");
        valintatulos.setHakijaOid("oid2");
        valintatulos.setHakukohdeOid("1.2.246.562.11.00000000007");
        valintatulos.setHakuOid(t.getHakuOid());
        valintatulos.setTila(ValintatuloksenTila.PERUNUT);
        valintatulos.setValintatapajonoOid("tkk_jono_1");

        valintatuloses.add(valintatulos);

        SijoitteluAlgorithmFactoryImpl h = new SijoitteluAlgorithmFactoryImpl();
        SijoitteluAlgorithm s = h.constructAlgorithm(hakukohteet, valintatuloses);
        s.start();

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        // assertoi
        TestHelper.assertoi(hakukohteet.get(0).getValintatapajonot()
                .get(0), "1.2.246.562.24.00000000004", HakemuksenTila.PERUUTETTU);
        TestHelper.assertoi(hakukohteet.get(0).getValintatapajonot()
                .get(0), "1.2.246.562.24.00000000003", HakemuksenTila.PERUNUT);
        TestHelper.assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot()
                .get(0), "1.2.246.562.24.00000000001");

        TestHelper.assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot()
                .get(1), "1.2.246.562.24.00000000004");

    }

}