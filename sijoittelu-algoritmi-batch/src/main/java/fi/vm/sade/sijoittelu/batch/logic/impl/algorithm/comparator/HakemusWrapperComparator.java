package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.comparator;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.domain.comparator.HakemusComparator;

import java.util.Comparator;

public class HakemusWrapperComparator implements Comparator<HakemusWrapper> {
    private static final HakemusComparator hc = new HakemusComparator();

    @Override
    public int compare(HakemusWrapper o1, HakemusWrapper o2) {
        return hc.compare(o1.getHakemus(), o2.getHakemus());
    }
}
