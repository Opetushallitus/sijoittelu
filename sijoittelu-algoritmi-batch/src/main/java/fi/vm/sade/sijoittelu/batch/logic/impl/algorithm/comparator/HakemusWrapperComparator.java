package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.comparator;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.domain.comparator.HakemusComparator;

import java.util.Comparator;

public class HakemusWrapperComparator implements Comparator<HakemusWrapper> {
    private static final HakemusComparator hc = new HakemusComparator();

    @Override
    public int compare(HakemusWrapper o1, HakemusWrapper o2) {
        int byHyvaksyPeruuntunut = Boolean.compare(o2.getHyvaksyPeruuntunut(), o1.getHyvaksyPeruuntunut());
        return byHyvaksyPeruuntunut != 0 ? byHyvaksyPeruuntunut : hc.compare(o1.getHakemus(), o2.getHakemus());
    }
}
