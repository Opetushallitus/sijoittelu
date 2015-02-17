package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.spec;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluTestSpec;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import org.junit.Test;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.TestHelper.*;

/**
 * @author Jussi Jartamo
 */
public class HakijaryhmaSpecTest extends SijoitteluTestSpec {


    @Test
    public void testaaHakijaryhmaYlitaytonRajatapaukset() {
        SijoitteluajoWrapper ajo = ajaSijoittelu("testdata_erikoistapaukset/sijoittelu_2hakukohdetta_2jonoa_ja_hakijaryhma_1kiintiolla.json");
        assertoi(ajo, hakukohde("1"), valintatapajono("1"), hyvaksyttyjaHakemuksiaAinoastaan("2", "3"));
        assertoi(ajo, hakukohde("1"), valintatapajono("2"), hyvaksyttyjaHakemuksiaAinoastaan("10", "7"));
    }

    @Test
    public void testaaHakijaryhmaYlitaytonRajatapauksetVaralleTiputus() {
        SijoitteluajoWrapper ajo = ajaSijoittelu("testdata_erikoistapaukset/sijoittelu_ylitaytto_rajatapaus.json");
        assertoi(ajo, hakukohde("1"), valintatapajono("1"), hyvaksyttyjaHakemuksiaAinoastaan("2", "3", "4"));
    }
}

