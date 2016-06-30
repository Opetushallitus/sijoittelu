package fi.vm.sade.sijoittelu.tulos.dao.impl;

import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;

public class HakemusCacheObject {
    public final Hakukohde hakukohde;
    public final Valintatapajono valintatapajono;
    public final Hakemus hakemus;

    public HakemusCacheObject(Hakukohde hakukohde, Valintatapajono valintatapajono, Hakemus hakemus) {
        this.hakukohde = hakukohde;
        this.valintatapajono = valintatapajono;
        this.hakemus = hakemus;
    }
}
