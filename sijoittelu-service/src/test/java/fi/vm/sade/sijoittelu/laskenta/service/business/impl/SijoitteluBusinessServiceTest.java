package fi.vm.sade.sijoittelu.laskenta.service.business.impl;

import fi.vm.sade.authentication.business.service.Authorizer;
import fi.vm.sade.generic.service.exception.NotAuthorizedException;
import fi.vm.sade.sijoittelu.domain.IlmoittautumisTila;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.laskenta.external.resource.ValintaTulosServiceResource;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriArvoDTO;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriDTO;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.VastaanotettavuusDTO;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.sijoittelu.laskenta.service.it.TarjontaIntegrationService;
import fi.vm.sade.sijoittelu.tulos.dao.HakukohdeDao;
import fi.vm.sade.sijoittelu.tulos.dao.SijoitteluDao;
import fi.vm.sade.sijoittelu.tulos.dao.ValintatulosDao;
import fi.vm.sade.sijoittelu.tulos.roles.SijoitteluRole;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fi.vm.sade.sijoittelu.laskenta.external.resource.dto.VastaanotettavuusDTO.VastaanottoActionValue.VastaanotaSitovasti;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class SijoitteluBusinessServiceTest {
    final String HAKU_OID = TestDataGenerator.HAKU_OID;
    final String HAKUKOHDE_OID = TestDataGenerator.HAKUKOHDE_OID_1;
    final String VALINTATAPAJONO_OID = TestDataGenerator.VALINTATAPAJONO_OID_1;
    final String HAKEMUS_OID = TestDataGenerator.HAKEMUS_OID_1;
    final String HAKEMUS_OID_2 = TestDataGenerator.HAKEMUS_OID_2;
    final String SELITE = "selite";
    final String ROOT_ORG_OID = "rootOrgOid";
    final String MUOKKAAJA = "muokkaaja";

    private SijoitteluBusinessService sijoitteluBusinessService;
    private ValintatulosDao valintatulosDaoMock;
    private HakukohdeDao hakukohdeDao;
    private SijoitteluDao sijoitteluDao;
    private Authorizer authorizer;
    private TestDataGenerator testDataGenerator;
    private ValintaTulosServiceResource valintaTulosServiceResourceMock;
    private TarjontaIntegrationService tarjontaIntegrationService;


    @Before
    public void setUp() throws Exception {
        sijoitteluBusinessService = new SijoitteluBusinessService();

        valintatulosDaoMock = mock(ValintatulosDao.class);
        sijoitteluDao = mock(SijoitteluDao.class);
        hakukohdeDao = mock(HakukohdeDao.class);
        authorizer = mock(Authorizer.class);
        valintaTulosServiceResourceMock = mock(ValintaTulosServiceResource.class);
        tarjontaIntegrationService = mock(TarjontaIntegrationService.class);

        ReflectionTestUtils.setField(sijoitteluBusinessService, "valintatulosDao", valintatulosDaoMock);
        ReflectionTestUtils.setField(sijoitteluBusinessService, "sijoitteluDao", sijoitteluDao);
        ReflectionTestUtils.setField(sijoitteluBusinessService, "hakukohdeDao", hakukohdeDao);
        ReflectionTestUtils.setField(sijoitteluBusinessService, "authorizer", authorizer);
        ReflectionTestUtils.setField(sijoitteluBusinessService, "valintaTulosServiceResource", valintaTulosServiceResourceMock);
        ReflectionTestUtils.setField(sijoitteluBusinessService, "tarjontaIntegrationService", tarjontaIntegrationService);
        ReflectionTestUtils.setField(sijoitteluBusinessService, ROOT_ORG_OID, ROOT_ORG_OID);

        testDataGenerator = new TestDataGenerator();

    }

    @Test
    public void testSitovaVastaanottoOhjataanValintaTulosServicelle() throws Exception {
        when(sijoitteluDao.getSijoitteluByHakuOid(HAKU_OID)).thenReturn(Optional.of(testDataGenerator.generateTestData()));
        when(hakukohdeDao.getHakukohdeForSijoitteluajo(TestDataGenerator.SIJOITTELU_AJO_ID_2, HAKUKOHDE_OID)).thenReturn(testDataGenerator.createHakukohdes(1).get(0));
        mockVastaanotettavuus(HAKEMUS_OID, HAKEMUS_OID, HAKUKOHDE_OID, VastaanotaSitovasti);

        final boolean julkaistavissa = false;
        final boolean hyvaksyttyVarasijalta = false;
        final boolean hyvaksyPeruuntunut = false;
        Valintatulos v = new Valintatulos(
                HAKEMUS_OID,
                HAKEMUS_OID,
                HAKUKOHDE_OID,
                HAKU_OID,
                1,
                hyvaksyttyVarasijalta,
                IlmoittautumisTila.EI_TEHTY,
                julkaistavissa,
                ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI,
                VALINTATAPAJONO_OID);
        sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
                sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
                v, SELITE, MUOKKAAJA);

        ArgumentCaptor<String> hakijaOid = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> hakemusOid = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> hakukohdeOid = ArgumentCaptor.forClass(String.class);
        verify(valintaTulosServiceResourceMock).vastaanotettavuus(hakijaOid.capture(), hakemusOid.capture(), hakukohdeOid.capture());

        assertEquals(HAKEMUS_OID, hakijaOid.getValue());
        assertEquals(HAKEMUS_OID, hakemusOid.getValue());
        assertEquals(HAKUKOHDE_OID, hakukohdeOid.getValue());

        ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
        verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());

        Valintatulos valintatulos = argument.getValue();
        assertEquals(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, valintatulos.getTila());
        assertEquals(IlmoittautumisTila.EI_TEHTY, valintatulos.getIlmoittautumisTila());
        assertEquals(julkaistavissa, valintatulos.getJulkaistavissa());
        assertEquals(hyvaksyttyVarasijalta, valintatulos.getHyvaksyttyVarasijalta());
        assertEquals(hyvaksyPeruuntunut, valintatulos.getHyvaksyPeruuntunut());
        assertEquals(1, valintatulos.getLogEntries().size());
        assertEquals("tila: KESKEN -> VASTAANOTTANUT_SITOVASTI", valintatulos.getLogEntries().get(0).getMuutos());
        assertEquals(SELITE, valintatulos.getLogEntries().get(0).getSelite());
        assertEquals(MUOKKAAJA, valintatulos.getLogEntries().get(0).getMuokkaaja());
    }

    @Test
    public void testVaihdaTilaPeruuntuneeksi() throws Exception {

        Sijoittelu sijoittelu = testDataGenerator.generateTestData();

        when(sijoitteluDao.getSijoitteluByHakuOid(HAKU_OID))
                .thenReturn(Optional.of(sijoittelu));
        when(hakukohdeDao.getHakukohdeForSijoitteluajo(TestDataGenerator.SIJOITTELU_AJO_ID_2, HAKUKOHDE_OID))
                .thenReturn(testDataGenerator.createHakukohdes(1).get(0));
        doThrow(new NotAuthorizedException())
                .when(authorizer)
                .checkOrganisationAccess(ROOT_ORG_OID, SijoitteluRole.CRUD_ROLE);

        when(valintatulosDaoMock.loadValintatulos(HAKUKOHDE_OID, VALINTATAPAJONO_OID, HAKEMUS_OID))
                .thenReturn(getValintatulos(ValintatuloksenTila.KESKEN));

        boolean hyvaksyttyVarasijalta = false;
        boolean julkaistavissa = false;
        Valintatulos v = new Valintatulos(
                HAKEMUS_OID,
                HAKEMUS_OID,
                HAKUKOHDE_OID,
                HAKU_OID,
                1,
                hyvaksyttyVarasijalta,
                IlmoittautumisTila.EI_TEHTY,
                julkaistavissa,
                ValintatuloksenTila.PERUNUT,
                VALINTATAPAJONO_OID);
        sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
                sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
                v, SELITE, "");

        ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
        verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
        Valintatulos valintatulos = argument.getValue();
        assertEquals(ValintatuloksenTila.PERUNUT, valintatulos.getTila());
        assertEquals(IlmoittautumisTila.EI_TEHTY, valintatulos.getIlmoittautumisTila());
    }

    @Test
    public void testOphVaihdaTilaPeruuntuneeksiSuoraan() throws Exception {
        Sijoittelu sijoittelu = testDataGenerator.generateTestData();

        when(sijoitteluDao.getSijoitteluByHakuOid(HAKU_OID))
                .thenReturn(Optional.of(sijoittelu));
        when(hakukohdeDao.getHakukohdeForSijoitteluajo(TestDataGenerator.SIJOITTELU_AJO_ID_2, HAKUKOHDE_OID))
                .thenReturn(testDataGenerator.createHakukohdes(1).get(0));

        when(valintatulosDaoMock.loadValintatulos(HAKUKOHDE_OID, VALINTATAPAJONO_OID, HAKEMUS_OID))
                .thenReturn(getValintatulos(ValintatuloksenTila.KESKEN));

        boolean hyvaksyttyVarasijalta = false;
        boolean julkaistavissa = false;
        Valintatulos v = new Valintatulos(
                HAKEMUS_OID,
                HAKEMUS_OID,
                HAKUKOHDE_OID,
                HAKU_OID,
                1,
                hyvaksyttyVarasijalta,
                IlmoittautumisTila.EI_TEHTY,
                julkaistavissa,
                ValintatuloksenTila.PERUNUT,
                VALINTATAPAJONO_OID);
        sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
                sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
                v, SELITE, "");

        ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
        verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
        Valintatulos valintatulos = argument.getValue();
        assertEquals(ValintatuloksenTila.PERUNUT, valintatulos.getTila());
        assertEquals(IlmoittautumisTila.EI_TEHTY, valintatulos.getIlmoittautumisTila());
    }

    @Test
    public void testOphVaihdaVarallaOlevanTila() throws Exception {
        Sijoittelu sijoittelu = testDataGenerator.generateTestData();

        when(sijoitteluDao.getSijoitteluByHakuOid(HAKU_OID))
                .thenReturn(Optional.of(sijoittelu));
        when(hakukohdeDao.getHakukohdeForSijoitteluajo(TestDataGenerator.SIJOITTELU_AJO_ID_2, HAKUKOHDE_OID))
                .thenReturn(testDataGenerator.createHakukohdes(1).get(0));

        when(valintatulosDaoMock.loadValintatulos(HAKUKOHDE_OID, VALINTATAPAJONO_OID, HAKEMUS_OID_2))
                .thenReturn(getValintatulos(ValintatuloksenTila.KESKEN));

        mockVastaanotettavuus(HAKEMUS_OID_2, HAKEMUS_OID_2, HAKUKOHDE_OID, VastaanotaSitovasti);

        boolean hyvaksyttyVarasijalta = false;
        boolean julkaistavissa = false;
        Valintatulos v = new Valintatulos(
                HAKEMUS_OID_2,
                HAKEMUS_OID_2,
                HAKUKOHDE_OID,
                HAKU_OID,
                2,
                hyvaksyttyVarasijalta,
                IlmoittautumisTila.EI_TEHTY,
                julkaistavissa,
                ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI,
                VALINTATAPAJONO_OID);
        sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
                sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
                v, SELITE, "");

        ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
        verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
        Valintatulos valintatulos = argument.getValue();
        assertEquals(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, valintatulos.getTila());
        assertEquals(IlmoittautumisTila.EI_TEHTY, valintatulos.getIlmoittautumisTila());
    }

    private Valintatulos getValintatulos(ValintatuloksenTila tila) {
        Valintatulos valintatulos = new Valintatulos();
        valintatulos.setHakemusOid(HAKEMUS_OID, "");
        valintatulos.setHakijaOid(HAKEMUS_OID, "");
        valintatulos.setHakukohdeOid(HAKUKOHDE_OID, "");
        valintatulos.setHakuOid(HAKU_OID, "");
        valintatulos.setHakutoive(0, "");
        valintatulos.setValintatapajonoOid(VALINTATAPAJONO_OID, "");
        valintatulos.setTila(tila, "");
        return valintatulos;
    }

    private Valintatulos getValintatulos(ValintatuloksenTila tila, boolean hyvaksyPeruuntunut) {
        Valintatulos valintatulos = getValintatulos(tila);
        valintatulos.setHyvaksyPeruuntunut(hyvaksyPeruuntunut, "");
        return valintatulos;
    }

    @Test
    public void testVaihdaIlmoittautumisTilaLasnaKokoLukuvuosi() throws Exception {
        Sijoittelu sijoittelu = testDataGenerator.generateTestData();

        when(sijoitteluDao.getSijoitteluByHakuOid(HAKU_OID))
                .thenReturn(Optional.of(sijoittelu));
        when(hakukohdeDao.getHakukohdeForSijoitteluajo(TestDataGenerator.SIJOITTELU_AJO_ID_2, HAKUKOHDE_OID))
                .thenReturn(testDataGenerator.createHakukohdes(1).get(0));
        doThrow(new NotAuthorizedException())
                .when(authorizer)
                .checkOrganisationAccess(ROOT_ORG_OID, SijoitteluRole.CRUD_ROLE);

        when(valintatulosDaoMock.loadValintatulos(HAKUKOHDE_OID, VALINTATAPAJONO_OID, HAKEMUS_OID))
                .thenReturn(getValintatulos(ValintatuloksenTila.KESKEN));

        mockVastaanotettavuus(HAKEMUS_OID, HAKEMUS_OID, HAKUKOHDE_OID, VastaanotaSitovasti);

        boolean hyvaksyttyVarasijalta = false;
        boolean julkaistavissa = false;
        Valintatulos v = new Valintatulos(
                HAKEMUS_OID,
                HAKEMUS_OID,
                HAKUKOHDE_OID,
                HAKU_OID,
                1,
                hyvaksyttyVarasijalta,
                IlmoittautumisTila.LASNA_KOKO_LUKUVUOSI,
                julkaistavissa,
                ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI,
                VALINTATAPAJONO_OID);
        sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
                sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
                v, SELITE, "");

        ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
        verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
        Valintatulos valintatulos = argument.getValue();
        assertEquals(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, valintatulos.getTila());
        assertEquals(IlmoittautumisTila.LASNA_KOKO_LUKUVUOSI, valintatulos.getIlmoittautumisTila());
    }

    @Test
    public void testVaihdaIlmoittautumisTilaPoissaKokoLukuvuosi() throws Exception {
        Sijoittelu sijoittelu = testDataGenerator.generateTestData();

        when(sijoitteluDao.getSijoitteluByHakuOid(HAKU_OID))
                .thenReturn(Optional.of(sijoittelu));
        when(hakukohdeDao.getHakukohdeForSijoitteluajo(TestDataGenerator.SIJOITTELU_AJO_ID_2, HAKUKOHDE_OID))
                .thenReturn(testDataGenerator.createHakukohdes(1).get(0));
        doThrow(new NotAuthorizedException())
                .when(authorizer)
                .checkOrganisationAccess(ROOT_ORG_OID, SijoitteluRole.CRUD_ROLE);

        when(valintatulosDaoMock.loadValintatulos(HAKUKOHDE_OID, VALINTATAPAJONO_OID, HAKEMUS_OID))
                .thenReturn(getValintatulos(ValintatuloksenTila.KESKEN));

        mockVastaanotettavuus(HAKEMUS_OID, HAKEMUS_OID, HAKUKOHDE_OID, VastaanotaSitovasti);

        boolean hyvaksyttyVarasijalta = false;
        boolean julkaistavissa = false;
        Valintatulos v = new Valintatulos(
                HAKEMUS_OID,
                HAKEMUS_OID,
                HAKUKOHDE_OID,
                HAKU_OID,
                1,
                hyvaksyttyVarasijalta,
                IlmoittautumisTila.POISSA_KOKO_LUKUVUOSI,
                julkaistavissa,
                ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI,
                VALINTATAPAJONO_OID);
        sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
                sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
                v, SELITE, "");

        ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
        verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
        Valintatulos valintatulos = argument.getValue();
        assertEquals(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, valintatulos.getTila());
        assertEquals(IlmoittautumisTila.POISSA_KOKO_LUKUVUOSI, valintatulos.getIlmoittautumisTila());
    }

    @Test
    public void testVaihdaIlmoittautumisTilaEiIlmoittautunut() throws Exception {
        Sijoittelu sijoittelu = testDataGenerator.generateTestData();

        when(sijoitteluDao.getSijoitteluByHakuOid(HAKU_OID))
                .thenReturn(Optional.of(sijoittelu));
        when(hakukohdeDao.getHakukohdeForSijoitteluajo(TestDataGenerator.SIJOITTELU_AJO_ID_2, HAKUKOHDE_OID))
                .thenReturn(testDataGenerator.createHakukohdes(1).get(0));
        doThrow(new NotAuthorizedException())
                .when(authorizer)
                .checkOrganisationAccess(ROOT_ORG_OID, SijoitteluRole.CRUD_ROLE);

        when(valintatulosDaoMock.loadValintatulos(HAKUKOHDE_OID, VALINTATAPAJONO_OID, HAKEMUS_OID))
                .thenReturn(getValintatulos(ValintatuloksenTila.KESKEN));

        mockVastaanotettavuus(HAKEMUS_OID, HAKEMUS_OID, HAKUKOHDE_OID, VastaanotaSitovasti);

        boolean hyvaksyttyVarasijalta = false;
        boolean julkaistavissa = false;
        Valintatulos v = new Valintatulos(
                HAKEMUS_OID,
                HAKEMUS_OID,
                HAKUKOHDE_OID,
                HAKU_OID,
                1,
                hyvaksyttyVarasijalta,
                IlmoittautumisTila.EI_ILMOITTAUTUNUT,
                julkaistavissa,
                ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI,
                VALINTATAPAJONO_OID);
        sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
                sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
                v, SELITE, "");

        ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
        verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
        Valintatulos valintatulos = argument.getValue();
        assertEquals(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, valintatulos.getTila());
        assertEquals(IlmoittautumisTila.EI_ILMOITTAUTUNUT, valintatulos.getIlmoittautumisTila());
    }

    @Test
    public void testVaihdaIlmoittautumisTilaLasnaSyksy() throws Exception {
        Sijoittelu sijoittelu = testDataGenerator.generateTestData();

        when(sijoitteluDao.getSijoitteluByHakuOid(HAKU_OID))
                .thenReturn(Optional.of(sijoittelu));
        when(hakukohdeDao.getHakukohdeForSijoitteluajo(TestDataGenerator.SIJOITTELU_AJO_ID_2, HAKUKOHDE_OID))
                .thenReturn(testDataGenerator.createHakukohdes(1).get(0));
        doThrow(new NotAuthorizedException())
                .when(authorizer)
                .checkOrganisationAccess(ROOT_ORG_OID, SijoitteluRole.CRUD_ROLE);

        when(valintatulosDaoMock.loadValintatulos(HAKUKOHDE_OID, VALINTATAPAJONO_OID, HAKEMUS_OID))
                .thenReturn(getValintatulos(ValintatuloksenTila.KESKEN));

        mockVastaanotettavuus(HAKEMUS_OID, HAKEMUS_OID, HAKUKOHDE_OID, VastaanotaSitovasti);

        boolean hyvaksyttyVarasijalta = false;
        boolean julkaistavissa = false;
        Valintatulos v = new Valintatulos(
                HAKEMUS_OID,
                HAKEMUS_OID,
                HAKUKOHDE_OID,
                HAKU_OID,
                1,
                hyvaksyttyVarasijalta,
                IlmoittautumisTila.LASNA_SYKSY,
                julkaistavissa,
                ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI,
                VALINTATAPAJONO_OID);
        sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
                sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
                v, SELITE, "");

        ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
        verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
        Valintatulos valintatulos = argument.getValue();
        assertEquals(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, valintatulos.getTila());
        assertEquals(IlmoittautumisTila.LASNA_SYKSY, valintatulos.getIlmoittautumisTila());
    }

    @Test
    public void testVaihdaIlmoittautumisTilaPoissaSyksy() throws Exception {
        Sijoittelu sijoittelu = testDataGenerator.generateTestData();

        when(sijoitteluDao.getSijoitteluByHakuOid(HAKU_OID))
                .thenReturn(Optional.of(sijoittelu));
        when(hakukohdeDao.getHakukohdeForSijoitteluajo(TestDataGenerator.SIJOITTELU_AJO_ID_2, HAKUKOHDE_OID))
                .thenReturn(testDataGenerator.createHakukohdes(1).get(0));
        doThrow(new NotAuthorizedException())
                .when(authorizer)
                .checkOrganisationAccess(ROOT_ORG_OID, SijoitteluRole.CRUD_ROLE);

        when(valintatulosDaoMock.loadValintatulos(HAKUKOHDE_OID, VALINTATAPAJONO_OID, HAKEMUS_OID))
                .thenReturn(getValintatulos(ValintatuloksenTila.KESKEN));

        mockVastaanotettavuus(HAKEMUS_OID, HAKEMUS_OID, HAKUKOHDE_OID, VastaanotaSitovasti);

        boolean hyvaksyttyVarasijalta = false;
        boolean julkaistavissa = false;
        Valintatulos v = new Valintatulos(
                HAKEMUS_OID,
                HAKEMUS_OID,
                HAKUKOHDE_OID,
                HAKU_OID,
                1,
                hyvaksyttyVarasijalta,
                IlmoittautumisTila.POISSA_SYKSY,
                julkaistavissa,
                ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI,
                VALINTATAPAJONO_OID);
        sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
                sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
                v, SELITE, "");

        ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
        verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
        Valintatulos valintatulos = argument.getValue();
        assertEquals(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, valintatulos.getTila());
        assertEquals(IlmoittautumisTila.POISSA_SYKSY, valintatulos.getIlmoittautumisTila());
    }

    @Test
    public void testVaihdaIlmoittautumisTilaLasna() throws Exception {
        Sijoittelu sijoittelu = testDataGenerator.generateTestData();

        when(sijoitteluDao.getSijoitteluByHakuOid(HAKU_OID))
                .thenReturn(Optional.of(sijoittelu));
        when(hakukohdeDao.getHakukohdeForSijoitteluajo(TestDataGenerator.SIJOITTELU_AJO_ID_2, HAKUKOHDE_OID))
                .thenReturn(testDataGenerator.createHakukohdes(1).get(0));
        doThrow(new NotAuthorizedException())
                .when(authorizer)
                .checkOrganisationAccess(ROOT_ORG_OID, SijoitteluRole.CRUD_ROLE);

        when(valintatulosDaoMock.loadValintatulos(HAKUKOHDE_OID, VALINTATAPAJONO_OID, HAKEMUS_OID))
                .thenReturn(getValintatulos(ValintatuloksenTila.KESKEN));

        mockVastaanotettavuus(HAKEMUS_OID, HAKEMUS_OID, HAKUKOHDE_OID, VastaanotaSitovasti);

        boolean hyvaksyttyVarasijalta = false;
        boolean julkaistavissa = false;
        Valintatulos v = new Valintatulos(
                HAKEMUS_OID,
                HAKEMUS_OID,
                HAKUKOHDE_OID,
                HAKU_OID,
                1,
                hyvaksyttyVarasijalta,
                IlmoittautumisTila.LASNA,
                julkaistavissa,
                ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI,
                VALINTATAPAJONO_OID);
        sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
                sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
                v, SELITE, "");

        ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
        verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
        Valintatulos valintatulos = argument.getValue();
        assertEquals(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, valintatulos.getTila());
        assertEquals(IlmoittautumisTila.LASNA, valintatulos.getIlmoittautumisTila());
    }

    @Test
    public void testVaihdaIlmoittautumisTilaPoissa() throws Exception {
        Sijoittelu sijoittelu = testDataGenerator.generateTestData();

        when(sijoitteluDao.getSijoitteluByHakuOid(HAKU_OID))
                .thenReturn(Optional.of(sijoittelu));
        when(hakukohdeDao.getHakukohdeForSijoitteluajo(TestDataGenerator.SIJOITTELU_AJO_ID_2, HAKUKOHDE_OID))
                .thenReturn(testDataGenerator.createHakukohdes(1).get(0));
        doThrow(new NotAuthorizedException())
                .when(authorizer)
                .checkOrganisationAccess(ROOT_ORG_OID, SijoitteluRole.CRUD_ROLE);

        when(valintatulosDaoMock.loadValintatulos(HAKUKOHDE_OID, VALINTATAPAJONO_OID, HAKEMUS_OID))
                .thenReturn(getValintatulos(ValintatuloksenTila.KESKEN));

        mockVastaanotettavuus(HAKEMUS_OID, HAKEMUS_OID, HAKUKOHDE_OID, VastaanotaSitovasti);

        boolean hyvaksyttyVarasijalta = false;
        boolean julkaistavissa = false;
        Valintatulos v = new Valintatulos(
                HAKEMUS_OID,
                HAKEMUS_OID,
                HAKUKOHDE_OID,
                HAKU_OID,
                1,
                hyvaksyttyVarasijalta,
                IlmoittautumisTila.POISSA,
                julkaistavissa,
                ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI,
                VALINTATAPAJONO_OID);
        sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
                sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
                v, SELITE, "");

        ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
        verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
        Valintatulos valintatulos = argument.getValue();
        assertEquals(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, valintatulos.getTila());
        assertEquals(IlmoittautumisTila.POISSA, valintatulos.getIlmoittautumisTila());
    }

    @Test
    public void testHyvaksyPeruuntunutCanBeSet() {
        mockVastaanotettavuus(HAKEMUS_OID, HAKEMUS_OID, HAKUKOHDE_OID, VastaanotaSitovasti);

        assertTrue(vaihdaHakemuksenHyvaksyPeruuntunut(true).getHyvaksyPeruuntunut());
    }

    @Test
    public void testHyvaksyPeruuntunutCanBeUnset() {
        mockVastaanotettavuus(HAKEMUS_OID, HAKEMUS_OID, HAKUKOHDE_OID, VastaanotaSitovasti);

        assertFalse(vaihdaHakemuksenHyvaksyPeruuntunut(false).getHyvaksyPeruuntunut());
    }

    private void mockVastaanotettavuus(String hakijaOid, String hakemusOid, String hakukohdeOid, VastaanotettavuusDTO.VastaanottoActionValue... vastaanotettavuudet) {
        when(valintaTulosServiceResourceMock.vastaanotettavuus(hakijaOid, hakemusOid, hakukohdeOid)).thenReturn(new VastaanotettavuusDTO() {{
            setAllowedActions(Arrays.asList(vastaanotettavuudet).stream().<VastaanottoAction>map(vastaanottoActionValue -> new VastaanottoAction() {{
                setAction(vastaanottoActionValue);
            }}).collect(Collectors.toList()));
        }});
    }

    @Test(expected = NotAuthorizedException.class)
    public void testJulkaistavissaCannotBeSetIfValintaesitysNotJulkaistavissa() {
        ParametriDTO params = new ParametriDTO();
        params.setPH_VEH(new ParametriArvoDTO(new Date().getTime() + 10000));
        doThrow(new NotAuthorizedException())
                .when(authorizer)
                .checkOrganisationAccess(SijoitteluBusinessService.OPH_OID, SijoitteluRole.CRUD_ROLE);
        runValintaesitysJulkaistavissaTest(params);
    }

    @Test
    public void testJulkaistavissaCanBeSetIfOPHEvenIfValintaesitysNotJulkaistavissa() {
        when(valintatulosDaoMock.loadValintatulos(HAKUKOHDE_OID, VALINTATAPAJONO_OID, HAKEMUS_OID))
                .thenReturn(getValintatulos(ValintatuloksenTila.KESKEN));
        ParametriDTO params = new ParametriDTO();
        params.setPH_VEH(new ParametriArvoDTO(new Date().getTime() + 10000));
        runValintaesitysJulkaistavissaTest(params);
    }

    @Test
    public void testJulkaistavissaCanBeSetIfValintaesitysIsJulkaistavissa() {
        when(valintatulosDaoMock.loadValintatulos(HAKUKOHDE_OID, VALINTATAPAJONO_OID, HAKEMUS_OID))
                .thenReturn(getValintatulos(ValintatuloksenTila.KESKEN));
        ParametriDTO params = new ParametriDTO();
        params.setPH_VEH(new ParametriArvoDTO(new Date().getTime() - 10000));
        runValintaesitysJulkaistavissaTest(params);
    }

    private void runValintaesitysJulkaistavissaTest(ParametriDTO params) {
        when(valintatulosDaoMock.loadValintatulos(HAKUKOHDE_OID, VALINTATAPAJONO_OID, HAKEMUS_OID))
                .thenReturn(getValintatulos(ValintatuloksenTila.KESKEN));
        when(tarjontaIntegrationService.getHaunParametrit(HAKU_OID))
                .thenReturn(params);
        when(tarjontaIntegrationService.getHaunKohdejoukko(HAKU_OID))
                .thenReturn(Optional.of("haunkohdejoukko_xx"));
        Valintatulos v = new Valintatulos(
                HAKEMUS_OID,
                HAKEMUS_OID,
                HAKUKOHDE_OID,
                HAKU_OID,
                1,
                false,
                IlmoittautumisTila.EI_TEHTY,
                true,
                ValintatuloksenTila.KESKEN,
                VALINTATAPAJONO_OID);
        sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID, testDataGenerator.createHakukohdes(1).get(0), v, SELITE, "");
    }

    private Valintatulos vaihdaHakemuksenHyvaksyPeruuntunut(boolean hyvaksyPeruuntunut) {
        when(valintatulosDaoMock.loadValintatulos(HAKUKOHDE_OID, VALINTATAPAJONO_OID, HAKEMUS_OID))
                .thenReturn(getValintatulos(ValintatuloksenTila.KESKEN));
        boolean hyvaksyttyVarasijalta = false;
        boolean julkaistavissa = false;
        Valintatulos v = new Valintatulos(
                HAKEMUS_OID,
                HAKEMUS_OID,
                HAKUKOHDE_OID,
                HAKU_OID,
                1,
                hyvaksyttyVarasijalta,
                IlmoittautumisTila.POISSA,
                julkaistavissa,
                ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI,
                VALINTATAPAJONO_OID);
        v.setHyvaksyPeruuntunut(hyvaksyPeruuntunut, "");
        sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
                testDataGenerator.createHakukohdes(1).get(0),
                v, SELITE, "");
        ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
        verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
        return argument.getValue();
    }
}
