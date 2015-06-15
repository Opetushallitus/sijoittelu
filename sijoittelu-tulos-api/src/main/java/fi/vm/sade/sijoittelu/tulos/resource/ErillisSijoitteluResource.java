package fi.vm.sade.sijoittelu.tulos.resource;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author Jussi Jartamo
 *         <p/>
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
