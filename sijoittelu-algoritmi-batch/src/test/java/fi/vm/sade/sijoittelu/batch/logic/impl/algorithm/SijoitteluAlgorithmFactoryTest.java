package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.domain.*;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class SijoitteluAlgorithmFactoryTest {

    private final SijoitteluAlgorithmFactory sijoitteluAlgorithmFactory = new SijoitteluAlgorithmFactory();

    @Test
    public void testconstructAlgorithm_PERUNUT() {
        SijoitteluAlgorithm sijoitteluAlgorithm = sijoitteluAlgorithm(valintatulosWithTila(ValintatuloksenTila.PERUNUT));

        assertEquals(sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().size(), 1);
        HakemusWrapper hakemusWrapper = sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
        assertEquals(hakemusWrapper.getHakemus().getTila(), HakemuksenTila.PERUNUT);
        assertFalse(hakemusWrapper.isTilaVoidaanVaihtaa());
    }

    @Test
    public void testconstructAlgorithm_EHDOLLISESTI_VASTAANOTTANUT() {
        Valintatulos valintatulos = valintatulosWithTila(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT);
        SijoitteluAlgorithm sijoitteluAlgorithm = sijoitteluAlgorithm(valintatulos);

        assertEquals(sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().size(), 1);
        HakemusWrapper hakemusWrapper = sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
        assertEquals(hakemusWrapper.getHakemus().getTila(), HakemuksenTila.HYVAKSYTTY);
        assertEquals(hakemusWrapper.getHakemus().getIlmoittautumisTila(), valintatulos.getIlmoittautumisTila());
        assertTrue(!hakemusWrapper.isTilaVoidaanVaihtaa());
    }

    @Test
    public void testconstructAlgorithm_EI_VASTAANOTETTU_MAARA_AIKANA() {
        SijoitteluAlgorithm sijoitteluAlgorithm = sijoitteluAlgorithm(valintatulosWithTila(ValintatuloksenTila.EI_VASTAANOTETTU_MAARA_AIKANA));

        assertEquals(sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().size(), 1);
        HakemusWrapper hakemusWrapper = sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
        assertEquals(hakemusWrapper.getHakemus().getTila(), HakemuksenTila.PERUNUT);
        assertFalse(hakemusWrapper.isTilaVoidaanVaihtaa());
    }

    @Test
    public void testconstructAlgorithm_PERUUTETTU() {
        SijoitteluAlgorithm sijoitteluAlgorithm = sijoitteluAlgorithm(valintatulosWithTila(ValintatuloksenTila.PERUUTETTU));

        assertEquals(sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().size(), 1);
        HakemusWrapper hakemusWrapper = sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
        assertEquals(hakemusWrapper.getHakemus().getTila(), HakemuksenTila.PERUUTETTU);
        assertTrue(!hakemusWrapper.isTilaVoidaanVaihtaa());
    }

    @Test
    public void testconstructAlgorithm_VASTAANOTTANUT_SITOVASTI() {
        Valintatulos valintatulos = valintatulosWithTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI);
        valintatulos.setJulkaistavissa(true);
        SijoitteluAlgorithm sijoitteluAlgorithm = sijoitteluAlgorithm(valintatulos);

        assertEquals(sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().size(), 1);
        HakemusWrapper hakemusWrapper = sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
        assertEquals(hakemusWrapper.getHakemus().getTila(), HakemuksenTila.HYVAKSYTTY);
        assertEquals(hakemusWrapper.getHakemus().getIlmoittautumisTila(), valintatulos.getIlmoittautumisTila());
        assertFalse(hakemusWrapper.isTilaVoidaanVaihtaa());
    }

    @Test
    public void testconstructAlgorithm_ILMOITETTU() {
        Valintatulos valintatulos = valintatulosWithTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI);
        valintatulos.setJulkaistavissa(true);
        SijoitteluAlgorithm sijoitteluAlgorithm = sijoitteluAlgorithm(valintatulos);

        assertEquals(sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().size(), 1);
        HakemusWrapper hakemusWrapper = sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
        assertEquals(hakemusWrapper.getHakemus().getTila(), HakemuksenTila.HYVAKSYTTY);
        assertFalse(hakemusWrapper.isTilaVoidaanVaihtaa());
    }

    @Test
    public void testconstructAlgorithm_VASTAANOTTANUT() {
        Valintatulos valintatulos = valintatulosWithTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI);
        valintatulos.setJulkaistavissa(true);
        SijoitteluAlgorithm sijoitteluAlgorithm = sijoitteluAlgorithm(valintatulos);

        assertEquals(sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().size(), 1);
        HakemusWrapper hakemusWrapper = sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
        assertEquals(hakemusWrapper.getHakemus().getTila(), HakemuksenTila.HYVAKSYTTY);
        assertFalse(hakemusWrapper.isTilaVoidaanVaihtaa());
    }

    @Test
    public void testconstructAlgorithm_PERUUNTUNUT() {
        Valintatulos valintatulos = valintatulosWithTila(ValintatuloksenTila.KESKEN);
        valintatulos.setHyvaksyPeruuntunut(true);
        SijoitteluAlgorithm sijoitteluAlgorithm = sijoitteluAlgorithm(valintatulos, HakemuksenTila.PERUUNTUNUT);

        HakemusWrapper hakemusWrapper = sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
        assertEquals(hakemusWrapper.getHakemus().getTila(), HakemuksenTila.HYVAKSYTTY);
        assertFalse(hakemusWrapper.isTilaVoidaanVaihtaa());
    }

    private SijoitteluAlgorithm sijoitteluAlgorithm(Valintatulos valintatulos) {
        return sijoitteluAlgorithm(valintatulos, HakemuksenTila.HYVAKSYTTY);
    }

    private SijoitteluAlgorithm sijoitteluAlgorithm(Valintatulos valintatulos, HakemuksenTila tila) {
        return sijoitteluAlgorithmFactory.constructAlgorithm(generateHakukohteet(tila), Collections.singletonList(valintatulos));
    }

    private Valintatulos valintatulosWithTila(ValintatuloksenTila tila) {
        Valintatulos valintatulos = new Valintatulos();
        valintatulos.setValintatapajonoOid("123");
        valintatulos.setHakukohdeOid("123");
        valintatulos.setHakemusOid("123");
        valintatulos.setTila(tila);
        return valintatulos;
    }

    private List<Hakukohde> generateHakukohteet(HakemuksenTila tila) {
        Hakukohde hakukohde = new Hakukohde();
        hakukohde.setValintatapajonot(generateValintatapajono(tila));
        hakukohde.setOid("123");
        return Collections.singletonList(hakukohde);
    }

    private List<Valintatapajono> generateValintatapajono(HakemuksenTila tila) {
        Valintatapajono valintatapajono = new Valintatapajono();
        valintatapajono.setOid("123");
        List<Hakemus> hakemukset = generateHakemukset(tila);
        valintatapajono.setHakemukset(hakemukset);
        return Collections.singletonList(valintatapajono);
    }

    private List<Hakemus> generateHakemukset(HakemuksenTila tila) {
        Hakemus hakemus = new Hakemus();
        hakemus.setHakemusOid("123");
        hakemus.setTila(tila);
        return Collections.singletonList(hakemus);
    }
}
