package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.spec;

import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.TestHelper.assertoi;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.TestHelper.hakukohde;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.TestHelper.hyvaksyttyjaHakemuksiaAinoastaan;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.TestHelper.peruuntuneitaHakemuksiaAinoastaan;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.TestHelper.valintatapajono;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.TestHelper.varallaAinoastaan;

import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluConfiguration;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluTestSpec;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluajoWrapperFactory;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoittelunTila;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.TestHelper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.SijoitteluAlgorithmUtil;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakukohdeWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Jussi Jartamo
 */
public class HakijaryhmaSpecTest extends SijoitteluTestSpec {

    @Test
    public void testaaVastaanottoSiirtyyYlemmalleHakukohteelleVaikkaHakijaOnSinneVaralla() {
        // Testaa BUG-1064, missä alemman prioriteetin hakutoiveessa hyväksytty hakija ottaa paikan ehdollisesti vastaan
        // ja sijoittelussa hakija nousee korkeamman prioriteetin hakutoiveeseensa ylitäyttösääntöjen mukaisesti, mutta
        // hakijaryhmän sijoittelun jälkeen hakija jää ko. hakutoiveeseen varalle. Seurauksena vastaanottotieto oli siirtynyt ylemmälle hakutoiveelle,
        // hakija oli varalla ko. hakutoiveeseen ja peruuntuneena alkuperäiseen alemman prioriteetin hakutoiveeseen.
        HakuDTO haku = TestHelper.readHakuDTOFromJson("testdata_erikoistapaukset/sijoittelu_vastaanotto_siirtyy_ylemmalle_vaikka_hakija_varasijalla.json");
        Valintatulos valintatulos = new Valintatulos();
        String HAKEMUS_OID = "1.2.246.562.24.00000000003";
        valintatulos.setHakemusOid(HAKEMUS_OID, "");
        valintatulos.setHakukohdeOid("1.2.246.562.11.00000000001", "");
        valintatulos.setValintatapajonoOid("tkk_jono_2", "");
        valintatulos.setJulkaistavissa(true, "");
        valintatulos.setTila(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT, "");

        Valintatulos kesken = new Valintatulos();
        kesken.setHakemusOid(HAKEMUS_OID, "");
        kesken.setHakukohdeOid("1.2.246.562.11.00000000002", "");
        kesken.setValintatapajonoOid("tkk_jono_1", "");
        kesken.setTila(ValintatuloksenTila.KESKEN, "");

        List<Valintatulos> valintatulokset = Arrays.asList(valintatulos, kesken);
        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());

        Hakukohde ensimmainenHk = hakukohteet.stream().filter(h -> h.getOid().endsWith("1")).findAny().get();
        ensimmainenHk.getValintatapajonot().iterator().next().getHakemukset().iterator().next().setEdellinenTila(HakemuksenTila.HYVAKSYTTY);

        final SijoitteluajoWrapper sijoitteluAjo = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluConfiguration(), new SijoitteluAjo(), hakukohteet, valintatulokset, Collections.emptyMap());
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(sijoitteluAjo);

        HakukohdeWrapper ekaHakukohde = s.sijoitteluAjo.getHakukohteet().stream().filter(h -> h.getHakukohde().getOid().equals("1.2.246.562.11.00000000001")).findAny().get();
        HakemusWrapper hakemusWrapperEkaHakukohde = ekaHakukohde.hakukohteenHakemukset().filter(h -> h.getHakemus().getHakemusOid().equals(HAKEMUS_OID)).findAny().get();
        HakukohdeWrapper tokaHakukohde = s.sijoitteluAjo.getHakukohteet().stream().filter(h -> h.getHakukohde().getOid().equals("1.2.246.562.11.00000000002")).findAny().get();
        HakemusWrapper hakemusWrapperTokaHakukohde = tokaHakukohde.hakukohteenHakemukset().filter(h -> h.getHakemus().getHakemusOid().equals(HAKEMUS_OID)).findAny().get();

        // Ylemmässä hakukohteessa ei vastaanottoa hakemukselle ja hakemus varalla
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakemusWrapperEkaHakukohde.getHakemus().getTila());
        Assert.assertEquals(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT, valintatulos.getTila());

        // Alemmassa hakukohteessa hyväksytty & ehdollisesti vastaanottanut
        Assert.assertEquals(HakemuksenTila.VARALLA, hakemusWrapperTokaHakukohde.getHakemus().getTila());
        Assert.assertEquals(ValintatuloksenTila.KESKEN, kesken.getTila());

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

   /**
    * BUG-1496
    * Vaikka jonolla ei ole varasijatäyttöä, niin jos tasasijasääntö on arvonta, sijoittelu asettaa arvonnan hävinneen
    * hakijan varasijalle. Hakija pitäisi peruunnuttaa.
    */
    @Test
    public void arvontaEiJataKorkeakouluhaulleHakijaaViimeisenHyvaksytynTasasijaltaVarasijalleJosEiOleVarasijatayttoa() {
        SijoitteluajoWrapper ajo = ajaSijoittelu("testdata_erikoistapaukset/sijoittelu_tarkka_kiintio_2_hakijaryhmaa_kiintio_ylittyy_tasapisteilla.json",
            sijoitteluajoWrapper -> sijoitteluajoWrapper.setKKHaku(true));
        assertoi(ajo, hakukohde("1"), valintatapajono("1"), hyvaksyttyjaHakemuksiaAinoastaan("1", "2", "3"));
        assertoi(ajo, hakukohde("1"), valintatapajono("1"), peruuntuneitaHakemuksiaAinoastaan("4"));
    }

    @Test
    public void arvontaJattaaToisenAsteenHaulleViimeisenHyvaksytynTasasijaltaHakijanVarasijalleVaikkaEiOleVarasijatayttoa() {
        SijoitteluajoWrapper ajo = ajaSijoittelu("testdata_erikoistapaukset/sijoittelu_tarkka_kiintio_2_hakijaryhmaa_kiintio_ylittyy_tasapisteilla.json",
            sijoitteluajoWrapper -> sijoitteluajoWrapper.setKKHaku(false));
        assertoi(ajo, hakukohde("1"), valintatapajono("1"), hyvaksyttyjaHakemuksiaAinoastaan("1", "2", "3"));
        assertoi(ajo, hakukohde("1"), valintatapajono("1"), varallaAinoastaan("4"));
    }
}

