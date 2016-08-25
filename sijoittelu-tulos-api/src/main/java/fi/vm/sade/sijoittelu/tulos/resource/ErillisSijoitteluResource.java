package fi.vm.sade.sijoittelu.tulos.resource;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 *         Autentikointi annotaatiot implementaatiossa!
 */
@Path("erillissijoittelu")
public interface ErillisSijoitteluResource {

    @GET
    @Produces(APPLICATION_JSON)
    @Path("/{hakuOid}/sijoitteluajo/{sijoitteluajoId}/hakukohde/{hakukohdeOid}")
    Response getHakukohdeBySijoitteluajo(
            @PathParam("hakuOid") String hakuOid,
            @PathParam("sijoitteluajoId") String sijoitteluajoId,
            @PathParam("hakukohdeOid") String hakukohdeOid);
}
