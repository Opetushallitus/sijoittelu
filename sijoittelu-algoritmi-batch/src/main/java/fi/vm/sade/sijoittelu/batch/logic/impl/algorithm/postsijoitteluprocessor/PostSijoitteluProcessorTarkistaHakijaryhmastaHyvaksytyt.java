package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilaTaulukot.kuuluuHyvaksyttyihinTiloihin;

public class PostSijoitteluProcessorTarkistaHakijaryhmastaHyvaksytyt implements PostSijoitteluProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(PostSijoitteluProcessorTarkistaHakijaryhmastaHyvaksytyt.class);

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        sijoitteluajoWrapper.getHakukohteet().forEach(hk -> {
            hk.getHakijaryhmaWrappers().forEach(hr -> {
                String hakijaryhmaOid = hr.getHakijaryhma().getOid();
                hr.getHakukohdeWrapper().hakukohteenHakemukset().forEach(h -> {
                    if (h.getHakemus().getHyvaksyttyHakijaryhmista().contains(hakijaryhmaOid) &&
                            !kuuluuHyvaksyttyihinTiloihin(h.getHakemus().getTila())) {
                        LOG.error(String.format(
                                "Hakukohteen %s hakijaryhmästä %s jonossa %s hyväksytyksi merkitty hakemus %s on tilassa %s",
                                hr.getHakukohdeWrapper().getHakukohde().getOid(),
                                hakijaryhmaOid,
                                h.getValintatapajono().getValintatapajono().getOid(),
                                h.getHakemus().getHakemusOid(),
                                h.getHakemus().getTila()
                        ));
                    }
                });
            });
        });
    }
}
