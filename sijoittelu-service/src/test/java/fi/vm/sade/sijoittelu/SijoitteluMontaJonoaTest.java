package fi.vm.sade.sijoittelu;

import com.google.common.collect.Lists;

import fi.vm.sade.testing.AbstractIntegrationTest;
import fi.vm.sade.testing.TestConfigurationWithMocks;
import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.PrintHelper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluConfiguration;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluajoWrapperFactory;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoittelunTila;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.SijoitteluAlgorithmUtil;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.IlmoittautumisTila;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.TilaHistoria;
import fi.vm.sade.sijoittelu.domain.TilanKuvaukset;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintalaskenta.tulos.service.impl.ValintatietoService;
import org.apache.commons.lang3.time.DateUtils;
import org.joda.time.DateTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class SijoitteluMontaJonoaTest extends AbstractIntegrationTest {

    @Autowired
    private ValintatietoService valintatietoService;

    @Autowired
    private ApplicationContext applicationContext;

	@Test
  @Sql("monta_jonoa.sql")
	public void testSijoitteluMontaJonoa() {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
      hakukohteet.get(0).getValintatapajonot().stream()
        .filter(jono -> jono.getOid().equals("oid1")).findFirst().orElseThrow().setAloituspaikat(0);

        SijoitteluAlgorithmUtil.sijoittele(createSijoitteluAjoWrapper(tallennaEdellisetTilat(hakukohteet), Collections.emptyList()));

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

        hakukohteet.get(0).getValintatapajonot().stream()
            .filter(jono -> jono.getOid().equals("oid1")).findFirst().orElseThrow().setAloituspaikat(1);
        final SijoitteluajoWrapper sijoitteluAjo = createSijoitteluAjoWrapper(tallennaEdellisetTilat(hakukohteet), Arrays.asList(tulos));
        final SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(sijoitteluAjo);

        Valintatapajono jono1 = hakukohteet.get(0).getValintatapajonot().stream()
          .filter(jono -> jono.getOid().equals("oid1")).findFirst().orElseThrow();

      Valintatapajono jono2 = hakukohteet.get(0).getValintatapajonot().stream()
        .filter(jono -> jono.getOid().equals("oid2")).findFirst().orElseThrow();

        assertoiAinoastaanValittu(jono1, "1.2.246.562.11.00001068863");
        assertoiAinoastaanValittu(jono2, "1.2.246.562.11.00001090792");

        jono1.getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001090792")) {
                Assertions.assertEquals(hak.getTila(), HakemuksenTila.VARALLA);
            }
        });

        jono2.getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001068863")) {
                Assertions.assertEquals(hak.getTila(), HakemuksenTila.PERUUNTUNUT);
            }
        });
        System.out.println(PrintHelper.tulostaSijoittelu(s));
        List<Valintatulos> list = sijoitteluAjo.getMuuttuneetValintatulokset();
        Assertions.assertEquals(2, list.size());
        list.forEach(v -> {
            if(v.getValintatapajonoOid().equals("oid1")) {
                Assertions.assertEquals(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, v.getTila());
                Assertions.assertEquals(IlmoittautumisTila.LASNA_KOKO_LUKUVUOSI, v.getIlmoittautumisTila());
            }
            if(v.getValintatapajonoOid().equals("oid2")) {
                Assertions.assertEquals(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, v.getTila());
                Assertions.assertEquals(IlmoittautumisTila.LASNA_KOKO_LUKUVUOSI, v.getIlmoittautumisTila());
            }
        });


    }

    @Test
    @Sql("monta_jonoa.sql")
    public void testSijoitteluMontaJonoaEiValintatulosta() {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(hakukohteet, new ArrayList<>(), Collections.emptyMap());

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "1.2.246.562.11.00001068863");
        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(1), "1.2.246.562.11.00001090792");

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001090792")) {
                Assertions.assertEquals(hak.getTila(), HakemuksenTila.VARALLA);
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(1).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001068863")) {
                Assertions.assertEquals(hak.getTila(), HakemuksenTila.PERUUNTUNUT);
            }
        });

    }

    @Test
    @Sql("monta_jonoa.sql")
    public void testPoissaolevaTaytto() {

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
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(hakukohteet, Collections.singletonList(tulos), Collections.emptyMap());

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "1.2.246.562.11.00001068863");
        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(1), "1.2.246.562.11.00001090792");

    }

    @Test
    @Sql("monta_jonoa.sql")
    public void testPerunut() {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());

        System.out.println(PrintHelper.tulostaSijoittelu(SijoitteluAlgorithmUtil.sijoittele(hakukohteet, Collections.emptyList(), Collections.emptyMap())));

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
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(tallennaEdellisetTilat(hakukohteet), Collections.singletonList(tulos), Collections.emptyMap());

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "1.2.246.562.11.00001090792");
        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(1), "1.2.246.562.11.00001067411");

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001068863")) {
                Assertions.assertEquals(hak.getTila(), HakemuksenTila.PERUNUT);
                TilaHistoria th = new TilaHistoria(HakemuksenTila.HYVAKSYTTY);
                Date date = DateUtils.addDays(new Date(), -2);
                th.setLuotu(date);
                hak.getTilaHistoria().add(th);
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(1).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001068863")) {
                Assertions.assertEquals(hak.getTila(), HakemuksenTila.PERUNUT);
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
        s = SijoitteluAlgorithmUtil.sijoittele(tallennaEdellisetTilat(hakukohteet), Arrays.asList(tulos, tulos2), Collections.emptyMap());

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(0), "1.2.246.562.11.00001090792", "1.2.246.562.11.00001068863");
        assertoiAinoastaanValittu(hakukohteet.get(0).getValintatapajonot().get(1), "1.2.246.562.11.00001067411");

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001068863")) {
                Assertions.assertEquals(hak.getTila(), HakemuksenTila.HYVAKSYTTY);
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(1).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001068863")) {
                Assertions.assertEquals(hak.getTila(), HakemuksenTila.PERUUNTUNUT);
            }
        });

    }

    @Test
    @Sql("ei_varasijatayttoa.sql")
    public void testEiVarasijatayttoa() {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        final SijoitteluajoWrapper sijoitteluAjo = createSijoitteluAjoWrapper(tallennaEdellisetTilat(hakukohteet), Collections.emptyList());
        sijoitteluAjo.setKKHaku(true);
        sijoitteluAjo.setVarasijaSaannotAstuvatVoimaan(LocalDateTime.now().plusDays(1));
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(sijoitteluAjo);

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001068863")) {
                Assertions.assertEquals(hak.getTila(), HakemuksenTila.HYVAKSYTTY);
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001090792")) {
                Assertions.assertEquals(hak.getTila(), HakemuksenTila.VARALLA);
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001067411")) {
                Assertions.assertEquals(hak.getTila(), HakemuksenTila.VARALLA);
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("hylatty")) {
                Assertions.assertEquals(hak.getTila(), HakemuksenTila.HYLATTY);
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
        SijoitteluajoWrapper sijoitteluajoWrapper = createSijoitteluAjoWrapper(tallennaEdellisetTilat(hakukohteet), Arrays.asList(tulos));
        sijoitteluajoWrapper.setVarasijaSaannotAstuvatVoimaan(LocalDateTime.now().plusDays(1));
        sijoitteluajoWrapper.setKKHaku(true);
        s = SijoitteluAlgorithmUtil.sijoittele(sijoitteluajoWrapper);

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001068863")) {
                Assertions.assertEquals(hak.getTila(), HakemuksenTila.PERUNUT);
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001090792")) {
                Assertions.assertEquals(hak.getTila(), HakemuksenTila.HYVAKSYTTY);
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001067411")) {
                Assertions.assertEquals(hak.getTila(), HakemuksenTila.VARALLA);
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("hylatty")) {
                Assertions.assertEquals(hak.getTila(), HakemuksenTila.HYLATTY);
            }
        });
        s = SijoitteluAlgorithmUtil.sijoittele(tallennaEdellisetTilat(hakukohteet), Arrays.asList(tulos, tulos2), Collections.emptyMap());

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001068863")) {
                Assertions.assertEquals(hak.getTila(), HakemuksenTila.PERUNUT);
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001090792")) {
                Assertions.assertEquals(hak.getTila(), HakemuksenTila.HYVAKSYTTY);
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001067411")) {
                Assertions.assertEquals(hak.getTila(), HakemuksenTila.PERUUNTUNUT);
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("hylatty")) {
                Assertions.assertEquals(hak.getTila(), HakemuksenTila.HYLATTY);
            }
        });
        sijoitteluajoWrapper = createSijoitteluAjoWrapper(tallennaEdellisetTilat(hakukohteet), Arrays.asList(tulos, tulos2));
        sijoitteluajoWrapper.setKKHaku(true);
        s = SijoitteluAlgorithmUtil.sijoittele(sijoitteluajoWrapper);

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001068863")) {
                Assertions.assertEquals(hak.getTila(), HakemuksenTila.PERUNUT);
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001090792")) {
                Assertions.assertEquals(hak.getTila(), HakemuksenTila.HYVAKSYTTY);
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001067411")) {
                Assertions.assertEquals(hak.getTila(), HakemuksenTila.PERUUNTUNUT);
                Assertions.assertEquals(hak.getTilanKuvaukset().get("FI"), TilanKuvaukset.peruuntunutAloituspaikatTaynna.get("FI"));
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("hylatty")) {
                Assertions.assertEquals(hak.getTila(), HakemuksenTila.HYLATTY);
            }
        });

    }

    @Test
    @Sql("varasijat_rajattu.sql")
    public void testVarasijatRajattu() {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");
        haku.getHakukohteet().get(0).getValinnanvaihe().get(0).getValintatapajonot().get(0)
          .setVarasijojaKaytetaanAlkaen(Date.from(LocalDate.of(2019, Month.AUGUST, 26)
            .atStartOfDay(ZoneId.systemDefault()).toInstant()));
        haku.getHakukohteet().get(0).getValinnanvaihe().get(0).getValintatapajonot().get(0).setVarasijat(1);

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(hakukohteet, new ArrayList<>(), Collections.emptyMap());

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001068863")) {
                Assertions.assertEquals(hak.getTila(), HakemuksenTila.HYVAKSYTTY);
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001090792")) {
                Assertions.assertEquals(hak.getTila(), HakemuksenTila.VARALLA);
            }
        });

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001067411")) {
                Assertions.assertEquals(hak.getTila(), HakemuksenTila.PERUUNTUNUT);
                Assertions.assertEquals(hak.getTilanKuvaukset().get("FI"), TilanKuvaukset.peruuntunutEiMahduKasiteltavienVarasijojenMaaraan.get("FI"));
            }
        });

    }

    @Test
    @Sql("ehdolliset_sitoviksi.sql")
    public void testEhdollisetSitoviksi() {

        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());

        final SijoitteluajoWrapper sijoitteluAjo1 = createSijoitteluAjoWrapper(hakukohteet, Collections.emptyList());
        sijoitteluAjo1.setKKHaku(true);
        SijoittelunTila s1 = SijoitteluAlgorithmUtil.sijoittele(sijoitteluAjo1);

        System.out.println(PrintHelper.tulostaSijoittelu(s1));

        Valintatulos tulos1 = createTulos("hakemus2", "hakukohde1", "oid1");
        tulos1.setTila(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT, "");

        Valintatulos tulos2 = createTulos("hakemus3", "hakukohde1", "oid2");
        tulos2.setTila(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT, "");

        final SijoitteluajoWrapper sijoitteluAjo2 = createSijoitteluAjoWrapper(tallennaEdellisetTilat(hakukohteet), Arrays.asList(tulos1, tulos2));
        sijoitteluAjo2.setKKHaku(true);
        sijoitteluAjo2.setVarasijaTayttoPaattyy(LocalDateTime.now().minus(Period.ofDays(10)));
        SijoittelunTila s2 = SijoitteluAlgorithmUtil.sijoittele(sijoitteluAjo2);

        System.out.println(PrintHelper.tulostaSijoittelu(s2));

        List<Valintatulos> muuttuneetValintatulokset = sijoitteluAjo2.getMuuttuneetValintatulokset();

        System.out.println("muuttuneetValintatulokset: " + muuttuneetValintatulokset);

        Assertions.assertEquals(2, muuttuneetValintatulokset.size());
        Assertions.assertEquals(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, muuttuneetValintatulokset.get(0).getTila());
        Assertions.assertEquals(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, muuttuneetValintatulokset.get(1).getTila());

    }

    // Täyttöjonosääntö vaatii speksausta
    @Disabled
    @Test
    //@UsingDataSet(locations = "tayttojono.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testTayttoJono() {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

        haku.getHakukohteet().get(0).getValinnanvaihe().get(0).getValintatapajonot().get(0).setTayttojono("oid2");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(hakukohteet, new ArrayList(), Collections.emptyMap());

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("1.2.246.562.11.00001067411")) {
                Assertions.assertEquals(hak.getTila(), HakemuksenTila.HYVAKSYTTY);
                Assertions.assertEquals(hak.getTilanKuvaukset().get("FI"), TilanKuvaukset.hyvaksyttyTayttojonoSaannolla("Koe").get("FI"));
            }
        });

    }

    @Test
    @Sql("peruuta_alemmat.sql")
    public void testPeruutaAlemmat() {

        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");


        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(hakukohteet, new ArrayList<>(), Collections.emptyMap());

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        hakukohteet.remove(2);
        s = SijoitteluAlgorithmUtil.sijoittele(hakukohteet, new ArrayList<>(), Collections.emptyMap());

        System.out.println(PrintHelper.tulostaSijoittelu(s));

    }

    @Test
    public void testTasasijaArvonta() {

        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");




        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        SijoitteluAlgorithmUtil.sijoittele(hakukohteet, new ArrayList(), Collections.emptyMap());

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
        SijoitteluAlgorithmUtil.sijoittele(hakukohteet, Arrays.asList(tulos11, tulos12, tulos21, tulos22, tulos31, tulos32, tulos41, tulos42, tulos51, tulos52, tulos61, tulos62, tulos71, tulos72), Collections.emptyMap());

        tulos11.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, "");
        tulos11.setIlmoittautumisTila(IlmoittautumisTila.POISSA, "");

        tulos22.setTila(ValintatuloksenTila.PERUNUT, "");
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(hakukohteet, Arrays.asList(tulos11, tulos12, tulos21, tulos22, tulos31, tulos32, tulos41, tulos42, tulos51, tulos52, tulos61, tulos62, tulos71, tulos72), Collections.emptyMap());

        Assertions.assertNotNull(s);
        System.out.println(PrintHelper.tulostaSijoittelu(s));


    }

    private void sijoitteleAndPrintResult(List<Hakukohde> hakukohteet, List<Valintatulos> valintatulokset, boolean isKorkeakoulu) {
        SijoitteluajoWrapper sijoitteluAjo = createSijoitteluAjoWrapper(tallennaEdellisetTilat(hakukohteet), valintatulokset);
        sijoitteluAjo.setKKHaku(isKorkeakoulu);
        SijoittelunTila sijoittelunTila = SijoitteluAlgorithmUtil.sijoittele(sijoitteluAjo);
        System.out.println(PrintHelper.tulostaSijoittelu(sijoittelunTila));
    }

    private void testPoissaOloTaytto(List<IlmoittautumisTila> ilmoittautumiset,
                                     boolean isKorkeakoulu, int expectedHyvaksyttyCount) {
        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream()
                .map(DomainConverter::convertToHakukohde).collect(Collectors.toList());

        List<Valintatulos> valintatulokset = getValintatulosForPoissaoloTest();

        sijoitteleAndPrintResult(hakukohteet, valintatulokset, isKorkeakoulu);

        for (int i = 0; i < ilmoittautumiset.size(); i ++) {
            IlmoittautumisTila ilmoittautuminen = ilmoittautumiset.get(i);
            valintatulokset.get(i).setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, "");
            valintatulokset.get(i).setIlmoittautumisTila(ilmoittautuminen, "");
        }

        sijoitteleAndPrintResult(hakukohteet, valintatulokset, isKorkeakoulu);

        int koko = (int) hakukohteet.get(0).getValintatapajonot().get(0)
                .getHakemukset().stream()
                .filter(hak->hak.getTila() == HakemuksenTila.HYVAKSYTTY || hak.getTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY)
                .count();

        Assertions.assertEquals(expectedHyvaksyttyCount, koko);
    }

    private List<Valintatulos> getValintatulosForPoissaoloTest() {
        List<Valintatulos> valintatulokset = new ArrayList<>();
        for (int i = 1; i <= 10; i ++) {
            valintatulokset.add(createTulos("oid" + i, "hakukohde1", "jono1"));
        }
        return valintatulokset;
    }

    @Test
    @Sql("poissa_oleva_taytto.sql")
    public void testPoissaOloTaytto2() {
        testPoissaOloTaytto(Lists.newArrayList(IlmoittautumisTila.POISSA, IlmoittautumisTila.POISSA), false, 5);
    }

    @Test
    @Sql("poissa_oleva_taytto.sql")
    public void testPoissaOloKevat2AstePaikkaaEiTayteta() {
        testPoissaOloTaytto(Lists.newArrayList(IlmoittautumisTila.POISSA_SYKSY, IlmoittautumisTila.POISSA), false, 5);
    }

    @Test
    @Sql("poissa_oleva_taytto.sql")
    public void testPoissaOloKevatKorkeakouluPaikkaTaytetaan() {
        testPoissaOloTaytto(Lists.newArrayList(IlmoittautumisTila.POISSA_SYKSY, IlmoittautumisTila.POISSA), true, 5);
    }

    @Test
    @Sql("poissa_oleva_taytto3.sql")
    public void testPoissaOloTaytto3() {
        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(tallennaEdellisetTilat(hakukohteet), new ArrayList(), Collections.emptyMap());

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
        s = SijoitteluAlgorithmUtil.sijoittele(tallennaEdellisetTilat(hakukohteet), Arrays.asList(tulos1, tulos2, tulos3, tulos4, tulos6, tulos7, tulos8, tulos9, tulos10, tulos11, tulos21, tulos31, tulos41, tulos61, tulos71, tulos81, tulos91, tulos101), Collections.emptyMap());

        tulos1.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, "");
        tulos1.setIlmoittautumisTila(IlmoittautumisTila.POISSA, "");

        tulos2.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, "");
        tulos2.setIlmoittautumisTila(IlmoittautumisTila.POISSA, "");

        tulos61.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, "");
        tulos71.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, "");
        tulos81.setTila(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, "");
        s = SijoitteluAlgorithmUtil.sijoittele(tallennaEdellisetTilat(hakukohteet), Arrays.asList(tulos1, tulos2, tulos3, tulos4, tulos6, tulos7, tulos8, tulos9, tulos10, tulos11, tulos21, tulos31, tulos41, tulos61, tulos71, tulos81, tulos91, tulos101), Collections.emptyMap());

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        int koko = (int) hakukohteet.get(0).getValintatapajonot().get(0)
                .getHakemukset().stream()
                .filter(hak->hak.getTila() == HakemuksenTila.HYVAKSYTTY || hak.getTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY)
                .count();

        Assertions.assertEquals(4, koko);


    }

    @Test
    @Sql("peruuntunut_taytto.sql")
    public void testPeruuntunutTaytto() {

        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");
        haku.getHakukohteet().get(0).getValinnanvaihe().get(0).getValintatapajonot().get(0)
          .setVarasijojaKaytetaanAlkaen(Date.from(LocalDate.of(2019, Month.AUGUST, 26)
            .atStartOfDay(ZoneId.systemDefault()).toInstant()));

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(hakukohteet, new ArrayList<>(), Collections.emptyMap());

        Valintatulos tulos1 = createTulos("oid1", "hakukohde1", "jono1");
        Valintatulos tulos2 = createTulos("oid2", "hakukohde1", "jono1");

        Valintatulos tulos3 = createTulos("oid3", "hakukohde1", "jono1");
        Valintatulos tulos4 = createTulos("oid4", "hakukohde1", "jono1");

        Valintatulos tulos6 = createTulos("oid6", "hakukohde1", "jono1");

        Valintatulos tulos7 = createTulos("oid7", "hakukohde1", "jono1");
        Valintatulos tulos8 = createTulos("oid8", "hakukohde1", "jono1");

        Valintatulos tulos9 = createTulos("oid9", "hakukohde1", "jono1");
        Valintatulos tulos10 = createTulos("oid10", "hakukohde1", "jono1");
        SijoitteluAlgorithmUtil.sijoittele(hakukohteet, Arrays.asList(tulos1, tulos2, tulos3, tulos4, tulos6, tulos7, tulos8, tulos9, tulos10), Collections.emptyMap());
        final SijoitteluajoWrapper sijoitteluAjo = createSijoitteluAjoWrapper(hakukohteet, Arrays.asList(tulos1, tulos2, tulos3, tulos4, tulos6, tulos7, tulos8, tulos9, tulos10));
        sijoitteluAjo.getHakukohteet().get(0).getValintatapajonot().get(0).getHakemukset().stream().filter(hak->hak.getHakemus().getHakemusOid().equals("oid1") || hak.getHakemus().getHakemusOid().equals("oid2")).forEach(hak -> {
            hak.setTilaVoidaanVaihtaa(false);
            hak.getHakemus().setTila(HakemuksenTila.PERUUNTUNUT);
        });
        SijoitteluAlgorithmUtil.sijoittele(sijoitteluAjo);

        int koko = (int) hakukohteet.get(0).getValintatapajonot().get(0)
                .getHakemukset().stream()
                .filter(hak->hak.getTila() == HakemuksenTila.HYVAKSYTTY || hak.getTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY)
                .count();

        Assertions.assertEquals(koko, 4);

    }

    private SijoitteluajoWrapper createSijoitteluAjoWrapper(List<Hakukohde> hakukohdes, List<Valintatulos> valintatulosList) {
        SijoitteluajoWrapper sijoitteluajoWrapper = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluConfiguration(), new SijoitteluAjo(), hakukohdes, Collections.emptyMap());
        sijoitteluajoWrapper.paivitaVastaanottojenVaikutusHakemustenTiloihin(valintatulosList, Collections.emptyMap());
        return sijoitteluajoWrapper;
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

    public static void assertoiAinoastaanValittu(Valintatapajono h, String... oids) {
        List<String> wanted = Arrays.asList(oids);
        List<String> actual = new ArrayList<>();
        for (Hakemus hakemus : h.getHakemukset()) {
            if (hakemus.getTila() == HakemuksenTila.HYVAKSYTTY || hakemus.getTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY) {
                actual.add(hakemus.getHakemusOid());
            }
        }
        Assertions.assertTrue(actual.containsAll(wanted), "Actual result does not contain all wanted approved OIDs");
        Assertions.assertTrue(wanted.containsAll(actual), "Wanted result contains more approved OIDs than actual");
    }

    private List<Hakukohde> tallennaEdellisetTilat(List<Hakukohde> hakukohteet) {
        hakukohteet.forEach(hk ->
            hk.getValintatapajonot().forEach(jono -> {
                jono.getHakemukset().forEach(h ->
                    h.setEdellinenTila(h.getTila()));
            }));
        return hakukohteet;
    }

}
