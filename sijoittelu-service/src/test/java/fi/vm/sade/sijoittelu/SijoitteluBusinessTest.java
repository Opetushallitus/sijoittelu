package fi.vm.sade.sijoittelu;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.lordofthejars.nosqlunit.annotation.UsingDataSet;
import com.lordofthejars.nosqlunit.core.LoadStrategyEnum;
import com.lordofthejars.nosqlunit.mongodb.MongoDbRule;
import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriDTO;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.sijoittelu.laskenta.service.business.ValintatulosWithVastaanotto;
import fi.vm.sade.sijoittelu.laskenta.service.it.TarjontaIntegrationService;
import fi.vm.sade.sijoittelu.tulos.dao.HakukohdeDao;
import fi.vm.sade.sijoittelu.tulos.dao.SijoitteluDao;
import fi.vm.sade.sijoittelu.tulos.dao.ValintatulosDao;

import fi.vm.sade.valintalaskenta.domain.dto.ValintatapajonoDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintalaskenta.tulos.service.impl.ValintatietoService;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.ApplicationContext;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.collect.Sets.newHashSet;
import static com.lordofthejars.nosqlunit.mongodb.MongoDbRule.MongoDbRuleBuilder.newMongoDbRule;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * User: wuoti Date: 11.11.2013 Time: 15.13
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-sijoittelu-batch-mongo.xml" })
public class SijoitteluBusinessTest {

    private static final Logger LOG = LoggerFactory.getLogger(SijoitteluBusinessTest.class);

    @Autowired
    private ValintatulosDao valintatulosDao;

    @Autowired
    private SijoitteluDao sijoitteluDao;

    @Autowired
    private HakukohdeDao hakukohdeDao;

    @Autowired
    private SijoitteluBusinessService sijoitteluService;

    @Autowired
    private TarjontaIntegrationService tarjontaIntegrationService;

    private ValintatulosWithVastaanotto valintatulosWithVastaanotto;

    @Autowired
    private ValintatietoService valintatietoService;

    @Autowired
    private ApplicationContext applicationContext;

    @Rule
    public MongoDbRule mongoDbRule = newMongoDbRule().defaultSpringMongoDb("test");

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void setup() {
        tarjontaIntegrationService = mock(TarjontaIntegrationService.class);
        valintatulosWithVastaanotto = mock(ValintatulosWithVastaanotto.class);

        ReflectionTestUtils.setField(sijoitteluService,
                "tarjontaIntegrationService",
                tarjontaIntegrationService);
        ReflectionTestUtils.setField(sijoitteluService,
                "valintatulosWithVastaanotto",
                valintatulosWithVastaanotto);

        fi.vm.sade.sijoittelu.laskenta.external.resource.dto.HakuDTO hakuDto = new fi.vm.sade.sijoittelu.laskenta.external.resource.dto.HakuDTO();
        hakuDto.setKohdejoukkoUri("haunkohdejoukko_11#1");

        when(tarjontaIntegrationService.getHakuByHakuOid(anyString())).thenReturn(hakuDto);

        String json = "{ \"target\": \"1.2.246.562.29.173465377510\", \"__modified__\": 1416309364472, \"__modifiedBy__\": \"1.2.246.562.24.47840234552\", \"PH_TJT\": {\"date\": null}, \"PH_HKLPT\": {\"date\": null}, \"PH_HKMT\": {\"date\": null}, \"PH_KKM\": { \"dateStart\": null, \"dateEnd\": null }, \"PH_HVVPTP\": {\"date\": null}, \"PH_KTT\": { \"dateStart\": null, \"dateEnd\": null }, \"PH_OLVVPKE\": { \"dateStart\": null, \"dateEnd\": null }, \"PH_VLS\": { \"dateStart\": null, \"dateEnd\": null }, \"PH_SS\": { \"dateStart\": null, \"dateEnd\": null }, \"PH_JKLIP\": {\"date\": null}, \"PH_HKP\": {\"date\": 14168663953898}, \"PH_VTSSV\": {\"date\": 1416866395389}, \"PH_VSSAV\": {\"date\": 1416866458888}, \"PH_VTJH\": { \"dateStart\": null, \"dateEnd\": null }, \"PH_EVR\": {\"date\": null}, \"PH_OPVP\": {\"date\": null}, \"PH_HPVOA\": {\"date\": null}, \"PH_IP\": {\"date\": null} }";

        ParametriDTO dto = new GsonBuilder().create().fromJson(json, new TypeToken<ParametriDTO>() {
        }.getType());

        when(tarjontaIntegrationService.getHaunParametrit(anyString())).thenReturn(dto);
    }

