package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.*;
import static fi.vm.sade.sijoittelu.domain.Tasasijasaanto.YLITAYTTO;
import static org.junit.Assert.assertEquals;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.helper.HakuBuilder.HakemusBuilder;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.helper.HakuBuilder.HakukohdeBuilder;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.helper.HakuBuilder.ValintatapajonoBuilder;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.SijoitteluAlgorithmUtil;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.*;
import org.junit.Ignore;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

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

    final boolean bugiKorjattu = false;

    @Test
    public void peruuntuvaHakijaHyvaksytaanEnsimmaisessaSijoittelussaVarasijojenAstuttuaVoimaanJosAloituspaikoilleTuleeTilaaKunEiVarasijatayttoa() {
        jono.setAloituspaikat(2);
        jono.setEiVarasijatayttoa(true);

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan, toinenHakukohdeJohonHakemus1Hyvaksytaan);

        if(bugiKorjattu) {
            assertHakemustenTilat(PERUUNTUNUT, HYVAKSYTTY, HYVAKSYTTY, PERUUNTUNUT);
        } else {
            assertHakemustenTilat(PERUUNTUNUT, HYVAKSYTTY, PERUUNTUNUT, PERUUNTUNUT);
        }

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan, toinenHakukohdeJohonHakemus1Hyvaksytaan, toinenHakukohdeJohonHakemus2Hyvaksytaan);

        if(bugiKorjattu) {
            assertHakemustenTilat(PERUUNTUNUT, PERUUNTUNUT, HYVAKSYTTY, PERUUNTUNUT);
        } else {
            assertHakemustenTilat(PERUUNTUNUT, PERUUNTUNUT, PERUUNTUNUT, PERUUNTUNUT);
        }
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

        if(bugiKorjattu) {
            assertHakemustenTilat(PERUUNTUNUT, HYVAKSYTTY, VARALLA, PERUUNTUNUT);
        } else {
            assertHakemustenTilat(PERUUNTUNUT, HYVAKSYTTY, PERUUNTUNUT, PERUUNTUNUT);
        }

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan, toinenHakukohdeJohonHakemus1Hyvaksytaan, toinenHakukohdeJohonHakemus2Hyvaksytaan);

        if(bugiKorjattu) {
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

        if(bugiKorjattu) {
            assertHakemustenTilat(PERUUNTUNUT, VARASIJALTA_HYVAKSYTTY, VARALLA, PERUUNTUNUT);
        } else {
            assertHakemustenTilat(PERUUNTUNUT, VARASIJALTA_HYVAKSYTTY, PERUUNTUNUT, PERUUNTUNUT);
        }

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan, toinenHakukohdeJohonHakemus1Hyvaksytaan, toinenHakukohdeJohonHakemus2Hyvaksytaan);

        if(bugiKorjattu) {
            assertHakemustenTilat(PERUUNTUNUT, PERUUNTUNUT, VARASIJALTA_HYVAKSYTTY, PERUUNTUNUT);
        } else {
            assertHakemustenTilat(PERUUNTUNUT, PERUUNTUNUT, PERUUNTUNUT, PERUUNTUNUT);
        }
    }

    @Test
    @Ignore //Onko validi testi?
    public void peruuntuneetEivatNouseHyvaksytyksiEnsimmaisenVarasijojenAstuttuaVoimaanAjetunSijoittelunJalkeenKunEiVarasijatayttoa() {
        jono.setAloituspaikat(1);
        jono.setEiVarasijatayttoa(false);
        jono.setVarasijat(1);

        sijoittele(kkHakuVarasijasaannotEiVoimassa, hakukohdeJossaVarasijojaRajoitetaan);
        assertHakemustenTilat(HYVAKSYTTY, VARALLA, VARALLA, VARALLA);

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan, toinenHakukohdeJohonHakemus1Hyvaksytaan);

        assertEquals(hakemus1.getHakemusOid(), toinenHakukohdeJohonHakemus1Hyvaksytaan.getValintatapajonot().get(0).getHakemukset().get(0).getHakemusOid());
        assertEquals(HYVAKSYTTY, toinenHakukohdeJohonHakemus1Hyvaksytaan.getValintatapajonot().get(0).getHakemukset().get(0).getTila());

        assertHakemustenTilat(PERUUNTUNUT, HYVAKSYTTY, PERUUNTUNUT, PERUUNTUNUT);

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohdeJossaVarasijojaRajoitetaan, toinenHakukohdeJohonHakemus1Hyvaksytaan, toinenHakukohdeJohonHakemus2Hyvaksytaan);

        assertHakemustenTilat(PERUUNTUNUT, PERUUNTUNUT, PERUUNTUNUT, PERUUNTUNUT);
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

        if (bugiKorjattu) {
            assertHakemustenTilat(PERUUNTUNUT, HYVAKSYTTY, VARALLA, VARALLA);
        } else {
            assertHakemustenTilat(PERUUNTUNUT, VARALLA, VARALLA, VARALLA);
        }
    }

    @Test
    public void hylatystaVaralleNousevatHakemuksetPaasevatVaralleMyosRajatussaVarasijataytossaJosNeTulevatRiittavanKorkealleJonosijalle() {
        jono.setAloituspaikat(1);
        jono.setEiVarasijatayttoa(false);
        jono.setVarasijat(1);
        jono.setSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa(false);

        sijoittele(sijoitteluajoWrapper -> sijoitteluajoWrapper.setKKHaku(true), hakukohdeJossaVarasijojaRajoitetaan);
        assertHakemustenTilat(HYVAKSYTTY, VARALLA, PERUUNTUNUT, PERUUNTUNUT);

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();

        final Hakemus kiilaavaHakemus = new HakemusBuilder().withOid("kiilaavaHakemus")
            .withEdellinenTila(HYLATTY).withTila(VARALLA).build();
        jono.getHakemukset().add(kiilaavaHakemus);
        hakemus1.setJonosija(0);
        kiilaavaHakemus.setJonosija(1);
        hakemus2.setJonosija(2);
        hakemus3.setJonosija(4);
        hakemus4.setJonosija(5);
        jono.setSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa(true);

        sijoittele(sijoitteluajoWrapper -> sijoitteluajoWrapper.setKKHaku(true), hakukohdeJossaVarasijojaRajoitetaan);

        if (bugiKorjattu) {
            assertHakemustenTilat(HYVAKSYTTY, VARALLA, PERUUNTUNUT, PERUUNTUNUT);
            assertEquals(VARALLA, kiilaavaHakemus.getTila());
        } else {
            assertHakemustenTilat(HYVAKSYTTY, PERUUNTUNUT, PERUUNTUNUT, PERUUNTUNUT);
            assertEquals(VARALLA, kiilaavaHakemus.getTila());
        }
    }

    private void korjaaTilaJaEdellinenTilaSijoittelunJalkeen() {
        Arrays.asList(hakemus1, hakemus2, hakemus3, hakemus4).forEach(h -> {
            HakemuksenTila tila = h.getTila();
            h.setEdellinenTila(tila);
            if(!HYLATTY.equals(tila)) {
                h.setTila(VARALLA);
            }
            h.setTilanKuvaukset(Collections.EMPTY_MAP);
        });
    }

    Consumer<SijoitteluajoWrapper> toinenAsteVarasijasaannotVoimassa = sijoitteluajoWrapper -> sijoitteluajoWrapper.setKKHaku(false);
    Consumer<SijoitteluajoWrapper> kkHakuVarasijasaannotVoimassa = sijoitteluajoWrapper -> sijoitteluajoWrapper.setKKHaku(true);
    Consumer<SijoitteluajoWrapper> kkHakuVarasijasaannotEiVoimassa = sijoitteluajoWrapper -> {
        sijoitteluajoWrapper.setVarasijaSaannotAstuvatVoimaan(LocalDateTime.now().plusDays(1));
        sijoitteluajoWrapper.setKKHaku(true);
    };

    private void assertHakemustenTilat(HakemuksenTila h1, HakemuksenTila h2, HakemuksenTila h3, HakemuksenTila h4) {
        assertEquals("Hakemus1", h1, hakemus1.getTila());
        assertEquals("Hakemus2", h2, hakemus2.getTila());
        assertEquals("Hakemus3", h3, hakemus3.getTila());
        assertEquals("hakemus4", h4, hakemus4.getTila());
    }

    private void sijoittele(Consumer<SijoitteluajoWrapper> prepareAjoWrapper, Hakukohde... hakukohteet) {
        SijoitteluajoWrapper sijoitteluAjoWrapper =
            SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluAjo(), Arrays.asList(hakukohteet), Collections.emptyList(), Collections.emptyMap());
        System.out.println(sijoitteluAjoWrapper.varasijaSaannotVoimassa());
        System.out.println(sijoitteluAjoWrapper.getVarasijaSaannotAstuvatVoimaan().toString());
        prepareAjoWrapper.accept(sijoitteluAjoWrapper);
        SijoitteluAlgorithmUtil.sijoittele(sijoitteluAjoWrapper);
    }
}
