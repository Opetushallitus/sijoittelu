package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import com.google.common.hash.HashCode;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;

/**
 * @author Jussi Jartamo
 */
public class PostSijoitteluProcessorMuutostiedonAsetus implements PostSijoitteluProcessor{


    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        sijoitteluajoWrapper.getHakukohteet().stream()
                .flatMap(h -> h.getValintatapajonot().stream())
                .flatMap(v -> v.getHakemukset().stream())
                .forEach(h -> {
                    HashCode hc = null;
                    h.getHakemus().setOnkoMuuttunutViimeSijoittelussa(!(hc = h.luoHash()).equals(h.getLahtotilanteenHash()));
                    //LOG.error("## {} {}", h.getHakemus().getHakemusOid(), hc);
                });
    }
}
