package fi.vm.sade.sijoittelu.laskenta.service.business.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fi.vm.sade.authentication.business.service.Authorizer;
import fi.vm.sade.generic.service.exception.NotAuthorizedException;
import fi.vm.sade.security.service.authz.util.AuthorizationUtil;
import fi.vm.sade.service.valintatiedot.schema.HakuTyyppi;
import fi.vm.sade.service.valintatiedot.schema.HakukohdeTyyppi;
import fi.vm.sade.sijoittelu.batch.logic.impl.DomainConverter;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithm;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluAlgorithmFactory;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.HakukohdeItem;
import fi.vm.sade.sijoittelu.domain.IlmoittautumisTila;
import fi.vm.sade.sijoittelu.domain.LogEntry;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.TilaHistoria;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.laskenta.dao.Dao;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.sijoittelu.laskenta.service.exception.HakemusEiOleHyvaksyttyException;
import fi.vm.sade.sijoittelu.laskenta.service.exception.HakemustaEiLoytynytException;
import fi.vm.sade.sijoittelu.laskenta.service.exception.ValintatapajonoaEiLoytynytException;
import fi.vm.sade.sijoittelu.laskenta.service.exception.ValintatulosOnJoVastaanotettuException;
import fi.vm.sade.sijoittelu.laskenta.service.exception.ValintatulostaEiOleIlmoitettuException;
import fi.vm.sade.sijoittelu.tulos.roles.SijoitteluRole;

/**
 * @author Kari Kammonen
 */
@Service
public class SijoitteluBusinessServiceImpl implements SijoitteluBusinessService {

	private static final Logger LOG = LoggerFactory
			.getLogger(SijoitteluBusinessServiceImpl.class);

	@Autowired
	private SijoitteluAlgorithmFactory algorithmFactory;

	@Autowired
	private Dao dao;

	@Autowired
	private Authorizer authorizer;

	@Value("${root.organisaatio.oid}")
	private String rootOrgOid;

	/**
	 * ei versioi sijoittelua, tekeee uuden sijoittelun olemassaoleville
	 * kohteille ei kayteta viela mihinkaan
	 * 
	 * @param hakuOid
	 */
	public void sijoittele(String hakuOid) {

		Sijoittelu sijoittelu = getOrCreateSijoittelu(hakuOid);
		SijoitteluAjo viimeisinSijoitteluajo = sijoittelu
				.getLatestSijoitteluajo();
		List<Hakukohde> hakukohteet = dao
				.getHakukohdeForSijoitteluajo(viimeisinSijoitteluajo
						.getSijoitteluajoId());
		List<Valintatulos> valintatulokset = dao.loadValintatulokset(hakuOid);
		SijoitteluAlgorithm sijoitteluAlgorithm = algorithmFactory
				.constructAlgorithm(hakukohteet, valintatulokset);

		viimeisinSijoitteluajo.setStartMils(System.currentTimeMillis());
		sijoitteluAlgorithm.start();
		viimeisinSijoitteluajo.setEndMils(System.currentTimeMillis());

		// and after
		dao.persistSijoittelu(sijoittelu);
		for (Hakukohde hakukohde : hakukohteet) {
			dao.persistHakukohde(hakukohde);
		}
	}

