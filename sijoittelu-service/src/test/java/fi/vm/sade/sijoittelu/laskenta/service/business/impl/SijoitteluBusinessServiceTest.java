package fi.vm.sade.sijoittelu.laskenta.service.business.impl;

import fi.vm.sade.authentication.business.service.Authorizer;
import fi.vm.sade.generic.service.exception.NotAuthorizedException;
import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.sijoittelu.laskenta.external.resource.ValintaTulosServiceResource;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriArvoDTO;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriDTO;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.sijoittelu.laskenta.service.it.TarjontaIntegrationService;
import fi.vm.sade.sijoittelu.tulos.dao.HakukohdeDao;
import fi.vm.sade.sijoittelu.tulos.dao.SijoitteluDao;
import fi.vm.sade.sijoittelu.tulos.dao.ValintatulosDao;
import fi.vm.sade.sijoittelu.tulos.roles.SijoitteluRole;
import fi.vm.sade.sijoittelu.tulos.service.RaportointiService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;

import java.util.Date;
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
    final String MUOKKAAJA = "muokkaaja";

    private SijoitteluBusinessService sijoitteluBusinessService;
    private ValintatulosDao valintatulosDaoMock;
    private HakukohdeDao hakukohdeDao;
    private SijoitteluDao sijoitteluDao;
    private Authorizer authorizer;
    private TestDataGenerator testDataGenerator;
    private TarjontaIntegrationService tarjontaIntegrationService;
    private RaportointiService raportointiService;


    @Before
    public void setUp() throws Exception {
        valintatulosDaoMock = mock(ValintatulosDao.class);
        sijoitteluDao = mock(SijoitteluDao.class);
        hakukohdeDao = mock(HakukohdeDao.class);
        authorizer = mock(Authorizer.class);
        tarjontaIntegrationService = mock(TarjontaIntegrationService.class);
        ValintaTulosServiceResource valintaTulosServiceResourceMock = mock(ValintaTulosServiceResource.class);
        raportointiService = mock(RaportointiService.class);

        sijoitteluBusinessService = new SijoitteluBusinessService(1,1,valintatulosDaoMock,hakukohdeDao,sijoitteluDao, raportointiService, null, authorizer,null,null,tarjontaIntegrationService,valintaTulosServiceResourceMock);
        testDataGenerator = new TestDataGenerator();

    }

    @Test
    public void testVaihdaTilaPeruuntuneeksi() throws Exception {

        Sijoittelu sijoittelu = testDataGenerator.generateTestData();

        when(raportointiService.cachedLatestSijoitteluAjoForHakukohde(HAKU_OID, HAKUKOHDE_OID))
                .thenReturn(Optional.of(sijoittelu.getLatestSijoitteluajo()));
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
                false,
                VALINTATAPAJONO_OID,
                new Date());
        sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
                sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
                v, SELITE, "");

        ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
        verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
        Valintatulos valintatulos = argument.getValue();
        assertEquals(IlmoittautumisTila.EI_TEHTY, valintatulos.getIlmoittautumisTila());
    }

    @Test
    public void testOphVaihdaTilaPeruuntuneeksiSuoraan() throws Exception {
        Sijoittelu sijoittelu = testDataGenerator.generateTestData();

        when(raportointiService.cachedLatestSijoitteluAjoForHakukohde(HAKU_OID, HAKUKOHDE_OID))
                .thenReturn(Optional.of(sijoittelu.getLatestSijoitteluajo()));
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
                false,
                VALINTATAPAJONO_OID,
                new Date());
        sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
                sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
                v, SELITE, "");

        ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
        verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
        Valintatulos valintatulos = argument.getValue();
        assertEquals(IlmoittautumisTila.EI_TEHTY, valintatulos.getIlmoittautumisTila());
    }

    @Test
    public void testOphVaihdaVarallaOlevanTila() throws Exception {
        Sijoittelu sijoittelu = testDataGenerator.generateTestData();

        when(raportointiService.cachedLatestSijoitteluAjoForHakukohde(HAKU_OID, HAKUKOHDE_OID))
                .thenReturn(Optional.of(sijoittelu.getLatestSijoitteluajo()));
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
                ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI,
                false,
                VALINTATAPAJONO_OID,
                new Date());
        sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
                sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
                v, SELITE, "");

        ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
        verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
        Valintatulos valintatulos = argument.getValue();
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

    @Test
    public void testVaihdaIlmoittautumisTilaLasnaKokoLukuvuosi() throws Exception {
        Sijoittelu sijoittelu = testDataGenerator.generateTestData();

        when(raportointiService.cachedLatestSijoitteluAjoForHakukohde(HAKU_OID, HAKUKOHDE_OID))
                .thenReturn(Optional.of(sijoittelu.getLatestSijoitteluajo()));

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
                IlmoittautumisTila.LASNA_KOKO_LUKUVUOSI,
                julkaistavissa,
                ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI,
                false,
                VALINTATAPAJONO_OID,
                new Date());
        sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
                sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
                v, SELITE, "");

        ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
        verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
        Valintatulos valintatulos = argument.getValue();
        assertEquals(IlmoittautumisTila.LASNA_KOKO_LUKUVUOSI, valintatulos.getIlmoittautumisTila());
    }

    @Test
    public void testVaihdaIlmoittautumisTilaPoissaKokoLukuvuosi() throws Exception {
        Sijoittelu sijoittelu = testDataGenerator.generateTestData();

        when(raportointiService.cachedLatestSijoitteluAjoForHakukohde(HAKU_OID, HAKUKOHDE_OID))
                .thenReturn(Optional.of(sijoittelu.getLatestSijoitteluajo()));
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
                IlmoittautumisTila.POISSA_KOKO_LUKUVUOSI,
                julkaistavissa,
                ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI,
                false,
                VALINTATAPAJONO_OID,
                new Date());
        sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
                sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
                v, SELITE, "");

        ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
        verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
        Valintatulos valintatulos = argument.getValue();
        assertEquals(IlmoittautumisTila.POISSA_KOKO_LUKUVUOSI, valintatulos.getIlmoittautumisTila());
    }

    @Test
    public void testAsetaJononValintaehdotusHyväksytyksi() throws Exception {
        Sijoittelu sijoittelu = testDataGenerator.generateTestData();

        when(raportointiService.cachedLatestSijoitteluAjoForHakukohde(HAKU_OID, HAKUKOHDE_OID))
                .thenReturn(Optional.of(sijoittelu.getLatestSijoitteluajo()));
        when(hakukohdeDao.getHakukohdeForSijoitteluajo(TestDataGenerator.SIJOITTELU_AJO_ID_2, HAKUKOHDE_OID))
                .thenReturn(testDataGenerator.createHakukohdes(1).get(0));

        sijoitteluBusinessService.asetaJononValintaesitysHyvaksytyksi(sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID), VALINTATAPAJONO_OID, true, HAKU_OID);

        ArgumentCaptor<Hakukohde> argument = ArgumentCaptor.forClass(Hakukohde.class);
        verify(hakukohdeDao).persistHakukohde(argument.capture(), Matchers.eq(HAKU_OID));
        Hakukohde hakukohde = argument.getValue();
        assertEquals(true, SijoitteluBusinessService.getValintatapajono(VALINTATAPAJONO_OID, hakukohde).getValintaesitysHyvaksytty());
    }

    @Test
    public void testVaihdaIlmoittautumisTilaEiIlmoittautunut() throws Exception {
        Sijoittelu sijoittelu = testDataGenerator.generateTestData();

        when(raportointiService.cachedLatestSijoitteluAjoForHakukohde(HAKU_OID, HAKUKOHDE_OID))
                .thenReturn(Optional.of(sijoittelu.getLatestSijoitteluajo()));
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
                IlmoittautumisTila.EI_ILMOITTAUTUNUT,
                julkaistavissa,
                ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI,
                false,
                VALINTATAPAJONO_OID,
                new Date());
        sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
                sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
                v, SELITE, "");

        ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
        verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
        Valintatulos valintatulos = argument.getValue();
        assertEquals(IlmoittautumisTila.EI_ILMOITTAUTUNUT, valintatulos.getIlmoittautumisTila());
    }

    @Test
    public void testVaihdaIlmoittautumisTilaLasnaSyksy() throws Exception {
        Sijoittelu sijoittelu = testDataGenerator.generateTestData();

        when(raportointiService.cachedLatestSijoitteluAjoForHakukohde(HAKU_OID, HAKUKOHDE_OID))
                .thenReturn(Optional.of(sijoittelu.getLatestSijoitteluajo()));

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
                IlmoittautumisTila.LASNA_SYKSY,
                julkaistavissa,
                ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI,
                false,
                VALINTATAPAJONO_OID,
                new Date());
        sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
                sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
                v, SELITE, "");

        ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
        verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
        Valintatulos valintatulos = argument.getValue();
        assertEquals(IlmoittautumisTila.LASNA_SYKSY, valintatulos.getIlmoittautumisTila());
    }

    @Test
    public void testVaihdaIlmoittautumisTilaPoissaSyksy() throws Exception {
        Sijoittelu sijoittelu = testDataGenerator.generateTestData();

        when(raportointiService.cachedLatestSijoitteluAjoForHakukohde(HAKU_OID, HAKUKOHDE_OID))
                .thenReturn(Optional.of(sijoittelu.getLatestSijoitteluajo()));
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
                IlmoittautumisTila.POISSA_SYKSY,
                julkaistavissa,
                ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI,
                false,
                VALINTATAPAJONO_OID,
                new Date());
        sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
                sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
                v, SELITE, "");

        ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
        verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
        Valintatulos valintatulos = argument.getValue();
        assertEquals(IlmoittautumisTila.POISSA_SYKSY, valintatulos.getIlmoittautumisTila());
    }

    @Test
    public void testVaihdaIlmoittautumisTilaLasna() throws Exception {
        Sijoittelu sijoittelu = testDataGenerator.generateTestData();

        when(raportointiService.cachedLatestSijoitteluAjoForHakukohde(HAKU_OID, HAKUKOHDE_OID))
                .thenReturn(Optional.of(sijoittelu.getLatestSijoitteluajo()));

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
                IlmoittautumisTila.LASNA,
                julkaistavissa,
                ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI,
                false,
                VALINTATAPAJONO_OID,
                new Date());
        sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
                sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
                v, SELITE, "");

        ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
        verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
        Valintatulos valintatulos = argument.getValue();
        assertEquals(IlmoittautumisTila.LASNA, valintatulos.getIlmoittautumisTila());
    }

    @Test
    public void testVaihdaIlmoittautumisTilaPoissa() throws Exception {
        Sijoittelu sijoittelu = testDataGenerator.generateTestData();

        when(raportointiService.cachedLatestSijoitteluAjoForHakukohde(HAKU_OID, HAKUKOHDE_OID))
                .thenReturn(Optional.of(sijoittelu.getLatestSijoitteluajo()));

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
                IlmoittautumisTila.POISSA,
                julkaistavissa,
                ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI,
                false,
                VALINTATAPAJONO_OID,
                new Date());
        sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
                sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
                v, SELITE, "");

        ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
        verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
        Valintatulos valintatulos = argument.getValue();
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
                false,
                VALINTATAPAJONO_OID,
                new Date());
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
                false,
                VALINTATAPAJONO_OID,
                new Date());
        v.setHyvaksyPeruuntunut(hyvaksyPeruuntunut, "");
        sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
                testDataGenerator.createHakukohdes(1).get(0),
                v, SELITE, "");
        ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
        verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
        return argument.getValue();
    }
}
