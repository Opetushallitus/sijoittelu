package fi.vm.sade.sijoittelu.laskenta.external.resource;

import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/sijoittelu")
public interface SijoitteluValintaTulosServiceResource {

    @POST
    @Path("/sijoitteluajo")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    void storeSijoitteluajo(SijoitteluAjo sijoitteluajo);

}
