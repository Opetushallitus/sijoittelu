package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;


import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.*;
import fi.vm.sade.sijoittelu.domain.*;
import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;


public class PreSijoitteluProcessorPeruutaAlemmatPeruneetJaHyvaksytytTest {
    @Test
    public void testProcess_HakemuksenTila_Varalla() {
        PreSijoitteluProcessorPeruutaAlemmatPeruneetJaHyvaksytyt pre
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
        sijoitteluajoWrapper.getHakukohteet().get(1).getValintatapajonot().get(0).getHakemukset().add(hakemus2);

        henkilo.getHakemukset().add(hakemus);
        henkilo.getHakemukset().add(hakemus2);

        pre.process(sijoitteluajoWrapper);
        Assert.assertEquals(sijoitteluajoWrapper.getHakukohteet().get(0)
                .getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getTila(), HakemuksenTila.HYVAKSYTTY);
        Assert.assertEquals(sijoitteluajoWrapper.getHakukohteet().get(1)
                .getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getTila(), HakemuksenTila.PERUUNTUNUT);
    }

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

        henkilo.getHakemukset().add(hakemus);
        henkilo.getHakemukset().add(hakemus2);

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
        hakemus.setTilaVoidaanVaihtaa(true);

        Hakemus h2 = new Hakemus();
        h2.setPrioriteetti(1);
        h2.setTila(HakemuksenTila.HYVAKSYTTY);
        h2.setHakemusOid("321");
        hakemus2.setHakemus(h2);
        hakemus2.setTilaVoidaanVaihtaa(false);


        HenkiloWrapper henkilo = generateHenkiloWrapper();

        sijoitteluajoWrapper.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().add(hakemus);


        Valintatulos valintatulos = new Valintatulos();
        valintatulos.setHakemusOid("321");
        valintatulos.setHakukohdeOid("321");
        valintatulos.setValintatapajonoOid("123");
        valintatulos.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI);
        henkilo.getValintatulos().add(valintatulos);
        hakemus.setHenkilo(henkilo);
        hakemus2.setHenkilo(henkilo);


        sijoitteluajoWrapper.getHakukohteet().get(1).getValintatapajonot().get(0).getHakemukset().add(hakemus2);

        henkilo.getHakemukset().add(hakemus);
        henkilo.getHakemukset().add(hakemus2);

        pre.process(sijoitteluajoWrapper);

        Assert.assertEquals(sijoitteluajoWrapper.getHakukohteet().get(0)
                .getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getTila(), HakemuksenTila.PERUUNTUNUT);
        Assert.assertEquals(sijoitteluajoWrapper.getHakukohteet().get(1)
                .getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getTila(), HakemuksenTila.HYVAKSYTTY);
    }

    @Test
    public void testProcess_Vastaanottanut_Ehdollisesti() {
        PreSijoitteluProcessorPeruutaAlemmatPeruneetJaHyvaksytyt pre
                = new PreSijoitteluProcessorPeruutaAlemmatPeruneetJaHyvaksytyt();
        SijoitteluajoWrapper sijoitteluajoWrapper = generateSijoitteluajoWrapper();

        HakemusWrapper hakemus = new HakemusWrapper();
        HakemusWrapper hakemus2 = new HakemusWrapper();

        Hakemus h = new Hakemus();
        h.setPrioriteetti(1);
        h.setTila(HakemuksenTila.HYVAKSYTTY);
        h.setHakemusOid("123");

        hakemus.setTilaVoidaanVaihtaa(false);
        hakemus.setHakemus(h);
        Hakemus h2 = new Hakemus();
        h2.setPrioriteetti(2);
        h2.setTila(HakemuksenTila.HYVAKSYTTY);
        h2.setHakemusOid("321");
        hakemus2.setHakemus(h2);
        hakemus2.setTilaVoidaanVaihtaa(true);


        HenkiloWrapper henkilo = generateHenkiloWrapper();


        sijoitteluajoWrapper.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().add(hakemus);
        Valintatulos valintatulos = new Valintatulos();
        valintatulos.setHakemusOid("321");
        valintatulos.setHakukohdeOid("321");
        valintatulos.setValintatapajonoOid("123");
        valintatulos.setTila(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT);
        henkilo.getValintatulos().add(valintatulos);

        Valintatulos valintatulos2 = new Valintatulos();
        valintatulos2.setHakemusOid("123");
        valintatulos2.setHakukohdeOid("123");
        valintatulos2.setValintatapajonoOid("123");
        valintatulos2.setTila(ValintatuloksenTila.VASTAANOTTANUT);
        henkilo.getValintatulos().add(valintatulos2);

        hakemus.setHenkilo(henkilo);
        hakemus2.setHenkilo(henkilo);


        sijoitteluajoWrapper.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().add(hakemus2);

        henkilo.getHakemukset().add(hakemus);
        henkilo.getHakemukset().add(hakemus2);

        pre.process(sijoitteluajoWrapper);
        Assert.assertEquals(sijoitteluajoWrapper.getHakukohteet().get(0)
                .getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getTila(), HakemuksenTila.HYVAKSYTTY);
        Assert.assertEquals(sijoitteluajoWrapper.getHakukohteet().get(0)
                .getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getTila(), HakemuksenTila.PERUUNTUNUT);
    }

    private List<ValintatapajonoWrapper> generateValintatapajonoWrapper() {
        List<ValintatapajonoWrapper> valintatapajonot = new ArrayList<>();

        ValintatapajonoWrapper valintatapajono = new ValintatapajonoWrapper();
        Valintatapajono valintatapajono1 = new Valintatapajono();
        valintatapajono1.setOid("123");
        valintatapajono.setValintatapajono(valintatapajono1);

        List<HakemusWrapper> hakemukset = generateHakemusWrapper();
        valintatapajono.setHakemukset(hakemukset);

        valintatapajonot.add(valintatapajono);
/*
        ValintatapajonoWrapper valintatapajono2 = new ValintatapajonoWrapper();
        Valintatapajono valintat2 = new Valintatapajono();
        valintat2.setOid("321");
        valintatapajono2.setValintatapajono(valintat2);

        valintatapajono2.setHakemukset(hakemukset);

        valintatapajonot.add(valintatapajono2);
        */

        return valintatapajonot;
    }

    private List<HakukohdeWrapper> generateHakukohdeWrapper() {
        List<HakukohdeWrapper> hakukohteet = new ArrayList<>();

        HakukohdeWrapper hakukohde1 = new HakukohdeWrapper();
        Hakukohde hakukohde = new Hakukohde();
        hakukohde.setOid("123");

        hakukohde1.setHakukohde(hakukohde);
        List<ValintatapajonoWrapper> valintatapajonoWrapper = generateValintatapajonoWrapper();
        hakukohde1.setValintatapajonot(valintatapajonoWrapper);

        hakukohteet.add(hakukohde1);


        HakukohdeWrapper hakukohde2 = new HakukohdeWrapper();
        Hakukohde hakuk2 = new Hakukohde();
        hakuk2.setOid("321");

        hakukohde2.setHakukohde(hakuk2);
        hakukohde2.setValintatapajonot(valintatapajonoWrapper);

        hakukohteet.add(hakukohde2);

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