package fi.vm.sade.sijoittelu.laskenta.service.impl;

import fi.vm.sade.service.sijoittelu.SijoitteluService;
import fi.vm.sade.service.valintatiedot.schema.HakuTyyppi;
import fi.vm.sade.service.valintatiedot.schema.HakukohdeTyyppi;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;

import javax.jws.WebParam;

import static fi.vm.sade.sijoittelu.laskenta.roles.SijoitteluRole.CRUD;
import static fi.vm.sade.sijoittelu.laskenta.roles.SijoitteluRole.READ;
import static fi.vm.sade.sijoittelu.laskenta.roles.SijoitteluRole.UPDATE;

/**
 * Created with IntelliJ IDEA.
 * User: kkammone
 * Date: 29.4.2013
 * Time: 9:21
 * To change this template use File | Settings | File Templates.
 */
@PreAuthorize("isAuthenticated()")
public class SijoitteluServiceImpl implements SijoitteluService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SijoitteluServiceImpl.class);

    @Autowired
    private SijoitteluBusinessService sijoitteluBusinessService;

    @Override
    @Secured({CRUD})
    public HakuTyyppi sijoittele(@WebParam(partName = "parameters", name = "sijoittele", targetNamespace = "http://sijoittelu.service.sade.vm.fi/types") HakuTyyppi haku) {
        sijoitteluBusinessService.sijoittele(haku);
        return haku;
    }
}

