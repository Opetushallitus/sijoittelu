package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;


import fi.vm.sade.sijoittelu.domain.*;
import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SijoitteluAlgorithmFactoryTest {
    @Test
    public void testconstructAlgorithm_PERUNUT() {
        SijoitteluAlgorithmFactory sijoitteluAlgorithmFactory = new SijoitteluAlgorithmFactory();

        List<Valintatulos> valintatulokset = new ArrayList<>();
        Valintatulos valintatulos = new Valintatulos();
        valintatulos.setValintatapajonoOid("123");
        valintatulos.setHakukohdeOid("123");
        valintatulos.setHakemusOid("123");
        valintatulos.setTila(ValintatuloksenTila.PERUNUT);

        valintatulokset.add(valintatulos);

        SijoitteluAlgorithm  sijoitteluAlgorithm = sijoitteluAlgorithmFactory.constructAlgorithm(generateHakukohteet(), valintatulokset);

        Assert.assertEquals(sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().size(), 1);
        Assert.assertEquals(sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().
                        get(0).getHakemukset().get(0).getHakemus().getTila(), HakemuksenTila.PERUNUT);
        Assert.assertFalse(sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().
                get(0).getHakemukset().get(0).isTilaVoidaanVaihtaa());


    }

    @Test
    public void testconstructAlgorithm_EHDOLLISESTI_VASTAANOTTANUT() {
        SijoitteluAlgorithmFactory sijoitteluAlgorithmFactory = new SijoitteluAlgorithmFactory();

        List<Valintatulos> valintatulokset = new ArrayList<>();
        Valintatulos valintatulos = new Valintatulos();
        valintatulos.setValintatapajonoOid("123");
        valintatulos.setHakukohdeOid("123");
        valintatulos.setHakemusOid("123");
        valintatulos.setTila(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT);

        valintatulokset.add(valintatulos);

        SijoitteluAlgorithm  sijoitteluAlgorithm = sijoitteluAlgorithmFactory.constructAlgorithm(generateHakukohteet(), valintatulokset);

        Assert.assertEquals(sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().size(), 1);
        Assert.assertEquals(sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().
                get(0).getHakemukset().get(0).getHakemus().getTila(), HakemuksenTila.HYVAKSYTTY);
        Assert.assertEquals(sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().
                get(0).getHakemukset().get(0).getHakemus().getIlmoittautumisTila(), valintatulos.getIlmoittautumisTila());
        Assert.assertTrue(!sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().
                get(0).getHakemukset().get(0).isTilaVoidaanVaihtaa());

    }


    @Test
    public void testconstructAlgorithm_EI_VASTAANOTETTU_MAARA_AIKANA() {
        SijoitteluAlgorithmFactory sijoitteluAlgorithmFactory = new SijoitteluAlgorithmFactory();

        List<Valintatulos> valintatulokset = new ArrayList<>();
        Valintatulos valintatulos = new Valintatulos();
        valintatulos.setValintatapajonoOid("123");
        valintatulos.setHakukohdeOid("123");
        valintatulos.setHakemusOid("123");
        valintatulos.setTila(ValintatuloksenTila.EI_VASTAANOTETTU_MAARA_AIKANA);

        valintatulokset.add(valintatulos);

        SijoitteluAlgorithm  sijoitteluAlgorithm = sijoitteluAlgorithmFactory.constructAlgorithm(
                generateHakukohteet(), valintatulokset);

        Assert.assertEquals(sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().size(), 1);
        Assert.assertEquals(sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().
                get(0).getHakemukset().get(0).getHakemus().getTila(), HakemuksenTila.PERUNUT);

        Assert.assertFalse(sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().
                get(0).getHakemukset().get(0).isTilaVoidaanVaihtaa());

    }


    @Test
    public void testconstructAlgorithm_PERUUTETTU() {
        SijoitteluAlgorithmFactory sijoitteluAlgorithmFactory = new SijoitteluAlgorithmFactory();

        List<Valintatulos> valintatulokset = new ArrayList<>();
        Valintatulos valintatulos = new Valintatulos();
        valintatulos.setValintatapajonoOid("123");
        valintatulos.setHakukohdeOid("123");
        valintatulos.setHakemusOid("123");
        valintatulos.setTila(ValintatuloksenTila.PERUUTETTU);

        valintatulokset.add(valintatulos);

        SijoitteluAlgorithm  sijoitteluAlgorithm = sijoitteluAlgorithmFactory.constructAlgorithm(
                generateHakukohteet(), valintatulokset);

        Assert.assertEquals(sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().size(), 1);
        Assert.assertEquals(sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().
                get(0).getHakemukset().get(0).getHakemus().getTila(), HakemuksenTila.PERUUTETTU);
        Assert.assertTrue(!sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().
                get(0).getHakemukset().get(0).isTilaVoidaanVaihtaa());

    }

    @Test
    public void testconstructAlgorithm_VASTAANOTTANUT_SITOVASTI() {
        SijoitteluAlgorithmFactory sijoitteluAlgorithmFactory = new SijoitteluAlgorithmFactory();

        List<Valintatulos> valintatulokset = new ArrayList<>();
        Valintatulos valintatulos = new Valintatulos();
        valintatulos.setValintatapajonoOid("123");
        valintatulos.setHakukohdeOid("123");
        valintatulos.setHakemusOid("123");
        valintatulos.setJulkaistavissa(true);
        valintatulos.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI);

        valintatulokset.add(valintatulos);

        SijoitteluAlgorithm  sijoitteluAlgorithm = sijoitteluAlgorithmFactory.constructAlgorithm(
                generateHakukohteet(), valintatulokset);

        Assert.assertEquals(sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().size(), 1);
        Assert.assertEquals(sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().
                get(0).getHakemukset().get(0).getHakemus().getTila(), HakemuksenTila.HYVAKSYTTY);
        Assert.assertEquals(sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().
                get(0).getHakemukset().get(0).getHakemus().getIlmoittautumisTila(), valintatulos.getIlmoittautumisTila());

        Assert.assertFalse(sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().
                get(0).getHakemukset().get(0).isTilaVoidaanVaihtaa());

    }


    @Test
    public void testconstructAlgorithm_ILMOITETTU() {
        SijoitteluAlgorithmFactory sijoitteluAlgorithmFactory = new SijoitteluAlgorithmFactory();

        List<Valintatulos> valintatulokset = new ArrayList<>();
        Valintatulos valintatulos = new Valintatulos();
        valintatulos.setValintatapajonoOid("123");
        valintatulos.setHakukohdeOid("123");
        valintatulos.setHakemusOid("123");
        valintatulos.setJulkaistavissa(true);

        valintatulos.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI);

        valintatulokset.add(valintatulos);

        SijoitteluAlgorithm  sijoitteluAlgorithm = sijoitteluAlgorithmFactory.constructAlgorithm(
                generateHakukohteet(), valintatulokset);

        Assert.assertEquals(sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().size(), 1);
        Assert.assertEquals(sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().
                get(0).getHakemukset().get(0).getHakemus().getTila(), HakemuksenTila.HYVAKSYTTY);
        Assert.assertFalse(sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().
                get(0).getHakemukset().get(0).isTilaVoidaanVaihtaa());

    }

    @Test
    public void testconstructAlgorithm_VASTAANOTTANUT() {
        SijoitteluAlgorithmFactory sijoitteluAlgorithmFactory = new SijoitteluAlgorithmFactory();

        List<Valintatulos> valintatulokset = new ArrayList<>();
        Valintatulos valintatulos = new Valintatulos();
        valintatulos.setValintatapajonoOid("123");
        valintatulos.setHakukohdeOid("123");
        valintatulos.setHakemusOid("123");
        valintatulos.setJulkaistavissa(true);

        valintatulos.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI);

        valintatulokset.add(valintatulos);

        SijoitteluAlgorithm  sijoitteluAlgorithm = sijoitteluAlgorithmFactory.constructAlgorithm(
                generateHakukohteet(), valintatulokset);

        Assert.assertEquals(sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().size(), 1);
        Assert.assertEquals(sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().
                get(0).getHakemukset().get(0).getHakemus().getTila(), HakemuksenTila.HYVAKSYTTY);
        Assert.assertFalse(sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().
                get(0).getHakemukset().get(0).isTilaVoidaanVaihtaa());

    }

    private List<Hakukohde> generateHakukohteet() {
        List<Hakukohde> hakukohdes = new ArrayList<>();

        Hakukohde hakukohde = new Hakukohde();
        hakukohde.setValintatapajonot(generateValintatapajono());
        hakukohde.setOid("123");
        hakukohdes.add(hakukohde);

        return hakukohdes;
    }

    private List<Valintatapajono> generateValintatapajono() {
        List<Valintatapajono> valintatapajonot = new ArrayList<>();

        Valintatapajono valintatapajono = new Valintatapajono();
        valintatapajono.setOid("123");

        ArrayList<Hakemus> hakemukset = (ArrayList<Hakemus>) generateHakemukset();
        valintatapajono.setHakemukset(hakemukset);

        valintatapajonot.add(valintatapajono);
        return valintatapajonot;
    }

    private List<Hakemus> generateHakemukset() {
        List<Hakemus> hakemukset = new ArrayList<>();
        Hakemus hakemus = new Hakemus();
        hakemus.setHakemusOid("123");
        hakemukset.add(hakemus);
        return hakemukset;
    }
}
