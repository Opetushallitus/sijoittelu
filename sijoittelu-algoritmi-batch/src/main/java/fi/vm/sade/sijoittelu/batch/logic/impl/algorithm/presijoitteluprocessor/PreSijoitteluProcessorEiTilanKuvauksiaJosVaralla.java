package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.TilanKuvaukset;
import fi.vm.sade.sijoittelu.domain.TilankuvauksenTarkenne;

/* Hakemuksen tilan kuvaukset kopioidaan edellisen sijoittelun tuloksista ennen sijoittelun ajoa
 * (katso SijoitteluBusinessService::merge). Näin voidaan haluttaessa säilyttää aiemmassa sijoittelussa asetettu tilan
 * kuvaus mikäli hakemuksen tila pysyy samana (esim. hakemuksen pitäminen peruuntuneena). Tämä esiprosessori poistaa
 * edellisen sijoittelun tuloksista kopioidut tilan kuvaukset varalla olevilta hakemuksilta. Muussa tilassa olevien
 * hakemusten tilan kuvauksien oletetaan olevan oikein.
 */
public class PreSijoitteluProcessorEiTilanKuvauksiaJosVaralla implements PreSijoitteluProcessor {
    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        sijoitteluajoWrapper.getHakukohteet().forEach(hk -> {
            hk.getValintatapajonot().forEach(j -> {
                j.getHakemukset().forEach(h -> {
                    if (h.getHakemus().getTila() == HakemuksenTila.VARALLA) {
                        h.getHakemus().setTilankuvauksenTarkenne(TilankuvauksenTarkenne.EI_TILANKUVAUKSEN_TARKENNETTA, TilanKuvaukset.tyhja);
                    }
                });
            });
        });
    }
}
