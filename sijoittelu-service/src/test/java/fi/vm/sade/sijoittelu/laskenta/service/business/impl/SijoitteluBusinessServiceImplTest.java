package fi.vm.sade.sijoittelu.laskenta.service.business.impl;

import fi.vm.sade.sijoittelu.tulos.dao.HakukohdeDao;
import fi.vm.sade.sijoittelu.tulos.dao.SijoitteluCacheDao;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import fi.vm.sade.authentication.business.service.Authorizer;
import fi.vm.sade.generic.service.exception.NotAuthorizedException;
import fi.vm.sade.sijoittelu.domain.IlmoittautumisTila;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.laskenta.service.exception.HakemusEiOleHyvaksyttyException;
import fi.vm.sade.sijoittelu.laskenta.service.exception.ValintatulostaEiOleIlmoitettuException;
import fi.vm.sade.sijoittelu.tulos.dao.ValintatulosDao;
import fi.vm.sade.sijoittelu.tulos.roles.SijoitteluRole;

import java.util.Optional;

/**
 * Created with IntelliJ IDEA. User: jukais Date: 3.9.2013 Time: 10.50 To change
 * this template use File | Settings | File Templates.
 */
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
    private SijoitteluCacheDao sijoitteluDao;
	private Authorizer authorizer;
	private TestDataGenerator testDataGenerator;

	@Before
	public void setUp() throws Exception {
		sijoitteluBusinessService = new SijoitteluBusinessServiceImpl();

		valintatulosDaoMock = Mockito.mock(ValintatulosDao.class);
        sijoitteluDao = Mockito.mock(SijoitteluCacheDao.class);
        hakukohdeDao = Mockito.mock(HakukohdeDao.class);
		authorizer = Mockito.mock(Authorizer.class);

        ReflectionTestUtils.setField(sijoitteluBusinessService, "valintatulosDao", valintatulosDaoMock);
        ReflectionTestUtils.setField(sijoitteluBusinessService, "sijoitteluDao", sijoitteluDao);
        ReflectionTestUtils.setField(sijoitteluBusinessService, "hakukohdeDao", hakukohdeDao);
		ReflectionTestUtils.setField(sijoitteluBusinessService, "authorizer", authorizer);
		ReflectionTestUtils.setField(sijoitteluBusinessService, ROOT_ORG_OID, ROOT_ORG_OID);

		testDataGenerator = new TestDataGenerator();

	}

	@Test(expected = ValintatulostaEiOleIlmoitettuException.class)
	public void testVaihdTilaSuoraanPeruuntuneeksi() throws Exception {

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
						HAKEMUS_OID)).thenReturn(getValintatulos(null));

		sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID, HAKUKOHDE_OID,
				VALINTATAPAJONO_OID, HAKEMUS_OID, ValintatuloksenTila.PERUNUT,
				SELITE, IlmoittautumisTila.EI_TEHTY);
	}

    // Toiminnallisuus poistettu käytöstä
