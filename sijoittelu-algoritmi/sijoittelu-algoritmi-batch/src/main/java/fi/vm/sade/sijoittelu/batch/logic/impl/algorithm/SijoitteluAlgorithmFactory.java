package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;

/**
 * 
 * @author Kari Kammonen
 *
 */
public interface SijoitteluAlgorithmFactory {

    public SijoitteluAlgorithm constructAlgorithm(SijoitteluAjo sijoitteluAjo);
    
}
