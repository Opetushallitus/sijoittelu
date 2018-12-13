package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.hakukohteet;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluajoWrapperFactory;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.SijoitteluAlgorithmUtil;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakukohdeWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static fi.vm.sade.sijoittelu.SijoitteluMatchers.hasTila;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.HYVAKSYTTY;
import static fi.vm.sade.sijoittelu.domain.HakemuksenTila.VARALLA;

public class SijoitteleHakukohdeLisapaikatTest {
    private final String hakijaryhmaOid = "hakijaryhmaOid";
    private Hakijaryhma hakijaryhma;
    private Valintatapajono valintatapajono;
    private Valintatapajono valintatapajono2;
    private List<Hakemus> kaytettavatHakemukset;
    private List<Hakemus> hakemuksetHakijaryhmaYlitayttyvassaJonossa;

    private Hakemus parempiHakemus1EiHakijarymaa;
    private Hakemus parempiHakemus2EiHakijarymaa;

    private Hakemus heikompiHakemus1EiHakijarymaa;
    private Hakemus heikompiHakemus2EiHakijarymaa;


    private Hakukohde hakukohde;

    private SijoitteluajoWrapper ajoWrapper;

    @Before
    public void valmisteleSijoitteluajoWrapper() {
        hakijaryhma = new Hakijaryhma();
        hakijaryhma.setOid(hakijaryhmaOid);
        hakijaryhma.setHakukohdeOid("hakukohdeOid");
        hakijaryhma.setKiintio(5);

        valintatapajono = new Valintatapajono();
        valintatapajono.setOid("testijono A");
        valintatapajono.setAloituspaikat(5);
        valintatapajono.setPrioriteetti(0);
        valintatapajono.setTasasijasaanto(Tasasijasaanto.YLITAYTTO);

        kaytettavatHakemukset = new ArrayList<>();
        kaytettavatHakemukset.add(SijoitteluAlgorithmUtil.generateHakemus(0, 0, null));
        kaytettavatHakemukset.add(SijoitteluAlgorithmUtil.generateHakemus(1, 1, hakijaryhma));
        kaytettavatHakemukset.add(SijoitteluAlgorithmUtil.generateHakemus(2, 1, null));
        kaytettavatHakemukset.add(SijoitteluAlgorithmUtil.generateHakemus(234, 2, null));
        kaytettavatHakemukset.add(SijoitteluAlgorithmUtil.generateHakemus(235, 3, null));
        kaytettavatHakemukset.add(SijoitteluAlgorithmUtil.generateHakemus(4, 2, hakijaryhma));
        kaytettavatHakemukset.add(SijoitteluAlgorithmUtil.generateHakemus(5, 3, null));
        kaytettavatHakemukset.add(SijoitteluAlgorithmUtil.generateHakemus(6, 3, null));
        kaytettavatHakemukset.add(SijoitteluAlgorithmUtil.generateHakemus(17, 4, null));
        kaytettavatHakemukset.add(SijoitteluAlgorithmUtil.generateHakemus(18, 5, null));

        valintatapajono2 = new Valintatapajono();
        valintatapajono2.setOid("testijono B");
        valintatapajono2.setAloituspaikat(5);
        valintatapajono2.setPrioriteetti(1);
        valintatapajono2.setTasasijasaanto(Tasasijasaanto.YLITAYTTO);

        hakemuksetHakijaryhmaYlitayttyvassaJonossa = new ArrayList<>();
        parempiHakemus1EiHakijarymaa = SijoitteluAlgorithmUtil.generateHakemus(8, 1, null);
        parempiHakemus2EiHakijarymaa = SijoitteluAlgorithmUtil.generateHakemus(9, 1, null);
        hakemuksetHakijaryhmaYlitayttyvassaJonossa.add(parempiHakemus1EiHakijarymaa);
        hakemuksetHakijaryhmaYlitayttyvassaJonossa.add(parempiHakemus2EiHakijarymaa);
        hakemuksetHakijaryhmaYlitayttyvassaJonossa.add(SijoitteluAlgorithmUtil.generateHakemus(10, 2, hakijaryhma));
        hakemuksetHakijaryhmaYlitayttyvassaJonossa.add(SijoitteluAlgorithmUtil.generateHakemus(11, 3, null));
        hakemuksetHakijaryhmaYlitayttyvassaJonossa.add(SijoitteluAlgorithmUtil.generateHakemus(12, 3, null));
        hakemuksetHakijaryhmaYlitayttyvassaJonossa.add(SijoitteluAlgorithmUtil.generateHakemus(77, 4, null));
        hakemuksetHakijaryhmaYlitayttyvassaJonossa.add(SijoitteluAlgorithmUtil.generateHakemus(13, 4, hakijaryhma));
        hakemuksetHakijaryhmaYlitayttyvassaJonossa.add(SijoitteluAlgorithmUtil.generateHakemus(14, 4, hakijaryhma));
        hakemuksetHakijaryhmaYlitayttyvassaJonossa.add(SijoitteluAlgorithmUtil.generateHakemus(15, 4, hakijaryhma));
        hakemuksetHakijaryhmaYlitayttyvassaJonossa.add(SijoitteluAlgorithmUtil.generateHakemus(44, 4, hakijaryhma));
        hakemuksetHakijaryhmaYlitayttyvassaJonossa.add(SijoitteluAlgorithmUtil.generateHakemus(45, 4, hakijaryhma));
        hakemuksetHakijaryhmaYlitayttyvassaJonossa.add(SijoitteluAlgorithmUtil.generateHakemus(46, 4, hakijaryhma));
        hakemuksetHakijaryhmaYlitayttyvassaJonossa.add(SijoitteluAlgorithmUtil.generateHakemus(23, 6, hakijaryhma));
        hakemuksetHakijaryhmaYlitayttyvassaJonossa.add(SijoitteluAlgorithmUtil.generateHakemus(24, 7, hakijaryhma));

        valintatapajono.setHakemukset(kaytettavatHakemukset);

        valintatapajono2.setHakemukset(hakemuksetHakijaryhmaYlitayttyvassaJonossa);

        hakukohde = new Hakukohde();
        hakukohde.setOid("hakukohdeOid");
        List<Valintatapajono> jonos = new ArrayList<>();
        jonos.add(valintatapajono);
        jonos.add(valintatapajono2);
        hakukohde.setValintatapajonot(jonos);
        hakukohde.setHakijaryhmat(Collections.singletonList(hakijaryhma));

        ajoWrapper = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(
                        new SijoitteluAjo(),
                        Collections.singletonList(hakukohde),
                        Collections.emptyList(),
                        Collections.emptyMap());

        heikompiHakemus1EiHakijarymaa = SijoitteluAlgorithmUtil.generateHakemus(8, 5, null);
        heikompiHakemus2EiHakijarymaa = SijoitteluAlgorithmUtil.generateHakemus(9, 6, null);


    }

