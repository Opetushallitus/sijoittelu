package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.PERUUNTUNUT;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;

import java.util.function.Consumer;

public class PreSijoitteluProcessorPidaPeruuntuneinaSivssnovHakemuksetRajatunVarasijataytonJonoissa implements PreSijoitteluProcessor {
    private final Consumer<HakemusWrapper> lukitseEdellinenPeruuntunutTila = hakemusWrapper -> {
        Hakemus hakemus = hakemusWrapper.getHakemus();
        if (PERUUNTUNUT.equals(hakemus.getEdellinenTila()) && !hakemusWrapper.getHyvaksyPeruuntunut()) {
            hakemus.setTila(HakemuksenTila.PERUUNTUNUT);
            hakemusWrapper.setTilaVoidaanVaihtaa(false);
        }
    };

    @Override
    public void process(SijoitteluajoWrapper sijoitteluajoWrapper) {
        sijoitteluajoWrapper.getHakukohteet().forEach(hakukohdeWrapper ->
            hakukohdeWrapper.getValintatapajonot().forEach(this::process)
        );
    }

    private void process(ValintatapajonoWrapper valintatapajonoWrapper) {
        Valintatapajono jono = valintatapajonoWrapper.getValintatapajono();
        if (jono.getSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa() && !jono.vapaaVarasijataytto()) {
            valintatapajonoWrapper.getHakemukset().forEach(lukitseEdellinenPeruuntunutTila);
        }
    }
}
