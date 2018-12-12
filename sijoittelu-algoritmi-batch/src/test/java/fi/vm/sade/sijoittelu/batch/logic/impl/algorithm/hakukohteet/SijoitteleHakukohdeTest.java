package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.hakukohteet;

import static fi.vm.sade.sijoittelu.SijoitteluMatchers.hasTila;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.*;
import static fi.vm.sade.sijoittelu.domain.Tasasijasaanto.ARVONTA;
import static fi.vm.sade.sijoittelu.domain.Tasasijasaanto.YLITAYTTO;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertThat;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluajoWrapperFactory;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.helper.HakuBuilder;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.helper.HakuBuilder.HakemusBuilder;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.helper.HakuBuilder.HakukohdeBuilder;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.helper.HakuBuilder.ValintatapajonoBuilder;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.SijoitteluAlgorithmUtil;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.*;
import fi.vm.sade.sijoittelu.domain.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SijoitteleHakukohdeTest {

    private final String hakijaryhmaOid = "hakijaryhmaOid";

    private final Hakemus hakemus1 = new HakuBuilder.HakemusBuilder().withOid("hakemus1")
        .withJonosija(1).withTila(HYVAKSYTTY).withEdellinenTila(HYVAKSYTTY).withPrioriteetti(1).build();
    private final Hakemus hakemus2 = new HakuBuilder.HakemusBuilder().withOid("hakemus2")
        .withJonosija(2).withTila(VARALLA).withEdellinenTila(HYVAKSYTTY).withPrioriteetti(3).build();
    private final Hakemus hakemus3 = new HakuBuilder.HakemusBuilder().withOid("hakemus3")
        .withJonosija(3).withTila(VARALLA).withEdellinenTila(VARASIJALTA_HYVAKSYTTY).withPrioriteetti(2).build();
    private final Hakemus hakemus4 = new HakuBuilder.HakemusBuilder().withOid("hakemus4")
        .withJonosija(4).withTila(VARALLA).withEdellinenTila(HYLATTY).withPrioriteetti(1).build();

    private Valintatapajono jono = new HakuBuilder.ValintatapajonoBuilder().withOid("jono1")
        .withTasasijasaanto(YLITAYTTO)
        .withAloituspaikat(2)
        .withPrioriteetti(0)
        .withHakemukset(hakemus1, hakemus2, hakemus3, hakemus4)
        .withSivssnov(true)
        .build();

    private SijoitteluajoWrapper sijoitteluajoWrapper;
    private HakukohdeWrapper hakukohdeWrapper;
    private ValintatapajonoWrapper valintatapajonoWrapper;

    private static List<Hakemus> generateHakemukset(int nToGenerate, int startingJonosija, Hakijaryhma hakijaryhma) {
        List<Hakemus> results = new ArrayList<>(nToGenerate);
        for (int i = startingJonosija; i < startingJonosija+nToGenerate; i++) {
            results.add(generateHakemus(i, hakijaryhma));
        }
        return results;
    }

    private static Hakemus generateHakemus(int i, Hakijaryhma hakijaryhma) {
        return generateHakemus(i, i, hakijaryhma);
    }

    private static Hakemus generateHakemus(int i, int jonosija, Hakijaryhma hakijaryhma) {
        Hakemus h = new Hakemus();
        h.setJonosija(jonosija);
        h.setPrioriteetti(0);
        h.setHakemusOid("hakemus" + i);
        h.setHakijaOid("hakija" + i);
        if (hakijaryhma != null) {
            hakijaryhma.getHakemusOid().add(h.getHakemusOid());
        }
        h.setPisteet(new BigDecimal(i));
        h.setTila(HakemuksenTila.VARALLA);
        return h;
    }

    @Before
    public void valmisteleSijoitteluajoWrapper() {
        jono.setEiVarasijatayttoa(false);
        Hakukohde hakukohde = new HakuBuilder.HakukohdeBuilder("Testihakukohde_1").withValintatapajono(jono).build();
        sijoitteluajoWrapper = new HakuBuilder.SijoitteluajoWrapperBuilder(Collections.singletonList(hakukohde)).build();
        sijoitteluajoWrapper.setVarasijaSaannotAstuvatVoimaan(LocalDateTime.now().minusDays(2));
        sijoitteluajoWrapper.setVarasijaTayttoPaattyy(LocalDateTime.now().minusDays(1));
        assertTrue(sijoitteluajoWrapper.varasijaSaannotVoimassa());
        List<HakemusWrapper> hwlist = new ArrayList<>();
        HakemusWrapper hw1 = new HakemusWrapper();
        hw1.setHakemus(hakemus1);
        hw1.setHenkilo(new HenkiloWrapper());
        HakemusWrapper hw2 = new HakemusWrapper();
        hw2.setHakemus(hakemus2);
        hw2.setHenkilo(new HenkiloWrapper());
        HakemusWrapper hw3 = new HakemusWrapper();
        hw3.setHakemus(hakemus3);
        hw3.setHenkilo(new HenkiloWrapper());
        HakemusWrapper hw4 = new HakemusWrapper();
        hw4.setHakemus(hakemus4);
        hw4.setHenkilo(new HenkiloWrapper());
        hwlist.add(hw1);
        hwlist.add(hw2);
        hwlist.add(hw3);
        hwlist.add(hw4);

        valintatapajonoWrapper = new ValintatapajonoWrapper();
        valintatapajonoWrapper.setValintatapajono(jono);
        valintatapajonoWrapper.setHakemukset(hwlist);
        valintatapajonoWrapper.getHakemukset().forEach(hw -> {
            hw.setValintatapajono(valintatapajonoWrapper);
        });
        hakukohdeWrapper = new HakukohdeWrapper();
        hakukohdeWrapper.setHakukohde(hakukohde);
        hakukohdeWrapper.setValintatapajonot(Collections.singletonList(valintatapajonoWrapper));
        valintatapajonoWrapper.setHakukohdeWrapper(hakukohdeWrapper);
    }

    @Test
    public void testValintatapajononSijoitteluEiPeruunnutaEdellisenSijoitteluajonHyvaksymiaHakijoitaJosVarasijatayttoOnPaattynyt() {
        SijoitteleHakukohde.sijoitteleHakukohde(sijoitteluajoWrapper, hakukohdeWrapper);
        assertTrue(HakemuksenTila.HYVAKSYTTY.equals(hakemus1.getTila()));
        assertTrue(HakemuksenTila.HYVAKSYTTY.equals(hakemus2.getTila()));
        assertTrue(HakemuksenTila.VARASIJALTA_HYVAKSYTTY.equals(hakemus3.getTila()));
        assertTrue(HakemuksenTila.PERUUNTUNUT.equals(hakemus4.getTila()));
    }

    @Test
    public void eiPeruttuaKorkeampaaTaiSamaaHakutoivettaTest() {

        HenkiloWrapper henkiloWrapper = new HenkiloWrapper();

        HakemusWrapper hakemusWrapper1 = new HakemusWrapper();
        HakemusWrapper hakemusWrapper2 = new HakemusWrapper();
        HakemusWrapper hakemusWrapper3 = new HakemusWrapper();
        HakemusWrapper hakemusWrapper4 = new HakemusWrapper();
        Hakemus hakemus1 = new Hakemus();
        Hakemus hakemus2 = new Hakemus();
        Hakemus hakemus3 = new Hakemus();
        Hakemus hakemus4 = new Hakemus();
        hakemusWrapper1.setHakemus(hakemus1);
        hakemusWrapper2.setHakemus(hakemus2);
        hakemusWrapper3.setHakemus(hakemus3);
        hakemusWrapper4.setHakemus(hakemus4);

        henkiloWrapper.getHakemukset().add(hakemusWrapper1);
        henkiloWrapper.getHakemukset().add(hakemusWrapper2);
        henkiloWrapper.getHakemukset().add(hakemusWrapper3);
        henkiloWrapper.getHakemukset().add(hakemusWrapper4);
        hakemusWrapper1.setHenkilo(henkiloWrapper);
        hakemusWrapper2.setHenkilo(henkiloWrapper);
        hakemusWrapper3.setHenkilo(henkiloWrapper);
        hakemusWrapper4.setHenkilo(henkiloWrapper);


        hakemus1.setPrioriteetti(1);
        hakemus1.setTila(VARALLA);
        hakemus2.setPrioriteetti(2);
        hakemus2.setTila(PERUNUT);
        hakemus3.setPrioriteetti(3);
        hakemus3.setTila(VARALLA);
        hakemus4.setPrioriteetti(4);
        hakemus4.setTila(VARALLA);


        Assert.assertTrue(SijoitteleHakukohde.eiPeruttuaKorkeampaaTaiSamaaHakutoivetta(hakemusWrapper1));
        //ok koska kakkonen on sama instanssi kuin verrattava kohde
        Assert.assertTrue(SijoitteleHakukohde.eiPeruttuaKorkeampaaTaiSamaaHakutoivetta(hakemusWrapper2));
        Assert.assertFalse(SijoitteleHakukohde.eiPeruttuaKorkeampaaTaiSamaaHakutoivetta(hakemusWrapper3));
        Assert.assertFalse(SijoitteleHakukohde.eiPeruttuaKorkeampaaTaiSamaaHakutoivetta(hakemusWrapper4));
    }

    @Test
    public void arvontaEiJataTasapisteisiaVarasijoilleJosVarasijatayttoEiOleKaytossa() {
        Valintatapajono jono = new ValintatapajonoBuilder().withOid("jono").withTasasijasaanto(ARVONTA).
            withAloituspaikat(1).withPrioriteetti(1).build();
        Hakukohde hakukohde = new HakukohdeBuilder("hakukohde").withValintatapajono(jono).build();

        Hakemus hyvaksyttavaHakemus = new HakemusBuilder().withOid("hyvaksyttavaOid").withTila(VARALLA).withPrioriteetti(1).withJonosija(1).build();
        Hakemus hakemusJokaEiMahdu = new HakemusBuilder().withOid("hakemusJokaEiMahduOid").withTila(VARALLA).withPrioriteetti(1).withJonosija(1).build();
        hyvaksyttavaHakemus.setTasasijaJonosija(1);
        hakemusJokaEiMahdu.setTasasijaJonosija(2);
        jono.getHakemukset().addAll(Arrays.asList(hyvaksyttavaHakemus, hakemusJokaEiMahdu));

        jono.setSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa(false);
        jono.setEiVarasijatayttoa(false);
        SijoitteluAlgorithmUtil.sijoittele(Collections.singletonList(hakukohde), Collections.emptyList(), Collections.emptyMap());
        assertThat(hyvaksyttavaHakemus, hasTila(HYVAKSYTTY));
        assertThat(hakemusJokaEiMahdu, hasTila(VARALLA));

        jono.setSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa(false);
        jono.setEiVarasijatayttoa(true);
        SijoitteluAlgorithmUtil.sijoittele(Collections.singletonList(hakukohde), Collections.emptyList(), Collections.emptyMap());
        assertThat(hyvaksyttavaHakemus, hasTila(HYVAKSYTTY));
        assertThat(hakemusJokaEiMahdu, hasTila(PERUUNTUNUT));
    }
    @Test
    public void jonosijoitteluSaaOikeanMaaranLisapaikkojaWIP() {
        Hakijaryhma hakijaryhma = new Hakijaryhma();
        hakijaryhma.setOid(hakijaryhmaOid);
        hakijaryhma.setHakukohdeOid("hakukohdeOid");
        hakijaryhma.setKiintio(4);

        Valintatapajono valintatapajono = new Valintatapajono();
        valintatapajono.setOid("testijono");
        valintatapajono.setAloituspaikat(5);
        valintatapajono.setPrioriteetti(0);
        valintatapajono.setTasasijasaanto(Tasasijasaanto.YLITAYTTO);
        List<Hakemus> kaytettavatHakemukset = generateHakemukset(1, 0, null);
        kaytettavatHakemukset.addAll(generateHakemukset(2, 2, null));
        kaytettavatHakemukset.addAll(generateHakemukset(5, 6, hakijaryhma));
        kaytettavatHakemukset.addAll(generateHakemukset(6, 9, null));
        valintatapajono.setHakemukset(kaytettavatHakemukset);

        Hakukohde hakukohde = new Hakukohde();
        hakukohde.setOid("hakukohdeOid");
        hakukohde.setValintatapajonot(Collections.singletonList(valintatapajono));
        hakukohde.setHakijaryhmat(Collections.singletonList(hakijaryhma));

        SijoitteluajoWrapper ajoWrapper =
                SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(
                        new SijoitteluAjo(),
                        Collections.singletonList(hakukohde),
                        Collections.emptyList(),
                        Collections.emptyMap()
                );
        HakukohdeWrapper hakukohdeWrapper = ajoWrapper.getHakukohteet().get(0);

        hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().setJonosija(2);
        hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().setJonosija(2);
        hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().setJonosija(2);
        hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(4).getHakemus().setJonosija(3);
        hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(5).getHakemus().setJonosija(3);
        hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(6).getHakemus().setJonosija(3);
        hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(7).getHakemus().setJonosija(3);

        SijoitteleHakukohde.sijoitteleHakukohde(ajoWrapper, hakukohdeWrapper);

        Valintatapajono jono = new ValintatapajonoBuilder().withOid("jonoValintaryhmanLisapaikat").withTasasijasaanto(YLITAYTTO).
                withAloituspaikat(5).withPrioriteetti(1).build();
        Hakemus hyvaksyttyHakijaryhmasta1 = new HakemusBuilder().withOid("hyvaksyttavaOid").withTila(HYVAKSYTTY).withPrioriteetti(1).withJonosija(1).build();

        Hakemus hyvaksyttavaHakemus1 = new HakemusBuilder().withOid("hyvaksyttavaOid").withTila(VARALLA).withPrioriteetti(1).withJonosija(1).build();
        Hakemus hyvaksyttavaHakemus2 = new HakemusBuilder().withOid("hyvaksyttavaOid").withTila(VARALLA).withPrioriteetti(1).withJonosija(1).build();
        Hakemus epaselvaHakemus1 = new HakemusBuilder().withOid("epaselvaOid").withTila(VARALLA).withPrioriteetti(1).withJonosija(1).build();

        Assert.assertTrue(true);
    }

    @Test
    public void hakijaryhmanYlitayttotilanteessaHyvaksytaanParempiaHakijoitaLisapaikoilleKunLisapaikatKaytossa() {
        Hakijaryhma hakijaryhma = new Hakijaryhma();
        hakijaryhma.setOid(hakijaryhmaOid);
        hakijaryhma.setHakukohdeOid("hakukohdeOid");
        hakijaryhma.setKiintio(5);

        Valintatapajono valintatapajono = new Valintatapajono();
        valintatapajono.setOid("testijono A");
        valintatapajono.setAloituspaikat(5);
        valintatapajono.setPrioriteetti(0);
        valintatapajono.setTasasijasaanto(Tasasijasaanto.YLITAYTTO);
        List<Hakemus> kaytettavatHakemukset = new ArrayList<>();

        kaytettavatHakemukset.add(generateHakemus(0, 0, null));
        kaytettavatHakemukset.add(generateHakemus(1, 1, hakijaryhma));
        kaytettavatHakemukset.add(generateHakemus(2, 1, null));
        kaytettavatHakemukset.add(generateHakemus(234, 2, null));
        kaytettavatHakemukset.add(generateHakemus(235, 3, null));
        //kaytettavatHakemukset.add(generateHakemus(3, 2, hakijaryhma));
        kaytettavatHakemukset.add(generateHakemus(4, 2, hakijaryhma));
        kaytettavatHakemukset.add(generateHakemus(5, 3, null));
        kaytettavatHakemukset.add(generateHakemus(6, 3, null));
        kaytettavatHakemukset.add(generateHakemus(17, 4, null));
        kaytettavatHakemukset.add(generateHakemus(18, 5, null));

        Valintatapajono valintatapajono2 = new Valintatapajono();
        valintatapajono2.setOid("testijono B");
        valintatapajono2.setAloituspaikat(5);
        valintatapajono2.setPrioriteetti(1);
        valintatapajono2.setTasasijasaanto(Tasasijasaanto.YLITAYTTO);
        List<Hakemus> kaytettavatHakemukset2 = new ArrayList<>();

        Hakemus parempiHakemus1EiHakijarymaa = generateHakemus(8, 1, null);
        Hakemus parempiHakemus2EiHakijarymaa = generateHakemus(9, 1, null);
        kaytettavatHakemukset2.add(parempiHakemus1EiHakijarymaa);
        kaytettavatHakemukset2.add(parempiHakemus2EiHakijarymaa);
        kaytettavatHakemukset2.add(generateHakemus(10, 2, hakijaryhma));
        kaytettavatHakemukset2.add(generateHakemus(11, 3, null));
        kaytettavatHakemukset2.add(generateHakemus(12, 3, null));
        kaytettavatHakemukset2.add(generateHakemus(77, 4, null));
        kaytettavatHakemukset2.add(generateHakemus(13, 4, hakijaryhma));
        kaytettavatHakemukset2.add(generateHakemus(14, 4, hakijaryhma));
        kaytettavatHakemukset2.add(generateHakemus(15, 4, hakijaryhma));
        kaytettavatHakemukset2.add(generateHakemus(44, 4, hakijaryhma));
        kaytettavatHakemukset2.add(generateHakemus(45, 4, hakijaryhma));
        kaytettavatHakemukset2.add(generateHakemus(46, 4, hakijaryhma));
        kaytettavatHakemukset2.add(generateHakemus(23, 6, hakijaryhma));
        kaytettavatHakemukset2.add(generateHakemus(24, 7, hakijaryhma));

        valintatapajono.setHakemukset(kaytettavatHakemukset);

        valintatapajono2.setHakemukset(kaytettavatHakemukset2);

        Hakukohde hakukohde = new Hakukohde();
        hakukohde.setOid("hakukohdeOid");
        List<Valintatapajono> jonos = new ArrayList<>();
        jonos.add(valintatapajono);
        jonos.add(valintatapajono2);
        hakukohde.setValintatapajonot(jonos);
        hakukohde.setHakijaryhmat(Collections.singletonList(hakijaryhma));

        SijoitteluajoWrapper ajoWrapper =
                SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(
                        new SijoitteluAjo(),
                        Collections.singletonList(hakukohde),
                        Collections.emptyList(),
                        Collections.emptyMap()
                );
        HakukohdeWrapper hakukohdeWrapper = ajoWrapper.getHakukohteet().get(0);
        ajoWrapper.setLisapaikkaTapa(LisapaikkaTapa.TAPA1); //Olennainen säätö tämän testin kannalta

        Assert.assertThat(parempiHakemus1EiHakijarymaa, hasTila(VARALLA));
        Assert.assertThat(parempiHakemus2EiHakijarymaa, hasTila(VARALLA));

        SijoitteleHakukohde.sijoitteleHakukohde(ajoWrapper, hakukohdeWrapper);
        int hyvaksyttyjaJonossaB = (int) kaytettavatHakemukset2.stream().filter(hw -> hw.getTila().equals(HYVAKSYTTY)).count();
        int hakijaryhmastaHyvaksyttyjaJonossaB = (int) kaytettavatHakemukset2.stream().filter(hw -> !hw.getHyvaksyttyHakijaryhmista().isEmpty()).count();
        Assert.assertEquals(9, hyvaksyttyjaJonossaB);
        Assert.assertEquals(7, hakijaryhmastaHyvaksyttyjaJonossaB);
        Assert.assertThat(parempiHakemus1EiHakijarymaa, hasTila(HYVAKSYTTY));
        Assert.assertThat(parempiHakemus2EiHakijarymaa, hasTila(HYVAKSYTTY));


    }

}
