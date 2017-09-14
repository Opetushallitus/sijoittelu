package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.HYLATTY;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.HYVAKSYTTY;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.PERUUNTUNUT;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.VARALLA;
import static fi.vm.sade.sijoittelu.domain.Tasasijasaanto.YLITAYTTO;
import static org.junit.Assert.assertEquals;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.helper.HakuBuilder.HakemusBuilder;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.helper.HakuBuilder.HakukohdeBuilder;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.helper.HakuBuilder.ValintatapajonoBuilder;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.SijoitteluAlgorithmUtil;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

public class RajattuVarasijatayttoTest {
    private final Hakemus hakemus1 = new HakemusBuilder().withOid("hakemus1")
        .withJonosija(1).withTila(VARALLA).withPrioriteetti(1).build();
    private final Hakemus hakemus2 = new HakemusBuilder().withOid("hakemus2")
        .withJonosija(2).withTila(VARALLA).build();
    private final Hakemus hakemus3 = new HakemusBuilder().withOid("hakemus3")
        .withJonosija(3).withTila(VARALLA).build();
    private final Hakemus hakemus4 = new HakemusBuilder().withOid("hakemus4")
        .withJonosija(4).withTila(VARALLA).build();

    private Valintatapajono jono = new ValintatapajonoBuilder().withOid("jono1")
        .withTasasijasaanto(YLITAYTTO)
        .withPrioriteetti(0)
        .withHakemukset(hakemus1, hakemus2, hakemus3, hakemus4)
        .withSivssnov(true)
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

    @Test
    public void peruuntuneetNousevatVarasijataytonAikanaRajoitetuilleVarasijoilleJosNiilleOnTullutTilaa() {
        jono.setAloituspaikat(1);
        jono.setEiVarasijatayttoa(false);
        jono.setVarasijat(1);

        sijoittele(sijoitteluajoWrapper -> sijoitteluajoWrapper.setKKHaku(true), hakukohdeJossaVarasijojaRajoitetaan);
        assertEquals(HYVAKSYTTY, hakemus1.getTila());
        assertEquals(VARALLA, hakemus2.getTila());
        assertEquals(PERUUNTUNUT, hakemus3.getTila());
        assertEquals(PERUUNTUNUT, hakemus4.getTila());

        sijoittele(sijoitteluajoWrapper -> sijoitteluajoWrapper.setKKHaku(true), hakukohdeJossaVarasijojaRajoitetaan, toinenHakukohdeJohonHakemus1Hyvaksytaan);

        assertEquals(hakemus1.getHakemusOid(), toinenHakukohdeJohonHakemus1Hyvaksytaan.getValintatapajonot().get(0).getHakemukset().get(0).getHakemusOid());
        assertEquals(HYVAKSYTTY, toinenHakukohdeJohonHakemus1Hyvaksytaan.getValintatapajonot().get(0).getHakemukset().get(0).getTila());

        final boolean bugiKorjattu = false;
        assertEquals(PERUUNTUNUT, hakemus1.getTila());
        assertEquals(HYVAKSYTTY, hakemus2.getTila());
        if (bugiKorjattu) {
            assertEquals(VARALLA, hakemus3.getTila());
        } else {
            assertEquals(PERUUNTUNUT, hakemus3.getTila());
        }
        assertEquals(PERUUNTUNUT, hakemus4.getTila());
    }

    @Test
    public void korkeaAsteellaPeruuntuneetNousevatVarasijataytonAikanaHyvaksytyiksiJosTilaaOnTullutVaikkaVarasijatayttoOlisiEstetty() {
        jono.setAloituspaikat(1);
        jono.setEiVarasijatayttoa(true);
        jono.setVarasijat(null);

        sijoittele(sijoitteluajoWrapper -> sijoitteluajoWrapper.setKKHaku(true), hakukohdeJossaVarasijojaRajoitetaan);
        assertEquals(HYVAKSYTTY, hakemus1.getTila());
        assertEquals(PERUUNTUNUT, hakemus2.getTila());
        assertEquals(PERUUNTUNUT, hakemus3.getTila());
        assertEquals(PERUUNTUNUT, hakemus4.getTila());

        sijoittele(sijoitteluajoWrapper -> sijoitteluajoWrapper.setKKHaku(true), hakukohdeJossaVarasijojaRajoitetaan, toinenHakukohdeJohonHakemus1Hyvaksytaan);

        assertEquals(hakemus1.getHakemusOid(), toinenHakukohdeJohonHakemus1Hyvaksytaan.getValintatapajonot().get(0).getHakemukset().get(0).getHakemusOid());
        assertEquals(HYVAKSYTTY, toinenHakukohdeJohonHakemus1Hyvaksytaan.getValintatapajonot().get(0).getHakemukset().get(0).getTila());

        final boolean bugiKorjattu = false;
        assertEquals(PERUUNTUNUT, hakemus1.getTila());
        if (bugiKorjattu) {
            assertEquals(HYVAKSYTTY, hakemus2.getTila());
        } else {
            assertEquals(PERUUNTUNUT, hakemus2.getTila());
        }
        assertEquals(PERUUNTUNUT, hakemus3.getTila());
        assertEquals(PERUUNTUNUT, hakemus4.getTila());
    }

