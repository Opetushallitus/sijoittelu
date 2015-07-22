package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakukohdeWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Jussi Jartamo
 */
public class PreSijoitteluProcessorLahtotilanteenHash implements PreSijoitteluProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(PreSijoitteluProcessorLahtotilanteenHash.class);
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
                    h.setLahtotilanteenHash(h.luoHash());
                    //LOG.error("## {} {}", h.getHakemus().getHakemusOid(), h.getLahtotilanteenHash());
                });
    }
}
