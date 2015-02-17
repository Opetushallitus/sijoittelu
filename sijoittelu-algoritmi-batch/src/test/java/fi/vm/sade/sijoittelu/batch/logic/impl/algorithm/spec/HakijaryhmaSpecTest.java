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

    @Test
    public void testaaHakijaryhmaAlitayttoLukko() {
        SijoitteluajoWrapper ajo = ajaSijoittelu("testdata_erikoistapaukset/sijoittelu_alitaytto_lukko.json");
        assertoi(ajo, hakukohde("1"), valintatapajono("1"), hyvaksyttyjaHakemuksiaAinoastaan("1"));
    }

    @Test
    public void liianIsoKiintio() {
        SijoitteluajoWrapper ajo = ajaSijoittelu("testdata_erikoistapaukset/sijoittelu_liian_iso_kiintio.json");
        assertoi(ajo, hakukohde("1"), valintatapajono("1"), hyvaksyttyjaHakemuksiaAinoastaan("1", "2"));
    }

    @Test
    public void tarkkaKiintioPerusTapaus() {
        SijoitteluajoWrapper ajo = ajaSijoittelu("testdata_erikoistapaukset/sijoittelu_tarkka_kiintio.json");
        assertoi(ajo, hakukohde("1"), valintatapajono("1"), hyvaksyttyjaHakemuksiaAinoastaan("1", "2", "4"));
    }

    @Test
    public void tarkkaKiintioTasasijaYlitys() {
        SijoitteluajoWrapper ajo = ajaSijoittelu("testdata_erikoistapaukset/sijoittelu_tarkka_kiintio_tasasija_kiintion_ylitys.json");
        assertoi(ajo, hakukohde("1"), valintatapajono("1"), hyvaksyttyjaHakemuksiaAinoastaan("1", "2", "3"));
    }

    @Test
    public void tarkkaKiintioParasVaralle() {
        SijoitteluajoWrapper ajo = ajaSijoittelu("testdata_erikoistapaukset/sijoittelu_tarkka_kiintio_2_hakijaryhmaa.json");
        assertoi(ajo, hakukohde("1"), valintatapajono("1"), hyvaksyttyjaHakemuksiaAinoastaan("2", "3", "4"));
    }

    @Test
    public void tarkkaKiintioJaKiintioJaaVajaaksi() {
        SijoitteluajoWrapper ajo = ajaSijoittelu("testdata_erikoistapaukset/sijoittelu_tarkka_kiintio_2_hakijaryhmaa_kiintio_jaa_vajaaksi.json");
        assertoi(ajo, hakukohde("1"), valintatapajono("1"), hyvaksyttyjaHakemuksiaAinoastaan("2", "1", "4"));
    }

    @Test
    public void tarkkaKiintioYlittyy() {
        SijoitteluajoWrapper ajo = ajaSijoittelu("testdata_erikoistapaukset/sijoittelu_tarkka_kiintio_2_hakijaryhmaa_kiintio_ylittyy.json");
        assertoi(ajo, hakukohde("1"), valintatapajono("1"), hyvaksyttyjaHakemuksiaAinoastaan("1", "2", "3"));
    }
}

