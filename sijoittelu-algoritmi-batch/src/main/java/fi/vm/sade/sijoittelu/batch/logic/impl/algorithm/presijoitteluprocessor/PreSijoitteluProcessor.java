package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;

public interface PreSijoitteluProcessor {
    void process(SijoitteluajoWrapper sijoitteluajoWrapper);
}
