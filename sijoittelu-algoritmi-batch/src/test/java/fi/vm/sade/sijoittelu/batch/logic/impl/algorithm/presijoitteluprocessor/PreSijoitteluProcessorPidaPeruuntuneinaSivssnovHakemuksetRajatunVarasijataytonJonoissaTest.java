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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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

    @Before
    public void valmisteleSijoitteluajoWrapper() {
        Hakukohde hakukohde = new HakukohdeBuilder("hk1").withValintatapajono(jono).build();
        sijoitteluajoWrapper = new SijoitteluajoWrapperBuilder(Collections.singletonList(hakukohde)).build();
    }

    @Test
    public void testEiKoskeValintatapajonoihinJoissaOnVapaaVarasijataytto()  {
        jono.setEiVarasijatayttoa(false);
        jono.setVarasijat(0);
        Assert.assertTrue(jono.vapaaVarasijataytto());

        p.process(sijoitteluajoWrapper);
        Assert.assertEquals(HakemuksenTila.VARALLA, hakemusVaralla.getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, peruuntunutHakemus.getTila());
    }

    @Test
    public void testEiKoskeJonoihinJoitaEiOleSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa() {
        jono.setEiVarasijatayttoa(true);
        Assert.assertFalse(jono.vapaaVarasijataytto());
        jono.setSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa(false);

        p.process(sijoitteluajoWrapper);
        Assert.assertEquals(HakemuksenTila.VARALLA, hakemusVaralla.getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, peruuntunutHakemus.getTila());
    }

    @Test
    public void sailyttaaPeruuntuneetHakemuksetPeruuntuneinaSivssnovJonoissaIlmanVarasijatayttoa() {
        jono.setEiVarasijatayttoa(true);
        Assert.assertFalse(jono.vapaaVarasijataytto());

        p.process(sijoitteluajoWrapper);
        Assert.assertEquals(HakemuksenTila.VARALLA, hakemusVaralla.getTila());
        Assert.assertEquals(HakemuksenTila.PERUUNTUNUT, peruuntunutHakemus.getTila());

        HakemusWrapper peruuntuneenHakemuksenWrapper = sijoitteluajoWrapper.getHakukohteet().get(0)
            .getValintatapajonot().get(0).getHakemukset().stream().filter(h ->
                h.getHakemus().getHakemusOid().equals(peruuntunutHakemus.getHakemusOid())).findFirst().get();
        Assert.assertFalse(peruuntuneenHakemuksenWrapper.isTilaVoidaanVaihtaa());
    }

    @Test
    public void sailyttaaPeruuntuneetHakemuksetPeruuntuneinaSivssnovJonoissaJoissaVarasijatayttoOnRajattu() {
        jono.setEiVarasijatayttoa(false);
        jono.setVarasijat(7);
        Assert.assertTrue(jono.rajoitettuVarasijaTaytto());
        Assert.assertFalse(jono.vapaaVarasijataytto());

        p.process(sijoitteluajoWrapper);
        Assert.assertEquals(HakemuksenTila.VARALLA, hakemusVaralla.getTila());
        Assert.assertEquals(HakemuksenTila.PERUUNTUNUT, peruuntunutHakemus.getTila());

        HakemusWrapper peruuntuneenHakemuksenWrapper = sijoitteluajoWrapper.getHakukohteet().get(0)
            .getValintatapajonot().get(0).getHakemukset().stream().filter(h ->
                h.getHakemus().getHakemusOid().equals(peruuntunutHakemus.getHakemusOid())).findFirst().get();
        Assert.assertFalse(peruuntuneenHakemuksenWrapper.isTilaVoidaanVaihtaa());
    }
}
