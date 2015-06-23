package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;

public interface PostSijoitteluProcessor {
    String name();
    void process(SijoitteluajoWrapper sijoitteluajoWrapper);
}
