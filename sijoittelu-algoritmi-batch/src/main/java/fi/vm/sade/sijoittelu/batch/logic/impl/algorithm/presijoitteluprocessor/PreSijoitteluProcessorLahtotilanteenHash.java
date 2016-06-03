package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import com.google.common.hash.HashCode;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import org.apache.commons.lang3.tuple.Triple;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Jussi Jartamo
 */
public class PreSijoitteluProcessorLahtotilanteenHash implements PreSijoitteluProcessor {

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        final Map<Triple, HashCode> edellisenSijoittelunHashit = new HashMap<>();

        sijoitteluajoWrapper.getEdellisenSijoittelunHakukohteet().ifPresent(hakukohteet ->
                hakukohteet.stream()
                        .forEach(hakukohde ->
                            hakukohde.getValintatapajonot().stream().forEach(valintatapajono ->
                                valintatapajono.getHakemukset().stream().forEach(hakemus -> {
                                    final Triple key = Triple.of(
                                            hakukohde.getOid(),
                                            valintatapajono.getOid(),
                                            hakemus.getHakemusOid()
                                    );
                                    edellisenSijoittelunHashit.put(key, HakemusWrapper.luoHash(hakemus));
                                })
                            )
                        )
        );

        sijoitteluajoWrapper.getHakukohteet().stream()
                .flatMap(h -> h.getValintatapajonot().stream())
                .flatMap(v -> v.getHakemukset().stream())
                .forEach(h -> {
                    final Triple key = Triple.of(
                            h.getHakukohdeOid(),
                            h.getValintatapajono().getValintatapajono().getOid(),
                            h.getHakemus().getHakemusOid()
                    );
                    if (edellisenSijoittelunHashit.containsKey(key)) {
                        h.setLahtotilanteenHash(edellisenSijoittelunHashit.get(key));
                    } else {
                        h.setLahtotilanteenHash(HakemusWrapper.luoHash(h.getHakemus()));
                    }
                });
    }
}
