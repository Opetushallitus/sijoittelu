package fi.vm.sade.sijoittelu.laskenta.resource;

import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.sijoittelu.tulos.dao.DAO;
import fi.vm.sade.sijoittelu.tulos.dao.exception.SijoitteluEntityNotFoundException;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.JsonViews;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import org.codehaus.jackson.map.annotate.JsonView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created with IntelliJ IDEA.
 * User: kkammone
 * Date: 20.5.2013
 * Time: 17:23
 * To change this template use File | Settings | File Templates.
 */
@Path("/hakemus")
@Component
public class TilaResource {

    @Autowired
    private SijoitteluBusinessService sijoitteluBusinessService;

    @GET
    @JsonView(JsonViews.Basic.class)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{hakukohdeOid}/{valintatapajonoOid}/{hakemusOid}")
    public boolean hakemus(@PathParam("sijoitteluajoId") Long sijoitteluajoId,
                           @PathParam("hakukohdeOid") String hakukohdeOid,
                           @PathParam("hakemusOid") String hakemusOid) {



     /*   try {
            Hakukohde hakukohde = dao.getHakukohdeBySijoitteluajo(sijoitteluajoId, hakukohdeOid);
            if (hakukohde == null) {
                throw new SijoitteluEntityNotFoundException("No hakukohde found for sijoitteluajo " + sijoitteluajoId
                        + " with and hakukohde OID " + hakukohdeOid);
            }
            return hakukohde;
        } catch (SijoitteluEntityNotFoundException e) {
            throw new WebApplicationException(e, Response.Status.NOT_FOUND);
        }
        */
        return true;
    }


    @POST
    @JsonView(JsonViews.Basic.class)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{hakukohdeOid}/{valintatapajonoOid}/{hakemusOid}/tila")
    public boolean muutaHakemuksenTilaa(@PathParam("sijoitteluajoId") Long sijoitteluajoId,
                                        @PathParam("hakukohdeOid") String hakukohdeOid,
                                        @PathParam("hakemusOid") String hakemusOid,
                                        ValintatuloksenTila tila) {

        sijoitteluBusinessService.vaihdaHakemuksenTila(sijoitteluajoId,hakukohdeOid ,hakemusOid,tila );
     /*   try {
            Hakukohde hakukohde = dao.getHakukohdeBySijoitteluajo(sijoitteluajoId, hakukohdeOid);
            if (hakukohde == null) {
                throw new SijoitteluEntityNotFoundException("No hakukohde found for sijoitteluajo " + sijoitteluajoId
                        + " with and hakukohde OID " + hakukohdeOid);
            }
            return hakukohde;
        } catch (SijoitteluEntityNotFoundException e) {
            throw new WebApplicationException(e, Response.Status.NOT_FOUND);
        }
        */
        return true;
    }


}



