package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;

/**
 * 
 * @author Kari Kammonen
 * 
 */
public interface PreSijoitteluProcessor {

    void process(SijoitteluajoWrapper sijoitteluajoWrapper);

}
