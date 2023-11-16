package fi.vm.sade.sijoittelu;

import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.PrintHelper.tulostaSijoittelu;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

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
import fi.vm.sade.testing.AbstractIntegrationTest;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintalaskenta.domain.valinta.Hakijaryhma;
import fi.vm.sade.valintalaskenta.domain.valinta.Jonosija;
import fi.vm.sade.valintalaskenta.tulos.service.impl.ValintatietoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import fi.vm.sade.valintalaskenta.domain.testdata.TestEntityDataUtil;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import java.time.*;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class HakijaryhmaTest extends AbstractIntegrationTest {
  private static final Logger LOG = LoggerFactory.getLogger(HakijaryhmaTest.class);
  @Autowired
  private ValintatietoService valintatietoService;

  @Autowired
  private ApplicationContext applicationContext;

  @Test
  @Sql("vaasan_yliopisto_valinnan_vaiheet.sql")
  public void testSijoitteluHakijaryhmalla() {

    HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

    List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
    SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(hakukohteet, Collections.emptyList(), Collections.emptyMap());

    System.out.println(tulostaSijoittelu(s));

    assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "1.2.246.562.11.00001067411", "1.2.246.562.11.00001068863");


  }

  @Test
  @Sql("vaasan_yliopisto_valinnan_vaiheet_kaksi_hakijaryhmaa.sql")
  public void testSijoitteluKaksiHakijaryhmaa() {

    HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

    List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
    SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(hakukohteet, Collections.emptyList(), Collections.emptyMap());

    System.out.println(tulostaSijoittelu(s));

    assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "1.2.246.562.11.00001067411", "1.2.246.562.11.00001090792");


  }

  @Test
  @Sql("vaasan_yliopisto_valinnan_vaiheet_kaksi_hakijaryhmaa_toinen_eri_jonossa.sql")
  public void testSijoitteluKaksiHakijaryhmaaToisessaEriJono() {

    HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

    List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
    SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(hakukohteet, Collections.emptyList(), Collections.emptyMap());

    System.out.println(tulostaSijoittelu(s));
    Assertions.assertEquals(2, hakukohteet.get(0).getHakijaryhmat().size());
    assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "1.2.246.562.11.00001068863", "1.2.246.562.11.00001067411");
  }

  @Test
  @Sql("vaasan_yliopisto_valinnan_vaiheet_ei_hakijaryhmaa.sql")
  public void testSijoitteluEiHakijaryhma() {

    HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

    List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
    SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(hakukohteet, Collections.emptyList(), Collections.emptyMap());

    System.out.println(tulostaSijoittelu(s));

    Assertions.assertEquals(0, hakukohteet.get(0).getHakijaryhmat().size());
    assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "1.2.246.562.11.00001068863", "1.2.246.562.11.00001090792", "1.2.246.562.11.00001067411");
  }

  @Test
  @Disabled
  //@UsingDataSet(locations = "alitaytto_simple_case.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
  // Korjaa tämä kun ikiluuppi on korjattu
  public void testAlitayttoRekursio() {

    HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

    List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
    final SijoitteluajoWrapper sijoitteluAjo = createSijoitteluajoWrapper(hakukohteet);
    SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(sijoitteluAjo);
    sijoitteluAjo.setKaikkiKohteetSijoittelussa(LocalDateTime.now().plusDays(10));

    LOG.info("\r\n{}", tulostaSijoittelu(s));
    assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "hakija1");

  }


  @Test
  @Sql("ylitaytto_simple_case.sql")
  public void testYlitayttoRekursio() {

    HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

    List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());

    final SijoitteluajoWrapper sijoitteluAjo = createSijoitteluajoWrapper(hakukohteet);
    sijoitteluAjo.setKaikkiKohteetSijoittelussa(LocalDateTime.now().plusDays(10));
    SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(sijoitteluAjo);

    LOG.info("\r\n{}", tulostaSijoittelu(s));

    assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "hakemus3", "hakemus4", "hakemus5");

  }

  @Disabled
  @Test
  //@UsingDataSet(locations = "ylitaytto_vaihe.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
  public void testYlitaytto() {

    HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

    List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
    final SijoitteluajoWrapper sijoitteluAjo = createSijoitteluajoWrapper(hakukohteet);
    sijoitteluAjo.setKaikkiKohteetSijoittelussa(LocalDateTime.now().plusDays(10));
    SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(sijoitteluAjo);

    System.out.println(tulostaSijoittelu(s));

//        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "hakija1", "hakija3", "hakija4", "hakija5");

  }

  @Test
  @Sql(scripts = "vain_ryhmaan_kuuluvat_hyvaksytaan.sql", config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED))
  public void testSijoitteluVainHakijaryhmaanKuuluvatVoivatTullaHyvaksytyksi() {

    HakuDTO haku = valintatietoService.haeValintatiedot("haku1");

    List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
    SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(hakukohteet, Collections.emptyList(), Collections.emptyMap());

    System.out.println(tulostaSijoittelu(s));

    assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "hakemus1", "hakemus2");
    assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(1), "hakemus3");


  }

  @Test
  @Sql(scripts = "toisensa_pois_sulkevat_hakijaryhmat.sql", config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED))
  public void testSijoitteluToisensaPoisSulkevatRyhmat() {
    Hakijaryhma ryhma = new Hakijaryhma();
    ryhma.hakijaryhmaOid = "ryhma1";
    ryhma.hakukohdeOid = "hakukohde1";

    HakuDTO haku = valintatietoService.haeValintatiedot("haku1");

    List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
    Assertions.assertThrows(IllegalStateException.class, () -> SijoitteluAlgorithmUtil.sijoittele(hakukohteet, Collections.emptyList(), Collections.emptyMap()));
  }

  @Test
  @Sql("hakijaryhma_varasijasaannot_paattyneet.sql")
  //@UsingDataSet(locations = "hakijaryhma_varasijasaannot_paattyneet.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
  public void testSijoitteluVarasijaSaannotPaattyneet() {

    HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");
    haku.getHakukohteet().get(0).getValinnanvaihe().get(0).getValintatapajonot().stream()
      .filter(v -> v.getNimi().equals("valintatapajono1")).forEach(v ->
        v.setVarasijojaTaytetaanAsti(new Date(LocalDate.of(2016, Month.APRIL, 23).toEpochDay())));
    List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());

    final SijoitteluajoWrapper sijoitteluAjo = createSijoitteluajoWrapper(hakukohteet);
    sijoitteluAjo.setKaikkiKohteetSijoittelussa(LocalDateTime.now().plusDays(10));

        /*
          Luodaan alkutilanne:

          valintatapajono1: A (HYVÄKSYTTY), B (VARALLA)
          valintatapajono2: C (HYVÄKSYTTY), D (VARALLA)
         */
    SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(sijoitteluAjo);
    System.out.println(tulostaSijoittelu(s));

    assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "A");
    assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(1), "C");

        /*
          Muokataan kiintiöitä, että B ja D uudelleen sijoitellaan. Lisäksi
          lisätään ensimmäiselle valintatapajonolle varasijatäyttö päättyneeksi,
          joten D tulisi ainoastaan hyväksyä tässä sijoittelussa.
         */
    s.sijoitteluAjo.getHakukohteet().stream()
      .flatMap(hk -> hk.getHakijaryhmaWrappers().stream())
      .map(HakijaryhmaWrapper::getHakijaryhma)
      .forEach(hk -> hk.setKiintio(4));
    s.sijoitteluAjo.getHakukohteet().forEach(hk -> {
      List<Valintatapajono> valintatapajonot = hk.getValintatapajonot().stream().map(ValintatapajonoWrapper::getValintatapajono).toList();
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

  public static void assertoiAinoastaanValittu(Valintatapajono h, String... oids) {
    List<String> wanted = Arrays.asList(oids);
    List<String> actual = new ArrayList<>();
    for (Hakemus hakemus : h.getHakemukset()) {
      if (hakemus.getTila() == HakemuksenTila.HYVAKSYTTY) {
        actual.add(hakemus.getHakemusOid());
      }
    }
    Assertions.assertTrue(actual.containsAll(wanted), "Actual result does not contain all wanted approved OIDs");
    Assertions.assertTrue(wanted.containsAll(actual), "Wanted result contains more approved OIDs than actual");
  }

  private SijoitteluajoWrapper createSijoitteluajoWrapper(List<Hakukohde> hakukohteet) {
    SijoitteluajoWrapper sijoitteluajoWrapper = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluConfiguration(), new SijoitteluAjo(), hakukohteet, Collections.emptyMap());
    sijoitteluajoWrapper.paivitaVastaanottojenVaikutusHakemustenTiloihin(Collections.emptyList(), Collections.emptyMap());
    return sijoitteluajoWrapper;
  }
}
