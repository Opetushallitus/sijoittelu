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
import static fi.vm.sade.sijoittelu.domain.Tasasijasaanto.*;
import static org.junit.Assert.assertEquals;

public class SijoitteluHyvaksyPeruuntunutTest {

    private final Hakemus hakemus1 = new HakuBuilder.HakemusBuilder().withOid("hakemus1")
            .withJonosija(1).withTila(VARALLA).withEdellinenTila(HYVAKSYTTY).withPrioriteetti(1).build();
    private final Hakemus hakemus2 = new HakuBuilder.HakemusBuilder().withOid("hakemus2")
            .withJonosija(2).withTila(VARALLA).withEdellinenTila(PERUUNTUNUT).withPrioriteetti(1).build();
    private final Hakemus hakemus3 = new HakuBuilder.HakemusBuilder().withOid("hakemus3")
            .withJonosija(3).withTila(VARALLA).withEdellinenTila(PERUUNTUNUT).withPrioriteetti(1).build();

    private Valintatapajono jono = new HakuBuilder.ValintatapajonoBuilder().withOid("jono1")
            .withTasasijasaanto(YLITAYTTO)
            .withPrioriteetti(0)
            .withHakemukset(hakemus1, hakemus2, hakemus3)
            .withSivssnov(true)
            .withAloituspaikat(0)
            .build();

    private Hakukohde hakukohde = new HakuBuilder.HakukohdeBuilder("hakukohde1")
            .withValintatapajono(jono).build();

    private Valintatulos valintatulos1 = new Valintatulos(hakemus1.getHakemusOid(),
            "hakija1", hakukohde.getOid(), false,
            IlmoittautumisTila.EI_TEHTY, true, ValintatuloksenTila.KESKEN, false,
            hakukohde.getValintatapajonot().get(0).getOid());

    private Valintatulos valintatulos2 = new Valintatulos(hakemus2.getHakemusOid(),
            "hakija2", hakukohde.getOid(), false,
            IlmoittautumisTila.EI_TEHTY, true, ValintatuloksenTila.KESKEN, false,
            hakukohde.getValintatapajonot().get(0).getOid());

    private Valintatulos valintatulos3 = new Valintatulos(hakemus3.getHakemusOid(),
            "hakija3", hakukohde.getOid(), false,
            IlmoittautumisTila.EI_TEHTY, true, ValintatuloksenTila.KESKEN, false,
            hakukohde.getValintatapajonot().get(0).getOid());

    Consumer<SijoitteluajoWrapper> kkHakuVarasijasaannotVoimassa = sijoitteluajoWrapper -> {
        sijoitteluajoWrapper.setKKHaku(true);
        sijoitteluajoWrapper.setVarasijaSaannotAstuvatVoimaan(LocalDateTime.now().minusDays(1));
        sijoitteluajoWrapper.setKaikkiKohteetSijoittelussa(LocalDateTime.now().minusDays(1));
        sijoitteluajoWrapper.setVarasijaTayttoPaattyy(LocalDateTime.now().plusDays(1));
    };

    private void sijoittele(Consumer<SijoitteluajoWrapper> prepareAjoWrapper, List<Valintatulos> valintatulokset, Hakukohde... hakukohteet) {
        SijoitteluajoWrapper sijoitteluAjoWrapper =
                SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluConfiguration(), new SijoitteluAjo(), Arrays.asList(hakukohteet), valintatulokset, Collections.emptyMap());
        prepareAjoWrapper.accept(sijoitteluAjoWrapper);
        SijoitteluAlgorithmUtil.sijoittele(sijoitteluAjoWrapper);
    }

    private void assertHakemustenTilat(HakemuksenTila h1, HakemuksenTila h2, HakemuksenTila h3) {
        assertEquals("Hakemus1", h1, hakemus1.getTila());
        assertEquals("Hakemus2", h2, hakemus2.getTila());
        assertEquals("Hakemus3", h3, hakemus3.getTila());
    }

    @Test
    public void hyvaksyPeruuntunutJonollaEiVarasijatayttoa() {
        jono.setEiVarasijatayttoa(true);
        jono.setVarasijat(0);
        jono.setSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa(true);
        valintatulos3.setHyvaksyPeruuntunut(true, "");
        sijoittele(kkHakuVarasijasaannotVoimassa, Arrays.asList(valintatulos1, valintatulos2, valintatulos3), hakukohde);
        assertHakemustenTilat(HYVAKSYTTY, PERUUNTUNUT, HYVAKSYTTY);
    }
}
