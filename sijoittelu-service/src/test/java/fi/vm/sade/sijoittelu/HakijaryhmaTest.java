package fi.vm.sade.sijoittelu;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.PrintHelper.tulostaSijoittelu;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.core.LoadStrategyEnum;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;

import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluConfiguration;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluajoWrapperFactory;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoittelunTila;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.SijoitteluAlgorithmUtil;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakijaryhmaWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintalaskenta.tulos.service.impl.ValintatietoService;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@ContextConfiguration(locations = "classpath:test-sijoittelu-batch-mongo.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@UsingDataSet
public class HakijaryhmaTest {
    private static final Logger LOG = LoggerFactory.getLogger(HakijaryhmaTest.class);
    @Autowired
    private ValintatietoService valintatietoService;

    @Autowired
    private ApplicationContext applicationContext;

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("test");

	@Test
    @UsingDataSet(locations = "vaasan_yliopisto_valinnan_vaiheet.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
	public void testSijoitteluOneHakijaryhma() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(hakukohteet, Collections.emptyList(), Collections.emptyMap());

        System.out.println(tulostaSijoittelu(s));

        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "1.2.246.562.11.00001067411", "1.2.246.562.11.00001068863");


	}

    @Test
    @UsingDataSet(locations = "vaasan_yliopisto_valinnan_vaiheet_kaksi_hakijaryhmaa.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testSijoitteluTwoHakijaryhma() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(hakukohteet, Collections.emptyList(), Collections.emptyMap());

        System.out.println(tulostaSijoittelu(s));

        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "1.2.246.562.11.00001067411", "1.2.246.562.11.00001090792");


    }

    @Test
    @UsingDataSet(locations = "vaasan_yliopisto_valinnan_vaiheet_kaksi_hakijaryhmaa_toinen_eri_jonossa.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testSijoitteluTwoHakijaryhmaToisessaEriJono() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(hakukohteet, Collections.emptyList(), Collections.emptyMap());

        System.out.println(tulostaSijoittelu(s));
        Assert.assertEquals(2, hakukohteet.get(0).getHakijaryhmat().size());
        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "1.2.246.562.11.00001068863", "1.2.246.562.11.00001067411");
    }

    @Test
    @UsingDataSet(locations = "vaasan_yliopisto_valinnan_vaiheet_ei_hakijaryhmaa.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testSijoitteluEiHakijaryhma() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(hakukohteet, Collections.emptyList(), Collections.emptyMap());

        System.out.println(tulostaSijoittelu(s));

        Assert.assertEquals(0, hakukohteet.get(0).getHakijaryhmat().size());
        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "1.2.246.562.11.00001068863", "1.2.246.562.11.00001090792", "1.2.246.562.11.00001067411");
    }

    @Test
    @Ignore
    @UsingDataSet(locations = "alitaytto_simple_case.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    // Korjaa tämä kun ikiluuppi on korjattu
    public void testAlitayttoRekursio() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        final SijoitteluajoWrapper sijoitteluAjo = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluConfiguration(), new SijoitteluAjo(), hakukohteet, Collections.emptyList(), Collections.emptyMap());
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(sijoitteluAjo);
        sijoitteluAjo.setKaikkiKohteetSijoittelussa(LocalDateTime.now().plusDays(10));

        LOG.info("\r\n{}", tulostaSijoittelu(s));
        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "hakija1");

    }

    @Test
    @UsingDataSet(locations = "ylitaytto_simple_case.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testYlitayttoRekursio() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());

        final SijoitteluajoWrapper sijoitteluAjo = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluConfiguration(), new SijoitteluAjo(), hakukohteet, Collections.emptyList(), Collections.emptyMap());
        sijoitteluAjo.setKaikkiKohteetSijoittelussa(LocalDateTime.now().plusDays(10));
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(sijoitteluAjo);

        LOG.info("\r\n{}", tulostaSijoittelu(s));

        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "hakija3", "hakija4", "hakija5");

    }

    @Ignore
    @Test
    @UsingDataSet(locations = "ylitaytto_vaihe.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testYlitaytto() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        final SijoitteluajoWrapper sijoitteluAjo = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluConfiguration(), new SijoitteluAjo(), hakukohteet, Collections.emptyList(), Collections.emptyMap());
        sijoitteluAjo.setKaikkiKohteetSijoittelussa(LocalDateTime.now().plusDays(10));
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(sijoitteluAjo);

        System.out.println(tulostaSijoittelu(s));

//        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "hakija1", "hakija3", "hakija4", "hakija5");

    }

    @Test
    @UsingDataSet(locations = "vain_ryhmaan_kuuluvat_hyvaksytaan.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testSijoitteluVainHakijaryhmaanKuuluvatVoivatTullaHyvaksytyksi() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(hakukohteet, Collections.emptyList(), Collections.emptyMap());

        System.out.println(tulostaSijoittelu(s));

        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "hakemus1", "hakemus2");
        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(1), "hakemus3");


    }

    @Test(expected = IllegalStateException.class)
    @UsingDataSet(locations = "toisensa_pois_sulkevat_hakijaryhmat.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testSijoitteluToisensaPoisSulkevatRyhmat() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        SijoitteluAlgorithmUtil.sijoittele(hakukohteet, Collections.emptyList(), Collections.emptyMap());

    }

    @Test
    @UsingDataSet(locations = "hakijaryhma_varasijasaannot_paattyneet.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testSijoitteluVarasijaSaannotPaattyneet() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");
        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());

        final SijoitteluajoWrapper sijoitteluAjo = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluConfiguration(), new SijoitteluAjo(), hakukohteet, Collections.emptyList(), Collections.emptyMap());
        sijoitteluAjo.setKaikkiKohteetSijoittelussa(LocalDateTime.now().plusDays(10));

        /**
         * Luodaan alkutilanne:
         *
         * valintatapajono1: A (HYVÄKSYTTY), B (VARALLA)
         * valintatapajono2: C (HYVÄKSYTTY), D (VARALLA)
         */
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(sijoitteluAjo);
        System.out.println(tulostaSijoittelu(s));

        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "A");
        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(1), "C");

        /**
         * Muokataan kiintiöitä, että B ja D uudelleen sijoitellaan. Lisäksi
         * lisätään ensimmäiselle valintatapajonolle varasijatäyttö päättyneeksi,
         * joten D tulisi ainoastaan hyväksyä tässä sijoittelussa.
         */
        s.sijoitteluAjo.getHakukohteet().stream()
                .flatMap(hk -> hk.getHakijaryhmaWrappers().stream())
                .map(HakijaryhmaWrapper::getHakijaryhma)
                .forEach(hk -> hk.setKiintio(4));
        s.sijoitteluAjo.getHakukohteet().forEach(hk -> {
            List<Valintatapajono> valintatapajonot = hk.getValintatapajonot().stream().map(ValintatapajonoWrapper::getValintatapajono).collect(Collectors.toList());
            valintatapajonot.forEach(vtj -> vtj.setAloituspaikat(2));
            valintatapajonot.stream().filter(v -> v.getNimi().equals("valintatapajono1")).forEach(v -> {
                Date kolmePaivaaSitten = Date.from(LocalDateTime.now().minusDays(3L).atZone(ZoneId.systemDefault()).toInstant());
                v.setVarasijojaTaytetaanAsti(kolmePaivaaSitten);
            });
        });

        SijoittelunTila s2 = SijoitteluAlgorithmUtil.sijoittele(s.sijoitteluAjo);
        System.out.println(tulostaSijoittelu(s2));

        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "A");
        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(1), "C", "D");
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
