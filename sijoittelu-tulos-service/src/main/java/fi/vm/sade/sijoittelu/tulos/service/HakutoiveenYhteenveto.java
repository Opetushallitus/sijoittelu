package fi.vm.sade.sijoittelu.tulos.service;

import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakutoiveDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.HakutoiveenValintatapajonoDTO;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.Vastaanotettavuustila;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.YhteenvedonValintaTila;
import fi.vm.sade.sijoittelu.tulos.dto.raportointi.YhteenvedonVastaanottotila;

public class HakutoiveenYhteenveto {
    public final HakutoiveDTO hakutoive;
    public final HakutoiveenValintatapajonoDTO valintatapajono;
    public final YhteenvedonValintaTila valintatila;
    public final YhteenvedonVastaanottotila vastaanottotila;
    public final Vastaanotettavuustila vastaanotettavuustila;
    public final boolean julkaistavissa;

    HakutoiveenYhteenveto(final HakutoiveDTO hakutoive, final HakutoiveenValintatapajonoDTO valintatapajono, final YhteenvedonValintaTila valintatila, final YhteenvedonVastaanottotila vastaanottotila, final Vastaanotettavuustila vastaanotettavuustila, final boolean julkaistavissa) {
        this.hakutoive = hakutoive;
        this.valintatapajono = valintatapajono;
        this.valintatila = valintatila;
        this.vastaanottotila = vastaanottotila;
        this.vastaanotettavuustila = vastaanotettavuustila;
        this.julkaistavissa = julkaistavissa;
    }
}
