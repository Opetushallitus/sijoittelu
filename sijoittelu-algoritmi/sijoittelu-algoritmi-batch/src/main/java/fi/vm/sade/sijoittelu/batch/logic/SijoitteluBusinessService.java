package fi.vm.sade.sijoittelu.batch.logic;

import fi.vm.sade.service.sijoittelu.types.SijoitteleTyyppi;

/**
 * 
 * @author Kari Kammonen
 * 
 */
public interface SijoitteluBusinessService {

    void sijoittele(SijoitteleTyyppi sijoitteluTyyppi);

}
