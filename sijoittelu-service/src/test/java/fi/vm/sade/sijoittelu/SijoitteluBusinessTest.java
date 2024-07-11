package fi.vm.sade.sijoittelu;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import fi.vm.sade.testing.AbstractIntegrationTest;
import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.HakukohdeItem;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.sijoittelu.domain.Valintatapajono.JonosijaTieto;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluajoResourcesLoader;
import fi.vm.sade.sijoittelu.laskenta.service.business.ValintarekisteriService;
import fi.vm.sade.sijoittelu.laskenta.service.business.ValintatulosWithVastaanotto;
import fi.vm.sade.sijoittelu.laskenta.service.it.Haku;
import fi.vm.sade.sijoittelu.laskenta.service.it.TarjontaIntegrationService;
import fi.vm.sade.valintalaskenta.domain.dto.ValintatapajonoDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintalaskenta.tulos.service.impl.ValintatietoService;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@ActiveProfiles("test")
public class SijoitteluBusinessTest extends AbstractIntegrationTest {

    @Autowired
    private ValintarekisteriService valintarekisteriService;

    @Autowired
    private SijoitteluBusinessService sijoitteluService;

    private ValintatulosWithVastaanotto valintatulosWithVastaanotto;

    private TarjontaIntegrationService tarjontaIntegrationService;

    @Autowired
    private ValintatietoService valintatietoService;

    @Autowired
    private ApplicationContext applicationContext;

    private ArgumentCaptor<SijoitteluAjo> sijoitteluAjoArgumentCaptor;
    private ArgumentCaptor<List> hakukohdeArgumentCaptor;
    private ArgumentCaptor<List> valintatulosArgumentCaptor;

    @BeforeEach
    public void setup() {
        valintatulosWithVastaanotto = mock(ValintatulosWithVastaanotto.class);
        valintarekisteriService = mock(ValintarekisteriService.class);
        tarjontaIntegrationService = mock(TarjontaIntegrationService.class);
        SijoitteluajoResourcesLoader sijoitteluajoResourcesLoader = new SijoitteluajoResourcesLoader(tarjontaIntegrationService, valintarekisteriService);

        ReflectionTestUtils.setField(sijoitteluService,
                "sijoitteluajoResourcesLoader",
                sijoitteluajoResourcesLoader);
        ReflectionTestUtils.setField(sijoitteluService,
                "valintatulosWithVastaanotto",
                valintatulosWithVastaanotto);
        ReflectionTestUtils.setField(sijoitteluService,
                "valintarekisteriService",
                valintarekisteriService);

        when(tarjontaIntegrationService.getHaku(anyString())).thenReturn(new Haku(
                "hakuOid",
                "haunkohdejoukko_11#1",
                null,
                true,
                Instant.ofEpochMilli(1416866395389L),
                Instant.ofEpochMilli(1416866458888L),
                null,
                Instant.ofEpochMilli(14168663953898L),
                "",
                true
        ));

        sijoitteluAjoArgumentCaptor = ArgumentCaptor.forClass(SijoitteluAjo.class);
        hakukohdeArgumentCaptor = ArgumentCaptor.forClass(List.class);
        valintatulosArgumentCaptor = ArgumentCaptor.forClass(List.class);
    }

