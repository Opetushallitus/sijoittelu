package fi.vm.sade.sijoittelu.service.impl;

import fi.vm.sade.service.sijoittelu.SijoitteluService;
import fi.vm.sade.service.sijoittelu.types.SijoitteleTyyppi;
import fi.vm.sade.sijoittelu.service.business.SijoitteluBusinessService;
import org.springframework.beans.factory.annotation.Autowired;
import fi.vm.sade.sijoittelu.service.business.impl.SijoitteluBusinessService;

import javax.jws.WebParam;

/**
 * Created with IntelliJ IDEA.
 * User: kkammone
 * Date: 29.4.2013
 * Time: 9:21
 * To change this template use File | Settings | File Templates.
 */
public class SijoitteluServiceImpl implements SijoitteluService {

    @Autowired
    private SijoitteluBusinessService sijoitteluBusinessService;

    @Override
    public void sijoittele(@WebParam(partName = "parameters", name = "sijoittele", targetNamespace = "http://sijoittelu.service.sade.vm.fi/types") SijoitteleTyyppi parameters) {
        sijoitteluBusinessService.sijoittele(parameters);
    }
}
