package fi.vm.sade.sijoittelu.laskenta.resource;

import static fi.vm.sade.sijoittelu.laskenta.roles.SijoitteluRole.READ_UPDATE_CRUD;
import static fi.vm.sade.sijoittelu.laskenta.roles.SijoitteluRole.UPDATE_CRUD;

import java.util.*;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.sijoittelu.tulos.dao.HakukohdeDao;
import fi.vm.sade.sijoittelu.tulos.dao.SijoitteluDao;
import fi.vm.sade.sijoittelu.tulos.service.RaportointiService;
import org.codehaus.jackson.map.annotate.JsonView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;

/**
 * Created with IntelliJ IDEA. User: kkammone Date: 20.5.2013 Time: 17:23 To
 * change this template use File | Settings | File Templates.
 */
@Path("tila")
@Component
@PreAuthorize("isAuthenticated()")
@Api(value = "/tila", description = "Resurssi sijoittelun tilojen käsittelyyn")
public class TilaResource {

	private final static Logger LOGGER = LoggerFactory
			.getLogger(TilaResource.class);

    static final String LATEST = "latest";

	@Autowired
	private SijoitteluBusinessService sijoitteluBusinessService;

    @Autowired
    private RaportointiService raportointiService;

    @Autowired
    private HakukohdeDao hakukohdeDao;


    @Autowired
    private SijoitteluDao sijoitteluDao;

	@GET
	@JsonView(JsonViews.MonenHakemuksenTila.class)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{hakemusOid}")
	@PreAuthorize(READ_UPDATE_CRUD)
	@ApiOperation(value = "Hakemuksen valintatulosten haku")
	public List<Valintatulos> hakemus(@PathParam("hakemusOid") String hakemusOid) {
		List<Valintatulos> v = sijoitteluBusinessService
				.haeHakemuksenTila(hakemusOid);
		if (v == null) {
			v = new ArrayList<Valintatulos>();
		}
		return v;
	}

	@GET
	@JsonView(JsonViews.Tila.class)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{hakemusOid}/{hakuOid}/{hakukohdeOid}/{valintatapajonoOid}/")
	@PreAuthorize(READ_UPDATE_CRUD)
	@ApiOperation(value = "Hakemuksen valintatulosten haku tietyssä hakukohteessa ja valintatapajonossa")
	public Valintatulos hakemus(@PathParam("hakuOid") String hakuOid,
			@PathParam("hakukohdeOid") String hakukohdeOid,
			@PathParam("valintatapajonoOid") String valintatapajonoOid,
			@PathParam("hakemusOid") String hakemusOid) {
		Valintatulos v = sijoitteluBusinessService.haeHakemuksenTila(hakuOid,
				hakukohdeOid, valintatapajonoOid, hakemusOid);
		if (v == null) {
			v = new Valintatulos();
		}
		return v;
	}

	@GET
	@JsonView(JsonViews.MonenHakemuksenTila.class)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/hakukohde/{hakukohdeOid}/{valintatapajonoOid}/")
	@ApiOperation(value = "Valintatulosten haku hakukohteelle ja valintatapajonolle")
	@PreAuthorize(READ_UPDATE_CRUD)
	public List<Valintatulos> haku(
			@PathParam("hakukohdeOid") String hakukohdeOid,
			@PathParam("valintatapajonoOid") String valintatapajonoOid) {
		List<Valintatulos> v = sijoitteluBusinessService.haeHakemustenTilat(
				hakukohdeOid, valintatapajonoOid);
		if (v == null) {
			v = new ArrayList<Valintatulos>();
		}
		return v;
	}

	@GET
	@JsonView(JsonViews.MonenHakemuksenTila.class)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/hakukohde/{hakukohdeOid}")
	@ApiOperation(value = "Valintatulosten haku hakukohteelle ja valintatapajonolle")
	@PreAuthorize(READ_UPDATE_CRUD)
	public List<Valintatulos> hakukohteelle(
			@PathParam("hakukohdeOid") String hakukohdeOid) {
		List<Valintatulos> v = sijoitteluBusinessService
				.haeHakukohteenTilat(hakukohdeOid);
		if (v == null) {
			v = new ArrayList<Valintatulos>();
		}
		return v;
	}

