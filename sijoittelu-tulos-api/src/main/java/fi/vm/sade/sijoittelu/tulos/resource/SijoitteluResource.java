package fi.vm.sade.sijoittelu.tulos.resource;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.dto.SijoitteluDTO;
import fi.vm.sade.sijoittelu.tulos.dto.SijoitteluajoDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaPaginationObject;

/**
 *         Autentikointi annotaatiot implementaatiossa!
 */
@Path("sijoittelu")
@Deprecated
//@Api(value = "/sijoittelu", description = "Resurssi sijoittelun tuloksien hakemiseen")
public interface SijoitteluResource {

    static final String LATEST = "latest";

    @GET
    @Path("/{hakuOid}/hyvaksytyt/hakukohde/{hakukohdeOid}")
    @Produces(APPLICATION_JSON)
    HakijaPaginationObject hyvaksytytHakukohteeseen(
            @ApiParam(value = "Haun tunniste", required = true)
            @PathParam("hakuOid") String hakuOid,
            @PathParam("hakukohdeOid") String hakukohdeOid);

    @GET
    @Produces(APPLICATION_JSON)
    @Path("/{hakuOid}/sijoitteluajo/{sijoitteluajoId}/hakukohde/{hakukohdeOid}")
        //@ApiOperation(position = 3, value = "Hakee hakukohteen tiedot tietyssa sijoitteluajossa.", response = HakukohdeDTO.class)
    Response getHakukohdeBySijoitteluajo(
            @ApiParam(value = "Haun tunniste", required = true) @PathParam("hakuOid") String hakuOid,
            @ApiParam(value = "Sijoitteluajon tunniste tai 'latest' avainsana", required = true) @PathParam("sijoitteluajoId") String sijoitteluajoId,
            @ApiParam(value = "Hakukohteen tunniste", required = true) @PathParam("hakukohdeOid") String hakukohdeOid);

    @GET
    @Produces(APPLICATION_JSON)
    @Path("/{hakuOid}/sijoitteluajo/{sijoitteluajoId}/hakukohdedto/{hakukohdeOid}")
        //@ApiOperation(position = 3, value = "Hakee hakukohteen tiedot tietyssa sijoitteluajossa.", response = HakukohdeDTO.class)
    HakukohdeDTO getHakukohdeBySijoitteluajoPlainDTO(
            @ApiParam(value = "Haun tunniste", required = true) @PathParam("hakuOid") String hakuOid,
            @ApiParam(value = "Sijoitteluajon tunniste tai 'latest' avainsana", required = true) @PathParam("sijoitteluajoId") String sijoitteluajoId,
            @ApiParam(value = "Hakukohteen tunniste", required = true) @PathParam("hakukohdeOid") String hakukohdeOid);

    @GET
    @Produces(APPLICATION_JSON)
    @Path("/{hakuOid}/sijoitteluajo/{sijoitteluajoId}/hakemus/{hakemusOid}")
        //@ApiOperation(position = 5, value = "Nayttaa yksittaisen hakemuksen kaikki hakutoiveet ja tiedot kaikista valintatapajonoista", response = HakijaDTO.class)
    HakijaDTO hakemus(
            @ApiParam(value = "Haun tunniste", required = true) @PathParam("hakuOid") String hakuOid,
            @ApiParam(value = "Sijoitteluajon tunniste tai 'latest' avainsana", required = true) @PathParam("sijoitteluajoId") String sijoitteluajoId,
            @ApiParam(value = "Hakemuksen tunniste", required = true) @PathParam("hakemusOid") String hakemusOid);

}
