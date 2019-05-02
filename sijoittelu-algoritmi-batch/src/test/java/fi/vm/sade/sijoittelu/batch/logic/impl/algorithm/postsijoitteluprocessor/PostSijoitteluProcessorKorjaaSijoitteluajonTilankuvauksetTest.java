package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.postsijoitteluprocessor;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluConfiguration;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluajoWrapperFactory;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.helper.HakuBuilder;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.SijoitteluAlgorithmUtil;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.function.Consumer;

import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.*;
import static fi.vm.sade.sijoittelu.domain.TilankuvauksenTarkenne.*;
import static org.junit.Assert.assertEquals;

public class PostSijoitteluProcessorKorjaaSijoitteluajonTilankuvauksetTest {
    private final Hakemus hakemus1 = new HakuBuilder.HakemusBuilder().withOid("hakemus1")
            .withJonosija(1).withTila(HYVAKSYTTY).withHakijaOid("test1").withTilankuvauksenTarkenne(null).withPrioriteetti(1).build();
    private final Hakemus hakemus2 = new HakuBuilder.HakemusBuilder().withOid("hakemus2")
            .withJonosija(2).withTila(HYVAKSYTTY).withHakijaOid("test2").withTilankuvauksenTarkenne(TilankuvauksenTarkenne.PERUUNTUNUT_HYVAKSYTTY_TOISESSA_JONOSSA).withPrioriteetti(1).build();
    private final Hakemus hakemus3 = new HakuBuilder.HakemusBuilder().withOid("hakemus3")
            .withJonosija(3).withTila(HYVAKSYTTY).withHakijaOid("test3").withTilankuvauksenTarkenne(TilankuvauksenTarkenne.EI_TILANKUVAUKSEN_TARKENNETTA).withPrioriteetti(1).build();
    private final Hakemus hakemus4 = new HakuBuilder.HakemusBuilder().withOid("hakemus4")
            .withJonosija(4).withTila(VARASIJALTA_HYVAKSYTTY).withHakijaOid("test4").withTilankuvauksenTarkenne(null).withPrioriteetti(1).build();
    private final Hakemus hakemus5 = new HakuBuilder.HakemusBuilder().withOid("hakemus5")
            .withJonosija(5).withTila(VARASIJALTA_HYVAKSYTTY).withHakijaOid("test5").withTilankuvauksenTarkenne(TilankuvauksenTarkenne.PERUUNTUNUT_EI_MAHDU_VARASIJOJEN_MAARAAN).withPrioriteetti(1).build();
    private final Hakemus hakemus6 = new HakuBuilder.HakemusBuilder().withOid("hakemus6")
            .withJonosija(6).withTila(VARASIJALTA_HYVAKSYTTY).withHakijaOid("test6").withTilankuvauksenTarkenne(TilankuvauksenTarkenne.HYVAKSYTTY_VARASIJALTA).withPrioriteetti(1).build();
    private final Hakemus hakemus7 = new HakuBuilder.HakemusBuilder().withOid("hakemus7")
            .withJonosija(7).withTila(VARALLA).withEdellinenTila(VARALLA).withHakijaOid("test7").withTilankuvauksenTarkenne(null).withPrioriteetti(1).build();
    private final Hakemus hakemus8 = new HakuBuilder.HakemusBuilder().withOid("hakemus8")
            .withJonosija(8).withTila(VARALLA).withEdellinenTila(VARALLA).withHakijaOid("test8").withTilankuvauksenTarkenne(TilankuvauksenTarkenne.HYLATTY_HAKIJARYHMAAN_KUULUMATTOMANA).withPrioriteetti(1).build();
    private final Hakemus hakemus9 = new HakuBuilder.HakemusBuilder().withOid("hakemus9")
            .withJonosija(9).withTila(VARALLA).withEdellinenTila(VARALLA).withHakijaOid("test9").withTilankuvauksenTarkenne(TilankuvauksenTarkenne.EI_TILANKUVAUKSEN_TARKENNETTA).withPrioriteetti(1).build();
    private final Hakemus hakemus10 = new HakuBuilder.HakemusBuilder().withOid("hakemus10")
            .withJonosija(10).withTila(PERUUNTUNUT).withHakijaOid("test10").withTilankuvauksenTarkenne(TilankuvauksenTarkenne.PERUUNTUNUT_VASTAANOTTANUT_TOISEN_PAIKAN).withPrioriteetti(1).build();

    private Valintatapajono jono = new HakuBuilder.ValintatapajonoBuilder().withOid("jono1")
            .withPrioriteetti(0)
            .withTasasijasaanto(Tasasijasaanto.YLITAYTTO)
            .withHakemukset(hakemus1, hakemus2, hakemus3, hakemus4, hakemus5, hakemus6, hakemus7, hakemus8, hakemus9, hakemus10)
            .build();

