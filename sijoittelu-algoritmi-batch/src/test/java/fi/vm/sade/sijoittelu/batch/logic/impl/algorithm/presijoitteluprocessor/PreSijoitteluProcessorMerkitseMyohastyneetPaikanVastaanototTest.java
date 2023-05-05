package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluConfiguration;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.*;
import fi.vm.sade.sijoittelu.domain.*;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class PreSijoitteluProcessorMerkitseMyohastyneetPaikanVastaanototTest {

    private final PreSijoitteluProcessorMerkitseMyohastyneetPaikanVastaanotot processor = new PreSijoitteluProcessorMerkitseMyohastyneetPaikanVastaanotot();

    @Test
    public void marksLateApplicationsAutomatically() {
        SijoitteluajoWrapper wrapper = generateSijoitteluajoWrapper(true, LocalDateTime.now().minusDays(2));
        processor.process(wrapper);
        HakemusWrapper hw = wrapper.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
        assertEquals(HakemuksenTila.PERUNUT, hw.getHakemus().getTila());
        assertEquals(ValintatuloksenTila.EI_VASTAANOTETTU_MAARA_AIKANA, hw.getValintatulos().orElse(new Valintatulos()).getTila());
        assertFalse(hw.isTilaVoidaanVaihtaa());
    }

     @Test
     public void doesNotMarkApplicationLateIfMerkitseMyohAutoFlagIsNotSet() {
         SijoitteluajoWrapper wrapper = generateSijoitteluajoWrapper(false, LocalDateTime.now().minusDays(2));
         processor.process(wrapper);
         HakemusWrapper hw = wrapper.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
         assertEquals(HakemuksenTila.VARALLA, hw.getHakemus().getTila());
         assertEquals(ValintatuloksenTila.KESKEN, hw.getValintatulos().orElse(new Valintatulos()).getTila());
         assertTrue(hw.isTilaVoidaanVaihtaa());
     }

     @Test
     public void doesNotMarkApplicationLateIfReserveFillIsNotOver() {
         SijoitteluajoWrapper wrapper = generateSijoitteluajoWrapper(false, LocalDateTime.now().plusDays(2));
         processor.process(wrapper);
         HakemusWrapper hw = wrapper.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().get(0);
         assertEquals(HakemuksenTila.VARALLA, hw.getHakemus().getTila());
         assertEquals(ValintatuloksenTila.KESKEN, hw.getValintatulos().orElse(new Valintatulos()).getTila());
         assertTrue(hw.isTilaVoidaanVaihtaa());
     }

    private List<ValintatapajonoWrapper> generateValintatapajonoWrapper(boolean merkitseMyohAuto) {
        List<ValintatapajonoWrapper> valintatapajonot = new ArrayList<>();

        ValintatapajonoWrapper valintatapajono = new ValintatapajonoWrapper();
        Valintatapajono valintatapajono1 = new Valintatapajono();
        valintatapajono1.setOid("123");
        valintatapajono1.setPrioriteetti(0);
        valintatapajono.setMerkitseMyohAuto(merkitseMyohAuto);
        valintatapajono.setValintatapajono(valintatapajono1);

        List<HakemusWrapper> hakemukset = generateHakemusWrapper(valintatapajono);
        valintatapajono.setHakemukset(hakemukset);

        valintatapajonot.add(valintatapajono);

        return valintatapajonot;
    }

    private List<HakemusWrapper> generateHakemusWrapper(ValintatapajonoWrapper jono) {
        List<HakemusWrapper> hakemukset = new ArrayList<>();
        HakemusWrapper wrapper = new HakemusWrapper();
        Hakemus hakemus = new Hakemus();
        hakemus.setHakemusOid("1.2.3");
        hakemus.setTila(HakemuksenTila.VARALLA);
        wrapper.setHakemus(hakemus);
        wrapper.setHenkilo(generateHenkiloWrapper("1.2.3", jono.getValintatapajono().getOid()));
        wrapper.setValintatapajono(jono);
        hakemukset.add(wrapper);
        return hakemukset;
    }

    private List<HakukohdeWrapper> generateHakukohdeWrapper(boolean merkitseMyohAuto) {
        List<HakukohdeWrapper> hakukohteet = new ArrayList<>();

        HakukohdeWrapper hakukohde1 = new HakukohdeWrapper();
        Hakukohde hakukohde = new Hakukohde();
        hakukohde.setOid("123");
        hakukohde1.setHakukohde(hakukohde);
        hakukohde1.setValintatapajonot(generateValintatapajonoWrapper(merkitseMyohAuto));
        hakukohteet.add(hakukohde1);

        return hakukohteet;
    }

    private SijoitteluajoWrapper generateSijoitteluajoWrapper(boolean merkitseMyohAuto, LocalDateTime dateToUseForChecking) {
        SijoitteluajoWrapper sijoitteluajo = new SijoitteluajoWrapper(new SijoitteluConfiguration(), new SijoitteluAjo());
        sijoitteluajo.setVarasijaTayttoPaattyy(dateToUseForChecking);
        List<HakukohdeWrapper> hakukohdeWrapper = generateHakukohdeWrapper(merkitseMyohAuto);
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
