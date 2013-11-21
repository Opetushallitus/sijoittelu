package fi.vm.sade.sijoittelu.tulos.resource;

import fi.vm.sade.sijoittelu.tulos.dto.*;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaDTO;
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
    List<String> hakemukset(@PathParam("hakuOid") String hakuOid,
                            @PathParam("sijoitteluajoId") String sijoitteluajoId,
                            @QueryParam("vastaanottotieto") List<String> vastaanottotieto,
                            @QueryParam("tila") List<String> tila,
                            @QueryParam("hakukohdeOid")  List <String> hakukohdeOid,
                            @QueryParam("count") Integer count,
                            @QueryParam("startIndex") Integer startIndex);

    @GET
    @JsonView(JsonViews.Hakija.class)
    @Produces(APPLICATION_JSON)
    @Path("{hakuOid}/sijoitteluajo/{sijoitteluajoId}/hakemus/{hakemusOid}")
    HakijaDTO hakemus(@PathParam("hakuOid") String hakuOid,
                      @PathParam("sijoitteluajoId") String sijoitteluajoId,
                      @PathParam("hakemusOid") String hakemusOid);







    /*
    @GET
    @Produces(APPLICATION_JSON)
    @JsonView(JsonViews.Hakemus.class)
    @Path("{hakuOid}/sijoitteluajo/{sijoitteluajoId}/hakemus/{hakemusOid}")
    List<HakemusDTO> getHakemusBySijoitteluajo(@PathParam("hakuOid") String hakuOid,
                                               @PathParam("sijoitteluajoId") String sijoitteluajoId,
                                               @PathParam("hakemusOid") String hakemusOid);
    */
    /*

    @GET
    @JsonView(JsonViews.Hakija.class)
    @Produces(APPLICATION_JSON)
    @Deprecated
    @Path("{hakuOid}/sijoitteluajo/{sijoitteluajoId}/hyvaksytyt")
    List<HakijaDTO> hyvaksytyt(@PathParam("hakuOid") String hakuOid,
                                        @PathParam("sijoitteluajoId") String sijoitteluajoId);


    @GET
    @JsonView(JsonViews.Hakija.class)
    @Produces(APPLICATION_JSON)
    @Deprecated
    @Path("{hakuOid}/hakukohde/{hakukohdeOid}/sijoitteluajo/{sijoitteluajoId}/hyvaksytyt")
    Collection<HakijaDTO> hyvaksytyt(@PathParam("hakuOid") String hakuOid,
                                              @PathParam("hakukohdeOid") String hakukohdeOid,
                                              @PathParam("sijoitteluajoId") String sijoitteluajoId);

    @GET
    @JsonView(JsonViews.Hakija.class)
    @Produces(APPLICATION_JSON)
    @Deprecated
    @Path("{hakuOid}/sijoitteluajo/{sijoitteluajoId}/ilmanhyvaksyntaa")
    List<HakijaDTO> ilmanhyvaksyntaa(@PathParam("hakuOid") String hakuOid,
                                         @PathParam("sijoitteluajoId") String sijoitteluajoId);
    @GET
    @JsonView(JsonViews.Hakija.class)
    @Produces(APPLICATION_JSON)
    @Deprecated
    @Path("{hakuOid}/hakukohde/{hakukohdeOid}/sijoitteluajo/{sijoitteluajoId}/ilmanhyvaksyntaa")
    List<HakijaDTO> ilmanhyvaksyntaa(@PathParam("hakuOid") String hakuOid,
                                         @PathParam("hakukohdeOid") String hakukohdeOid,
                                         @PathParam("sijoitteluajoId") String sijoitteluajoId);

    @GET
    @JsonView(JsonViews.Hakija.class)
    @Produces(APPLICATION_JSON)
    @Deprecated
    @Path("{hakuOid}/sijoitteluajo/{sijoitteluajoId}/vastaanottaneet")
    Collection<HakijaDTO> vastaanottaneet(@PathParam("hakuOid") String hakuOid,
                                          @PathParam("sijoitteluajoId") String sijoitteluajoId);


    @GET
    @JsonView(JsonViews.Hakija.class)
    @Produces(APPLICATION_JSON)
    @Deprecated
    @Path("{hakuOid}/hakukohde/{hakukohdeOid}/sijoitteluajo/{sijoitteluajoId}/vastaanottaneet")
    Collection<HakijaDTO> vastaanottaneet(@PathParam("hakuOid") String hakuOid,
                                          @PathParam("hakukohdeOid") String hakukohdeOid,
                                          @PathParam("sijoitteluajoId") String sijoitteluajoId);
   */
}
