package fi.vm.sade.sijoittelu.laskenta.service.impl;

import fi.vm.sade.service.sijoittelu.SijoitteluService;
import fi.vm.sade.service.sijoittelu.types.SijoitteleTyyppi;
import fi.vm.sade.service.valintatiedot.schema.HakukohdeTyyppi;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.jws.WebParam;

/**
 * Created with IntelliJ IDEA.
 * User: kkammone
 * Date: 29.4.2013
 * Time: 9:21
 * To change this template use File | Settings | File Templates.
 */
public class SijoitteluServiceImpl implements SijoitteluService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SijoitteluServiceImpl.class);

    @Autowired
    private SijoitteluBusinessService sijoitteluBusinessService;

    @Override
    public void sijoittele(@WebParam(partName = "parameters", name = "sijoittele", targetNamespace = "http://sijoittelu.service.sade.vm.fi/types") SijoitteleTyyppi sijoitteleTyyppi) {

        if(LOGGER.isInfoEnabled()) {
            LOGGER.info("Sijoittele :: Haku: " + sijoitteleTyyppi.getTarjonta().getHaku().getOid());
            LOGGER.info("Sijoittele :: Hakukohteet");
            for(HakukohdeTyyppi ht : sijoitteleTyyppi.getTarjonta().getHakukohde())   {
                LOGGER.info("Hakukohde:" + ht.getOid()); ;
            }
        }

        sijoitteluBusinessService.sijoittele(sijoitteleTyyppi);
    }
}