    @Test
    @UsingDataSet(locations = "peruuta_alemmat.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testPeruutaAlemmat() throws IOException {
        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");

        sijoitteluService.sijoittele(haku, newHashSet("jono1", "jono2", "jono3"), newHashSet("jono1", "jono2", "jono3"));


        Optional<SijoitteluAjo> latestSijoitteluajo = sijoitteluDao.getLatestSijoitteluajo("haku1");
        System.out.println("SijoitteluajoID: " + latestSijoitteluajo.get().getSijoitteluajoId());

        hakukohdeDao.getHakukohdeForSijoitteluajo(latestSijoitteluajo.get().getSijoitteluajoId()).forEach(hakukohde -> {
            System.out.println("HAKUKOHDE: " + hakukohde.getOid());
            hakukohde.getValintatapajonot().forEach(jono -> {
                System.out.println("  JONO: " + jono.getOid());
                jono.getHakemukset().forEach(hakemus -> {
                    System.out.println(hakemus.getHakemusOid() + " " + hakemus.getTila().name() + " " + hakemus.getPrioriteetti());
                });
            });
        });

        haku.getHakukohteet().remove(0);

        sijoitteluService.sijoittele(haku, newHashSet("jono2", "jono3"), newHashSet("jono1", "jono2", "jono3"));

        latestSijoitteluajo = sijoitteluDao.getLatestSijoitteluajo("haku1");
        System.out.println("SijoitteluajoID: " + latestSijoitteluajo.get().getSijoitteluajoId());

        hakukohdeDao.getHakukohdeForSijoitteluajo(latestSijoitteluajo.get().getSijoitteluajoId()).forEach(hakukohde -> {
            System.out.println("HAKUKOHDE: " + hakukohde.getOid());
            hakukohde.getValintatapajonot().forEach(jono -> {
                System.out.println("  JONO: " + jono.getOid());
                jono.getHakemukset().forEach(hakemus -> {
                    System.out.println("    " + hakemus.getHakemusOid() + " " + hakemus.getTila().name() + " " + hakemus.getPrioriteetti());
                });
            });
        });

        haku = valintatietoService.haeValintatiedot("haku1");

        sijoitteluService.sijoittele(haku, newHashSet("jono1", "jono2", "jono3"), newHashSet("jono1", "jono2", "jono3"));

        latestSijoitteluajo = sijoitteluDao.getLatestSijoitteluajo("haku1");
        System.out.println("SijoitteluajoID: " + latestSijoitteluajo.get().getSijoitteluajoId());

        hakukohdeDao.getHakukohdeForSijoitteluajo(latestSijoitteluajo.get().getSijoitteluajoId()).forEach(hakukohde -> {
            System.out.println("HAKUKOHDE: " + hakukohde.getOid());
            hakukohde.getValintatapajonot().forEach(jono -> {
                System.out.println("  JONO: " + jono.getOid());
                jono.getHakemukset().forEach(hakemus -> {
                    System.out.println("    " + hakemus.getHakemusOid() + " " + hakemus.getTila().name() + " " + hakemus.getPrioriteetti());
                });
            });
        });

    }

    @Test
    @UsingDataSet(locations = "peruuta_alemmat.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testEnemmanJonojaKuinValintaperusteissaVaadittu() {
        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");

        assertEquals(getValintatapaJonoOids(haku), newHashSet("jono1", "jono2", "jono3"));

        Set<String> valintaperusteenJonot = newHashSet("jono2");

        sijoitteluService.sijoittele(haku, valintaperusteenJonot, newHashSet("jono1", "jono2", "jono3"));

        haku.getHakukohteet().remove(0);

        assertEquals(getValintatapaJonoOids(haku), newHashSet("jono2", "jono3"));

        sijoitteluService.sijoittele(haku, valintaperusteenJonot, newHashSet("jono1", "jono2", "jono3"));
    }

    @Test
    @UsingDataSet(locations = "peruuta_alemmat.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testPuuttuviaJonojaJotkaVaaditaanValintaperusteissa() {
        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");

        Set<String> valintaperusteenJonot = newHashSet("jono1", "jono2", "jono3");

        sijoitteluService.sijoittele(haku, valintaperusteenJonot, newHashSet("jono1", "jono2", "jono3"));

        haku.getHakukohteet().remove(0);

        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Edellisessä sijoittelussa olleet jonot [jono1] puuttuvat sijoittelusta, vaikka ne ovat valintaperusteissa yhä aktiivisina");

        sijoitteluService.sijoittele(haku, valintaperusteenJonot, newHashSet("jono1", "jono2", "jono3"));
    }

    @Test
    @UsingDataSet(locations = "peruuta_alemmat.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testPuuttuvaJonoKunPoistettuValintaperusteista() {
        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");

        sijoitteluService.sijoittele(haku, newHashSet("jono1", "jono2", "jono3"), newHashSet("jono1", "jono2", "jono3"));

        haku.getHakukohteet().remove(0);

        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Edellisessä sijoittelussa olleet jonot [jono1] ovat kadonneet valintaperusteista");

        sijoitteluService.sijoittele(haku, newHashSet("jono2", "jono3"), newHashSet("jono2", "jono3"));
    }

