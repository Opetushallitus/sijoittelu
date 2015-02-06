package fi.vm.sade.sijoittelu.laskenta.external.resource;

import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ResultHakuDTO;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * 
 * @author Jussi Jartamo
 * 
 */
@Path("/v1/haku")
public interface HakuV1Resource {

	@GET
	@Path("/{oid}")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	// @ApiOperation(value = "Palauttaa haun annetulla oid:lla", notes =
	// "Palauttaa haun annetulla oid:lla", response = HakuV1RDTO.class)
	public ResultHakuDTO findByOid(@PathParam("oid") String oid);

}
