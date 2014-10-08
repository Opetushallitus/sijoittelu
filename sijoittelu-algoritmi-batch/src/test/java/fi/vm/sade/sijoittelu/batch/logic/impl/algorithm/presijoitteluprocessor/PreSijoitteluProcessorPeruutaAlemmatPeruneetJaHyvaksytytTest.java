package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;


import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.*;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


public class PreSijoitteluProcessorPeruutaAlemmatPeruneetJaHyvaksytytTest {
    @Test
    public void testProcess_HakemuksenTila_Varalla() {
  /*      PreSijoitteluProcessorPeruutaAlemmatPeruneetJaHyvaksytyt pre
                = new PreSijoitteluProcessorPeruutaAlemmatPeruneetJaHyvaksytyt();
        SijoitteluajoWrapper sijoitteluajoWrapper = generateSijoitteluajoWrapper();

        HakemusWrapper hakemus = new HakemusWrapper();
        HakemusWrapper hakemus2 = new HakemusWrapper();

        Hakemus h = new Hakemus();
        h.setPrioriteetti(1);
        h.setTila(HakemuksenTila.HYVAKSYTTY);
        hakemus.setHakemus(h);
        Hakemus h2 = new Hakemus();
        h2.setPrioriteetti(2);
        h2.setTila(HakemuksenTila.VARALLA);
        hakemus2.setHakemus(h2);



        HenkiloWrapper henkilo = generateHenkiloWrapper();
        henkilo.getHakemukset().add(hakemus);
        henkilo.getHakemukset().add(hakemus2);
        hakemus.setHenkilo(henkilo);
        sijoitteluajoWrapper.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().add(hakemus);


        hakemus2.setHenkilo(henkilo);
        sijoitteluajoWrapper.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().add(hakemus2);



        pre.process(sijoitteluajoWrapper);
        Assert.assertEquals(sijoitteluajoWrapper.getHakukohteet().get(0)
                .getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getTila(), HakemuksenTila.HYVAKSYTTY);
        Assert.assertEquals(sijoitteluajoWrapper.getHakukohteet().get(0)
                .getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getTila(), HakemuksenTila.PERUUNTUNUT);*/
    }
/*
    @Test
    public void testProcess_Prioriteetti_Alempi_Kuin_Varalla() {
        PreSijoitteluProcessorPeruutaAlemmatPeruneetJaHyvaksytyt pre
                = new PreSijoitteluProcessorPeruutaAlemmatPeruneetJaHyvaksytyt();
        SijoitteluajoWrapper sijoitteluajoWrapper = generateSijoitteluajoWrapper();

        HakemusWrapper hakemus = new HakemusWrapper();
        HakemusWrapper hakemus2 = new HakemusWrapper();

        Hakemus h = new Hakemus();
        h.setPrioriteetti(2);
        h.setTila(HakemuksenTila.HYVAKSYTTY);
        hakemus.setHakemus(h);
        Hakemus h2 = new Hakemus();
        h2.setPrioriteetti(1);
        h2.setTila(HakemuksenTila.VARALLA);
        hakemus2.setHakemus(h2);



        HenkiloWrapper henkilo = generateHenkiloWrapper();
        henkilo.getHakemukset().add(hakemus);
        henkilo.getHakemukset().add(hakemus2);
        hakemus.setHenkilo(henkilo);
        sijoitteluajoWrapper.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().add(hakemus);
        hakemus2.setHenkilo(henkilo);
        sijoitteluajoWrapper.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().add(hakemus2);



        pre.process(sijoitteluajoWrapper);
        Assert.assertEquals(sijoitteluajoWrapper.getHakukohteet().get(0)
                .getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getTila(), HakemuksenTila.HYVAKSYTTY);
        Assert.assertEquals(sijoitteluajoWrapper.getHakukohteet().get(0)
                .getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getTila(), HakemuksenTila.VARALLA);
    }


    @Test
    public void testProcess_Vastaanottanut_Sitovasti() {
       PreSijoitteluProcessorPeruutaAlemmatPeruneetJaHyvaksytyt pre
                = new PreSijoitteluProcessorPeruutaAlemmatPeruneetJaHyvaksytyt();
        SijoitteluajoWrapper sijoitteluajoWrapper = generateSijoitteluajoWrapper();

        HakemusWrapper hakemus = new HakemusWrapper();
        HakemusWrapper hakemus2 = new HakemusWrapper();

        Hakemus h = new Hakemus();
        h.setPrioriteetti(2);
        h.setTila(HakemuksenTila.HYVAKSYTTY);
        h.setHakemusOid("123");
        hakemus.setHakemus(h);
        Hakemus h2 = new Hakemus();
        h2.setPrioriteetti(1);
        h2.setTila(HakemuksenTila.HYVAKSYTTY);
        h2.setHakemusOid("321");
        hakemus2.setHakemus(h2);
        hakemus2.setTilaVoidaanVaihtaa(false);


        HenkiloWrapper henkilo = generateHenkiloWrapper();
        henkilo.getHakemukset().add(hakemus);
        henkilo.getHakemukset().add(hakemus2);
        hakemus.setHenkilo(henkilo);
        sijoitteluajoWrapper.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().add(hakemus);
        Valintatulos valintatulos = new Valintatulos();
        valintatulos.setHakemusOid("321");
        valintatulos.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI);
        henkilo.getValintatulos().add(valintatulos);
        hakemus2.setHenkilo(henkilo);
        sijoitteluajoWrapper.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().add(hakemus2);



        pre.process(sijoitteluajoWrapper);
        Assert.assertEquals(sijoitteluajoWrapper.getHakukohteet().get(0)
                .getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getTila(), HakemuksenTila.PERUUNTUNUT);
        Assert.assertEquals(sijoitteluajoWrapper.getHakukohteet().get(0)
                .getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getTila(), HakemuksenTila.HYVAKSYTTY);
    }
*/
    private List<ValintatapajonoWrapper> generateValintatapajonoWrapper() {
        List<ValintatapajonoWrapper> valintatapajonot = new ArrayList<>();

        ValintatapajonoWrapper valintatapajono = new ValintatapajonoWrapper();
        List<HakemusWrapper> hakemukset = generateHakemusWrapper();
        valintatapajono.setHakemukset(hakemukset);

        valintatapajonot.add(valintatapajono);
        return valintatapajonot;
    }

    private List<HakukohdeWrapper> generateHakukohdeWrapper() {
        List<HakukohdeWrapper> hakukohteet = new ArrayList<>();

        HakukohdeWrapper hakukohde1 = new HakukohdeWrapper();

        List<ValintatapajonoWrapper> valintatapajonoWrapper = generateValintatapajonoWrapper();
        hakukohde1.setValintatapajonot(valintatapajonoWrapper);

        hakukohteet.add(hakukohde1);

        return hakukohteet;
    }

    private List<HakemusWrapper> generateHakemusWrapper() {
        List<HakemusWrapper> hakemukset = new ArrayList<>();
        return hakemukset;
    }

    private SijoitteluajoWrapper generateSijoitteluajoWrapper() {
        SijoitteluajoWrapper sijoitteluajo = new SijoitteluajoWrapper();
        List<HakukohdeWrapper> hakukohdeWrapper = generateHakukohdeWrapper();

        sijoitteluajo.setHakukohteet(hakukohdeWrapper);
        return sijoitteluajo;
    }

    private HenkiloWrapper generateHenkiloWrapper() {
        return new HenkiloWrapper();
    }
}