//	@Test(expected = ValintatulosOnJoVastaanotettuException.class)
//	public void testVaihdaVahvistettuIlmoitetuksi() throws Exception {
//
//		Sijoittelu sijoittelu = testDataGenerator.generateTestData();
//
//		Mockito.when(sijoitteluDao.getSijoitteluByHakuOid(HAKU_OID)).thenReturn(
//				sijoittelu);
//		Mockito.when(
//				daoMock.getHakukohdeForSijoitteluajo(
//						TestDataGenerator.SIJOITTELU_AJO_ID_2, HAKUKOHDE_OID))
//				.thenReturn(testDataGenerator.createHakukohdes(1).get(0));
//		Mockito.doThrow(new NotAuthorizedException())
//				.when(authorizer)
//				.checkOrganisationAccess(ROOT_ORG_OID, SijoitteluRole.CRUD_ROLE);
//
//		Mockito.when(
//				daoMock.loadValintatulos(HAKUKOHDE_OID, VALINTATAPAJONO_OID,
//						HAKEMUS_OID)).thenReturn(
//				getValintatulos(ValintatuloksenTila.PERUNUT));
//
//		sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID, HAKUKOHDE_OID,
//				VALINTATAPAJONO_OID, HAKEMUS_OID, ValintatuloksenTila.PERUNUT,
//				SELITE, IlmoittautumisTila.EI_TEHTY);
//	}

	@Test(expected = HakemusEiOleHyvaksyttyException.class)
	public void testVaihdaHyvaksymattomanHakemuksenTilaIlmoitetuksi()
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
						HAKEMUS_OID_2)).thenReturn(getValintatulos(null));

		sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID, HAKUKOHDE_OID,
				VALINTATAPAJONO_OID, HAKEMUS_OID_2,
				ValintatuloksenTila.ILMOITETTU, SELITE,
				IlmoittautumisTila.EI_TEHTY);
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
						HAKEMUS_OID)).thenReturn(getValintatulos(null));

		sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID, HAKUKOHDE_OID,
				VALINTATAPAJONO_OID, HAKEMUS_OID,
				ValintatuloksenTila.ILMOITETTU, SELITE,
				IlmoittautumisTila.EI_TEHTY);
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

		sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID, HAKUKOHDE_OID,
				VALINTATAPAJONO_OID, HAKEMUS_OID, ValintatuloksenTila.PERUNUT,
				SELITE, IlmoittautumisTila.EI_TEHTY);
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
						HAKEMUS_OID)).thenReturn(getValintatulos(null));

		sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID, HAKUKOHDE_OID,
				VALINTATAPAJONO_OID, HAKEMUS_OID, ValintatuloksenTila.PERUNUT,
				SELITE, IlmoittautumisTila.EI_TEHTY);
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
						HAKEMUS_OID_2)).thenReturn(getValintatulos(null));

		sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID, HAKUKOHDE_OID,
				VALINTATAPAJONO_OID, HAKEMUS_OID_2,
				ValintatuloksenTila.VASTAANOTTANUT, SELITE,
				IlmoittautumisTila.EI_TEHTY);
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

		sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID, HAKUKOHDE_OID,
				VALINTATAPAJONO_OID, HAKEMUS_OID,
				ValintatuloksenTila.VASTAANOTTANUT, SELITE,
				IlmoittautumisTila.LASNA_KOKO_LUKUVUOSI);
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

		sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID, HAKUKOHDE_OID,
				VALINTATAPAJONO_OID, HAKEMUS_OID,
				ValintatuloksenTila.VASTAANOTTANUT, SELITE,
				IlmoittautumisTila.POISSA_KOKO_LUKUVUOSI);
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

		sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID, HAKUKOHDE_OID,
				VALINTATAPAJONO_OID, HAKEMUS_OID,
				ValintatuloksenTila.VASTAANOTTANUT, SELITE,
				IlmoittautumisTila.EI_ILMOITTAUTUNUT);
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

		sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID, HAKUKOHDE_OID,
				VALINTATAPAJONO_OID, HAKEMUS_OID,
				ValintatuloksenTila.VASTAANOTTANUT, SELITE,
				IlmoittautumisTila.LASNA_SYKSY);
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

		sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID, HAKUKOHDE_OID,
				VALINTATAPAJONO_OID, HAKEMUS_OID,
				ValintatuloksenTila.VASTAANOTTANUT, SELITE,
				IlmoittautumisTila.POISSA_SYKSY);
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

		sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID, HAKUKOHDE_OID,
				VALINTATAPAJONO_OID, HAKEMUS_OID,
				ValintatuloksenTila.VASTAANOTTANUT, SELITE,
				IlmoittautumisTila.LASNA);
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

		sijoitteluBusinessService.vaihdaHakemuksenTila(HAKU_OID, HAKUKOHDE_OID,
				VALINTATAPAJONO_OID, HAKEMUS_OID,
				ValintatuloksenTila.VASTAANOTTANUT, SELITE,
				IlmoittautumisTila.POISSA);
	}
}
