package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakemusWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakijaryhmaWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.HakukohdeWrapper;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakijaryhma;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.Tasasijasaanto;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
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
        Hakemus h = new Hakemus();
        h.setJonosija(i);
        h.setPrioriteetti(0);
        h.setHakemusOid("hakemus" + i);
        h.setHakijaOid("hakija" + i);
        hakijaryhma.getHakemusOid().add(h.getHakemusOid());
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
        Assert.assertEquals("hakijaryhmaOid", hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getHakijaryhmaOid());
        Assert.assertEquals("hakijaryhmaOid", hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getHakijaryhmaOid());
        Assert.assertEquals("hakijaryhmaOid", hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().getHakijaryhmaOid());
        Assert.assertNull(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().getHakijaryhmaOid());
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
        Assert.assertEquals("hakijaryhmaOid", hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getHakijaryhmaOid());
        Assert.assertNull(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getHakijaryhmaOid());
        Assert.assertNull(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().getHakijaryhmaOid());
        Assert.assertNull(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().getHakijaryhmaOid());
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
        Assert.assertEquals("hakijaryhmaOid", hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getHakijaryhmaOid());
        Assert.assertEquals("hakijaryhmaOid", hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getHakijaryhmaOid());
        Assert.assertEquals("hakijaryhmaOid", hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().getHakijaryhmaOid());
        Assert.assertEquals("hakijaryhmaOid", hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().getHakijaryhmaOid());
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
        Assert.assertEquals("hakijaryhmaOid", hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(0).getHakemus().getHakijaryhmaOid());
        Assert.assertEquals("hakijaryhmaOid", hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(1).getHakemus().getHakijaryhmaOid());
        Assert.assertEquals("hakijaryhmaOid", hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(2).getHakemus().getHakijaryhmaOid());
        Assert.assertNull(hakukohdeWrapper.getValintatapajonot().get(0).getHakemukset().get(3).getHakemus().getHakijaryhmaOid());
    }
}
