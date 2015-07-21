package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;

/**
 * @author Jussi Jartamo
 */
public class PostSijoitteluProcessorMuutostiedonAsetus implements PostSijoitteluProcessor{
    @Override
    public String name() {
        return getClass().getSimpleName();
    }


    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        sijoitteluajoWrapper.getHakukohteet().stream()
                .flatMap(h -> h.getValintatapajonot().stream())
                .flatMap(v -> v.getHakemukset().stream())
                .forEach(h ->h.getHakemus().setOnkoMuuttunutViimeSijoittelussa(h.luoHash().equals(h.getLahtotilanteenHash())));
    }
}
