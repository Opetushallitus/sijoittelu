package fi.vm.sade.sijoittelu.laskenta.service.business;

import fi.vm.sade.service.valintatiedot.schema.HakuTyyppi;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;

/**
 *
 * @author Kari Kammonen
 *
 */
public interface SijoitteluBusinessService {

    void sijoittele(HakuTyyppi sijoitteluTyyppi);

    Valintatulos haeHakemuksenTila(String hakuoid,String hakukohdeOid, String valintatapajonoOid, String hakemusOid);

    void vaihdaHakemuksenTila(String hakuoid, String hakukohdeOid, String valintatapajonoOid, String hakemusOid, ValintatuloksenTila tila, String selite);
}
