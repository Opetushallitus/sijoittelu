package fi.vm.sade.sijoittelu.tulos.resource;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.codehaus.jackson.map.annotate.JsonView;

import fi.vm.sade.sijoittelu.tulos.dto.HakijaDTO;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.dto.JsonViews;
import fi.vm.sade.sijoittelu.tulos.dto.SijoitteluDTO;
import fi.vm.sade.sijoittelu.tulos.dto.SijoitteluajoDTO;

/**
 * 
 * @author Jussi Jartamo
 * 
 *         Autentikointi annotaatiot implementaatiossa!
 */
@Path("sijoittelu")
public interface SijoitteluResource {

    public static final String LATEST = "latest";

    @GET
    @JsonView(JsonViews.Sijoittelu.class)
    @Produces(APPLICATION_JSON)
    @Path("{hakuOid}")
    public SijoitteluDTO getSijoitteluByHakuOid(@PathParam("hakuOid") String hakuOid);

    @GET
    @JsonView(JsonViews.Sijoitteluajo.class)
    @Produces(APPLICATION_JSON)
    @Path("{hakuOid}/sijoitteluajo/{sijoitteluajoId}")
    public SijoitteluajoDTO getSijoitteluajo(@PathParam("hakuOid") String hakuOid,
            @PathParam("sijoitteluajoId") String sijoitteluajoId);

    @GET
    @JsonView(JsonViews.Hakukohde.class)
    @Produces(APPLICATION_JSON)
    @Path("{hakuOid}/sijoitteluajo/{sijoitteluajoId}/hakukohde/{hakukohdeOid}")
    public HakukohdeDTO getHakukohdeBySijoitteluajo(@PathParam("hakuOid") String hakuOid,
            @PathParam("sijoitteluajoId") String sijoitteluajoId, @PathParam("hakukohdeOid") String hakukohdeOid);

    @GET
    @JsonView(JsonViews.Hakija.class)
    @Produces(APPLICATION_JSON)
    @Path("{hakuOid}/sijoitteluajo/{sijoitteluajoId}/koulutuspaikalliset}")
    public List<HakijaDTO> koulutuspaikalliset(@PathParam("hakuOid") String hakuOid,
            @PathParam("sijoitteluajoId") String sijoitteluajoId);

    @GET
    @JsonView(JsonViews.Hakija.class)
    @Produces(APPLICATION_JSON)
    @Path("{hakuOid}/hakukohde/{hakukohdeOid}/sijoitteluajo/{sijoitteluajoId}/koulutuspaikalliset}")
    public List<HakijaDTO> koulutuspaikalliset(@PathParam("hakuOid") String hakuOid,
            @PathParam("hakukohdeOid") String hakukohdeOid, @PathParam("sijoitteluajoId") String sijoitteluajoId);

    @GET
    @JsonView(JsonViews.Hakija.class)
    @Produces(APPLICATION_JSON)
    @Path("{hakuOid}/sijoitteluajo/{sijoitteluajoId}/ilmankoulutuspaikkaa}")
    public List<HakijaDTO> ilmankoulutuspaikkaa(@PathParam("hakuOid") String hakuOid,
            @PathParam("sijoitteluajoId") String sijoitteluajoId);

}
