package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jussi Jartamo
 */
public class PostSijoitteluProcessorMuutostiedonAsetus implements PostSijoitteluProcessor{
    private static final Logger LOG = LoggerFactory.getLogger(PostSijoitteluProcessorMuutostiedonAsetus.class);
    @Override
    public String name() {
        return getClass().getSimpleName();
    }


    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        sijoitteluajoWrapper.getHakukohteet().stream()
                .flatMap(h -> h.getValintatapajonot().stream())
                .flatMap(v -> v.getHakemukset().stream())
                .forEach(h -> {
                    HashCode hc = null;
                    h.getHakemus().setOnkoMuuttunutViimeSijoittelussa((hc = h.luoHash()).equals(h.getLahtotilanteenHash()));
                    //LOG.error("## {} {}", h.getHakemus().getHakemusOid(), hc);
                });
    }
}
