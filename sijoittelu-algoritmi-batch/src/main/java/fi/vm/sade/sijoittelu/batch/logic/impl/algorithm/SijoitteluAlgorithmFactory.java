package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;

import java.util.List;

/**
 * 
 * @author Kari Kammonen
 *
 */
public interface SijoitteluAlgorithmFactory {

    public SijoitteluAlgorithm constructAlgorithm(List<Hakukohde> hakukohteet);
    
}
