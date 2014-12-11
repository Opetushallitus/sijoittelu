package fi.vm.sade.sijoittelu;

import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.core.LoadStrategyEnum;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import de.flapdoodle.embed.process.collections.Collections;
import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.PrintHelper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithm;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithmFactoryImpl;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilanKuvaukset;
import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintalaskenta.tulos.service.impl.ValintatietoService;
import junit.framework.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;

@ContextConfiguration(locations = "classpath:test-sijoittelu-batch-mongo.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@UsingDataSet
public class SijoitteluMontaJonoaTests {

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

        Valintatulos tulos = new Valintatulos();
        tulos.setHakemusOid("1.2.246.562.11.00001068863");
        tulos.setHakukohdeOid("1.2.246.562.20.18895322503");
        tulos.setHakuOid("1.2.246.562.29.173465377510");
        tulos.setHakutoive(2);
        tulos.setHyvaksyttyVarasijalta(false);
        tulos.setIlmoittautumisTila(IlmoittautumisTila.LASNA_KOKO_LUKUVUOSI);
        tulos.setJulkaistavissa(true);
        tulos.setTila(ValintatuloksenTila.VASTAANOTTANUT);
        tulos.setValintatapajonoOid("oid2");

        SijoitteluAlgorithmFactoryImpl h = new SijoitteluAlgorithmFactoryImpl();
        SijoitteluAlgorithm s = h.constructAlgorithm(hakukohteet, Arrays.asList(tulos));
        s.start();

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

        List<Valintatulos> list = s.getSijoitteluAjo().getMuuttuneetValintatulokset();
        Assert.assertTrue(list.size() == 2);
        list.forEach(v -> {
            if(v.getValintatapajonoOid().equals("oid1")) {
                Assert.assertTrue(v.getTila().equals(ValintatuloksenTila.VASTAANOTTANUT));
                Assert.assertTrue(v.getIlmoittautumisTila().equals(IlmoittautumisTila.LASNA_KOKO_LUKUVUOSI));
            }
            if(v.getValintatapajonoOid().equals("oid2")) {
                Assert.assertTrue(v.getTila().equals(ValintatuloksenTila.KESKEN));
                Assert.assertTrue(v.getIlmoittautumisTila().equals(IlmoittautumisTila.EI_TEHTY));
            }
        });


    }

    @Test
    @UsingDataSet(locations = "monta_jonoa.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testSijoitteluMontaJonoaEiValintatulosta() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());

        SijoitteluAlgorithmFactoryImpl h = new SijoitteluAlgorithmFactoryImpl();
        SijoitteluAlgorithm s = h.constructAlgorithm(hakukohteet, Collections.newArrayList());
        s.start();

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
        tulos.setHakemusOid("1.2.246.562.11.00001067411");
        tulos.setHakijaOid("1.2.246.562.11.00001067411");
        tulos.setHakukohdeOid("1.2.246.562.20.18895322503");
        tulos.setHakuOid("1.2.246.562.29.173465377510");
        tulos.setHakutoive(1);
        tulos.setHyvaksyttyVarasijalta(false);
        tulos.setIlmoittautumisTila(IlmoittautumisTila.POISSA);
        tulos.setJulkaistavissa(true);
        tulos.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI);
        tulos.setValintatapajonoOid("oid1");

        SijoitteluAlgorithmFactoryImpl h = new SijoitteluAlgorithmFactoryImpl();
        SijoitteluAlgorithm s = h.constructAlgorithm(hakukohteet, Arrays.asList(tulos));
        s.start();

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "1.2.246.562.11.00001068863", "1.2.246.562.11.00001067411");
        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(1), "1.2.246.562.11.00001090792");

    }

    @Test
    @UsingDataSet(locations = "monta_jonoa.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testPerunut() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        Valintatulos tulos = new Valintatulos();
        tulos.setHakemusOid("1.2.246.562.11.00001068863");
        tulos.setHakijaOid("1.2.246.562.11.00001068863");
        tulos.setHakukohdeOid("1.2.246.562.20.18895322503");
        tulos.setHakuOid("1.2.246.562.29.173465377510");
        tulos.setHakutoive(1);
        tulos.setHyvaksyttyVarasijalta(false);
        tulos.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY);
        tulos.setJulkaistavissa(true);
        tulos.setTila(ValintatuloksenTila.EI_VASTAANOTETTU_MAARA_AIKANA);
        tulos.setValintatapajonoOid("oid1");

        SijoitteluAlgorithmFactoryImpl h = new SijoitteluAlgorithmFactoryImpl();
        SijoitteluAlgorithm s = h.constructAlgorithm(hakukohteet, Arrays.asList(tulos));
        s.start();

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "1.2.246.562.11.00001090792");
        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(1), "1.2.246.562.11.00001067411");

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001068863")) {
                Assert.assertTrue(hak.getTila().equals(HakemuksenTila.PERUNUT));
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(1).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001068863")) {
                Assert.assertTrue(hak.getTila().equals(HakemuksenTila.PERUNUT));
            }
        });



        Valintatulos tulos2 = new Valintatulos();
        tulos2.setHakemusOid("1.2.246.562.11.00001068863");
        tulos2.setHakijaOid("1.2.246.562.11.00001068863");
        tulos2.setHakukohdeOid("1.2.246.562.20.18895322503");
        tulos2.setHakuOid("1.2.246.562.29.173465377510");
        tulos2.setHakutoive(1);
        tulos2.setHyvaksyttyVarasijalta(false);
        tulos2.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY);
        tulos2.setJulkaistavissa(true);
        tulos2.setTila(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT);
        tulos2.setValintatapajonoOid("oid2");

        s = h.constructAlgorithm(hakukohteet, Arrays.asList(tulos, tulos2));
        s.start();

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "1.2.246.562.11.00001090792");
        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(1), "1.2.246.562.11.00001068863");

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001068863")) {
                Assert.assertTrue(hak.getTila().equals(HakemuksenTila.PERUNUT));
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(1).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001068863")) {
                Assert.assertTrue(hak.getTila().equals(HakemuksenTila.HYVAKSYTTY));
            }
        });

        tulos2.setTila(ValintatuloksenTila.KESKEN);

        s = h.constructAlgorithm(hakukohteet, Arrays.asList(tulos, tulos2));
        s.start();

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "1.2.246.562.11.00001090792");
        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(1), "1.2.246.562.11.00001067411");

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001068863")) {
                Assert.assertTrue(hak.getTila().equals(HakemuksenTila.PERUNUT));
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(1).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001068863")) {
                Assert.assertTrue(hak.getTila().equals(HakemuksenTila.PERUNUT));
            }
        });

    }

    @Test
    @UsingDataSet(locations = "ei_varasijatayttoa.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testEiVarasijatayttoa() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());

        SijoitteluAlgorithmFactoryImpl h = new SijoitteluAlgorithmFactoryImpl();
        SijoitteluAlgorithm s = h.constructAlgorithm(hakukohteet, Collections.newArrayList());
        s.getSijoitteluAjo().setVarasijaSaannotAstuvatVoimaan(LocalDateTime.now().plusDays(1));
        s.start();

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

        Valintatulos tulos = new Valintatulos();
        tulos.setHakemusOid("1.2.246.562.11.00001068863");
        tulos.setHakijaOid("1.2.246.562.11.00001068863");
        tulos.setHakukohdeOid("1.2.246.562.20.18895322503");
        tulos.setHakutoive(1);
        tulos.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY);
        tulos.setTila(ValintatuloksenTila.PERUNUT);
        tulos.setJulkaistavissa(true);
        tulos.setValintatapajonoOid("oid1");

        Valintatulos tulos2 = new Valintatulos();
        tulos2.setHakemusOid("1.2.246.562.11.00001090792");
        tulos2.setHakijaOid("1.2.246.562.11.00001090792");
        tulos2.setHakukohdeOid("1.2.246.562.20.18895322503");
        tulos2.setHakutoive(1);
        tulos2.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY);
        tulos2.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI);
        tulos2.setJulkaistavissa(true);
        tulos2.setValintatapajonoOid("oid1");

        s = h.constructAlgorithm(hakukohteet, Arrays.asList(tulos));
        s.getSijoitteluAjo().setVarasijaSaannotAstuvatVoimaan(LocalDateTime.now().plusDays(1));
        s.start();

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

        s = h.constructAlgorithm(hakukohteet, Arrays.asList(tulos, tulos2));
        s.start();

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

        s = h.constructAlgorithm(hakukohteet, Arrays.asList(tulos, tulos2));
        s.getSijoitteluAjo().setKKHaku(true);
        s.start();

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

    }

    @Test
    @UsingDataSet(locations = "varasijat_rajattu.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testVarasijatRajattu() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

        haku.getHakukohteet().get(0).getValinnanvaihe().get(0).getValintatapajonot().get(0).setVarasijat(1);

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());

        SijoitteluAlgorithmFactoryImpl h = new SijoitteluAlgorithmFactoryImpl();
        SijoitteluAlgorithm s = h.constructAlgorithm(hakukohteet, Collections.newArrayList());
        s.start();

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
    @UsingDataSet(locations = "tayttojono.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testTayttoJono() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

        haku.getHakukohteet().get(0).getValinnanvaihe().get(0).getValintatapajonot().get(0).setTayttojono("oid2");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());

        SijoitteluAlgorithmFactoryImpl h = new SijoitteluAlgorithmFactoryImpl();
        SijoitteluAlgorithm s = h.constructAlgorithm(hakukohteet, Collections.newArrayList());
        s.start();

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

        SijoitteluAlgorithmFactoryImpl h = new SijoitteluAlgorithmFactoryImpl();
        SijoitteluAlgorithm s = h.constructAlgorithm(hakukohteet, Collections.newArrayList());
        s.start();

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        hakukohteet.remove(2);

        h = new SijoitteluAlgorithmFactoryImpl();
        s = h.constructAlgorithm(hakukohteet, Collections.newArrayList());
        s.start();

        System.out.println(PrintHelper.tulostaSijoittelu(s));

    }

    @Test
    @UsingDataSet(locations = "monta_jonoa_tasasija_arvonta.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testTasasijaArvonta() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");




        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());

        SijoitteluAlgorithmFactoryImpl h = new SijoitteluAlgorithmFactoryImpl();
        SijoitteluAlgorithm s = h.constructAlgorithm(hakukohteet, Collections.newArrayList());
        s.start();

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


        s = h.constructAlgorithm(hakukohteet, Arrays.asList(tulos11, tulos12,tulos21, tulos22,tulos31, tulos32,tulos41, tulos42,tulos51, tulos52,tulos61, tulos62,tulos71, tulos72));
        s.start();

        tulos11.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI);
        tulos11.setIlmoittautumisTila(IlmoittautumisTila.POISSA);

        tulos22.setTila(ValintatuloksenTila.PERUNUT);

        s = h.constructAlgorithm(hakukohteet, Arrays.asList(tulos11, tulos12,tulos21, tulos22,tulos31, tulos32,tulos41, tulos42,tulos51, tulos52,tulos61, tulos62,tulos71, tulos72));
        s.start();

        System.out.println(PrintHelper.tulostaSijoittelu(s));


    }


    @Test
    @UsingDataSet(locations = "poissa_oleva_taytto.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testPoissaOloTaytto2() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());

        SijoitteluAlgorithmFactoryImpl h = new SijoitteluAlgorithmFactoryImpl();
        SijoitteluAlgorithm s = h.constructAlgorithm(hakukohteet, Collections.newArrayList());
        s.start();

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



        s = h.constructAlgorithm(hakukohteet, Arrays.asList(tulos1, tulos2,tulos3, tulos4,tulos5, tulos6,tulos7, tulos8,tulos9, tulos10));
        s.start();

        tulos1.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI);
        tulos1.setIlmoittautumisTila(IlmoittautumisTila.POISSA);

        tulos2.setTila(ValintatuloksenTila.PERUNUT);

        tulos3.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI);
        tulos3.setIlmoittautumisTila(IlmoittautumisTila.POISSA);

        s = h.constructAlgorithm(hakukohteet, Arrays.asList(tulos1, tulos2,tulos3, tulos4,tulos5, tulos6,tulos7, tulos8,tulos9, tulos10));
        s.start();

        int koko = hakukohteet.get(0).getValintatapajonot().get(0)
                .getHakemukset().stream()
                .filter(hak->hak.getTila() == HakemuksenTila.HYVAKSYTTY || hak.getTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY)
                .collect(Collectors.toList()).size();

        Assert.assertEquals(koko, 7);


    }

    private Valintatulos createTulos(String hakemus, String hakukohde, String valintatapajono) {
        Valintatulos tulos = new Valintatulos();
        tulos.setHakemusOid(hakemus);
        tulos.setHakijaOid("hakijaoid");
        tulos.setHakukohdeOid(hakukohde);
        tulos.setHakutoive(1);
        tulos.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY);
        tulos.setTila(ValintatuloksenTila.KESKEN);
        tulos.setJulkaistavissa(true);
        tulos.setValintatapajonoOid(valintatapajono);
        return tulos;
    }

    public final static void assertoiAinoastaanValittu(Valintatapajono h, String... oids) {
        List<String> wanted = Arrays.asList(oids);
        List<String> actual = new ArrayList<String>();
        for (Hakemus hakemus : h.getHakemukset()) {
            if (hakemus.getTila() == HakemuksenTila.HYVAKSYTTY) {
                actual.add(hakemus.getHakemusOid());
            }
        }
        Assert.assertTrue("Actual result does not contain all wanted approved OIDs", actual.containsAll(wanted));
        Assert.assertTrue("Wanted result contains more approved OIDs than actual", wanted.containsAll(actual));
    }

}