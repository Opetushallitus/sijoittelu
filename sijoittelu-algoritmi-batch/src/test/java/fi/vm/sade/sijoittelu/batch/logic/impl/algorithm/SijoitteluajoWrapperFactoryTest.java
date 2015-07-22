package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.*;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class SijoitteluajoWrapperFactoryTest {
    @Test
    public void testconstructAlgorithm_PERUNUT() {
        SijoitteluajoWrapper sijoitteluAjo = sijoitteluAjo(valintatulosWithTila(ValintatuloksenTila.PERUNUT));

        assertEquals(sijoitteluAjo.getHakukohteet().size(), 1);
        HakemusWrapper hakemusWrapper = sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
        assertEquals(hakemusWrapper.getHakemus().getTila(), HakemuksenTila.PERUNUT);
        assertFalse(hakemusWrapper.isTilaVoidaanVaihtaa());
    }

    @Test
    public void testconstructAlgorithm_EHDOLLISESTI_VASTAANOTTANUT() {
        Valintatulos valintatulos = valintatulosWithTila(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT);
        SijoitteluajoWrapper sijoitteluAjo = sijoitteluAjo(valintatulos);

        assertEquals(sijoitteluAjo.getHakukohteet().size(), 1);
        HakemusWrapper hakemusWrapper = sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
        assertEquals(hakemusWrapper.getHakemus().getTila(), HakemuksenTila.HYVAKSYTTY);
        assertEquals(hakemusWrapper.getHakemus().getIlmoittautumisTila(), valintatulos.getIlmoittautumisTila());
        assertTrue(!hakemusWrapper.isTilaVoidaanVaihtaa());
    }

    @Test
    public void testconstructAlgorithm_EI_VASTAANOTETTU_MAARA_AIKANA() {
        SijoitteluajoWrapper sijoitteluAjo = sijoitteluAjo(valintatulosWithTila(ValintatuloksenTila.EI_VASTAANOTETTU_MAARA_AIKANA));

        assertEquals(sijoitteluAjo.getHakukohteet().size(), 1);
        HakemusWrapper hakemusWrapper = sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
        assertEquals(hakemusWrapper.getHakemus().getTila(), HakemuksenTila.PERUNUT);
        assertFalse(hakemusWrapper.isTilaVoidaanVaihtaa());
    }

    @Test
    public void testconstructAlgorithm_PERUUTETTU() {
        SijoitteluajoWrapper sijoitteluAjo = sijoitteluAjo(valintatulosWithTila(ValintatuloksenTila.PERUUTETTU));

        assertEquals(sijoitteluAjo.getHakukohteet().size(), 1);
        HakemusWrapper hakemusWrapper = sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
        assertEquals(hakemusWrapper.getHakemus().getTila(), HakemuksenTila.PERUUTETTU);
        assertTrue(!hakemusWrapper.isTilaVoidaanVaihtaa());
    }

    @Test
    public void testconstructAlgorithm_VASTAANOTTANUT_SITOVASTI() {
        Valintatulos valintatulos = valintatulosWithTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI);
        valintatulos.setJulkaistavissa(true);
        SijoitteluajoWrapper sijoitteluAjo = sijoitteluAjo(valintatulos);

        assertEquals(sijoitteluAjo.getHakukohteet().size(), 1);
        HakemusWrapper hakemusWrapper = sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
        assertEquals(hakemusWrapper.getHakemus().getTila(), HakemuksenTila.HYVAKSYTTY);
        assertEquals(hakemusWrapper.getHakemus().getIlmoittautumisTila(), valintatulos.getIlmoittautumisTila());
        assertFalse(hakemusWrapper.isTilaVoidaanVaihtaa());
    }

    @Test
    public void testconstructAlgorithm_ILMOITETTU() {
        Valintatulos valintatulos = valintatulosWithTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI);
        valintatulos.setJulkaistavissa(true);
        SijoitteluajoWrapper sijoitteluAjo = sijoitteluAjo(valintatulos);

        assertEquals(sijoitteluAjo.getHakukohteet().size(), 1);
        HakemusWrapper hakemusWrapper = sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
        assertEquals(hakemusWrapper.getHakemus().getTila(), HakemuksenTila.HYVAKSYTTY);
        assertFalse(hakemusWrapper.isTilaVoidaanVaihtaa());
    }

    @Test
    public void testconstructAlgorithm_VASTAANOTTANUT() {
        Valintatulos valintatulos = valintatulosWithTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI);
        valintatulos.setJulkaistavissa(true);
        SijoitteluajoWrapper sijoitteluAjo = sijoitteluAjo(valintatulos);

        assertEquals(sijoitteluAjo.getHakukohteet().size(), 1);
        HakemusWrapper hakemusWrapper = sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
        assertEquals(hakemusWrapper.getHakemus().getTila(), HakemuksenTila.HYVAKSYTTY);
        assertFalse(hakemusWrapper.isTilaVoidaanVaihtaa());
    }

    @Test
    public void testconstructAlgorithm_PERUUNTUNUT() {
        Valintatulos valintatulos = valintatulosWithTila(ValintatuloksenTila.KESKEN);
        valintatulos.setHyvaksyPeruuntunut(true);
        List<Hakemus> hakemukset = generateHakemukset(HakemuksenTila.PERUUNTUNUT, HakemuksenTila.PERUUNTUNUT);
        SijoitteluajoWrapper sijoitteluAjo = sijoitteluAjo(valintatulos, hakemukset);

        HakemusWrapper hakemusWrapper = sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
        assertEquals(HakemuksenTila.HYVAKSYTTY, hakemusWrapper.getHakemus().getTila());
        assertFalse(hakemusWrapper.isTilaVoidaanVaihtaa());
    }

    private SijoitteluajoWrapper sijoitteluAjo(Valintatulos valintatulos) {
        final Hakemus hakemus = new Hakemus();
        hakemus.setHakemusOid("123");
        return sijoitteluAjo(valintatulos, Collections.singletonList(hakemus));
    }

    private SijoitteluajoWrapper sijoitteluAjo(Valintatulos valintatulos, List<Hakemus> hakemukset) {
        List<Valintatapajono> valintatapajonot = generateValintatapajono(hakemukset);
        final List<Hakukohde> hakukohteet = generateHakukohteet(valintatapajonot);
        final List<Valintatulos> valintatulokset = Collections.singletonList(valintatulos);
        return SijoitteluajoWrapperFactory.createSijoitteluAjo(hakukohteet, valintatulokset);
    }

    private Valintatulos valintatulosWithTila(ValintatuloksenTila tila) {
        Valintatulos valintatulos = new Valintatulos();
        valintatulos.setValintatapajonoOid("123");
        valintatulos.setHakukohdeOid("123");
        valintatulos.setHakemusOid("123");
        valintatulos.setTila(tila);
        return valintatulos;
    }

    private List<Hakukohde> generateHakukohteet(List<Valintatapajono> valintatapajonot) {
        Hakukohde hakukohde = new Hakukohde();
        hakukohde.setValintatapajonot(valintatapajonot);
        hakukohde.setOid("123");
        return Collections.singletonList(hakukohde);
    }

    private List<Valintatapajono> generateValintatapajono(List<Hakemus> hakemukset) {
        Valintatapajono valintatapajono = new Valintatapajono();
        valintatapajono.setOid("123");
        valintatapajono.setHakemukset(hakemukset);
        return Collections.singletonList(valintatapajono);
    }

    private List<Hakemus> generateHakemukset(HakemuksenTila edellinenTila, HakemuksenTila tila) {
        Hakemus hakemus = new Hakemus();
        hakemus.setHakemusOid("123");
        hakemus.setEdellinenTila(edellinenTila);
        hakemus.setTila(tila);
        return Collections.singletonList(hakemus);
    }
}
