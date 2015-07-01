package fi.vm.sade.sijoittelu.laskenta.service.business.impl;

import fi.vm.sade.authentication.business.service.Authorizer;
import fi.vm.sade.generic.service.exception.NotAuthorizedException;
import fi.vm.sade.sijoittelu.domain.IlmoittautumisTila;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.tulos.dao.HakukohdeDao;
import fi.vm.sade.sijoittelu.tulos.dao.SijoitteluDao;
import fi.vm.sade.sijoittelu.tulos.dao.ValintatulosDao;
import fi.vm.sade.sijoittelu.tulos.roles.SijoitteluRole;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

public class SijoitteluBusinessServiceImplTest {
	final String HAKU_OID = TestDataGenerator.HAKU_OID;
	final String HAKUKOHDE_OID = TestDataGenerator.HAKUKOHDE_OID_1;
	final String VALINTATAPAJONO_OID = TestDataGenerator.VALINTATAPAJONO_OID_1;
	final String HAKEMUS_OID = TestDataGenerator.HAKEMUS_OID_1;
	final String HAKEMUS_OID_2 = TestDataGenerator.HAKEMUS_OID_2;
	final String SELITE = "selite";
	final String ROOT_ORG_OID = "rootOrgOid";

	private SijoitteluBusinessServiceImpl sijoitteluBusinessService;
    private ValintatulosDao valintatulosDaoMock;
    private HakukohdeDao hakukohdeDao;
    private SijoitteluDao sijoitteluDao;
	private Authorizer authorizer;
	private TestDataGenerator testDataGenerator;

	@Before
	public void setUp() throws Exception {
		sijoitteluBusinessService = new SijoitteluBusinessServiceImpl();

		valintatulosDaoMock = Mockito.mock(ValintatulosDao.class);
        sijoitteluDao = Mockito.mock(SijoitteluDao.class);
        hakukohdeDao = Mockito.mock(HakukohdeDao.class);
		authorizer = Mockito.mock(Authorizer.class);

        ReflectionTestUtils.setField(sijoitteluBusinessService, "valintatulosDao", valintatulosDaoMock);
        ReflectionTestUtils.setField(sijoitteluBusinessService, "sijoitteluDao", sijoitteluDao);
        ReflectionTestUtils.setField(sijoitteluBusinessService, "hakukohdeDao", hakukohdeDao);
		ReflectionTestUtils.setField(sijoitteluBusinessService, "authorizer", authorizer);
		ReflectionTestUtils.setField(sijoitteluBusinessService, ROOT_ORG_OID, ROOT_ORG_OID);

		testDataGenerator = new TestDataGenerator();

	}

