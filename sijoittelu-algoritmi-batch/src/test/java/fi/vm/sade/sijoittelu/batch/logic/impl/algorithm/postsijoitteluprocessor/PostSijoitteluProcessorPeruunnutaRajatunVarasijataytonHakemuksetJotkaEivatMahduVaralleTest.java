package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.*;
import static fi.vm.sade.sijoittelu.domain.Tasasijasaanto.YLITAYTTO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.helper.HakuBuilder.HakemusBuilder;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.helper.HakuBuilder.HakukohdeBuilder;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.helper.HakuBuilder.SijoitteluajoWrapperBuilder;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.helper.HakuBuilder.ValintatapajonoBuilder;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Collections;

public class PostSijoitteluProcessorPeruunnutaRajatunVarasijataytonHakemuksetJotkaEivatMahduVaralleTest {
    private final PostSijoitteluProcessorPeruunnutaRajatunVarasijataytonHakemuksetJotkaEivatMahduVaralle p =
        new PostSijoitteluProcessorPeruunnutaRajatunVarasijataytonHakemuksetJotkaEivatMahduVaralle();

    private final Hakemus hakemus1 = new HakemusBuilder().withOid("hakemus1")
        .withJonosija(1).withTila(HYVAKSYTTY).withPrioriteetti(1).build();
    private final Hakemus hakemus2 = new HakemusBuilder().withOid("hakemus2")
        .withJonosija(2).withTila(VARALLA).withEdellinenTila(VARALLA).build();
    private final Hakemus hakemus3 = new HakemusBuilder().withOid("hakemus3")
        .withJonosija(3).withTila(VARALLA).withEdellinenTila(VARALLA).build();
    private final Hakemus hakemus4 = new HakemusBuilder().withOid("hakemus4")
        .withJonosija(4).withTila(VARALLA).withEdellinenTila(VARALLA).build();

    private Valintatapajono jono = new ValintatapajonoBuilder().withOid("jono1")
        .withTasasijasaanto(YLITAYTTO)
        .withAloituspaikat(1)
        .withPrioriteetti(0)
        .withHakemukset(hakemus1, hakemus2, hakemus3, hakemus4)
        .withSivssnov(false)
        .build();

    private SijoitteluajoWrapper sijoitteluajoWrapper;

    @Before
    public void valmisteleSijoitteluajoWrapper() {
        Hakukohde hakukohde = new HakukohdeBuilder("hk1").withValintatapajono(jono).build();
        sijoitteluajoWrapper = new SijoitteluajoWrapperBuilder(Collections.singletonList(hakukohde)).build();
        assertTrue(sijoitteluajoWrapper.varasijaSaannotVoimassa());
    }

    @Test
    public void peruunnutetaanKaikkiVarallaolijatJonostaJossaEiOleVarasijatayttoa() {
        jono.setEiVarasijatayttoa(true);
        sijoitteluajoWrapper.setKKHaku(true);

        p.process(sijoitteluajoWrapper);

        assertEquals(HYVAKSYTTY, hakemus1.getTila());
        assertEquals(PERUUNTUNUT, hakemus2.getTila());
        assertEquals(PERUUNTUNUT, hakemus3.getTila());
        assertEquals(PERUUNTUNUT, hakemus4.getTila());
    }

    @Test
    public void jatetaanToisellaAsteellaVaralleKaikkiVarallaolijatJonostaJossaEiOleVarasijatayttoa() {
        jono.setEiVarasijatayttoa(true);
        sijoitteluajoWrapper.setKKHaku(false);

        p.process(sijoitteluajoWrapper);

        assertEquals(HYVAKSYTTY, hakemus1.getTila());
        assertEquals(VARALLA, hakemus2.getTila());
        assertEquals(VARALLA, hakemus3.getTila());
        assertEquals(VARALLA, hakemus4.getTila());
    }

    @Test
    public void peruunnutetaanVarallaolijatJotkaEivatMahduVarasijoilleKunVarasijojenMaaraOnRajoitettu() {
        jono.setEiVarasijatayttoa(false);
        jono.setVarasijat(2);

        p.process(sijoitteluajoWrapper);

        assertEquals(HYVAKSYTTY, hakemus1.getTila());
        assertEquals(VARALLA, hakemus2.getTila());
        assertEquals(VARALLA, hakemus3.getTila());
        assertEquals(PERUUNTUNUT, hakemus4.getTila());
    }

