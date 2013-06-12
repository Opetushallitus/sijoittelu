package fi.vm.sade.sijoittelu.tulos.resource;

import fi.vm.sade.sijoittelu.tulos.dao.DAO;
import fi.vm.sade.sijoittelu.tulos.dao.exception.SijoitteluEntityNotFoundException;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.JsonViews;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import org.codehaus.jackson.map.annotate.JsonView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * User: wuoti
 * Date: 29.4.2013
 * Time: 15.34
 */
@Path("sijoitteluajo")
@Component
public class SijoitteluajoResource {

    private final static Logger LOGGER = LoggerFactory.getLogger(SijoitteluajoResource.class);


    @Autowired
    private DAO dao;

    @GET
    @JsonView(JsonViews.Basic.class)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{sijoitteluajoId}")
    public SijoitteluAjo getSijoitteluajo(@PathParam("sijoitteluajoId") Long sijoitteluajoId) {
        try {
            return dao.getSijoitteluajo(sijoitteluajoId);
        } catch (SijoitteluEntityNotFoundException e) {
            throw new WebApplicationException(e, Response.Status.NOT_FOUND);
        }
    }

    @GET
    @JsonView(JsonViews.Basic.class)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{sijoitteluajoId}/{hakukohdeOid}")
    public Hakukohde getHakukohdeBySijoitteluajo(@PathParam("sijoitteluajoId") Long sijoitteluajoId,
                                                 @PathParam("hakukohdeOid") String hakukohdeOid) {
        try {
            Hakukohde hakukohde = dao.getHakukohdeBySijoitteluajo(sijoitteluajoId, hakukohdeOid);
            if (hakukohde == null) {
                throw new SijoitteluEntityNotFoundException("No hakukohde found for sijoitteluajo " + sijoitteluajoId
                        + " with and hakukohde OID " + hakukohdeOid);
            }
            return hakukohde;
        } catch (SijoitteluEntityNotFoundException e) {
            throw new WebApplicationException(e, Response.Status.NOT_FOUND);
        }
    }
     /*
    @POST
    @JsonView(JsonViews.Basic.class)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{hakukohdeOid}/{valintatapajonoOid}")
    public Hakukohde gg(@PathParam("sijoitteluajoId") Long sijoitteluajoId,
                        @PathParam("hakukohdeOid") String hakukohdeOid) {
        try {
            Hakukohde hakukohde = dao.getHakukohdeBySijoitteluajo(sijoitteluajoId, hakukohdeOid);
            if (hakukohde == null) {
                throw new SijoitteluEntityNotFoundException("No hakukohde found for sijoitteluajo " + sijoitteluajoId
                        + " with and hakukohde OID " + hakukohdeOid);
            }
            return hakukohde;
        } catch (SijoitteluEntityNotFoundException e) {
            throw new WebApplicationException(e, Response.Status.NOT_FOUND);
        }
    }
       */

}
