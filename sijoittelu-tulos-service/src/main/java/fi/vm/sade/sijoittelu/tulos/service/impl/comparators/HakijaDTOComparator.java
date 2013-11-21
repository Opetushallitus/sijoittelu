package fi.vm.sade.sijoittelu.tulos.service.impl.comparators;

import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaDTO;

import java.util.Comparator;

/**
 * Created with IntelliJ IDEA.
 * User: kkammone
 * Date: 21.11.2013
 * Time: 10:02
 * To change this template use File | Settings | File Templates.
 */
public class HakijaDTOComparator implements Comparator<HakijaDTO> {

    @Override
    public int compare(HakijaDTO o1, HakijaDTO o2) {
        return o1.getHakemusOid().compareTo(o2.getHakemusOid());

    }
}
