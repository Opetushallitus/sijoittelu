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
import fi.vm.sade.sijoittelu.laskenta.service.business.ValintarekisteriService;
import fi.vm.sade.sijoittelu.laskenta.service.business.ValintatulosWithVastaanotto;
import fi.vm.sade.sijoittelu.laskenta.service.it.TarjontaIntegrationService;

import fi.vm.sade.valintalaskenta.domain.dto.ValintatapajonoDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintalaskenta.tulos.service.impl.ValintatietoService;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:test-sijoittelu-batch-mongo.xml" })
public class SijoitteluBusinessTest {

    private static final Logger LOG = LoggerFactory.getLogger(SijoitteluBusinessTest.class);

    @Autowired
    private ValintarekisteriService valintarekisteriService;

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

    private ArgumentCaptor<SijoitteluAjo> sijoitteluAjoArgumentCaptor;
    private ArgumentCaptor<List> hakukohdeArgumentCaptor;
    private ArgumentCaptor<List> valintatulosArgumentCaptor;

    @Before
    public void setup() {
        tarjontaIntegrationService = mock(TarjontaIntegrationService.class);
        valintatulosWithVastaanotto = mock(ValintatulosWithVastaanotto.class);
        valintarekisteriService = mock(ValintarekisteriService.class);

        ReflectionTestUtils.setField(sijoitteluService,
                "tarjontaIntegrationService",
                tarjontaIntegrationService);
        ReflectionTestUtils.setField(sijoitteluService,
                "valintatulosWithVastaanotto",
                valintatulosWithVastaanotto);
        ReflectionTestUtils.setField(sijoitteluService,
                "valintarekisteriService",
                valintarekisteriService);

        fi.vm.sade.sijoittelu.laskenta.external.resource.dto.HakuDTO hakuDto = new fi.vm.sade.sijoittelu.laskenta.external.resource.dto.HakuDTO();
        hakuDto.setKohdejoukkoUri("haunkohdejoukko_11#1");

        when(tarjontaIntegrationService.getHakuByHakuOid(anyString())).thenReturn(hakuDto);

        String json = "{ \"target\": \"1.2.246.562.29.173465377510\", \"__modified__\": 1416309364472, \"__modifiedBy__\": \"1.2.246.562.24.47840234552\", \"PH_TJT\": {\"date\": null}, \"PH_HKLPT\": {\"date\": null}, \"PH_HKMT\": {\"date\": null}, \"PH_KKM\": { \"dateStart\": null, \"dateEnd\": null }, \"PH_HVVPTP\": {\"date\": null}, \"PH_KTT\": { \"dateStart\": null, \"dateEnd\": null }, \"PH_OLVVPKE\": { \"dateStart\": null, \"dateEnd\": null }, \"PH_VLS\": { \"dateStart\": null, \"dateEnd\": null }, \"PH_SS\": { \"dateStart\": null, \"dateEnd\": null }, \"PH_JKLIP\": {\"date\": null}, \"PH_HKP\": {\"date\": 14168663953898}, \"PH_VTSSV\": {\"date\": 1416866395389}, \"PH_VSSAV\": {\"date\": 1416866458888}, \"PH_VTJH\": { \"dateStart\": null, \"dateEnd\": null }, \"PH_EVR\": {\"date\": null}, \"PH_OPVP\": {\"date\": null}, \"PH_HPVOA\": {\"date\": null}, \"PH_IP\": {\"date\": null} }";

        ParametriDTO dto = new GsonBuilder().create().fromJson(json, new TypeToken<ParametriDTO>() {}.getType());

        when(tarjontaIntegrationService.getHaunParametrit(anyString())).thenReturn(dto);

        sijoitteluAjoArgumentCaptor = ArgumentCaptor.forClass(SijoitteluAjo.class);
        hakukohdeArgumentCaptor = ArgumentCaptor.forClass(List.class);
        valintatulosArgumentCaptor = ArgumentCaptor.forClass(List.class);
    }

