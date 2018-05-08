package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.helper.HakuBuilder;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.SijoitteluAlgorithmUtil;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.*;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.*;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.VARALLA;
import static fi.vm.sade.sijoittelu.domain.Tasasijasaanto.YLITAYTTO;
import static fi.vm.sade.sijoittelu.domain.TilankuvauksenTarkenne.*;
import static org.junit.Assert.assertEquals;

public class SijoitteluajonTilankuvausKorjaustTest {
    private final Hakemus hakemus1 = new HakuBuilder.HakemusBuilder().withOid("hakemus1")
            .withJonosija(1).withTila(HYVAKSYTTY).withTilankuvauksenTarkenne(null).withPrioriteetti(1).build();
    private final Hakemus hakemus2 = new HakuBuilder.HakemusBuilder().withOid("hakemus2")
            .withJonosija(2).withTila(HYVAKSYTTY).withTilankuvauksenTarkenne(TilankuvauksenTarkenne.PERUUNTUNUT_HYVAKSYTTY_TOISESSA_JONOSSA).withPrioriteetti(1).build();
    private final Hakemus hakemus3 = new HakuBuilder.HakemusBuilder().withOid("hakemus3")
            .withJonosija(3).withTila(VARALLA).withTilankuvauksenTarkenne(null).withPrioriteetti(1).build();
    private final Hakemus hakemus4 = new HakuBuilder.HakemusBuilder().withOid("hakemus4")
            .withJonosija(4).withTila(VARALLA).withTilankuvauksenTarkenne(TilankuvauksenTarkenne.HYLATTY_HAKIJARYHMAAN_KUULUMATTOMANA).withPrioriteetti(1).build();
    private final Hakemus hakemus5 = new HakuBuilder.HakemusBuilder().withOid("hakemus4")
            .withJonosija(5).withTila(VARASIJALTA_HYVAKSYTTY).withTilankuvauksenTarkenne(null).withPrioriteetti(1).build();
    private final Hakemus hakemus6 = new HakuBuilder.HakemusBuilder().withOid("hakemus4")
            .withJonosija(6).withTila(VARASIJALTA_HYVAKSYTTY).withTilankuvauksenTarkenne(TilankuvauksenTarkenne.PERUUNTUNUT_EI_MAHDU_VARASIJOJEN_MAARAAN).withPrioriteetti(1).build();

    private Valintatapajono jono = new HakuBuilder.ValintatapajonoBuilder().withOid("jono1")
            .withTasasijasaanto(YLITAYTTO)
            .withPrioriteetti(0)
            .withHakemukset(hakemus1, hakemus2, hakemus3, hakemus4, hakemus5, hakemus6)
            .build();

    private Hakukohde hakukohdeJossaVarasijojaRajoitetaan = new HakuBuilder.HakukohdeBuilder("hakukohdeOid")
            .withValintatapajono(jono).build();
    private Hakukohde toinenHakukohdeJohonHakemus1Hyvaksytaan = new HakuBuilder.HakukohdeBuilder("toinenHakukohdeOid")
            .withValintatapajono(new HakuBuilder.ValintatapajonoBuilder()
                    .withAloituspaikat(1)
                    .withTasasijasaanto(YLITAYTTO)
                    .withPrioriteetti(0)
                    .withHakemus(new HakuBuilder.HakemusBuilder()
                            .withOid(hakemus1.getHakemusOid())
                            .withJonosija(0)
                            .withPrioriteetti(hakemus1.getPrioriteetti() - 1)
                            .withTila(VARALLA).build())
                    .build()).build();
    private Hakukohde toinenHakukohdeJohonHakemus2Hyvaksytaan = new HakuBuilder.HakukohdeBuilder("kolmasHakukohdeOid")
            .withValintatapajono(new HakuBuilder.ValintatapajonoBuilder()
                    .withAloituspaikat(1)
                    .withTasasijasaanto(YLITAYTTO)
                    .withPrioriteetti(0)
                    .withSivssnov(true)
                    .withHakemus(new HakuBuilder.HakemusBuilder()
                            .withOid(hakemus2.getHakemusOid())
                            .withJonosija(0)
                            .withPrioriteetti(hakemus2.getPrioriteetti() - 1)
                            .withTila(VARALLA).build())
                    .build()).build();

    final boolean bugiKorjattu = true;

