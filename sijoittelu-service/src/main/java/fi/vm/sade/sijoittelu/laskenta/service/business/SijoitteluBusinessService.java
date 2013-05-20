package fi.vm.sade.sijoittelu.laskenta.service.business;

import fi.vm.sade.service.sijoittelu.types.SijoitteleTyyppi;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;

/**
 * 
 * @author Kari Kammonen
 * 
 */
public interface SijoitteluBusinessService {

    void sijoittele(SijoitteleTyyppi sijoitteluTyyppi);

    void vaihdaHakemuksenTila(Long sijoitteluajoId, String hakukohdeOid, String hakemusOid, ValintatuloksenTila tila);
}
