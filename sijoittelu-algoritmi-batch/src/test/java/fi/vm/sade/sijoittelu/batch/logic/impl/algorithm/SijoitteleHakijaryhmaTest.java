package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakijaryhmaWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakukohdeWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.*;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SijoitteleHakijaryhmaTest {
    private static final int KIINTIO = 5;
    private static final int N_OF_HAKEMUKSET_TO_GENERATE = 40;
    private final SijoitteluAjo ajo = new SijoitteluAjo();
    private SijoitteluajoWrapper ajoWrapper;
    private final Hakukohde hakukohde = new Hakukohde();
    private final Valintatapajono valintatapajono = new Valintatapajono();

    private final String hakijaryhmaOid = "hakijaryhmaOid";
    private final Hakijaryhma hakijaryhma = new Hakijaryhma();

    private HakukohdeWrapper hakukohdeWrapper;
    private HakijaryhmaWrapper hakijaryhmaWrapper;

    @Before
    public void populateTestObjects() {
        hakijaryhma.setKiintio(KIINTIO);
        hakijaryhma.setOid(hakijaryhmaOid);
        hakijaryhma.setHakukohdeOid("hakukohdeOid");

        valintatapajono.setAloituspaikat(17);
        valintatapajono.setPrioriteetti(0);
        valintatapajono.setTasasijasaanto(Tasasijasaanto.ARVONTA);
        valintatapajono.setHakemukset(generateHakemukset(N_OF_HAKEMUKSET_TO_GENERATE, hakijaryhma));

        hakukohde.setOid("hakukohdeOid");
        hakukohde.setValintatapajonot(Collections.singletonList(valintatapajono));
        hakukohde.setHakijaryhmat(Collections.singletonList(hakijaryhma));

        HakijaryhmaWrapper dummyRyhmaWrapper = new HakijaryhmaWrapper();
        HakukohdeWrapper dummyKohdeWrapper = new HakukohdeWrapper();
        dummyKohdeWrapper.setHakukohde(hakukohde);
        dummyRyhmaWrapper.setHakijaryhma(hakijaryhma);
        dummyRyhmaWrapper.setHakukohdeWrapper(dummyKohdeWrapper);

        ajoWrapper = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(ajo, Collections.singletonList(hakukohde), Collections.emptyList(), Collections.emptyMap());
        hakukohdeWrapper = ajoWrapper.getHakukohteet().get(0);
        hakijaryhmaWrapper = hakukohdeWrapper.getHakijaryhmaWrappers().get(0);
    }

    private static List<Hakemus> generateHakemukset(int nToGenerate, Hakijaryhma hakijaryhma) {
        List<Hakemus> results = new ArrayList<>(nToGenerate);
        for (int i = 0; i < nToGenerate; i++) {
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

    @Test
    public void hakijaryhmaVoidaanSijoitellaJosKaikkienVarallaolijoidenTilaaVoidaanVaihtaa() {
        Set<HakukohdeWrapper> muuttuneetHakukohteet = SijoitteleHakijaryhma.sijoitteleHakijaryhma(ajoWrapper, hakijaryhmaWrapper);
        Assert.assertThat(muuttuneetHakukohteet, Matchers.hasSize(0));

        List<HakemusWrapper> allHakemusWrappers = hakijaryhmaWrapper.getHenkiloWrappers().stream().flatMap(h -> h.getHakemukset().stream()).collect(Collectors.toList());
        List<HakemusWrapper> hyvaksytytHakemukset = allHakemusWrappers.stream().filter(h -> h.getHakemus().getTila().equals(HakemuksenTila.HYVAKSYTTY)).collect(Collectors.toList());
        List<HakemusWrapper> varallaolevatHakemukset = allHakemusWrappers.stream().filter(h -> h.getHakemus().getTila().equals(HakemuksenTila.VARALLA)).collect(Collectors.toList());

        Assert.assertThat(hyvaksytytHakemukset, Matchers.hasSize(KIINTIO));
        Assert.assertThat(varallaolevatHakemukset, Matchers.hasSize(N_OF_HAKEMUKSET_TO_GENERATE - KIINTIO));
    }
    @Test
    public void sijoitteluVarallaOlijoitaEiHyvaksytaKunSaannotEiVoimassa() {
        final LocalDateTime today = ajoWrapper.getToday();
        try {
            ajoWrapper.setVarasijaTayttoPaattyy(ajoWrapper.getToday().minusDays(1));
            Set<HakukohdeWrapper> muuttuneetHakukohteet = SijoitteleHakijaryhma.sijoitteleHakijaryhma(ajoWrapper, hakijaryhmaWrapper);
            Assert.assertThat(muuttuneetHakukohteet, Matchers.hasSize(0));

            List<HakemusWrapper> allHakemusWrappers = hakijaryhmaWrapper.getHenkiloWrappers().stream().flatMap(h -> h.getHakemukset().stream()).collect(Collectors.toList());
            List<HakemusWrapper> hyvaksytytHakemukset = allHakemusWrappers.stream().filter(h -> h.getHakemus().getTila().equals(HakemuksenTila.HYVAKSYTTY)).collect(Collectors.toList());
            List<HakemusWrapper> varallaolevatHakemukset = allHakemusWrappers.stream().filter(h -> h.getHakemus().getTila().equals(HakemuksenTila.VARALLA)).collect(Collectors.toList());

            Assert.assertThat(hyvaksytytHakemukset, Matchers.hasSize(0));
            Assert.assertThat(varallaolevatHakemukset, Matchers.hasSize(N_OF_HAKEMUKSET_TO_GENERATE));
        } finally {
            ajoWrapper.setVarasijaTayttoPaattyy(today);
        }
    }

    @Test(expected = IllegalStateException.class)
    public void hakijaryhmanSijoitteluKaatuuJosJonkunVarallaolijanTilaaEiVoidaVaihtaa() throws Exception {
        List<HakemusWrapper> hakemusWrappers = hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset();
        int i = 0;
        for (HakemusWrapper hakemusWrapper : hakemusWrappers) {
            if (++i % 3 == 0) {
                hakemusWrapper.setTilaVoidaanVaihtaa(false);
            }
        }

        SijoitteleHakijaryhma.sijoitteleHakijaryhma(ajoWrapper, hakijaryhmaWrapper);
    }

    @Test
    public void hakijaryhmaSijoitteluHyvaksyyVainKiintioonMahtuvatAlitayttojonosta() {
        Hakijaryhma hakijaryhma = new Hakijaryhma();
        hakijaryhma.setOid("hakijaryhmaOid");
        hakijaryhma.setHakukohdeOid("hakukohdeOid");
        hakijaryhma.setKiintio(3);

        Valintatapajono valintatapajono = new Valintatapajono();
        valintatapajono.setAloituspaikat(3);
        valintatapajono.setPrioriteetti(0);
        valintatapajono.setTasasijasaanto(Tasasijasaanto.ALITAYTTO);
        valintatapajono.setHakemukset(generateHakemukset(4, hakijaryhma));
        hakijaryhma.getHakemusOid().remove(3);

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
        HakijaryhmaWrapper hakijaryhmaWrapper = hakukohdeWrapper.getHakijaryhmaWrappers().get(0);

        hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().setJonosija(1);
        hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().setJonosija(1);
        hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().setJonosija(1);

        SijoitteleHakukohde.sijoitteleHakukohde(ajoWrapper, hakukohdeWrapper);
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().getTila());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains("hakijaryhmaOid"));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains("hakijaryhmaOid"));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains("hakijaryhmaOid"));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
    }

    @Test
    public void hakijaryhmaSijoitteluEiMerkitseHakijaryhmastaHyvaksytyiksiJononKauttaHyvaksyttyja() {
        Hakijaryhma hakijaryhma = new Hakijaryhma();
        hakijaryhma.setOid("hakijaryhmaOid");
        hakijaryhma.setHakukohdeOid("hakukohdeOid");
        hakijaryhma.setKiintio(1);

        Valintatapajono valintatapajono = new Valintatapajono();
        valintatapajono.setAloituspaikat(3);
        valintatapajono.setPrioriteetti(0);
        valintatapajono.setTasasijasaanto(Tasasijasaanto.ALITAYTTO);
        valintatapajono.setHakemukset(generateHakemukset(3, hakijaryhma));

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

        SijoitteleHakukohde.sijoitteleHakukohde(ajoWrapper, hakukohdeWrapper);
        SijoitteleHakukohde.sijoitteleHakukohde(ajoWrapper, hakukohdeWrapper);
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().getTila());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains("hakijaryhmaOid"));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
    }

    @Test
    public void hakijaryhmaSijoitteluNoudattaaJononAlitayttoaJosKaikkiKuuluvatHakijaryhmaan() {
        Hakijaryhma hakijaryhma = new Hakijaryhma();
        hakijaryhma.setOid("hakijaryhmaOid");
        hakijaryhma.setHakukohdeOid("hakukohdeOid");
        hakijaryhma.setKiintio(4);

        Valintatapajono valintatapajono = new Valintatapajono();
        valintatapajono.setAloituspaikat(3);
        valintatapajono.setPrioriteetti(0);
        valintatapajono.setTasasijasaanto(Tasasijasaanto.ALITAYTTO);
        valintatapajono.setHakemukset(generateHakemukset(4, hakijaryhma));

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
        HakijaryhmaWrapper hakijaryhmaWrapper = hakukohdeWrapper.getHakijaryhmaWrappers().get(0);

        hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().setJonosija(1);
        hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().setJonosija(1);
        hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().setJonosija(1);

        SijoitteleHakukohde.sijoitteleHakukohde(ajoWrapper, hakukohdeWrapper);
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().getTila());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains("hakijaryhmaOid"));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
    }

    @Test
    public void hakijaryhmaSijoitteluYlitayttaaJononJaKiintionJosYlitaytto() {
        Hakijaryhma hakijaryhma = new Hakijaryhma();
        hakijaryhma.setOid("hakijaryhmaOid");
        hakijaryhma.setHakukohdeOid("hakukohdeOid");
        hakijaryhma.setKiintio(3);

        Valintatapajono valintatapajono = new Valintatapajono();
        valintatapajono.setAloituspaikat(3);
        valintatapajono.setPrioriteetti(0);
        valintatapajono.setTasasijasaanto(Tasasijasaanto.YLITAYTTO);
        valintatapajono.setHakemukset(generateHakemukset(4, hakijaryhma));

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
        HakijaryhmaWrapper hakijaryhmaWrapper = hakukohdeWrapper.getHakijaryhmaWrappers().get(0);

        hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().setJonosija(1);
        hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().setJonosija(1);
        hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().setJonosija(1);

        SijoitteleHakukohde.sijoitteleHakukohde(ajoWrapper, hakukohdeWrapper);
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().getTila());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains("hakijaryhmaOid"));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains("hakijaryhmaOid"));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains("hakijaryhmaOid"));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains("hakijaryhmaOid"));
    }

    @Test
    public void hakijaryhmaSijoitteluTayttaaTasanJosArvonta() {
        Hakijaryhma hakijaryhma = new Hakijaryhma();
        hakijaryhma.setOid("hakijaryhmaOid");
        hakijaryhma.setHakukohdeOid("hakukohdeOid");
        hakijaryhma.setKiintio(3);

        Valintatapajono valintatapajono = new Valintatapajono();
        valintatapajono.setAloituspaikat(3);
        valintatapajono.setPrioriteetti(0);
        valintatapajono.setTasasijasaanto(Tasasijasaanto.ARVONTA);
        valintatapajono.setHakemukset(generateHakemukset(4, hakijaryhma));

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
        HakijaryhmaWrapper hakijaryhmaWrapper = hakukohdeWrapper.getHakijaryhmaWrappers().get(0);

        hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().setJonosija(1);
        hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().setJonosija(1);
        hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().setJonosija(1);

        SijoitteleHakukohde.sijoitteleHakukohde(ajoWrapper, hakukohdeWrapper);
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().getTila());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains("hakijaryhmaOid"));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains("hakijaryhmaOid"));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains("hakijaryhmaOid"));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
    }

    @Test
    public void hakijaryhmaSijoitteluHyvaksyyEnsinJonosijaSittenJonoprioriteettijarjestyksessa() {
        Hakijaryhma hakijaryhma = new Hakijaryhma();
        hakijaryhma.setOid("hakijaryhmaOid");
        hakijaryhma.setHakukohdeOid("hakukohdeOid");
        hakijaryhma.setKiintio(3);

        Valintatapajono valintatapajono1 = new Valintatapajono();
        valintatapajono1.setAloituspaikat(3);
        valintatapajono1.setPrioriteetti(0);
        valintatapajono1.setTasasijasaanto(Tasasijasaanto.ALITAYTTO);
        valintatapajono1.setHakemukset(new ArrayList<>(6));
        valintatapajono1.getHakemukset().add(generateHakemus(3, 0, null));
        valintatapajono1.getHakemukset().add(generateHakemus(4, 1, null));
        valintatapajono1.getHakemukset().add(generateHakemus(5, 2, null));
        valintatapajono1.getHakemukset().add(generateHakemus(0, 3, hakijaryhma));
        valintatapajono1.getHakemukset().add(generateHakemus(1, 4, hakijaryhma));
        valintatapajono1.getHakemukset().add(generateHakemus(2, 5, hakijaryhma));

        Valintatapajono valintatapajono2 = new Valintatapajono();
        valintatapajono2.setAloituspaikat(3);
        valintatapajono2.setPrioriteetti(1);
        valintatapajono2.setTasasijasaanto(Tasasijasaanto.ALITAYTTO);
        valintatapajono2.setHakemukset(new ArrayList<>(3));
        valintatapajono2.getHakemukset().add(generateHakemus(0, 0, hakijaryhma));
        valintatapajono2.getHakemukset().add(generateHakemus(1, 1, hakijaryhma));
        valintatapajono2.getHakemukset().add(generateHakemus(2, 2, hakijaryhma));

        Hakukohde hakukohde = new Hakukohde();
        hakukohde.setOid("hakukohdeOid");
        hakukohde.setValintatapajonot(new ArrayList<>());
        hakukohde.getValintatapajonot().add(valintatapajono1);
        hakukohde.getValintatapajonot().add(valintatapajono2);
        hakukohde.setHakijaryhmat(Collections.singletonList(hakijaryhma));

        SijoitteluajoWrapper ajoWrapper =
                SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(
                        new SijoitteluAjo(),
                        Collections.singletonList(hakukohde),
                        Collections.emptyList(),
                        Collections.emptyMap()
                );
        HakukohdeWrapper hakukohdeWrapper = ajoWrapper.getHakukohteet().get(0);
        HakijaryhmaWrapper hakijaryhmaWrapper = hakukohdeWrapper.getHakijaryhmaWrappers().get(0);

        SijoitteleHakukohde.sijoitteleHakukohde(ajoWrapper, hakukohdeWrapper);
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(4).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(5).getHakemus().getTila());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(0).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(1).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(2).getHakemus().getTila());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(0).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains("hakijaryhmaOid"));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(1).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains("hakijaryhmaOid"));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(2).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains("hakijaryhmaOid"));
    }

    @Test
    public void sijoitteleHakijaryhmaTayttaaKiintiotaKaikistaJonoista() {
        Hakijaryhma hakijaryhma = new Hakijaryhma();
        hakijaryhma.setOid("hakijaryhmaOid");
        hakijaryhma.setHakukohdeOid("hakukohdeOid");
        hakijaryhma.setKiintio(3);

        Valintatapajono valintatapajono1 = new Valintatapajono();
        valintatapajono1.setAloituspaikat(3);
        valintatapajono1.setPrioriteetti(0);
        valintatapajono1.setTasasijasaanto(Tasasijasaanto.ALITAYTTO);
        valintatapajono1.setHakemukset(new ArrayList<>(4));
        valintatapajono1.getHakemukset().add(generateHakemus(0, 0, hakijaryhma));
        valintatapajono1.getHakemukset().add(generateHakemus(1, 1, hakijaryhma));
        valintatapajono1.getHakemukset().add(generateHakemus(2, 1, hakijaryhma));
        valintatapajono1.getHakemukset().add(generateHakemus(3, 1, hakijaryhma));

        Valintatapajono valintatapajono2 = new Valintatapajono();
        valintatapajono2.setAloituspaikat(3);
        valintatapajono2.setPrioriteetti(1);
        valintatapajono2.setTasasijasaanto(Tasasijasaanto.ALITAYTTO);
        valintatapajono2.setHakemukset(new ArrayList<>(4));
        valintatapajono2.getHakemukset().add(generateHakemus(4, 0, hakijaryhma));
        valintatapajono2.getHakemukset().add(generateHakemus(5, 1, hakijaryhma));
        valintatapajono2.getHakemukset().add(generateHakemus(6, 1, hakijaryhma));
        valintatapajono2.getHakemukset().add(generateHakemus(7, 1, null));

        Hakukohde hakukohde = new Hakukohde();
        hakukohde.setOid("hakukohdeOid");
        hakukohde.setValintatapajonot(new ArrayList<>());
        hakukohde.getValintatapajonot().add(valintatapajono1);
        hakukohde.getValintatapajonot().add(valintatapajono2);
        hakukohde.setHakijaryhmat(Collections.singletonList(hakijaryhma));

        SijoitteluajoWrapper ajoWrapper =
                SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(
                        new SijoitteluAjo(),
                        Collections.singletonList(hakukohde),
                        Collections.emptyList(),
                        Collections.emptyMap()
                );
        HakukohdeWrapper hakukohdeWrapper = ajoWrapper.getHakukohteet().get(0);
        HakijaryhmaWrapper hakijaryhmaWrapper = hakukohdeWrapper.getHakijaryhmaWrappers().get(0);

        SijoitteleHakukohde.sijoitteleHakukohde(ajoWrapper, hakukohdeWrapper);
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().getTila());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains("hakijaryhmaOid"));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(0).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(1).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(2).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(3).getHakemus().getTila());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(0).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains("hakijaryhmaOid"));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(1).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains("hakijaryhmaOid"));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(2).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains("hakijaryhmaOid"));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(3).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
    }

    @Test
    public void sijoitteleHakijarymaHyvaksyyHakijanVainYhdestaJonosta() {
        Hakijaryhma hakijaryhma = new Hakijaryhma();
        hakijaryhma.setOid("hakijaryhmaOid");
        hakijaryhma.setHakukohdeOid("hakukohdeOid");
        hakijaryhma.setKiintio(2);

        Valintatapajono valintatapajono1 = new Valintatapajono();
        valintatapajono1.setAloituspaikat(2);
        valintatapajono1.setPrioriteetti(0);
        valintatapajono1.setTasasijasaanto(Tasasijasaanto.ALITAYTTO);
        valintatapajono1.setHakemukset(new ArrayList<>(4));
        valintatapajono1.getHakemukset().add(generateHakemus(0, 0, hakijaryhma));
        valintatapajono1.getHakemukset().add(generateHakemus(1, 1, hakijaryhma));
        valintatapajono1.getHakemukset().add(generateHakemus(2, 2, hakijaryhma));
        valintatapajono1.getHakemukset().add(generateHakemus(3, 3, hakijaryhma));

        Valintatapajono valintatapajono2 = new Valintatapajono();
        valintatapajono2.setAloituspaikat(2);
        valintatapajono2.setPrioriteetti(1);
        valintatapajono2.setTasasijasaanto(Tasasijasaanto.ALITAYTTO);
        valintatapajono2.setHakemukset(new ArrayList<>(4));
        valintatapajono2.getHakemukset().add(generateHakemus(0, 0, hakijaryhma));
        valintatapajono2.getHakemukset().add(generateHakemus(4, 1, hakijaryhma));
        valintatapajono2.getHakemukset().add(generateHakemus(5, 2, hakijaryhma));
        valintatapajono2.getHakemukset().add(generateHakemus(6, 3, null));

        Hakukohde hakukohde = new Hakukohde();
        hakukohde.setOid("hakukohdeOid");
        hakukohde.setValintatapajonot(new ArrayList<>());
        hakukohde.getValintatapajonot().add(valintatapajono1);
        hakukohde.getValintatapajonot().add(valintatapajono2);
        hakukohde.setHakijaryhmat(Collections.singletonList(hakijaryhma));

        SijoitteluajoWrapper ajoWrapper =
                SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(
                        new SijoitteluAjo(),
                        Collections.singletonList(hakukohde),
                        Collections.emptyList(),
                        Collections.emptyMap()
                );
        HakukohdeWrapper hakukohdeWrapper = ajoWrapper.getHakukohteet().get(0);
        HakijaryhmaWrapper hakijaryhmaWrapper = hakukohdeWrapper.getHakijaryhmaWrappers().get(0);

        SijoitteleHakukohde.sijoitteleHakukohde(ajoWrapper, hakukohdeWrapper);
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().getTila());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains("hakijaryhmaOid"));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains("hakijaryhmaOid"));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
        Assert.assertEquals(HakemuksenTila.PERUUNTUNUT, hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(0).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(1).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(2).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(3).getHakemus().getTila());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(0).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(1).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(2).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(3).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
    }

    @Test
    public void sijoitteleHakijaryhmaHyvaksyyHyvaksytynPeruuntuneenEnnenMuita() {
        Hakijaryhma hakijaryhma = new Hakijaryhma();
        hakijaryhma.setOid("hakijaryhmaOid");
        hakijaryhma.setHakukohdeOid("hakukohdeOid");
        hakijaryhma.setKiintio(3);

        Valintatapajono valintatapajono1 = new Valintatapajono();
        valintatapajono1.setAloituspaikat(2);
        valintatapajono1.setPrioriteetti(0);
        valintatapajono1.setTasasijasaanto(Tasasijasaanto.YLITAYTTO);
        valintatapajono1.setHakemukset(new ArrayList<>(2));
        valintatapajono1.getHakemukset().add(generateHakemus(0, 0, hakijaryhma));
        valintatapajono1.getHakemukset().add(generateHakemus(1, 1, hakijaryhma));

        Valintatapajono valintatapajono2 = new Valintatapajono();
        valintatapajono2.setOid("valintatapajonoOid");
        valintatapajono2.setAloituspaikat(1);
        valintatapajono2.setPrioriteetti(1);
        valintatapajono2.setTasasijasaanto(Tasasijasaanto.YLITAYTTO);
        valintatapajono2.setHakemukset(new ArrayList<>(3));
        valintatapajono2.getHakemukset().add(generateHakemus(2, 0, hakijaryhma));
        valintatapajono2.getHakemukset().add(generateHakemus(3, 1, hakijaryhma));
        Hakemus peruuntuneenaHyvaksytty = generateHakemus(4, 2, hakijaryhma);
        Valintatulos peruuntuneenaHyvaksytynValintatulos = new Valintatulos(
                "valintatapajonoOid",
                "hakemus4",
                "hakukohdeOid",
                "hakija4",
                "hakuOid",
                0
        );
        peruuntuneenaHyvaksytynValintatulos.setTila(ValintatuloksenTila.KESKEN, "");
        peruuntuneenaHyvaksytynValintatulos.setHyvaksyPeruuntunut(true, "");
        peruuntuneenaHyvaksytty.setTila(HakemuksenTila.PERUUNTUNUT);
        valintatapajono2.getHakemukset().add(peruuntuneenaHyvaksytty);

        Hakukohde hakukohde = new Hakukohde();
        hakukohde.setOid("hakukohdeOid");
        hakukohde.setValintatapajonot(new ArrayList<>());
        hakukohde.getValintatapajonot().add(valintatapajono1);
        hakukohde.getValintatapajonot().add(valintatapajono2);
        hakukohde.setHakijaryhmat(Collections.singletonList(hakijaryhma));

        SijoitteluajoWrapper ajoWrapper =
                SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(
                        new SijoitteluAjo(),
                        Collections.singletonList(hakukohde),
                        Collections.singletonList(peruuntuneenaHyvaksytynValintatulos),
                        Collections.emptyMap()
                );
        HakukohdeWrapper hakukohdeWrapper = ajoWrapper.getHakukohteet().get(0);
        HakijaryhmaWrapper hakijaryhmaWrapper = hakukohdeWrapper.getHakijaryhmaWrappers().get(0);

        SijoitteleHakukohde.sijoitteleHakukohde(ajoWrapper, hakukohdeWrapper);
        Assert.assertTrue(hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(2).getHyvaksyPeruuntunut());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getTila());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains("hakijaryhmaOid"));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains("hakijaryhmaOid"));
        Assert.assertEquals(HakemuksenTila.VARALLA, hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(0).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(1).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(2).getHakemus().getTila());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(0).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(1).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(2).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains("hakijaryhmaOid"));
    }

    @Test
    public void sijoitteleHakijaryhmaSiirtaaVaralleHakijaryhmaanKuulumattomiaKiintionTayttamiseksiYlitayttojonossa() {
        Hakijaryhma hakijaryhma = new Hakijaryhma();
        hakijaryhma.setOid("hakijaryhmaOid");
        hakijaryhma.setHakukohdeOid("hakukohdeOid");
        hakijaryhma.setKiintio(3);

        Valintatapajono valintatapajono1 = new Valintatapajono();
        valintatapajono1.setAloituspaikat(1);
        valintatapajono1.setPrioriteetti(0);
        valintatapajono1.setTasasijasaanto(Tasasijasaanto.ALITAYTTO);
        valintatapajono1.setHakemukset(new ArrayList<>(2));
        Hakemus h1 = generateHakemus(0, 0, null);
        h1.setTila(HakemuksenTila.HYVAKSYTTY);
        valintatapajono1.getHakemukset().add(h1);
        valintatapajono1.getHakemukset().add(generateHakemus(1, 1, hakijaryhma));

        Valintatapajono valintatapajono2 = new Valintatapajono();
        valintatapajono2.setAloituspaikat(1);
        valintatapajono2.setPrioriteetti(1);
        valintatapajono2.setTasasijasaanto(Tasasijasaanto.ARVONTA);
        valintatapajono2.setHakemukset(new ArrayList<>(2));
        Hakemus h2 = generateHakemus(2, 0, null);
        h2.setTila(HakemuksenTila.HYVAKSYTTY);
        valintatapajono2.getHakemukset().add(h2);
        valintatapajono2.getHakemukset().add(generateHakemus(3, 1, hakijaryhma));

        Valintatapajono valintatapajono3 = new Valintatapajono();
        valintatapajono3.setAloituspaikat(1);
        valintatapajono3.setPrioriteetti(2);
        valintatapajono3.setTasasijasaanto(Tasasijasaanto.YLITAYTTO);
        valintatapajono3.setHakemukset(new ArrayList<>(2));
        Hakemus h3 = generateHakemus(4, 0, null);
        h3.setTila(HakemuksenTila.HYVAKSYTTY);
        valintatapajono3.getHakemukset().add(h3);
        valintatapajono3.getHakemukset().add(generateHakemus(5, 1, hakijaryhma));

        Hakukohde hakukohde = new Hakukohde();
        hakukohde.setOid("hakukohdeOid");
        hakukohde.setValintatapajonot(new ArrayList<>());
        hakukohde.getValintatapajonot().add(valintatapajono1);
        hakukohde.getValintatapajonot().add(valintatapajono2);
        hakukohde.getValintatapajonot().add(valintatapajono3);
        hakukohde.setHakijaryhmat(Collections.singletonList(hakijaryhma));

        SijoitteluajoWrapper ajoWrapper =
                SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(
                        new SijoitteluAjo(),
                        Collections.singletonList(hakukohde),
                        Collections.emptyList(),
                        Collections.emptyMap()
                );
        HakukohdeWrapper hakukohdeWrapper = ajoWrapper.getHakukohteet().get(0);
        HakijaryhmaWrapper hakijaryhmaWrapper = hakukohdeWrapper.getHakijaryhmaWrappers().get(0);

        SijoitteleHakukohde.sijoitteleHakukohde(ajoWrapper, hakukohdeWrapper);
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getTila());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(0).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(1).getHakemus().getTila());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(0).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(1).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakukohdeWrapper.getValintatapajonot().get(2).getHakemukset().get(0).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(2).getHakemukset().get(1).getHakemus().getTila());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(2).getHakemukset().get(0).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(2).getHakemukset().get(1).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains("hakijaryhmaOid"));
    }
}
