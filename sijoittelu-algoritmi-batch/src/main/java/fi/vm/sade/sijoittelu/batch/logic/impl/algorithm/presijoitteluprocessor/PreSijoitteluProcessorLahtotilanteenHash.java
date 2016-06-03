package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import com.google.common.hash.HashCode;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jussi Jartamo
 */
public class PreSijoitteluProcessorLahtotilanteenHash implements PreSijoitteluProcessor {

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        final Map<String, HashCode> edellisenSijoittelunHashit = new HashMap<>();

        sijoitteluajoWrapper.getEdellisenSijoittelunHakukohteet().ifPresent(hakukohteet ->
                hakukohteet.stream()
                        .flatMap(h -> h.getValintatapajonot().stream())
                        .flatMap(v -> v.getHakemukset().stream())
                        .forEach(h ->
                                edellisenSijoittelunHashit.put(h.getHakemusOid(), HakemusWrapper.luoHash(h))
                        )
        );

        sijoitteluajoWrapper.getHakukohteet().stream()
                .flatMap(h -> h.getValintatapajonot().stream())
                .flatMap(v -> v.getHakemukset().stream())
                .forEach(h -> {
                    final String hakemusOid = h.getHakemus().getHakemusOid();
                    if (edellisenSijoittelunHashit.containsKey(hakemusOid)) {
                        h.setLahtotilanteenHash(edellisenSijoittelunHashit.get(hakemusOid));
                    } else {
                        h.setLahtotilanteenHash(HakemusWrapper.luoHash(h.getHakemus()));
                    }
                });
    }
}
