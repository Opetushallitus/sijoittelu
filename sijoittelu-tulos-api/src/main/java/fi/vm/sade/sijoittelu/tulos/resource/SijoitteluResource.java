package fi.vm.sade.sijoittelu.tulos.resource;

import fi.vm.sade.sijoittelu.tulos.dto.*;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.PaginationObject;
import org.codehaus.jackson.map.annotate.JsonView;

import javax.ws.rs.*;
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
                                             @PathParam("sijoitteluajoId") String sijoitteluajoId,
                                             @PathParam("hakukohdeOid") String hakukohdeOid);
    @GET
    @JsonView(JsonViews.Hakija.class)
    @Produces(APPLICATION_JSON)
    @Path("{hakuOid}/sijoitteluajo/{sijoitteluajoId}/hakemukset")
    PaginationObject<HakijaDTO> hakemukset(@PathParam("hakuOid") String hakuOid,
                            @PathParam("sijoitteluajoId") String sijoitteluajoId,
                            @QueryParam("hyvaksytyt") Boolean hyvaksytyt,
                            @QueryParam("ilmanHyvaksyntaa") Boolean ilmanHyvaksyntaa,
                            @QueryParam("vastaanottaneet") Boolean vastaanottaneet,
                            @QueryParam("hakukohdeOid")  List <String> hakukohdeOid,
                            @QueryParam("count") Integer count,
                            @QueryParam("index") Integer index);

    @GET
    @JsonView(JsonViews.Hakija.class)
    @Produces(APPLICATION_JSON)
    @Path("{hakuOid}/sijoitteluajo/{sijoitteluajoId}/hakemus/{hakemusOid}")
    HakijaDTO hakemus(@PathParam("hakuOid") String hakuOid,
                      @PathParam("sijoitteluajoId") String sijoitteluajoId,
                      @PathParam("hakemusOid") String hakemusOid);

}
