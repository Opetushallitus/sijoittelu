package fi.vm.sade.sijoittelu.tulos.service.impl.comparators;

import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakijaDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.KevytHakijaDTO;

import java.util.Comparator;

public class KevytHakijaDTOComparator implements Comparator<KevytHakijaDTO> {

    @Override
    public int compare(KevytHakijaDTO o1, KevytHakijaDTO o2) {
        return o1.getHakemusOid().compareTo(o2.getHakemusOid());
    }
}