    @Test()
    @UsingDataSet(locations = "peruuta_alemmat.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testPuuttuvaJonoKunPassivoituValintaperusteista() {
        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");

        sijoitteluService.sijoittele(haku, newHashSet("jono1", "jono2", "jono3"), newHashSet("jono1", "jono2", "jono3"));

        haku.getHakukohteet().remove(0);

        sijoitteluService.sijoittele(haku, newHashSet("jono2", "jono3"), newHashSet("jono1", "jono2", "jono3"));
    }

    @Test()
    @UsingDataSet(locations = "peruuta_alemmat.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testPuuttuvaJonoKunEiPassivoituValintaperusteista() {
        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");

        assertEquals(getValintatapaJonoOids(haku), newHashSet("jono1", "jono2", "jono3"));

        sijoitteluService.sijoittele(haku, newHashSet("jono1", "jono2", "jono3"), newHashSet("jono1", "jono2", "jono3"));

        haku.getHakukohteet().remove(0);

        assertEquals(getValintatapaJonoOids(haku), newHashSet("jono2", "jono3"));

        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Edellisessä sijoittelussa olleet jonot [jono1] puuttuvat sijoittelusta, vaikka ne ovat valintaperusteissa yhä aktiivisina");

        sijoitteluService.sijoittele(haku, newHashSet("jono1", "jono2", "jono3"), newHashSet("jono1", "jono2", "jono3"));
    }

    @Test()
    @UsingDataSet(locations = "peruuta_alemmat.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testPeruunnaHyvaksyttyaYlemmatHakutoiveetKunAMKOPEHaku() {
        fi.vm.sade.sijoittelu.laskenta.external.resource.dto.HakuDTO tarjontaHaku = new fi.vm.sade.sijoittelu.laskenta.external.resource.dto.HakuDTO();
        tarjontaHaku.setKohdejoukkoUri("haunkohdejoukko_12" + "#1");
        tarjontaHaku.setKohdejoukonTarkenne("haunkohdejoukontarkenne_2#1");
        when(tarjontaIntegrationService.getHakuByHakuOid(anyString())).thenReturn(tarjontaHaku);
        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");
        sijoitteluService.sijoittele(haku, newHashSet("jono1", "jono2", "jono3"), newHashSet("jono1", "jono2", "jono3"));
        sijoitteluService.sijoittele(haku, newHashSet("jono1", "jono2", "jono3"), newHashSet("jono1", "jono2", "jono3"));

        List<Hakukohde> hakukohteet = hakukohdeDao.getHakukohdeForSijoitteluajo(sijoitteluDao.getLatestSijoitteluajo("haku1").get().getSijoitteluajoId());
        Hakemus hakija3YlempiToive = hakukohteet.stream()
                .filter(h -> h.getOid().equals("hakukohde1")).findAny().get().getValintatapajonot().get(0)
                .getHakemukset().stream().filter(h -> h.getHakemusOid().equals("hakija3")).findAny().get();
        Hakemus hakija3AlempiToive = hakukohteet.stream()
                .filter(h -> h.getOid().equals("hakukohde2")).findAny().get()
                .getValintatapajonot().get(0)
                .getHakemukset().stream().filter(h -> h.getHakemusOid().equals("hakija3")).findAny().get();
        Hakemus hakija4YlempiToive = hakukohteet.stream()
                .filter(h -> h.getOid().equals("hakukohde1")).findAny().get().getValintatapajonot().get(0)
                .getHakemukset().stream().filter(h -> h.getHakemusOid().equals("hakija4")).findAny().get();
        Hakemus hakija4AlempiToive = hakukohteet.stream()
                .filter(h -> h.getOid().equals("hakukohde2")).findAny().get()
                .getValintatapajonot().get(0)
                .getHakemukset().stream().filter(h -> h.getHakemusOid().equals("hakija4")).findAny().get();
        assertEquals(HakemuksenTila.HYVAKSYTTY, hakija3AlempiToive.getTila());
        assertEquals(HakemuksenTila.PERUUNTUNUT, hakija3YlempiToive.getTila());
        assertEquals(HakemuksenTila.HYVAKSYTTY, hakija4AlempiToive.getTila());
        assertEquals(HakemuksenTila.PERUUNTUNUT, hakija4YlempiToive.getTila());

        Valintatulos peruYlempiHakija = new Valintatulos("jono1", "hakija2", "hakukohde1", "1.2.246.562.24.42438870792", "haku1", 0);
        peruYlempiHakija.setJulkaistavissa(true, "");
        peruYlempiHakija.setTila(ValintatuloksenTila.EI_VASTAANOTETTU_MAARA_AIKANA, "");

        Valintatulos julkaiseHakijan3AlempiToive = new Valintatulos("jono2", "hakija3", "hakukohde2", "1.2.246.562.24.45661259022", "haku1", 1);
        julkaiseHakijan3AlempiToive.setJulkaistavissa(true, "");
        julkaiseHakijan3AlempiToive.setTila(ValintatuloksenTila.KESKEN, "");

        when(valintatulosWithVastaanotto.forHaku("haku1")).thenReturn(Arrays.asList(peruYlempiHakija, julkaiseHakijan3AlempiToive));
        sijoitteluService.sijoittele(haku, newHashSet("jono1", "jono2", "jono3"), newHashSet("jono1", "jono2", "jono3"));

        hakukohteet = hakukohdeDao.getHakukohdeForSijoitteluajo(sijoitteluDao.getLatestSijoitteluajo("haku1").get().getSijoitteluajoId());
        hakija3YlempiToive = hakukohteet.stream()
                .filter(h -> h.getOid().equals("hakukohde1")).findAny().get().getValintatapajonot().get(0)
                .getHakemukset().stream().filter(h -> h.getHakemusOid().equals("hakija3")).findAny().get();
        hakija3AlempiToive = hakukohteet.stream()
                .filter(h -> h.getOid().equals("hakukohde2")).findAny().get()
                .getValintatapajonot().get(0)
                .getHakemukset().stream().filter(h -> h.getHakemusOid().equals("hakija3")).findAny().get();
        hakija4YlempiToive = hakukohteet.stream()
                .filter(h -> h.getOid().equals("hakukohde1")).findAny().get().getValintatapajonot().get(0)
                .getHakemukset().stream().filter(h -> h.getHakemusOid().equals("hakija4")).findAny().get();
        hakija4AlempiToive = hakukohteet.stream()
                .filter(h -> h.getOid().equals("hakukohde2")).findAny().get()
                .getValintatapajonot().get(0)
                .getHakemukset().stream().filter(h -> h.getHakemusOid().equals("hakija4")).findAny().get();
        assertEquals(HakemuksenTila.HYVAKSYTTY, hakija3AlempiToive.getTila());
        assertEquals(HakemuksenTila.PERUUNTUNUT, hakija3YlempiToive.getTila());
        assertEquals(HakemuksenTila.PERUUNTUNUT, hakija4AlempiToive.getTila());
        assertEquals(HakemuksenTila.HYVAKSYTTY, hakija4YlempiToive.getTila());
    }

