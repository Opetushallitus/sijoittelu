package fi.vm.sade.sijoittelu;

import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.core.LoadStrategyEnum;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import de.flapdoodle.embed.process.collections.Collections;
import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.PrintHelper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAjoCreator;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithm;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintalaskenta.tulos.service.impl.ValintatietoService;
import junit.framework.Assert;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;

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

        SijoitteluAlgorithm s = SijoitteluAlgorithm.sijoittele(hakukohteet, Collections.<Valintatulos>newArrayList());

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "1.2.246.562.11.00001067411", "1.2.246.562.11.00001068863");


	}

    @Test
    @UsingDataSet(locations = "vaasan_yliopisto_valinnan_vaiheet_kaksi_hakijaryhmaa.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testSijoitteluTwoHakijaryhma() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());

        SijoitteluAlgorithm s = SijoitteluAlgorithm.sijoittele(hakukohteet, Collections.<Valintatulos>newArrayList());

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "1.2.246.562.11.00001067411", "1.2.246.562.11.00001090792");


    }

    @Test
    @UsingDataSet(locations = "vaasan_yliopisto_valinnan_vaiheet_kaksi_hakijaryhmaa_toinen_eri_jonossa.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testSijoitteluTwoHakijaryhmaToisessaEriJono() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());

        SijoitteluAlgorithm s = SijoitteluAlgorithm.sijoittele(hakukohteet, Collections.<Valintatulos>newArrayList());

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "1.2.246.562.11.00001068863", "1.2.246.562.11.00001067411");
    }

    @Test
    @UsingDataSet(locations = "vaasan_yliopisto_valinnan_vaiheet_ei_hakijaryhmaa.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testSijoitteluEiHakijaryhma() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());

        SijoitteluAlgorithm s = SijoitteluAlgorithm.sijoittele(hakukohteet, Collections.<Valintatulos>newArrayList());

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "1.2.246.562.11.00001068863", "1.2.246.562.11.00001090792", "1.2.246.562.11.00001067411");
    }

    @Test
    @Ignore
    @UsingDataSet(locations = "alitaytto_simple_case.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    // Korjaa tämä kun ikiluuppi on korjattu
    public void testAlitayttoRekursio() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        final SijoitteluajoWrapper sijoitteluAjo = SijoitteluAjoCreator.createSijoitteluAjo(hakukohteet, Collections.<Valintatulos>newArrayList());
        SijoitteluAlgorithm s = SijoitteluAlgorithm.sijoittele(sijoitteluAjo);
        sijoitteluAjo.setKaikkiKohteetSijoittelussa(LocalDateTime.now().plusDays(10));

        LOG.info("\r\n{}",PrintHelper.tulostaSijoittelu(s));
        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "hakija1");

    }

    @Test
    @UsingDataSet(locations = "ylitaytto_simple_case.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testYlitayttoRekursio() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        final SijoitteluajoWrapper sijoitteluAjo = SijoitteluAjoCreator.createSijoitteluAjo(hakukohteet, Collections.<Valintatulos>newArrayList());
        sijoitteluAjo.setKaikkiKohteetSijoittelussa(LocalDateTime.now().plusDays(10));
        SijoitteluAlgorithm s = SijoitteluAlgorithm.sijoittele(sijoitteluAjo);

        LOG.info("\r\n{}",PrintHelper.tulostaSijoittelu(s));

        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "hakija3", "hakija4", "hakija5");

    }

    @Ignore
    @Test
    @UsingDataSet(locations = "ylitaytto_vaihe.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testYlitaytto() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        final SijoitteluajoWrapper sijoitteluAjo = SijoitteluAjoCreator.createSijoitteluAjo(hakukohteet, Collections.<Valintatulos>newArrayList());
        sijoitteluAjo.setKaikkiKohteetSijoittelussa(LocalDateTime.now().plusDays(10));
        SijoitteluAlgorithm s = SijoitteluAlgorithm.sijoittele(sijoitteluAjo);

        System.out.println(PrintHelper.tulostaSijoittelu(s));

//        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "hakija1", "hakija3", "hakija4", "hakija5");

    }

    @Test
    @UsingDataSet(locations = "vain_ryhmaan_kuuluvat_hyvaksytaan.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testSijoitteluVainHakijaryhmaanKuuluvatVoivatTullaHyvaksytyksi() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());

        SijoitteluAlgorithm s = SijoitteluAlgorithm.sijoittele(hakukohteet, Collections.<Valintatulos>newArrayList());

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "hakemus1", "hakemus2");
        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(1), "hakemus3");


    }

    @Test
    @UsingDataSet(locations = "toisensa_pois_sulkevat_hakijaryhmat.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testSijoitteluToisensaPoisSulkevatRyhmat() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());

        SijoitteluAlgorithm s = SijoitteluAlgorithm.sijoittele(hakukohteet, Collections.<Valintatulos>newArrayList());

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        List<Hakemus> hyvaksytyt = hakukohteet.get(0)
                .getValintatapajonot()
                .stream()
                .flatMap(v -> v.getHakemukset().stream())
                .filter(hakemus -> hakemus.getTila().equals(HakemuksenTila.HYVAKSYTTY)).collect(Collectors.toList());

        Assert.assertEquals(0, hyvaksytyt.size());


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