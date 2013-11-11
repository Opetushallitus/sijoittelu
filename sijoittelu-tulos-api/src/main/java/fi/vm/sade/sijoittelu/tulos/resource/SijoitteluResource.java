package fi.vm.sade.sijoittelu.tulos.resource;

import fi.vm.sade.sijoittelu.tulos.dto.*;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaDTO;
import org.codehaus.jackson.map.annotate.JsonView;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import java.util.Collection;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * 
 * @author Jussi Jartamo
 * 
 *         Autentikointi annotaatiot implementaatiossa!
 */
@Path("sijoittelu")
public interface SijoitteluResource {

    static final String LATEST = "latest";

    @GET
    @Produces(APPLICATION_JSON)
    @Path("{hakuOid}/sijoitteluajo/{sijoitteluajoId}/hakemus/{hakemusOid}")
    List<HakemusDTO> getHakemusBySijoitteluajo(@PathParam("hakuOid") String hakuOid,
            @PathParam("sijoitteluajoId") String sijoitteluajoId, @PathParam("hakemusOid") String hakemusOid);

    @GET
    @JsonView(JsonViews.Sijoittelu.class)
    @Produces(APPLICATION_JSON)
    @Path("{hakuOid}")
    SijoitteluDTO getSijoitteluByHakuOid(@PathParam("hakuOid") String hakuOid);

    @GET
    @JsonView(JsonViews.Sijoitteluajo.class)
    @Produces(APPLICATION_JSON)
    @Path("{hakuOid}/sijoitteluajo/{sijoitteluajoId}")
    SijoitteluajoDTO getSijoitteluajo(@PathParam("hakuOid") String hakuOid,
            @PathParam("sijoitteluajoId") String sijoitteluajoId);

    @GET
    @JsonView(JsonViews.Hakukohde.class)
    @Produces(APPLICATION_JSON)
    @Path("{hakuOid}/sijoitteluajo/{sijoitteluajoId}/hakukohde/{hakukohdeOid}")
    HakukohdeDTO getHakukohdeBySijoitteluajo(@PathParam("hakuOid") String hakuOid,
            @PathParam("sijoitteluajoId") String sijoitteluajoId, @PathParam("hakukohdeOid") String hakukohdeOid);

    @GET
    @JsonView(JsonViews.Hakija.class)
    @Produces(APPLICATION_JSON)
    @Path("{hakuOid}/sijoitteluajo/{sijoitteluajoId}/koulutuspaikalliset")
    List<HakijaDTO> koulutuspaikalliset(@PathParam("hakuOid") String hakuOid,
            @PathParam("sijoitteluajoId") String sijoitteluajoId);

    @GET
    @JsonView(JsonViews.Hakija.class)
    @Produces(APPLICATION_JSON)
    @Path("{hakuOid}/hakukohde/{hakukohdeOid}/sijoitteluajo/{sijoitteluajoId}/koulutuspaikalliset")
    Collection<HakijaDTO> koulutuspaikalliset(@PathParam("hakuOid") String hakuOid,
            @PathParam("hakukohdeOid") String hakukohdeOid, @PathParam("sijoitteluajoId") String sijoitteluajoId);

    @GET
    @JsonView(JsonViews.Hakija.class)
    @Produces(APPLICATION_JSON)
    @Path("{hakuOid}/sijoitteluajo/{sijoitteluajoId}/ilmankoulutuspaikkaa")
    List<HakijaDTO> ilmankoulutuspaikkaa(@PathParam("hakuOid") String hakuOid,
            @PathParam("sijoitteluajoId") String sijoitteluajoId);

    @GET
    @JsonView(JsonViews.Hakija.class)
    @Produces(APPLICATION_JSON)
    @Path("{hakuOid}/sijoitteluajo/{sijoitteluajoId}/hakijat")
    List<HakijaDTO> hakijat(@PathParam("hakuOid") String hakuOid, @PathParam("sijoitteluajoId") String sijoitteluajoId);

}