	@Test
	public void testVaihdaTilaIlmoitetuksi() throws Exception {

		Sijoittelu sijoittelu = testDataGenerator.generateTestData();

		Mockito.when(sijoitteluDao.getSijoitteluByHakuOid(HAKU_OID)).thenReturn(
				Optional.of(sijoittelu));
		Mockito.when(
				hakukohdeDao.getHakukohdeForSijoitteluajo(
						TestDataGenerator.SIJOITTELU_AJO_ID_2, HAKUKOHDE_OID))
				.thenReturn(testDataGenerator.createHakukohdes(1).get(0));
		Mockito.doThrow(new NotAuthorizedException())
				.when(authorizer)
				.checkOrganisationAccess(ROOT_ORG_OID, SijoitteluRole.CRUD_ROLE);

		Mockito.when(
				valintatulosDaoMock.loadValintatulos(HAKUKOHDE_OID, VALINTATAPAJONO_OID,
						HAKEMUS_OID)).thenReturn(getValintatulos(ValintatuloksenTila.KESKEN));

		sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
				sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
				VALINTATAPAJONO_OID, HAKEMUS_OID,
				ValintatuloksenTila.ILMOITETTU, SELITE,
				IlmoittautumisTila.EI_TEHTY, false, false);

		ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
		verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
		Valintatulos valintatulos = argument.getValue();
		assertEquals(ValintatuloksenTila.ILMOITETTU, valintatulos.getTila());
		assertEquals(IlmoittautumisTila.EI_TEHTY, valintatulos.getIlmoittautumisTila());
	}

	@Test
	public void testVaihdaTilaPeruuntuneeksi() throws Exception {

		Sijoittelu sijoittelu = testDataGenerator.generateTestData();

		Mockito.when(sijoitteluDao.getSijoitteluByHakuOid(HAKU_OID)).thenReturn(
                Optional.of(sijoittelu));
		Mockito.when(
				hakukohdeDao.getHakukohdeForSijoitteluajo(
						TestDataGenerator.SIJOITTELU_AJO_ID_2, HAKUKOHDE_OID))
				.thenReturn(testDataGenerator.createHakukohdes(1).get(0));
		Mockito.doThrow(new NotAuthorizedException())
				.when(authorizer)
				.checkOrganisationAccess(ROOT_ORG_OID, SijoitteluRole.CRUD_ROLE);

		Mockito.when(
				valintatulosDaoMock.loadValintatulos(HAKUKOHDE_OID, VALINTATAPAJONO_OID,
						HAKEMUS_OID)).thenReturn(
				getValintatulos(ValintatuloksenTila.ILMOITETTU));

		sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
				sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
				VALINTATAPAJONO_OID, HAKEMUS_OID, ValintatuloksenTila.PERUNUT,
				SELITE, IlmoittautumisTila.EI_TEHTY, false, false);

		ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
		verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
		Valintatulos valintatulos = argument.getValue();
		assertEquals(ValintatuloksenTila.PERUNUT, valintatulos.getTila());
		assertEquals(IlmoittautumisTila.EI_TEHTY, valintatulos.getIlmoittautumisTila());
	}

	@Test
	public void testOphVaihdaTilaPeruuntuneeksiSuoraan() throws Exception {
		Sijoittelu sijoittelu = testDataGenerator.generateTestData();

		Mockito.when(sijoitteluDao.getSijoitteluByHakuOid(HAKU_OID)).thenReturn(
                Optional.of(sijoittelu));
		Mockito.when(
				hakukohdeDao.getHakukohdeForSijoitteluajo(
						TestDataGenerator.SIJOITTELU_AJO_ID_2, HAKUKOHDE_OID))
				.thenReturn(testDataGenerator.createHakukohdes(1).get(0));

		Mockito.when(
				valintatulosDaoMock.loadValintatulos(HAKUKOHDE_OID, VALINTATAPAJONO_OID,
						HAKEMUS_OID)).thenReturn(getValintatulos(ValintatuloksenTila.KESKEN));

		sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
				sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
				VALINTATAPAJONO_OID, HAKEMUS_OID, ValintatuloksenTila.PERUNUT,
				SELITE, IlmoittautumisTila.EI_TEHTY, false, false);

		ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
		verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
		Valintatulos valintatulos = argument.getValue();
		assertEquals(ValintatuloksenTila.PERUNUT, valintatulos.getTila());
		assertEquals(IlmoittautumisTila.EI_TEHTY, valintatulos.getIlmoittautumisTila());
	}

	@Test
	public void testOphVaihdaVarallaOlevanTila() throws Exception {
		Sijoittelu sijoittelu = testDataGenerator.generateTestData();

		Mockito.when(sijoitteluDao.getSijoitteluByHakuOid(HAKU_OID)).thenReturn(
                Optional.of(sijoittelu));
		Mockito.when(
				hakukohdeDao.getHakukohdeForSijoitteluajo(
						TestDataGenerator.SIJOITTELU_AJO_ID_2, HAKUKOHDE_OID))
				.thenReturn(testDataGenerator.createHakukohdes(1).get(0));

		Mockito.when(
				valintatulosDaoMock.loadValintatulos(HAKUKOHDE_OID, VALINTATAPAJONO_OID,
						HAKEMUS_OID_2)).thenReturn(getValintatulos(ValintatuloksenTila.KESKEN));

		sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
				sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
				VALINTATAPAJONO_OID, HAKEMUS_OID_2,
				ValintatuloksenTila.VASTAANOTTANUT, SELITE,
				IlmoittautumisTila.EI_TEHTY, false, false);

		ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
		verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
		Valintatulos valintatulos = argument.getValue();
		assertEquals(ValintatuloksenTila.VASTAANOTTANUT, valintatulos.getTila());
		assertEquals(IlmoittautumisTila.EI_TEHTY, valintatulos.getIlmoittautumisTila());
	}

	private Valintatulos getValintatulos(ValintatuloksenTila tila) {
		Valintatulos valintatulos = new Valintatulos();
		valintatulos.setHakemusOid(HAKEMUS_OID);
		valintatulos.setHakijaOid(HAKEMUS_OID);
		valintatulos.setHakukohdeOid(HAKUKOHDE_OID);
		valintatulos.setHakuOid(HAKU_OID);
		valintatulos.setHakutoive(0);
		valintatulos.setValintatapajonoOid(VALINTATAPAJONO_OID);
		valintatulos.setTila(tila);
		return valintatulos;
	}

	@Test
	public void testVaihdaIlmoittautumisTilaLasnaKokoLukuvuosi()
			throws Exception {
		Sijoittelu sijoittelu = testDataGenerator.generateTestData();

		Mockito.when(sijoitteluDao.getSijoitteluByHakuOid(HAKU_OID)).thenReturn(
                Optional.of(sijoittelu));
		Mockito.when(
				hakukohdeDao.getHakukohdeForSijoitteluajo(
						TestDataGenerator.SIJOITTELU_AJO_ID_2, HAKUKOHDE_OID))
				.thenReturn(testDataGenerator.createHakukohdes(1).get(0));
		Mockito.doThrow(new NotAuthorizedException())
				.when(authorizer)
				.checkOrganisationAccess(ROOT_ORG_OID, SijoitteluRole.CRUD_ROLE);

		Mockito.when(
				valintatulosDaoMock.loadValintatulos(HAKUKOHDE_OID, VALINTATAPAJONO_OID,
						HAKEMUS_OID)).thenReturn(
				getValintatulos(ValintatuloksenTila.ILMOITETTU));

		sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
				sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
				VALINTATAPAJONO_OID, HAKEMUS_OID,
				ValintatuloksenTila.VASTAANOTTANUT, SELITE,
				IlmoittautumisTila.LASNA_KOKO_LUKUVUOSI, false, false);

		ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
		verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
		Valintatulos valintatulos = argument.getValue();
		assertEquals(ValintatuloksenTila.VASTAANOTTANUT, valintatulos.getTila());
		assertEquals(IlmoittautumisTila.LASNA_KOKO_LUKUVUOSI, valintatulos.getIlmoittautumisTila());
	}

	@Test
	public void testVaihdaIlmoittautumisTilaPoissaKokoLukuvuosi()
			throws Exception {
		Sijoittelu sijoittelu = testDataGenerator.generateTestData();

		Mockito.when(sijoitteluDao.getSijoitteluByHakuOid(HAKU_OID)).thenReturn(
                Optional.of(sijoittelu));
		Mockito.when(
				hakukohdeDao.getHakukohdeForSijoitteluajo(
						TestDataGenerator.SIJOITTELU_AJO_ID_2, HAKUKOHDE_OID))
				.thenReturn(testDataGenerator.createHakukohdes(1).get(0));
		Mockito.doThrow(new NotAuthorizedException())
				.when(authorizer)
				.checkOrganisationAccess(ROOT_ORG_OID, SijoitteluRole.CRUD_ROLE);

		Mockito.when(
				valintatulosDaoMock.loadValintatulos(HAKUKOHDE_OID, VALINTATAPAJONO_OID,
						HAKEMUS_OID)).thenReturn(
				getValintatulos(ValintatuloksenTila.ILMOITETTU));

		sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
				sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
				VALINTATAPAJONO_OID, HAKEMUS_OID,
				ValintatuloksenTila.VASTAANOTTANUT, SELITE,
				IlmoittautumisTila.POISSA_KOKO_LUKUVUOSI, false, false);

		ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
		verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
		Valintatulos valintatulos = argument.getValue();
		assertEquals(ValintatuloksenTila.VASTAANOTTANUT, valintatulos.getTila());
		assertEquals(IlmoittautumisTila.POISSA_KOKO_LUKUVUOSI, valintatulos.getIlmoittautumisTila());
	}

	@Test
	public void testVaihdaIlmoittautumisTilaEiIlmoittautunut() throws Exception {
		Sijoittelu sijoittelu = testDataGenerator.generateTestData();

		Mockito.when(sijoitteluDao.getSijoitteluByHakuOid(HAKU_OID)).thenReturn(
                Optional.of(sijoittelu));
		Mockito.when(
				hakukohdeDao.getHakukohdeForSijoitteluajo(
						TestDataGenerator.SIJOITTELU_AJO_ID_2, HAKUKOHDE_OID))
				.thenReturn(testDataGenerator.createHakukohdes(1).get(0));
		Mockito.doThrow(new NotAuthorizedException())
				.when(authorizer)
				.checkOrganisationAccess(ROOT_ORG_OID, SijoitteluRole.CRUD_ROLE);

		Mockito.when(
				valintatulosDaoMock.loadValintatulos(HAKUKOHDE_OID, VALINTATAPAJONO_OID,
						HAKEMUS_OID)).thenReturn(
				getValintatulos(ValintatuloksenTila.ILMOITETTU));

		sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
				sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
				VALINTATAPAJONO_OID, HAKEMUS_OID,
				ValintatuloksenTila.VASTAANOTTANUT, SELITE,
				IlmoittautumisTila.EI_ILMOITTAUTUNUT, false, false);

		ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
		verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
		Valintatulos valintatulos = argument.getValue();
		assertEquals(ValintatuloksenTila.VASTAANOTTANUT, valintatulos.getTila());
		assertEquals(IlmoittautumisTila.EI_ILMOITTAUTUNUT, valintatulos.getIlmoittautumisTila());
	}

	@Test
	public void testVaihdaIlmoittautumisTilaLasnaSyksy() throws Exception {
		Sijoittelu sijoittelu = testDataGenerator.generateTestData();

		Mockito.when(sijoitteluDao.getSijoitteluByHakuOid(HAKU_OID)).thenReturn(
                Optional.of(sijoittelu));
		Mockito.when(
				hakukohdeDao.getHakukohdeForSijoitteluajo(
						TestDataGenerator.SIJOITTELU_AJO_ID_2, HAKUKOHDE_OID))
				.thenReturn(testDataGenerator.createHakukohdes(1).get(0));
		Mockito.doThrow(new NotAuthorizedException())
				.when(authorizer)
				.checkOrganisationAccess(ROOT_ORG_OID, SijoitteluRole.CRUD_ROLE);

		Mockito.when(
				valintatulosDaoMock.loadValintatulos(HAKUKOHDE_OID, VALINTATAPAJONO_OID,
						HAKEMUS_OID)).thenReturn(
				getValintatulos(ValintatuloksenTila.ILMOITETTU));

		sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
				sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
				VALINTATAPAJONO_OID, HAKEMUS_OID,
				ValintatuloksenTila.VASTAANOTTANUT, SELITE,
				IlmoittautumisTila.LASNA_SYKSY, false, false);

		ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
		verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
		Valintatulos valintatulos = argument.getValue();
		assertEquals(ValintatuloksenTila.VASTAANOTTANUT, valintatulos.getTila());
		assertEquals(IlmoittautumisTila.LASNA_SYKSY, valintatulos.getIlmoittautumisTila());
	}

	@Test
	public void testVaihdaIlmoittautumisTilaPoissaSyksy() throws Exception {
		Sijoittelu sijoittelu = testDataGenerator.generateTestData();

		Mockito.when(sijoitteluDao.getSijoitteluByHakuOid(HAKU_OID)).thenReturn(
                Optional.of(sijoittelu));
		Mockito.when(
				hakukohdeDao.getHakukohdeForSijoitteluajo(
						TestDataGenerator.SIJOITTELU_AJO_ID_2, HAKUKOHDE_OID))
				.thenReturn(testDataGenerator.createHakukohdes(1).get(0));
		Mockito.doThrow(new NotAuthorizedException())
				.when(authorizer)
				.checkOrganisationAccess(ROOT_ORG_OID, SijoitteluRole.CRUD_ROLE);

		Mockito.when(
				valintatulosDaoMock.loadValintatulos(HAKUKOHDE_OID, VALINTATAPAJONO_OID,
						HAKEMUS_OID)).thenReturn(
				getValintatulos(ValintatuloksenTila.ILMOITETTU));

		sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
				sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
				VALINTATAPAJONO_OID, HAKEMUS_OID,
				ValintatuloksenTila.VASTAANOTTANUT, SELITE,
				IlmoittautumisTila.POISSA_SYKSY, false, false);

		ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
		verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
		Valintatulos valintatulos = argument.getValue();
		assertEquals(ValintatuloksenTila.VASTAANOTTANUT, valintatulos.getTila());
		assertEquals(IlmoittautumisTila.POISSA_SYKSY, valintatulos.getIlmoittautumisTila());
	}

	@Test
	public void testVaihdaIlmoittautumisTilaLasna() throws Exception {
		Sijoittelu sijoittelu = testDataGenerator.generateTestData();

		Mockito.when(sijoitteluDao.getSijoitteluByHakuOid(HAKU_OID)).thenReturn(
                Optional.of(sijoittelu));
		Mockito.when(
				hakukohdeDao.getHakukohdeForSijoitteluajo(
						TestDataGenerator.SIJOITTELU_AJO_ID_2, HAKUKOHDE_OID))
				.thenReturn(testDataGenerator.createHakukohdes(1).get(0));
		Mockito.doThrow(new NotAuthorizedException())
				.when(authorizer)
				.checkOrganisationAccess(ROOT_ORG_OID, SijoitteluRole.CRUD_ROLE);

		Mockito.when(
				valintatulosDaoMock.loadValintatulos(HAKUKOHDE_OID, VALINTATAPAJONO_OID,
						HAKEMUS_OID)).thenReturn(
				getValintatulos(ValintatuloksenTila.ILMOITETTU));

		sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
				sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
				VALINTATAPAJONO_OID, HAKEMUS_OID,
				ValintatuloksenTila.VASTAANOTTANUT, SELITE,
				IlmoittautumisTila.LASNA, false, false);

		ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
		verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
		Valintatulos valintatulos = argument.getValue();
		assertEquals(ValintatuloksenTila.VASTAANOTTANUT, valintatulos.getTila());
		assertEquals(IlmoittautumisTila.LASNA, valintatulos.getIlmoittautumisTila());
	}

	@Test
	public void testVaihdaIlmoittautumisTilaPoissa() throws Exception {
		Sijoittelu sijoittelu = testDataGenerator.generateTestData();

		Mockito.when(sijoitteluDao.getSijoitteluByHakuOid(HAKU_OID)).thenReturn(
                Optional.of(sijoittelu));
		Mockito.when(
				hakukohdeDao.getHakukohdeForSijoitteluajo(
						TestDataGenerator.SIJOITTELU_AJO_ID_2, HAKUKOHDE_OID))
				.thenReturn(testDataGenerator.createHakukohdes(1).get(0));
		Mockito.doThrow(new NotAuthorizedException())
				.when(authorizer)
				.checkOrganisationAccess(ROOT_ORG_OID, SijoitteluRole.CRUD_ROLE);

		Mockito.when(
				valintatulosDaoMock.loadValintatulos(HAKUKOHDE_OID, VALINTATAPAJONO_OID,
						HAKEMUS_OID)).thenReturn(
				getValintatulos(ValintatuloksenTila.ILMOITETTU));

		sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID,
				sijoitteluBusinessService.getHakukohde(HAKU_OID, HAKUKOHDE_OID),
				VALINTATAPAJONO_OID, HAKEMUS_OID,
				ValintatuloksenTila.VASTAANOTTANUT, SELITE,
				IlmoittautumisTila.POISSA, false, false);

		ArgumentCaptor<Valintatulos> argument = ArgumentCaptor.forClass(Valintatulos.class);
		verify(valintatulosDaoMock).createOrUpdateValintatulos(argument.capture());
		Valintatulos valintatulos = argument.getValue();
		assertEquals(ValintatuloksenTila.VASTAANOTTANUT, valintatulos.getTila());
		assertEquals(IlmoittautumisTila.POISSA, valintatulos.getIlmoittautumisTila());
	}
}