    @Test
    @Sql("peruuta_alemmat.sql")
    public void testPeruutaAlemmat() {
        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");

        sijoitteluService.sijoittele(haku, newHashSet("jono1", "jono2", "jono3"), newHashSet("jono1", "jono2", "jono3"), System.currentTimeMillis(), Collections.emptyMap());

        verify(valintarekisteriService, times(1)).tallennaSijoittelu(
                sijoitteluAjoArgumentCaptor.capture(),
                hakukohdeArgumentCaptor.capture(),
                valintatulosArgumentCaptor.capture());

        SijoitteluAjo sijoitteluAjo1 = sijoitteluAjoArgumentCaptor.getValue();
        System.out.println("SijoitteluajoID: " + sijoitteluAjo1.getSijoitteluajoId());

        List<Hakukohde> hakukohteet1 = hakukohdeArgumentCaptor.getValue();
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
        when(valintarekisteriService.getSijoitteluajonHakukohteet(anyLong(), anyString())).thenReturn(hakukohteet1);

        removeJono(haku);

        sijoitteluService.sijoittele(haku, newHashSet("jono2", "jono3"), newHashSet("jono1", "jono2", "jono3"), System.currentTimeMillis(), Collections.emptyMap());
        verify(valintarekisteriService, times(2)).tallennaSijoittelu(
                sijoitteluAjoArgumentCaptor.capture(),
                hakukohdeArgumentCaptor.capture(),
                valintatulosArgumentCaptor.capture());

        SijoitteluAjo sijoitteluAjo2 = sijoitteluAjoArgumentCaptor.getAllValues().get(2);
        assertNotEquals(sijoitteluAjo2.getSijoitteluajoId(), sijoitteluAjo1.getSijoitteluajoId());

        List<Hakukohde> hakukohteet2 = hakukohdeArgumentCaptor.getAllValues().get(2);
        assertEquals(3, hakukohteet2.size());
        assertEquals(Arrays.asList("jono2", "jono3"), hakukohteet2.stream()
          .map(Hakukohde::getValintatapajonot)
          .flatMap(List::stream)
          .map(Valintatapajono::getOid)
          .collect(Collectors.toList()));

        printHakukohteet(hakukohteet2);

        when(valintarekisteriService.getLatestSijoitteluajo("haku1")).thenReturn(sijoitteluAjo2);
        when(valintarekisteriService.getSijoitteluajonHakukohteet(sijoitteluAjo2.getSijoitteluajoId(), "haku1")).thenReturn(hakukohteet2);

        haku = valintatietoService.haeValintatiedot("haku1");

        sijoitteluService.sijoittele(haku, newHashSet("jono1", "jono2", "jono3"), newHashSet("jono1", "jono2", "jono3"), System.currentTimeMillis(), Collections.emptyMap());

        verify(valintarekisteriService, times(3)).tallennaSijoittelu(
                sijoitteluAjoArgumentCaptor.capture(),
                hakukohdeArgumentCaptor.capture(),
                valintatulosArgumentCaptor.capture());

        SijoitteluAjo sijoitteluAjo3 = sijoitteluAjoArgumentCaptor.getAllValues().get(5);
        assertNotEquals(sijoitteluAjo3.getSijoitteluajoId(), sijoitteluAjo1.getSijoitteluajoId());

        List<Hakukohde> hakukohteet3 = hakukohdeArgumentCaptor.getAllValues().get(5);
        assertEquals(3, hakukohteet3.size());
        assertEquals(Arrays.asList("jono1", "jono2", "jono3"), hakukohteet3.stream()
          .map(Hakukohde::getValintatapajonot)
          .flatMap(List::stream)
          .map(Valintatapajono::getOid)
          .collect(Collectors.toList()));

        printHakukohteet(hakukohteet3);

        List<Hakemus> hakija1Results = hakukohteet3.stream()
            .flatMap(hk -> hk.getValintatapajonot().stream()
            .flatMap(v -> v.getHakemukset().stream()))
            .filter(h -> "hakemus1".equals(h.getHakemusOid()))
            .toList();
        assertEquals(HakemuksenTila.HYVAKSYTTY, hakija1Results.get(0).getTila());
        assertEquals(HakemuksenTila.PERUUNTUNUT, hakija1Results.get(1).getTila());
        assertEquals(HakemuksenTila.PERUUNTUNUT, hakija1Results.get(2).getTila());
    }

