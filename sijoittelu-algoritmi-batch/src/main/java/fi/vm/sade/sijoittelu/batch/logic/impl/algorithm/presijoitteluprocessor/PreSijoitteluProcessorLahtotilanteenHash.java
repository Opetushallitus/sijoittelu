package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;

/**
 * @author Jussi Jartamo
 */
public class PreSijoitteluProcessorLahtotilanteenHash implements PreSijoitteluProcessor {

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
