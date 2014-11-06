package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;

/**
 * 
 * @author Kari Kammonen
 * 
 */
public interface SijoitteluAlgorithm {

    /**
     * Kaynnistaa sijoittelualgoritmin
     */
    void start();

    SijoitteluajoWrapper getSijoitteluAjo();

}
