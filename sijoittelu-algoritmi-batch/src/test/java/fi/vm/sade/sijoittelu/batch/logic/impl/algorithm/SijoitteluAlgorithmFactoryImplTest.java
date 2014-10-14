package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;


import fi.vm.sade.sijoittelu.domain.*;
import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SijoitteluAlgorithmFactoryImplTest {
    @Test
    public void testconstructAlgorithm_Perunut() {
        SijoitteluAlgorithmFactory sijoitteluAlgorithmFactory = new SijoitteluAlgorithmFactoryImpl();

        List<Valintatulos> valintatulokset = new ArrayList<>();
        Valintatulos valintatulos = new Valintatulos();
        valintatulos.setValintatapajonoOid("123");
        valintatulos.setHakukohdeOid("123");
        valintatulos.setHakemusOid("123");
        valintatulos.setTila(ValintatuloksenTila.PERUNUT);

        valintatulokset.add(valintatulos);

        SijoitteluAlgorithmImpl  sijoitteluAlgorithm = (SijoitteluAlgorithmImpl) sijoitteluAlgorithmFactory.constructAlgorithm(
                generateHakukohteet(), valintatulokset);

        Assert.assertEquals(sijoitteluAlgorithm.sijoitteluAjo.getHakukohteet().size(), 1);
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