	/**
	 * verioi sijoittelun ja tuo uudet kohteet
	 * 
	 * @param sijoitteluTyyppi
	 */
	@Override
	public void sijoittele(HakuTyyppi sijoitteluTyyppi) {

		String hakuOid = sijoitteluTyyppi.getHakuOid();
		Sijoittelu sijoittelu = getOrCreateSijoittelu(hakuOid);
		SijoitteluAjo viimeisinSijoitteluajo = sijoittelu
				.getLatestSijoitteluajo();

		List<Hakukohde> uudetHakukohteet = convertHakukohteet(sijoitteluTyyppi
				.getHakukohteet());
		List<Hakukohde> olemassaolevatHakukohteet = Collections
				.<Hakukohde> emptyList();
		if (viimeisinSijoitteluajo != null) {
			olemassaolevatHakukohteet = dao
					.getHakukohdeForSijoitteluajo(viimeisinSijoitteluajo
							.getSijoitteluajoId());
		}
		SijoitteluAjo uusiSijoitteluajo = createSijoitteluAjo(sijoittelu);
		List<Hakukohde> kaikkiHakukohteet = merge(uusiSijoitteluajo,
				olemassaolevatHakukohteet, uudetHakukohteet);

		List<Valintatulos> valintatulokset = dao.loadValintatulokset(hakuOid);
		SijoitteluAlgorithm sijoitteluAlgorithm = algorithmFactory
				.constructAlgorithm(kaikkiHakukohteet, valintatulokset);

		uusiSijoitteluajo.setStartMils(System.currentTimeMillis());
		sijoitteluAlgorithm.start();
		uusiSijoitteluajo.setEndMils(System.currentTimeMillis());

		processOldApplications(olemassaolevatHakukohteet, kaikkiHakukohteet);

		dao.persistSijoittelu(sijoittelu);
	}

	private void processOldApplications(
			final List<Hakukohde> olemassaolevatHakukohteet,
			final List<Hakukohde> kaikkiHakukohteet) {
		// wanhat hakemukset
		Map<String, Hakemus> hakemusHashMap = new HashMap<String, Hakemus>();
		for (Hakukohde hakukohde : olemassaolevatHakukohteet) {
			for (Valintatapajono valintatapajono : hakukohde
					.getValintatapajonot()) {
				for (Hakemus hakemus : valintatapajono.getHakemukset()) {
					hakemusHashMap.put(
							hakukohde.getOid() + valintatapajono.getOid()
									+ hakemus.getHakemusOid(), hakemus);
				}
			}
		}

		for (Hakukohde hakukohde : kaikkiHakukohteet) {
			for (Valintatapajono valintatapajono : hakukohde
					.getValintatapajonot()) {
				for (Hakemus hakemus : valintatapajono.getHakemukset()) {
					Hakemus edellinen = hakemusHashMap.get(hakukohde.getOid()
							+ valintatapajono.getOid()
							+ hakemus.getHakemusOid());
					if (edellinen != null
							&& edellinen.getTilaHistoria() != null
							&& !edellinen.getTilaHistoria().isEmpty()) {
						hakemus.setTilaHistoria(edellinen.getTilaHistoria());

						if (hakemus.getTila() != edellinen.getTila()) {
							TilaHistoria th = new TilaHistoria();
							th.setLuotu(new Date());
							th.setTila(hakemus.getTila());
							hakemus.getTilaHistoria().add(th);
						}
					} else {
						TilaHistoria th = new TilaHistoria();
						th.setLuotu(new Date());
						th.setTila(hakemus.getTila());
						hakemus.getTilaHistoria().add(th);
					}

				}
			}

			dao.persistHakukohde(hakukohde);
		}
	}

	// nykyisellaan vain korvaa hakukohteet, mietittava toiminta tarkemmin
	private List<Hakukohde> merge(SijoitteluAjo uusiSijoitteluajo,
			List<Hakukohde> olemassaolevatHakukohteet,
			List<Hakukohde> uudetHakukohteet) {
		Map<String, Hakukohde> kaikkiHakukohteet = new HashMap<String, Hakukohde>();
		for (Hakukohde hakukohde : olemassaolevatHakukohteet) {
			hakukohde.setId(null); // poista id vanhoilta hakukohteilta, niin
									// etta ne voidaan peristoida uusina
									// dokumentteina
			kaikkiHakukohteet.put(hakukohde.getOid(), hakukohde);
		}
		// ylikirjoita uusilla kohteilla kylmasti
		for (Hakukohde hakukohde : uudetHakukohteet) {
			kaikkiHakukohteet.put(hakukohde.getOid(), hakukohde);
		}

		for (Hakukohde hakukohde : kaikkiHakukohteet.values()) {
			HakukohdeItem hki = new HakukohdeItem();
			hki.setOid(hakukohde.getOid());
			uusiSijoitteluajo.getHakukohteet().add(hki);
			hakukohde
					.setSijoitteluajoId(uusiSijoitteluajo.getSijoitteluajoId());
		}

		return new ArrayList<Hakukohde>(kaikkiHakukohteet.values());
	}

