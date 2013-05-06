package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.comparator;

import java.util.Comparator;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;

/**
 * 
 * @author Kari Kammonen
 * 
 */
public class HakemusWrapperComparator implements Comparator<HakemusWrapper> {

    @Override
    public int compare(HakemusWrapper o1, HakemusWrapper o2) {
        int p1 = o1.getHakemus().getJonosija();
        int p2 = o2.getHakemus().getJonosija();

        if (p1 == p2) {
            Integer v1 = o1.getHakemus().getTasasijaJonosija();
            Integer v2 = o2.getHakemus().getTasasijaJonosija();
            if (v1 == null && v2 == null) {
                return 0;
            } else if (v1 == null) {
                return 1;
            } else if (v2 == null) {
                return -1;
            } else if (v1 > v2) {
                return 1;
            } else if (v1 < v2) {
                return -1;
            } else {
                return 0;
            }
        } else if (p1 > p2) {
            return 1;
        } else {
            return -1;
        }
    }
}