    @Test
    @UsingDataSet(locations = "peruuta_alemmat.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testPeruutaAlemmat() {
        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");

        sijoitteluService.sijoittele(haku, newHashSet("jono1", "jono2", "jono3"), newHashSet("jono1", "jono2", "jono3"), System.currentTimeMillis());

        verify(valintarekisteriService, times(1)).tallennaSijoittelu(
                sijoitteluAjoArgumentCaptor.capture(),
                hakukohdeArgumentCaptor.capture(),
                valintatulosArgumentCaptor.capture());

        SijoitteluAjo sijoitteluAjo1 = sijoitteluAjoArgumentCaptor.getValue();
        System.out.println("SijoitteluajoID: " + sijoitteluAjo1.getSijoitteluajoId());

        List<Hakukohde> hakukohteet1 = hakukohdeArgumentCaptor.<List<Hakukohde>>getValue();
        assertEquals(3, hakukohteet1.size());
        assertTrue(Arrays.asList("jono1", "jono2", "jono3").equals(
          hakukohteet1.stream()
                  .map(Hakukohde::getValintatapajonot)
                  .flatMap(List::stream)
                  .map(Valintatapajono::getOid)
                  .collect(Collectors.toList())
        ));

        printHakukohteet(hakukohteet1);

        when(valintarekisteriService.getLatestSijoitteluajo("haku1")).thenReturn(sijoitteluAjo1);
        when(valintarekisteriService.getSijoitteluajonHakukohteet(anyLong())).thenReturn(hakukohteet1);

        removeJono(haku, "jono1");

        sijoitteluService.sijoittele(haku, newHashSet("jono2", "jono3"), newHashSet("jono1", "jono2", "jono3"), System.currentTimeMillis());
        verify(valintarekisteriService, times(2)).tallennaSijoittelu(
                sijoitteluAjoArgumentCaptor.capture(),
                hakukohdeArgumentCaptor.capture(),
                valintatulosArgumentCaptor.capture());

        SijoitteluAjo sijoitteluAjo2 = sijoitteluAjoArgumentCaptor.getAllValues().get(2);
        assertFalse(sijoitteluAjo2.getSijoitteluajoId().equals(sijoitteluAjo1.getSijoitteluajoId()));

        List<Hakukohde> hakukohteet2 = hakukohdeArgumentCaptor.<List<Hakukohde>>getAllValues().get(2);
        assertEquals(3, hakukohteet2.size());
        assertTrue(Arrays.asList("jono2", "jono3").equals(
                hakukohteet2.stream()
                        .map(Hakukohde::getValintatapajonot)
                        .flatMap(List::stream)
                        .map(Valintatapajono::getOid)
                        .collect(Collectors.toList())
        ));

        printHakukohteet(hakukohteet2);

        when(valintarekisteriService.getLatestSijoitteluajo("haku1")).thenReturn(sijoitteluAjo2);
        when(valintarekisteriService.getSijoitteluajonHakukohteet(sijoitteluAjo2.getSijoitteluajoId())).thenReturn(hakukohteet2);

        haku = valintatietoService.haeValintatiedot("haku1");

        sijoitteluService.sijoittele(haku, newHashSet("jono1", "jono2", "jono3"), newHashSet("jono1", "jono2", "jono3"), System.currentTimeMillis());

        verify(valintarekisteriService, times(3)).tallennaSijoittelu(
                sijoitteluAjoArgumentCaptor.capture(),
                hakukohdeArgumentCaptor.capture(),
                valintatulosArgumentCaptor.capture());

        SijoitteluAjo sijoitteluAjo3 = sijoitteluAjoArgumentCaptor.getAllValues().get(5);
        assertFalse(sijoitteluAjo3.getSijoitteluajoId().equals(sijoitteluAjo1.getSijoitteluajoId()));

        List<Hakukohde> hakukohteet3 = hakukohdeArgumentCaptor.<List<Hakukohde>>getAllValues().get(5);
        assertEquals(3, hakukohteet3.size());
        assertTrue(Arrays.asList("jono1", "jono2", "jono3").equals(
                hakukohteet3.stream()
                        .map(Hakukohde::getValintatapajonot)
                        .flatMap(List::stream)
                        .map(Valintatapajono::getOid)
                        .collect(Collectors.toList())
        ));

        printHakukohteet(hakukohteet3);
    }

