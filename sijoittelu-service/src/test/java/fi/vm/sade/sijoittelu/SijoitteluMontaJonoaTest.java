package fi.vm.sade.sijoittelu;

import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.core.LoadStrategyEnum;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import de.flapdoodle.embed.process.collections.Collections;
import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.PrintHelper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluajoWrapperFactory;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithm;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoittelunTila;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.SijoitteluAlgorithmUtil;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilanKuvaukset;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintalaskenta.tulos.service.impl.ValintatietoService;
import junit.framework.Assert;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;

@ContextConfiguration(locations = "classpath:test-sijoittelu-batch-mongo.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@UsingDataSet
public class SijoitteluMontaJonoaTest {

    @Autowired
    private ValintatietoService valintatietoService;

    @Autowired
    private ApplicationContext applicationContext;

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("test");

	@Test
    @UsingDataSet(locations = "monta_jonoa.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void testSijoitteluMontaJonoa() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        hakukohteet.get(0).getValintatapajonot().get(0).setAloituspaikat(0);

        SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluAjo(), tallennaEdellisetTilat(hakukohteet), Arrays.asList());
        SijoitteluAlgorithmUtil.sijoittele(SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluAjo(), tallennaEdellisetTilat(hakukohteet), Arrays.asList()));

        Valintatulos tulos = new Valintatulos();
        tulos.setHakemusOid("1.2.246.562.11.00001068863", "");
        tulos.setHakukohdeOid("1.2.246.562.20.18895322503", "");
        tulos.setHakuOid("1.2.246.562.29.173465377510", "");
        tulos.setHakutoive(2, "");
        tulos.setHyvaksyttyVarasijalta(false, "");
        tulos.setIlmoittautumisTila(IlmoittautumisTila.LASNA_KOKO_LUKUVUOSI, "");
        tulos.setJulkaistavissa(true, "");
        tulos.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, "");
        tulos.setValintatapajonoOid("oid2", "");

        hakukohteet.get(0).getValintatapajonot().get(0).setAloituspaikat(1);
        final SijoitteluajoWrapper sijoitteluAjo = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluAjo(), tallennaEdellisetTilat(hakukohteet), Arrays.asList(tulos));
        final SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(sijoitteluAjo);

        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "1.2.246.562.11.00001068863");
        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(1), "1.2.246.562.11.00001090792");

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001090792")) {
                Assert.assertTrue(hak.getTila().equals(HakemuksenTila.VARALLA));
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(1).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001068863")) {
                Assert.assertTrue(hak.getTila().equals(HakemuksenTila.PERUUNTUNUT));
            }
        });
        System.out.println(PrintHelper.tulostaSijoittelu(s));
        List<Valintatulos> list = sijoitteluAjo.getMuuttuneetValintatulokset();
        Assert.assertEquals(2, list.size());
        list.forEach(v -> {
            if(v.getValintatapajonoOid().equals("oid1")) {
                Assert.assertEquals(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, v.getTila());
                Assert.assertEquals(IlmoittautumisTila.LASNA_KOKO_LUKUVUOSI, v.getIlmoittautumisTila());
            }
            if(v.getValintatapajonoOid().equals("oid2")) {
                Assert.assertEquals(ValintatuloksenTila.KESKEN, v.getTila());
                Assert.assertEquals(IlmoittautumisTila.EI_TEHTY, v.getIlmoittautumisTila());
            }
        });


    }

    @Test
    @UsingDataSet(locations = "monta_jonoa.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testSijoitteluMontaJonoaEiValintatulosta() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(new SijoitteluAjo(), hakukohteet, new ArrayList());

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "1.2.246.562.11.00001068863");
        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(1), "1.2.246.562.11.00001090792");

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001090792")) {
                Assert.assertTrue(hak.getTila().equals(HakemuksenTila.VARALLA));
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(1).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001068863")) {
                Assert.assertTrue(hak.getTila().equals(HakemuksenTila.PERUUNTUNUT));
            }
        });

    }

    @Test
    @UsingDataSet(locations = "monta_jonoa.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testPoissaolevaTaytto() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        Valintatulos tulos = new Valintatulos();
        tulos.setHakemusOid("1.2.246.562.11.00001067411", "");
        tulos.setHakijaOid("1.2.246.562.11.00001067411", "");
        tulos.setHakukohdeOid("1.2.246.562.20.18895322503", "");
        tulos.setHakuOid("1.2.246.562.29.173465377510", "");
        tulos.setHakutoive(1, "");
        tulos.setHyvaksyttyVarasijalta(false, "");
        tulos.setIlmoittautumisTila(IlmoittautumisTila.POISSA, "");
        tulos.setJulkaistavissa(true, "");
        tulos.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, "");
        tulos.setValintatapajonoOid("oid1", "");
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(new SijoitteluAjo(), hakukohteet, Arrays.asList(tulos));

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "1.2.246.562.11.00001068863");
        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(1), "1.2.246.562.11.00001090792");

    }

    @Test
    @UsingDataSet(locations = "monta_jonoa.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testPerunut() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());

        System.out.println(PrintHelper.tulostaSijoittelu(SijoitteluAlgorithmUtil.sijoittele(new SijoitteluAjo(), hakukohteet, Arrays.asList())));

        Valintatulos tulos = new Valintatulos();
        tulos.setHakemusOid("1.2.246.562.11.00001068863", "");
        tulos.setHakijaOid("1.2.246.562.11.00001068863", "");
        tulos.setHakukohdeOid("1.2.246.562.20.18895322503", "");
        tulos.setHakuOid("1.2.246.562.29.173465377510", "");
        tulos.setHakutoive(1, "");
        tulos.setHyvaksyttyVarasijalta(false, "");
        tulos.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY, "");
        tulos.setJulkaistavissa(true, "");
        tulos.setTila(ValintatuloksenTila.EI_VASTAANOTETTU_MAARA_AIKANA, "");
        tulos.setValintatapajonoOid("oid1", "");
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(new SijoitteluAjo(), tallennaEdellisetTilat(hakukohteet), Arrays.asList(tulos));

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "1.2.246.562.11.00001090792");
        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(1), "1.2.246.562.11.00001067411");

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001068863")) {
                Assert.assertTrue(hak.getTila().equals(HakemuksenTila.PERUNUT));
                TilaHistoria th = new TilaHistoria(HakemuksenTila.HYVAKSYTTY);
                Date date = DateUtils.addDays(new Date(), -2);
                th.setLuotu(date);
                hak.getTilaHistoria().add(th);
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(1).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001068863")) {
                Assert.assertTrue(hak.getTila().equals(HakemuksenTila.PERUNUT));
                TilaHistoria th = new TilaHistoria(HakemuksenTila.HYVAKSYTTY);
                Date date = DateUtils.addDays(new Date(), -4);
                th.setLuotu(date);
                hak.getTilaHistoria().add(th);
            }
        });

        tulos.setTila(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT, "");

        Valintatulos tulos2 = new Valintatulos();
        tulos2.setHakemusOid("1.2.246.562.11.00001068863", "");
        tulos2.setHakijaOid("1.2.246.562.11.00001068863", "");
        tulos2.setHakukohdeOid("1.2.246.562.20.18895322503", "");
        tulos2.setHakuOid("1.2.246.562.29.173465377510", "");
        tulos2.setHakutoive(1, "");
        tulos2.setHyvaksyttyVarasijalta(false, "");
        tulos2.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY, "");
        tulos2.setJulkaistavissa(true, "");
        tulos2.setTila(ValintatuloksenTila.KESKEN, "");
        tulos2.setValintatapajonoOid("oid2", "");

        //hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        s = SijoitteluAlgorithmUtil.sijoittele(new SijoitteluAjo(), tallennaEdellisetTilat(hakukohteet), Arrays.asList(tulos, tulos2));

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "1.2.246.562.11.00001090792", "1.2.246.562.11.00001068863");
        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(1), "1.2.246.562.11.00001067411");

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001068863")) {
                Assert.assertTrue(hak.getTila().equals(HakemuksenTila.HYVAKSYTTY));
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(1).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001068863")) {
                Assert.assertTrue(hak.getTila().equals(HakemuksenTila.PERUUNTUNUT));
            }
        });

    }

    @Test
    @UsingDataSet(locations = "ei_varasijatayttoa.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testEiVarasijatayttoa() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        final SijoitteluajoWrapper sijoitteluAjo = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluAjo(), tallennaEdellisetTilat(hakukohteet), Collections.newArrayList());
        sijoitteluAjo.setVarasijaSaannotAstuvatVoimaan(LocalDateTime.now().plusDays(1));
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(sijoitteluAjo);

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001068863")) {
                Assert.assertTrue(hak.getTila().equals(HakemuksenTila.HYVAKSYTTY));
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001090792")) {
                Assert.assertTrue(hak.getTila().equals(HakemuksenTila.VARALLA));
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001067411")) {
                Assert.assertTrue(hak.getTila().equals(HakemuksenTila.VARALLA));
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("hylatty")) {
                Assert.assertTrue(hak.getTila().equals(HakemuksenTila.HYLATTY));
            }
        });

        Valintatulos tulos = new Valintatulos();
        tulos.setHakemusOid("1.2.246.562.11.00001068863", "");
        tulos.setHakijaOid("1.2.246.562.11.00001068863", "");
        tulos.setHakukohdeOid("1.2.246.562.20.18895322503", "");
        tulos.setHakutoive(1, "");
        tulos.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY, "");
        tulos.setTila(ValintatuloksenTila.PERUNUT, "");
        tulos.setJulkaistavissa(true, "");
        tulos.setValintatapajonoOid("oid1", "");

        Valintatulos tulos2 = new Valintatulos();
        tulos2.setHakemusOid("1.2.246.562.11.00001090792", "");
        tulos2.setHakijaOid("1.2.246.562.11.00001090792", "");
        tulos2.setHakukohdeOid("1.2.246.562.20.18895322503", "");
        tulos2.setHakutoive(1, "");
        tulos2.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY, "");
        tulos2.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, "");
        tulos2.setJulkaistavissa(true, "");
        tulos2.setValintatapajonoOid("oid1", "");
        SijoitteluajoWrapper sijoitteluajoWrapper = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluAjo(), tallennaEdellisetTilat(hakukohteet), Arrays.asList(tulos));
        sijoitteluajoWrapper.setVarasijaSaannotAstuvatVoimaan(LocalDateTime.now().plusDays(1));
        s = SijoitteluAlgorithmUtil.sijoittele(sijoitteluajoWrapper);

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001068863")) {
                Assert.assertTrue(hak.getTila().equals(HakemuksenTila.PERUNUT));
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001090792")) {
                Assert.assertTrue(hak.getTila().equals(HakemuksenTila.HYVAKSYTTY));
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001067411")) {
                Assert.assertTrue(hak.getTila().equals(HakemuksenTila.VARALLA));
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("hylatty")) {
                Assert.assertTrue(hak.getTila().equals(HakemuksenTila.HYLATTY));
            }
        });
        s = SijoitteluAlgorithmUtil.sijoittele(new SijoitteluAjo(), tallennaEdellisetTilat(hakukohteet), Arrays.asList(tulos, tulos2));

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001068863")) {
                Assert.assertTrue(hak.getTila().equals(HakemuksenTila.PERUNUT));
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001090792")) {
                Assert.assertTrue(hak.getTila().equals(HakemuksenTila.HYVAKSYTTY));
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001067411")) {
                Assert.assertTrue(hak.getTila().equals(HakemuksenTila.VARALLA));
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("hylatty")) {
                Assert.assertTrue(hak.getTila().equals(HakemuksenTila.HYLATTY));
            }
        });
        sijoitteluajoWrapper = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluAjo(), tallennaEdellisetTilat(hakukohteet), Arrays.asList(tulos, tulos2));
        sijoitteluajoWrapper.setKKHaku(true);
        s = SijoitteluAlgorithmUtil.sijoittele(sijoitteluajoWrapper);

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001068863")) {
                Assert.assertTrue(hak.getTila().equals(HakemuksenTila.PERUNUT));
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001090792")) {
                Assert.assertTrue(hak.getTila().equals(HakemuksenTila.HYVAKSYTTY));
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001067411")) {
                Assert.assertTrue(hak.getTila().equals(HakemuksenTila.PERUUNTUNUT));
                Assert.assertEquals(hak.getTilanKuvaukset().get("FI"), TilanKuvaukset.peruuntunutAloituspaikatTaynna().get("FI"));
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("hylatty")) {
                Assert.assertTrue(hak.getTila().equals(HakemuksenTila.HYLATTY));
            }
        });

    }

    @Test
    @UsingDataSet(locations = "varasijat_rajattu.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testVarasijatRajattu() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

        haku.getHakukohteet().get(0).getValinnanvaihe().get(0).getValintatapajonot().get(0).setVarasijat(1);

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(new SijoitteluAjo(), hakukohteet, new ArrayList());

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001068863")) {
                Assert.assertTrue(hak.getTila().equals(HakemuksenTila.HYVAKSYTTY));
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001090792")) {
                Assert.assertTrue(hak.getTila().equals(HakemuksenTila.VARALLA));
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001067411")) {
                Assert.assertTrue(hak.getTila().equals(HakemuksenTila.PERUUNTUNUT));
                Assert.assertEquals(hak.getTilanKuvaukset().get("FI"), TilanKuvaukset.peruuntunutEiMahduKasiteltavienVarasijojenMaaraan().get("FI"));
            }
        });

    }

    @Test
    @UsingDataSet(locations = "ehdolliset_sitoviksi.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testEhdollisetSitoviksi() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());

        LocalDateTime time = LocalDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault());

        //hakukohteet.get(0).getValintatapajonot().get(0).setVarasijojaTaytetaanAsti(new Date(time.minusHours(10).toInstant(ZoneOffset.UTC).toEpochMilli()));

        final SijoitteluajoWrapper sijoitteluAjo1 = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluAjo(), hakukohteet, Arrays.asList());
        sijoitteluAjo1.setKKHaku(true);
        SijoittelunTila s1 = SijoitteluAlgorithmUtil.sijoittele(sijoitteluAjo1);

        System.out.println(PrintHelper.tulostaSijoittelu(s1));

        Valintatulos tulos1 = createTulos("hakemus2", "hakukohde1", "oid1");
        tulos1.setTila(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT, "");

        Valintatulos tulos2 = createTulos("hakemus3", "hakukohde1", "oid2");
        tulos2.setTila(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT, "");

        final SijoitteluajoWrapper sijoitteluAjo2 = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluAjo(), tallennaEdellisetTilat(hakukohteet), Arrays.asList(tulos1, tulos2));
        sijoitteluAjo2.setKKHaku(true);
        sijoitteluAjo2.setVarasijaTayttoPaattyy(LocalDateTime.now().minus(Period.ofDays(10)));
        SijoittelunTila s2 = SijoitteluAlgorithmUtil.sijoittele(sijoitteluAjo2);

        System.out.println(PrintHelper.tulostaSijoittelu(s2));

        List<Valintatulos> muuttuneetValintatulokset = sijoitteluAjo2.getMuuttuneetValintatulokset();

        System.out.println("muuttuneetValintatulokset: " + muuttuneetValintatulokset);

        Assert.assertEquals(2, muuttuneetValintatulokset.size());
        Assert.assertEquals(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, muuttuneetValintatulokset.get(0).getTila());
        Assert.assertEquals(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, muuttuneetValintatulokset.get(1).getTila());

    }

    // Täyttöjonosääntö vaatii speksausta
    @Ignore
    @Test
    @UsingDataSet(locations = "tayttojono.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testTayttoJono() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

        haku.getHakukohteet().get(0).getValinnanvaihe().get(0).getValintatapajonot().get(0).setTayttojono("oid2");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(new SijoitteluAjo(), hakukohteet, new ArrayList());

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001067411")) {
                Assert.assertTrue(hak.getTila().equals(HakemuksenTila.HYVAKSYTTY));
                Assert.assertEquals(hak.getTilanKuvaukset().get("FI"), TilanKuvaukset.hyvaksyttyTayttojonoSaannolla("Koe").get("FI"));
            }
        });

    }

    @Test
    @UsingDataSet(locations = "peruuta_alemmat.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testPeruutaAlemmat() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");


        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(new SijoitteluAjo(), hakukohteet, new ArrayList());

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        hakukohteet.remove(2);
        s = SijoitteluAlgorithmUtil.sijoittele(new SijoitteluAjo(), hakukohteet, new ArrayList());

        System.out.println(PrintHelper.tulostaSijoittelu(s));

    }

    @Test
    @UsingDataSet(locations = "monta_jonoa_tasasija_arvonta.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testTasasijaArvonta() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");




        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(new SijoitteluAjo(), hakukohteet, new ArrayList());

        Valintatulos tulos11 = createTulos("oid1", "hakukohde1", "oid1");
        Valintatulos tulos12 = createTulos("oid1", "hakukohde1", "oid2");

        Valintatulos tulos21 = createTulos("oid2", "hakukohde1", "oid1");
        Valintatulos tulos22 = createTulos("oid2", "hakukohde1", "oid2");

        Valintatulos tulos31 = createTulos("oid3", "hakukohde1", "oid1");
        Valintatulos tulos32 = createTulos("oid3", "hakukohde1", "oid2");

        Valintatulos tulos41 = createTulos("oid4", "hakukohde1", "oid1");
        Valintatulos tulos42 = createTulos("oid4", "hakukohde1", "oid2");

        Valintatulos tulos51 = createTulos("oid5", "hakukohde1", "oid1");
        Valintatulos tulos52 = createTulos("oid5", "hakukohde1", "oid2");

        Valintatulos tulos61 = createTulos("oid6", "hakukohde1", "oid1");
        Valintatulos tulos62 = createTulos("oid6", "hakukohde1", "oid2");

        Valintatulos tulos71 = createTulos("oid7", "hakukohde1", "oid1");
        Valintatulos tulos72 = createTulos("oid7", "hakukohde1", "oid2");
        s = SijoitteluAlgorithmUtil.sijoittele(new SijoitteluAjo(), hakukohteet, Arrays.asList(tulos11, tulos12, tulos21, tulos22, tulos31, tulos32, tulos41, tulos42, tulos51, tulos52, tulos61, tulos62, tulos71, tulos72));

        tulos11.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, "");
        tulos11.setIlmoittautumisTila(IlmoittautumisTila.POISSA, "");

        tulos22.setTila(ValintatuloksenTila.PERUNUT, "");
        s = SijoitteluAlgorithmUtil.sijoittele(new SijoitteluAjo(), hakukohteet, Arrays.asList(tulos11, tulos12, tulos21, tulos22, tulos31, tulos32, tulos41, tulos42, tulos51, tulos52, tulos61, tulos62, tulos71, tulos72));

        System.out.println(PrintHelper.tulostaSijoittelu(s));


    }


    @Test
    @UsingDataSet(locations = "poissa_oleva_taytto.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testPoissaOloTaytto2() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(new SijoitteluAjo(), tallennaEdellisetTilat(hakukohteet), new ArrayList());

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        Valintatulos tulos1 = createTulos("oid1", "hakukohde1", "jono1");
        Valintatulos tulos2 = createTulos("oid2", "hakukohde1", "jono1");

        Valintatulos tulos3 = createTulos("oid3", "hakukohde1", "jono1");
        Valintatulos tulos4 = createTulos("oid4", "hakukohde1", "jono1");

        Valintatulos tulos5 = createTulos("oid5", "hakukohde1", "jono1");
        Valintatulos tulos6 = createTulos("oid6", "hakukohde1", "jono1");

        Valintatulos tulos7 = createTulos("oid7", "hakukohde1", "jono1");
        Valintatulos tulos8 = createTulos("oid8", "hakukohde1", "jono1");

        Valintatulos tulos9 = createTulos("oid9", "hakukohde1", "jono1");
        Valintatulos tulos10 = createTulos("oid10", "hakukohde1", "jono1");
        s = SijoitteluAlgorithmUtil.sijoittele(new SijoitteluAjo(), tallennaEdellisetTilat(hakukohteet), Arrays.asList(tulos1, tulos2, tulos3, tulos4, tulos5, tulos6, tulos7, tulos8, tulos9, tulos10));

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        tulos1.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, "");
        tulos1.setIlmoittautumisTila(IlmoittautumisTila.POISSA, "");

        tulos2.setTila(ValintatuloksenTila.PERUNUT, "");

        tulos3.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, "");
        tulos3.setIlmoittautumisTila(IlmoittautumisTila.POISSA, "");
        s = SijoitteluAlgorithmUtil.sijoittele(new SijoitteluAjo(), tallennaEdellisetTilat(hakukohteet), Arrays.asList(tulos1, tulos2, tulos3, tulos4, tulos5, tulos6, tulos7, tulos8, tulos9, tulos10));

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        int koko = hakukohteet.get(0).getValintatapajonot().get(0)
                .getHakemukset().stream()
                .filter(hak->hak.getTila() == HakemuksenTila.HYVAKSYTTY || hak.getTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY)
                .collect(Collectors.toList()).size();

        Assert.assertEquals(koko, 7);


    }

    @Test
    @UsingDataSet(locations = "poissa_oleva_taytto3.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testPoissaOloTaytto3() throws IOException {
        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(new SijoitteluAjo(), tallennaEdellisetTilat(hakukohteet), new ArrayList());

        Valintatulos tulos1 = createTulos("oid1", "hakukohde1", "jono1");
        Valintatulos tulos2 = createTulos("oid2", "hakukohde1", "jono1");

        Valintatulos tulos3 = createTulos("oid3", "hakukohde1", "jono1");
        Valintatulos tulos4 = createTulos("oid4", "hakukohde1", "jono1");

        Valintatulos tulos6 = createTulos("oid6", "hakukohde1", "jono1");

        Valintatulos tulos7 = createTulos("oid7", "hakukohde1", "jono1");
        Valintatulos tulos8 = createTulos("oid8", "hakukohde1", "jono1");

        Valintatulos tulos9 = createTulos("oid9", "hakukohde1", "jono1");
        Valintatulos tulos10 = createTulos("oid10", "hakukohde1", "jono1");

        Valintatulos tulos11 = createTulos("oid1", "hakukohde2", "jono2");
        Valintatulos tulos21 = createTulos("oid2", "hakukohde2", "jono2");

        Valintatulos tulos31 = createTulos("oid3", "hakukohde2", "jono2");
        Valintatulos tulos41 = createTulos("oid4", "hakukohde2", "jono2");

        Valintatulos tulos61 = createTulos("oid6", "hakukohde2", "jono2");

        Valintatulos tulos71 = createTulos("oid7", "hakukohde2", "jono2");
        Valintatulos tulos81 = createTulos("oid8", "hakukohde2", "jono2");

        Valintatulos tulos91 = createTulos("oid9", "hakukohde2", "jono2");
        Valintatulos tulos101 = createTulos("oid10", "hakukohde2", "jono2");
        s = SijoitteluAlgorithmUtil.sijoittele(new SijoitteluAjo(), tallennaEdellisetTilat(hakukohteet), Arrays.asList(tulos1, tulos2, tulos3, tulos4, tulos6, tulos7, tulos8, tulos9, tulos10, tulos11, tulos21, tulos31, tulos41, tulos61, tulos71, tulos81, tulos91, tulos101));

        tulos1.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, "");
        tulos1.setIlmoittautumisTila(IlmoittautumisTila.POISSA, "");

        tulos2.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, "");
        tulos2.setIlmoittautumisTila(IlmoittautumisTila.POISSA, "");

        tulos61.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, "");
        tulos71.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, "");
        tulos81.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, "");
        s = SijoitteluAlgorithmUtil.sijoittele(new SijoitteluAjo(), tallennaEdellisetTilat(hakukohteet), Arrays.asList(tulos1, tulos2, tulos3, tulos4, tulos6, tulos7, tulos8, tulos9, tulos10, tulos11, tulos21, tulos31, tulos41, tulos61, tulos71, tulos81, tulos91, tulos101));

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        int koko = hakukohteet.get(0).getValintatapajonot().get(0)
                .getHakemukset().stream()
                .filter(hak->hak.getTila() == HakemuksenTila.HYVAKSYTTY || hak.getTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY)
                .collect(Collectors.toList()).size();

        Assert.assertEquals(koko, 6);


    }

    @Test
    @UsingDataSet(locations = "peruuntunut_taytto.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testPeruunutunutTaytto() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(new SijoitteluAjo(), hakukohteet, new ArrayList());

        Valintatulos tulos1 = createTulos("oid1", "hakukohde1", "jono1");
        Valintatulos tulos2 = createTulos("oid2", "hakukohde1", "jono1");

        Valintatulos tulos3 = createTulos("oid3", "hakukohde1", "jono1");
        Valintatulos tulos4 = createTulos("oid4", "hakukohde1", "jono1");

        Valintatulos tulos6 = createTulos("oid6", "hakukohde1", "jono1");

        Valintatulos tulos7 = createTulos("oid7", "hakukohde1", "jono1");
        Valintatulos tulos8 = createTulos("oid8", "hakukohde1", "jono1");

        Valintatulos tulos9 = createTulos("oid9", "hakukohde1", "jono1");
        Valintatulos tulos10 = createTulos("oid10", "hakukohde1", "jono1");
        s = SijoitteluAlgorithmUtil.sijoittele(new SijoitteluAjo(), hakukohteet, Arrays.asList(tulos1, tulos2, tulos3, tulos4, tulos6, tulos7, tulos8, tulos9, tulos10));
        final SijoitteluajoWrapper sijoitteluAjo = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluAjo(), hakukohteet, Arrays.asList(tulos1, tulos2, tulos3, tulos4, tulos6, tulos7, tulos8, tulos9, tulos10));
        sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().stream().filter(hak->hak.getHakemus().getHakemusOid().equals("oid1") || hak.getHakemus().getHakemusOid().equals("oid2")).forEach(hak -> {
            hak.setTilaVoidaanVaihtaa(false);
            hak.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
        });
        s = SijoitteluAlgorithmUtil.sijoittele(sijoitteluAjo);

        int koko = hakukohteet.get(0).getValintatapajonot().get(0)
                .getHakemukset().stream()
                .filter(hak->hak.getTila() == HakemuksenTila.HYVAKSYTTY || hak.getTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY)
                .collect(Collectors.toList()).size();

        Assert.assertEquals(koko, 4);


    }

    private Valintatulos createTulos(String hakemus, String hakukohde, String valintatapajono) {
        Valintatulos tulos = new Valintatulos();
        tulos.setHakemusOid(hakemus, "");
        tulos.setHakijaOid("hakijaoid", "");
        tulos.setHakukohdeOid(hakukohde, "");
        tulos.setHakutoive(1, "");
        tulos.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY, "");
        tulos.setTila(ValintatuloksenTila.KESKEN, "");
        tulos.setJulkaistavissa(false, "");
        tulos.setValintatapajonoOid(valintatapajono, "");
        return tulos;
    }

    public final static void assertoiAinoastaanValittu(Valintatapajono h, String... oids) {
        List<String> wanted = Arrays.asList(oids);
        List<String> actual = new ArrayList<String>();
        for (Hakemus hakemus : h.getHakemukset()) {
            if (hakemus.getTila() == HakemuksenTila.HYVAKSYTTY || hakemus.getTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY) {
                actual.add(hakemus.getHakemusOid());
            }
        }
        Assert.assertTrue("Actual result does not contain all wanted approved OIDs", actual.containsAll(wanted));
        Assert.assertTrue("Wanted result contains more approved OIDs than actual", wanted.containsAll(actual));
    }

    private List<Hakukohde> tallennaEdellisetTilat(List<Hakukohde> hakukohteet) {
        hakukohteet.stream().forEach(hk -> {
            hk.getValintatapajonot().stream().forEach(jono -> {
                jono.getHakemukset().stream().forEach(h -> {
                    h.setEdellinenTila(h.getTila());
                });
            });
        });
        return hakukohteet;
    }

}