    @Test
    @UsingDataSet(locations = "valisijoittelu_hylkays.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testAlahylkaaValisijoittelussaHylattyja() throws IOException {

        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");

        List<Hakukohde> hakukohteet = haku.getHakukohteet().stream().map(h -> DomainConverter.convertToHakukohde(h)).collect(Collectors.toList());

        Hakemus hakemus1 = hakukohteet.stream()
                .flatMap(h -> h.getValintatapajonot().stream())
                .flatMap(j -> j.getHakemukset().stream())
                .filter(h -> h.getHakemusOid().equals("hakemus1"))
                .findFirst()
                .get();

        Hakemus hakemus2 = hakukohteet.stream()
                .flatMap(h -> h.getValintatapajonot().stream())
                .flatMap(j -> j.getHakemukset().stream())
                .filter(h -> h.getHakemusOid().equals("hakemus2"))
                .findFirst()
                .get();

        Hakemus hakemus3 = hakukohteet.stream()
                .flatMap(h -> h.getValintatapajonot().stream())
                .flatMap(j -> j.getHakemukset().stream())
                .filter(h -> h.getHakemusOid().equals("hakemus3"))
                .findFirst()
                .get();

        assertEquals(HakemuksenTila.VARALLA, hakemus1.getTila());
        assertEquals(HakemuksenTila.HYLATTY, hakemus2.getTila());
        assertEquals(HakemuksenTila.HYLATTY, hakemus3.getTila());

    }

    private Set<String> getValintatapaJonoOids(HakuDTO haku) {
        return Collections.unmodifiableSet(haku.getHakukohteet().stream()
                .flatMap(h -> h.getValinnanvaihe().stream().flatMap(v -> v.getValintatapajonot().stream().map(ValintatapajonoDTO::getOid)))
                .collect(toSet()));
    }

//    @Configuration
//    @ComponentScan("fi.vm.sade.sijoittelu.laskenta.service.business.impl")
//    static class someConfig {
//
//        @Bean
//        PropertyPlaceholderConfigurer propConfig() {
//            PropertyPlaceholderConfigurer ppc =  new PropertyPlaceholderConfigurer();
//            ppc.setLocation(new ClassPathResource("common.properties"));
//            return ppc;
//        }
//    }

}
