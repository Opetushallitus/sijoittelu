package fi.vm.sade.sijoittelu.tulos.service;

import java.util.Date;
import java.util.Optional;

import fi.vm.sade.sijoittelu.tulos.dto.raportointi.*;

public class HakutoiveenYhteenveto {
    public final HakutoiveDTO hakutoive;
    public final HakutoiveenValintatapajonoDTO valintatapajono;
    public final YhteenvedonValintaTila valintatila;
    public final YhteenvedonVastaanottotila vastaanottotila;
    public final Vastaanotettavuustila vastaanotettavuustila;
    public final boolean julkaistavissa;
    public final Optional<Date> viimeisinVastaanottotilanMuutos;

    HakutoiveenYhteenveto(final HakutoiveDTO hakutoive, final HakutoiveenValintatapajonoDTO valintatapajono, final YhteenvedonValintaTila valintatila, final YhteenvedonVastaanottotila vastaanottotila, final Vastaanotettavuustila vastaanotettavuustila, final boolean julkaistavissa, final Optional<Date> viimeisinVastaanottotilanMuutos) {
        this.hakutoive = hakutoive;
        this.valintatapajono = valintatapajono;
        this.valintatila = valintatila;
        this.vastaanottotila = vastaanottotila;
        this.vastaanotettavuustila = vastaanotettavuustila;
        this.julkaistavissa = julkaistavissa;
        this.viimeisinVastaanottotilanMuutos = viimeisinVastaanottotilanMuutos;
    }
}
