package fi.vm.sade.sijoittelu.laskenta.resource;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.routing.RoundRobinRouter;
import akka.util.Timeout;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import fi.vm.sade.service.valintaperusteet.dto.HakijaryhmaValintatapajonoDTO;
import fi.vm.sade.service.valintaperusteet.dto.ValintatapajonoDTO;
import fi.vm.sade.service.valintaperusteet.resource.ValintalaskentakoostepalveluResource;
import fi.vm.sade.service.valintaperusteet.resource.ValintaperusteetResource;
import fi.vm.sade.sijoittelu.laskenta.service.business.ActorService;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.sijoittelu.laskenta.util.EnumConverter;
import fi.vm.sade.valintalaskenta.domain.dto.valintakoe.Tasasijasaanto;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.ValintatietoValintatapajonoDTO;
import fi.vm.sade.valintalaskenta.tulos.service.ValintalaskentaTulosService;
import fi.vm.sade.valintalaskenta.tulos.service.impl.ValintatietoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import static java.lang.Boolean.TRUE;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.*;
import java.util.*;
import java.util.stream.Collectors;
import static java.util.Optional.*;
import static java.util.Collections.*;

import static fi.vm.sade.sijoittelu.laskenta.actors.creators.SpringExtension.SpringExtProvider;
import static fi.vm.sade.valintalaskenta.tulos.roles.ValintojenToteuttaminenRole.CRUD;

@Path("sijoittele")
@Controller
// @PreAuthorize("isAuthenticated()")
@Api(value = "/sijoittele", description = "Resurssi sijoitteluun")
public class SijoitteluResource {

	private final static Logger LOGGER = LoggerFactory
			.getLogger(SijoitteluResource.class);

	private final SijoitteluBusinessService sijoitteluBusinessService;
	private final ValintatietoService valintatietoService;
	private final ValintalaskentakoostepalveluResource valintalaskentakoostepalveluResource;

	@Autowired
	public SijoitteluResource(SijoitteluBusinessService sijoitteluBusinessService,
							  ValintatietoService valintatietoService,
							  ValintalaskentakoostepalveluResource valintalaskentakoostepalveluResource) {
		this.sijoitteluBusinessService = sijoitteluBusinessService;
		this.valintatietoService = valintatietoService;
		this.valintalaskentakoostepalveluResource = valintalaskentakoostepalveluResource;
	}

