package fi.vm.sade.sijoittelu.tulos.resource;

import fi.vm.sade.sijoittelu.domain.JsonViews;
import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.tulos.dao.DAO;
import fi.vm.sade.sijoittelu.tulos.dao.exception.SijoitteluEntityNotFoundException;
import org.codehaus.jackson.map.annotate.JsonView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static fi.vm.sade.sijoittelu.tulos.roles.SijoitteluRole.*;


/**
 * User: wuoti
 * Date: 26.4.2013
 * Time: 12.41
 */
@Path("sijoittelu")
@Component
@PreAuthorize("isAuthenticated()")
public class SijoitteluResource {


    private final static Logger LOGGER = LoggerFactory.getLogger(SijoitteluResource.class);


    @Autowired
    private DAO dao;

    @GET
    @JsonView(JsonViews.Basic.class)
    @Produces(MediaType.APPLICATION_JSON)
    @Secured({READ, UPDATE, CRUD})
    public  List<Sijoittelu> getSijoittelu() {
        List<Sijoittelu> sijoittelu = dao.getSijoittelu();

        if (sijoittelu == null) {
            //    throw new WebApplicationException(Response.Status.NOT_FOUND);
            sijoittelu =  Collections.EMPTY_LIST;
        }

        return sijoittelu;
    }



    @GET
    @JsonView(JsonViews.Basic.class)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{hakuOid}")
    @Secured({READ, UPDATE, CRUD})
    public Sijoittelu getSijoitteluByHakuOid(@PathParam("hakuOid") String hakuOid) {
        Sijoittelu sijoittelu = dao.getSijoitteluByHakuOid(hakuOid);
        //if (sijoittelu == null) {
        //    throw new WebApplicationException(Response.Status.NOT_FOUND);
        // }

        return sijoittelu;
    }

    @GET
    @JsonView(JsonViews.Basic.class)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{hakuOid}/sijoitteluajo")
    @Secured({READ, UPDATE, CRUD})
    public List<SijoitteluAjo> getSijoitteluajoByHakuOid(@PathParam("hakuOid") String hakuOid,
                                                         @QueryParam("latest") Boolean latest) {

        Sijoittelu sijoittelu = dao.getSijoitteluByHakuOid(hakuOid);
        if (sijoittelu != null) {
            try {
                if (latest != null && latest) {
                    return Arrays.asList(sijoittelu.getLatestSijoitteluajo());
                } else {
                    return sijoittelu.getSijoitteluajot();
                }
            } catch (SijoitteluEntityNotFoundException e) {
            }
        }
        return Collections.EMPTY_LIST;
    }

    /**
     * Palauttaa sijoitteluajon, jonka ajoajankohta osuu lähimmäksi annettua timestamp-parametria
     *
     * @param hakuOid
     * @param timestamp
     * @return

     @GET
     @JsonView(JsonViews.Basic.class)
     @Produces(MediaType.APPLICATION_JSON)
     @Path("/{hakuOid}/sijoitteluajo/{time}")
     public SijoitteluAjo getSijoitteluajoByTimestamp(@PathParam("hakuOid") String hakuOid,
     @PathParam("time") Long timestamp) {

     return dao.getSijoitteluajoByHakuOidAndTimestamp(hakuOid, timestamp);
     }
     */
}
