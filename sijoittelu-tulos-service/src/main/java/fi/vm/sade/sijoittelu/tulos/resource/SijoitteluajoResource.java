package fi.vm.sade.sijoittelu.tulos.resource;

import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.sijoittelu.domain.comparator.HakemusComparator;
import fi.vm.sade.sijoittelu.domain.dto.HakemusDTO;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    @Path("{sijoitteluajoId}/hakukohde/{hakukohdeOid}")
    @Secured({READ, UPDATE, CRUD})
    public Hakukohde getHakukohdeBySijoitteluajo(@PathParam("sijoitteluajoId") Long sijoitteluajoId,
                                                 @PathParam("hakukohdeOid") String hakukohdeOid) {

        Hakukohde hakukohde = dao.getHakukohdeBySijoitteluajo(sijoitteluajoId, hakukohdeOid);
        HakemusComparator c = new HakemusComparator();
        for(Valintatapajono v : hakukohde.getValintatapajonot()) {
            Collections.sort(v.getHakemukset(), c);
        }
        return hakukohde;

    }

    @GET
    @JsonView(JsonViews.Basic.class)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{sijoitteluajoId}/hakemus/{hakemusOid}")
    @Secured({READ, UPDATE, CRUD})
    public List<HakemusDTO> getHakemusBySijoitteluajo(@PathParam("sijoitteluajoId") Long sijoitteluajoId,
                                                      @PathParam("hakemusOid") String hakemusOid) {

        List<Hakukohde> hakukohdeList = dao.haeHakukohteetJoihinHakemusOsallistuu(sijoitteluajoId, hakemusOid);
        List<HakemusDTO> hakemusDTOList = new ArrayList<HakemusDTO>();

        for(Hakukohde h : hakukohdeList) {
            for(Valintatapajono v : h.getValintatapajonot()) {
                for(Hakemus ha : v.getHakemukset()) {
                    if(hakemusOid.equals(ha.getHakemusOid())) {
                        HakemusDTO dto = new HakemusDTO();
                        hakemusDTOList.add(dto);
                        dto.setEtunimi(ha.getEtunimi());
                        dto.setHakemusOid(ha.getHakemusOid());
                        dto.setHakijaOid(ha.getHakijaOid());
                        dto.setHakukohdeOid(h.getOid());
                        //dto.setHakuOid(h.get);//add to domain later
                        dto.setJonosija(ha.getJonosija());
                        dto.setPrioriteetti(ha.getPrioriteetti());
                        dto.setSijoitteluajoId(h.getSijoitteluajoId());
                        dto.setSukunimi(ha.getSukunimi());
                        dto.setTarjoajaOid(h.getTarjoajaOid());
                        dto.setTasasijaJonosija(ha.getTasasijaJonosija());
                        dto.setTila(ha.getTila());
                        dto.setValintatapajonoOid(v.getOid());
                    }
                }
            }
        }
        return hakemusDTOList;
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



}
