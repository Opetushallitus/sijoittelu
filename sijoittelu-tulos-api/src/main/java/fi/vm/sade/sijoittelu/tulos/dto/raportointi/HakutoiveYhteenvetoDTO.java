package fi.vm.sade.sijoittelu.tulos.dto.raportointi;

import java.util.Date;

import fi.vm.sade.sijoittelu.tulos.dto.IlmoittautumisTila;

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
    public final Date viimeisinVastaanottotilanMuutos;
    public final Integer jonosija;
    public final Date varasijojaKaytetaanAlkaen;
    public final Date varasijojaTaytetaanAsti;
    public final Integer varasijanumero;
    public final boolean julkaistavissa;

    public HakutoiveYhteenvetoDTO(String hakukohdeOid, String tarjoajaOid, YhteenvedonValintaTila valintatila, YhteenvedonVastaanottotila vastaanottotila, IlmoittautumisTila ilmoittautumistila, Vastaanotettavuustila vastaanotettavuustila, Date viimeisinVastaanottotilanMuutos, Integer jonosija, Date varasijojaKaytetaanAlkaen, Date varasijojaTaytetaanAsti, Integer varasijanumero, boolean julkaistavissa) {
        this.hakukohdeOid = hakukohdeOid;
        this.tarjoajaOid = tarjoajaOid;
        this.valintatila = valintatila;
        this.vastaanottotila = vastaanottotila;
        this.ilmoittautumistila = ilmoittautumistila;
        this.vastaanotettavuustila = vastaanotettavuustila;
        this.viimeisinVastaanottotilanMuutos = viimeisinVastaanottotilanMuutos;
        this.jonosija = jonosija;
        this.varasijojaKaytetaanAlkaen = varasijojaKaytetaanAlkaen;
        this.varasijojaTaytetaanAsti = varasijojaTaytetaanAsti;
        this.varasijanumero = varasijanumero;
        this.julkaistavissa = julkaistavissa;
    }
}
