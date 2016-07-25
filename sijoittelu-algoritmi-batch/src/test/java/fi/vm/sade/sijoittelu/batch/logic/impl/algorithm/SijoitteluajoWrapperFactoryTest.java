package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import com.google.common.collect.Lists;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilanKuvaukset;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.*;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(Enclosed.class)
public class SijoitteluajoWrapperFactoryTest {
    public static class Valintatuloksen_tilan_vaikutus_hakemukseen {
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
            assertEquals("Peruuntunut, ei vastaanottanut määräaikana", hakemusWrapper.getHakemus().getTilanKuvaukset().get("FI"));
            assertFalse(hakemusWrapper.isTilaVoidaanVaihtaa());
        }

        @Test
        public void PERUUTETTU_peruuttaa_hakemuksen() {
            SijoitteluajoWrapper sijoitteluAjo = sijoitteluAjo(valintatulosWithTila(ValintatuloksenTila.PERUUTETTU));

            assertEquals(sijoitteluAjo.getHakukohteet().size(), 1);
            HakemusWrapper hakemusWrapper = sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
            assertEquals(HakemuksenTila.PERUUTETTU, hakemusWrapper.getHakemus().getTila());
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
            List<Hakemus> hakemuksetYlempiJono = generateHakemuksetEdellisellaTilalla(HakemuksenTila.PERUUNTUNUT, HakemuksenTila.PERUUNTUNUT);

            Valintatulos valintatulosAlempi = valintatulosWithTila(ValintatuloksenTila.KESKEN, "123");
            valintatulosAlempi.setHyvaksyPeruuntunut(true, "");
            valintatulosAlempi.setJulkaistavissa(true, "");
            List<Hakemus> hakemuksetAlempiJono = generateHakemuksetEdellisellaTilalla(HakemuksenTila.HYVAKSYTTY, HakemuksenTila.HYVAKSYTTY);

            SijoitteluajoWrapper sijoitteluAjo = sijoitteluAjo(Lists.newArrayList(valintatulosYlempi, valintatulosAlempi), hakemuksetYlempiJono, hakemuksetAlempiJono);

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
            List<Hakemus> hakemuksetYlempiJono = generateHakemuksetEdellisellaTilalla(HakemuksenTila.HYVAKSYTTY, HakemuksenTila.HYVAKSYTTY);

            Valintatulos valintatulosAlempi = valintatulosWithTila(ValintatuloksenTila.KESKEN, "123");
            valintatulosAlempi.setHyvaksyPeruuntunut(true, "");
            valintatulosAlempi.setJulkaistavissa(true, "");
            List<Hakemus> hakemuksetAlempiJono = generateHakemuksetEdellisellaTilalla(HakemuksenTila.PERUUNTUNUT, HakemuksenTila.PERUUNTUNUT);

            SijoitteluajoWrapper sijoitteluAjo = sijoitteluAjo(Lists.newArrayList(valintatulosYlempi, valintatulosAlempi), hakemuksetYlempiJono, hakemuksetAlempiJono);

            HakemusWrapper hakemusWrapperYlempi = sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
            assertHyvaksytty(hakemusWrapperYlempi);
            assertFalse(hakemusWrapperYlempi.isTilaVoidaanVaihtaa());

            HakemusWrapper hakemusWrapperAlempi = sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(1).getHakemukset().get(0);
            assertPeruuntunut(hakemusWrapperAlempi);
            assertFalse(hakemusWrapperAlempi.isTilaVoidaanVaihtaa());
        }

        @Test
        public void hyvaksyttyVarasijalta_flag_ei_hyvaksy_hakemusta_jos_korkeampi_jo_hyvaksytty() {
            Valintatulos valintatulosYlempi = valintatulosWithTila(ValintatuloksenTila.KESKEN);
            valintatulosYlempi.setHyvaksyttyVarasijalta(true, "");
            valintatulosYlempi.setJulkaistavissa(true, "");
            List<Hakemus> hakemuksetYlempiJono = generateHakemuksetEdellisellaTilalla(HakemuksenTila.HYVAKSYTTY, HakemuksenTila.HYVAKSYTTY);

            Valintatulos valintatulosAlempi = valintatulosWithTila(ValintatuloksenTila.KESKEN, "123");
            valintatulosAlempi.setHyvaksyttyVarasijalta(true, "");
            valintatulosAlempi.setJulkaistavissa(true, "");
            List<Hakemus> hakemuksetAlempiJono = generateHakemuksetEdellisellaTilalla(HakemuksenTila.PERUUNTUNUT, HakemuksenTila.PERUUNTUNUT);

            SijoitteluajoWrapper sijoitteluAjo = sijoitteluAjo(Lists.newArrayList(valintatulosYlempi, valintatulosAlempi), hakemuksetYlempiJono, hakemuksetAlempiJono);

            HakemusWrapper hakemusWrapperYlempi = sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
            assertHyvaksytty(hakemusWrapperYlempi);
            assertFalse(hakemusWrapperYlempi.isTilaVoidaanVaihtaa());

            HakemusWrapper hakemusWrapperAlempi = sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(1).getHakemukset().get(0);
            assertPeruuntunut(hakemusWrapperAlempi);
            assertFalse(hakemusWrapperAlempi.isTilaVoidaanVaihtaa());
        }

