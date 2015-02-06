package fi.vm.sade.sijoittelu.tulos.resource;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * 
 * @author Jussi Jartamo
 * 
 *         Autentikointi annotaatiot implementaatiossa!
 */
@Path("erillissijoittelu")
@Api(value = "/erillissijoittelu", description = "Resurssi erillissijoittelun tuloksien hakemiseen")
public interface ErillisSijoitteluResource {


	@GET
	@Produces(APPLICATION_JSON)
	@Path("/{hakuOid}/sijoitteluajo/{sijoitteluajoId}/hakukohde/{hakukohdeOid}")
	@ApiOperation(position = 3, value = "Hakee hakukohteen tiedot tietyssa sijoitteluajossa.", response = HakukohdeDTO.class)
	Response getHakukohdeBySijoitteluajo(
            @ApiParam(value = "Haun tunniste", required = true) @PathParam("hakuOid") String hakuOid,
            @ApiParam(value = "Sijoitteluajon tunniste", required = true) @PathParam("sijoitteluajoId") String sijoitteluajoId,
            @ApiParam(value = "Hakukohteen tunniste", required = true) @PathParam("hakukohdeOid") String hakukohdeOid);



}
