package fi.vm.sade.sijoittelu.laskenta.dao;

import fi.vm.sade.sijoittelu.domain.Sijoittelu;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;

/**
 * 
 * @author Kari Kammonen
 * 
 */
public interface Dao {

    Sijoittelu loadSijoittelu(String hakuOid);

    SijoitteluAjo loadSijoitteluajo(Long ajoId);

    void persistSijoittelu(Sijoittelu sijoittelu);

    void persistSijoitteluAjo(SijoitteluAjo sijoitteluAjo);

}
