package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostSijoitteluProcessorKorjaaSijoitteluajonTilankuvaukset  implements PostSijoitteluProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(PostSijoitteluProcessorKorjaaSijoitteluajonTilankuvaukset.class);

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        sijoitteluajoWrapper.getHakukohteet().stream()
                .flatMap(hk -> hk.getValintatapajonot().stream())
                .flatMap(v -> v.getHakemukset().stream())
                .filter(h -> (h.getHakemus().getTila() == HakemuksenTila.HYVAKSYTTY
                        || h.getHakemus().getTila() == HakemuksenTila.VARALLA
                        || h.getHakemus().getTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY))
                .forEach(hw -> {
                    if (hw.getHakemus().isTilaSiivottu()) {
                        LOG.info("Hakemuksen {} valintatuloksen tila on {}. " +
                                        "Asetettiin valintatuloksen tilan tarkenteeksi {}. (hakijaOid: {}, hakukohdeOid: {})",
                                hw.getHakemus().getHakemusOid(), hw.getHakemus().getTila(), hw.getHakemus().getTilankuvauksenTarkenne(),
                                hw.getHakemus().getHakijaOid(), hw.getHakukohdeOid());
                    }
                });
    }
}
