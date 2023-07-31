package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.presijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluTestSpec;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.helper.HakuBuilder;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.helper.HakuBuilder.HakukohdeBuilder;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.helper.HakuBuilder.SijoitteluajoWrapperBuilder;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.helper.HakuBuilder.ValintatapajonoBuilder;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

public class PreSijoitteluProcessorPidaPeruuntuneinaSivssnovHakemuksetRajatunVarasijataytonJonoissaTest extends SijoitteluTestSpec {
    private final PreSijoitteluProcessorPidaPeruuntuneinaSivssnovHakemuksetRajatunVarasijataytonJonoissa p =
        new PreSijoitteluProcessorPidaPeruuntuneinaSivssnovHakemuksetRajatunVarasijataytonJonoissa();

    private final Hakemus hakemusVaralla = new HakuBuilder.HakemusBuilder().withOid("hakemusVaralla")
        .withTila(HakemuksenTila.VARALLA)
        .withEdellinenTila(HakemuksenTila.VARALLA).build();
    private final Hakemus peruuntunutHakemus = new HakuBuilder.HakemusBuilder().withOid("peruuntunutHakemus")
        .withTila(HakemuksenTila.VARALLA)
        .withEdellinenTila(HakemuksenTila.PERUUNTUNUT).build();

    private Valintatapajono jono = new ValintatapajonoBuilder().withOid("jono1")
        .withHakemus(hakemusVaralla)
        .withHakemus(peruuntunutHakemus)
        .withSivssnov(true)
        .build();

    private SijoitteluajoWrapper sijoitteluajoWrapper;

    @BeforeEach
    public void valmisteleSijoitteluajoWrapper() {
        Hakukohde hakukohde = new HakukohdeBuilder("hk1").withValintatapajono(jono).build();
        sijoitteluajoWrapper = new SijoitteluajoWrapperBuilder(Collections.singletonList(hakukohde)).build();
    }

    @Test
    public void testEiKoskeValintatapajonoihinJoissaOnVapaaVarasijataytto()  {
        jono.setEiVarasijatayttoa(false);
        jono.setVarasijat(0);
        Assertions.assertTrue(jono.vapaaVarasijataytto());

        p.process(sijoitteluajoWrapper);
        Assertions.assertEquals(HakemuksenTila.VARALLA, hakemusVaralla.getTila());
        Assertions.assertEquals(HakemuksenTila.VARALLA, peruuntunutHakemus.getTila());
    }

    @Test
    public void testEiKoskeJonoihinJoitaEiOleSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa() {
        jono.setEiVarasijatayttoa(true);
        Assertions.assertFalse(jono.vapaaVarasijataytto());
        jono.setSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa(false);

        p.process(sijoitteluajoWrapper);
        Assertions.assertEquals(HakemuksenTila.VARALLA, hakemusVaralla.getTila());
        Assertions.assertEquals(HakemuksenTila.VARALLA, peruuntunutHakemus.getTila());
    }

    @Test
    public void sailyttaaPeruuntuneetHakemuksetPeruuntuneinaSivssnovJonoissaIlmanVarasijatayttoa() {
        jono.setEiVarasijatayttoa(true);
        Assertions.assertFalse(jono.vapaaVarasijataytto());

        p.process(sijoitteluajoWrapper);
        Assertions.assertEquals(HakemuksenTila.VARALLA, hakemusVaralla.getTila());
        Assertions.assertEquals(HakemuksenTila.PERUUNTUNUT, peruuntunutHakemus.getTila());

        HakemusWrapper peruuntuneenHakemuksenWrapper = sijoitteluajoWrapper.getHakukohteet().get(0)
            .getValintatapajonot().get(0).getHakemukset().stream().filter(h ->
                h.getHakemus().getHakemusOid().equals(peruuntunutHakemus.getHakemusOid())).findFirst().get();
        Assertions.assertFalse(peruuntuneenHakemuksenWrapper.isTilaVoidaanVaihtaa());
    }

    @Test
    public void sailyttaaPeruuntuneetHakemuksetPeruuntuneinaSivssnovJonoissaJoissaVarasijatayttoOnRajattu() {
        jono.setEiVarasijatayttoa(false);
        jono.setVarasijat(7);
        Assertions.assertTrue(jono.rajoitettuVarasijaTaytto());
        Assertions.assertFalse(jono.vapaaVarasijataytto());

        p.process(sijoitteluajoWrapper);
        Assertions.assertEquals(HakemuksenTila.VARALLA, hakemusVaralla.getTila());
        Assertions.assertEquals(HakemuksenTila.PERUUNTUNUT, peruuntunutHakemus.getTila());

        HakemusWrapper peruuntuneenHakemuksenWrapper = sijoitteluajoWrapper.getHakukohteet().get(0)
            .getValintatapajonot().get(0).getHakemukset().stream().filter(h ->
                h.getHakemus().getHakemusOid().equals(peruuntunutHakemus.getHakemusOid())).findFirst().get();
        Assertions.assertFalse(peruuntuneenHakemuksenWrapper.isTilaVoidaanVaihtaa());
    }
}
