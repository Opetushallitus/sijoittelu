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
@Component
// @PreAuthorize("isAuthenticated()")
@Api(value = "/sijoittele", description = "Resurssi sijoitteluun")
public class SijoitteluResource {

	private final static Logger LOGGER = LoggerFactory
			.getLogger(SijoitteluResource.class);

	@Autowired
	private SijoitteluBusinessService sijoitteluBusinessService;

	@Autowired
	private ValintatietoService valintatietoService;

	@Autowired
	private ValintalaskentakoostepalveluResource valintalaskentakoostepalveluResource;

	@GET
	@Path("{hakuOid}")
	// @PreAuthorize(CRUD)
	@ApiOperation(value = "Hakemuksen valintatulosten haku")
	public String sijoittele(@PathParam("hakuOid") String hakuOid) {

		LOGGER.error("Valintatietoja valmistetaan haulle {}!", hakuOid);

		HakuDTO haku = valintatietoService.haeValintatiedot(hakuOid);

		LOGGER.error("Valintatiedot haettu serviceltä {}!", hakuOid);

		LOGGER.error("Asetetaan valintaperusteet {}!", hakuOid);
		final Map<String, HakijaryhmaValintatapajonoDTO> hakijaryhmaByOid = haeMahdollisestiMuuttuneetHakijaryhmat(haku);
		final Map<String, ValintatapajonoDTO> valintatapajonoByOid = Maps.newHashMap(haeMahdollisestiMuuttuneetValintatapajonot(haku));
		//;

		haku.getHakukohteet()
				.forEach(
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
											//hakijaryhma.setPrioriteetti();
											//hakijaryhma.setTarkkaKiintio();
											//hakijaryhma.setValintatapajonoOid();
										}
									}
							);
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
								LOGGER.error("Kaikkia jonoja ei ole sijoiteltu");
								hakukohde.setKaikkiJonotSijoiteltu(false);
							}
						});

		LOGGER.error("Valintaperusteet asetettu {}!", hakuOid);

		try {
		    sijoitteluBusinessService.sijoittele(haku);
		    LOGGER.error("Sijoittelu suoritettu onnistuneesti!");
            return "true";
		} catch (Exception e) {
		    e.printStackTrace();
		    LOGGER.error("Sijoittelu epäonnistui syystä {}!\r\n{}",
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
			LOGGER.error("Haetaan hakijaryhmät sijoittelua varten");
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
	private Map<String, ValintatapajonoDTO> haeMahdollisestiMuuttuneetValintatapajonot(HakuDTO haku) {
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

		Map<String, ValintatapajonoDTO> valintatapajonoByOid = Collections.emptyMap();
		if(!hakukohdeOidsWithAktiivisetJonot.isEmpty()) {
			LOGGER.error("Haetaan valintatapajonoja sijoittelua varten");
			try {
				valintatapajonoByOid =
						valintalaskentakoostepalveluResource.haeValintatapajonotSijoittelulle(Lists.newArrayList(hakukohdeOidsWithAktiivisetJonot))
								.stream()
										// Valintaperusteet pitaisi palauttaa vain aktiivisia mutta filtteroidaan varmuuden vuoksi
								.filter(v -> TRUE.equals(v.getAktiivinen()))
										//
								.collect(Collectors.toMap(v -> v.getOid(), v -> v));
			} catch(Exception e) {
				LOGGER.error("Valintatapajonojen hakeminen epäonnistui virheeseen {} {}", e.getMessage(), Arrays.toString(e.getStackTrace()));
				throw e;
			}
			LOGGER.info("Saatiin hakukohteille {} yhteensä {} aktiivista valintatapajonoa", Arrays.toString(hakukohdeOidsWithAktiivisetJonot.toArray()),
					valintatapajonoByOid.size());
		}
		return valintatapajonoByOid;
	}
}
