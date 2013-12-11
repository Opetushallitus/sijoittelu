package fi.vm.sade.sijoittelu.tulos.resource;

import com.wordnik.swagger.annotations.ApiOperation;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.tulos.dto.HakukohdeDTO;
import fi.vm.sade.sijoittelu.tulos.dto.SijoitteluDTO;
import fi.vm.sade.sijoittelu.tulos.dto.SijoitteluajoDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaPaginationObject;
import fi.vm.sade.sijoittelu.tulos.service.RaportointiService;
import fi.vm.sade.sijoittelu.tulos.service.SijoitteluTulosService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.ws.rs.PathParam;
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
    @ApiOperation(value = "xxxx1", httpMethod = "GET")
    public SijoitteluDTO getSijoitteluByHakuOid(String hakuOid) {
        return sijoitteluTulosService.getSijoitteluByHakuOid(hakuOid);
    }

    @Override
    @Secured({ READ, UPDATE, CRUD })
    @ApiOperation(value = "xxxx2", httpMethod = "GET")
    public SijoitteluajoDTO getSijoitteluajo(String hakuOid, String sijoitteluajoId) {
        SijoitteluAjo ajo  = getSijoitteluAjo(sijoitteluajoId, hakuOid) ;
        return sijoitteluTulosService.getSijoitteluajo(ajo);
    }

    @Override
    @Secured({ READ, UPDATE, CRUD })
    @ApiOperation(value = "xxxx3", httpMethod = "GET")
    public HakukohdeDTO getHakukohdeBySijoitteluajo(String hakuOid, String sijoitteluajoId, String hakukohdeOid) {
        SijoitteluAjo ajo  = getSijoitteluAjo(sijoitteluajoId, hakuOid) ;
        return sijoitteluTulosService.getHakukohdeBySijoitteluajo(ajo, hakukohdeOid);
    }

    @Override
    @Secured({ READ, UPDATE, CRUD })
    @ApiOperation(value = "xxxx4", httpMethod = "GET")
    public HakijaPaginationObject hakemukset( String hakuOid,
                                                   String sijoitteluajoId,
                                                   Boolean hyvaksytyt,
                                                   Boolean ilmanHyvaksyntaa,
                                                   Boolean vastaanottaneet,
                                                   List<String> hakukohdeOid,
                                                   Integer count,
                                                   Integer index) {
        SijoitteluAjo ajo  = getSijoitteluAjo(sijoitteluajoId, hakuOid) ;
        return raportointiService.hakemukset(ajo, hyvaksytyt, ilmanHyvaksyntaa,vastaanottaneet, hakukohdeOid,count,index);
    }

    @Override
    @Secured({ READ, UPDATE, CRUD })
    @ApiOperation(value = "xxxx5", httpMethod = "GET")
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

}
