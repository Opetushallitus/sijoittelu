package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluConfiguration;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.*;
import fi.vm.sade.sijoittelu.domain.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class PreSijoitteluProcessorMerkitseMyohastyneetPaikanVastaanototTest {

    private final PreSijoitteluProcessorMerkitseMyohastyneetPaikanVastaanotot processor = new PreSijoitteluProcessorMerkitseMyohastyneetPaikanVastaanotot();

    @Test
    public void marksLateApplicationsAutomaticallyCancelled() {
        SijoitteluajoWrapper wrapper = generateSijoitteluajoWrapper(true, true);
        processor.process(wrapper);
        HakemusWrapper hw = wrapper.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
        assertEquals(HakemuksenTila.PERUNUT, hw.getHakemus().getTila());
        assertEquals(ValintatuloksenTila.EI_VASTAANOTETTU_MAARA_AIKANA, hw.getValintatulos().orElse(new Valintatulos()).getTila());
        assertFalse(hw.isTilaVoidaanVaihtaa());
        assertEquals(1, wrapper.getMuuttuneetValintatulokset().size());
    }

    @Test
    public void doesNotMarkLateApplicationsThatAreNotAcceptedCancelled() {
        SijoitteluajoWrapper wrapper = generateSijoitteluajoWrapper(true, true);
        wrapper.getHakukohteet().stream().flatMap(HakukohdeWrapper::hakukohteenHakemukset)
                .forEach(hakemus -> hakemus.getHakemus().setTila(HakemuksenTila.VARALLA));
        processor.process(wrapper);
        HakemusWrapper hw = wrapper.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
        assertEquals(HakemuksenTila.VARALLA, hw.getHakemus().getTila());
        assertEquals(ValintatuloksenTila.KESKEN, hw.getValintatulos().orElse(new Valintatulos()).getTila());
        assertTrue(hw.isTilaVoidaanVaihtaa());
        assertTrue(wrapper.getMuuttuneetValintatulokset().isEmpty());
    }

    @Test
    public void doesNotMarkLateApplicationsThatAreNotInProcessCancelled() {
        SijoitteluajoWrapper wrapper = generateSijoitteluajoWrapper(true, true);
        wrapper.getHakukohteet().stream().flatMap(HakukohdeWrapper::hakukohteenHakemukset)
                .forEach(hakemus -> hakemus.getValintatulos().orElse(new Valintatulos()).setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, ""));
        processor.process(wrapper);
        HakemusWrapper hw = wrapper.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
        assertEquals(HakemuksenTila.HYVAKSYTTY, hw.getHakemus().getTila());
        assertEquals(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, hw.getValintatulos().orElse(new Valintatulos()).getTila());
        assertTrue(hw.isTilaVoidaanVaihtaa());
        assertTrue(wrapper.getMuuttuneetValintatulokset().isEmpty());
    }

    @Test
    public void marksLateApplicationsAutomaticallyCancelledAndLeavesNotMarkedLateAsTheyAre() {
        SijoitteluajoWrapper wrapper = generateSijoitteluajoWrapper(true, false, true, false);
        processor.process(wrapper);
        List<HakemusWrapper> hw = wrapper.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset();
        assertEquals(HakemuksenTila.HYVAKSYTTY, hw.get(0).getHakemus().getTila());
        assertEquals(ValintatuloksenTila.KESKEN, hw.get(0).getValintatulos().orElse(new Valintatulos()).getTila());
        assertTrue(hw.get(0).isTilaVoidaanVaihtaa());
        assertEquals(HakemuksenTila.PERUNUT, hw.get(1).getHakemus().getTila());
        assertEquals(ValintatuloksenTila.EI_VASTAANOTETTU_MAARA_AIKANA, hw.get(1).getValintatulos().orElse(new Valintatulos()).getTila());
        assertFalse(hw.get(1).isTilaVoidaanVaihtaa());
        assertEquals(HakemuksenTila.HYVAKSYTTY, hw.get(2).getHakemus().getTila());
        assertEquals(ValintatuloksenTila.KESKEN, hw.get(2).getValintatulos().orElse(new Valintatulos()).getTila());
        assertTrue(hw.get(2).isTilaVoidaanVaihtaa());
        assertEquals(1, wrapper.getMuuttuneetValintatulokset().size());
    }

     @Test
     public void doesNotMarkApplicationCancelledIfMerkitseMyohAutoFlagIsNotSet() {
         SijoitteluajoWrapper wrapper = generateSijoitteluajoWrapper(false, true);
         processor.process(wrapper);
         HakemusWrapper hw = wrapper.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
         assertEquals(HakemuksenTila.HYVAKSYTTY, hw.getHakemus().getTila());
         assertEquals(ValintatuloksenTila.KESKEN, hw.getValintatulos().orElse(new Valintatulos()).getTila());
         assertTrue(hw.isTilaVoidaanVaihtaa());
         assertTrue(wrapper.getMuuttuneetValintatulokset().isEmpty());
     }

     @Test
     public void doesNotMarkApplicationCancelledIfApplicationIsNotLate() {
         SijoitteluajoWrapper wrapper = generateSijoitteluajoWrapper(true, false);
         processor.process(wrapper);
         HakemusWrapper hw = wrapper.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
         assertEquals(HakemuksenTila.HYVAKSYTTY, hw.getHakemus().getTila());
         assertEquals(ValintatuloksenTila.KESKEN, hw.getValintatulos().orElse(new Valintatulos()).getTila());
         assertTrue(hw.isTilaVoidaanVaihtaa());
         assertTrue(wrapper.getMuuttuneetValintatulokset().isEmpty());
     }

    private List<ValintatapajonoWrapper> generateValintatapajonoWrapper(boolean merkitseMyohAuto, boolean ... hakemusMyohassa) {
        List<ValintatapajonoWrapper> valintatapajonot = new ArrayList<>();

        ValintatapajonoWrapper valintatapajono = new ValintatapajonoWrapper();
        Valintatapajono valintatapajono1 = new Valintatapajono();
        valintatapajono1.setOid("123");
        valintatapajono1.setPrioriteetti(0);
        valintatapajono.setMerkitseMyohAuto(merkitseMyohAuto);
        valintatapajono.setValintatapajono(valintatapajono1);

        List<HakemusWrapper> hakemukset = generateHakemusWrapper(valintatapajono, hakemusMyohassa);
        valintatapajono.setHakemukset(hakemukset);

        valintatapajonot.add(valintatapajono);

        return valintatapajonot;
    }

    private List<HakemusWrapper> generateHakemusWrapper(ValintatapajonoWrapper jono, boolean ... hakemusMyohassa) {
        List<HakemusWrapper> hakemukset = new ArrayList<>();
        for (int i = 0; i < hakemusMyohassa.length; i++) {
            HakemusWrapper wrapper = new HakemusWrapper();
            Hakemus hakemus = new Hakemus();
            hakemus.setHakemusOid("1.2.3." + i);
            hakemus.setTila(HakemuksenTila.HYVAKSYTTY);
            hakemus.setVastaanottoMyohassa(hakemusMyohassa[i]);
            wrapper.setHakemus(hakemus);
            wrapper.setHenkilo(generateHenkiloWrapper("1.2.3." + i, jono.getValintatapajono().getOid()));
            wrapper.setValintatapajono(jono);
            hakemukset.add(wrapper);
        }
        return hakemukset;
    }

    private List<HakukohdeWrapper> generateHakukohdeWrapper(boolean merkitseMyohAuto, boolean ... hakemusMyohassa) {
        List<HakukohdeWrapper> hakukohteet = new ArrayList<>();

        HakukohdeWrapper hakukohde1 = new HakukohdeWrapper();
        Hakukohde hakukohde = new Hakukohde();
        hakukohde.setOid("123");
        hakukohde1.setHakukohde(hakukohde);
        hakukohde1.setValintatapajonot(generateValintatapajonoWrapper(merkitseMyohAuto, hakemusMyohassa));
        hakukohteet.add(hakukohde1);

        return hakukohteet;
    }

    private SijoitteluajoWrapper generateSijoitteluajoWrapper(boolean merkitseMyohAuto, boolean ... hakemusMyohassa) {
        SijoitteluajoWrapper sijoitteluajo = new SijoitteluajoWrapper(new SijoitteluConfiguration(), new SijoitteluAjo());
        List<HakukohdeWrapper> hakukohdeWrapper = generateHakukohdeWrapper(merkitseMyohAuto, hakemusMyohassa);
        sijoitteluajo.setHakukohteet(hakukohdeWrapper);
        return sijoitteluajo;
    }

    private HenkiloWrapper generateHenkiloWrapper(String hakemusOid, String valintatapaJonoOid) {
        HenkiloWrapper wrapper = new HenkiloWrapper();
        wrapper.setHakemusOid(hakemusOid);
        Valintatulos vt = new Valintatulos();
        vt.setTila(ValintatuloksenTila.KESKEN, "");
        vt.setHakemusOid(hakemusOid, "");
        vt.setValintatapajonoOid(valintatapaJonoOid, "");
        wrapper.setValintatulos(List.of(vt));
        return wrapper;
    }
}
