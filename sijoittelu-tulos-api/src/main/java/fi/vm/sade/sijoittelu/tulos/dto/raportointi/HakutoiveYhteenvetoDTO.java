package fi.vm.sade.sijoittelu.tulos.dto.raportointi;

import fi.vm.sade.sijoittelu.tulos.dto.HakemuksenTila;
import fi.vm.sade.sijoittelu.tulos.dto.IlmoittautumisTila;
import fi.vm.sade.sijoittelu.tulos.dto.ValintatuloksenTila;

public class HakutoiveYhteenvetoDTO {

    public final String hakukohdeOid;
    public final String tarjoajaOid;
    public final HakemuksenTila tila;
    public final ValintatuloksenTila vastaanottotieto;
    public final IlmoittautumisTila ilmoittautumisTila;
    public final Integer jonosija;
    public final Integer varasijanNumero;

    public HakutoiveYhteenvetoDTO(String hakukohdeOid, String tarjoajaOid, HakemuksenTila tila, ValintatuloksenTila vastaanottotieto, IlmoittautumisTila ilmoittautumisTila, Integer jonosija, Integer varasijanNumero) {
        this.hakukohdeOid = hakukohdeOid;
        this.tarjoajaOid = tarjoajaOid;
        this.tila = tila;
        this.vastaanottotieto = vastaanottotieto;
        this.ilmoittautumisTila = ilmoittautumisTila;
        this.jonosija = jonosija;
        this.varasijanNumero = varasijanNumero;
    }
}