	@POST
	@JsonView(JsonViews.Tila.class)
	@Produces(MediaType.APPLICATION_JSON)
	@Path("haku/{hakuOid}/hakukohde/{hakukohdeOid}")
	@PreAuthorize(UPDATE_CRUD)
	@ApiOperation(value = "Valintatulosten tuonti hakukohteelle")
	public Response muutaHakemustenTilaa(@PathParam("hakuOid") String hakuOid,
			@PathParam("hakukohdeOid") String hakukohdeOid,
			List<Valintatulos> valintatulokset,
			@QueryParam("selite") String selite) {

		try {
			for (Valintatulos v : valintatulokset) {
				ValintatuloksenTila tila = v.getTila();
				IlmoittautumisTila ilmoittautumisTila = v
						.getIlmoittautumisTila();
				sijoitteluBusinessService.vaihdaHakemuksenTila(hakuOid,
						hakukohdeOid, v.getValintatapajonoOid(),
						v.getHakemusOid(), tila, selite, ilmoittautumisTila);
			}
			return Response.status(Response.Status.ACCEPTED).build();
		} catch (Exception e) {
			LOGGER.error("Error inserting valintakoekoodi.", e);
			Map error = new HashMap();
			error.put("message", e.getMessage());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity(error).build();
		}
	}

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("haku/{hakuOid}/hakukohde/{hakukohdeOid}/hakemus/{hakemusOid}")
    @PreAuthorize(UPDATE_CRUD)
    @ApiOperation(value = "Hakemuksen sijoittelun tilan muuttaminen")
    public Response muutaSijoittelunTilaa(@PathParam("hakuOid") String hakuOid,
                                         @PathParam("hakukohdeOid") String hakukohdeOid,
                                         @PathParam("hakemusOid") String hakemusOid,
                                         boolean hyvaksy) {

        try {

            Optional<SijoitteluAjo> sijoitteluAjoOpt = raportointiService.latestSijoitteluAjoForHaku(hakuOid);

            if(!sijoitteluAjoOpt.isPresent()) {
                Sijoittelu sijoittelu = new Sijoittelu();
                sijoittelu.setCreated(new Date());
                sijoittelu.setSijoitteluId(System.currentTimeMillis());
                sijoittelu.setHakuOid(hakuOid);

                SijoitteluAjo sijoitteluAjo = new SijoitteluAjo();
                Long now = System.currentTimeMillis();
                sijoitteluAjo.setSijoitteluajoId(now);
                sijoitteluAjo.setHakuOid(sijoittelu.getHakuOid());
                sijoittelu.getSijoitteluajot().add(sijoitteluAjo);

                sijoitteluDao.persistSijoittelu(sijoittelu);

                sijoitteluAjoOpt = Optional.of(sijoitteluAjo);
            }

            SijoitteluAjo ajo = sijoitteluAjoOpt.get();

            Optional<HakukohdeItem> itemOpt = ajo.getHakukohteet().parallelStream().filter(h -> h.getOid().equals(hakukohdeOid)).findFirst();

            if(!itemOpt.isPresent()) {
                Sijoittelu sijoittelu = sijoitteluDao.getSijoitteluByHakuOid(hakuOid).get();
                HakukohdeItem item = new HakukohdeItem();
                item.setOid(hakukohdeOid);
                sijoittelu.getLatestSijoitteluajo().getHakukohteet().add(item);

                sijoitteluDao.persistSijoittelu(sijoittelu);

                Hakukohde hakukohde = new Hakukohde();
                hakukohde.setKaikkiJonotSijoiteltu(true);
                hakukohde.setOid(hakukohdeOid);
                hakukohde.setSijoitteluajoId(ajo.getSijoitteluajoId());

                Valintatapajono jono = new Valintatapajono();
                jono.setHyvaksytty(0);
                jono.setVaralla(0);
                jono.setOid(UUID.randomUUID().toString());
                jono.setAloituspaikat(0);
                jono.setPrioriteetti(0);

                hakukohde.getValintatapajonot().add(jono);
                hakukohdeDao.persistHakukohde(hakukohde);

            }

            Hakukohde kohde = hakukohdeDao.getHakukohdeForSijoitteluajo(ajo.getSijoitteluajoId(), hakukohdeOid);

            Valintatapajono jono = kohde.getValintatapajonot().get(0);

            Optional<Hakemus> hakemusOpt = jono.getHakemukset().parallelStream().filter(h -> h.getHakemusOid().equals(hakemusOid)).findFirst();

            if(hakemusOpt.isPresent()) {
                if(hyvaksy) {
                    hakemusOpt.get().setTila(HakemuksenTila.HYVAKSYTTY);
                    jono.setHyvaksytty(jono.getHyvaksytty() + 1);
                } else {
                    hakemusOpt.get().setTila(HakemuksenTila.HYLATTY);
                    jono.setHyvaksytty(jono.getHyvaksytty() - 1);
                }

            } else {
                Hakemus hakemus = new Hakemus();
                hakemus.setHakemusOid(hakemusOid);
                hakemus.setJonosija(1);
                hakemus.setPrioriteetti(1);
                if(hyvaksy) {
                    hakemus.setTila(HakemuksenTila.HYVAKSYTTY);
                    jono.setHyvaksytty(jono.getHyvaksytty() + 1);
                } else {
                    hakemus.setTila(HakemuksenTila.HYLATTY);
                    jono.setHyvaksytty(jono.getHyvaksytty() - 1);
                }
                jono.getHakemukset().add(hakemus);

            }

            hakukohdeDao.persistHakukohde(kohde);

            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            LOGGER.error("Hakemuksen tilan asetus epäonnistui", e);
            Map error = new HashMap();
            error.put("message", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(error).build();
        }
    }

}
