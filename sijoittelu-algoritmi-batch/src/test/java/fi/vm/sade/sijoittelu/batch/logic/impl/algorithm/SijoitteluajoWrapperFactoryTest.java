package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilanKuvaukset;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.*;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

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
        public void hyvaksyPeruuntunut_flag_hyvaksyy_hakemuksen() {
            Valintatulos valintatulos = valintatulosWithTila(ValintatuloksenTila.KESKEN);
            valintatulos.setHyvaksyPeruuntunut(true, "");
            List<Hakemus> hakemukset = generateHakemuksetEdellisellaTilalla(HakemuksenTila.PERUUNTUNUT, HakemuksenTila.PERUUNTUNUT);
            SijoitteluajoWrapper sijoitteluAjo = sijoitteluAjo(valintatulos, hakemukset);

            HakemusWrapper hakemusWrapper = sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
            assertHyvaksytty(hakemusWrapper);
            assertFalse(hakemusWrapper.isTilaVoidaanVaihtaa());
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

    private static SijoitteluajoWrapper sijoitteluAjo(Valintatulos valintatulos) {
        return sijoitteluAjo(valintatulos, yksiHakemus());
    }

    private static List<Hakemus> yksiHakemus() {
        Hakemus h = new Hakemus();
        h.setHakemusOid("123");
        h.setTila(HakemuksenTila.HYVAKSYTTY);
        return asetaRandomTilanKuvaus(Collections.singletonList(h));
    }

    private static SijoitteluajoWrapper sijoitteluAjo(Valintatulos valintatulos, List<Hakemus> hakemukset) {
        List<Valintatapajono> valintatapajonot = generateValintatapajono(hakemukset);
        final List<Hakukohde> hakukohteet = generateHakukohteet(valintatapajonot);
        final List<Valintatulos> valintatulokset = Collections.singletonList(valintatulos);
        return SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluAjo(), hakukohteet, valintatulokset);
    }

    private static Valintatulos valintatulosWithTila(ValintatuloksenTila tila) {
        Valintatulos valintatulos = new Valintatulos();
        valintatulos.setValintatapajonoOid("123", "");
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

    private static List<Valintatapajono> generateValintatapajono(List<Hakemus> hakemukset) {
        Valintatapajono valintatapajono = new Valintatapajono();
        valintatapajono.setOid("123");
        valintatapajono.setHakemukset(hakemukset);
        return Collections.singletonList(valintatapajono);
    }

    private static List<Hakemus> generateHakemuksetEdellisellaTilalla(HakemuksenTila edellinenTila, HakemuksenTila tila) {
        Hakemus hakemus = new Hakemus();
        hakemus.setHakemusOid("123");
        hakemus.setEdellinenTila(edellinenTila);
        hakemus.setTila(tila);
        return Collections.singletonList(hakemus);
    }
}
