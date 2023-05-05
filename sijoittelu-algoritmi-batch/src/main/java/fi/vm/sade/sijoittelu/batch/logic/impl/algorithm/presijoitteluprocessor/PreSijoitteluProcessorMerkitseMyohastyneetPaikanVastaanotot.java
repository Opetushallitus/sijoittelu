package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;

public class PreSijoitteluProcessorMerkitseMyohastyneetPaikanVastaanotot implements PreSijoitteluProcessor {
    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        sijoitteluajoWrapper.getHakukohteet().forEach(hk -> {
            hk.getValintatapajonot().forEach(jono -> {
                if (jono.getMerkitseMyohAuto() && sijoitteluajoWrapper.onkoVarasijaSaannotVoimassaJaVarasijaTayttoPaattynyt(jono)) {
                    jono.getHakemukset().forEach(this::setHakemusToStatusPerunut);
                }
            });
        });
    }

    private void setHakemusToStatusPerunut(HakemusWrapper hakemusWrapper) {
        hakemusWrapper.getValintatulos().map(vt -> {
            vt.setTila(ValintatuloksenTila.EI_VASTAANOTETTU_MAARA_AIKANA, "Ei vastaan otettu määräaikana");
            return vt;
        });
        hakemusWrapper.getHakemus().setTila(HakemuksenTila.PERUNUT);
        hakemusWrapper.setTilaVoidaanVaihtaa(false);
    }
}
