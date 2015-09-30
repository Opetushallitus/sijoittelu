package fi.vm.sade.sijoittelu.laskenta.service.business.impl;

import fi.vm.sade.authentication.business.service.Authorizer;
import fi.vm.sade.generic.service.exception.NotAuthorizedException;
import fi.vm.sade.sijoittelu.domain.IlmoittautumisTila;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.laskenta.external.resource.ValintaTulosServiceResource;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.VastaanottoDTO;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.sijoittelu.tulos.dao.HakukohdeDao;
import fi.vm.sade.sijoittelu.tulos.dao.SijoitteluDao;
import fi.vm.sade.sijoittelu.tulos.dao.ValintatulosDao;
import fi.vm.sade.sijoittelu.tulos.roles.SijoitteluRole;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

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


    @Before
    public void setUp() throws Exception {
        sijoitteluBusinessService = new SijoitteluBusinessService();

        valintatulosDaoMock = mock(ValintatulosDao.class);
        sijoitteluDao = mock(SijoitteluDao.class);
        hakukohdeDao = mock(HakukohdeDao.class);
        authorizer = mock(Authorizer.class);
        valintaTulosServiceResourceMock = mock(ValintaTulosServiceResource.class);

        ReflectionTestUtils.setField(sijoitteluBusinessService, "valintatulosDao", valintatulosDaoMock);
        ReflectionTestUtils.setField(sijoitteluBusinessService, "sijoitteluDao", sijoitteluDao);
        ReflectionTestUtils.setField(sijoitteluBusinessService, "hakukohdeDao", hakukohdeDao);
        ReflectionTestUtils.setField(sijoitteluBusinessService, "authorizer", authorizer);
        ReflectionTestUtils.setField(sijoitteluBusinessService, "valintaTulosServiceResource", valintaTulosServiceResourceMock);
        ReflectionTestUtils.setField(sijoitteluBusinessService, ROOT_ORG_OID, ROOT_ORG_OID);

        testDataGenerator = new TestDataGenerator();

    }

    @Test
    public void testSitovaVastaanottoOhjataanValintaTulosServicelle() throws Exception {
        when(sijoitteluDao.getSijoitteluByHakuOid(HAKU_OID)).thenReturn(Optional.of(testDataGenerator.generateTestData()));
        when(hakukohdeDao.getHakukohdeForSijoitteluajo(TestDataGenerator.SIJOITTELU_AJO_ID_2, HAKUKOHDE_OID)).thenReturn(testDataGenerator.createHakukohdes(1).get(0));

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

        ArgumentCaptor<String> hakuOid = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> hakemusOid = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> hakukohdeOid = ArgumentCaptor.forClass(String.class);
        verify(valintaTulosServiceResourceMock).vastaanotettavuus(hakuOid.capture(), hakemusOid.capture(), hakukohdeOid.capture());

        assertEquals(HAKU_OID, hakuOid.getValue());
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
    public void testVaihdaTilaIlmoitetuksi() throws Exception {

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
                ValintatuloksenTila.ILMOITETTU,
                VALINTATAPAJONO_OID);
        sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
                sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
                v, SELITE, "");

        ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
        verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
        Valintatulos valintatulos = argument.getValue();
        assertEquals(ValintatuloksenTila.ILMOITETTU, valintatulos.getTila());
        assertEquals(IlmoittautumisTila.EI_TEHTY, valintatulos.getIlmoittautumisTila());
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
                .thenReturn(getValintatulos(ValintatuloksenTila.ILMOITETTU));

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
                ValintatuloksenTila.VASTAANOTTANUT,
                VALINTATAPAJONO_OID);
        sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
                sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
                v, SELITE, "");

        ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
        verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
        Valintatulos valintatulos = argument.getValue();
        assertEquals(ValintatuloksenTila.VASTAANOTTANUT, valintatulos.getTila());
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
                .thenReturn(getValintatulos(ValintatuloksenTila.ILMOITETTU));

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
                ValintatuloksenTila.VASTAANOTTANUT,
                VALINTATAPAJONO_OID);
        sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
                sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
                v, SELITE, "");

        ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
        verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
        Valintatulos valintatulos = argument.getValue();
        assertEquals(ValintatuloksenTila.VASTAANOTTANUT, valintatulos.getTila());
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
                .thenReturn(getValintatulos(ValintatuloksenTila.ILMOITETTU));

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
                ValintatuloksenTila.VASTAANOTTANUT,
                VALINTATAPAJONO_OID);
        sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
                sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
                v, SELITE, "");

        ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
        verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
        Valintatulos valintatulos = argument.getValue();
        assertEquals(ValintatuloksenTila.VASTAANOTTANUT, valintatulos.getTila());
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
                .thenReturn(getValintatulos(ValintatuloksenTila.ILMOITETTU));

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
                ValintatuloksenTila.VASTAANOTTANUT,
                VALINTATAPAJONO_OID);
        sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
                sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
                v, SELITE, "");

        ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
        verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
        Valintatulos valintatulos = argument.getValue();
        assertEquals(ValintatuloksenTila.VASTAANOTTANUT, valintatulos.getTila());
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
                .thenReturn(getValintatulos(ValintatuloksenTila.ILMOITETTU));

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
                ValintatuloksenTila.VASTAANOTTANUT,
                VALINTATAPAJONO_OID);
        sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
                sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
                v, SELITE, "");

        ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
        verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
        Valintatulos valintatulos = argument.getValue();
        assertEquals(ValintatuloksenTila.VASTAANOTTANUT, valintatulos.getTila());
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
                .thenReturn(getValintatulos(ValintatuloksenTila.ILMOITETTU));

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
                ValintatuloksenTila.VASTAANOTTANUT,
                VALINTATAPAJONO_OID);
        sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
                sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
                v, SELITE, "");

        ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
        verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
        Valintatulos valintatulos = argument.getValue();
        assertEquals(ValintatuloksenTila.VASTAANOTTANUT, valintatulos.getTila());
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
                .thenReturn(getValintatulos(ValintatuloksenTila.ILMOITETTU));

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
                ValintatuloksenTila.VASTAANOTTANUT,
                VALINTATAPAJONO_OID);
        sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
                sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
                v, SELITE, "");

        ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
        verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
        Valintatulos valintatulos = argument.getValue();
        assertEquals(ValintatuloksenTila.VASTAANOTTANUT, valintatulos.getTila());
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
                .thenReturn(getValintatulos(ValintatuloksenTila.ILMOITETTU));

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
                ValintatuloksenTila.VASTAANOTTANUT,
                VALINTATAPAJONO_OID);
        sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
                sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
                v, SELITE, "");

        ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
        verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
        Valintatulos valintatulos = argument.getValue();
        assertEquals(ValintatuloksenTila.VASTAANOTTANUT, valintatulos.getTila());
        assertEquals(IlmoittautumisTila.POISSA, valintatulos.getIlmoittautumisTila());
    }

    @Test
    public void testHyvaksyPeruuntunutCanBeSet() {
        assertTrue(vaihdaHakemuksenHyvaksyPeruuntunut(true).getHyvaksyPeruuntunut());
    }

    @Test
    public void testHyvaksyPeruuntunutCanBeUnset() {
        assertFalse(vaihdaHakemuksenHyvaksyPeruuntunut(false).getHyvaksyPeruuntunut());
    }

    private Valintatulos vaihdaHakemuksenHyvaksyPeruuntunut(boolean hyvaksyPeruuntunut) {
        when(valintatulosDaoMock.loadValintatulos(HAKUKOHDE_OID, VALINTATAPAJONO_OID, HAKEMUS_OID))
                .thenReturn(getValintatulos(ValintatuloksenTila.ILMOITETTU));
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
                ValintatuloksenTila.VASTAANOTTANUT,
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