	private List<Hakukohde> convertHakukohteet(
			List<HakukohdeTyyppi> sisaantulevatHakukohteet) {
		List<Hakukohde> hakukohdes = new ArrayList<Hakukohde>();
		for (HakukohdeTyyppi hkt : sisaantulevatHakukohteet) {
			Hakukohde hakukohde = DomainConverter.convertToHakukohde(hkt);
			hakukohdes.add(hakukohde);
		}
		return hakukohdes;
	}

	private SijoitteluAjo createSijoitteluAjo(Sijoittelu sijoittelu) {
		SijoitteluAjo sijoitteluAjo = new SijoitteluAjo();
		Long now = System.currentTimeMillis();
		sijoitteluAjo.setSijoitteluajoId(now);
		sijoitteluAjo.setHakuOid(sijoittelu.getHakuOid()); // silta varalta etta
															// tehdaan omaksi
															// entityksi
		sijoittelu.getSijoitteluajot().add(sijoitteluAjo);
		return sijoitteluAjo;
	}

	private Sijoittelu getOrCreateSijoittelu(String hakuoid) {
		Sijoittelu sijoittelu = dao.getSijoitteluByHakuOid(hakuoid);
		if (sijoittelu == null) {
			sijoittelu = new Sijoittelu();
			sijoittelu.setCreated(new Date());
			sijoittelu.setSijoitteluId(System.currentTimeMillis());
			sijoittelu.setHakuOid(hakuoid);
		}
		return sijoittelu;
	}

	@Override
	public Valintatulos haeHakemuksenTila(String hakuoid, String hakukohdeOid,
			String valintatapajonoOid, String hakemusOid) {
		if (StringUtils.isBlank(hakukohdeOid)
				|| StringUtils.isBlank(hakemusOid)) {
			throw new RuntimeException(
					"Invalid search params, fix exception later");
		}
		return dao.loadValintatulos(hakukohdeOid, valintatapajonoOid,
				hakemusOid);
	}

	@Override
	public List<Valintatulos> haeHakemustenTilat(String hakukohdeOid,
			String valintatapajonoOid) {
		if (StringUtils.isBlank(hakukohdeOid)) {
			throw new RuntimeException(
					"Invalid search params, fix exception later");
		}

		return dao.loadValintatulokset(hakukohdeOid, valintatapajonoOid);
	}

	@Override
	public List<Valintatulos> haeHakemustenTilat(String hakukohdeOid) {
		if (StringUtils.isBlank(hakukohdeOid)) {
			throw new RuntimeException(
					"Invalid search params, fix exception later");
		}

		return dao.loadValintatulokset(hakukohdeOid);
	}

	@Override
	public List<Valintatulos> haeHakemuksenTila(String hakemusOid) {
		if (StringUtils.isBlank(hakemusOid)) {
			throw new RuntimeException(
					"Invalid search params, fix exception later");
		}
		return dao.loadValintatulos(hakemusOid);
	}

