package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.comparator;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;

import java.util.Comparator;

/**
 * 
 * @author Kari Kammonen
 * 
 */
public class ValintatapajonoWrapperComparator implements Comparator<ValintatapajonoWrapper> {

    @Override
    public int compare(ValintatapajonoWrapper o1, ValintatapajonoWrapper o2) {
        int p1 = o1.getValintatapajono().getPrioriteetti();
        int p2 = o2.getValintatapajono().getPrioriteetti();

        if (p1 == p2) {
            return 0;
        } else if (p1 > p2) {
            return 1;
        } else {
            return -1;
        }
    }

}