    @Test
    public void eiTehdaMitaanJonoilleJoillaOnVapaaVarasijataytto() {
        jono.setEiVarasijatayttoa(false);
        jono.setVarasijat(null);
        assertTrue(jono.vapaaVarasijataytto());

        p.process(sijoitteluajoWrapper);

        assertEquals(HYVAKSYTTY, hakemus1.getTila());
        assertEquals(VARALLA, hakemus2.getTila());
        assertEquals(VARALLA, hakemus3.getTila());
        assertEquals(VARALLA, hakemus4.getTila());
    }

    @Test
    public void eiTehdaMitaanJonoilleJotkaOnAiemminSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa() {
        jono.setSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa(true);
        jono.setEiVarasijatayttoa(true);

        p.process(sijoitteluajoWrapper);

        assertEquals(HYVAKSYTTY, hakemus1.getTila());
        assertEquals(VARALLA, hakemus2.getTila());
        assertEquals(VARALLA, hakemus3.getTila());
        assertEquals(VARALLA, hakemus4.getTila());

        jono.setEiVarasijatayttoa(false);
        jono.setVarasijat(2);

        p.process(sijoitteluajoWrapper);

        assertEquals(HYVAKSYTTY, hakemus1.getTila());
        assertEquals(VARALLA, hakemus2.getTila());
        assertEquals(VARALLA, hakemus3.getTila());
        assertEquals(VARALLA, hakemus4.getTila());
    }

    @Test
    public void eiTehdaMitaanJosHaunVarasijasaannotEivatOleVoimassa() {
        sijoitteluajoWrapper.setVarasijaSaannotAstuvatVoimaan(LocalDateTime.now().plusDays(7));
        jono.setEiVarasijatayttoa(true);

        p.process(sijoitteluajoWrapper);

        assertEquals(HYVAKSYTTY, hakemus1.getTila());
        assertEquals(VARALLA, hakemus2.getTila());
        assertEquals(VARALLA, hakemus3.getTila());
        assertEquals(VARALLA, hakemus4.getTila());

        jono.setEiVarasijatayttoa(false);
        jono.setVarasijat(2);

        p.process(sijoitteluajoWrapper);

        assertEquals(HYVAKSYTTY, hakemus1.getTila());
        assertEquals(VARALLA, hakemus2.getTila());
        assertEquals(VARALLA, hakemus3.getTila());
        assertEquals(VARALLA, hakemus4.getTila());
    }

    @Test
    public void hakijaNoussutVaralle1() {
        sijoitteluajoWrapper.setVarasijaSaannotAstuvatVoimaan(LocalDateTime.now().minusDays(7));
        sijoitteluajoWrapper.setKKHaku(true);
        jono.setEiVarasijatayttoa(false);
        jono.setVarasijat(2);
        jono.setSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa(true);

        hakemus2.setEdellinenTila(HYLATTY);
        hakemus4.setEdellinenTila(HYLATTY);

        p.process(sijoitteluajoWrapper);

        assertEquals(HYVAKSYTTY, hakemus1.getTila());
        assertEquals(VARALLA, hakemus2.getTila());
        assertEquals(VARALLA, hakemus3.getTila());
        assertEquals(PERUUNTUNUT, hakemus4.getTila());
    }

    @Test
    public void hakijaNoussutVaralle2() {
        sijoitteluajoWrapper.setVarasijaSaannotAstuvatVoimaan(LocalDateTime.now().minusDays(7));
        sijoitteluajoWrapper.setKKHaku(true);
        jono.setEiVarasijatayttoa(true);
        jono.setVarasijat(null);
        jono.setSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa(true);

        hakemus2.setEdellinenTila(HYLATTY);
        hakemus4.setEdellinenTila(HYLATTY);

        p.process(sijoitteluajoWrapper);

        assertEquals(HYVAKSYTTY, hakemus1.getTila());
        assertEquals(PERUUNTUNUT, hakemus2.getTila());
        assertEquals(PERUUNTUNUT, hakemus3.getTila());
        assertEquals(PERUUNTUNUT, hakemus4.getTila());
    }
}
