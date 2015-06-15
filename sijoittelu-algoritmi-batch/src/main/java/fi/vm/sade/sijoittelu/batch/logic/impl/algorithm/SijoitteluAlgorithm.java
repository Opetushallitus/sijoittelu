package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;

public interface SijoitteluAlgorithm {
    void start();

    SijoitteluajoWrapper getSijoitteluAjo();
}
