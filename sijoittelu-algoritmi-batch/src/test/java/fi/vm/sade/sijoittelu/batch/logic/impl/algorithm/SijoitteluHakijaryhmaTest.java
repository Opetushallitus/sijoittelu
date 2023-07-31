package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;


import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.TestHelper.*;


/**
 *
 * @author kjsaila
 *
 */
public class SijoitteluHakijaryhmaTest extends SijoitteluTestSpec{

    /**
     *
     *
     * @throws java.io.IOException
     */
    @Test
    public void testSijoittelu() throws IOException {
        SijoitteluajoWrapper ajo = ajaSijoittelu("testdata/sijoittelu_monta_kohdetta_ja_jonoa.json");

        // Hakukohde 1
        assertoi(ajo, hakukohde("1"), valintatapajono("1"), hyvaksyttyjaHakemuksiaAinoastaan("2", "5"));
        assertoi(ajo, hakukohde("1"), valintatapajono("2"), hyvaksyttyjaHakemuksiaAinoastaan("6", "8", "9"));
        assertoi(ajo, hakukohde("1"), valintatapajono("3"), hyvaksyttyjaHakemuksiaAinoastaan("10"));

        // Hakukohde 2
        assertoi(ajo, hakukohde("2"), valintatapajono("4"), hyvaksyttyjaHakemuksiaAinoastaan("1", "3", "7"));

        // Hakukohde 3
        assertoi(ajo, hakukohde("3"), valintatapajono("5"), hyvaksyttyjaHakemuksiaAinoastaan("4"));
        assertoi(ajo, hakukohde("3"), valintatapajono("6"), hyvaksyttyjaHakemuksiaAinoastaan("a"));

        ajo = ajaSijoittelu("testdata/sijoittelu_monta_kohdetta_jonoa_ja_hakijaryhmaa_1.json");

        // Hakukohde 1
        assertoi(ajo, hakukohde("1"), valintatapajono("1"), hyvaksyttyjaHakemuksiaAinoastaan("2", "6"));
        assertoi(ajo, hakukohde("1"), valintatapajono("2"), hyvaksyttyjaHakemuksiaAinoastaan("8", "9", "10"));
        assertoi(ajo, hakukohde("1"), valintatapajono("3"), eiHyvaksyttyja());

        // Hakukohde 2
        assertoi(ajo, hakukohde("2"), valintatapajono("4"), hyvaksyttyjaHakemuksiaAinoastaan("1", "3", "7", "5"));

        // Hakukohde 3
        assertoi(ajo, hakukohde("3"), valintatapajono("5"), hyvaksyttyjaHakemuksiaAinoastaan("4"));
        assertoi(ajo, hakukohde("3"), valintatapajono("6"), hyvaksyttyjaHakemuksiaAinoastaan("a"));

        ajo = ajaSijoittelu("testdata/sijoittelu_monta_kohdetta_jonoa_ja_hakijaryhmaa_2.json");

        // Hakukohde 1
        assertoi(ajo, hakukohde("1"), valintatapajono("1"), hyvaksyttyjaHakemuksiaAinoastaan("2", "6"));
        assertoi(ajo, hakukohde("1"), valintatapajono("2"), hyvaksyttyjaHakemuksiaAinoastaan("9"));
        assertoi(ajo, hakukohde("1"), valintatapajono("3"), hyvaksyttyjaHakemuksiaAinoastaan("8","10"));

        // Hakukohde 2
        assertoi(ajo, hakukohde("2"), valintatapajono("4"), hyvaksyttyjaHakemuksiaAinoastaan("1", "3", "7", "5"));

        // Hakukohde 3
        assertoi(ajo, hakukohde("3"), valintatapajono("5"), hyvaksyttyjaHakemuksiaAinoastaan("4"));
        assertoi(ajo, hakukohde("3"), valintatapajono("6"), hyvaksyttyjaHakemuksiaAinoastaan("a"));

        ajo = ajaSijoittelu("testdata/sijoittelu_monta_kohdetta_jonoa_ja_hakijaryhmaa_3.json");

        // Hakukohde 1
        assertoi(ajo, hakukohde("1"), valintatapajono("1"), hyvaksyttyjaHakemuksiaAinoastaan("2", "8"));
        assertoi(ajo, hakukohde("1"), valintatapajono("2"), hyvaksyttyjaHakemuksiaAinoastaan("9"));
        assertoi(ajo, hakukohde("1"), valintatapajono("3"), hyvaksyttyjaHakemuksiaAinoastaan("6"));

        // Hakukohde 2
        assertoi(ajo, hakukohde("2"), valintatapajono("4"), hyvaksyttyjaHakemuksiaAinoastaan("1", "3", "7", "5", "10"));

        // Hakukohde 3
        assertoi(ajo, hakukohde("3"), valintatapajono("5"), hyvaksyttyjaHakemuksiaAinoastaan("4"));
        assertoi(ajo, hakukohde("3"), valintatapajono("6"), hyvaksyttyjaHakemuksiaAinoastaan("a"));

        ajo = ajaSijoittelu("testdata/sijoittelu_monta_kohdetta_jonoa_ja_hakijaryhmaa_4.json");

        // Hakukohde 1
        assertoi(ajo, hakukohde("1"), valintatapajono("1"), hyvaksyttyjaHakemuksiaAinoastaan("2", "8"));
        assertoi(ajo, hakukohde("1"), valintatapajono("2"), hyvaksyttyjaHakemuksiaAinoastaan("9"));
        assertoi(ajo, hakukohde("1"), valintatapajono("3"), hyvaksyttyjaHakemuksiaAinoastaan("6", "7"));

        // Hakukohde 2
        assertoi(ajo, hakukohde("2"), valintatapajono("4"), hyvaksyttyjaHakemuksiaAinoastaan("1", "3", "5"));

        // Hakukohde 3
        assertoi(ajo, hakukohde("3"), valintatapajono("5"), hyvaksyttyjaHakemuksiaAinoastaan("4"));
        assertoi(ajo, hakukohde("3"), valintatapajono("6"), hyvaksyttyjaHakemuksiaAinoastaan("a", "10"));

        ajo = ajaSijoittelu("testdata/sijoittelu_monta_kohdetta_jonoa_ja_hakijaryhmaa_5.json");

        // Hakukohde 1
        assertoi(ajo, hakukohde("1"), valintatapajono("1"), hyvaksyttyjaHakemuksiaAinoastaan("2", "8"));
        assertoi(ajo, hakukohde("1"), valintatapajono("2"), hyvaksyttyjaHakemuksiaAinoastaan("9"));
        assertoi(ajo, hakukohde("1"), valintatapajono("3"), hyvaksyttyjaHakemuksiaAinoastaan("6", "7"));

        // Hakukohde 2
        assertoi(ajo, hakukohde("2"), valintatapajono("4"), hyvaksyttyjaHakemuksiaAinoastaan("1", "3", "5"));

        // Hakukohde 3
        assertoi(ajo, hakukohde("3"), valintatapajono("5"), eiHyvaksyttyja());
        assertoi(ajo, hakukohde("3"), valintatapajono("6"), hyvaksyttyjaHakemuksiaAinoastaan("4", "a", "10"));

    }

}