    @Test
    public void toisellaAsteellaVarallaolijatNousevatVarasijataytonAikanaHyvaksytyiksiJosTilaaOnTullutVaikkaVarasijatayttoOlisiEstetty() {
        jono.setAloituspaikat(1);
        jono.setEiVarasijatayttoa(true);
        jono.setVarasijat(null);

        sijoittele(sijoitteluajoWrapper -> sijoitteluajoWrapper.setKKHaku(false), hakukohdeJossaVarasijojaRajoitetaan);
        assertEquals(HYVAKSYTTY, hakemus1.getTila());
        // Huom: 2. asteella (musiikkialan koulutusten takia) ei peruunnuteta niitä, jotka eivät mahdu aloituspaikkoihin
        assertEquals(VARALLA, hakemus2.getTila());
        assertEquals(VARALLA, hakemus3.getTila());
        assertEquals(VARALLA, hakemus4.getTila());

        sijoittele(sijoitteluajoWrapper -> sijoitteluajoWrapper.setKKHaku(false), hakukohdeJossaVarasijojaRajoitetaan, toinenHakukohdeJohonHakemus1Hyvaksytaan);

        assertEquals(hakemus1.getHakemusOid(), toinenHakukohdeJohonHakemus1Hyvaksytaan.getValintatapajonot().get(0).getHakemukset().get(0).getHakemusOid());
        assertEquals(HYVAKSYTTY, toinenHakukohdeJohonHakemus1Hyvaksytaan.getValintatapajonot().get(0).getHakemukset().get(0).getTila());

        final boolean bugiKorjattu = false;
        assertEquals(PERUUNTUNUT, hakemus1.getTila());
        if (bugiKorjattu) {
            assertEquals(HYVAKSYTTY, hakemus2.getTila());
        } else {
            assertEquals(VARALLA, hakemus2.getTila());
        }
        assertEquals(VARALLA, hakemus3.getTila());
        assertEquals(VARALLA, hakemus4.getTila());
    }

    @Test
    public void hylatystaVaralleNousevatHakemuksetPaasevatVaralleMyosRajatussaVarasijataytossaJosNeTulevatRiittavanKorkealleJonosijalle() {
        jono.setAloituspaikat(1);
        jono.setEiVarasijatayttoa(false);
        jono.setVarasijat(1);

        sijoittele(sijoitteluajoWrapper -> sijoitteluajoWrapper.setKKHaku(true), hakukohdeJossaVarasijojaRajoitetaan);
        assertEquals(HYVAKSYTTY, hakemus1.getTila());
        assertEquals(VARALLA, hakemus2.getTila());
        assertEquals(PERUUNTUNUT, hakemus3.getTila());
        assertEquals(PERUUNTUNUT, hakemus4.getTila());

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

        final boolean bugiKorjattu = false;

        assertEquals(HYVAKSYTTY, hakemus1.getTila());
        assertEquals(VARALLA, kiilaavaHakemus.getTila());
        if (bugiKorjattu) {
            assertEquals(VARALLA, hakemus2.getTila());
        } else {
            assertEquals(PERUUNTUNUT, hakemus2.getTila());
        }
        assertEquals(PERUUNTUNUT, hakemus3.getTila());
        assertEquals(PERUUNTUNUT, hakemus4.getTila());
    }

    private void sijoittele(Consumer<SijoitteluajoWrapper> prepareAjoWrapper, Hakukohde... hakukohteet) {
        SijoitteluajoWrapper sijoitteluAjoWrapper =
            SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluAjo(), Arrays.asList(hakukohteet), Collections.emptyList(), Collections.emptyMap());
        prepareAjoWrapper.accept(sijoitteluAjoWrapper);
        SijoitteluAlgorithmUtil.sijoittele(sijoitteluAjoWrapper);
    }
}
