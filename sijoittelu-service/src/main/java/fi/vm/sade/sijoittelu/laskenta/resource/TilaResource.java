package fi.vm.sade.sijoittelu.laskenta.resource;

import fi.vm.sade.sijoittelu.domain.JsonViews;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import org.codehaus.jackson.map.annotate.JsonView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fi.vm.sade.sijoittelu.laskenta.roles.SijoitteluRole.*;
/**
 * Created with IntelliJ IDEA.
 * User: kkammone
 * Date: 20.5.2013
 * Time: 17:23
 * To change this template use File | Settings | File Templates.
 */
@Path("tila")
@Component
@PreAuthorize("isAuthenticated()")
public class TilaResource {

    private final static Logger LOGGER = LoggerFactory.getLogger(TilaResource.class);

    @Autowired
    private SijoitteluBusinessService sijoitteluBusinessService;


    @GET
    @JsonView(JsonViews.MonenHakemuksenTila.class)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{hakemusOid}")
    @Secured({READ, UPDATE, CRUD})
    public List<Valintatulos> hakemus(@PathParam("hakemusOid") String hakemusOid) {
        List<Valintatulos> v = sijoitteluBusinessService.haeHakemuksenTila(hakemusOid);
        if (v == null) {
            v = new ArrayList<Valintatulos>();
        }
        return v;
    }

    @GET
    @JsonView(JsonViews.Tila.class)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{hakemusOid}/{hakuOid}/{hakukohdeOid}/{valintatapajonoOid}/")
    @Secured({READ, UPDATE, CRUD})
    public Valintatulos hakemus(@PathParam("hakuOid") String hakuOid,
                                @PathParam("hakukohdeOid") String hakukohdeOid,
                                @PathParam("valintatapajonoOid") String valintatapajonoOid,
                                @PathParam("hakemusOid") String hakemusOid) {
        Valintatulos v = sijoitteluBusinessService.haeHakemuksenTila(hakuOid, hakukohdeOid, valintatapajonoOid, hakemusOid);
        if (v == null) {
            v = new Valintatulos();
        }
        return v;
    }

    @GET
    @JsonView(JsonViews.MonenHakemuksenTila.class)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/hakukohde/{hakukohdeOid}/{valintatapajonoOid}/")
    @Secured({READ, UPDATE, CRUD})
    public List<Valintatulos> haku(@PathParam("hakukohdeOid") String hakukohdeOid,
                                @PathParam("valintatapajonoOid") String valintatapajonoOid) {
        List<Valintatulos> v = sijoitteluBusinessService.haeHakemustenTilat(hakukohdeOid, valintatapajonoOid);
        if (v == null) {
            v = new ArrayList<Valintatulos>();
        }
        return v;
    }

    @POST
    @JsonView(JsonViews.Tila.class)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{hakemusOid}/{hakuOid}/{hakukohdeOid}/{valintatapajonoOid}/")
    @Secured({UPDATE, CRUD})
    public Response muutaHakemuksenTilaa(@PathParam("hakuOid") String hakuOid,
                                         @PathParam("hakukohdeOid") String hakukohdeOid,
                                         @PathParam("valintatapajonoOid") String valintatapajonoOid,
                                         @PathParam("hakemusOid") String hakemusOid,
                                         Valintatulos v,
                                         @QueryParam("selite") String selite) {

        try {
            ValintatuloksenTila tila = v.getTila();
            sijoitteluBusinessService.vaihdaHakemuksenTila(hakuOid, hakukohdeOid, valintatapajonoOid, hakemusOid, tila, selite);
            return Response.status(Response.Status.ACCEPTED).build();
        } catch (Exception e) {
            LOGGER.error("Error inserting valintakoekoodi.", e);
            Map error = new HashMap();
            error.put("message", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error).build();
        }
    }

}



