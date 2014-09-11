package fi.vm.sade.sijoittelu.tulos.dto.raportointi;

import fi.vm.sade.sijoittelu.tulos.dto.IlmoittautumisTila;
import fi.vm.sade.sijoittelu.tulos.dto.ValintatuloksenTila;

/**
 *  Hakemuksen hakutoiveen sijoittelutilan yhteenveto (hakujonokohtaiset tiedot koostettu yhteen)
 */
public class HakutoiveYhteenvetoDTO {
    public final String hakukohdeOid;
    public final String tarjoajaOid;
    public final YhteenvedonValintaTila valintatila;
    public final YhteenvedonVastaanottotila vastaanottotila;
    public final IlmoittautumisTila ilmoittautumistila;
    public final Vastaanotettavuustila vastaanotettavuustila;
    public final Integer jonosija;
    public final Integer varasijanumero;
    public final boolean julkaistavissa;

    public HakutoiveYhteenvetoDTO(final String hakukohdeOid, final String tarjoajaOid, final YhteenvedonValintaTila valintatila, final YhteenvedonVastaanottotila vastaanottotila, final IlmoittautumisTila ilmoittautumistila, final Vastaanotettavuustila vastaanotettavuustila, final Integer jonosija, final Integer varasijanumero, final boolean julkaistavissa) {
        this.hakukohdeOid = hakukohdeOid;
        this.tarjoajaOid = tarjoajaOid;
        this.valintatila = valintatila;
        this.vastaanottotila = vastaanottotila;
        this.ilmoittautumistila = ilmoittautumistila;
        this.vastaanotettavuustila = vastaanotettavuustila;
        this.jonosija = jonosija;
        this.varasijanumero = varasijanumero;
        this.julkaistavissa = julkaistavissa;
    }
}