    @Test
    public void hakijaryhmanYlitayttotilanteessaHyvaksytaanParempiaHakijoitaLisapaikoilleKunLisapaikkaTapa1Kaytossa() {

        HakukohdeWrapper hakukohdeWrapper = ajoWrapper.getHakukohteet().get(0);
        ajoWrapper.setLisapaikkaTapa(LisapaikkaTapa.TAPA1);

        Assert.assertThat(parempiHakemus1EiHakijarymaa, hasTila(VARALLA));
        Assert.assertThat(parempiHakemus2EiHakijarymaa, hasTila(VARALLA));

        SijoitteleHakukohde.sijoitteleHakukohde(ajoWrapper, hakukohdeWrapper);

        int hyvaksyttyjaJonossaB = (int) hakemuksetHakijaryhmaYlitayttyvassaJonossa.stream().filter(hw -> hw.getTila().equals(HYVAKSYTTY)).count();
        int hakijaryhmastaHyvaksyttyjaJonossaB = (int) hakemuksetHakijaryhmaYlitayttyvassaJonossa.stream().filter(hw -> !hw.getHyvaksyttyHakijaryhmista().isEmpty()).count();
        Assert.assertEquals(9, hyvaksyttyjaJonossaB);
        Assert.assertEquals(7, hakijaryhmastaHyvaksyttyjaJonossaB);
        Assert.assertThat(parempiHakemus1EiHakijarymaa, hasTila(HYVAKSYTTY));
        Assert.assertThat(parempiHakemus2EiHakijarymaa, hasTila(HYVAKSYTTY));
    }

