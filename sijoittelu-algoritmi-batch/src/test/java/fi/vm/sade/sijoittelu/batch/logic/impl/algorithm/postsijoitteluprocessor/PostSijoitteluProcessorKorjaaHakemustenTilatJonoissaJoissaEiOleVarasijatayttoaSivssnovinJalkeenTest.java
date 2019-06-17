package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.HYLATTY;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.HYVAKSYTTY;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.PERUUNTUNUT;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.VARALLA;
import static fi.vm.sade.sijoittelu.domain.Tasasijasaanto.YLITAYTTO;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.helper.HakuBuilder;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.time.LocalDateTime;
import java.util.Collections;

@RunWith(JUnit4.class)
public class PostSijoitteluProcessorKorjaaHakemustenTilatJonoissaJoissaEiOleVarasijatayttoaSivssnovinJalkeenTest {
    private final PostSijoitteluProcessorKorjaaHakemustenTilatJonoissaJoissaEiOleVarasijatayttoaSivssnovinJalkeen p =
        new PostSijoitteluProcessorKorjaaHakemustenTilatJonoissaJoissaEiOleVarasijatayttoaSivssnovinJalkeen();

    private final Hakemus hakemus1 = new HakuBuilder.HakemusBuilder().withOid("hakemus1")
        .withJonosija(1).withTasasijaJonosija(1).withTila(HYVAKSYTTY).withPrioriteetti(1).build();
    private final Hakemus hakemus2 = new HakuBuilder.HakemusBuilder().withOid("hakemus2")
        .withJonosija(2).withTasasijaJonosija(1).withTila(VARALLA).withEdellinenTila(VARALLA).build();
    private final Hakemus hakemus3 = new HakuBuilder.HakemusBuilder().withOid("hakemus3")
        .withJonosija(3).withTasasijaJonosija(1).withTila(VARALLA).withEdellinenTila(VARALLA).build();
    private final Hakemus hakemus4 = new HakuBuilder.HakemusBuilder().withOid("hakemus4")
        .withJonosija(4).withTasasijaJonosija(1).withTila(VARALLA).withEdellinenTila(VARALLA).build();

    private Valintatapajono jono = new HakuBuilder.ValintatapajonoBuilder().withOid("jono1")
        .withTasasijasaanto(YLITAYTTO)
        .withAloituspaikat(1)
        .withPrioriteetti(0)
        .withHakemukset(hakemus1, hakemus2, hakemus3, hakemus4)
        .withSivssnov(false)
        .build();

    private SijoitteluajoWrapper sijoitteluajoWrapper;

    @Before
    public void valmisteleSijoitteluajoWrapper() {
        Hakukohde hakukohde = new HakuBuilder.HakukohdeBuilder("hk1").withValintatapajono(jono).build();
        sijoitteluajoWrapper = new HakuBuilder.SijoitteluajoWrapperBuilder(Collections.singletonList(hakukohde)).build();
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
    }

    @Test
    public void hakijaNoussutVaralle() {
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
