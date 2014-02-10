package fi.vm.sade.sijoittelu.tulos.resource;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.dto.JsonViews;
import fi.vm.sade.sijoittelu.tulos.dto.SijoitteluDTO;
import fi.vm.sade.sijoittelu.tulos.dto.SijoitteluajoDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaPaginationObject;
import org.codehaus.jackson.map.annotate.JsonView;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 *
 * @author Jussi Jartamo
 *
 *         Autentikointi annotaatiot implementaatiossa!
 */
@Path("sijoittelu")
@Api(value = "/sijoittelu", description = "Resurssi sijoittelun tuloksien hakemiseen")
public interface SijoitteluResource {

    static final String LATEST = "latest";

    @GET
    @JsonView(JsonViews.Sijoittelu.class)
    @Produces(APPLICATION_JSON)
    @Path("/{hakuOid}")
    @ApiOperation(position = 1, value = "Hakee sijoittelun tiedot haulle. Paa-asiallinen kaytto sijoitteluajojen tunnisteiden hakuun.", response = SijoitteluDTO.class)
    SijoitteluDTO getSijoitteluByHakuOid( @ApiParam(value = "Haun yksilollinen tunniste", required = true) @PathParam("hakuOid") String hakuOid);

    @GET
    @JsonView(JsonViews.Sijoitteluajo.class)
    @Produces(APPLICATION_JSON)
    @Path("/{hakuOid}/sijoitteluajo/{sijoitteluajoId}")
    @ApiOperation(position = 2,value = "Hakee sijoitteluajon tiedot. Paasiallinen kaytto sijoitteluun osallistuvien hakukohteiden hakemiseen", response = SijoitteluajoDTO.class)
    SijoitteluajoDTO getSijoitteluajo( @ApiParam(value = "Haun tunniste", required = true) @PathParam("hakuOid") String hakuOid ,
                                       @ApiParam(value = "Sijoitteluajon tunniste tai 'latest' avainsana", required = true) @PathParam("sijoitteluajoId") String sijoitteluajoId);

    @GET
    @JsonView(JsonViews.Hakukohde.class)
    @Produces(APPLICATION_JSON)
    @Path("/{hakuOid}/sijoitteluajo/{sijoitteluajoId}/hakukohde/{hakukohdeOid}")
    @ApiOperation(position = 3, value = "Hakee hakukohteen tiedot tietyssa sijoitteluajossa.", response = HakukohdeDTO.class)
    Response getHakukohdeBySijoitteluajo( @ApiParam(value = "Haun tunniste", required = true) @PathParam("hakuOid") String hakuOid,
                                              @ApiParam(value = "Sijoitteluajon tunniste tai 'latest' avainsana", required = true) @PathParam("sijoitteluajoId") String sijoitteluajoId,
                                              @ApiParam(value = "Hakukohteen tunniste", required = true) @PathParam("hakukohdeOid") String hakukohdeOid);
    @GET
    @JsonView(JsonViews.Hakija.class)
    @Produces(APPLICATION_JSON)
    @Path("/{hakuOid}/sijoitteluajo/{sijoitteluajoId}/hakemukset")
    @ApiOperation(position = 4, value = "Sivutettu listaus hakemuksien/hakijoiden listaukseen. Yksityiskohtainen listaus kaikista hakutoiveista ja niiden valintatapajonoista", response = HakijaPaginationObject.class)
    HakijaPaginationObject hakemukset( @ApiParam(value = "Haun tunniste", required = true) @PathParam("hakuOid") String hakuOid,
                                       @ApiParam(value = "Sijoitteluajon tunniste tai 'latest' avainsana", required = true) @PathParam("sijoitteluajoId") String sijoitteluajoId,
                                       @ApiParam(value = "Listaa jossakin kohteessa hyvaksytyt", required = false) @QueryParam("hyvaksytyt") Boolean hyvaksytyt,
                                       @ApiParam(value = "Listaa henkilot jotka ovat taysin ilman hyvaksyntaa (missaan kohteessa) ", required = false) @QueryParam("ilmanHyvaksyntaa") Boolean ilmanHyvaksyntaa,
                                       @ApiParam(value = "Listaa henkilot jotka ovat ottaneet paikan lasna tai poissaolevana vastaan", required = false) @QueryParam("vastaanottaneet") Boolean vastaanottaneet,
                                       @ApiParam(value = "Rajoita hakua niin etta naytetaan hakijat jotka ovat jollain toiveella hakeneet naihin kohetisiin", required = false) @QueryParam("hakukohdeOid")  List <String> hakukohdeOid,
                                       @ApiParam(value = "Nayta n kappaletta tuloksia. Kayta sivutuksessa", required = false) @QueryParam("count") Integer count,
                                       @ApiParam(value = "Aloita nayttaminen kohdasta n. Kayta sivutuksessa.", required = false) @QueryParam("index") Integer index);

    @GET
    @JsonView(JsonViews.Hakija.class)
    @Produces(APPLICATION_JSON)
    @Path("/{hakuOid}/sijoitteluajo/{sijoitteluajoId}/hakemus/{hakemusOid}")
    @ApiOperation(position = 5, value = "Nayttaa yksittaisen hakemuksen kaikki hakutoiveet ja tiedot kaikista valintatapajonoista", response = HakijaDTO.class)
    HakijaDTO hakemus( @ApiParam(value = "Haun tunniste", required = true) @PathParam("hakuOid") String hakuOid,
                       @ApiParam(value = "Sijoitteluajon tunniste tai 'latest' avainsana", required = true) @PathParam("sijoitteluajoId") String sijoitteluajoId,
                       @ApiParam(value = "Hakemuksen tunniste", required = true) @PathParam("hakemusOid") String hakemusOid);

}