    @Test
    @Sql("peruuta_alemmat.sql")
    public void testAlempiaHakutoiveitaEiPeruunnutetaJosHakutoiveidenPriorisointiEiOleKaytossa() {
        when(tarjontaIntegrationService.getHaku(anyString())).thenReturn(new Haku(
                "hakuOid",
                "haunkohdejoukko_11#1",
                null,
                false,
                Instant.ofEpochMilli(1416866395389L),
                Instant.ofEpochMilli(1416866458888L),
                null,
                Instant.ofEpochMilli(14168663953898L),
                "",
                true
        ));

        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");

        sijoitteluService.sijoittele(haku, newHashSet("jono1", "jono2", "jono3"), newHashSet("jono1", "jono2", "jono3"), System.currentTimeMillis(), Collections.emptyMap());

        verify(valintarekisteriService, times(1)).tallennaSijoittelu(
                sijoitteluAjoArgumentCaptor.capture(),
                hakukohdeArgumentCaptor.capture(),
                valintatulosArgumentCaptor.capture());

        List<Hakukohde> hakukohteet1 = hakukohdeArgumentCaptor.getValue();
        assertEquals(3, hakukohteet1.size());
        assertEquals(Arrays.asList("jono1", "jono2", "jono3"), hakukohteet1.stream()
            .map(Hakukohde::getValintatapajonot)
            .flatMap(List::stream)
            .map(Valintatapajono::getOid)
            .collect(Collectors.toList()));

        List<Hakemus> hakija1Results = hakukohteet1.stream()
            .flatMap(hk -> hk.getValintatapajonot().stream()
            .flatMap(v -> v.getHakemukset().stream()))
            .filter(h -> "hakemus1".equals(h.getHakemusOid()))
            .collect(Collectors.toList());
        assertEquals(HakemuksenTila.HYVAKSYTTY, hakija1Results.get(0).getTila());
        assertEquals(HakemuksenTila.HYVAKSYTTY, hakija1Results.get(1).getTila());
        assertEquals(HakemuksenTila.HYVAKSYTTY, hakija1Results.get(2).getTila());
    }

