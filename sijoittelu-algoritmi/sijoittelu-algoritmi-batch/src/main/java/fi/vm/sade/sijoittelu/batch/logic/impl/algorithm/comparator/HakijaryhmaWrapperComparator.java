package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.comparator;

import java.util.Comparator;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakijaryhmaWrapper;

/**
 * 
 * @author Kari Kammonen
 * 
 */
public class HakijaryhmaWrapperComparator implements Comparator<HakijaryhmaWrapper> {

    @Override
    public int compare(HakijaryhmaWrapper o1, HakijaryhmaWrapper o2) {
        int p1 = o1.getHakijaryhma().getPrioriteetti();
        int p2 = o2.getHakijaryhma().getPrioriteetti();

        if (p1 == p2) {
            return 0;
        } else if (p1 > p2) {
            return 1;
        } else {
            return -1;
        }
    }
}
