package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.helper.HakuBuilder;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.SijoitteluAlgorithmUtil;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilojenMuokkaus;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.*;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.*;
import static fi.vm.sade.sijoittelu.domain.Tasasijasaanto.YLITAYTTO;
import static org.junit.Assert.assertEquals;

public class EhdollinenVastaanottoSitovaksiTest {
    private final boolean BUG_FIXED = true;

    private final Hakemus hakemus1 = new HakuBuilder.HakemusBuilder().withOid("hakemus1")
            .withJonosija(1).withTila(VARALLA).withPrioriteetti(0).build();
    private final Hakemus hakemus2 = new HakuBuilder.HakemusBuilder().withOid("hakemus2")
            .withJonosija(2).withTila(VARALLA).withPrioriteetti(0).build();
    private final Hakemus hakemus3 = new HakuBuilder.HakemusBuilder().withOid("hakemus3")
            .withJonosija(3).withTila(VARALLA).withPrioriteetti(0).build();
    private final Hakemus hakemus4 = new HakuBuilder.HakemusBuilder().withOid("hakemus4")
            .withJonosija(4).withTila(VARALLA).withPrioriteetti(0).build();

    private Valintatapajono jono = new HakuBuilder.ValintatapajonoBuilder().withOid("jono1")
            .withTasasijasaanto(YLITAYTTO)
            .withPrioriteetti(0)
            .withHakemukset(hakemus1, hakemus2, hakemus3, hakemus4)
            .withSivssnov(false)
            .build();

    private Hakukohde ylempiHakukohdeJossaEnsinVaralla = new HakuBuilder.HakukohdeBuilder("ylempiHakukohdeOid")
            .withValintatapajono(jono).build();

    private final Hakemus vastaanottoHakemus3 = new HakuBuilder.HakemusBuilder().withOid(hakemus3.getHakemusOid())
            .withJonosija(0).withPrioriteetti(1).withTila(HYVAKSYTTY).withEdellinenTila(HYVAKSYTTY).build();

    private Hakukohde alempiHakukohdeJohonVastaanottoKohdistuu = new HakuBuilder.HakukohdeBuilder("alempiHakukohdeOid")
            .withValintatapajono(new HakuBuilder.ValintatapajonoBuilder()
                    .withOid("alempiOid")
                    .withAloituspaikat(1)
                    .withTasasijasaanto(YLITAYTTO)
                    .withPrioriteetti(0)
                    .withHakemus(vastaanottoHakemus3)
                    .build()).build();

    private Valintatulos vastaanotto = new Valintatulos(vastaanottoHakemus3.getHakemusOid(),
            "hakija3", alempiHakukohdeJohonVastaanottoKohdistuu.getOid(), false,
            IlmoittautumisTila.EI_TEHTY, true, ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT, false,
            alempiHakukohdeJohonVastaanottoKohdistuu.getValintatapajonot().get(0).getOid());

    @Test
    public void vastaanottoMuuttuuSitovaksi() {
        sijoittele(kkHakuVarasijasaannotVoimassa, Arrays.asList(vastaanotto), alempiHakukohdeJohonVastaanottoKohdistuu);
        assertEquals(HYVAKSYTTY, vastaanottoHakemus3.getTila());
        assertEquals(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, vastaanotto.getTila());
    }

    @Test
    public void vastaanottoMuuttuuSitovaksiKoskaVarasijatayttoLoppuu() {
        jono.setEiVarasijatayttoa(false);
        jono.setVarasijat(null);
        jono.setAloituspaikat(1);

        sijoittele(kkHakuVarasijasaannotVoimassa, Arrays.asList(vastaanotto), ylempiHakukohdeJossaEnsinVaralla, alempiHakukohdeJohonVastaanottoKohdistuu);
        assertHakemustenTilat(HYVAKSYTTY, VARALLA, VARALLA, VARALLA);
        assertEquals(HYVAKSYTTY, vastaanottoHakemus3.getTila());
        assertEquals(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT, vastaanotto.getTila());

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();

        sijoittele(kkHakuVarasijatayttoPaattynyt, Arrays.asList(vastaanotto), ylempiHakukohdeJossaEnsinVaralla, alempiHakukohdeJohonVastaanottoKohdistuu);
        assertHakemustenTilat(HYVAKSYTTY, PERUUNTUNUT, PERUUNTUNUT, PERUUNTUNUT);
        assertEquals(HYVAKSYTTY, vastaanottoHakemus3.getTila());
        assertEquals(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, vastaanotto.getTila());
    }

