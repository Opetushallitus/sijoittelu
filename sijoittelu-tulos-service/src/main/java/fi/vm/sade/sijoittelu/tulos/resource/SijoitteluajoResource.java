package fi.vm.sade.sijoittelu.tulos.resource;

import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.sijoittelu.tulos.dao.DAO;
import fi.vm.sade.sijoittelu.tulos.dao.exception.SijoitteluEntityNotFoundException;
import org.codehaus.jackson.map.annotate.JsonView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static fi.vm.sade.sijoittelu.tulos.roles.SijoitteluRole.*;

/**
 * User: wuoti
 * Date: 29.4.2013
 * Time: 15.34
 */
@Path("sijoitteluajo")
@Component
@PreAuthorize("isAuthenticated()")
public class SijoitteluajoResource {

    private final static Logger LOGGER = LoggerFactory.getLogger(SijoitteluajoResource.class);


    @Autowired
    private DAO dao;

    @GET
    @JsonView(JsonViews.Basic.class)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{sijoitteluajoId}")
    @Secured({READ, UPDATE, CRUD})
    public SijoitteluAjo getSijoitteluajo(@PathParam("sijoitteluajoId") Long sijoitteluajoId) {
        try {
            return dao.getSijoitteluajo(sijoitteluajoId);
        } catch (SijoitteluEntityNotFoundException e) {
            throw new WebApplicationException(e, Response.Status.NOT_FOUND);
        }
    }

    @GET
    @JsonView(JsonViews.Basic.class)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{sijoitteluajoId}/{hakukohdeOid}")
    @Secured({READ, UPDATE, CRUD})
    public Hakukohde getHakukohdeBySijoitteluajo(@PathParam("sijoitteluajoId") Long sijoitteluajoId,
                                                 @PathParam("hakukohdeOid") String hakukohdeOid) {
        try {
            Hakukohde hakukohde = dao.getHakukohdeBySijoitteluajo(sijoitteluajoId, hakukohdeOid);
            System.out.println("no tää on tää metodi");
            System.out.println( tulostaHakukohde(hakukohde));
            if (hakukohde == null) {
                throw new SijoitteluEntityNotFoundException("No hakukohde found for sijoitteluajo " + sijoitteluajoId
                        + " with and hakukohde OID " + hakukohdeOid);
            }
            return hakukohde;
        } catch (SijoitteluEntityNotFoundException e) {
            throw new WebApplicationException(e, Response.Status.NOT_FOUND);
        }
    }
    /*
   @POST
   @JsonView(JsonViews.Basic.class)
   @Produces(MediaType.APPLICATION_JSON)
   @Path("/{hakukohdeOid}/{valintatapajonoOid}")
   public Hakukohde gg(@PathParam("sijoitteluajoId") Long sijoitteluajoId,
                       @PathParam("hakukohdeOid") String hakukohdeOid) {
       try {
           Hakukohde hakukohde = dao.getHakukohdeBySijoitteluajo(sijoitteluajoId, hakukohdeOid);
           if (hakukohde == null) {
               throw new SijoitteluEntityNotFoundException("No hakukohde found for sijoitteluajo " + sijoitteluajoId
                       + " with and hakukohde OID " + hakukohdeOid);
           }
           return hakukohde;
       } catch (SijoitteluEntityNotFoundException e) {
           throw new WebApplicationException(e, Response.Status.NOT_FOUND);
       }
   }
      */
    public final static String tulostaHakukohde(Hakukohde s) {

        StringBuilder sb = new StringBuilder();

        sb.append("===================================================\n");
        sb.append("Hakukohde: " + s.getOid() + "\n");
        sb.append("===================================================\n");

        sb.append("HAKUKOHDE: [" + s.getOid() + "]\n");

        for (Valintatapajono jono : s.getValintatapajonot()) {
            sb.append("  JONO [" + jono.getOid() + "], prioriteetti [" + jono.getPrioriteetti() + "] aloituspaikat [" + jono.getAloituspaikat() + "], tasasijasaanto [" + jono.getTasasijasaanto() + "]\n");
            for (Hakemus hakemus : jono.getHakemukset()) {
                sb.append("          " + hakemus.getJonosija() + "." + hakemus.getTasasijaJonosija()  + "  " + hakemus.getHakijaOid() + " " + hakemus.getHakemusOid() + " "  + hakemus.getTila() + " hakijan prijo:" + hakemus.getPrioriteetti() + "\n");
            }
        }
        sb.append("===================================================\n");
        return sb.toString();
    }
}
