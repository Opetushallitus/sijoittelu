package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.comparator;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakukohdeWrapper;

import java.util.Comparator;

/**
 * 
 * @author Kari Kammonen
 * 
 */
public class HakukohdeWrapperComparator implements Comparator<HakukohdeWrapper> {

    @Override
    public int compare(HakukohdeWrapper o1, HakukohdeWrapper o2) {
        String oid1 = o1.getHakukohde().getOid();
        String oid2 = o2.getHakukohde().getOid();
        return oid1.compareTo(oid2);
    }

}
