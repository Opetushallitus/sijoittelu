package fi.vm.sade.sijoittelu;

import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.core.LoadStrategyEnum;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;

import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.PrintHelper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluajoWrapperFactory;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithm;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoittelunTila;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintalaskenta.tulos.service.impl.ValintatietoService;
import junit.framework.Assert;
import org.eclipse.core.internal.dtree.TestHelper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;

@ContextConfiguration(locations = "classpath:test-sijoittelu-batch-mongo.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@UsingDataSet
public class VastaanottoTest {

    @Autowired
    private ValintatietoService valintatietoService;

    @Autowired
    private ApplicationContext applicationContext;

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("test");

	@Test
    @UsingDataSet(locations = "vastaanotot.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void testVastaanotot() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());

        Valintatulos sitova = new Valintatulos();
        sitova.setHakemusOid("oid1", "");
        sitova.setHakijaOid("oid", "");
        sitova.setHakukohdeOid("hakukohde2", "");
        sitova.setHakuOid("1.2.246.562.29.173465377510", "");
        sitova.setHakutoive(2, "");
        sitova.setHyvaksyttyVarasijalta(false, "");
        sitova.setJulkaistavissa(true, "");
        sitova.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY, "");
        sitova.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, "");
        sitova.setValintatapajonoOid("jono2", "");

        Valintatulos ehdollinen = new Valintatulos();
        ehdollinen.setHakemusOid("oid2", "");
        ehdollinen.setHakijaOid("oid", "");
        ehdollinen.setHakukohdeOid("hakukohde2", "");
        ehdollinen.setHakuOid("1.2.246.562.29.173465377510", "");
        ehdollinen.setHakutoive(2, "");
        ehdollinen.setHyvaksyttyVarasijalta(false, "");
        ehdollinen.setJulkaistavissa(true, "");
        ehdollinen.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY, "");
        ehdollinen.setTila(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT, "");
        ehdollinen.setValintatapajonoOid("jono2", "");

        Valintatulos perunut = new Valintatulos();
        perunut.setHakemusOid("oid3", "");
        perunut.setHakijaOid("oid", "");
        perunut.setHakukohdeOid("hakukohde1", "");
        perunut.setHakuOid("1.2.246.562.29.173465377510", "");
        perunut.setHakutoive(1, "");
        perunut.setHyvaksyttyVarasijalta(false, "");
        perunut.setJulkaistavissa(true, "");
        perunut.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY, "");
        perunut.setTila(ValintatuloksenTila.PERUNUT, "");
        perunut.setValintatapajonoOid("jono1", "");

        Valintatulos ehdollinenPidettava = new Valintatulos();
        ehdollinenPidettava.setHakemusOid("oid4", "");
        ehdollinenPidettava.setHakijaOid("oid4", "");
        ehdollinenPidettava.setHakukohdeOid("hakukohde1", "");
        ehdollinenPidettava.setHakuOid("1.2.246.562.29.173465377510", "");
        ehdollinenPidettava.setHakutoive(1, "");
        ehdollinenPidettava.setHyvaksyttyVarasijalta(false, "");
        ehdollinenPidettava.setJulkaistavissa(true, "");
        ehdollinenPidettava.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY, "");
        ehdollinenPidettava.setTila(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT, "");
        ehdollinenPidettava.setValintatapajonoOid("jono1", "");

        Valintatulos ehdollinenPoistettava = new Valintatulos();
        ehdollinenPoistettava.setHakemusOid("oid4", "");
        ehdollinenPoistettava.setHakijaOid("oid4", "");
        ehdollinenPoistettava.setHakukohdeOid("hakukohde2", "");
        ehdollinenPoistettava.setHakuOid("1.2.246.562.29.173465377510", "");
        ehdollinenPoistettava.setHakutoive(2, "");
        ehdollinenPoistettava.setHyvaksyttyVarasijalta(false, "");
        ehdollinenPoistettava.setJulkaistavissa(true, "");
        ehdollinenPoistettava.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY, "");
        ehdollinenPoistettava.setTila(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT, "");
        ehdollinenPoistettava.setValintatapajonoOid("jono2", "");
        final SijoitteluajoWrapper sijoitteluAjo = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluAjo(), hakukohteet, Arrays.asList(sitova, ehdollinen, perunut, ehdollinenPidettava, ehdollinenPoistettava));

        SijoittelunTila s = SijoitteluAlgorithm.sijoittele(sijoitteluAjo);

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        Assert.assertEquals(4, sijoitteluAjo.getMuuttuneetValintatulokset().size());
        Valintatulos t = sijoitteluAjo.getMuuttuneetValintatulokset().get(0);
        Assert.assertEquals(t.getHakemusOid(), "oid4");
        Assert.assertEquals(t.getValintatapajonoOid(), "jono2");
        Assert.assertEquals(t.getHakukohdeOid(), "hakukohde2");
        Assert.assertEquals(t.getTila(), ValintatuloksenTila.KESKEN);

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
           if(hak.getHakemusOid().equals("oid1")) {
               Assert.assertEquals(hak.getTila(), HakemuksenTila.PERUUNTUNUT);
           } else if(hak.getHakemusOid().equals("oid2")) {
               Assert.assertEquals(hak.getTila(), HakemuksenTila.HYVAKSYTTY);
           } else if(hak.getHakemusOid().equals("oid3")) {
               Assert.assertEquals(hak.getTila(), HakemuksenTila.PERUNUT);
           } else {
               Assert.assertEquals(hak.getTila(), HakemuksenTila.HYVAKSYTTY);
           }
        });

        hakukohteet.get(1).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("oid1")) {
                Assert.assertEquals(hak.getTila(), HakemuksenTila.HYVAKSYTTY);
            } else if(hak.getHakemusOid().equals("oid2")) {
                Assert.assertEquals(hak.getTila(), HakemuksenTila.PERUUNTUNUT);
            } else if(hak.getHakemusOid().equals("oid3")) {
                Assert.assertEquals(hak.getTila(), HakemuksenTila.PERUUNTUNUT);
            } else {
                Assert.assertEquals(hak.getTila(), HakemuksenTila.PERUUNTUNUT);
            }
        });

        hakukohteet.get(2).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("oid1")) {
                Assert.assertEquals(hak.getTila(), HakemuksenTila.PERUUNTUNUT);
            } else if(hak.getHakemusOid().equals("oid2")) {
                Assert.assertEquals(hak.getTila(), HakemuksenTila.PERUUNTUNUT);
            } else if(hak.getHakemusOid().equals("oid3")) {
                Assert.assertEquals(hak.getTila(), HakemuksenTila.PERUUNTUNUT);
            } else {
                Assert.assertEquals(hak.getTila(), HakemuksenTila.PERUUNTUNUT);
            }
        });


	}

    @Test
    @UsingDataSet(locations = "vastaanotot_perunut_kaikille_jonoille.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testPeruutettuVastaanottoKaikilleJonoille() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");
        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());

        Valintatulos jono2Tulos = new Valintatulos();
        jono2Tulos.setHakemusOid("1.2.246.562.11.00001090792", "");
        jono2Tulos.setHakijaOid("1.2.246.562.24.45661259022", "");
        jono2Tulos.setHakukohdeOid("1.2.246.562.20.18895322503", "");
        jono2Tulos.setHakuOid("1.2.246.562.29.173465377510", "");
        jono2Tulos.setHakutoive(4, "");
        jono2Tulos.setHyvaksyttyVarasijalta(false, "");
        jono2Tulos.setJulkaistavissa(true, "");
        jono2Tulos.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY, "");
        jono2Tulos.setTila(ValintatuloksenTila.KESKEN, "");
        jono2Tulos.setValintatapajonoOid("jono2", "");

        Valintatulos jono1Tulos = new Valintatulos();
        jono1Tulos.setHakemusOid("1.2.246.562.11.00001090792", "");
        jono1Tulos.setHakijaOid("1.2.246.562.24.45661259022", "");
        jono1Tulos.setHakukohdeOid("1.2.246.562.20.18895322503", "");
        jono1Tulos.setHakuOid("1.2.246.562.29.173465377510", "");
        jono1Tulos.setHakutoive(4, "");
        jono1Tulos.setHyvaksyttyVarasijalta(false, "");
        jono1Tulos.setJulkaistavissa(true, "");
        jono1Tulos.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY, "");
        jono1Tulos.setTila(ValintatuloksenTila.PERUUTETTU, "");
        jono1Tulos.setValintatapajonoOid("jono1", "");

        final SijoitteluajoWrapper sijoitteluAjo = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluAjo(), hakukohteet, Arrays.asList(jono1Tulos, jono2Tulos));

        SijoittelunTila s = SijoitteluAlgorithm.sijoittele(sijoitteluAjo);

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        assertoi(hakukohteet.get(0).getValintatapajonot()
                .get(0), "1.2.246.562.11.00001090792", HakemuksenTila.PERUUTETTU);
        assertoi(hakukohteet.get(0).getValintatapajonot()
                .get(1), "1.2.246.562.11.00001090792", HakemuksenTila.PERUUTETTU);

    }

    @Test
    @UsingDataSet(locations = "vastaanotot_perunut_kaikille_jonoille.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testEiVastaanottanutMaaraaikanaVastaanottoKaikilleJonoille() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");
        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());

        Valintatulos jono2Tulos = new Valintatulos();
        jono2Tulos.setHakemusOid("1.2.246.562.11.00001090792", "");
        jono2Tulos.setHakijaOid("1.2.246.562.24.45661259022", "");
        jono2Tulos.setHakukohdeOid("1.2.246.562.20.18895322503", "");
        jono2Tulos.setHakuOid("1.2.246.562.29.173465377510", "");
        jono2Tulos.setHakutoive(4, "");
        jono2Tulos.setHyvaksyttyVarasijalta(false, "");
        jono2Tulos.setJulkaistavissa(true, "");
        jono2Tulos.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY, "");
        jono2Tulos.setTila(ValintatuloksenTila.KESKEN, "");
        jono2Tulos.setValintatapajonoOid("jono2", "");

        Valintatulos jono1Tulos = new Valintatulos();
        jono1Tulos.setHakemusOid("1.2.246.562.11.00001090792", "");
        jono1Tulos.setHakijaOid("1.2.246.562.24.45661259022", "");
        jono1Tulos.setHakukohdeOid("1.2.246.562.20.18895322503", "");
        jono1Tulos.setHakuOid("1.2.246.562.29.173465377510", "");
        jono1Tulos.setHakutoive(4, "");
        jono1Tulos.setHyvaksyttyVarasijalta(false, "");
        jono1Tulos.setJulkaistavissa(true, "");
        jono1Tulos.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY, "");
        jono1Tulos.setTila(ValintatuloksenTila.EI_VASTAANOTETTU_MAARA_AIKANA, "");
        jono1Tulos.setValintatapajonoOid("jono1", "");

        final SijoitteluajoWrapper sijoitteluAjo = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluAjo(), hakukohteet, Arrays.asList(jono1Tulos, jono2Tulos));

        SijoittelunTila s = SijoitteluAlgorithm.sijoittele(sijoitteluAjo);

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        assertoi(hakukohteet.get(0).getValintatapajonot()
                .get(0), "1.2.246.562.11.00001090792", HakemuksenTila.PERUNUT);
        assertoi(hakukohteet.get(0).getValintatapajonot()
                .get(1), "1.2.246.562.11.00001090792", HakemuksenTila.PERUNUT);

    }

    @Test
    @UsingDataSet(locations = "vastaanotot_perunut_kaikille_jonoille.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testPerunutVastaanottoKaikilleJonoille() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");
        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());

        Valintatulos jono2Tulos = new Valintatulos();
        jono2Tulos.setHakemusOid("1.2.246.562.11.00001090792", "");
        jono2Tulos.setHakijaOid("1.2.246.562.24.45661259022", "");
        jono2Tulos.setHakukohdeOid("1.2.246.562.20.18895322503", "");
        jono2Tulos.setHakuOid("1.2.246.562.29.173465377510", "");
        jono2Tulos.setHakutoive(4, "");
        jono2Tulos.setHyvaksyttyVarasijalta(false, "");
        jono2Tulos.setJulkaistavissa(true, "");
        jono2Tulos.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY, "");
        jono2Tulos.setTila(ValintatuloksenTila.KESKEN, "");
        jono2Tulos.setValintatapajonoOid("jono2", "");

        Valintatulos jono1Tulos = new Valintatulos();
        jono1Tulos.setHakemusOid("1.2.246.562.11.00001090792", "");
        jono1Tulos.setHakijaOid("1.2.246.562.24.45661259022", "");
        jono1Tulos.setHakukohdeOid("1.2.246.562.20.18895322503", "");
        jono1Tulos.setHakuOid("1.2.246.562.29.173465377510", "");
        jono1Tulos.setHakutoive(4, "");
        jono1Tulos.setHyvaksyttyVarasijalta(false, "");
        jono1Tulos.setJulkaistavissa(true, "");
        jono1Tulos.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY, "");
        jono1Tulos.setTila(ValintatuloksenTila.PERUNUT, "");
        jono1Tulos.setValintatapajonoOid("jono1", "");

        final SijoitteluajoWrapper sijoitteluAjo = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluAjo(), hakukohteet, Arrays.asList(jono1Tulos, jono2Tulos));

        SijoittelunTila s = SijoitteluAlgorithm.sijoittele(sijoitteluAjo);

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        assertoi(hakukohteet.get(0).getValintatapajonot()
                .get(0), "1.2.246.562.11.00001090792", HakemuksenTila.PERUNUT);
        assertoi(hakukohteet.get(0).getValintatapajonot()
                .get(1), "1.2.246.562.11.00001090792", HakemuksenTila.PERUNUT);

    }

    private static void assertoi(Valintatapajono valintatapajono, String oid, HakemuksenTila tila) {
        Hakemus check = null;
        for (Hakemus hakemus : valintatapajono.getHakemukset()) {
            if (hakemus.getHakemusOid().equals(oid)) {
                check = hakemus;
            }
        }
        Assert.assertEquals(tila, check.getTila());
    }

}
