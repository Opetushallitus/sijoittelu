package fi.vm.sade.sijoittelu.laskenta.external.resource;

import fi.vm.sade.tarjonta.service.resources.v1.dto.HakukohdeV1RDTO;
import fi.vm.sade.tarjonta.service.resources.v1.dto.ResultV1RDTO;

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
@Path("/v1/hakukohde")
public interface HakukohdeV1Resource {

	@GET
	@Path("/{oid}")
	@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
	public ResultV1RDTO<HakukohdeV1RDTO> findByOid(@PathParam("oid") String oid);

}
