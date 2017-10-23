package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.hakukohteet;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluajoWrapperFactory;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoittelunTila;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.comparator.HakemusWrapperComparator;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.helper.HakuBuilder;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.SijoitteluAlgorithmUtil;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakijaryhmaWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakukohdeWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.ValintatapajonoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakijaryhma;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.Tasasijasaanto;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
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
        hakijaryhma.setOid(hakijaryhmaOid);
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

        hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().setJonosija(1);
        hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().setJonosija(1);
        hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().setJonosija(1);

        SijoitteleHakukohde.sijoitteleHakukohde(ajoWrapper, hakukohdeWrapper);
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().getTila());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains(hakijaryhmaOid));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains(hakijaryhmaOid));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains(hakijaryhmaOid));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
    }

    @Test
    public void hakijaryhmaSijoitteluEiMerkitseHakijaryhmastaHyvaksytyiksiJononKauttaHyvaksyttyja() {
        Hakijaryhma hakijaryhma = new Hakijaryhma();
        hakijaryhma.setOid(hakijaryhmaOid);
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
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains(hakijaryhmaOid));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
    }

    @Test
    public void hakijaryhmaSijoitteluNoudattaaJononAlitayttoaJosKaikkiKuuluvatHakijaryhmaan() {
        Hakijaryhma hakijaryhma = new Hakijaryhma();
        hakijaryhma.setOid(hakijaryhmaOid);
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

        hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().setJonosija(1);
        hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().setJonosija(1);
        hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().setJonosija(1);

        SijoitteleHakukohde.sijoitteleHakukohde(ajoWrapper, hakukohdeWrapper);
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().getTila());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains(hakijaryhmaOid));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
    }

    @Test
    public void hakijaryhmaSijoitteluYlitayttaaJononJaKiintionJosYlitaytto() {
        Hakijaryhma hakijaryhma = new Hakijaryhma();
        hakijaryhma.setOid(hakijaryhmaOid);
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

        hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().setJonosija(1);
        hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().setJonosija(1);
        hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().setJonosija(1);

        SijoitteleHakukohde.sijoitteleHakukohde(ajoWrapper, hakukohdeWrapper);
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().getTila());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains(hakijaryhmaOid));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains(hakijaryhmaOid));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains(hakijaryhmaOid));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains(hakijaryhmaOid));
    }

    @Test
    public void hakijaryhmaSijoitteluTayttaaTasanJosArvonta() {
        Hakijaryhma hakijaryhma = new Hakijaryhma();
        hakijaryhma.setOid(hakijaryhmaOid);
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

        hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().setJonosija(1);
        hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().setJonosija(1);
        hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().setJonosija(1);

        SijoitteleHakukohde.sijoitteleHakukohde(ajoWrapper, hakukohdeWrapper);
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().getTila());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains(hakijaryhmaOid));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains(hakijaryhmaOid));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains(hakijaryhmaOid));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
    }

    @Test
    public void hakijaryhmaSijoitteluHyvaksyyEnsinJonosijaSittenJonoprioriteettijarjestyksessa() {
        Hakijaryhma hakijaryhma = new Hakijaryhma();
        hakijaryhma.setOid(hakijaryhmaOid);
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
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(0).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains(hakijaryhmaOid));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(1).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains(hakijaryhmaOid));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(2).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains(hakijaryhmaOid));
    }

    @Test
    public void sijoitteleHakijaryhmaTayttaaKiintiotaKaikistaJonoista() {
        Hakijaryhma hakijaryhma = new Hakijaryhma();
        hakijaryhma.setOid(hakijaryhmaOid);
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

        SijoitteleHakukohde.sijoitteleHakukohde(ajoWrapper, hakukohdeWrapper);
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().getTila());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains(hakijaryhmaOid));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(0).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(1).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(2).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(3).getHakemus().getTila());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(0).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains(hakijaryhmaOid));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(1).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains(hakijaryhmaOid));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(2).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains(hakijaryhmaOid));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(3).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
    }

    @Test
    public void sijoitteleHakijarymaHyvaksyyHakijanVainYhdestaJonosta() {
        Hakijaryhma hakijaryhma = new Hakijaryhma();
        hakijaryhma.setOid(hakijaryhmaOid);
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

        SijoitteleHakukohde.sijoitteleHakukohde(ajoWrapper, hakukohdeWrapper);
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().getTila());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains(hakijaryhmaOid));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains(hakijaryhmaOid));
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
        hakijaryhma.setOid(hakijaryhmaOid);
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

        SijoitteleHakukohde.sijoitteleHakukohde(ajoWrapper, hakukohdeWrapper);
        Assert.assertTrue(hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(2).getHyvaksyPeruuntunut());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getTila());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains(hakijaryhmaOid));
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains(hakijaryhmaOid));
        Assert.assertEquals(HakemuksenTila.VARALLA, hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(0).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(1).getHakemus().getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(2).getHakemus().getTila());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(0).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(1).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(1).getHakemukset().get(2).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains(hakijaryhmaOid));
    }

    @Test
    public void sijoitteleHakijaryhmaSiirtaaVaralleHakijaryhmaanKuulumattomiaKiintionTayttamiseksiYlitayttojonossa() {
        Hakijaryhma hakijaryhma = new Hakijaryhma();
        hakijaryhma.setOid(hakijaryhmaOid);
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
        Assert.assertThat(hakukohdeWrapper.getValintatapajonot().get(2).getHakemukset().get(1).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains(hakijaryhmaOid));
    }

    @Test
    public void bug1468() {
        hakijaryhma.setOid("1.1.1.1");
        hakijaryhma.setPrioriteetti(1);
        hakijaryhma.setValintatapajonoOid(valintatapajono.getOid());
        Hakijaryhma toinenHakijaryhma = new Hakijaryhma();
        toinenHakijaryhma.setOid("2.2.2.2");
        toinenHakijaryhma.setPrioriteetti(2);
        toinenHakijaryhma.setValintatapajonoOid(valintatapajono.getOid());
        hakukohde.setHakijaryhmat(Arrays.asList(hakijaryhma, toinenHakijaryhma));

        valintatapajono.setOid("koejono-oid");
        valintatapajono.setAloituspaikat(5);
        hakijaryhma.setKiintio(3);
        toinenHakijaryhma.setKiintio(3);
        valintatapajono.setTasasijasaanto(Tasasijasaanto.YLITAYTTO);
        valintatapajono.getHakemukset().clear();

        for (int i = 0; i < 10; i++) {
            valintatapajono.getHakemukset().add(generateHakemus(i, i, hakijaryhma));
            valintatapajono.getHakemukset().add(generateHakemus(100 + i, 100 + i, toinenHakijaryhma));
        }

        Hakukohde toinenHakukohdeMuuttumisenTriggeroimiseksi = new Hakukohde();
        hakukohde.setOid("1hakukohdeOid");
        toinenHakukohdeMuuttumisenTriggeroimiseksi.setOid("2hakukohdeOid"); // this needs to be processed second to trigger the bug
        Valintatapajono toisenKohteenJono = new Valintatapajono();
        toisenKohteenJono.setOid("toisenKohteenJonoOid");
        toisenKohteenJono.setAloituspaikat(1);
        toisenKohteenJono.setPrioriteetti(1);
        toisenKohteenJono.setTasasijasaanto(Tasasijasaanto.YLITAYTTO);
        toisenKohteenJono.getHakemukset().add(generateHakemus(0, 1, null));
        toinenHakukohdeMuuttumisenTriggeroimiseksi.setValintatapajonot(Collections.singletonList(toisenKohteenJono));

        Hakemus hakemusWithTwoHakutoives = valintatapajono.getHakemukset().get(0);
        toisenKohteenJono.getHakemukset().get(0).setHakemusOid(hakemusWithTwoHakutoives.getHakemusOid());
        toisenKohteenJono.getHakemukset().get(0).setPrioriteetti(1);
        hakemusWithTwoHakutoives.setPrioriteetti(2);

        SijoittelunTila sijoittelunTila = SijoitteluAlgorithmUtil.sijoittele(
            Arrays.asList(hakukohde, toinenHakukohdeMuuttumisenTriggeroimiseksi), Collections.emptyList(), Collections.emptyMap());

        ajoWrapper = sijoittelunTila.sijoitteluAjo;

        List<HakemusWrapper> toisenHakukohteenHakemukset = ajoWrapper.getHakukohteet().get(1).getValintatapajonot()
            .get(0).getHakemukset().stream().sorted(new HakemusWrapperComparator()).collect(Collectors.toList());
        Assert.assertThat(toisenHakukohteenHakemukset, Matchers.hasSize(1));
        Assert.assertThat(toisenHakukohteenHakemukset.get(0).getHakemus().getTila(), Matchers.equalTo(HakemuksenTila.HYVAKSYTTY));
        Assert.assertThat(toisenHakukohteenHakemukset.get(0).getHakemus().getHakemusOid(), Matchers.equalTo(hakemusWithTwoHakutoives.getHakemusOid()));

        hakukohdeWrapper = ajoWrapper.getHakukohteet().get(0);
        List<HakemusWrapper> jononHakemukset = hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().stream().sorted(new HakemusWrapperComparator()).collect(Collectors.toList());

        // ryhmästä 1.1.1.1 tulee tulla hyväksytyiksi kolme hakemusta, jotka ovat jonosijajärjestyksessä ensimmäisen, peruuntuneen jälkeen
        Assert.assertThat(jononHakemukset.get(0).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
        Assert.assertThat(jononHakemukset.get(0).getHakemus().getTila(), Matchers.equalTo(HakemuksenTila.PERUUNTUNUT));
        Assert.assertThat(jononHakemukset.get(0).getHakemus().getHakemusOid(), Matchers.equalTo(hakemusWithTwoHakutoives.getHakemusOid()));
        Assert.assertThat(jononHakemukset.get(1).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains("1.1.1.1"));
        Assert.assertThat(jononHakemukset.get(2).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains("1.1.1.1"));
        Assert.assertThat(jononHakemukset.get(3).getHakemus().getTila(), Matchers.equalTo(HakemuksenTila.HYVAKSYTTY));
        Assert.assertThat(jononHakemukset.get(3).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains("1.1.1.1"));

        // ryhmästä 2.2.2.2 tulee tulla hyväksytyiksi kaksi hakemusta, joilla on ryhmän 2.2.2.2 parhaat jonosijat
        Assert.assertThat(jononHakemukset.get(10).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains("2.2.2.2"));
        Assert.assertThat(jononHakemukset.get(10).getHakemus().getTila(), Matchers.equalTo(HakemuksenTila.HYVAKSYTTY));
        Assert.assertThat(jononHakemukset.get(11).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.contains("2.2.2.2"));
        Assert.assertThat(jononHakemukset.get(11).getHakemus().getTila(), Matchers.equalTo(HakemuksenTila.HYVAKSYTTY));

        Assert.assertThat(jononHakemukset.get(12).getHakemus().getTila(), Matchers.equalTo(HakemuksenTila.VARALLA));
        Assert.assertThat(jononHakemukset.get(12).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
        Assert.assertThat(jononHakemukset.get(12).getHakemus().getHakemusOid(), Matchers.equalTo("hakemus102"));

        for (int i = 4; i < 10; i++) {
            Assert.assertEquals("Hakemuksen " + i + " tila", HakemuksenTila.VARALLA, jononHakemukset.get(i).getHakemus().getTila());
            Assert.assertThat(jononHakemukset.get(i).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
        }
        for (int i = 12; i < 20; i++) {
            Assert.assertEquals("Hakemuksen " + i + " tila", HakemuksenTila.VARALLA, jononHakemukset.get(i).getHakemus().getTila());
            Assert.assertThat(jononHakemukset.get(i).getHakemus().getHyvaksyttyHakijaryhmista(), Matchers.empty());
        }
    }

    @Test
    public void josHakijaryhmanKiintioOnJonojenAloituspaikkojenSummaaSuurempiJonotTayttyvatRyhmaanKuuluvista() {
        Hakijaryhma hakijaryhma = new Hakijaryhma();
        hakijaryhma.setOid(hakijaryhmaOid);
        hakijaryhma.setHakukohdeOid("hakukohdeOid");
        hakijaryhma.setKiintio(6);

        Hakemus hakemus0jono1 = generateHakemus(0, 0, null);
        Hakemus hakemus1jono1 = generateHakemus(1, 1, hakijaryhma);
        Hakemus hakemus2jono1 = generateHakemus(2, 2, hakijaryhma);
        Hakemus hakemus3jono1 = generateHakemus(3, 3, null);
        Hakemus hakemus4jono1 = generateHakemus(4, 4, hakijaryhma);
        Hakemus hakemus5jono1 = generateHakemus(5, 5, null);
        Hakemus hakemus6jono1 = generateHakemus(6, 6, hakijaryhma);
        Hakemus hakemus7jono1 = generateHakemus(7, 7, hakijaryhma);
        Hakemus hakemus8jono1 = generateHakemus(8, 8, hakijaryhma);

        Hakemus hakemus0jono2 = generateHakemus(0, 0, null);
        Hakemus hakemus1jono2 = generateHakemus(1, 1, hakijaryhma);
        Hakemus hakemus2jono2 = generateHakemus(2, 2, hakijaryhma);
        Hakemus hakemus3jono2 = generateHakemus(3, 3, null);
        Hakemus hakemus4jono2 = generateHakemus(4, 4, hakijaryhma);
        Hakemus hakemus5jono2 = generateHakemus(5, 4, null);
        Hakemus hakemus6jono2 = generateHakemus(6, 4, hakijaryhma);
        hakemus4jono2.setTasasijaJonosija(1);
        hakemus5jono2.setTasasijaJonosija(2);
        hakemus6jono2.setTasasijaJonosija(3);
        Hakemus hakemus7jono2 = generateHakemus(7, 7, hakijaryhma);
        Hakemus hakemus8jono2 = generateHakemus(8, 8, hakijaryhma);

        Valintatapajono valintatapajono1 = new HakuBuilder.ValintatapajonoBuilder().
            withAloituspaikat(3).
            withPrioriteetti(0).
            withOid("valintatapajono1").
            withTasasijasaanto(Tasasijasaanto.YLITAYTTO).build();
        //valintatapajono1.setEiVarasijatayttoa(true);
        valintatapajono1.setKaikkiEhdonTayttavatHyvaksytaan(false);
        valintatapajono1.setHakemukset(Arrays.asList(hakemus0jono1, hakemus1jono1, hakemus2jono1, hakemus3jono1,
            hakemus4jono1, hakemus5jono1, hakemus6jono1, hakemus7jono1, hakemus8jono1));

        Valintatapajono valintatapajono2 = new HakuBuilder.ValintatapajonoBuilder().
            withAloituspaikat(2).
            withPrioriteetti(1).
            withOid("valintatapajono2").
            withTasasijasaanto(Tasasijasaanto.YLITAYTTO).build();
        //valintatapajono2.setEiVarasijatayttoa(true);
        valintatapajono2.setKaikkiEhdonTayttavatHyvaksytaan(false);
        valintatapajono2.setHakemukset((Arrays.asList(hakemus0jono2, hakemus1jono2, hakemus2jono2, hakemus3jono2,
            hakemus4jono2, hakemus5jono2, hakemus6jono2, hakemus7jono2, hakemus8jono2)));

        Hakukohde hakukohde = new Hakukohde();
        hakukohde.setOid("hakukohdeOid");
        hakukohde.setValintatapajonot(Arrays.asList(valintatapajono1, valintatapajono2));
        hakukohde.setHakijaryhmat(Collections.singletonList(hakijaryhma));

        SijoitteluAlgorithmUtil.sijoittele(Collections.singletonList(hakukohde), Collections.emptyList(), Collections.emptyMap());

        Assert.assertEquals(HakemuksenTila.VARALLA, hakemus0jono1.getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakemus1jono1.getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakemus2jono1.getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakemus3jono1.getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakemus4jono1.getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakemus5jono1.getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakemus6jono1.getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakemus7jono1.getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakemus8jono1.getTila());

        Assert.assertEquals(HakemuksenTila.VARALLA, hakemus0jono2.getTila());
        Assert.assertEquals(HakemuksenTila.PERUUNTUNUT, hakemus1jono2.getTila());
        Assert.assertEquals(HakemuksenTila.PERUUNTUNUT, hakemus2jono2.getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakemus3jono2.getTila());
        Assert.assertEquals(HakemuksenTila.PERUUNTUNUT, hakemus4jono2.getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakemus5jono2.getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakemus6jono2.getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakemus7jono2.getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, hakemus8jono2.getTila());
    }

    @Test
    public void hakijaryhmasijoitteluHyvaksyyKaikkiSuoravalintajonosta() {
        Hakemus hakemus0jono1 = valintatapajono.getHakemukset().get(0);
        Hakemus hakemus1jono1 = valintatapajono.getHakemukset().get(1);
        Hakemus hakemus2jono1 = valintatapajono.getHakemukset().get(2);

        valintatapajono.setKaikkiEhdonTayttavatHyvaksytaan(true);
        valintatapajono.setAloituspaikat(0);  // suoravalintajonoilla on 0 aloituspaikkaa valintaperusteissa

        Valintatapajono valintatapajono2 = new HakuBuilder.ValintatapajonoBuilder().
            withAloituspaikat(2).
            withPrioriteetti(1).
            withOid("valintatapajono2").
            withTasasijasaanto(Tasasijasaanto.YLITAYTTO).build();
        ValintatapajonoWrapper valintatapajono2Wrapper = new ValintatapajonoWrapper();
        valintatapajono2Wrapper.setValintatapajono(valintatapajono2);

        HakukohdeWrapper hakukohdeWrapper = ajoWrapper.getHakukohteet().get(0);
        hakukohdeWrapper.getValintatapajonot().add(valintatapajono2Wrapper);

        hakijaryhmaWrapper.setHakukohdeWrapper(hakukohdeWrapper);
        SijoitteleHakijaryhma.sijoitteleHakijaryhma(ajoWrapper, hakijaryhmaWrapper);

        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakemus0jono1.getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakemus1jono1.getTila());
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, hakemus2jono1.getTila());
    }

    /**
     * The buggy code relied on the iteration order of Map<Integer, List<HakemusWrapper>> keys, so it is not
     * easy to reproduce the bug in a foolproof way.
     */
    @Test
    public void hakijaryhmasijoitteluHyvaksyyEnsisijaisestiParhaallaJonosijallaOlevanHakemuksenJononPrioriteetistaRiippumatta() {
        hakijaryhma.setKiintio(1);
        valintatapajono.setPrioriteetti(500);
        hakukohde.setValintatapajonot(new ArrayList<>());
        List<Hakemus> hyvinMatalallaJonoissaOleviaHakemuksia = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            Valintatapajono jono = new HakuBuilder.ValintatapajonoBuilder().
                withAloituspaikat(2).
                withPrioriteetti(1 + i).
                withOid("valintatapajono" + i).
                withTasasijasaanto(Tasasijasaanto.YLITAYTTO).build();

            for (int h = 0; h  < 10; h++) {
                Hakemus hakemus = generateHakemus(999 + i + h, 100 + i + jono.hashCode(), hakijaryhma); // produce varying high jonosijas
                hyvinMatalallaJonoissaOleviaHakemuksia.add(hakemus);
                jono.getHakemukset().add(hakemus);
            }
            hakukohde.getValintatapajonot().add(jono);
        }
        hakukohde.getValintatapajonot().add(valintatapajono);

        ajoWrapper = SijoitteluajoWrapperFactory.createSijoitteluAjoWrapper(ajo, Collections.singletonList(hakukohde), Collections.emptyList(), Collections.emptyMap());
        hakukohdeWrapper = ajoWrapper.getHakukohteet().get(0);
        hakijaryhmaWrapper = hakukohdeWrapper.getHakijaryhmaWrappers().get(0);

        valintatapajono.getHakemukset().forEach(h -> h.setJonosija(1 + h.getJonosija()));
        SijoitteleHakijaryhma.sijoitteleHakijaryhma(ajoWrapper, hakijaryhmaWrapper);

        hyvinMatalallaJonoissaOleviaHakemuksia.forEach(h -> Assert.assertEquals(HakemuksenTila.VARALLA, h.getTila()));
        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, valintatapajono.getHakemukset().get(0).getTila());
        Assert.assertEquals(HakemuksenTila.VARALLA, valintatapajono.getHakemukset().get(1).getTila());
    }

    /**
     * This test does not really fail because of the bug, just produces ERROR output...
     */
    @Test
    public void hakijaryhmasijoitteluEiYlitaytaKiintiotaArvontajonossaVaikkaViimeinenHyvaksyttyOlisiTasasijalla() {
        for (int i = 0; i < 5; i++) {
            valintatapajono.getHakemukset().get(i).setJonosija(0);
            valintatapajono.getHakemukset().get(i).setTasasijaJonosija(i);
        }

        valintatapajono.setTasasijasaanto(Tasasijasaanto.ARVONTA);
        valintatapajono.setAloituspaikat(4);
        hakijaryhma.setKiintio(2);

        hakijaryhmaWrapper.setHakukohdeWrapper(hakukohdeWrapper);
        SijoitteleHakijaryhma.sijoitteleHakijaryhma(ajoWrapper, hakijaryhmaWrapper);

        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, valintatapajono.getHakemukset().get(0).getTila());
        Assert.assertThat(valintatapajono.getHakemukset().get(0).getHyvaksyttyHakijaryhmista(), Matchers.contains(hakijaryhmaOid));

        Assert.assertEquals(HakemuksenTila.HYVAKSYTTY, valintatapajono.getHakemukset().get(1).getTila());
        Assert.assertThat(valintatapajono.getHakemukset().get(1).getHyvaksyttyHakijaryhmista(), Matchers.contains(hakijaryhmaOid));

        Assert.assertEquals(HakemuksenTila.VARALLA, valintatapajono.getHakemukset().get(2).getTila());
        Assert.assertThat(valintatapajono.getHakemukset().get(2).getHyvaksyttyHakijaryhmista(), Matchers.empty());
    }
}