	@Override
	public void vaihdaHakemuksenTila(String hakuoid, String hakukohdeOid,
			String valintatapajonoOid, String hakemusOid,
			ValintatuloksenTila tila, String selite,
			IlmoittautumisTila ilmoittautumisTila) {
		if (StringUtils.isBlank(hakuoid) || StringUtils.isBlank(hakukohdeOid)
				|| StringUtils.isBlank(valintatapajonoOid)
				|| StringUtils.isBlank(hakemusOid)) {
			throw new RuntimeException(
					"Invalid search params, fix exception later");
		}

		Sijoittelu sijoittelu = dao.getSijoitteluByHakuOid(hakuoid);
		SijoitteluAjo ajo = sijoittelu.getLatestSijoitteluajo();
		Long ajoId = ajo.getSijoitteluajoId();

		Hakukohde hakukohde = dao.getHakukohdeForSijoitteluajo(ajoId,
				hakukohdeOid);
		Valintatapajono valintatapajono = null;
		for (Valintatapajono v : hakukohde.getValintatapajonot()) {
			if (valintatapajonoOid.equals(v.getOid())) {
				valintatapajono = v;
				break;
			}
		}

		if (valintatapajono == null) {
			throw new ValintatapajonoaEiLoytynytException(
					"Valintatapajonoa ei löytynyt.");
		}

		Hakemus hakemus = getHakemus(hakemusOid, valintatapajono);

		// Oph-admin voi muokata aina
		// organisaatio updater voi muokata, jos hyväksytty

		String tarjoajaOid = hakukohde.getTarjoajaOid();
		authorizer.checkOrganisationAccess(tarjoajaOid,
				SijoitteluRole.UPDATE_ROLE, SijoitteluRole.CRUD_ROLE);

		boolean ophAdmin = checkIfOphAdmin(hakemus);

		Valintatulos v = dao.loadValintatulos(hakukohdeOid, valintatapajonoOid,
				hakemusOid);

		if (!ophAdmin) {
			if ((v == null || v.getTila() == null)
					&& tila != ValintatuloksenTila.ILMOITETTU) {
				throw new ValintatulostaEiOleIlmoitettuException(
						"Valintatulosta ei ole ilmoitettu");
			}

            // Otetaan toistaiseksi pois, koska ilmoittatumistilaa ei voi tällä toteutuksella muuttaa
//			if ((v != null && v.getTila() != null)
//					&& v.getTila() != ValintatuloksenTila.ILMOITETTU) {
//				throw new ValintatulosOnJoVastaanotettuException(
//						"Valintatulos on jo vastaanotettu");
//			}
		}

		if (v == null) {
			v = new Valintatulos();
			v.setHakemusOid(hakemus.getHakemusOid());
			v.setValintatapajonoOid(valintatapajono.getOid());
			v.setHakukohdeOid(hakukohde.getOid());
			v.setHakijaOid(hakemus.getHakijaOid());
			v.setHakutoive(hakemus.getPrioriteetti());
			v.setHakuOid(hakuoid);
		}
		v.setTila(tila);
		v.setIlmoittautumisTila(ilmoittautumisTila);

		LOG.info(
				"Asetetaan valintatuloksen tila - hakukohdeoid {}, valintatapajonooid {}, hakemusoid {}",
				new Object[] { hakukohdeOid, valintatapajonoOid, hakemusOid });
		LOG.info("Valintatuloksen uusi tila {}", tila);

		LogEntry logEntry = new LogEntry();
		logEntry.setLuotu(new Date());
		logEntry.setMuokkaaja(AuthorizationUtil.getCurrentUser());
		logEntry.setSelite(selite);
		if (tila == null) {
			logEntry.setMuutos("");
		} else {
			logEntry.setMuutos(tila.name());
		}

		v.getLogEntries().add(logEntry);

		dao.createOrUpdateValintatulos(v);
	}

	private boolean checkIfOphAdmin(final Hakemus hakemus) {
		boolean ophAdmin = false;
		try {
			authorizer.checkOrganisationAccess(rootOrgOid,
					SijoitteluRole.CRUD_ROLE);
			ophAdmin = true;
		} catch (NotAuthorizedException nae) {
			if (hakemus.getTila() != HakemuksenTila.HYVAKSYTTY) {
				throw new HakemusEiOleHyvaksyttyException(
						"sijoittelun hakemus ei ole hyvaksytty tilassa tai harkinnanvarainen");
			}
		}
		return ophAdmin;
	}

	private Hakemus getHakemus(final String hakemusOid,
			final Valintatapajono valintatapajono) {
		Hakemus hakemus = null;
		for (Hakemus h : valintatapajono.getHakemukset()) {
			if (hakemusOid.equals(h.getHakemusOid())) {
				hakemus = h;
			}
		}

		if (hakemus == null) {
			throw new HakemustaEiLoytynytException("Hakemusta ei löytynyt.");
		}
		return hakemus;
	}

}
