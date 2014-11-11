package fi.vm.sade.sijoittelu.laskenta.resource;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.pattern.Patterns;
import akka.routing.RoundRobinRouter;
import akka.util.Timeout;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import fi.vm.sade.service.valintaperusteet.dto.ValintatapajonoDTO;
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
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.*;
import java.util.*;
import java.util.stream.Collectors;

import static fi.vm.sade.sijoittelu.laskenta.actors.creators.SpringExtension.SpringExtProvider;
import static fi.vm.sade.valintalaskenta.tulos.roles.ValintojenToteuttaminenRole.CRUD;

@Path("sijoittele")
@Component
// @PreAuthorize("isAuthenticated()")
@Api(value = "/sijoittele", description = "Resurssi sijoitteluun")
public class SijoitteluResource {

	private final static Logger LOGGER = LoggerFactory
			.getLogger(SijoitteluResource.class);

	@Autowired
	private SijoitteluBusinessService sijoitteluBusinessService;

	@Autowired
	private ValintalaskentaTulosService tulosService;

	@Autowired
	private ValintatietoService valintatietoService;

	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private ValintaperusteetResource valintaperusteetResource;

	@Autowired
	private ActorService actorService;

	@GET
	@Path("{hakuOid}")
	// @PreAuthorize(CRUD)
	@ApiOperation(value = "Hakemuksen valintatulosten haku")
	public String sijoittele(@PathParam("hakuOid") String hakuOid) {

		LOGGER.error("Valintatietoja valmistetaan haulle {}!", hakuOid);

		HakuDTO haku = valintatietoService.haeValintatiedot(hakuOid);

		LOGGER.error("Valintatiedot haettu serviceltä {}!", hakuOid);

		LOGGER.error("Asetetaan valintaperusteet {}!", hakuOid);

		haku.getHakukohteet()
				.forEach(
						hakukohde -> {
							Map<String, ValintatapajonoDTO> jonot = valintaperusteetResource
									.haeValintatapajonotSijoittelulle(
											hakukohde.getOid())
									.parallelStream()
									.collect(
											Collectors.toMap(
													ValintatapajonoDTO::getOid,
													jono -> jono));
							hakukohde
									.getValinnanvaihe()
									.forEach(
											vaihe -> {
												List<ValintatietoValintatapajonoDTO> konvertoidut = new ArrayList<>();
												vaihe.getValintatapajonot()
														.forEach(
																jono -> {
																	if (jonot
																			.containsKey(jono
																					.getOid())
																			&& jono.getValmisSijoiteltavaksi()
																			&& jono.getAktiivinen()) {
																		ValintatapajonoDTO perusteJono = jonot
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
																		konvertoidut
																				.add(jono);
																		jonot.remove(jono
																				.getOid());
																	}
																});
												vaihe.setValintatapajonot(konvertoidut);
											});
							if (!jonot.isEmpty()) {
								hakukohde.setKaikkiJonotSijoiteltu(false);
							}
						});

		LOGGER.error("Valintaperusteet asetettu {}!", hakuOid);

		Timeout timeout = new Timeout(Duration.create(60, "minutes"));

		Future<Object> future = Patterns.ask(actorService.getSijoitteluActor(),
				haku, timeout);

		try {
			LOGGER.error("############### Odotellaan sijoittelun valmistumista ###############");
			boolean onnistui = (boolean) Await.result(future,
					timeout.duration());
			LOGGER.error("############### Sijoittelu valmis ###############");
			return String.valueOf(onnistui);
		} catch (Exception e) {
			LOGGER.error("############### Sijoittelu epäonnistui ###############");
			e.printStackTrace();
			return "false";
		}

		// try {
		// sijoitteluBusinessService.sijoittele(haku);
		// LOGGER.error("Sijoittelu suoritettu onnistuneesti!");
		// } catch (Exception e) {
		// e.printStackTrace();
		// LOGGER.error("Sijoittelu epäonnistui syystä {}!\r\n{}",
		// e.getMessage(), Arrays.toString(e.getStackTrace()));
		// return "false";
		// }
		// return "true";
	}

}
