package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.spec;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.PrintHelper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluTestSpec;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.TestHelper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import org.junit.Test;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.TestHelper.*;

import java.util.stream.Stream;

/**
 * @author Jussi Jartamo
 */
public class HakijaryhmaSpecTest extends SijoitteluTestSpec {


    @Test
    public void testaaHakijaryhmaYlitaytonRajatapaukset() {
        SijoitteluajoWrapper ajo = kaksiHakukohdettaJaKaksiJonoaJaHakijaryhmaYhdenKiintiolla();
        assertoi(ajo, hakukohde("1"), valintatapajono("1"), hyvaksyttyjaHakemuksiaAinoastaan("2", "3"));
        assertoi(ajo, hakukohde("1"), valintatapajono("2"), hyvaksyttyjaHakemuksiaAinoastaan("10", "7"));
    }
}