    @Test
    public void basicSijoitteluEiTilanmuutostaSijoittelunJalkeen() {
        jono.setAloituspaikat(2);
        jono.setEiVarasijatayttoa(false);
        jono.setVarasijat(2);

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan);

        assertHakemustenTilat(HYVAKSYTTY, HYVAKSYTTY, VARALLA, VARALLA, VARASIJALTA_HYVAKSYTTY, VARASIJALTA_HYVAKSYTTY);
        assertHakemustenTilakuvauksenTarkenteet(EI_TILANKUVAUKSEN_TARKENNETTA, EI_TILANKUVAUKSEN_TARKENNETTA, EI_TILANKUVAUKSEN_TARKENNETTA, EI_TILANKUVAUKSEN_TARKENNETTA, HYVAKSYTTY_VARASIJALTA, HYVAKSYTTY_VARASIJALTA);

        System.out.println("Hakemuksen 1 tila: " + hakemus1.getTila() + " tarkenne: " + hakemus1.getTilankuvauksenTarkenne());
        System.out.println("Hakemuksen 2 tila: " + hakemus2.getTila() + " tarkenne: " + hakemus2.getTilankuvauksenTarkenne());
        System.out.println("Hakemuksen 3 tila: " + hakemus3.getTila() + " tarkenne: " + hakemus3.getTilankuvauksenTarkenne());
        System.out.println("Hakemuksen 4 tila: " + hakemus4.getTila() + " tarkenne: " + hakemus4.getTilankuvauksenTarkenne());
        System.out.println("Hakemuksen 5 tila: " + hakemus5.getTila() + " tarkenne: " + hakemus5.getTilankuvauksenTarkenne());
        System.out.println("Hakemuksen 6 tila: " + hakemus6.getTila() + " tarkenne: " + hakemus6.getTilankuvauksenTarkenne());

    }