    @Test
    public void vastaanottoMuuttuuSitovaksiKoskaEiMahtunutRajatuilleVarasijoille() {
        jono.setAloituspaikat(1);
        jono.setVarasijat(1);

        sijoittele(kkHakuVarasijasaannotEiVoimassa, Arrays.asList(vastaanotto), ylempiHakukohdeJossaEnsinVaralla, alempiHakukohdeJohonVastaanottoKohdistuu);
        assertHakemustenTilat(HYVAKSYTTY, VARALLA, VARALLA, VARALLA);
        assertEquals(HYVAKSYTTY, vastaanottoHakemus3.getTila());
        assertEquals(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT, vastaanotto.getTila());

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();

        sijoittele(kkHakuVarasijasaannotVoimassa, Arrays.asList(vastaanotto), ylempiHakukohdeJossaEnsinVaralla, alempiHakukohdeJohonVastaanottoKohdistuu);
        assertHakemustenTilat(HYVAKSYTTY, VARALLA, PERUUNTUNUT, PERUUNTUNUT);
        assertEquals(HYVAKSYTTY, vastaanottoHakemus3.getTila());
        if (BUG_FIXED) {
            assertEquals(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, vastaanotto.getTila());
        } else {
            assertEquals(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT, vastaanotto.getTila());
        }
    }

    @Test
    public void vastaanottoMuuttuuSitovaksiKoskaEiVarasijatayttoa() {
        jono.setAloituspaikat(2);
        jono.setEiVarasijatayttoa(true);
        sijoittele(kkHakuVarasijasaannotEiVoimassa, Arrays.asList(vastaanotto), ylempiHakukohdeJossaEnsinVaralla, alempiHakukohdeJohonVastaanottoKohdistuu);
        assertHakemustenTilat(HYVAKSYTTY, HYVAKSYTTY, VARALLA, VARALLA);
        assertEquals(HYVAKSYTTY, vastaanottoHakemus3.getTila());
        assertEquals(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT, vastaanotto.getTila());

        korjaaTilaJaEdellinenTilaSijoittelunJalkeen();

        sijoittele(kkHakuVarasijasaannotVoimassa, Arrays.asList(vastaanotto), ylempiHakukohdeJossaEnsinVaralla, alempiHakukohdeJohonVastaanottoKohdistuu);
        assertHakemustenTilat(HYVAKSYTTY, HYVAKSYTTY, PERUUNTUNUT, PERUUNTUNUT);
        assertEquals(HYVAKSYTTY, vastaanottoHakemus3.getTila());
        if (BUG_FIXED) {
            assertEquals(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI, vastaanotto.getTila());
        } else {
            assertEquals(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT, vastaanotto.getTila());
        }
    }

    Consumer<SijoitteluajoWrapper> kkHakuVarasijasaannotVoimassa = sijoitteluajoWrapper -> sijoitteluajoWrapper.setKKHaku(true);
    Consumer<SijoitteluajoWrapper> kkHakuVarasijasaannotEiVoimassa = sijoitteluajoWrapper -> {
        sijoitteluajoWrapper.setVarasijaSaannotAstuvatVoimaan(LocalDateTime.now().plusDays(1));
        sijoitteluajoWrapper.setKKHaku(true);
    };
    Consumer<SijoitteluajoWrapper> kkHakuVarasijatayttoPaattynyt = sijoitteluajoWrapper -> {
        sijoitteluajoWrapper.setVarasijaSaannotAstuvatVoimaan(LocalDateTime.now().minusDays(2));
        sijoitteluajoWrapper.setVarasijaTayttoPaattyy(LocalDateTime.now().minusDays(1));
        sijoitteluajoWrapper.setKKHaku(true);
    };

    private void assertHakemustenTilat(HakemuksenTila h1, HakemuksenTila h2, HakemuksenTila h3, HakemuksenTila h4) {
        assertEquals("Hakemus1", h1, hakemus1.getTila());
        assertEquals("Hakemus2", h2, hakemus2.getTila());
        assertEquals("Hakemus3", h3, hakemus3.getTila());
        assertEquals("hakemus4", h4, hakemus4.getTila());
    }

    private void sijoittele(Consumer<SijoitteluajoWrapper> prepareAjoWrapper, List<Valintatulos> valintatulokset, Hakukohde... hakukohteet) {
        SijoitteluajoWrapper sijoitteluAjoWrapper =
                SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluConfiguration(), new SijoitteluAjo(), Arrays.asList(hakukohteet), valintatulokset, Collections.emptyMap());
        sijoitteluAjoWrapper.paivitaVastaanottojenVaikutusHakemustenTiloihin(valintatulokset, Collections.emptyMap());
        prepareAjoWrapper.accept(sijoitteluAjoWrapper);
        SijoitteluAlgorithmUtil.sijoittele(sijoitteluAjoWrapper);
    }

    private void korjaaTilaJaEdellinenTilaSijoittelunJalkeen() {
        Arrays.asList(hakemus1, hakemus2, hakemus3, hakemus4).forEach(h -> {
            HakemuksenTila tila = h.getTila();
            h.setEdellinenTila(tila);
            if (!HYLATTY.equals(tila)) {
                TilojenMuokkaus.asetaTilaksiVaralla(h);
            }
            h.setTilanKuvaukset(TilanKuvaukset.tyhja);
        });
    }
}
