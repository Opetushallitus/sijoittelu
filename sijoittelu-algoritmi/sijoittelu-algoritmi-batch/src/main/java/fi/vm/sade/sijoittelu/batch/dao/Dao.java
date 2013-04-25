package fi.vm.sade.sijoittelu.batch.dao;

import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;

/**
 * 
 * @author Kari Kammonen
 * 
 */
public interface Dao {

    Sijoittelu loadSijoittelu(long sijoitteluId);

    SijoitteluAjo loadSijoitteluajo(Long ajoId);

    void persistSijoittelu(Sijoittelu sijoittelu);

    void persistSijoitteluAjo(SijoitteluAjo sijoitteluAjo);

}
