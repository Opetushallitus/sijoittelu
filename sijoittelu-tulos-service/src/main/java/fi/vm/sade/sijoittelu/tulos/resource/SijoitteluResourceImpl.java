package fi.vm.sade.sijoittelu.tulos.resource;

import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.dto.SijoitteluDTO;
import fi.vm.sade.sijoittelu.tulos.dto.SijoitteluajoDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaDTO;
import fi.vm.sade.sijoittelu.tulos.service.RaportointiService;
import fi.vm.sade.sijoittelu.tulos.service.SijoitteluTulosService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.util.List;

import static fi.vm.sade.sijoittelu.tulos.roles.SijoitteluRole.*;

/**
 * User: wuoti Date: 26.4.2013 Time: 12.41
 */
@Component
@PreAuthorize("isAuthenticated()")
public class SijoitteluResourceImpl implements SijoitteluResource {

    private final static Logger LOGGER = LoggerFactory.getLogger(SijoitteluResourceImpl.class);

    @Autowired
    private SijoitteluTulosService sijoitteluTulosService;

    @Autowired
    private RaportointiService raportointiService;

    @Override
    @Secured({ READ, UPDATE, CRUD })
    public SijoitteluDTO getSijoitteluByHakuOid(String hakuOid) {
        return sijoitteluTulosService.getSijoitteluByHakuOid(hakuOid);
    }

    @Override
    @Secured({ READ, UPDATE, CRUD })
    public SijoitteluajoDTO getSijoitteluajo(String hakuOid, String sijoitteluajoId) {
        SijoitteluAjo ajo  = getSijoitteluAjo(sijoitteluajoId, hakuOid) ;
        return sijoitteluTulosService.getSijoitteluajo(ajo);
    }

    @Override
    @Secured({ READ, UPDATE, CRUD })
    public HakukohdeDTO getHakukohdeBySijoitteluajo(String hakuOid, String sijoitteluajoId, String hakukohdeOid) {
        SijoitteluAjo ajo  = getSijoitteluAjo(sijoitteluajoId, hakuOid) ;
        return sijoitteluTulosService.getHakukohdeBySijoitteluajo(ajo, hakukohdeOid);
    }

    @Override
    public List<String> hakemukset(@PathParam("hakuOid") String hakuOid,
                                      @PathParam("sijoitteluajoId") String sijoitteluajoId,
                                      @QueryParam("vastaanottotieto") List<String> vastaanottotieto,
                                      @QueryParam("tila") List<String> tila,
                                      @QueryParam("hakukohdeOid")  List <String> hakukohdeOid,
                                      @QueryParam("count") Integer count,
                                      @QueryParam("startIndex") Integer startIndex) {


        SijoitteluAjo ajo  = getSijoitteluAjo(sijoitteluajoId, hakuOid) ;
        return raportointiService.hakemukset(ajo, vastaanottotieto, tila, hakukohdeOid,count,startIndex);
    }



    @Override
    @Secured({ READ, UPDATE, CRUD })
    public HakijaDTO hakemus(@PathParam("hakuOid") String hakuOid,
                             @PathParam("sijoitteluajoId") String sijoitteluajoId,
                             @PathParam("hakemusOid") String hakemusOid) {
        SijoitteluAjo ajo  = getSijoitteluAjo(sijoitteluajoId, hakuOid) ;
        return raportointiService.hakemus(ajo, hakemusOid);
    }


    private SijoitteluAjo getSijoitteluAjo(String sijoitteluajoId, String hakuOid) {
        if (LATEST.equals(sijoitteluajoId)) {
            return raportointiService.latestSijoitteluAjoForHaku(hakuOid);
        } else {
            return raportointiService.getSijoitteluAjo(Long.parseLong(sijoitteluajoId));
        }
    }

        /*
    @Override
    @Secured({ READ, UPDATE, CRUD })
    public List<HakemusDTO> getHakemusBySijoitteluajo(String hakuOid, String sijoitteluajoId, String hakemusOid) {
        SijoitteluAjo ajo  = getSijoitteluAjo(sijoitteluajoId, hakuOid) ;
        return sijoitteluTulosService.haeHakukohteetJoihinHakemusOsallistuu(ajo, hakemusOid);
    }
      */


   /*
    @Override
    @Deprecated
    @Secured({ READ, UPDATE, CRUD })
    public List<HakijaDTO> hyvaksytyt(String hakuOid, String sijoitteluajoId) {
        SijoitteluAjo ajo = getSijoitteluAjo(sijoitteluajoId, hakuOid) ;
        return raportointiService.hyvaksytyt(ajo);
    }
    @Override
    @Deprecated
    @Secured({ READ, UPDATE, CRUD })
    public Collection<HakijaDTO> hyvaksytyt(String hakuOid, String hakukohdeOid, String sijoitteluajoId) {
        SijoitteluAjo ajo = getSijoitteluAjo(sijoitteluajoId, hakuOid) ;
        return raportointiService.hyvaksytyt(ajo, hakukohdeOid);
    }
    @Override
    @Deprecated
    @Secured({ READ, UPDATE, CRUD })
    public List<HakijaDTO> ilmanhyvaksyntaa(String hakuOid, String sijoitteluajoId) {
        SijoitteluAjo ajo  = getSijoitteluAjo(sijoitteluajoId, hakuOid) ;
        return raportointiService.ilmanhyvaksyntaa(ajo);
    }
    @Override
    @Deprecated
    @Secured({ READ, UPDATE, CRUD })
    public List<HakijaDTO> ilmanhyvaksyntaa(@PathParam("hakuOid") String hakuOid,
                                            @PathParam("hakukohdeOid") String hakukohdeOid,
                                            @PathParam("sijoitteluajoId") String sijoitteluajoId) {
        SijoitteluAjo ajo  = getSijoitteluAjo(sijoitteluajoId, hakuOid) ;
        return raportointiService.ilmanhyvaksyntaa(ajo, hakukohdeOid);
    }
    @Override
    @Deprecated
    @Secured({ READ, UPDATE, CRUD })
    public Collection<HakijaDTO> vastaanottaneet(@PathParam("hakuOid") String hakuOid,
                                                 @PathParam("sijoitteluajoId") String sijoitteluajoId) {
        SijoitteluAjo ajo  = getSijoitteluAjo(sijoitteluajoId, hakuOid) ;
        return raportointiService.vastaanottaneet(ajo);
    }
    @Override
    @Deprecated
    @Secured({ READ, UPDATE, CRUD })
    public Collection<HakijaDTO> vastaanottaneet(@PathParam("hakuOid") String hakuOid,
                                                 @PathParam("hakukohdeOid") String hakukohdeOid,
                                                 @PathParam("sijoitteluajoId") String sijoitteluajoId) {
        SijoitteluAjo ajo = getSijoitteluAjo(sijoitteluajoId, hakuOid) ;
        return raportointiService.vastaanottaneet(ajo, hakukohdeOid);
    }
    */
}
