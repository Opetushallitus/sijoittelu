package fi.vm.sade.sijoittelu;

import fi.vm.sade.testing.AbstractIntegrationTest;
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
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintalaskenta.tulos.service.impl.ValintatietoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ActiveProfiles("test")
public class VastaanottoTest extends AbstractIntegrationTest {

    @Autowired
    private ValintatietoService valintatietoService;

    @Autowired
    private ApplicationContext applicationContext; // Required by the @Rule below

	@Test
  @Sql("vastaanotot.sql")
	public void testVastaanotot() {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());
        hakukohteet.get(0).getValintatapajonot().get(0).setAloituspaikat(0);

        Valintatulos sitova = new Valintatulos();
        sitova.setHakemusOid("oid1", "");
        sitova.setHakijaOid("oid", "");
        sitova.setHakukohdeOid("hakukohde1", "");
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

        System.out.println(PrintHelper.tulostaSijoittelu(SijoitteluAlgorithmUtil.sijoittele(createSijoitteluajoWrapper(hakukohteet, Collections.emptyList()))));

        hakukohteet.stream().flatMap(hk -> hk.getValintatapajonot().stream()).forEach(jono -> jono.setAloituspaikat(3));
        List<Valintatulos> valintatulokset = Arrays.asList(sitova, ehdollinen, perunut, ehdollinenPidettava, ehdollinenPoistettava);
        final SijoitteluajoWrapper sijoitteluAjo = createSijoitteluajoWrapper(hakukohteet, valintatulokset);

        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(sijoitteluAjo);

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        Assertions.assertEquals(6, sijoitteluAjo.getMuuttuneetValintatulokset().size());

        hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
           if(hak.getHakemusOid().equals("oid1")) {
               Assertions.assertEquals(hak.getTila(), HakemuksenTila.VARASIJALTA_HYVAKSYTTY);
           } else if(hak.getHakemusOid().equals("oid2")) {
               Assertions.assertEquals(hak.getTila(), HakemuksenTila.VARASIJALTA_HYVAKSYTTY);
           } else if(hak.getHakemusOid().equals("oid3")) {
               Assertions.assertEquals(hak.getTila(), HakemuksenTila.VARASIJALTA_HYVAKSYTTY);
           } else {
               Assertions.assertEquals(hak.getTila(), HakemuksenTila.VARASIJALTA_HYVAKSYTTY);
           }
        });

        hakukohteet.get(1).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("oid1")) {
                Assertions.assertEquals(hak.getTila(), HakemuksenTila.PERUUNTUNUT);
            } else if(hak.getHakemusOid().equals("oid2")) {
                Assertions.assertEquals(hak.getTila(), HakemuksenTila.PERUUNTUNUT);
            } else if(hak.getHakemusOid().equals("oid3")) {
                Assertions.assertEquals(hak.getTila(), HakemuksenTila.PERUUNTUNUT);
            } else {
                Assertions.assertEquals(hak.getTila(), HakemuksenTila.PERUUNTUNUT);
            }
        });

        hakukohteet.get(2).getValintatapajonot().get(0).getHakemukset().forEach(hak -> {
            if(hak.getHakemusOid().equals("oid1")) {
                Assertions.assertEquals(hak.getTila(), HakemuksenTila.PERUUNTUNUT);
            } else if(hak.getHakemusOid().equals("oid2")) {
                Assertions.assertEquals(hak.getTila(), HakemuksenTila.PERUUNTUNUT);
            } else if(hak.getHakemusOid().equals("oid3")) {
                Assertions.assertEquals(hak.getTila(), HakemuksenTila.PERUUNTUNUT);
            } else {
                Assertions.assertEquals(hak.getTila(), HakemuksenTila.PERUUNTUNUT);
            }
        });


	}

    @Test
    @Sql("vastaanotot_perunut_kaikille_jonoille.sql")
    public void testPeruutettuVastaanottoKaikilleJonoille() {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");
        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());

        SijoitteluajoWrapper sijoitteluAjo = createSijoitteluajoWrapper(hakukohteet, Collections.emptyList());
        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(sijoitteluAjo);
        System.out.println(PrintHelper.tulostaSijoittelu(s));


        Valintatulos jono2Tulos = new Valintatulos();
        jono2Tulos.setHakemusOid("1.2.246.562.11.00001090792", "");
        jono2Tulos.setHakijaOid("1.2.246.562.24.45661259022", "");
        jono2Tulos.setHakukohdeOid("1.2.246.562.20.18895322503", "");
        jono2Tulos.setHakuOid("1.2.246.562.29.173465377510", "");
        jono2Tulos.setHakutoive(4, "");
        jono2Tulos.setHyvaksyttyVarasijalta(false, "");
        jono2Tulos.setJulkaistavissa(true, "");
        jono2Tulos.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY, "");
        jono2Tulos.setTila(ValintatuloksenTila.PERUUTETTU, "");
        jono2Tulos.setValintatapajonoOid("jono1", "");

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
        jono1Tulos.setValintatapajonoOid("jono2", "");

        sijoitteluAjo = createSijoitteluajoWrapper(hakukohteet, Arrays.asList(jono1Tulos, jono2Tulos));

        s = SijoitteluAlgorithmUtil.sijoittele(sijoitteluAjo);

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        assertoi(hakukohteet.get(0).getValintatapajonot()
                .get(0), "1.2.246.562.11.00001090792", HakemuksenTila.PERUUTETTU);
        assertoi(hakukohteet.get(0).getValintatapajonot()
                .get(1), "1.2.246.562.11.00001090792", HakemuksenTila.PERUUTETTU);

    }

    @Test
    @Sql("vastaanotot_perunut_kaikille_jonoille.sql")
    public void testEiVastaanottanutMaaraaikanaVastaanottoKaikilleJonoille() {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");
        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());

        SijoitteluajoWrapper sijoitteluAjo = createSijoitteluajoWrapper(hakukohteet, Collections.emptyList());

        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(sijoitteluAjo);

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        Valintatulos jono2Tulos = new Valintatulos();
        jono2Tulos.setHakemusOid("1.2.246.562.11.00001090792", "");
        jono2Tulos.setHakijaOid("1.2.246.562.24.45661259022", "");
        jono2Tulos.setHakukohdeOid("1.2.246.562.20.18895322503", "");
        jono2Tulos.setHakuOid("1.2.246.562.29.173465377510", "");
        jono2Tulos.setHakutoive(4, "");
        jono2Tulos.setHyvaksyttyVarasijalta(false, "");
        jono2Tulos.setJulkaistavissa(true, "");
        jono2Tulos.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY, "");
        jono2Tulos.setTila(ValintatuloksenTila.EI_VASTAANOTETTU_MAARA_AIKANA, "");
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

        sijoitteluAjo = createSijoitteluajoWrapper(hakukohteet, Arrays.asList(jono1Tulos, jono2Tulos));

        s = SijoitteluAlgorithmUtil.sijoittele(sijoitteluAjo);

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        assertoi(hakukohteet.get(0).getValintatapajonot()
                .get(0), "1.2.246.562.11.00001090792", HakemuksenTila.PERUNUT);
        assertoi(hakukohteet.get(0).getValintatapajonot()
                .get(1), "1.2.246.562.11.00001090792", HakemuksenTila.PERUNUT);

    }

    @Test
    @Sql("vastaanotot_perunut_kaikille_jonoille.sql")
    public void testPerunutVastaanottoKaikilleJonoille() {

        HakuDTO haku = valintatietoService.haeValintatiedot("1.2.246.562.29.173465377510");
        List<Hakukohde> hakukohteet = haku.getHakukohteet().parallelStream().map(DomainConverter::convertToHakukohde).collect(Collectors.toList());

        SijoitteluajoWrapper sijoitteluAjo = createSijoitteluajoWrapper(hakukohteet, Collections.emptyList());

        SijoittelunTila s = SijoitteluAlgorithmUtil.sijoittele(sijoitteluAjo);

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        Valintatulos jono2Tulos = new Valintatulos();
        jono2Tulos.setHakemusOid("1.2.246.562.11.00001090792", "");
        jono2Tulos.setHakijaOid("1.2.246.562.24.45661259022", "");
        jono2Tulos.setHakukohdeOid("1.2.246.562.20.18895322503", "");
        jono2Tulos.setHakuOid("1.2.246.562.29.173465377510", "");
        jono2Tulos.setHakutoive(4, "");
        jono2Tulos.setHyvaksyttyVarasijalta(false, "");
        jono2Tulos.setJulkaistavissa(true, "");
        jono2Tulos.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY, "");
        jono2Tulos.setTila(ValintatuloksenTila.PERUNUT, "");
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

        sijoitteluAjo = createSijoitteluajoWrapper(hakukohteet, Arrays.asList(jono1Tulos, jono2Tulos));

        s = SijoitteluAlgorithmUtil.sijoittele(sijoitteluAjo);

        System.out.println(PrintHelper.tulostaSijoittelu(s));

        assertoi(hakukohteet.get(0).getValintatapajonot()
                .get(0), "1.2.246.562.11.00001090792", HakemuksenTila.PERUNUT);
        assertoi(hakukohteet.get(0).getValintatapajonot()
                .get(1), "1.2.246.562.11.00001090792", HakemuksenTila.PERUNUT);

    }

    private SijoitteluajoWrapper createSijoitteluajoWrapper(List<Hakukohde> hakukohteet, List<Valintatulos> valintatulokset) {
        SijoitteluajoWrapper sijoitteluajoWrapper = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluConfiguration(), new SijoitteluAjo(), tallennaEdellisetTilat(hakukohteet), Collections.emptyMap());
        sijoitteluajoWrapper.paivitaVastaanottojenVaikutusHakemustenTiloihin(valintatulokset, Collections.emptyMap());
        return sijoitteluajoWrapper;
    }

    private static void assertoi(Valintatapajono valintatapajono, String oid, HakemuksenTila tila) {
        Hakemus check = null;
        for (Hakemus hakemus : valintatapajono.getHakemukset()) {
            if (hakemus.getHakemusOid().equals(oid)) {
                check = hakemus;
            }
        }
        Assertions.assertEquals(tila, check.getTila());
    }

    private List<Hakukohde> tallennaEdellisetTilat(List<Hakukohde> hakukohteet) {
        hakukohteet.forEach(hk ->
            hk.getValintatapajonot().forEach(jono -> {
                jono.getHakemukset().forEach(h -> h.setEdellinenTila(h.getTila()));
            }));
        return hakukohteet;
    }

}