        @Test
        public void hyvaksyVarasijalta_flag_hyvaksyy_hakemuksen() {
            Valintatulos valintatulos = valintatulosWithTila(ValintatuloksenTila.KESKEN);
            valintatulos.setHyvaksyttyVarasijalta(true, "");
            List<Hakemus> hakemukset = generateHakemuksetEdellisellaTilalla(HakemuksenTila.HYVAKSYTTY, HakemuksenTila.HYVAKSYTTY);
            SijoitteluajoWrapper sijoitteluAjo = sijoitteluAjo(valintatulos, hakemukset);

            HakemusWrapper hakemusWrapper = sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
            assertEquals(HakemuksenTila.VARASIJALTA_HYVAKSYTTY, hakemusWrapper.getHakemus().getTila());
            assertEquals("Varasijalta hyväksytty", hakemusWrapper.getHakemus().getTilanKuvaukset().get("FI"));
            assertFalse(hakemusWrapper.isTilaVoidaanVaihtaa());
        }
    }

    public static class EdellinenTilaHyvaksytty {
        @Test
        public void ei_voi_vaihtua_HYLATTY_tilaan() {
            Valintatulos valintatulos = valintatulosWithTila(ValintatuloksenTila.KESKEN);
            valintatulos.setJulkaistavissa(true, "");
            final List<Hakemus> hakemukset = generateHakemuksetEdellisellaTilalla(HakemuksenTila.HYVAKSYTTY, HakemuksenTila.HYLATTY);
            asetaRandomTilanKuvaus(hakemukset);
            final SijoitteluajoWrapper sijoitteluAjo = sijoitteluAjo(valintatulos, hakemukset);
            HakemusWrapper hakemusWrapper = sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
            assertHyvaksytty(hakemusWrapper);
        }
    }

    private static List<Hakemus> asetaRandomTilanKuvaus(final List<Hakemus> hakemukset) {
        hakemukset.get(0).setTilanKuvaukset(TilanKuvaukset.hylattyHakijaryhmaanKuulumattomana("blah"));
        return hakemukset;
    }

    private static void assertHyvaksytty(final HakemusWrapper hakemusWrapper) {
        assertEquals(HakemuksenTila.HYVAKSYTTY, hakemusWrapper.getHakemus().getTila());
        assertEquals(TilanKuvaukset.tyhja, hakemusWrapper.getHakemus().getTilanKuvaukset());
    }

    private static void assertPeruuntunut(final HakemusWrapper hakemusWrapper) {
        assertEquals(HakemuksenTila.PERUUNTUNUT, hakemusWrapper.getHakemus().getTila());
        assertEquals(TilanKuvaukset.tyhja, hakemusWrapper.getHakemus().getTilanKuvaukset());
    }

    private static SijoitteluajoWrapper sijoitteluAjo(Valintatulos valintatulos) {
        return sijoitteluAjo(valintatulos, yksiHakemus());
    }

    private static List<Hakemus> yksiHakemus() {
        Hakemus h = new Hakemus();
        h.setHakemusOid("123");
        h.setTila(HakemuksenTila.HYVAKSYTTY);
        h.setEdellinenTila(HakemuksenTila.HYVAKSYTTY);
        return asetaRandomTilanKuvaus(Collections.singletonList(h));
    }

    private static SijoitteluajoWrapper sijoitteluAjo(Valintatulos valintatulos, List<Hakemus> hakemukset) {
        return sijoitteluAjo(Collections.singletonList(valintatulos), hakemukset);
    }

    private static SijoitteluajoWrapper sijoitteluAjo(List<Valintatulos> valintatulokset, List<Hakemus>... hakemukset) {
        List<Valintatapajono> valintatapajonot = generateValintatapajono(hakemukset);
        final List<Hakukohde> hakukohteet = generateHakukohteet(valintatapajonot);
        return SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluAjo(), hakukohteet, valintatulokset, Collections.emptyMap());
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

    private static List<Hakemus> generateHakemuksetEdellisellaTilalla(HakemuksenTila edellinenTila, HakemuksenTila tila) {
        Hakemus hakemus = new Hakemus();
        hakemus.setHakemusOid("123");
        hakemus.setEdellinenTila(edellinenTila);
        hakemus.setTila(tila);
        return Collections.singletonList(hakemus);
    }
}
