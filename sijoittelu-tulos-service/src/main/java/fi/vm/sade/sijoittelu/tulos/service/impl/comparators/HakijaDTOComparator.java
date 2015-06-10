package fi.vm.sade.sijoittelu.tulos.service.impl.comparators;

import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaDTO;

import java.util.Comparator;

public class HakijaDTOComparator implements Comparator<HakijaDTO> {

    @Override
    public int compare(HakijaDTO o1, HakijaDTO o2) {
        return o1.getHakemusOid().compareTo(o2.getHakemusOid());
    }
}
