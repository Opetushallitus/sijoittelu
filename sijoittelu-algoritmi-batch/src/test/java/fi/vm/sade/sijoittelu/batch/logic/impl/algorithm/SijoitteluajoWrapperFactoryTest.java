package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import com.google.common.collect.Lists;

import fi.vm.sade.service.valintaperusteet.dto.ValintatapajonoDTO;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilojenMuokkaus;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.IlmoittautumisTila;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.TilanKuvaukset;
import fi.vm.sade.sijoittelu.domain.TilankuvauksenTarkenne;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SijoitteluajoWrapperFactoryTest {

    @Nested
    public class Valintatuloksen_tilan_vaikutus_hakemukseen {
        @Test
        public void PERUNUT_peruu_hakemuksen() {
            SijoitteluajoWrapper sijoitteluAjo = sijoitteluAjo(valintatulosWithTila(ValintatuloksenTila.PERUNUT));

            assertEquals(sijoitteluAjo.getHakukohteet().size(), 1);
            HakemusWrapper hakemusWrapper = sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
            assertEquals(HakemuksenTila.PERUNUT, hakemusWrapper.getHakemus().getTila());
            assertFalse(hakemusWrapper.isTilaVoidaanVaihtaa());
        }

        @Test
        public void EHDOLLISESTI_VASTAANOTTANUT_hyvaksyy_hakemuksen() {
            Valintatulos valintatulos = valintatulosWithTila(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT);
            SijoitteluajoWrapper sijoitteluAjo = sijoitteluAjo(valintatulos, yksiHakemus());

            assertEquals(sijoitteluAjo.getHakukohteet().size(), 1);
            HakemusWrapper hakemusWrapper = sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
            assertHyvaksytty(hakemusWrapper);
            assertEquals(hakemusWrapper.getHakemus().getIlmoittautumisTila(), valintatulos.getIlmoittautumisTila());
            assertTrue(!hakemusWrapper.isTilaVoidaanVaihtaa());
        }

        @Test
        public void EI_VASTAANOTETTU_MAARA_AIKANA_peruu_hakemuksen() {
            SijoitteluajoWrapper sijoitteluAjo = sijoitteluAjo(valintatulosWithTila(ValintatuloksenTila.EI_VASTAANOTETTU_MAARA_AIKANA));

            assertEquals(sijoitteluAjo.getHakukohteet().size(), 1);
            HakemusWrapper hakemusWrapper = sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
            assertEquals(HakemuksenTila.PERUNUT, hakemusWrapper.getHakemus().getTila());
            assertEquals(TilankuvauksenTarkenne.PERUUNTUNUT_EI_VASTAANOTTANUT_MAARAAIKANA, hakemusWrapper.getHakemus().getTilankuvauksenTarkenne());
            assertEquals(TilanKuvaukset.peruuntunutEiVastaanottanutMaaraaikana, hakemusWrapper.getHakemus().getTilanKuvaukset());
            assertFalse(hakemusWrapper.isTilaVoidaanVaihtaa());
        }

        @Test
        public void PERUUTETTU_peruuttaa_hakemuksenJaTyhjentaaTilanTarkenteen() {
            SijoitteluajoWrapper sijoitteluAjo = sijoitteluAjo(valintatulosWithTila(ValintatuloksenTila.PERUUTETTU));

            assertEquals(sijoitteluAjo.getHakukohteet().size(), 1);
            HakemusWrapper hakemusWrapper = sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
            assertEquals(HakemuksenTila.PERUUTETTU, hakemusWrapper.getHakemus().getTila());
            assertEquals(TilankuvauksenTarkenne.EI_TILANKUVAUKSEN_TARKENNETTA, hakemusWrapper.getHakemus().getTilankuvauksenTarkenne());
            assertEquals(TilanKuvaukset.tyhja, hakemusWrapper.getHakemus().getTilanKuvaukset());
            assertTrue(!hakemusWrapper.isTilaVoidaanVaihtaa());
        }

        @Test
        public void VASTAANOTTANUT_SITOVASTI_hyvaksyy_hakemuksen_ja_kopioi_ilmoittautumistilan() {
            Valintatulos valintatulos = valintatulosWithTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI);
            valintatulos.setIlmoittautumisTila(IlmoittautumisTila.LASNA, "");
            valintatulos.setJulkaistavissa(true, "");
            SijoitteluajoWrapper sijoitteluAjo = sijoitteluAjo(valintatulos);

            assertEquals(1, sijoitteluAjo.getHakukohteet().size());
            HakemusWrapper hakemusWrapper = sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
            assertHyvaksytty(hakemusWrapper);
            assertEquals(IlmoittautumisTila.LASNA, hakemusWrapper.getHakemus().getIlmoittautumisTila());
            assertFalse(hakemusWrapper.isTilaVoidaanVaihtaa());
        }

        @Test
        public void hyvaksyPeruuntunut_flag_hyvaksyy_hakemuksen_jos_ei_korkeampaa_hyvaksyttya() {
            Valintatulos valintatulosYlempi = valintatulosWithTila(ValintatuloksenTila.KESKEN);
            valintatulosYlempi.setHyvaksyPeruuntunut(true, "");
            valintatulosYlempi.setJulkaistavissa(true, "");
            Hakemus hakemusYlempiJono = new Hakemus();
            hakemusYlempiJono.setHakemusOid("123");
            TilojenMuokkaus.asetaTilaksiPeruuntunutAloituspaikatTaynna(hakemusYlempiJono);
            hakemusYlempiJono.setEdellinenTila(HakemuksenTila.PERUUNTUNUT);

            Valintatulos valintatulosAlempi = valintatulosWithTila(ValintatuloksenTila.KESKEN, "123");
            valintatulosAlempi.setHyvaksyPeruuntunut(true, "");
            valintatulosAlempi.setJulkaistavissa(true, "");
            Hakemus hakemusAlempiJono = new Hakemus();
            hakemusAlempiJono.setHakemusOid("123");
            TilojenMuokkaus.asetaTilaksiHyvaksytty(hakemusAlempiJono);
            hakemusAlempiJono.setEdellinenTila(HakemuksenTila.HYVAKSYTTY);

            SijoitteluajoWrapper sijoitteluAjo = sijoitteluAjo(Lists.newArrayList(valintatulosYlempi, valintatulosAlempi), Collections.singletonList(hakemusYlempiJono), Collections.singletonList(hakemusAlempiJono));

            HakemusWrapper hakemusWrapperYlempi = sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
            assertHyvaksytty(hakemusWrapperYlempi);
            assertFalse(hakemusWrapperYlempi.isTilaVoidaanVaihtaa());

            HakemusWrapper hakemusWrapperAlempi = sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
            assertHyvaksytty(hakemusWrapperAlempi);
            assertFalse(hakemusWrapperAlempi.isTilaVoidaanVaihtaa());
        }

        @Test
        public void hyvaksyPeruuntunut_flag_ei_hyvaksy_hakemusta_jos_korkeampi_jo_hyvaksytty() {
            Valintatulos valintatulosYlempi = valintatulosWithTila(ValintatuloksenTila.KESKEN);
            valintatulosYlempi.setHyvaksyPeruuntunut(true, "");
            valintatulosYlempi.setJulkaistavissa(true, "");
            Hakemus hakemusYlempiJono = new Hakemus();
            hakemusYlempiJono.setHakemusOid("123");
            TilojenMuokkaus.asetaTilaksiHyvaksytty(hakemusYlempiJono);
            hakemusYlempiJono.setEdellinenTila(HakemuksenTila.HYVAKSYTTY);

            Valintatulos valintatulosAlempi = valintatulosWithTila(ValintatuloksenTila.KESKEN, "123");
            valintatulosAlempi.setHyvaksyPeruuntunut(true, "");
            valintatulosAlempi.setJulkaistavissa(true, "");
            Hakemus hakemusAlempiJono = new Hakemus();
            hakemusAlempiJono.setHakemusOid("123");
            TilojenMuokkaus.asetaTilaksiPeruuntunutToinenJono(hakemusAlempiJono);
            hakemusAlempiJono.setEdellinenTila(HakemuksenTila.PERUUNTUNUT);

            SijoitteluajoWrapper sijoitteluAjo = sijoitteluAjo(Lists.newArrayList(valintatulosYlempi, valintatulosAlempi), Collections.singletonList(hakemusYlempiJono), Collections.singletonList(hakemusAlempiJono));

            HakemusWrapper hakemusWrapperYlempi = sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
            assertHyvaksytty(hakemusWrapperYlempi);
            assertFalse(hakemusWrapperYlempi.isTilaVoidaanVaihtaa());

            HakemusWrapper hakemusWrapperAlempi = sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(1).getHakemukset().get(0);
            assertPeruuntunut(hakemusWrapperAlempi);
            assertTrue(hakemusWrapperAlempi.isTilaVoidaanVaihtaa());
        }

        @Test
        public void hyvaksyttyVarasijalta_flag_ei_hyvaksy_hakemusta_jos_korkeampi_jo_hyvaksytty() {
            Valintatulos valintatulosYlempi = valintatulosWithTila(ValintatuloksenTila.KESKEN);
            valintatulosYlempi.setHyvaksyttyVarasijalta(true, "");
            valintatulosYlempi.setJulkaistavissa(true, "");
            Hakemus hakemusYlempiJono = new Hakemus();
            hakemusYlempiJono.setHakemusOid("123");
            TilojenMuokkaus.asetaTilaksiHyvaksytty(hakemusYlempiJono);
            hakemusYlempiJono.setEdellinenTila(HakemuksenTila.HYVAKSYTTY);

            Valintatulos valintatulosAlempi = valintatulosWithTila(ValintatuloksenTila.KESKEN, "123");
            valintatulosAlempi.setHyvaksyttyVarasijalta(true, "");
            valintatulosAlempi.setJulkaistavissa(true, "");
            Hakemus hakemusAlempiJono = new Hakemus();
            hakemusAlempiJono.setHakemusOid("123");
            TilojenMuokkaus.asetaTilaksiPeruuntunutToinenJono(hakemusAlempiJono);
            hakemusAlempiJono.setEdellinenTila(HakemuksenTila.PERUUNTUNUT);

            SijoitteluajoWrapper sijoitteluAjo = sijoitteluAjo(Lists.newArrayList(valintatulosYlempi, valintatulosAlempi), Collections.singletonList(hakemusYlempiJono), Collections.singletonList(hakemusAlempiJono));

            HakemusWrapper hakemusWrapperYlempi = sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
            assertHyvaksytty(hakemusWrapperYlempi);
            assertFalse(hakemusWrapperYlempi.isTilaVoidaanVaihtaa());

            HakemusWrapper hakemusWrapperAlempi = sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(1).getHakemukset().get(0);
            assertPeruuntunut(hakemusWrapperAlempi);
            assertTrue(hakemusWrapperAlempi.isTilaVoidaanVaihtaa());
        }

        @Test
        public void hyvaksyVarasijalta_flag_hyvaksyy_hakemuksen() {
            Valintatulos valintatulos = valintatulosWithTila(ValintatuloksenTila.KESKEN);
            valintatulos.setHyvaksyttyVarasijalta(true, "");
            Hakemus hakemus = new Hakemus();
            hakemus.setHakemusOid("123");
            TilojenMuokkaus.asetaTilaksiHyvaksytty(hakemus);
            hakemus.setEdellinenTila(HakemuksenTila.HYVAKSYTTY);
            SijoitteluajoWrapper sijoitteluAjo = sijoitteluAjo(valintatulos, Collections.singletonList(hakemus));

            HakemusWrapper hakemusWrapper = sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
            assertEquals(HakemuksenTila.VARASIJALTA_HYVAKSYTTY, hakemusWrapper.getHakemus().getTila());
            assertEquals(TilankuvauksenTarkenne.HYVAKSYTTY_VARASIJALTA, hakemusWrapper.getHakemus().getTilankuvauksenTarkenne());
            assertEquals(TilanKuvaukset.varasijaltaHyvaksytty, hakemusWrapper.getHakemus().getTilanKuvaukset());
            assertFalse(hakemusWrapper.isTilaVoidaanVaihtaa());
        }

        @Test
        public void setsMerkitseMyohAuto() {
            Hakemus hakemus = new Hakemus();
            hakemus.setHakemusOid("123");
            List<Valintatapajono> valintatapajonot = generateValintatapajono(new List[]{List.of(hakemus)});
            List<Hakukohde> hakukohteet = generateHakukohteet(valintatapajonot);
            ValintatapajonoDTO jono = new ValintatapajonoDTO();
            jono.setMerkitseMyohAuto(true);
            SijoitteluajoWrapper sijoitteluajoWrapper = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluConfiguration(), new SijoitteluAjo(),
                    hakukohteet, Collections.singletonMap(hakukohteet.get(0).getOid(), Collections.singletonMap(valintatapajonot.get(0).getOid(), jono)));
            assertEquals(1, sijoitteluajoWrapper.getHakukohteet().size());
            assertEquals(1, sijoitteluajoWrapper.getHakukohteet().get(0).getValintatapajonot().size());
            ValintatapajonoWrapper jonoWrapper = sijoitteluajoWrapper.getHakukohteet().get(0).getValintatapajonot().get(0);
            assertTrue(jonoWrapper.getMerkitseMyohAuto());
        }

        @Test
        public void jos_hakemuksken_edellinen_tila_on_HYVAKSYTTY_sallitaan_PERUNUT_tila() {
            Valintatulos valintatulos = valintatulosWithTila(ValintatuloksenTila.EI_VASTAANOTETTU_MAARA_AIKANA);
            Hakemus hakemus = new Hakemus();
            hakemus.setHakemusOid("123");
            TilojenMuokkaus.asetaTilaksiHyvaksytty(hakemus);
            hakemus.setEdellinenTila(HakemuksenTila.HYVAKSYTTY);

            SijoitteluajoWrapper sijoitteluAjo = sijoitteluAjo(valintatulos, Collections.singletonList(hakemus));
            HakemusWrapper hakemusWrapper = sijoitteluAjo.getHakukohteet().getFirst().getValintatapajonot().getFirst().getHakemukset().getFirst();

            assertEquals(HakemuksenTila.PERUNUT, hakemusWrapper.getHakemus().getTila());
            assertEquals(TilanKuvaukset.peruuntunutEiVastaanottanutMaaraaikana, hakemusWrapper.getHakemus().getTilanKuvaukset());
            assertEquals(TilankuvauksenTarkenne.PERUUNTUNUT_EI_VASTAANOTTANUT_MAARAAIKANA, hakemusWrapper.getHakemus().getTilankuvauksenTarkenne());
            assertFalse(hakemusWrapper.isTilaVoidaanVaihtaa());
        }

        @Test
        public void jos_hakemuksen_edellinenTila_on_PERUUTETTU_sallitaan_PERUNUT_tila() {
            Valintatulos valintatulos = valintatulosWithTila(ValintatuloksenTila.PERUNUT);
            Hakemus hakemus = new Hakemus();
            hakemus.setHakemusOid("123");
            TilojenMuokkaus.asetaTilaksiHyvaksytty(hakemus);
            hakemus.setEdellinenTila(HakemuksenTila.PERUUTETTU);
            SijoitteluajoWrapper sijoitteluAjo = sijoitteluAjo(valintatulos, Collections.singletonList(hakemus));
            HakemusWrapper hakemusWrapper = sijoitteluAjo.getHakukohteet().getFirst().getValintatapajonot().getFirst().getHakemukset().getFirst();

            assertEquals(HakemuksenTila.PERUNUT, hakemusWrapper.getHakemus().getTila());
            assertEquals(TilanKuvaukset.tyhja, hakemusWrapper.getHakemus().getTilanKuvaukset());
            assertEquals(TilankuvauksenTarkenne.EI_TILANKUVAUKSEN_TARKENNETTA, hakemusWrapper.getHakemus().getTilankuvauksenTarkenne());
            assertFalse(hakemusWrapper.isTilaVoidaanVaihtaa());
        }
    }

    @Nested
    public class EdellinenTilaHyvaksytty {
        @Test
        public void ei_voi_vaihtua_HYLATTY_tilaan() {
            Valintatulos valintatulos = valintatulosWithTila(ValintatuloksenTila.KESKEN);
            valintatulos.setJulkaistavissa(true, "");
            Hakemus hakemus = new Hakemus();
            hakemus.setHakemusOid("123");
            TilojenMuokkaus.asetaTilaksiHylatty(hakemus, TilanKuvaukset.tyhja);
            hakemus.setEdellinenTila(HakemuksenTila.HYVAKSYTTY);
            final SijoitteluajoWrapper sijoitteluAjo = sijoitteluAjo(valintatulos, Collections.singletonList(hakemus));
            HakemusWrapper hakemusWrapper = sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
            assertHyvaksytty(hakemusWrapper);
        }
    }

    private static void assertHyvaksytty(final HakemusWrapper hakemusWrapper) {
        assertEquals(HakemuksenTila.HYVAKSYTTY, hakemusWrapper.getHakemus().getTila());
        assertEquals(TilankuvauksenTarkenne.EI_TILANKUVAUKSEN_TARKENNETTA, hakemusWrapper.getHakemus().getTilankuvauksenTarkenne());
        assertEquals(TilanKuvaukset.tyhja, hakemusWrapper.getHakemus().getTilanKuvaukset());
    }

    private static void assertPeruuntunut(final HakemusWrapper hakemusWrapper) {
        assertEquals(HakemuksenTila.PERUUNTUNUT, hakemusWrapper.getHakemus().getTila());
        assertEquals(TilankuvauksenTarkenne.PERUUNTUNUT_HYVAKSYTTY_TOISESSA_JONOSSA, hakemusWrapper.getHakemus().getTilankuvauksenTarkenne());
        assertEquals(TilanKuvaukset.peruuntunutHyvaksyttyToisessaJonossa, hakemusWrapper.getHakemus().getTilanKuvaukset());
    }

    private static SijoitteluajoWrapper sijoitteluAjo(Valintatulos valintatulos) {
        return sijoitteluAjo(valintatulos, yksiHakemus());
    }

    private static List<Hakemus> yksiHakemus() {
        Hakemus h = new Hakemus();
        h.setHakemusOid("123");
        TilojenMuokkaus.asetaTilaksiHyvaksytty(h);
        h.setEdellinenTila(HakemuksenTila.HYVAKSYTTY);
        return Collections.singletonList(h);
    }

    private static SijoitteluajoWrapper sijoitteluAjo(Valintatulos valintatulos, List<Hakemus> hakemukset) {
        return sijoitteluAjo(Collections.singletonList(valintatulos), hakemukset);
    }

    private static SijoitteluajoWrapper sijoitteluAjo(List<Valintatulos> valintatulokset, List<Hakemus>... hakemukset) {
        List<Valintatapajono> valintatapajonot = generateValintatapajono(hakemukset);
        final List<Hakukohde> hakukohteet = generateHakukohteet(valintatapajonot);
        SijoitteluajoWrapper sijoitteluajoWrapper = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluConfiguration(), new SijoitteluAjo(), hakukohteet, Collections.emptyMap());
        sijoitteluajoWrapper.paivitaVastaanottojenVaikutusHakemustenTiloihin(valintatulokset, Collections.emptyMap());
        return sijoitteluajoWrapper;
    }

    private static Valintatulos valintatulosWithTila(ValintatuloksenTila tila) {
        return valintatulosWithTila(tila, "023");
    }

    private static Valintatulos valintatulosWithTila(ValintatuloksenTila tila, String jonoId) {
        Valintatulos valintatulos = new Valintatulos();
        valintatulos.setValintatapajonoOid(jonoId, "");
        valintatulos.setHakukohdeOid("123", "");
        valintatulos.setHakemusOid("123", "");
        valintatulos.setTila(tila, "");
        return valintatulos;
    }

    private static List<Hakukohde> generateHakukohteet(List<Valintatapajono> valintatapajonot) {
        Hakukohde hakukohde = new Hakukohde();
        hakukohde.setValintatapajonot(valintatapajonot);
        hakukohde.setOid("123");
        return Collections.singletonList(hakukohde);
    }

    private static List<Valintatapajono> generateValintatapajono(List<Hakemus>[] hakemukset) {
        List<Valintatapajono> jonot = new ArrayList<>(hakemukset.length);
        for (int i = 0; i < hakemukset.length; i++) {
            Valintatapajono valintatapajono = new Valintatapajono();
            valintatapajono.setOid(i + "23");
            valintatapajono.setHakemukset(hakemukset[i]);
            valintatapajono.setPrioriteetti(i);
            jonot.add(valintatapajono);
        }
        return jonot;
    }

}
