package fi.vm.sade.sijoittelu.laskenta.resource;

import fi.vm.sade.sijoittelu.domain.JsonViews;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import org.codehaus.jackson.map.annotate.JsonView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created with IntelliJ IDEA.
 * User: kkammone
 * Date: 20.5.2013
 * Time: 17:23
 * To change this template use File | Settings | File Templates.
 */
@Path("tila")
@Component
public class TilaResource {

    private final static Logger LOGGER = LoggerFactory.getLogger(TilaResource.class);

    @Autowired
    private SijoitteluBusinessService sijoitteluBusinessService;

    @GET
    @JsonView(JsonViews.Basic.class)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{hakuOid}/{hakukohdeOid}/{valintatapajonoOid}/{hakemusOid}")
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

    @POST
    @JsonView(JsonViews.Basic.class)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{hakuOid}/{hakukohdeOid}/{valintatapajonoOid}/{hakemusOid}")
    public boolean muutaHakemuksenTilaa(@PathParam("hakuOid") String hakuOid,
                                        @PathParam("hakukohdeOid") String hakukohdeOid,
                                        @PathParam("valintatapajonoOid") String valintatapajonoOid,
                                        @PathParam("hakemusOid") String hakemusOid,
                                        Valintatulos v) {
        try {
            ValintatuloksenTila tila = v.getTila();
            sijoitteluBusinessService.vaihdaHakemuksenTila(hakuOid, hakukohdeOid, valintatapajonoOid, hakemusOid, tila);
        } catch (Exception e) {
            throw new WebApplicationException(e, Response.Status.FORBIDDEN);
        }

        return true;
    }

}