    private void printHakukohteet(List<Hakukohde> hakukohteet){
        hakukohteet.stream().forEach(hakukohde -> {
            System.out.println("HAKUKOHDE: " + hakukohde.getOid());
            hakukohde.getValintatapajonot().forEach(jono -> {
                System.out.println("  JONO: " + jono.getOid());
                jono.getHakemukset().forEach(hakemus -> {
                    System.out.println(hakemus.getHakemusOid() + " " + hakemus.getTila().name() + " " + hakemus.getPrioriteetti());
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

        sijoitteluService.sijoittele(haku, valintaperusteenJonot, newHashSet("jono1", "jono2", "jono3"), System.currentTimeMillis());

        long sijoitteluajoId = captureSijoitteluajoForNextSijoittelu();

        removeJono(haku, "jono1");

        assertEquals(getValintatapaJonoOids(haku), newHashSet("jono2", "jono3"));

        sijoitteluService.sijoittele(haku, valintaperusteenJonot, newHashSet("jono1", "jono2", "jono3"), System.currentTimeMillis());

        assertSijoitteluUsedSijoitteluajo(sijoitteluajoId);
    }

    @Test
    @UsingDataSet(locations = "peruuta_alemmat.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testPuuttuviaJonojaJotkaVaaditaanValintaperusteissa() {
        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");

        Set<String> valintaperusteenJonot = newHashSet("jono1", "jono2", "jono3");

        sijoitteluService.sijoittele(haku, valintaperusteenJonot, newHashSet("jono1", "jono2", "jono3"), System.currentTimeMillis());

        long sijoitteluajoId = captureSijoitteluajoForNextSijoittelu();

        removeJono(haku, "jono1");

        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Edellisessä sijoittelussa olleet jonot [jono1] puuttuvat sijoittelusta, vaikka ne ovat valintaperusteissa yhä aktiivisina");

        sijoitteluService.sijoittele(haku, valintaperusteenJonot, newHashSet("jono1", "jono2", "jono3"), System.currentTimeMillis());

        assertSijoitteluUsedSijoitteluajo(sijoitteluajoId);
    }

    @Test
    @UsingDataSet(locations = "peruuta_alemmat.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testPuuttuvaJonoKunPoistettuValintaperusteista() {
        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");

        sijoitteluService.sijoittele(haku, newHashSet("jono1", "jono2", "jono3"), newHashSet("jono1", "jono2", "jono3"), System.currentTimeMillis());

        long sijoitteluajoId = captureSijoitteluajoForNextSijoittelu();

        removeJono(haku, "jono1");

        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Edellisessä sijoittelussa olleet jonot [jono1] ovat kadonneet valintaperusteista");

        sijoitteluService.sijoittele(haku, newHashSet("jono2", "jono3"), newHashSet("jono2", "jono3"), System.currentTimeMillis());

        assertSijoitteluUsedSijoitteluajo(sijoitteluajoId);
    }

    @Test()
    @UsingDataSet(locations = "peruuta_alemmat.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testPuuttuvaJonoKunPassivoituValintaperusteista() {
        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");

        sijoitteluService.sijoittele(haku, newHashSet("jono1", "jono2", "jono3"), newHashSet("jono1", "jono2", "jono3"), System.currentTimeMillis());

        long sijoitteluajoId = captureSijoitteluajoForNextSijoittelu();

        removeJono(haku, "jono1");

        sijoitteluService.sijoittele(haku, newHashSet("jono2", "jono3"), newHashSet("jono1", "jono2", "jono3"), System.currentTimeMillis());

        assertSijoitteluUsedSijoitteluajo(sijoitteluajoId);
    }

    @Test()
    @UsingDataSet(locations = "peruuta_alemmat.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testPuuttuvaJonoKunEiPassivoituValintaperusteista() {
        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");

        assertEquals(getValintatapaJonoOids(haku), newHashSet("jono1", "jono2", "jono3"));

        sijoitteluService.sijoittele(haku, newHashSet("jono1", "jono2", "jono3"), newHashSet("jono1", "jono2", "jono3"), System.currentTimeMillis() );

        long sijoitteluajoId = captureSijoitteluajoForNextSijoittelu();

        removeJono(haku, "jono1");

        assertEquals(getValintatapaJonoOids(haku), newHashSet("jono2", "jono3"));

        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Edellisessä sijoittelussa olleet jonot [jono1] puuttuvat sijoittelusta, vaikka ne ovat valintaperusteissa yhä aktiivisina");

        sijoitteluService.sijoittele(haku, newHashSet("jono1", "jono2", "jono3"), newHashSet("jono1", "jono2", "jono3"), System.currentTimeMillis());

        assertSijoitteluUsedSijoitteluajo(sijoitteluajoId);
    }

    @Test()
    @UsingDataSet(locations = "peruuta_alemmat.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testPuuttuvaHakukohde() {
        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");

        assertEquals(getValintatapaJonoOids(haku), newHashSet("jono1", "jono2", "jono3"));

        sijoitteluService.sijoittele(haku, newHashSet("jono1", "jono2", "jono3"), newHashSet("jono1", "jono2", "jono3"), System.currentTimeMillis() );

        long sijoitteluajoId = captureSijoitteluajoForNextSijoittelu();

        String removedHakukohdeOid = "hakukohde1";
        haku.getHakukohteet().removeIf(hk -> hk.getOid().equals(removedHakukohdeOid));

        assertEquals(getValintatapaJonoOids(haku), newHashSet("jono2", "jono3"));

        thrown.expect(RuntimeException.class);
        thrown.expectMessage("Edellisessä sijoittelussa olleet hakukohteet [" + removedHakukohdeOid + "] puuttuvat sijoittelusta");

        sijoitteluService.sijoittele(haku, newHashSet("jono1", "jono2", "jono3"), newHashSet("jono1", "jono2", "jono3"), System.currentTimeMillis());

        assertSijoitteluUsedSijoitteluajo(sijoitteluajoId);
    }

    private Long captureSijoitteluajoForNextSijoittelu() {
        verify(valintarekisteriService, times(1)).tallennaSijoittelu(
                sijoitteluAjoArgumentCaptor.capture(),
                hakukohdeArgumentCaptor.capture(),
                valintatulosArgumentCaptor.capture());

        SijoitteluAjo sijoitteluAjo = sijoitteluAjoArgumentCaptor.getValue();
        List<Hakukohde> hakukohteet = hakukohdeArgumentCaptor.<List<Hakukohde>>getValue();
        List<Valintatulos> valintatulokset = createValintatuloksetFromHakukohteet(hakukohteet);

        when(valintarekisteriService.getLatestSijoitteluajo("haku1")).thenReturn(sijoitteluAjo);
        when(valintarekisteriService.getSijoitteluajonHakukohteet(sijoitteluAjo.getSijoitteluajoId())).thenReturn(hakukohteet);
        when(valintarekisteriService.getValintatulokset("haku1")).thenReturn(valintatulokset);

        return sijoitteluAjo.getSijoitteluajoId();
    }

    private List<Valintatulos> createValintatuloksetFromHakukohteet(List<Hakukohde> hakukohteet) {
        List<Valintatulos> valintatulokset = new ArrayList<>();
        hakukohteet.forEach(hakukohde -> {
            hakukohde.getValintatapajonot().forEach(valintatapajono -> {
                valintatapajono.getHakemukset().forEach(hakemus -> {
                    Valintatulos valintatulos = new Valintatulos(
                            hakemus.getHakemusOid(),
                            hakemus.getHakijaOid(),
                            hakukohde.getOid(),
                            false,
                            hakemus.getIlmoittautumisTila(),
                            false,
                            ValintatuloksenTila.KESKEN,
                            false,
                            valintatapajono.getOid()
                    );
                    valintatulokset.add(valintatulos);
                });
            });
        });
        return valintatulokset;
    }

    private void assertSijoitteluUsedSijoitteluajo(long sijoitteluajoId) {
        verify(valintarekisteriService, times(2)).getLatestSijoitteluajo("haku1");
        verify(valintarekisteriService).getSijoitteluajonHakukohteet(sijoitteluajoId);
        verify(valintarekisteriService, times(2)).getValintatulokset("haku1");
    }

    @Test()
    @UsingDataSet(locations = "peruuta_alemmat.json", loadStrategy = LoadStrategyEnum.CLEAN_INSERT)
    public void testPeruunnaHyvaksyttyaYlemmatHakutoiveetKunAMKOPEHaku() {
        fi.vm.sade.sijoittelu.laskenta.external.resource.dto.HakuDTO tarjontaHaku = new fi.vm.sade.sijoittelu.laskenta.external.resource.dto.HakuDTO();
        tarjontaHaku.setKohdejoukkoUri("haunkohdejoukko_12" + "#1");
        tarjontaHaku.setKohdejoukonTarkenne("haunkohdejoukontarkenne_2#1");
        when(tarjontaIntegrationService.getHakuByHakuOid(anyString())).thenReturn(tarjontaHaku);
        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");
        sijoitteluService.sijoittele(haku, newHashSet("jono1", "jono2", "jono3"), newHashSet("jono1", "jono2", "jono3"), System.currentTimeMillis());

        verify(valintarekisteriService, times(1)).tallennaSijoittelu(
                sijoitteluAjoArgumentCaptor.capture(),
                hakukohdeArgumentCaptor.capture(),
                valintatulosArgumentCaptor.capture());

        SijoitteluAjo sijoitteluAjo = sijoitteluAjoArgumentCaptor.getValue();
        List<Hakukohde> hakukohteet = hakukohdeArgumentCaptor.<List<Hakukohde>>getValue();
        List<Valintatulos> valintatulokset = createValintatuloksetFromHakukohteet(hakukohteet);

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

        valintatulokset.stream().filter(v ->
            "jono1".equals(v.getValintatapajonoOid()) && "hakija2".equals(v.getHakemusOid()) && "hakukohde1".equals(v.getHakukohdeOid())
        ).forEach(v -> {
            v.setJulkaistavissa(true, "");
            v.setTila(ValintatuloksenTila.EI_VASTAANOTETTU_MAARA_AIKANA, "");
        });

        valintatulokset.stream().filter(v ->
                "jono2".equals(v.getValintatapajonoOid()) && "hakija3".equals(v.getHakemusOid()) && "hakukohde2".equals(v.getHakukohdeOid())
        ).forEach(v -> {
            v.setJulkaistavissa(true, "");
            v.setTila(ValintatuloksenTila.KESKEN, "");
        });

        when(valintarekisteriService.getLatestSijoitteluajo("haku1")).thenReturn(sijoitteluAjo);
        when(valintarekisteriService.getSijoitteluajonHakukohteet(sijoitteluAjo.getSijoitteluajoId())).thenReturn(hakukohteet);
        when(valintarekisteriService.getValintatulokset("haku1")).thenReturn(valintatulokset);

        sijoitteluService.sijoittele(haku, newHashSet("jono1", "jono2", "jono3"), newHashSet("jono1", "jono2", "jono3"), System.currentTimeMillis());

        verify(valintarekisteriService, times(2)).tallennaSijoittelu(
                sijoitteluAjoArgumentCaptor.capture(),
                hakukohdeArgumentCaptor.capture(),
                valintatulosArgumentCaptor.capture());

        List<Hakukohde> hakukohteet2 = hakukohdeArgumentCaptor.<List<Hakukohde>>getAllValues().get(2);
        hakija3YlempiToive = hakukohteet2.stream()
                .filter(h -> h.getOid().equals("hakukohde1")).findAny().get().getValintatapajonot().get(0)
                .getHakemukset().stream().filter(h -> h.getHakemusOid().equals("hakija3")).findAny().get();
        hakija3AlempiToive = hakukohteet2.stream()
                .filter(h -> h.getOid().equals("hakukohde2")).findAny().get()
                .getValintatapajonot().get(0)
                .getHakemukset().stream().filter(h -> h.getHakemusOid().equals("hakija3")).findAny().get();
        hakija4YlempiToive = hakukohteet2.stream()
                .filter(h -> h.getOid().equals("hakukohde1")).findAny().get().getValintatapajonot().get(0)
                .getHakemukset().stream().filter(h -> h.getHakemusOid().equals("hakija4")).findAny().get();
        hakija4AlempiToive = hakukohteet2.stream()
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
    public void testAlahylkaaValisijoittelussaHylattyja() {

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

    private void removeJono(HakuDTO haku, String jonoOid) {
        haku.getHakukohteet().forEach(hakukohdeDTO ->
            hakukohdeDTO.getValinnanvaihe().forEach(valintatietoValinnanvaiheDTO ->
                valintatietoValinnanvaiheDTO.getValintatapajonot().removeIf(j -> {
                    return j.getOid().equals(jonoOid);
                })));
    }
}