/*
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
    public void peruuntuvaHakijaNostetaanVarasijalleJaVarallaOlevaHyvaksytyksiEnsimmaisessaSijoittelussaVarasijojenAstuttuaVoimaanJosTuleeTilaaKunVarasijojaOnRajoitettu_v1() {
        jono.setAloituspaikat(1);
        jono.setEiVarasijatayttoa(false);
        jono.setVarasijat(1);

        sijoittele(kkHakuVarasijasaannotEiVoimassa, hakukohdeJossaVarasijojaRajoitetaan, toinenHakukohdeJohonHakemus1Hyvaksytaan);

        assertHakemustenTilat(PERUUNTUNUT, HYVAKSYTTY, VARALLA, VARALLA);

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan, toinenHakukohdeJohonHakemus1Hyvaksytaan);

        if (bugiKorjattu) {
            assertHakemustenTilat(PERUUNTUNUT, HYVAKSYTTY, VARALLA, PERUUNTUNUT);
        } else {
            assertHakemustenTilat(PERUUNTUNUT, HYVAKSYTTY, PERUUNTUNUT, PERUUNTUNUT);
        }

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan, toinenHakukohdeJohonHakemus1Hyvaksytaan, toinenHakukohdeJohonHakemus2Hyvaksytaan);

        if (bugiKorjattu) {
            assertHakemustenTilat(PERUUNTUNUT, PERUUNTUNUT, VARASIJALTA_HYVAKSYTTY, PERUUNTUNUT);
        } else {
            assertHakemustenTilat(PERUUNTUNUT, PERUUNTUNUT, PERUUNTUNUT, PERUUNTUNUT);
        }
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

        if (bugiKorjattu) {
            assertHakemustenTilat(PERUUNTUNUT, HYVAKSYTTY, VARALLA, PERUUNTUNUT);
        } else {
            assertHakemustenTilat(PERUUNTUNUT, HYVAKSYTTY, PERUUNTUNUT, PERUUNTUNUT);
        }

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan, toinenHakukohdeJohonHakemus1Hyvaksytaan, toinenHakukohdeJohonHakemus2Hyvaksytaan);

        if (bugiKorjattu) {
            assertHakemustenTilat(PERUUNTUNUT, PERUUNTUNUT, VARASIJALTA_HYVAKSYTTY, PERUUNTUNUT);
        } else {
            assertHakemustenTilat(PERUUNTUNUT, PERUUNTUNUT, PERUUNTUNUT, PERUUNTUNUT);
        }
    }

    @Test
    public void peruuntuneestaVaralleNousevatHakemuksetPaasevatVaralleMyosRajatussaVarasijataytossaJosNeTulevatRiittavanKorkealleJonosijalle() {
        jono.setAloituspaikat(1);
        jono.setEiVarasijatayttoa(false);
        jono.setVarasijat(1);

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan);
        assertHakemustenTilat(HYVAKSYTTY, VARALLA, PERUUNTUNUT, PERUUNTUNUT);

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();

        final Hakemus kiilaavaHakemus = new HakuBuilder.HakemusBuilder().withOid("kiilaavaHakemus").withEdellinenTila(PERUUNTUNUT).withTila(VARALLA).build();
        hakemusKiilaa(kiilaavaHakemus);

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan);

        if (bugiKorjattu) {
            assertHakemustenTilat(HYVAKSYTTY, VARALLA, PERUUNTUNUT, PERUUNTUNUT);
            assertEquals(VARALLA, kiilaavaHakemus.getTila());
        } else {
            assertHakemustenTilat(HYVAKSYTTY, PERUUNTUNUT, PERUUNTUNUT, PERUUNTUNUT);
            assertEquals(VARALLA, kiilaavaHakemus.getTila());
        }
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

        final Hakemus kiilaavaHakemus = new HakuBuilder.HakemusBuilder().withOid("kiilaavaHakemus").withEdellinenTila(PERUUNTUNUT).withTila(PERUUNTUNUT).build();
        hakemusKiilaa(kiilaavaHakemus);

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan);
        assertHakemustenTilat(HYVAKSYTTY, VARALLA, PERUUNTUNUT, PERUUNTUNUT);
        assertEquals(VARALLA, kiilaavaHakemus.getTila());
    }

    @Test
    public void kiilaavaHakemusEiNouseVaralleRajatussaVarasijataytossaJosSitovaVastaanottoAlemmassaHakutoiveessa() {
        jono.setAloituspaikat(1);
        jono.setEiVarasijatayttoa(false);
        jono.setVarasijat(1);

        final Hakemus kiilaavaHakemusAlemmassaHakutoiveessa = new HakuBuilder.HakemusBuilder().withOid("kiilaavaHakemus").withHakijaOid("kiilaavaHakija")
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

        sijoittele(kkHakuVarasijasaannotEiVoimassa, Arrays.asList(vastaanotto), hakukohdeJossaVarasijojaRajoitetaan, alempiHakukohdeJohonVastaanottoKohdistuu);
        assertHakemustenTilat(HYVAKSYTTY, VARALLA, VARALLA, VARALLA);
        assertEquals(HYVAKSYTTY, kiilaavaHakemusAlemmassaHakutoiveessa.getTila());

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();
        korjaaTilaJaEdellinenTilaSijoittelunJalkeen(kiilaavaHakemusAlemmassaHakutoiveessa);

        sijoittele(kkHakuVarasijasaannotVoimassa, Arrays.asList(vastaanotto), hakukohdeJossaVarasijojaRajoitetaan, alempiHakukohdeJohonVastaanottoKohdistuu);
        assertHakemustenTilat(HYVAKSYTTY, VARALLA, PERUUNTUNUT, PERUUNTUNUT);
        assertEquals(HYVAKSYTTY, kiilaavaHakemusAlemmassaHakutoiveessa.getTila());

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();
        korjaaTilaJaEdellinenTilaSijoittelunJalkeen(kiilaavaHakemusAlemmassaHakutoiveessa);

        final Hakemus kiilaavaHakemus = new HakuBuilder.HakemusBuilder().withOid("kiilaavaHakemus").withHakijaOid("kiilaavaHakija").withEdellinenTila(HYLATTY).withTila(VARALLA).withPrioriteetti(1).build();
        hakemusKiilaa(kiilaavaHakemus);

        sijoittele(kkHakuVarasijasaannotVoimassa, Arrays.asList(vastaanotto), hakukohdeJossaVarasijojaRajoitetaan, alempiHakukohdeJohonVastaanottoKohdistuu);
        assertHakemustenTilat(HYVAKSYTTY, VARALLA, PERUUNTUNUT, PERUUNTUNUT);
        assertEquals(HYVAKSYTTY, kiilaavaHakemusAlemmassaHakutoiveessa.getTila());
        assertEquals(PERUUNTUNUT, kiilaavaHakemus.getTila());
        assertEquals("Peruuntunut, ottanut vastaan toisen opiskelupaikan", kiilaavaHakemus.getTilanKuvaukset().get("FI"));
    }*/

    private void hakemusKiilaa(Hakemus kiilaavaHakemus) {
        jono.getHakemukset().add(kiilaavaHakemus);
        hakemus1.setJonosija(0);
        kiilaavaHakemus.setJonosija(1);
        hakemus2.setJonosija(2);
        hakemus3.setJonosija(4);
        hakemus4.setJonosija(5);
    }

    private void korjaaTilaJaEdellinenTilaSijoittelunJalkeen() {
        Arrays.asList(hakemus1, hakemus2, hakemus3, hakemus4).forEach(h -> korjaaTilaJaEdellinenTilaSijoittelunJalkeen(h));
    }

    private void korjaaTilaJaEdellinenTilaSijoittelunJalkeen(Hakemus hakemus) {
        HakemuksenTila tila = hakemus.getTila();
        hakemus.setEdellinenTila(tila);
        if(!HYLATTY.equals(tila)) {
            hakemus.setTila(VARALLA);
        }
        hakemus.setTilanKuvaukset(Collections.emptyMap());
    }

    Consumer<SijoitteluajoWrapper> toinenAsteVarasijasaannotVoimassa = sijoitteluajoWrapper -> sijoitteluajoWrapper.setKKHaku(false);
    Consumer<SijoitteluajoWrapper> kkHakuVarasijasaannotVoimassa = sijoitteluajoWrapper -> sijoitteluajoWrapper.setKKHaku(true);
    Consumer<SijoitteluajoWrapper> kkHakuVarasijasaannotEiVoimassa = sijoitteluajoWrapper -> {
        sijoitteluajoWrapper.setVarasijaSaannotAstuvatVoimaan(LocalDateTime.now().plusDays(1));
        sijoitteluajoWrapper.setKKHaku(true);
    };

    private void assertHakemustenTilat(HakemuksenTila h1, HakemuksenTila h2, HakemuksenTila h3, HakemuksenTila h4, HakemuksenTila h5, HakemuksenTila h6) {
        assertEquals("Hakemus1", h1, hakemus1.getTila());
        assertEquals("Hakemus2", h2, hakemus2.getTila());
        assertEquals("Hakemus3", h3, hakemus3.getTila());
        assertEquals("hakemus4", h4, hakemus4.getTila());
        assertEquals("hakemus5", h5, hakemus5.getTila());
        assertEquals("hakemus6", h6, hakemus6.getTila());
    }

    private void assertHakemustenTilakuvauksenTarkenteet(TilankuvauksenTarkenne t1, TilankuvauksenTarkenne t2, TilankuvauksenTarkenne t3, TilankuvauksenTarkenne t4, TilankuvauksenTarkenne t5, TilankuvauksenTarkenne t6) {
        assertEquals("Hakemus1", t1, hakemus1.getTilankuvauksenTarkenne());
        assertEquals("Hakemus2", t2, hakemus2.getTilankuvauksenTarkenne());
        assertEquals("Hakemus3", t3, hakemus3.getTilankuvauksenTarkenne());
        assertEquals("hakemus4", t4, hakemus4.getTilankuvauksenTarkenne());
        assertEquals("hakemus5", t5, hakemus5.getTilankuvauksenTarkenne());
        assertEquals("hakemus6", t6, hakemus6.getTilankuvauksenTarkenne());
    }

    private void sijoittele(Consumer<SijoitteluajoWrapper> prepareAjoWrapper, Hakukohde... hakukohteet) {
        sijoittele(prepareAjoWrapper, Collections.emptyList(), hakukohteet);
    }

    private void sijoittele(Consumer<SijoitteluajoWrapper> prepareAjoWrapper, List<Valintatulos> valintatulokset, Hakukohde... hakukohteet) {
        SijoitteluajoWrapper sijoitteluAjoWrapper =
                SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluAjo(), Arrays.asList(hakukohteet), valintatulokset, Collections.emptyMap());
        prepareAjoWrapper.accept(sijoitteluAjoWrapper);
        SijoitteluAlgorithmUtil.sijoittele(sijoitteluAjoWrapper);
    }

}