	@GET
	@Path("{hakuOid}")
	// @PreAuthorize(CRUD)
	@ApiOperation(value = "Hakemuksen valintatulosten haku", response = String.class)
	public String sijoittele(@PathParam("hakuOid") String hakuOid) {

		LOGGER.info("Valintatietoja valmistetaan haulle {}!", hakuOid);

		HakuDTO haku = valintatietoService.haeValintatiedot(hakuOid);

		LOGGER.info("Valintatiedot haettu serviceltä {}!", hakuOid);

		LOGGER.info("Asetetaan valintaperusteet {}!", hakuOid);
		final Map<String, HakijaryhmaValintatapajonoDTO> hakijaryhmaByOid = haeMahdollisestiMuuttuneetHakijaryhmat(haku);
		final Map<String, Map<String, ValintatapajonoDTO>> hakukohdeMapToValintatapajonoByOid = Maps.newHashMap(haeMahdollisestiMuuttuneetValintatapajonot(haku));
		//;

		haku.getHakukohteet().forEach(
		hakukohde -> {
			ofNullable(hakukohde.getHakijaryhma()).orElse(emptyList()).forEach(
					hakijaryhma -> {
						if(hakijaryhma != null && hakijaryhmaByOid.containsKey(hakijaryhma.getHakijaryhmaOid())) {
							HakijaryhmaValintatapajonoDTO h = hakijaryhmaByOid.get(hakijaryhma.getHakijaryhmaOid());
							//hakijaryhma.setCreatedAt();
							//hakijaryhma.setHakijaryhmaOid();
							//hakijaryhma.setHakukohdeOid();
							//hakijaryhma.setJonosijat();
							hakijaryhma.setKaytaKaikki(h.isKaytaKaikki());
							hakijaryhma.setKaytetaanRyhmaanKuuluvia(h.isKaytetaanRyhmaanKuuluvia());
							hakijaryhma.setKiintio(h.getKiintio());
							hakijaryhma.setKuvaus(h.getKuvaus());
							hakijaryhma.setNimi(h.getNimi());
							hakijaryhma.setTarkkaKiintio(h.isTarkkaKiintio());
							//hakijaryhma.setPrioriteetti();
							//hakijaryhma.setTarkkaKiintio();
							//hakijaryhma.setValintatapajonoOid();
						}
					}
			);
			Map<String, ValintatapajonoDTO> valintatapajonoByOid = hakukohdeMapToValintatapajonoByOid.getOrDefault(hakukohde.getOid(), new HashMap<>());
			hakukohde
					.getValinnanvaihe()
					.forEach(
							vaihe -> {
								List<ValintatietoValintatapajonoDTO> konvertoidut = new ArrayList<>();
								vaihe.getValintatapajonot()
										.forEach(
												jono -> {


													if (valintatapajonoByOid
															.containsKey(jono
																	.getOid())
															&& jono.getValmisSijoiteltavaksi()
															&& jono.getAktiivinen()) {
														ValintatapajonoDTO perusteJono = valintatapajonoByOid
																.get(jono
																		.getOid());
														jono.setAloituspaikat(perusteJono
																.getAloituspaikat());
														jono.setEiVarasijatayttoa(perusteJono
																.getEiVarasijatayttoa());
														jono.setPoissaOlevaTaytto(perusteJono
																.getPoissaOlevaTaytto());
														jono.setTasasijasaanto(EnumConverter
																.convert(
																		Tasasijasaanto.class,
																		perusteJono
																				.getTasapistesaanto()));
														jono.setTayttojono(perusteJono
																.getTayttojono());
														jono.setVarasijat(perusteJono
																.getVarasijat());
														jono.setVarasijaTayttoPaivat(perusteJono
																.getVarasijaTayttoPaivat());
														jono.setVarasijojaKaytetaanAlkaen(perusteJono
																.getVarasijojaKaytetaanAlkaen());
														jono.setVarasijojaTaytetaanAsti(perusteJono
																.getVarasijojaTaytetaanAsti());
														jono.setAktiivinen(perusteJono
																.getAktiivinen());
														jono.setKaikkiEhdonTayttavatHyvaksytaan(perusteJono.getKaikkiEhdonTayttavatHyvaksytaan());
														jono.setNimi(perusteJono.getNimi());
														konvertoidut
																.add(jono);
														valintatapajonoByOid.remove(jono
																.getOid());
													}
												});
								vaihe.setValintatapajonot(konvertoidut);
							});
			if (!valintatapajonoByOid.isEmpty()) {
				LOGGER.warn("Kaikkia jonoja ei ole sijoiteltu {}!", hakukohde.getOid());
				hakukohde.setKaikkiJonotSijoiteltu(false);
			}
		});

		LOGGER.info("Valintaperusteet asetettu {}!", hakuOid);

		try {
		    sijoitteluBusinessService.sijoittele(haku);
		    LOGGER.info("Sijoittelu suoritettu onnistuneesti haulle {}", hakuOid);
            return "true";
		} catch (Exception e) {
		    e.printStackTrace();
		    LOGGER.error("Sijoittelu epäonnistui haulle {} syystä {}!\r\n{}", hakuOid,
		    e.getMessage(), Arrays.toString(e.getStackTrace()));
		    return "false";
		}

	}
	private Map<String, HakijaryhmaValintatapajonoDTO> haeMahdollisestiMuuttuneetHakijaryhmat(HakuDTO haku) {
		Set<String> hakukohdeOidsWithHakijaryhma =
				haku.getHakukohteet().stream().filter(hakukohde -> hakukohde.getHakijaryhma() != null && !hakukohde.getHakijaryhma().isEmpty())
						.map(hakukohde -> hakukohde.getOid()).collect(Collectors.toSet());

		Map<String, HakijaryhmaValintatapajonoDTO> hakijaryhmaByOid = Collections.emptyMap();
		if(!hakukohdeOidsWithHakijaryhma.isEmpty()) {
			LOGGER.info("Haetaan hakijaryhmät sijoittelua varten");
			try {
				hakijaryhmaByOid =
						valintalaskentakoostepalveluResource.readByHakukohdeOids(Lists.newArrayList(hakukohdeOidsWithHakijaryhma))
								.stream()
										// Valintaperusteet pitaisi palauttaa vain aktiivisia mutta filtteroidaan varmuuden vuoksi
								.filter(v -> TRUE.equals(v.getAktiivinen()))
								.collect(Collectors.toMap(v -> v.getOid(), v -> v));
			} catch(Exception e) {
				LOGGER.error("Hakijaryhmien hakeminen epäonnistui virheeseen {} {}", e.getMessage(), Arrays.toString(e.getStackTrace()));
				throw e;
			}
			LOGGER.info("Saatiin hakukohteille {} yhteensä {} aktiivista hakijaryhmää", Arrays.toString(hakukohdeOidsWithHakijaryhma.toArray()),
					hakijaryhmaByOid.size());
		}
		return hakijaryhmaByOid;
	}
	private Map<String,Map<String, ValintatapajonoDTO>> haeMahdollisestiMuuttuneetValintatapajonot(HakuDTO haku) {
		Set<String> hakukohdeOidsWithAktiivisetJonot =
				haku.getHakukohteet().stream()
						// Joku valinnanvaihe jossa aktiivinen jono
						.filter(hakukohde ->
								hakukohde.getValinnanvaihe().stream().anyMatch(v ->
										v.getValintatapajonot().stream().anyMatch(j ->
												TRUE.equals(j.getAktiivinen()))))
								//
						.map(hakukohde -> hakukohde.getOid())
								//
						.collect(Collectors.toSet());

		if(!hakukohdeOidsWithAktiivisetJonot.isEmpty()) {
			LOGGER.info("Haetaan valintatapajonoja sijoittelua varten haulle {} ja hakukohteille {}", haku.getHakuOid(), Arrays.toString(hakukohdeOidsWithAktiivisetJonot.toArray()));
			try {
				return valintalaskentakoostepalveluResource.haeValintatapajonotSijoittelulle(Lists.newArrayList(hakukohdeOidsWithAktiivisetJonot))
								.entrySet()
								.stream()
								.collect(Collectors.toMap(v -> v.getKey(), v -> {
									Map<String, ValintatapajonoDTO> jonot =
									v.getValue().stream().filter(v0 -> TRUE.equals(v0.getAktiivinen())).collect(Collectors.toMap(v0 -> v0.getOid(), v0 -> {

										return v0;
									}));
									return jonot;
								}));
			} catch(Exception e) {
				LOGGER.error("Valintatapajonojen hakeminen epäonnistui virheeseen {} {}", e.getMessage(), Arrays.toString(e.getStackTrace()));
				throw e;
			}
		} else {
			return Collections.emptyMap();
		}
	}
}