    private Hakukohde hakukohde = new HakuBuilder.HakukohdeBuilder("hakukohdeOid")
            .withValintatapajono(jono).build();

    @Test
    public void tilanTarkenteidenSiivous() {
        jono.setAloituspaikat(2);
        jono.setEiVarasijatayttoa(false);
        jono.setVarasijat(4);

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohde);

        assertHakemustenTilat(HYVAKSYTTY, HYVAKSYTTY, HYVAKSYTTY, VARASIJALTA_HYVAKSYTTY, VARASIJALTA_HYVAKSYTTY,
                VARASIJALTA_HYVAKSYTTY, VARALLA, VARALLA, VARALLA, PERUUNTUNUT);
        assertHakemustenTilaKuvauksenTarkenteet(EI_TILANKUVAUKSEN_TARKENNETTA, EI_TILANKUVAUKSEN_TARKENNETTA, EI_TILANKUVAUKSEN_TARKENNETTA,
                HYVAKSYTTY_VARASIJALTA, HYVAKSYTTY_VARASIJALTA, HYVAKSYTTY_VARASIJALTA, EI_TILANKUVAUKSEN_TARKENNETTA,
                EI_TILANKUVAUKSEN_TARKENNETTA, EI_TILANKUVAUKSEN_TARKENNETTA, PERUUNTUNUT_VASTAANOTTANUT_TOISEN_PAIKAN);
        assertTilanKuvaukset(TilanKuvaukset.tyhja.get("FI"), TilanKuvaukset.tyhja.get("FI"), TilanKuvaukset.tyhja.get("FI"),
                TilanKuvaukset.varasijaltaHyvaksytty().get("FI"), TilanKuvaukset.varasijaltaHyvaksytty().get("FI"), TilanKuvaukset.varasijaltaHyvaksytty().get("FI"),
                TilanKuvaukset.tyhja.get("FI"), TilanKuvaukset.tyhja.get("FI"), TilanKuvaukset.tyhja.get("FI"),
                TilanKuvaukset.peruuntunutVastaanottanutToisenOpiskelupaikan().get("FI"));
    }

    @Test
    public void tilanmuutosSijoittelunJalkeenJaUudelleenSijoittelu() {
        jono.setAloituspaikat(3);
        jono.setEiVarasijatayttoa(false);
        jono.setVarasijat(0);

        hakemus5.setTila(HYLATTY);
        hakemus5.setTilankuvauksenTarkenne(TilankuvauksenTarkenne.PERUUNTUNUT_EI_MAHDU_VARASIJOJEN_MAARAAN);

        hakemus6.setTila(HYLATTY);
        hakemus6.setTilankuvauksenTarkenne(TilankuvauksenTarkenne.HYLATTY_HAKIJARYHMAAN_KUULUMATTOMANA);

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohde);

        assertHakemustenTilat(HYVAKSYTTY, HYVAKSYTTY, HYVAKSYTTY, VARASIJALTA_HYVAKSYTTY, HYLATTY,
                HYLATTY, VARALLA, VARALLA, VARALLA, PERUUNTUNUT);
        assertHakemustenTilaKuvauksenTarkenteet(EI_TILANKUVAUKSEN_TARKENNETTA, EI_TILANKUVAUKSEN_TARKENNETTA, EI_TILANKUVAUKSEN_TARKENNETTA,
                HYVAKSYTTY_VARASIJALTA, PERUUNTUNUT_EI_MAHDU_VARASIJOJEN_MAARAAN, HYLATTY_HAKIJARYHMAAN_KUULUMATTOMANA,
                EI_TILANKUVAUKSEN_TARKENNETTA, EI_TILANKUVAUKSEN_TARKENNETTA, EI_TILANKUVAUKSEN_TARKENNETTA, PERUUNTUNUT_VASTAANOTTANUT_TOISEN_PAIKAN);
        assertTilanKuvaukset(TilanKuvaukset.tyhja.get("FI"), TilanKuvaukset.tyhja.get("FI"), TilanKuvaukset.tyhja.get("FI"),
                TilanKuvaukset.varasijaltaHyvaksytty().get("FI"), TilanKuvaukset.peruuntunutEiMahduKasiteltavienVarasijojenMaaraan().get("FI"), TilanKuvaukset.hylattyHakijaryhmaanKuulumattomana(null).get("FI"),
                TilanKuvaukset.tyhja.get("FI"), TilanKuvaukset.tyhja.get("FI"), TilanKuvaukset.tyhja.get("FI"),
                TilanKuvaukset.peruuntunutVastaanottanutToisenOpiskelupaikan().get("FI"));

        // Lisätään varasija ja muutetaan hakemuksen tila:
        jono.setVarasijat(1);
        hakemus5.setTila(VARASIJALTA_HYVAKSYTTY);

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohde);

        assertHakemustenTilat(HYVAKSYTTY, HYVAKSYTTY, HYVAKSYTTY, VARASIJALTA_HYVAKSYTTY, VARASIJALTA_HYVAKSYTTY,
                HYLATTY, VARALLA, VARALLA, VARALLA, PERUUNTUNUT);
        assertHakemustenTilaKuvauksenTarkenteet(EI_TILANKUVAUKSEN_TARKENNETTA, EI_TILANKUVAUKSEN_TARKENNETTA, EI_TILANKUVAUKSEN_TARKENNETTA,
                HYVAKSYTTY_VARASIJALTA, HYVAKSYTTY_VARASIJALTA, HYLATTY_HAKIJARYHMAAN_KUULUMATTOMANA,
                EI_TILANKUVAUKSEN_TARKENNETTA, EI_TILANKUVAUKSEN_TARKENNETTA, EI_TILANKUVAUKSEN_TARKENNETTA, PERUUNTUNUT_VASTAANOTTANUT_TOISEN_PAIKAN);
        assertTilanKuvaukset(TilanKuvaukset.tyhja.get("FI"), TilanKuvaukset.tyhja.get("FI"), TilanKuvaukset.tyhja.get("FI"),
                TilanKuvaukset.varasijaltaHyvaksytty().get("FI"), TilanKuvaukset.varasijaltaHyvaksytty().get("FI"), TilanKuvaukset.hylattyHakijaryhmaanKuulumattomana(null).get("FI"),
                TilanKuvaukset.tyhja.get("FI"), TilanKuvaukset.tyhja.get("FI"), TilanKuvaukset.tyhja.get("FI"),
                TilanKuvaukset.peruuntunutVastaanottanutToisenOpiskelupaikan().get("FI"));

        // Hakemus2 perutaan ja hakemus 6 varalle:
        hakemus2.setTila(PERUNUT);
        hakemus6.setTila(VARALLA);

        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohde);

        assertHakemustenTilat(HYVAKSYTTY, PERUNUT, HYVAKSYTTY, VARASIJALTA_HYVAKSYTTY, VARASIJALTA_HYVAKSYTTY,
                VARALLA, VARALLA, VARALLA,VARALLA, PERUUNTUNUT);
        assertHakemustenTilaKuvauksenTarkenteet(EI_TILANKUVAUKSEN_TARKENNETTA, EI_TILANKUVAUKSEN_TARKENNETTA, EI_TILANKUVAUKSEN_TARKENNETTA,
                HYVAKSYTTY_VARASIJALTA, HYVAKSYTTY_VARASIJALTA, EI_TILANKUVAUKSEN_TARKENNETTA,
                EI_TILANKUVAUKSEN_TARKENNETTA, EI_TILANKUVAUKSEN_TARKENNETTA, EI_TILANKUVAUKSEN_TARKENNETTA, PERUUNTUNUT_VASTAANOTTANUT_TOISEN_PAIKAN);
        assertTilanKuvaukset(TilanKuvaukset.tyhja.get("FI"), TilanKuvaukset.tyhja.get("FI"), TilanKuvaukset.tyhja.get("FI"),
                TilanKuvaukset.varasijaltaHyvaksytty().get("FI"), TilanKuvaukset.varasijaltaHyvaksytty().get("FI"), TilanKuvaukset.tyhja.get("FI"),
                TilanKuvaukset.tyhja.get("FI"), TilanKuvaukset.tyhja.get("FI"), TilanKuvaukset.tyhja.get("FI"),
                TilanKuvaukset.peruuntunutVastaanottanutToisenOpiskelupaikan().get("FI"));
    }

    @Test
    public void customHakemuksenTilanKuvausTeksti() {
        jono.setAloituspaikat(2);
        jono.setEiVarasijatayttoa(false);
        jono.setVarasijat(4);

        //Jos tila tai tilan tarkenne muuttuu, muuttuu myös tilankuvauksen teksti.

        // Tarkenne muuttuu
        hakemus2.setTilankuvauksenTarkenne(TilankuvauksenTarkenne.PERUUNTUNUT_EI_MAHDU_VARASIJOJEN_MAARAAN);
        hakemus2.setTilanKuvaukset(new HashMap<String, String>() {{
            put("FI", "Suomenkielinen teksti 2");
            put("SV", "Svensk text 2");
            put("EN", "English Text 2");
        }});

        // Pitäisi olla tyhjä, koska tila on hyväksytty.
        hakemus3.setTilanKuvaukset(new HashMap<String, String>() {{
            put("FI", "Suomenkielinen teksti 3");
            put("SV", "Svensk text 3");
            put("EN", "English Text 3");
        }});

        // Pitäisi olla tyhjä, koska tila on varalla.
        hakemus7.setTilanKuvaukset(new HashMap<String, String>() {{
            put("FI", "Suomenkielinen teksti 6");
            put("SV", "Svensk text 6");
            put("EN", "English Text 6");
        }});


        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohde);

        assertHakemustenTilat(HYVAKSYTTY, HYVAKSYTTY, HYVAKSYTTY, VARASIJALTA_HYVAKSYTTY, VARASIJALTA_HYVAKSYTTY,
                VARASIJALTA_HYVAKSYTTY, VARALLA, VARALLA, VARALLA, PERUUNTUNUT);
        assertHakemustenTilaKuvauksenTarkenteet(EI_TILANKUVAUKSEN_TARKENNETTA, EI_TILANKUVAUKSEN_TARKENNETTA, EI_TILANKUVAUKSEN_TARKENNETTA,
                HYVAKSYTTY_VARASIJALTA, HYVAKSYTTY_VARASIJALTA, HYVAKSYTTY_VARASIJALTA, EI_TILANKUVAUKSEN_TARKENNETTA,
                EI_TILANKUVAUKSEN_TARKENNETTA, EI_TILANKUVAUKSEN_TARKENNETTA, PERUUNTUNUT_VASTAANOTTANUT_TOISEN_PAIKAN);
        assertTilanKuvaukset(TilanKuvaukset.tyhja.get("FI"), TilanKuvaukset.tyhja.get("FI"), TilanKuvaukset.tyhja.get("FI"),
                TilanKuvaukset.varasijaltaHyvaksytty().get("FI"), TilanKuvaukset.varasijaltaHyvaksytty().get("FI"), TilanKuvaukset.varasijaltaHyvaksytty().get("FI"),
                TilanKuvaukset.tyhja.get("FI"), TilanKuvaukset.tyhja.get("FI"), TilanKuvaukset.tyhja.get("FI"),
                TilanKuvaukset.peruuntunutVastaanottanutToisenOpiskelupaikan().get("FI"));

        jono.setVarasijat(3);

        // Tila muuttuu
        hakemus5.setTila(HYVAKSYTTY);
        hakemus5.setTilanKuvaukset(new HashMap<String, String>() {{
            put("FI", "Suomenkielinen teksti 5");
            put("SV", "Svensk text 5");
            put("EN", "English Text 5");
        }});

        // Tila ei muutu.
        hakemus6.setTilanKuvaukset(new HashMap<String, String>() {{
            put("FI", "Suomenkielinen teksti 6");
            put("SV", "Svensk text 6");
            put("EN", "English Text 6");
        }});


        sijoittele(kkHakuVarasijasaannotVoimassa, hakukohde);

        assertHakemustenTilat(HYVAKSYTTY, HYVAKSYTTY, HYVAKSYTTY, VARASIJALTA_HYVAKSYTTY, HYVAKSYTTY,
                VARASIJALTA_HYVAKSYTTY, VARALLA, VARALLA, VARALLA, PERUUNTUNUT);
        assertHakemustenTilaKuvauksenTarkenteet(EI_TILANKUVAUKSEN_TARKENNETTA, EI_TILANKUVAUKSEN_TARKENNETTA, EI_TILANKUVAUKSEN_TARKENNETTA,
                HYVAKSYTTY_VARASIJALTA, EI_TILANKUVAUKSEN_TARKENNETTA, HYVAKSYTTY_VARASIJALTA,
                EI_TILANKUVAUKSEN_TARKENNETTA, EI_TILANKUVAUKSEN_TARKENNETTA, EI_TILANKUVAUKSEN_TARKENNETTA, PERUUNTUNUT_VASTAANOTTANUT_TOISEN_PAIKAN);
        assertTilanKuvaukset(TilanKuvaukset.tyhja.get("FI"), TilanKuvaukset.tyhja.get("FI"), TilanKuvaukset.tyhja.get("FI"),
                TilanKuvaukset.varasijaltaHyvaksytty().get("FI"), TilanKuvaukset.tyhja.get("FI"), "Suomenkielinen teksti 6",
                TilanKuvaukset.tyhja.get("FI"), TilanKuvaukset.tyhja.get("FI"), TilanKuvaukset.tyhja.get("FI"),
                TilanKuvaukset.peruuntunutVastaanottanutToisenOpiskelupaikan().get("FI"));
    }

    Consumer<SijoitteluajoWrapper> kkHakuVarasijasaannotVoimassa = sijoitteluajoWrapper -> sijoitteluajoWrapper.setKKHaku(true);

    private void assertHakemustenTilat(HakemuksenTila h1, HakemuksenTila h2, HakemuksenTila h3, HakemuksenTila h4, HakemuksenTila h5, HakemuksenTila h6,
                                       HakemuksenTila h7, HakemuksenTila h8, HakemuksenTila h9, HakemuksenTila h10) {
        assertEquals("Hakemus1", h1, hakemus1.getTila());
        assertEquals("Hakemus2", h2, hakemus2.getTila());
        assertEquals("Hakemus3", h3, hakemus3.getTila());
        assertEquals("hakemus4", h4, hakemus4.getTila());
        assertEquals("hakemus5", h5, hakemus5.getTila());
        assertEquals("hakemus6", h6, hakemus6.getTila());
        assertEquals("hakemus7", h7, hakemus7.getTila());
        assertEquals("hakemus8", h8, hakemus8.getTila());
        assertEquals("hakemus9", h9, hakemus9.getTila());
        assertEquals("hakemus10", h10, hakemus10.getTila());

    }


    private void assertTilanKuvaukset(String t1, String t2, String t3, String t4, String t5,
                                      String t6, String t7, String t8, String t9, String t10) {
        assertEquals("Hakemus1", t1, hakemus1.getTilanKuvaukset().get("FI"));
        assertEquals("Hakemus2", t2, hakemus2.getTilanKuvaukset().get("FI"));
        assertEquals("Hakemus3", t3, hakemus3.getTilanKuvaukset().get("FI"));
        assertEquals("Hakemus4", t4, hakemus4.getTilanKuvaukset().get("FI"));
        assertEquals("Hakemus5", t5, hakemus5.getTilanKuvaukset().get("FI"));
        assertEquals("Hakemus6", t6, hakemus6.getTilanKuvaukset().get("FI"));
        assertEquals("Hakemus7", t7, hakemus7.getTilanKuvaukset().get("FI"));
        assertEquals("Hakemus8", t8, hakemus8.getTilanKuvaukset().get("FI"));
        assertEquals("Hakemus9", t9, hakemus9.getTilanKuvaukset().get("FI"));
        assertEquals("Hakemus10", t10, hakemus10.getTilanKuvaukset().get("FI"));
    }

    private void assertHakemustenTilaKuvauksenTarkenteet(TilankuvauksenTarkenne t1, TilankuvauksenTarkenne t2, TilankuvauksenTarkenne t3,
                                                         TilankuvauksenTarkenne t4, TilankuvauksenTarkenne t5, TilankuvauksenTarkenne t6,
                                                         TilankuvauksenTarkenne t7, TilankuvauksenTarkenne t8, TilankuvauksenTarkenne t9,
                                                         TilankuvauksenTarkenne t10) {
        assertEquals("Hakemus1", t1, hakemus1.getTilankuvauksenTarkenne());
        assertEquals("Hakemus2", t2, hakemus2.getTilankuvauksenTarkenne());
        assertEquals("Hakemus3", t3, hakemus3.getTilankuvauksenTarkenne());
        assertEquals("hakemus4", t4, hakemus4.getTilankuvauksenTarkenne());
        assertEquals("hakemus5", t5, hakemus5.getTilankuvauksenTarkenne());
        assertEquals("hakemus6", t6, hakemus6.getTilankuvauksenTarkenne());
        assertEquals("hakemus7", t7, hakemus7.getTilankuvauksenTarkenne());
        assertEquals("hakemus8", t8, hakemus8.getTilankuvauksenTarkenne());
        assertEquals("hakemus9", t9, hakemus9.getTilankuvauksenTarkenne());
        assertEquals("hakemus10", t10, hakemus10.getTilankuvauksenTarkenne());
    }

    private void sijoittele(Consumer<SijoitteluajoWrapper> prepareAjoWrapper, Hakukohde... hakukohteet) {
        SijoitteluajoWrapper sijoitteluAjoWrapper =
                SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(new SijoitteluConfiguration(), new SijoitteluAjo(), Arrays.asList(hakukohteet), Collections.emptyList(), Collections.emptyMap());
        prepareAjoWrapper.accept(sijoitteluAjoWrapper);
        SijoitteluAlgorithmUtil.sijoittele(sijoitteluAjoWrapper);
    }

}
