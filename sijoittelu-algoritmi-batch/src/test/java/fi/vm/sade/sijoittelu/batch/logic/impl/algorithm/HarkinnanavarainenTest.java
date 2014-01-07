package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import de.flapdoodle.embed.process.collections.Collections;
import fi.vm.sade.service.valintatiedot.schema.HakuTyyppi;
import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.ValintatapajonoTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import junit.framework.Assert;
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
public class HarkinnanavarainenTest {

    /**
     * Testaa perustapaus
     *
     * @throws java.io.IOException
     */
    @Test
    public void testSijoittelu() throws IOException {
        // tee sijoittelu
        HakuTyyppi t = TestHelper.xmlToObjects("testdata/sijoittelu_harkinnanvarainen.xml");


        List<Hakukohde> hakukohteet = new ArrayList<Hakukohde>() ;
        for(fi.vm.sade.service.valintatiedot.schema.HakukohdeTyyppi hkt :t.getHakukohteet()) {
            Hakukohde hakukohde = DomainConverter.convertToHakukohde(hkt);
            hakukohteet.add(hakukohde);
        }

        SijoitteluAlgorithmFactoryImpl h = new SijoitteluAlgorithmFactoryImpl();
        SijoitteluAlgorithm s = h.constructAlgorithm(hakukohteet, Collections.<Valintatulos>newArrayList());
        s.start();

        System.out.println(PrintHelper.tulostaSijoittelu(s));
        // tulosta
        FileWriter fstream = new FileWriter("target/sijoittelu_harkinnanvarainen.sijoitteluresult");
        fstream.write(PrintHelper.tulostaSijoittelu(s));
        fstream.flush();
        fstream.close();

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