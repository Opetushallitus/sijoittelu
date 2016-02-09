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
        hakemus.setValintatapajono(sijoitteluajoWrapper.getHakukohteet().get(0).getValintatapajonot().get(0));

        Hakemus h2 = new Hakemus();
        h2.setPrioriteetti(2);
        h2.setTila(HakemuksenTila.VARALLA);
        hakemus2.setHakemus(h2);
        hakemus2.setValintatapajono(sijoitteluajoWrapper.getHakukohteet().get(1).getValintatapajonot().get(0));



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
                .getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getTila(), HakemuksenTila.PERUUNTUNUT);
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
        hakemus.setValintatapajono(sijoitteluajoWrapper.getHakukohteet().get(0).getValintatapajonot().get(0));

        Hakemus h2 = new Hakemus();
        h2.setPrioriteetti(1);
        h2.setTila(HakemuksenTila.VARALLA);
        hakemus2.setHakemus(h2);
        hakemus2.setValintatapajono(sijoitteluajoWrapper.getHakukohteet().get(1).getValintatapajonot().get(0));



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
        hakemus.setValintatapajono(sijoitteluajoWrapper.getHakukohteet().get(1).getValintatapajonot().get(0));

        Hakemus h2 = new Hakemus();
        h2.setPrioriteetti(1);
        h2.setTila(HakemuksenTila.HYVAKSYTTY);
        h2.setHakemusOid("321");
        hakemus2.setHakemus(h2);
        hakemus2.setTilaVoidaanVaihtaa(false);
        hakemus2.setValintatapajono(sijoitteluajoWrapper.getHakukohteet().get(0).getValintatapajonot().get(0));


        HenkiloWrapper henkilo = generateHenkiloWrapper();

        sijoitteluajoWrapper.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().add(hakemus);


        Valintatulos valintatulos = new Valintatulos();
        valintatulos.setHakemusOid("321", "");
        valintatulos.setHakukohdeOid("321", "");
        valintatulos.setValintatapajonoOid("123", "");
        valintatulos.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, "");
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
                .getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getTila(), HakemuksenTila.HYVAKSYTTY);

        sijoitteluajoWrapper = generateSijoitteluajoWrapper();

        hakemus = new HakemusWrapper();
        hakemus2 = new HakemusWrapper();

        h = new Hakemus();
        h.setPrioriteetti(1);
        h.setTila(HakemuksenTila.HYVAKSYTTY);
        h.setHakemusOid("123");
        hakemus.setHakemus(h);
        hakemus.setTilaVoidaanVaihtaa(false);
        hakemus.setValintatapajono(sijoitteluajoWrapper.getHakukohteet().get(0).getValintatapajonot().get(0));

        h2 = new Hakemus();
        h2.setPrioriteetti(2);
        h2.setTila(HakemuksenTila.HYVAKSYTTY);
        h2.setHakemusOid("321");
        hakemus2.setHakemus(h2);
        hakemus2.setTilaVoidaanVaihtaa(true);
        hakemus2.setValintatapajono(sijoitteluajoWrapper.getHakukohteet().get(1).getValintatapajonot().get(0));


        henkilo = generateHenkiloWrapper();

        sijoitteluajoWrapper.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().add(hakemus);


        valintatulos = new Valintatulos();
        valintatulos.setHakemusOid("123", "");
        valintatulos.setHakukohdeOid("123", "");
        valintatulos.setValintatapajonoOid("123", "");
        valintatulos.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, "");
        henkilo.getValintatulos().add(valintatulos);
        hakemus.setHenkilo(henkilo);
        hakemus2.setHenkilo(henkilo);


        sijoitteluajoWrapper.getHakukohteet().get(1).getValintatapajonot().get(0).getHakemukset().add(hakemus2);

        henkilo.getHakemukset().add(hakemus);
        henkilo.getHakemukset().add(hakemus2);

        pre.process(sijoitteluajoWrapper);

        Assert.assertEquals(sijoitteluajoWrapper.getHakukohteet().get(0)
                .getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getTila(), HakemuksenTila.HYVAKSYTTY);
        Assert.assertEquals(sijoitteluajoWrapper.getHakukohteet().get(1)
                .getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getTila(), HakemuksenTila.PERUUNTUNUT);
    }

    @Test
    public void testToisenaPrioriteettinaOlevanHakutoiveenEhdollinenVastaanottoPeruuAlemmanHakutoiveen() {
            PreSijoitteluProcessorPeruutaAlemmatPeruneetJaHyvaksytyt pre
                    = new PreSijoitteluProcessorPeruutaAlemmatPeruneetJaHyvaksytyt();
            SijoitteluajoWrapper sijoitteluajoWrapper = generateSijoitteluajoWrapper();

            HakemusWrapper hakemus = new HakemusWrapper();
            HakemusWrapper hakemus2 = new HakemusWrapper();
            HakemusWrapper hakemus3 = new HakemusWrapper();

            Hakemus h = new Hakemus();
            h.setPrioriteetti(1);
            h.setTila(HakemuksenTila.VARALLA);
            h.setHakemusOid("123");
            hakemus.setTilaVoidaanVaihtaa(false);
            hakemus.setHakemus(h);
            hakemus.setValintatapajono(sijoitteluajoWrapper.getHakukohteet().get(0).getValintatapajonot().get(0));


            Hakemus h2 = new Hakemus();
            h2.setPrioriteetti(2);
            h2.setTila(HakemuksenTila.HYVAKSYTTY);
            h2.setHakemusOid("321");
            hakemus2.setHakemus(h2);
            hakemus2.setTilaVoidaanVaihtaa(true);
            hakemus2.setValintatapajono(sijoitteluajoWrapper.getHakukohteet().get(1).getValintatapajonot().get(0));

            Hakemus h3 = new Hakemus();
            h3.setPrioriteetti(3);
            h3.setTila(HakemuksenTila.VARALLA);
            h3.setHakemusOid("654");
            hakemus3.setHakemus(h3);
            hakemus3.setTilaVoidaanVaihtaa(true);
            hakemus3.setValintatapajono(sijoitteluajoWrapper.getHakukohteet().get(2).getValintatapajonot().get(0));

            HenkiloWrapper henkilo = generateHenkiloWrapper();


            Valintatulos valintatulos1 = new Valintatulos();
            valintatulos1.setHakemusOid("123", "");
            valintatulos1.setHakukohdeOid("123", "");
            valintatulos1.setValintatapajonoOid("123", "");
            valintatulos1.setTila(ValintatuloksenTila.KESKEN, "");
            henkilo.getValintatulos().add(valintatulos1);

            Valintatulos valintatulos2 = new Valintatulos();
            valintatulos2.setHakemusOid("321", "");
            valintatulos2.setHakukohdeOid("321", "");
            valintatulos2.setValintatapajonoOid("321", "");
            valintatulos2.setTila(ValintatuloksenTila.KESKEN, "");
            henkilo.getValintatulos().add(valintatulos2);

            Valintatulos valintatulos3 = new Valintatulos();
            valintatulos3.setHakemusOid("654", "");
            valintatulos3.setHakukohdeOid("654", "");
            valintatulos3.setValintatapajonoOid("654", "");
            valintatulos3.setTila(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT, "");
            henkilo.getValintatulos().add(valintatulos3);

            hakemus.setHenkilo(henkilo);
            hakemus2.setHenkilo(henkilo);
            hakemus3.setHenkilo(henkilo);
            henkilo.getHakemukset().add(hakemus);
            henkilo.getHakemukset().add(hakemus2);
            henkilo.getHakemukset().add(hakemus3);

            sijoitteluajoWrapper.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().add(hakemus);
            sijoitteluajoWrapper.getHakukohteet().get(1).getValintatapajonot().get(0).getHakemukset().add(hakemus2);
            sijoitteluajoWrapper.getHakukohteet().get(2).getValintatapajonot().get(0).getHakemukset().add(hakemus3);

            pre.process(sijoitteluajoWrapper);
            Hakemus vastaanottamatonHakemus1 = sijoitteluajoWrapper.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0).getHakemus();
            Hakemus vastaanottamatonHakemus2 = sijoitteluajoWrapper.getHakukohteet().get(1).getValintatapajonot().get(0).getHakemukset().get(0).getHakemus();
            Hakemus ehdollisestiVastaanotettuHakemus = sijoitteluajoWrapper.getHakukohteet().get(2).getValintatapajonot().get(0).getHakemukset().get(0).getHakemus();
            Assert.assertEquals("123", vastaanottamatonHakemus1.getHakemusOid());
            Assert.assertEquals("321", vastaanottamatonHakemus2.getHakemusOid());
            Assert.assertEquals("654", ehdollisestiVastaanotettuHakemus.getHakemusOid());
            Assert.assertEquals(HakemuksenTila.VARALLA, vastaanottamatonHakemus1.getTila());
            Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, vastaanottamatonHakemus2.getTila());
            Assert.assertEquals(HakemuksenTila.PERUUNTUNUT, ehdollisestiVastaanotettuHakemus.getTila());
        }

    @Test
    public void testAlimmanPrioriteetinHakutoiveenEhdollinenVastaanottoEiPeruMuitaHakutoiveita() {
        PreSijoitteluProcessorPeruutaAlemmatPeruneetJaHyvaksytyt pre
                = new PreSijoitteluProcessorPeruutaAlemmatPeruneetJaHyvaksytyt();
        SijoitteluajoWrapper sijoitteluajoWrapper = generateSijoitteluajoWrapper();

        HakemusWrapper hakemus = new HakemusWrapper();
        HakemusWrapper hakemus2 = new HakemusWrapper();
        HakemusWrapper hakemus3 = new HakemusWrapper();

        Hakemus h = new Hakemus();
        h.setPrioriteetti(1);
        h.setTila(HakemuksenTila.VARALLA);
        h.setHakemusOid("123");
        hakemus.setTilaVoidaanVaihtaa(false);
        hakemus.setHakemus(h);
        hakemus.setValintatapajono(sijoitteluajoWrapper.getHakukohteet().get(0).getValintatapajonot().get(0));


        Hakemus h2 = new Hakemus();
        h2.setPrioriteetti(2);
        h2.setTila(HakemuksenTila.VARALLA);
        h2.setHakemusOid("321");
        hakemus2.setHakemus(h2);
        hakemus2.setTilaVoidaanVaihtaa(true);
        hakemus2.setValintatapajono(sijoitteluajoWrapper.getHakukohteet().get(1).getValintatapajonot().get(0));

        Hakemus h3 = new Hakemus();
        h3.setPrioriteetti(3);
        h3.setTila(HakemuksenTila.HYVAKSYTTY);
        h3.setHakemusOid("654");
        hakemus3.setHakemus(h3);
        hakemus3.setTilaVoidaanVaihtaa(true);
        hakemus3.setValintatapajono(sijoitteluajoWrapper.getHakukohteet().get(2).getValintatapajonot().get(0));

        HenkiloWrapper henkilo = generateHenkiloWrapper();


        Valintatulos valintatulos1 = new Valintatulos();
        valintatulos1.setHakemusOid("123", "");
        valintatulos1.setHakukohdeOid("123", "");
        valintatulos1.setValintatapajonoOid("123", "");
        valintatulos1.setTila(ValintatuloksenTila.KESKEN, "");
        henkilo.getValintatulos().add(valintatulos1);

        Valintatulos valintatulos2 = new Valintatulos();
        valintatulos2.setHakemusOid("321", "");
        valintatulos2.setHakukohdeOid("321", "");
        valintatulos2.setValintatapajonoOid("321", "");
        valintatulos2.setTila(ValintatuloksenTila.KESKEN, "");
        henkilo.getValintatulos().add(valintatulos2);

        Valintatulos valintatulos3 = new Valintatulos();
        valintatulos3.setHakemusOid("654", "");
        valintatulos3.setHakukohdeOid("654", "");
        valintatulos3.setValintatapajonoOid("654", "");
        valintatulos3.setTila(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT, "");
        henkilo.getValintatulos().add(valintatulos3);

        hakemus.setHenkilo(henkilo);
        hakemus2.setHenkilo(henkilo);
        hakemus3.setHenkilo(henkilo);
        henkilo.getHakemukset().add(hakemus);
        henkilo.getHakemukset().add(hakemus2);
        henkilo.getHakemukset().add(hakemus3);

        sijoitteluajoWrapper.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().add(hakemus);
        sijoitteluajoWrapper.getHakukohteet().get(1).getValintatapajonot().get(0).getHakemukset().add(hakemus2);
        sijoitteluajoWrapper.getHakukohteet().get(2).getValintatapajonot().get(0).getHakemukset().add(hakemus3);

        pre.process(sijoitteluajoWrapper);
        Hakemus vastaanottamatonHakemus1 = sijoitteluajoWrapper.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0).getHakemus();
        Hakemus vastaanottamatonHakemus2 = sijoitteluajoWrapper.getHakukohteet().get(1).getValintatapajonot().get(0).getHakemukset().get(0).getHakemus();
        Hakemus ehdollisestiVastaanotettuHakemus = sijoitteluajoWrapper.getHakukohteet().get(2).getValintatapajonot().get(0).getHakemukset().get(0).getHakemus();
        Assert.assertEquals("123", vastaanottamatonHakemus1.getHakemusOid());
        Assert.assertEquals("321", vastaanottamatonHakemus2.getHakemusOid());
        Assert.assertEquals("654", ehdollisestiVastaanotettuHakemus.getHakemusOid());
        Assert.assertEquals(HakemuksenTila.VARALLA, vastaanottamatonHakemus1.getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, vastaanottamatonHakemus2.getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, ehdollisestiVastaanotettuHakemus.getTila());
    }

    private List<ValintatapajonoWrapper> generateValintatapajonoWrapper(String oid) {
        List<ValintatapajonoWrapper> valintatapajonot = new ArrayList<>();

        ValintatapajonoWrapper valintatapajono = new ValintatapajonoWrapper();
        Valintatapajono valintatapajono1 = new Valintatapajono();
        valintatapajono1.setOid(oid);
        valintatapajono1.setPrioriteetti(0);
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
//        List<ValintatapajonoWrapper> valintatapajonoWrapper = generateValintatapajonoWrapper();
        hakukohde1.setValintatapajonot(generateValintatapajonoWrapper("123"));

        hakukohteet.add(hakukohde1);


        HakukohdeWrapper hakukohde2 = new HakukohdeWrapper();
        Hakukohde hakuk2 = new Hakukohde();
        hakuk2.setOid("321");

        hakukohde2.setHakukohde(hakuk2);
        hakukohde2.setValintatapajonot(generateValintatapajonoWrapper("321"));

        hakukohteet.add(hakukohde2);


        HakukohdeWrapper hakukohde3 = new HakukohdeWrapper();
        Hakukohde hakuk3 = new Hakukohde();
        hakuk3.setOid("654");

        hakukohde3.setHakukohde(hakuk3);
        hakukohde3.setValintatapajonot(generateValintatapajonoWrapper("654"));

        hakukohteet.add(hakukohde3);


        return hakukohteet;
    }

    private List<HakemusWrapper> generateHakemusWrapper() {
        List<HakemusWrapper> hakemukset = new ArrayList<>();

        return hakemukset;
    }

    private SijoitteluajoWrapper generateSijoitteluajoWrapper() {
        SijoitteluajoWrapper sijoitteluajo = new SijoitteluajoWrapper(new SijoitteluAjo());
        List<HakukohdeWrapper> hakukohdeWrapper = generateHakukohdeWrapper();

        sijoitteluajo.setHakukohteet(hakukohdeWrapper);
        return sijoitteluajo;
    }

    private HenkiloWrapper generateHenkiloWrapper() {
        return new HenkiloWrapper();
    }
}
