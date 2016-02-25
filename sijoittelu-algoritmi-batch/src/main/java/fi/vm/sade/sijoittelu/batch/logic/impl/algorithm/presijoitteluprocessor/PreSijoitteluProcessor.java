package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;

public interface PreSijoitteluProcessor {

    default String name() {
        return getClass().getSimpleName();
    }

    void process(SijoitteluajoWrapper sijoitteluajoWrapper);
}
