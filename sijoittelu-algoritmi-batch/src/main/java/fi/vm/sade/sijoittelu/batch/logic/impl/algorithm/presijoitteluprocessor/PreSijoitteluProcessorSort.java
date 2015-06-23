package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.comparator.HakemusWrapperComparator;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.comparator.HakijaryhmaWrapperComparator;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.comparator.HakukohdeWrapperComparator;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.comparator.ValintatapajonoWrapperComparator;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakukohdeWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;

import java.util.Collections;

public class PreSijoitteluProcessorSort implements PreSijoitteluProcessor {

    @Override
    public String name() {
        return getClass().getSimpleName();
    }

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        this.sortDomain(sijoitteluajoWrapper);

    }

    /**
     * jarjestelee sijoittelun domainin. Ilman tata algoritmin toimintaan ei voi
     * luottaaa (XML data voi olla vaarassa jarjestyksessa)
     */
    private void sortDomain(SijoitteluajoWrapper sijoitteluajoWrapper) {
        for (HakukohdeWrapper hakukohde : sijoitteluajoWrapper.getHakukohteet()) {
            for (ValintatapajonoWrapper valintatapajonoWrapper : hakukohde.getValintatapajonot()) {
                Collections.sort(valintatapajonoWrapper.getHakemukset(), new HakemusWrapperComparator());
            }
            Collections.sort(hakukohde.getValintatapajonot(), new ValintatapajonoWrapperComparator());
            Collections.sort(hakukohde.getHakijaryhmaWrappers(), new HakijaryhmaWrapperComparator());
        }
        Collections.sort(sijoitteluajoWrapper.getHakukohteet(), new HakukohdeWrapperComparator());
    }
}
