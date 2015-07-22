package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;

public class SijoittelunTila {
    public int depth = 0;
    public final SijoitteluajoWrapper sijoitteluAjo;

    public SijoittelunTila(final SijoitteluajoWrapper sijoitteluAjo) {
        this.sijoitteluAjo = sijoitteluAjo;
    }
}
