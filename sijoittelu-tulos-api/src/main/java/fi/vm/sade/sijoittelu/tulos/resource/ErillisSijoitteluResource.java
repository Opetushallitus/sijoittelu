package fi.vm.sade.sijoittelu.tulos.resource;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

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