    private void printHakukohteet(List<Hakukohde> hakukohteet){
        hakukohteet.forEach(hakukohde -> {
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
    @Sql("peruuta_alemmat.sql")
    public void testEnemmanJonojaKuinValintaperusteissaVaadittu() {
        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");

        assertEquals(getValintatapaJonoOids(haku), newHashSet("jono1", "jono2", "jono3"));

        Set<String> valintaperusteenJonot = newHashSet("jono2");

        sijoitteluService.sijoittele(haku, valintaperusteenJonot, newHashSet("jono1", "jono2", "jono3"), System.currentTimeMillis(), Collections.emptyMap());

        long sijoitteluajoId = captureSijoitteluajoForNextSijoittelu();

        removeJono(haku);

        assertEquals(getValintatapaJonoOids(haku), newHashSet("jono2", "jono3"));

        sijoitteluService.sijoittele(haku, valintaperusteenJonot, newHashSet("jono1", "jono2", "jono3"), System.currentTimeMillis(), Collections.emptyMap());

        assertSijoitteluUsedSijoitteluajo(sijoitteluajoId);
    }

    @Test
    @Sql("peruuta_alemmat.sql")
    public void testPuuttuviaJonojaJotkaVaaditaanValintaperusteissa() {
        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");

        Set<String> valintaperusteenJonot = newHashSet("jono1", "jono2", "jono3");

        sijoitteluService.sijoittele(haku, valintaperusteenJonot, newHashSet("jono1", "jono2", "jono3"), System.currentTimeMillis(), Collections.emptyMap());

        long sijoitteluajoId = captureSijoitteluajoForNextSijoittelu();

        removeJono(haku);

        Exception exception = Assertions.assertThrows(RuntimeException.class, () -> sijoitteluService.sijoittele(haku, valintaperusteenJonot, newHashSet("jono1", "jono2", "jono3"), System.currentTimeMillis(), Collections.emptyMap()));;
        assertEquals("Edellisessä sijoittelussa olleet jonot puuttuvat sijoittelusta, vaikka ne ovat " +
                "valintaperusteissa yhä aktiivisina: [Hakukohde hakukohde1 , jono \"Jono1\" (jono1 , prio 0)]", exception.getMessage());

        assertSijoitteluUsedSijoitteluajo(sijoitteluajoId);
    }

    @Test
    @Sql("peruuta_alemmat.sql")
    public void testPuuttuvaJonoKunKadonnutLaskennanTuloksista() {
        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");

        sijoitteluService.sijoittele(haku, newHashSet("jono1", "jono2", "jono3"), newHashSet("jono1", "jono2", "jono3"), System.currentTimeMillis(), Collections.emptyMap());

        long sijoitteluajoId = captureSijoitteluajoForNextSijoittelu();

        removeJono(haku);

        Exception exception = Assertions.assertThrows(RuntimeException.class, () -> sijoitteluService.sijoittele(haku, Collections.emptySet(), newHashSet("jono2", "jono3"), System.currentTimeMillis(), Collections.emptyMap()));
        assertEquals("Edellisessä sijoittelussa olleet jonot puuttuvat laskennan tuloksista: [Hakukohde hakukohde1 , jono \"Jono1\" (jono1 , prio 0)]", exception.getMessage());

        assertSijoitteluUsedSijoitteluajo(sijoitteluajoId);
    }

    @Test
    @Sql("peruuta_alemmat.sql")
    public void testPuuttuvaJonoKunKadonnutValintaperusteista() {
        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");

        sijoitteluService.sijoittele(haku, newHashSet("jono1", "jono2", "jono3"), newHashSet("jono2", "jono3"), System.currentTimeMillis(), Collections.emptyMap());

        long sijoitteluajoId = captureSijoitteluajoForNextSijoittelu();

        Exception exception = Assertions.assertThrows(RuntimeException.class, () -> sijoitteluService.sijoittele(haku, Collections.emptySet(), newHashSet("jono2", "jono3"), System.currentTimeMillis(), Collections.emptyMap()));
        assertEquals("Edellisessä sijoittelussa olleet jonot ovat kadonneet valintaperusteista, " +
                "minkä olisi pitänyt ilmetä jo ladatessa tietoja SijoitteluResourcessa. " +
                "Toisaalta tämän validoinnin ei pitäisi voida triggeröityä, " +
                "koska jonojen puuttumisen valintaperusteista pitäisi aiheuttaa se, " +
                "etteivät laskennan tuloksetkaan Tule tänne asti. " +
                "Vaikuttaa siis bugilta: " +
                "[Hakukohde hakukohde1 , jono \"Jono1\" (jono1 , prio 0)]", exception.getMessage());

        assertSijoitteluUsedSijoitteluajo(sijoitteluajoId);
    }

    @Test
    @Sql("peruuta_alemmat.sql")
    public void testPuuttuvaJonoKunPassivoituValintaperusteista() {
        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");

        sijoitteluService.sijoittele(haku, newHashSet("jono1", "jono2", "jono3"), newHashSet("jono1", "jono2", "jono3"), System.currentTimeMillis(), Collections.emptyMap());

        long sijoitteluajoId = captureSijoitteluajoForNextSijoittelu();

        removeJono(haku);

        sijoitteluService.sijoittele(haku, newHashSet("jono2", "jono3"), newHashSet("jono1", "jono2", "jono3"), System.currentTimeMillis(), Collections.emptyMap());

        assertSijoitteluUsedSijoitteluajo(sijoitteluajoId);
    }

    @Test
    @Sql("peruuta_alemmat.sql")
    public void testPuuttuvaJonoKunEiPassivoituValintaperusteista() {
        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");

        assertEquals(getValintatapaJonoOids(haku), newHashSet("jono1", "jono2", "jono3"));

        sijoitteluService.sijoittele(haku, newHashSet("jono1", "jono2", "jono3"), newHashSet("jono1", "jono2", "jono3"), System.currentTimeMillis(), Collections.emptyMap());

        long sijoitteluajoId = captureSijoitteluajoForNextSijoittelu();

        removeJono(haku);

        assertEquals(getValintatapaJonoOids(haku), newHashSet("jono2", "jono3"));

        Exception exception = Assertions.assertThrows(RuntimeException.class, () -> sijoitteluService.sijoittele(haku, newHashSet("jono1", "jono2", "jono3"), newHashSet("jono1", "jono2", "jono3"), System.currentTimeMillis(), Collections.emptyMap()));
        assertEquals("Edellisessä sijoittelussa olleet jonot puuttuvat sijoittelusta, vaikka ne ovat " +
                "valintaperusteissa yhä aktiivisina: [Hakukohde hakukohde1 , jono \"Jono1\" (jono1 , prio 0)]", exception.getMessage());

        assertSijoitteluUsedSijoitteluajo(sijoitteluajoId);
    }

    @Test
    @Sql("peruuta_alemmat.sql")
    public void testSivssnovSijoittelunRajaKopioidaanEdellisestaSijoittelusta() {
        Optional<JonosijaTieto> jonosijaTieto = Optional.of(new JonosijaTieto(6, 1, HakemuksenTila.VARALLA, Collections.singletonList("hakemus6")));
        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");
        haku.getHakukohteet().forEach(hk ->
            hk.getValinnanvaihe().forEach(vv -> {
                vv.getValintatapajonot().forEach(j -> {
                    if ("jono1".equals(j.getOid())) {
                        j.setVarasijat(4);
                    }
                });
            }));

        assertEquals(getValintatapaJonoOids(haku), newHashSet("jono1", "jono2", "jono3"));

        SijoitteluAjo previousAjo = new SijoitteluAjo();
        previousAjo.setSijoitteluajoId(1928391L);
        HakukohdeItem hakukohdeItem = new HakukohdeItem();
        String hakukohdeOid = haku.getHakukohteet().iterator().next().getOid();
        hakukohdeItem.setOid(hakukohdeOid);
        previousAjo.setHakukohteet(Collections.singletonList(hakukohdeItem));
        when(valintarekisteriService.getLatestSijoitteluajo("haku1")).thenReturn(previousAjo);

        Hakukohde previousHakukohde = new Hakukohde();
        previousHakukohde.setOid(hakukohdeItem.getOid());
        Valintatapajono previousJono = new Valintatapajono();
        previousJono.setOid("jono1");
        previousJono.setVarasijat(4);
        previousJono.setSivssnovSijoittelunVarasijataytonRajoitus(jonosijaTieto);
        previousHakukohde.setValintatapajonot(Collections.singletonList(previousJono));
        when(valintarekisteriService.getSijoitteluajonHakukohteet(previousAjo.getSijoitteluajoId(), "haku1")).thenReturn(Collections.singletonList(previousHakukohde));

        sijoitteluService.sijoittele(haku, newHashSet("jono2", "jono3"), newHashSet("jono1"), System.currentTimeMillis(), Collections.emptyMap());

        verify(valintarekisteriService, times(1)).tallennaSijoittelu(
                sijoitteluAjoArgumentCaptor.capture(),
                hakukohdeArgumentCaptor.capture(),
                valintatulosArgumentCaptor.capture());

        List<Hakukohde> hakukohteetFromSijoittelu = hakukohdeArgumentCaptor.getValue();

        MatcherAssert.assertThat(hakukohteetFromSijoittelu, Matchers.hasSize(3));
        Hakukohde hakukohdeFromSijoittelu = hakukohteetFromSijoittelu.stream().filter(hk -> hk.getOid().equals(previousHakukohde.getOid())).findFirst().get();
        assertNotSame(hakukohdeFromSijoittelu, previousHakukohde);

        MatcherAssert.assertThat(hakukohdeFromSijoittelu.getValintatapajonot(), Matchers.hasSize(1));
        Valintatapajono jonoFromSijoittelu = hakukohdeFromSijoittelu.getValintatapajonot().iterator().next();
        assertNotSame(jonoFromSijoittelu, previousJono);
        assertEquals(jonosijaTieto, jonoFromSijoittelu.getSivssnovSijoittelunVarasijataytonRajoitus());
    }

    private Long captureSijoitteluajoForNextSijoittelu() {
        verify(valintarekisteriService, times(1)).tallennaSijoittelu(
                sijoitteluAjoArgumentCaptor.capture(),
                hakukohdeArgumentCaptor.capture(),
                valintatulosArgumentCaptor.capture());

        SijoitteluAjo sijoitteluAjo = sijoitteluAjoArgumentCaptor.getValue();
        List<Hakukohde> hakukohteet = hakukohdeArgumentCaptor.getValue();
        List<Valintatulos> valintatulokset = createValintatuloksetFromHakukohteet(hakukohteet);

        when(valintarekisteriService.getLatestSijoitteluajo("haku1")).thenReturn(sijoitteluAjo);
        when(valintarekisteriService.getSijoitteluajonHakukohteet(sijoitteluAjo.getSijoitteluajoId(), "haku1")).thenReturn(hakukohteet);
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
        verify(valintarekisteriService, atLeastOnce()).getLatestSijoitteluajo("haku1");
        verify(valintarekisteriService, atLeastOnce()).getSijoitteluajonHakukohteet(sijoitteluajoId, "haku1");
        verify(valintarekisteriService, atLeastOnce()).getValintatulokset("haku1");
    }

    @Test
    @Sql("peruuta_alemmat.sql")
    public void testPeruunnaHyvaksyttyaYlemmatHakutoiveetKunAMKOPEHaku() {
        when(tarjontaIntegrationService.getHaku(anyString())).thenReturn(new Haku(
                "hakuOid",
                "haunkohdejoukko_12#1",
                "haunkohdejoukontarkenne_2#1",
                true,
                Instant.ofEpochMilli(1416866395389L),
                Instant.ofEpochMilli(1416866458888L),
                null,
                Instant.ofEpochMilli(14168663953898L),
                "",
                true
        ));

        HakuDTO haku = valintatietoService.haeValintatiedot("haku1");
        sijoitteluService.sijoittele(haku, newHashSet("jono1", "jono2", "jono3"), newHashSet("jono1", "jono2", "jono3"), System.currentTimeMillis(), Collections.emptyMap());

        verify(valintarekisteriService, times(1)).tallennaSijoittelu(
                sijoitteluAjoArgumentCaptor.capture(),
                hakukohdeArgumentCaptor.capture(),
                valintatulosArgumentCaptor.capture());

        SijoitteluAjo sijoitteluAjo = sijoitteluAjoArgumentCaptor.getValue();
        List<Hakukohde> hakukohteet = hakukohdeArgumentCaptor.getValue();
        List<Valintatulos> valintatulokset = createValintatuloksetFromHakukohteet(hakukohteet);

        Hakemus hakija3YlempiToive = hakukohteet.stream()
                .filter(h -> h.getOid().equals("hakukohde1")).findAny().get().getValintatapajonot().get(0)
                .getHakemukset().stream().filter(h -> h.getHakemusOid().equals("hakemus3")).findAny().get();
        Hakemus hakija3AlempiToive = hakukohteet.stream()
                .filter(h -> h.getOid().equals("hakukohde2")).findAny().get()
                .getValintatapajonot().get(0)
                .getHakemukset().stream().filter(h -> h.getHakemusOid().equals("hakemus3")).findAny().get();
        Hakemus hakija4YlempiToive = hakukohteet.stream()
                .filter(h -> h.getOid().equals("hakukohde1")).findAny().get().getValintatapajonot().get(0)
                .getHakemukset().stream().filter(h -> h.getHakemusOid().equals("hakemus4")).findAny().get();
        Hakemus hakija4AlempiToive = hakukohteet.stream()
                .filter(h -> h.getOid().equals("hakukohde2")).findAny().get()
                .getValintatapajonot().get(0)
                .getHakemukset().stream().filter(h -> h.getHakemusOid().equals("hakemus4")).findAny().get();
        assertEquals(HakemuksenTila.HYVAKSYTTY, hakija3AlempiToive.getTila());
        assertEquals(HakemuksenTila.PERUUNTUNUT, hakija3YlempiToive.getTila());
        assertEquals(HakemuksenTila.HYVAKSYTTY, hakija4AlempiToive.getTila());
        assertEquals(HakemuksenTila.PERUUNTUNUT, hakija4YlempiToive.getTila());

        valintatulokset.stream().filter(v ->
            "jono1".equals(v.getValintatapajonoOid()) && "hakemus2".equals(v.getHakemusOid()) && "hakukohde1".equals(v.getHakukohdeOid())
        ).forEach(v -> {
            v.setJulkaistavissa(true, "");
            v.setTila(ValintatuloksenTila.EI_VASTAANOTETTU_MAARA_AIKANA, "");
        });

        valintatulokset.stream().filter(v ->
                "jono2".equals(v.getValintatapajonoOid()) && "hakemus3".equals(v.getHakemusOid()) && "hakukohde2".equals(v.getHakukohdeOid())
        ).forEach(v -> {
            v.setJulkaistavissa(true, "");
            v.setTila(ValintatuloksenTila.KESKEN, "");
        });

        when(valintarekisteriService.getLatestSijoitteluajo("haku1")).thenReturn(sijoitteluAjo);
        when(valintarekisteriService.getSijoitteluajonHakukohteet(sijoitteluAjo.getSijoitteluajoId(), "haku1")).thenReturn(hakukohteet);
        when(valintarekisteriService.getValintatulokset("haku1")).thenReturn(valintatulokset);

        sijoitteluService.sijoittele(haku, newHashSet("jono1", "jono2", "jono3"), newHashSet("jono1", "jono2", "jono3"), System.currentTimeMillis(), Collections.emptyMap());

        verify(valintarekisteriService, times(2)).tallennaSijoittelu(
                sijoitteluAjoArgumentCaptor.capture(),
                hakukohdeArgumentCaptor.capture(),
                valintatulosArgumentCaptor.capture());

        List<Hakukohde> hakukohteet2 = hakukohdeArgumentCaptor.getAllValues().get(2);
        hakija3YlempiToive = hakukohteet2.stream()
                .filter(h -> h.getOid().equals("hakukohde1")).findAny().get().getValintatapajonot().get(0)
                .getHakemukset().stream().filter(h -> h.getHakemusOid().equals("hakemus3")).findAny().get();
        hakija3AlempiToive = hakukohteet2.stream()
                .filter(h -> h.getOid().equals("hakukohde2")).findAny().get()
                .getValintatapajonot().get(0)
                .getHakemukset().stream().filter(h -> h.getHakemusOid().equals("hakemus3")).findAny().get();
        hakija4YlempiToive = hakukohteet2.stream()
                .filter(h -> h.getOid().equals("hakukohde1")).findAny().get().getValintatapajonot().get(0)
                .getHakemukset().stream().filter(h -> h.getHakemusOid().equals("hakemus4")).findAny().get();
        hakija4AlempiToive = hakukohteet2.stream()
                .filter(h -> h.getOid().equals("hakukohde2")).findAny().get()
                .getValintatapajonot().get(0)
                .getHakemukset().stream().filter(h -> h.getHakemusOid().equals("hakemus4")).findAny().get();
        assertEquals(HakemuksenTila.HYVAKSYTTY, hakija3AlempiToive.getTila());
        assertEquals(HakemuksenTila.PERUUNTUNUT, hakija3YlempiToive.getTila());
        assertEquals(HakemuksenTila.PERUUNTUNUT, hakija4AlempiToive.getTila());
        assertEquals(HakemuksenTila.HYVAKSYTTY, hakija4YlempiToive.getTila());
    }

    @Test
    @Sql("valisijoittelu_hylkays.sql")
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
        return haku.getHakukohteet().stream()
          .flatMap(h -> h.getValinnanvaihe().stream()
            .flatMap(v -> v.getValintatapajonot().stream().map(ValintatapajonoDTO::getOid)))
          .collect(Collectors.toUnmodifiableSet());
    }

    private void removeJono(HakuDTO haku) {
        haku.getHakukohteet().forEach(hakukohdeDTO ->
            hakukohdeDTO.getValinnanvaihe().forEach(valintatietoValinnanvaiheDTO ->
                valintatietoValinnanvaiheDTO.getValintatapajonot().removeIf(j -> {
                    return j.getOid().equals("jono1");
                })));
    }
}
