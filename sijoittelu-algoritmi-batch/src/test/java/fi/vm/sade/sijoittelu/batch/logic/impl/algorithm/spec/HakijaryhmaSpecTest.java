package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.spec;

import com.google.common.collect.Lists;
import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.*;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.SijoitteluAlgorithmUtil;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.valintalaskenta.domain.dto.HakukohdeDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.TestHelper.*;

/**
 * @author Jussi Jartamo
 */
public class HakijaryhmaSpecTest extends SijoitteluTestSpec {

    @Test
    public void testaaVastaanottoSiirtyyYlemmalleHakukohteelleVaikkaHakijaOnSinneVaralla() {
        HakuDTO haku = TestHelper.readHakuDTOFromJson("testdata_erikoistapaukset/sijoittelu_vastaanotto_siirtyy_ylemmalle_vaikka_hakija_varasijalla.json");
        Valintatulos valintatulos = new Valintatulos();
        valintatulos.setHakemusOid("1.2.246.562.24.00000000003","");
        valintatulos.setHakukohdeOid("1.2.246.562.11.00000000001","");
        valintatulos.setValintatapajonoOid("tkk_jono_2","");
        valintatulos.setJulkaistavissa(true, "");
        valintatulos.setTila(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT, "");

        Valintatulos kesken = new Valintatulos();
        kesken.setHakemusOid("1.2.246.562.24.00000000003","");
        kesken.setHakukohdeOid("1.2.246.562.11.00000000002","");
        kesken.setValintatapajonoOid("tkk_jono_1","");
        kesken.setTila(ValintatuloksenTila.KESKEN, "");

        List<Valintatulos> valintatulokset = Arrays.asList(valintatulos, kesken);
        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());

        Hakukohde ensimmainenHk = hakukohteet.stream().filter(h -> h.getOid().endsWith("1")).findAny().get();
        ensimmainenHk.getValintatapajonot().iterator().next().getHakemukset().iterator().next().setEdellinenTila(HakemuksenTila.HYVAKSYTTY);

        final SijoitteluajoWrapper sijoitteluAjo = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluAjo(), hakukohteet, valintatulokset, Collections.emptyMap());
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(sijoitteluAjo);
    }

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

