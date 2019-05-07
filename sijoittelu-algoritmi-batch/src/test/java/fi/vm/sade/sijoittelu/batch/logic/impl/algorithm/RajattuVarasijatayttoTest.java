package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.HYLATTY;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.HYVAKSYTTY;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.PERUUNTUNUT;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.VARALLA;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.VARASIJALTA_HYVAKSYTTY;
import static fi.vm.sade.sijoittelu.domain.Tasasijasaanto.ALITAYTTO;
import static fi.vm.sade.sijoittelu.domain.Tasasijasaanto.ARVONTA;
import static fi.vm.sade.sijoittelu.domain.Tasasijasaanto.YLITAYTTO;
import static org.junit.Assert.assertEquals;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.helper.HakuBuilder;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.helper.HakuBuilder.HakemusBuilder;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.helper.HakuBuilder.HakukohdeBuilder;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.helper.HakuBuilder.ValintatapajonoBuilder;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.SijoitteluAlgorithmUtil;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.IlmoittautumisTila;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.sijoittelu.domain.Valintatapajono.JonosijaTieto;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import org.junit.Ignore;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RajattuVarasijatayttoTest {
    private final Hakemus hakemus1 = new HakemusBuilder().withOid("hakemus1")
        .withJonosija(1).withTila(VARALLA).withPrioriteetti(1).build();
    private final Hakemus hakemus2 = new HakemusBuilder().withOid("hakemus2")
        .withJonosija(2).withTila(VARALLA).withPrioriteetti(1).build();
    private final Hakemus hakemus3 = new HakemusBuilder().withOid("hakemus3")
        .withJonosija(3).withTila(VARALLA).withPrioriteetti(1).build();
    private final Hakemus hakemus4 = new HakemusBuilder().withOid("hakemus4")
        .withJonosija(4).withTila(VARALLA).withPrioriteetti(1).build();

    private Valintatapajono jono = new ValintatapajonoBuilder().withOid("jono1")
        .withTasasijasaanto(YLITAYTTO)
        .withPrioriteetti(0)
        .withHakemukset(hakemus1, hakemus2, hakemus3, hakemus4)
        .withSivssnov(false)
        .build();

    private Hakukohde hakukohdeJossaVarasijojaRajoitetaan = new HakukohdeBuilder("hakukohdeOid")
        .withValintatapajono(jono).build();
    private Hakukohde toinenHakukohdeJohonHakemus1Hyvaksytaan = new HakukohdeBuilder("toinenHakukohdeOid")
        .withValintatapajono(new ValintatapajonoBuilder()
            .withAloituspaikat(1)
            .withTasasijasaanto(YLITAYTTO)
            .withPrioriteetti(0)
            .withHakemus(new HakemusBuilder()
                .withOid(hakemus1.getHakemusOid())
                .withJonosija(0)
                .withPrioriteetti(hakemus1.getPrioriteetti() - 1)
                .withTila(VARALLA).build())
            .build()).build();
    private Hakukohde toinenHakukohdeJohonHakemus2Hyvaksytaan = new HakukohdeBuilder("kolmasHakukohdeOid")
            .withValintatapajono(new ValintatapajonoBuilder()
                    .withAloituspaikat(1)
                    .withTasasijasaanto(YLITAYTTO)
                    .withPrioriteetti(0)
                    .withSivssnov(true)
                    .withHakemus(new HakemusBuilder()
                            .withOid(hakemus2.getHakemusOid())
                            .withJonosija(0)
                            .withPrioriteetti(hakemus2.getPrioriteetti() - 1)
                            .withTila(VARALLA).build())
                    .build()).build();

    @Test
    public void peruuntuvaHakijaHyvaksytaanEnsimmaisessaSijoittelussaVarasijojenAstuttuaVoimaanJosAloituspaikoilleTuleeTilaaKunEiVarasijatayttoa() {
        jono.setAloituspaikat(2);
        jono.setEiVarasijatayttoa(true);

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan, toinenHakukohdeJohonHakemus1Hyvaksytaan);

        assertHakemustenTilat(PERUUNTUNUT, HYVAKSYTTY, HYVAKSYTTY, PERUUNTUNUT);

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan, toinenHakukohdeJohonHakemus1Hyvaksytaan, toinenHakukohdeJohonHakemus2Hyvaksytaan);

        assertHakemustenTilat(PERUUNTUNUT, PERUUNTUNUT, HYVAKSYTTY, PERUUNTUNUT);
    }

    @Test
    public void toisellaAsteellaEiPeruunnutetaEikaHyvaksytaVarasijoilleJaaneitaHakijoitaKunEiVarasijatayttoa() {
        jono.setAloituspaikat(2);
        jono.setEiVarasijatayttoa(true);

        sijoittele(toinenAsteVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan);
        assertHakemustenTilat(HYVAKSYTTY, HYVAKSYTTY, VARALLA, VARALLA);

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();

        sijoittele(toinenAsteVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan, toinenHakukohdeJohonHakemus1Hyvaksytaan);
        assertHakemustenTilat(PERUUNTUNUT, HYVAKSYTTY, VARALLA, VARALLA);

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();

        sijoittele(toinenAsteVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan, toinenHakukohdeJohonHakemus1Hyvaksytaan, toinenHakukohdeJohonHakemus2Hyvaksytaan);

        assertHakemustenTilat(PERUUNTUNUT, PERUUNTUNUT, VARALLA, VARALLA);
    }

    @Test
    public void hyvaksyttyihinNousevaHakijaJaaVaralleVarasijasaantojenOllessaVoimassaKunEiVarasijatayttoa() {
        jono.setAloituspaikat(2);
        jono.setEiVarasijatayttoa(true);

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan);

        assertHakemustenTilat(HYVAKSYTTY, HYVAKSYTTY, PERUUNTUNUT, PERUUNTUNUT);

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();

        hakemus1.setJonosija(1);
        hakemus2.setJonosija(4);
        hakemus3.setJonosija(2);
        hakemus4.setJonosija(3);

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan);

        assertHakemustenTilat(HYVAKSYTTY, PERUUNTUNUT, VARALLA, PERUUNTUNUT);
    }

    @Test
    public void varalleVoiNoustaTasmalleenJonosijojenVerranKunEiVarasijatayttoaJaTasasijasaantoArvonta() {
        jono.setAloituspaikat(2);
        jono.setEiVarasijatayttoa(true);
        jono.setTasasijasaanto(ARVONTA);

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan);

        assertHakemustenTilat(HYVAKSYTTY, HYVAKSYTTY, PERUUNTUNUT, PERUUNTUNUT);

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();

        hakemus1.setJonosija(1);
        hakemus1.setTasasijaJonosija(1);
        hakemus2.setJonosija(3);
        hakemus3.setJonosija(2);
        hakemus3.setTasasijaJonosija(1);
        hakemus4.setJonosija(2);
        hakemus4.setTasasijaJonosija(2);

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan);

        assertHakemustenTilat(HYVAKSYTTY, PERUUNTUNUT, VARALLA, PERUUNTUNUT);
    }

    @Test
    public void varasijojaVoiYlitayttaaKunEiVarasijatayttoaJaTasasijasaantoYlitaytto() {
        jono.setAloituspaikat(2);
        jono.setEiVarasijatayttoa(true);
        jono.setTasasijasaanto(YLITAYTTO);

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan);

        assertHakemustenTilat(HYVAKSYTTY, HYVAKSYTTY, PERUUNTUNUT, PERUUNTUNUT);

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();

        hakemus1.setJonosija(1);
        hakemus1.setTasasijaJonosija(1);
        hakemus2.setJonosija(3);
        hakemus3.setJonosija(2);
        hakemus3.setTasasijaJonosija(1);
        hakemus4.setJonosija(2);
        hakemus4.setTasasijaJonosija(2);

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan);

        assertHakemustenTilat(HYVAKSYTTY, PERUUNTUNUT, VARALLA, VARALLA);
    }

    @Test
    public void varasijojaVoiYlitayttaaKunEiVarasijatayttoaJaTasasijasaantoYlitaytto2() {
        jono.setAloituspaikat(3);
        jono.setEiVarasijatayttoa(true);
        jono.setTasasijasaanto(YLITAYTTO);

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan);

        assertHakemustenTilat(HYVAKSYTTY, HYVAKSYTTY, HYVAKSYTTY, PERUUNTUNUT);

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();

        hakemus1.setJonosija(2);
        hakemus1.setTasasijaJonosija(1);
        hakemus2.setJonosija(1);
        hakemus2.setTasasijaJonosija(1);
        hakemus3.setJonosija(1);
        hakemus3.setTasasijaJonosija(2);
        hakemus4.setJonosija(1);
        hakemus4.setTasasijaJonosija(3);

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan);

        assertHakemustenTilat(PERUUNTUNUT, HYVAKSYTTY, HYVAKSYTTY, VARALLA);
    }

    @Test
    public void varasijojaVoiAlitayttaaKunEiVarasijatayttoaJaTasasijasaantoAlitaytto() {
        jono.setAloituspaikat(2);
        jono.setEiVarasijatayttoa(true);
        jono.setTasasijasaanto(ALITAYTTO);

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan);

        assertHakemustenTilat(HYVAKSYTTY, HYVAKSYTTY, PERUUNTUNUT, PERUUNTUNUT);

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();

        hakemus1.setJonosija(1);
        hakemus1.setTasasijaJonosija(1);
        hakemus2.setJonosija(3);
        hakemus3.setJonosija(2);
        hakemus3.setTasasijaJonosija(1);
        hakemus4.setJonosija(2);
        hakemus4.setTasasijaJonosija(2);

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan);

        assertHakemustenTilat(HYVAKSYTTY, PERUUNTUNUT, PERUUNTUNUT, PERUUNTUNUT);
    }

    @Test
    public void varasijojaVoiAlitayttaaKunEiVarasijatayttoaJaTasasijasaantoAlitaytto2() {
        jono.setAloituspaikat(2);
        jono.setEiVarasijatayttoa(true);
        jono.setTasasijasaanto(ALITAYTTO);

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan);

        assertHakemustenTilat(HYVAKSYTTY, HYVAKSYTTY, PERUUNTUNUT, PERUUNTUNUT);

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();

        hakemus1.setJonosija(2);
        hakemus1.setTasasijaJonosija(1);
        hakemus2.setJonosija(1);
        hakemus2.setTasasijaJonosija(1);
        hakemus3.setJonosija(1);
        hakemus3.setTasasijaJonosija(2);
        hakemus4.setJonosija(1);
        hakemus4.setTasasijaJonosija(3);

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan);

        assertHakemustenTilat(PERUUNTUNUT, PERUUNTUNUT, PERUUNTUNUT, PERUUNTUNUT);
    }

    @Test
    public void alleSivssnovSijoittelussaOlleenJonosijanOlevaHakijaJaaPeruuntuneeksiJosEiVarasijatayttoa() {
        jono.setAloituspaikat(2);
        jono.setEiVarasijatayttoa(true);

        hakemus3.setTila(HYLATTY);
        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan);

        assertHakemustenTilat(HYVAKSYTTY, HYVAKSYTTY, HYLATTY, PERUUNTUNUT);

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();

        hakemus3.setTila(VARALLA);
        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan, toinenHakukohdeJohonHakemus1Hyvaksytaan, toinenHakukohdeJohonHakemus2Hyvaksytaan);

        assertHakemustenTilat(PERUUNTUNUT, PERUUNTUNUT, PERUUNTUNUT, PERUUNTUNUT);
    }

    @Test
    public void alleSivssnovSijoittelussaOlleenJonosijanOlevaHakijaJaaPeruuntuneeksiJosEiVarasijatayttoa2() {
        jono.setAloituspaikat(2);
        jono.setEiVarasijatayttoa(true);

        hakemus3.setTila(HYLATTY);
        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan);

        assertHakemustenTilat(HYVAKSYTTY, HYVAKSYTTY, HYLATTY, PERUUNTUNUT);

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();

        hakemus3.setTila(VARALLA);
        hakemus1.setJonosija(4);
        hakemus2.setJonosija(1);
        hakemus3.setJonosija(3);
        hakemus4.setJonosija(2);
        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan, toinenHakukohdeJohonHakemus1Hyvaksytaan, toinenHakukohdeJohonHakemus2Hyvaksytaan);

        assertHakemustenTilat(PERUUNTUNUT, PERUUNTUNUT, PERUUNTUNUT, VARALLA);
    }

    @Test
    public void peruuntuvaHakijaNostetaanVarasijalleJaVarallaOlevaHyvaksytyksiEnsimmaisessaSijoittelussaVarasijojenAstuttuaVoimaanJosTuleeTilaaKunVarasijojaOnRajoitettu_v1() {
        jono.setAloituspaikat(1);
        jono.setEiVarasijatayttoa(false);
        jono.setVarasijat(1);

        sijoittele(kkHakuVarasijasaannotEiVoimassa, hakukohdeJossaVarasijojaRajoitetaan, toinenHakukohdeJohonHakemus1Hyvaksytaan);

        assertHakemustenTilat(PERUUNTUNUT, HYVAKSYTTY, VARALLA, VARALLA);

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan, toinenHakukohdeJohonHakemus1Hyvaksytaan);

        assertHakemustenTilat(PERUUNTUNUT, HYVAKSYTTY, VARALLA, PERUUNTUNUT);

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan, toinenHakukohdeJohonHakemus1Hyvaksytaan, toinenHakukohdeJohonHakemus2Hyvaksytaan);

        assertHakemustenTilat(PERUUNTUNUT, PERUUNTUNUT, VARASIJALTA_HYVAKSYTTY, PERUUNTUNUT);
    }

    @Test
    public void peruuntuvaHakijaNostetaanVarasijalleJaVarallaOlevaHyvaksytyksiEnsimmaisessaSijoittelussaVarasijojenAstuttuaVoimaanJosTuleeTilaaKunVarasijojaOnRajoitettu_v2() {
        jono.setAloituspaikat(1);
        jono.setEiVarasijatayttoa(false);
        jono.setVarasijat(1);

        sijoittele(kkHakuVarasijasaannotEiVoimassa, hakukohdeJossaVarasijojaRajoitetaan);

        assertHakemustenTilat(HYVAKSYTTY, VARALLA, VARALLA, VARALLA);

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan, toinenHakukohdeJohonHakemus1Hyvaksytaan);

        assertHakemustenTilat(PERUUNTUNUT, HYVAKSYTTY, VARALLA, PERUUNTUNUT);

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan, toinenHakukohdeJohonHakemus1Hyvaksytaan, toinenHakukohdeJohonHakemus2Hyvaksytaan);

        assertHakemustenTilat(PERUUNTUNUT, PERUUNTUNUT, VARASIJALTA_HYVAKSYTTY, PERUUNTUNUT);
    }

    @Test
    public void vainSivssnovSijoittelussaVarallePaasseetVoivatNoustaHyvaksytyiksiMyohemmissaSijoitteluissa() {
        jono.setAloituspaikat(1);
        jono.setEiVarasijatayttoa(false);
        jono.setVarasijat(1);

        hakemus3.setTila(HYLATTY);

        sijoittele(kkHakuVarasijasaannotEiVoimassa, hakukohdeJossaVarasijojaRajoitetaan);
        assertHakemustenTilat(HYVAKSYTTY, VARALLA, HYLATTY, VARALLA);

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();
        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan);

        assertHakemustenTilat(HYVAKSYTTY, VARALLA, HYLATTY, PERUUNTUNUT);

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();
        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan, toinenHakukohdeJohonHakemus1Hyvaksytaan, toinenHakukohdeJohonHakemus2Hyvaksytaan);

        assertEquals(hakemus1.getHakemusOid(), toinenHakukohdeJohonHakemus1Hyvaksytaan.getValintatapajonot().get(0).getHakemukset().get(0).getHakemusOid());
        assertEquals(HYVAKSYTTY, toinenHakukohdeJohonHakemus1Hyvaksytaan.getValintatapajonot().get(0).getHakemukset().get(0).getTila());

        assertHakemustenTilat(PERUUNTUNUT, PERUUNTUNUT, HYLATTY, PERUUNTUNUT);
    }

    @Test
    @Ignore //Onko validi testi?
    public void toisellaAsteellaVarallaolijatNousevatVarasijataytonAikanaHyvaksytyiksiJosTilaaOnTullutVaikkaVarasijatayttoOlisiEstetty() {
        jono.setAloituspaikat(1);
        jono.setEiVarasijatayttoa(true);
        jono.setVarasijat(null);

        sijoittele(toinenAsteVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan);
        // Huom: 2. asteella (musiikkialan koulutusten takia) ei peruunnuteta niitä, jotka eivät mahdu aloituspaikkoihin
        assertHakemustenTilat(HYVAKSYTTY, VARALLA, VARALLA, VARALLA);

        sijoittele(toinenAsteVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan, toinenHakukohdeJohonHakemus1Hyvaksytaan);

        assertEquals(hakemus1.getHakemusOid(), toinenHakukohdeJohonHakemus1Hyvaksytaan.getValintatapajonot().get(0).getHakemukset().get(0).getHakemusOid());
        assertEquals(HYVAKSYTTY, toinenHakukohdeJohonHakemus1Hyvaksytaan.getValintatapajonot().get(0).getHakemukset().get(0).getTila());

        assertHakemustenTilat(PERUUNTUNUT, HYVAKSYTTY, VARALLA, VARALLA);
    }

    @Test
    public void peruuntuneestaVaralleNousevatHakemuksetPaasevatVaralleMyosRajatussaVarasijataytossaJosNeTulevatRiittavanKorkealleJonosijalle() {
        jono.setAloituspaikat(1);
        jono.setEiVarasijatayttoa(false);
        jono.setVarasijat(1);

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan);
        assertHakemustenTilat(HYVAKSYTTY, VARALLA, PERUUNTUNUT, PERUUNTUNUT);

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();

        final Hakemus kiilaavaHakemus = new HakemusBuilder().withOid("kiilaavaHakemus").withEdellinenTila(PERUUNTUNUT).withTila(VARALLA).build();
        hakemusKiilaa(kiilaavaHakemus);

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan);

        assertHakemustenTilat(HYVAKSYTTY, VARALLA, PERUUNTUNUT, PERUUNTUNUT);
        assertEquals(VARALLA, kiilaavaHakemus.getTila());
    }

    @Test
    public void hylatystaVaralleNousevatHakemuksetPaasevatVaralleMyosRajatussaVarasijataytossaJosNeTulevatRiittavanKorkealleJonosijalle() {
        jono.setAloituspaikat(1);
        jono.setEiVarasijatayttoa(false);
        jono.setVarasijat(1);

        sijoittele(kkHakuVarasijasaannotEiVoimassa, hakukohdeJossaVarasijojaRajoitetaan);
        assertHakemustenTilat(HYVAKSYTTY, VARALLA, VARALLA, VARALLA);

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan);
        assertHakemustenTilat(HYVAKSYTTY, VARALLA, PERUUNTUNUT, PERUUNTUNUT);

        final Hakemus kiilaavaHakemus = new HakemusBuilder().withOid("kiilaavaHakemus").withEdellinenTila(PERUUNTUNUT).withTila(VARALLA).build();
        hakemusKiilaa(kiilaavaHakemus);

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan);
        assertHakemustenTilat(HYVAKSYTTY, VARALLA, PERUUNTUNUT, PERUUNTUNUT);
        assertEquals(VARALLA, kiilaavaHakemus.getTila());
    }

    @Test
    public void hylatytHakemuksetPysyvatHylattyinaRajatunVarasijataytonJonoissa() {
        jono.setAloituspaikat(1);
        jono.setEiVarasijatayttoa(false);
        jono.setVarasijat(1);

        sijoittele(kkHakuVarasijasaannotEiVoimassa, hakukohdeJossaVarasijojaRajoitetaan);
        assertHakemustenTilat(HYVAKSYTTY, VARALLA, VARALLA, VARALLA);

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();

        hakemus3.setTila(HYLATTY);

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan);
        assertHakemustenTilat(HYVAKSYTTY, VARALLA, HYLATTY, PERUUNTUNUT);
    }

    @Test
    public void hylatytHakemuksetPysyvatHylattyinaJonoissaIlmanVarasijatayttoa() {
        jono.setAloituspaikat(1);
        jono.setEiVarasijatayttoa(true);

        sijoittele(kkHakuVarasijasaannotEiVoimassa, hakukohdeJossaVarasijojaRajoitetaan);
        assertHakemustenTilat(HYVAKSYTTY, VARALLA, VARALLA, VARALLA);

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();

        hakemus3.setTila(HYLATTY);

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan);
        assertHakemustenTilat(HYVAKSYTTY, PERUUNTUNUT, HYLATTY, PERUUNTUNUT);
    }

    @Test
    public void kiilaavaHakemusEiNouseVaralleRajatussaVarasijataytossaJosSitovaVastaanottoAlemmassaHakutoiveessa() {
        jono.setAloituspaikat(1);
        jono.setEiVarasijatayttoa(false);
        jono.setVarasijat(1);

        final Hakemus kiilaavaHakemusAlemmassaHakutoiveessa = new HakemusBuilder().withOid("kiilaavaHakemus").withHakijaOid("kiilaavaHakija")
                .withEdellinenTila(HYVAKSYTTY).withTila(HYVAKSYTTY).withPrioriteetti(0).withJonosija(0).build();
        final Hakukohde alempiHakukohdeJohonVastaanottoKohdistuu = new HakuBuilder.HakukohdeBuilder("alempiHakukohdeOid")
                .withValintatapajono(new HakuBuilder.ValintatapajonoBuilder()
                        .withOid("alempiOid")
                        .withAloituspaikat(1)
                        .withTasasijasaanto(YLITAYTTO)
                        .withPrioriteetti(0)
                        .withHakemus(kiilaavaHakemusAlemmassaHakutoiveessa)
                        .build()).build();
        final Valintatulos vastaanotto = new Valintatulos(kiilaavaHakemusAlemmassaHakutoiveessa.getHakemusOid(),
                "kiilaavaHakija", alempiHakukohdeJohonVastaanottoKohdistuu.getOid(), false,
                IlmoittautumisTila.EI_TEHTY, true, ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT, false,
                alempiHakukohdeJohonVastaanottoKohdistuu.getValintatapajonot().get(0).getOid());

        sijoittele(kkHakuVarasijasaannotEiVoimassa, Collections.singletonList(vastaanotto), hakukohdeJossaVarasijojaRajoitetaan, alempiHakukohdeJohonVastaanottoKohdistuu);
        assertHakemustenTilat(HYVAKSYTTY, VARALLA, VARALLA, VARALLA);
        assertEquals(HYVAKSYTTY, kiilaavaHakemusAlemmassaHakutoiveessa.getTila());

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();
        korjaaTilaJaEdellinenTilaSijoittelunJalkeen(kiilaavaHakemusAlemmassaHakutoiveessa);

        sijoittele(kkHakuVarasijasaannotVoimassa, Collections.singletonList(vastaanotto), hakukohdeJossaVarasijojaRajoitetaan, alempiHakukohdeJohonVastaanottoKohdistuu);
        assertHakemustenTilat(HYVAKSYTTY, VARALLA, PERUUNTUNUT, PERUUNTUNUT);
        assertEquals(HYVAKSYTTY, kiilaavaHakemusAlemmassaHakutoiveessa.getTila());

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();
        korjaaTilaJaEdellinenTilaSijoittelunJalkeen(kiilaavaHakemusAlemmassaHakutoiveessa);

        final Hakemus kiilaavaHakemus = new HakemusBuilder().withOid("kiilaavaHakemus").withHakijaOid("kiilaavaHakija").withEdellinenTila(HYLATTY).withTila(VARALLA).withPrioriteetti(1).build();
        hakemusKiilaa(kiilaavaHakemus);

        sijoittele(kkHakuVarasijasaannotVoimassa, Collections.singletonList(vastaanotto), hakukohdeJossaVarasijojaRajoitetaan, alempiHakukohdeJohonVastaanottoKohdistuu);
        assertHakemustenTilat(HYVAKSYTTY, VARALLA, PERUUNTUNUT, PERUUNTUNUT);
        assertEquals(HYVAKSYTTY, kiilaavaHakemusAlemmassaHakutoiveessa.getTila());
        assertEquals(PERUUNTUNUT, kiilaavaHakemus.getTila());
        assertEquals("Peruuntunut, ottanut vastaan toisen opiskelupaikan", kiilaavaHakemus.getTilanKuvaukset().get("FI"));
    }

    @Test
    public void josViimeisellaVarasijallaOlevanPisteetHuononevatVarasijataytonAikanaEiOtetaYlimaaraisiaVaralle() {
        jono.setAloituspaikat(1);
        jono.setEiVarasijatayttoa(false);
        jono.setVarasijat(1);

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan);
        assertHakemustenTilat(HYVAKSYTTY, VARALLA, PERUUNTUNUT, PERUUNTUNUT);

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();

        hakemus1.setJonosija(1);
        hakemus2.setJonosija(4);
        hakemus3.setJonosija(2);
        hakemus4.setJonosija(3);

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan);

        assertHakemustenTilat(HYVAKSYTTY, PERUUNTUNUT, VARALLA, PERUUNTUNUT);
    }

    @Test
    public void josViimeisellaVarasijallaOlevanPisteetParanevatVarasijataytonAikanaEiPeruunnutetaLiikaaHakijoita() {
        jono.setAloituspaikat(1);
        jono.setEiVarasijatayttoa(false);
        jono.setVarasijat(2);

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan);
        assertHakemustenTilat(HYVAKSYTTY, VARALLA, VARALLA, PERUUNTUNUT);

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();

        hakemus1.setJonosija(2);
        hakemus2.setJonosija(3);
        hakemus3.setJonosija(1);
        hakemus4.setJonosija(4);

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan);

        assertHakemustenTilat(VARALLA, VARALLA, VARASIJALTA_HYVAKSYTTY, PERUUNTUNUT);
    }

    @Test
    public void rajattuVarasijatayttoYlitayttaaVarasijojaJononTasasijasaannostaRiippumatta() {
        jono.setAloituspaikat(2);
        jono.setEiVarasijatayttoa(false);
        jono.setVarasijat(1);
        jono.setTasasijasaanto(ALITAYTTO);

        hakemus1.setJonosija(1);
        hakemus1.setTasasijaJonosija(1);

        hakemus2.setJonosija(1);
        hakemus2.setTasasijaJonosija(2);

        hakemus3.setJonosija(2);
        hakemus3.setTasasijaJonosija(1);

        hakemus4.setJonosija(2);
        hakemus4.setTasasijaJonosija(2);

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan);

        assertHakemustenTilat(HYVAKSYTTY, HYVAKSYTTY, VARALLA, VARALLA);
    }

    @Test
    public void rajattuVarasijaTayttoTallentaaTiedonAlimmastaVarallaOlleestaJonosijastaSivssnovSijoittelussa() {
        assertEquals(Optional.empty(), jono.getSivssnovSijoittelunVarasijataytonRajoitus());

        jono.setAloituspaikat(1);
        jono.setEiVarasijatayttoa(false);
        jono.setVarasijat(1);

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan);
        assertHakemustenTilat(HYVAKSYTTY, VARALLA, PERUUNTUNUT, PERUUNTUNUT);

        assertEquals(Optional.of(new JonosijaTieto(Collections.singletonList(hakemus2))), jono.getSivssnovSijoittelunVarasijataytonRajoitus());
    }

    private void hakemusKiilaa(Hakemus kiilaavaHakemus) {
        jono.getHakemukset().add(kiilaavaHakemus);
        hakemus1.setJonosija(0);
        kiilaavaHakemus.setJonosija(1);
        hakemus2.setJonosija(2);
        hakemus3.setJonosija(4);
        hakemus4.setJonosija(5);
    }

    private void korjaaTilaJaEdellinenTilaSijoittelunJalkeen() {
        Arrays.asList(hakemus1, hakemus2, hakemus3, hakemus4).forEach(this::korjaaTilaJaEdellinenTilaSijoittelunJalkeen);
    }

    private void korjaaTilaJaEdellinenTilaSijoittelunJalkeen(Hakemus hakemus) {
        HakemuksenTila tila = hakemus.getTila();
        hakemus.setEdellinenTila(tila);
        if(!HYLATTY.equals(tila)) {
            hakemus.setTila(VARALLA);
        }
        hakemus.setTilanKuvaukset(Collections.emptyMap());
    }

    private Consumer<SijoitteluajoWrapper> toinenAsteVarasijasaannotVoimassa = sijoitteluajoWrapper -> sijoitteluajoWrapper.setKKHaku(false);
    private Consumer<SijoitteluajoWrapper> kkHakuVarasijasaannotVoimassa = sijoitteluajoWrapper -> sijoitteluajoWrapper.setKKHaku(true);
    private Consumer<SijoitteluajoWrapper> kkHakuVarasijasaannotEiVoimassa = sijoitteluajoWrapper -> {
        sijoitteluajoWrapper.setVarasijaSaannotAstuvatVoimaan(LocalDateTime.now().plusDays(1));
        sijoitteluajoWrapper.setKKHaku(true);
    };

    private void assertHakemustenTilat(HakemuksenTila h1, HakemuksenTila h2, HakemuksenTila h3, HakemuksenTila h4) {
        List<HakemuksenTila> tilat = Stream.of(hakemus1, hakemus2, hakemus3, hakemus4)
            .map(Hakemus::getTila).collect(Collectors.toList());
        assertEquals(Arrays.asList(h1, h2, h3, h4), tilat);
    }

    private void sijoittele(Consumer<SijoitteluajoWrapper> prepareAjoWrapper, Hakukohde... hakukohteet) {
        sijoittele(prepareAjoWrapper, Collections.emptyList(), hakukohteet);
    }

    private void sijoittele(Consumer<SijoitteluajoWrapper> prepareAjoWrapper, List<Valintatulos> valintatulokset, Hakukohde... hakukohteet) {
        SijoitteluajoWrapper sijoitteluAjoWrapper =
            SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluConfiguration(true), new SijoitteluAjo(), Arrays.asList(hakukohteet), valintatulokset, Collections.emptyMap());
        prepareAjoWrapper.accept(sijoitteluAjoWrapper);
        SijoitteluAlgorithmUtil.sijoittele(sijoitteluAjoWrapper);
    }
}
