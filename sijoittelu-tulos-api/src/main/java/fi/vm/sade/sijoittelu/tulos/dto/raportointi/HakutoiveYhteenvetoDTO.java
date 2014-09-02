package fi.vm.sade.sijoittelu.tulos.dto.raportointi;

import fi.vm.sade.sijoittelu.tulos.dto.IlmoittautumisTila;
import fi.vm.sade.sijoittelu.tulos.dto.ValintatuloksenTila;

/**
 *  Hakemuksen hakutoiveen sijoittelutilan yhteenveto (hakujonokohtaiset tiedot koostettu yhteen)
 */
public class HakutoiveYhteenvetoDTO {

    public final String hakukohdeOid;
    public final String tarjoajaOid;
    public final YhteenvedonTila valintatila;
    public final ValintatuloksenTila vastaanottotila;
    public final IlmoittautumisTila ilmoittautumistila;
    public final Vastaanotettavuustila vastaanotettavuustila;
    public final Integer jonosija;
    public final Integer varasijanumero;


    public HakutoiveYhteenvetoDTO(String hakukohdeOid,
                                  String tarjoajaOid,
                                  YhteenvedonTila valintatila,
                                  ValintatuloksenTila vastaanottotila,
                                  IlmoittautumisTila ilmoittautumistila,
                                  Vastaanotettavuustila vastaanotettavuustila,
                                  Integer jonosija,
                                  Integer varasijanumero) {
        this.hakukohdeOid = hakukohdeOid;
        this.tarjoajaOid = tarjoajaOid;
        this.valintatila = valintatila;
        this.vastaanottotila = vastaanottotila;
        this.ilmoittautumistila = ilmoittautumistila;
        this.jonosija = jonosija;
        this.varasijanumero = varasijanumero;
        this.vastaanotettavuustila = vastaanotettavuustila;
    }
}
