package fi.vm.sade.sijoittelu.tulos.dto.comparator;

import fi.vm.sade.sijoittelu.tulos.dto.HakemusDTO;

import java.util.Comparator;

/**
 * 
 * @author Kari Kammonen
 * 
 */
public class HakemusDTOComparator implements Comparator<HakemusDTO> {

    @Override
    public int compare(HakemusDTO o1, HakemusDTO o2) {
        int p1 = o1.getJonosija();
        int p2 = o2.getJonosija();

        if (p1 == p2) {
            Integer v1 = o1.getTasasijaJonosija();
            Integer v2 = o2.getTasasijaJonosija();
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