    @Test
    public void hakijaryhmanYlitayttotilanteessaEiJaetaMahdollisiaLisapaikkojaHuonommilleHakemuksilleVaikkaNiitaOlisi() {
        hakemuksetHakijaryhmaYlitayttyvassaJonossa = new ArrayList<>();
        hakemuksetHakijaryhmaYlitayttyvassaJonossa.add(heikompiHakemus1EiHakijarymaa);
        hakemuksetHakijaryhmaYlitayttyvassaJonossa.add(heikompiHakemus2EiHakijarymaa);
        hakemuksetHakijaryhmaYlitayttyvassaJonossa.add(SijoitteluAlgorithmUtil.generateHakemus(10, 2, hakijaryhma));
        hakemuksetHakijaryhmaYlitayttyvassaJonossa.add(SijoitteluAlgorithmUtil.generateHakemus(11, 10, null));
        hakemuksetHakijaryhmaYlitayttyvassaJonossa.add(SijoitteluAlgorithmUtil.generateHakemus(12, 8, null));
        hakemuksetHakijaryhmaYlitayttyvassaJonossa.add(SijoitteluAlgorithmUtil.generateHakemus(77, 7, null));
        hakemuksetHakijaryhmaYlitayttyvassaJonossa.add(SijoitteluAlgorithmUtil.generateHakemus(13, 4, hakijaryhma));
        hakemuksetHakijaryhmaYlitayttyvassaJonossa.add(SijoitteluAlgorithmUtil.generateHakemus(14, 4, hakijaryhma));
        hakemuksetHakijaryhmaYlitayttyvassaJonossa.add(SijoitteluAlgorithmUtil.generateHakemus(15, 4, hakijaryhma));
        hakemuksetHakijaryhmaYlitayttyvassaJonossa.add(SijoitteluAlgorithmUtil.generateHakemus(44, 4, hakijaryhma));
        hakemuksetHakijaryhmaYlitayttyvassaJonossa.add(SijoitteluAlgorithmUtil.generateHakemus(45, 4, hakijaryhma));
        hakemuksetHakijaryhmaYlitayttyvassaJonossa.add(SijoitteluAlgorithmUtil.generateHakemus(46, 4, hakijaryhma));
        hakemuksetHakijaryhmaYlitayttyvassaJonossa.add(SijoitteluAlgorithmUtil.generateHakemus(23, 6, hakijaryhma));
        hakemuksetHakijaryhmaYlitayttyvassaJonossa.add(SijoitteluAlgorithmUtil.generateHakemus(24, 7, hakijaryhma));

        valintatapajono.setHakemukset(kaytettavatHakemukset);

        valintatapajono2.setHakemukset(hakemuksetHakijaryhmaYlitayttyvassaJonossa);

        hakukohde = new Hakukohde();
        hakukohde.setOid("hakukohdeOid");
        List<Valintatapajono> jonos = new ArrayList<>();
        jonos.add(valintatapajono);
        jonos.add(valintatapajono2);
        hakukohde.setValintatapajonot(jonos);
        hakukohde.setHakijaryhmat(Collections.singletonList(hakijaryhma));

        SijoitteluajoWrapper aw = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(
                new SijoitteluAjo(),
                Collections.singletonList(hakukohde),
                Collections.emptyList(),
                Collections.emptyMap());

        HakukohdeWrapper hakukohdeWrapper = aw.getHakukohteet().get(0);
        aw.setLisapaikkaTapa(LisapaikkaTapa.TAPA1);
        SijoitteleHakukohde.sijoitteleHakukohde(aw, hakukohdeWrapper);

        int hyvaksyttyjaJonossaB = (int) hakemuksetHakijaryhmaYlitayttyvassaJonossa.stream().filter(hw -> hw.getTila().equals(HYVAKSYTTY)).count();
        int hakijaryhmastaHyvaksyttyjaJonossaB = (int) hakemuksetHakijaryhmaYlitayttyvassaJonossa.stream().filter(hw -> !hw.getHyvaksyttyHakijaryhmista().isEmpty()).count();

        Assert.assertEquals(7, hyvaksyttyjaJonossaB);
        Assert.assertEquals(7, hakijaryhmastaHyvaksyttyjaJonossaB);

        Assert.assertThat(heikompiHakemus1EiHakijarymaa, hasTila(VARALLA));
        Assert.assertThat(heikompiHakemus2EiHakijarymaa, hasTila(VARALLA));

    }

    @Test
    public void hakijaryhmanYlitayttotilanteessaEiAnnetaLisapaikkojaKunLisapaikatEivatKaytossa() {
        HakukohdeWrapper hakukohdeWrapper = ajoWrapper.getHakukohteet().get(0);
        //ajoWrapper.setLisapaikkaTapa(LisapaikkaTapa.EI_KAYTOSSA); voitaisiin asettaa tämä, mutta on jo defaulttina EI_KAYTOSSA

        Assert.assertThat(parempiHakemus1EiHakijarymaa, hasTila(VARALLA));
        Assert.assertThat(parempiHakemus2EiHakijarymaa, hasTila(VARALLA));

        SijoitteleHakukohde.sijoitteleHakukohde(ajoWrapper, hakukohdeWrapper);

        int hyvaksyttyjaJonossaB = (int) hakemuksetHakijaryhmaYlitayttyvassaJonossa.stream().filter(hw -> hw.getTila().equals(HYVAKSYTTY)).count();
        int hakijaryhmastaHyvaksyttyjaJonossaB = (int) hakemuksetHakijaryhmaYlitayttyvassaJonossa.stream().filter(hw -> !hw.getHyvaksyttyHakijaryhmista().isEmpty()).count();
        Assert.assertEquals(7, hyvaksyttyjaJonossaB);
        Assert.assertEquals(7, hakijaryhmastaHyvaksyttyjaJonossaB);
        Assert.assertThat(parempiHakemus1EiHakijarymaa, hasTila(VARALLA));
        Assert.assertThat(parempiHakemus2EiHakijarymaa, hasTila(VARALLA));
    }
}