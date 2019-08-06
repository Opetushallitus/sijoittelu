package fi.vm.sade.sijoittelu;

import static fi.vm.sade.valintalaskenta.domain.dto.valintakoe.Tasasijasaanto.ALITAYTTO;
import static fi.vm.sade.valintalaskenta.domain.dto.valintakoe.Tasasijasaanto.ARVONTA;
import static fi.vm.sade.valintalaskenta.domain.dto.valintakoe.Tasasijasaanto.YLITAYTTO;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.google.common.collect.Sets;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluConfiguration;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.HakukohdeItem;
import fi.vm.sade.sijoittelu.domain.IlmoittautumisTila;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.Tasasijasaanto;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.laskenta.external.resource.VirkailijaValintaTulosServiceResource;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriArvoDTO;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriDTO;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.sijoittelu.laskenta.service.business.ValintarekisteriService;
import fi.vm.sade.sijoittelu.laskenta.service.it.TarjontaIntegrationService;
import fi.vm.sade.sijoittelu.tulos.service.impl.converters.SijoitteluTulosConverter;
import fi.vm.sade.sijoittelu.tulos.service.impl.converters.SijoitteluTulosConverterImpl;
import fi.vm.sade.valintalaskenta.domain.dto.HakijaDTO;
import fi.vm.sade.valintalaskenta.domain.dto.HakukohdeDTO;
import fi.vm.sade.valintalaskenta.domain.dto.JarjestyskriteerituloksenTilaDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.ValintatietoValinnanvaiheDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.ValintatietoValintatapajonoDTO;
import io.reactivex.functions.Function3;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SijoitteluBusinessServiceValintarekisteriTest {

    private String hakuOid = "12345";
    private long sijoitteluajoId = 12345l;
    private String uusiHakukohdeOid = "112233";
    private List<String> valintarekisteriHakukohdeOidit = Arrays.asList("112244","112255");
    private List<String> hakukohdeOidit = Arrays.asList("112233", "112244","112255");

    private SijoitteluBusinessService service;
    private SijoitteluTulosConverter sijoitteluTulosConverter;
    private TarjontaIntegrationService tarjontaIntegrationService;
    private VirkailijaValintaTulosServiceResource valintaTulosServiceResource;
    private ValintarekisteriService valintarekisteriService;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        sijoitteluTulosConverter = new SijoitteluTulosConverterImpl();
        tarjontaIntegrationService = mock(TarjontaIntegrationService.class);
        valintaTulosServiceResource = mock(VirkailijaValintaTulosServiceResource.class);
        valintarekisteriService = mock(ValintarekisteriService.class);

        service = new SijoitteluBusinessService(sijoitteluTulosConverter, tarjontaIntegrationService, valintaTulosServiceResource, valintarekisteriService, new SijoitteluConfiguration());

        when(tarjontaIntegrationService.getHaunParametrit(hakuOid)).thenReturn(haunParametrit());
        when(tarjontaIntegrationService.getHakuByHakuOid(hakuOid)).thenReturn(tarjontaHaku());
    }

    private void setUpMocks1() {
        when(valintarekisteriService.getLatestSijoitteluajo(hakuOid)).thenReturn(valintarekisteriSijoitteluajo1());
        when(valintarekisteriService.getSijoitteluajonHakukohteet(sijoitteluajoId)).thenReturn(valintarekisteriHakukohteet1());
        when(valintarekisteriService.getValintatulokset(hakuOid)).thenReturn(valintarekisteriValintatulokset1());
    }

    private void setUpMocks2() {
        when(valintarekisteriService.getLatestSijoitteluajo(hakuOid)).thenReturn(valintarekisteriSijoitteluajo2());
        when(valintarekisteriService.getSijoitteluajonHakukohteet(sijoitteluajoId)).thenReturn(valintarekisteriHakukohteet2());
        when(valintarekisteriService.getValintatulokset(hakuOid)).thenReturn(valintarekisteriValintatulokset2());
    }

    private void verifyAndCaptureAndAssert(Function3<SijoitteluAjo, List<Hakukohde>, List<Valintatulos>, Boolean> assertFunction) {
        verify(valintarekisteriService).getLatestSijoitteluajo(hakuOid);
        verify(valintarekisteriService).getSijoitteluajonHakukohteet(sijoitteluajoId);
        verify(valintarekisteriService).getValintatulokset(hakuOid);

        ArgumentCaptor<SijoitteluAjo> sijoitteluajoCaptor = ArgumentCaptor.forClass(SijoitteluAjo.class);
        ArgumentCaptor<List<Hakukohde>> hakukohteetCaptor = ArgumentCaptor.forClass((Class)List.class);
        ArgumentCaptor<List<Valintatulos>> valintatuloksetCaptor = ArgumentCaptor.forClass((Class)List.class);

        verify(valintarekisteriService).tallennaSijoittelu(sijoitteluajoCaptor.capture(), hakukohteetCaptor.capture(), valintatuloksetCaptor.capture());

        try {
            assertFunction.apply(sijoitteluajoCaptor.getValue(), hakukohteetCaptor.getValue(), valintatuloksetCaptor.getValue());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void assertSijoitteluajo(SijoitteluAjo sijoitteluajo) {
        assertNotEquals(sijoitteluajoId, (long)sijoitteluajo.getSijoitteluajoId());
        assertEquals(hakukohdeOidit.size(), sijoitteluajo.getHakukohteet().size());
        assertTrue(hakukohdeOidit.containsAll(sijoitteluajo.getHakukohteet().stream().map(HakukohdeItem::getOid).collect(Collectors.toList())));
    }

    private void assertHakukohteet(long uusiSijoitteluajoId, List<Hakukohde> hakukohteet) {
        assertEquals(hakukohdeOidit.size(), hakukohteet.size());
        assertTrue(hakukohdeOidit.containsAll(hakukohteet.stream().map(Hakukohde::getOid).collect(Collectors.toList())));
        assertFalse(hakukohteet.stream().anyMatch(hk -> hk.getSijoitteluajoId() != uusiSijoitteluajoId));
    }

    private void assertHakemuksetKaksiJonoHyvaksyVarallaJaPeruAlempi(List<Hakukohde> hakukohteet) {
        List<Valintatapajono> valintatapajonot = hakukohteet.stream().filter(hk -> hk.getOid().equals(uusiHakukohdeOid)).findAny().get().getValintatapajonot();

        List<Hakemus> hakemuksetJono1 = valintatapajonot.stream().filter(jono -> "112233.111111".equals(jono.getOid())).findAny().get().getHakemukset();
        List<Hakemus> hakemuksetJono2 = valintatapajonot.stream().filter(jono -> "112233.222222".equals(jono.getOid())).findAny().get().getHakemukset();

        assertEquals(2, hakemuksetJono1.size());
        assertEquals(2, hakemuksetJono2.size());

        assertFalse(hakemuksetJono1.stream().anyMatch(t -> t.getTila() != HakemuksenTila.VARASIJALTA_HYVAKSYTTY));
        assertFalse(hakemuksetJono2.stream().anyMatch(t -> t.getTila() != HakemuksenTila.PERUUNTUNUT));
    }

    private void assertVastaanotetutValintatuloksetUudelleHakukohteelle(List<Valintatulos> valintatulokset) {
        assertFalse(valintatulokset.stream().anyMatch(v -> !v.getHakukohdeOid().equals(uusiHakukohdeOid)));
        assertFalse(valintatulokset.stream().anyMatch(v -> v.getTila() != ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI));
        assertEquals(4, valintatulokset.size());
    }

    private void assertHakemukset(List<Hakukohde> hakukohteet,
                                  HakemuksenTila uusiHakukohdeTila,
                                  HakemuksenTila muutHakukohteetTila,
                                  int expectedMuidenHakukohteidenHakemuksetCount) {
        List<Hakemus> uudenHakukohteenHakemukset = new ArrayList<>();
        List<Hakemus> muidenHakukohteidenHakemukset = new ArrayList<>();

        hakukohteet.forEach(hakukohde ->
            hakukohde.getValintatapajonot().forEach(jono -> {
                if(uusiHakukohdeOid.equals(hakukohde.getOid())) {
                    uudenHakukohteenHakemukset.addAll(jono.getHakemukset());
                } else {
                    muidenHakukohteidenHakemukset.addAll(jono.getHakemukset());
                }
            }));

        assertFalse(uudenHakukohteenHakemukset.stream().anyMatch(t -> t.getTila() != uusiHakukohdeTila));
        assertFalse(muidenHakukohteidenHakemukset.stream().anyMatch(t -> t.getTila() != muutHakukohteetTila));

        assertEquals(2, uudenHakukohteenHakemukset.size());
        assertEquals(expectedMuidenHakukohteidenHakemuksetCount, muidenHakukohteidenHakemukset.size());
    }

    private void assertHyvaksyttyVaralla(List<Hakukohde> hakukohteet, String hakukohdeOid, String jonoOid, int hyvaksytty, int varalla) {
        List<Valintatapajono> valintatapajonot = hakukohteet.stream().filter(hk -> hk.getOid().equals(hakukohdeOid)).findAny().get().getValintatapajonot();

        assertEquals(hyvaksytty, (int)valintatapajonot.stream().filter(jono -> jonoOid.equals(jono.getOid())).findAny().get().getHyvaksytty());
        assertEquals(varalla, (int)valintatapajonot.stream().filter(jono -> jonoOid.equals(jono.getOid())).findAny().get().getVaralla());
    }

    @Test
    public void testSijoitteleEiMuuttuneitaValinnantuloksia() {
        setUpMocks1();

        service.sijoittele(hakuDTO1(true), Collections.emptySet(), Sets.newHashSet("112233.111111", "112244.111111", "112255.111111"), System.currentTimeMillis());

        Function3<SijoitteluAjo, List<Hakukohde>, List<Valintatulos>, Boolean> assertFunction = (sijoitteluajo, hakukohteet, valintatulokset) -> {
            assertSijoitteluajo(sijoitteluajo);
            assertHakukohteet(sijoitteluajo.getSijoitteluajoId(), hakukohteet);
            assertHakemukset(hakukohteet, HakemuksenTila.HYVAKSYTTY, HakemuksenTila.VARASIJALTA_HYVAKSYTTY, 0);
            assertHyvaksyttyVaralla(hakukohteet, uusiHakukohdeOid, "112233.111111", 2, 0);

            assertEquals(0, valintatulokset.size());
            return true;
        };

        verifyAndCaptureAndAssert(assertFunction);
    }

    @Test
    public void testErillissijoitteleUusiHakukohde() {
        setUpMocks1();

        service.erillissijoittele(hakuDTO1(false));

        Function3<SijoitteluAjo, List<Hakukohde>, List<Valintatulos>, Boolean> assertFunction =
            (sijoitteluajo, hakukohteet, valintatulokset) -> {
                assertSijoitteluajo(sijoitteluajo);
                assertHakukohteet(sijoitteluajo.getSijoitteluajoId(), hakukohteet);
                assertHakemukset(hakukohteet, HakemuksenTila.HYVAKSYTTY, HakemuksenTila.VARALLA, 4);
                assertHyvaksyttyVaralla(hakukohteet, uusiHakukohdeOid, "112233.111111", 2, 0);
                //assertHyvaksyttyVaralla(hakukohteet, "112244", "112244.111111", 0, 2);

                assertEquals(0, valintatulokset.size());
                return true;
            };

        verifyAndCaptureAndAssert(assertFunction);
    }

    @Test
    public void testSijoitteleKaksiJonoaHyvaksyVarallaJaPeruAlempi() {
        setUpMocks2();

        service.sijoittele(hakuDTO2(true), Collections.emptySet(), Sets.newHashSet(
            "112233.111111", "112244.111111", "112255.111111", "112233.222222", "112244.222222", "112255.222222"), (long)123456789);

        Function3<SijoitteluAjo, List<Hakukohde>, List<Valintatulos>, Boolean> assertFunction = (sijoitteluajo, hakukohteet, valintatulokset) -> {
            assertSijoitteluajo(sijoitteluajo);
            assertHakukohteet(sijoitteluajo.getSijoitteluajoId(), hakukohteet);
            assertHakemuksetKaksiJonoHyvaksyVarallaJaPeruAlempi(hakukohteet);
            assertVastaanotetutValintatuloksetUudelleHakukohteelle(valintatulokset);
            assertHyvaksyttyVaralla(hakukohteet, uusiHakukohdeOid, "112233.111111", 2, 0);
            assertHyvaksyttyVaralla(hakukohteet, uusiHakukohdeOid, "112233.222222", 0, 0);

            return true;
        };

        verifyAndCaptureAndAssert(assertFunction);
    }

    @Test
    public void testErillissijoitteleKaksiJonoHyvaksyVarallaJaPeruAlempi() {
        setUpMocks2();

        service.erillissijoittele(hakuDTO2(false));

        Function3<SijoitteluAjo, List<Hakukohde>, List<Valintatulos>, Boolean> assertFunction = (sijoitteluajo, hakukohteet, valintatulokset) -> {
            assertSijoitteluajo(sijoitteluajo);
            assertHakukohteet(sijoitteluajo.getSijoitteluajoId(), hakukohteet);
            assertHakemuksetKaksiJonoHyvaksyVarallaJaPeruAlempi(hakukohteet);
            assertVastaanotetutValintatuloksetUudelleHakukohteelle(valintatulokset);
            assertHyvaksyttyVaralla(hakukohteet, uusiHakukohdeOid, "112233.111111", 2, 0);
            assertHyvaksyttyVaralla(hakukohteet, uusiHakukohdeOid, "112233.222222", 0, 0);

            return true;
        };

        verifyAndCaptureAndAssert(assertFunction);
    }

    @Test
    public void yliTayttoJonossaSamallaJonosijallaVarallaOlevatHakijatSaavatSamanVarasijanumeron() {
        ValintatietoValintatapajonoDTO jonoDto = sijoitteleYhteenJonoonYksiHyvaksyttyKaksiVaralleSamalleJonosijalle(YLITAYTTO);
        assertYksiHyvaksyttyKaksiVarallaSamallaVarasijalla(jonoDto);
    }

    @Test
    public void aliTayttoJonossaSamallaJonosijallaVarallaOlevatHakijatSaavatSamanVarasijanumeron() {
        ValintatietoValintatapajonoDTO jonoDto = sijoitteleYhteenJonoonYksiHyvaksyttyKaksiVaralleSamalleJonosijalle(ALITAYTTO);
        assertYksiHyvaksyttyKaksiVarallaSamallaVarasijalla(jonoDto);
    }

    @Test
    public void arvontaJonossaSamallaJonosijallaVarallaOlevatHakijatSaavatEriVarasijanumerot() {
        ValintatietoValintatapajonoDTO jonoDto = sijoitteleYhteenJonoonYksiHyvaksyttyKaksiVaralleSamalleJonosijalle(ARVONTA);
        ArgumentCaptor<SijoitteluAjo> sijoitteluajoCaptor = ArgumentCaptor.forClass(SijoitteluAjo.class);
        ArgumentCaptor<List<Hakukohde>> hakukohteetCaptor = ArgumentCaptor.forClass((Class)List.class);

        verify(valintarekisteriService).tallennaSijoittelu(sijoitteluajoCaptor.capture(),
            hakukohteetCaptor.capture(),
            ((ArgumentCaptor<List<Valintatulos>>) ArgumentCaptor.forClass((Class) List.class)).capture());

        SijoitteluAjo sijoitteluAjo = sijoitteluajoCaptor.getValue();
        List<Hakukohde> hakukohteet = hakukohteetCaptor.getValue();

        assertYksiHyvaksyttyKaksiVarallaYhdessaJonossa(sijoitteluAjo, hakukohteet);

        HakijaDTO ensimmainenHakijaVaralla = jonoDto.getHakija().get(1);
        HakijaDTO toinenHakijaVaralla = jonoDto.getHakija().get(2);

        List<Hakemus> hakemustenTulokset = hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset();
        Hakemus ensimmaisenVarallaolijanTulos = hakemustenTulokset.stream()
            .filter(h -> h.getHakemusOid().equals(ensimmainenHakijaVaralla.getHakemusOid())).findFirst().get();
        Hakemus toisenVarallaolijanTulos = hakemustenTulokset.stream()
            .filter(h -> h.getHakemusOid().equals(toinenHakijaVaralla.getHakemusOid())).findFirst().get();
        assertEquals(HakemuksenTila.VARALLA, ensimmaisenVarallaolijanTulos.getTila());
        assertEquals(HakemuksenTila.VARALLA, toisenVarallaolijanTulos.getTila());
        assertEquals(ensimmaisenVarallaolijanTulos.getJonosija(), toisenVarallaolijanTulos.getJonosija());
        assertEquals((ensimmaisenVarallaolijanTulos.getVarasijanNumero() + 1), (int) toisenVarallaolijanTulos.getVarasijanNumero());
    }

    private ValintatietoValintatapajonoDTO sijoitteleYhteenJonoonYksiHyvaksyttyKaksiVaralleSamalleJonosijalle
        (fi.vm.sade.valintalaskenta.domain.dto.valintakoe.Tasasijasaanto tasasijasaanto) {
        when(valintarekisteriService.getLatestSijoitteluajo(hakuOid)).thenReturn(null);
        when(valintarekisteriService.getSijoitteluajonHakukohteet(sijoitteluajoId)).thenReturn(Collections.emptyList());
        when(valintarekisteriService.getValintatulokset(hakuOid)).thenReturn(Collections.emptyList());

        String jonoOid = uusiHakukohdeOid + ".111111";
        ValintatietoValintatapajonoDTO jonoDto = jonoDTO(jonoOid);
        jonoDto.setTasasijasaanto(tasasijasaanto);
        jonoDto.setAloituspaikat(1);
        assertThat(jonoDto.getHakija(), hasSize(2));
        HakijaDTO ensimmainenSamallaSijallaVaralla = jonoDto.getHakija().get(jonoDto.getHakija().size() -1);
        ensimmainenSamallaSijallaVaralla.setTasasijaJonosija(1);
        HakijaDTO toinenSamallaSijallaVaralla = new HakijaDTO();
        toinenSamallaSijallaVaralla.setTasasijaJonosija(2);
        toinenSamallaSijallaVaralla.setOid("9.8.7.6.5");
        toinenSamallaSijallaVaralla.setHakemusOid("8.7.6.5.4.3");
        toinenSamallaSijallaVaralla.setTila(JarjestyskriteerituloksenTilaDTO.HYVAKSYTTAVISSA);
        toinenSamallaSijallaVaralla.setJonosija(ensimmainenSamallaSijallaVaralla.getJonosija());
        List<HakijaDTO> kaikkiHakijat = new ArrayList<>(jonoDto.getHakija());
        kaikkiHakijat.add(toinenSamallaSijallaVaralla);
        jonoDto.setHakija(kaikkiHakijat);

        service.sijoittele(hakuDTO(Collections.singletonList(jonoDto), false),
            Collections.emptySet(),
            Sets.newHashSet(jonoOid),
            -1L);

        verify(valintarekisteriService).getLatestSijoitteluajo(hakuOid);
        return jonoDto;
    }

    @Test
    public void testSijoitteleIlmanPriorisointia() {
        setUpMocks2();

        service.sijoitteleIlmanPriorisointia(hakuDTO2(true), Collections.emptySet(), Sets.newHashSet(
            "112233.111111", "112244.111111", "112255.111111", "112233.222222", "112244.222222", "112255.222222"), (long)123456789);

        Function3<SijoitteluAjo, List<Hakukohde>, List<Valintatulos>, Boolean> assertFunction = (sijoitteluajo, hakukohteet, valintatulokset) -> {
            assertSijoitteluajo(sijoitteluajo);
            assertHakukohteet(sijoitteluajo.getSijoitteluajoId(), hakukohteet);
            assertHakemuksetKaksiJonoHyvaksyVarallaJaPeruAlempi(hakukohteet);
            //assertVastaanotetutValintatuloksetUudelleHakukohteelle(valintatulokset);
            assertHyvaksyttyVaralla(hakukohteet, uusiHakukohdeOid, "112233.111111", 2, 0);
            assertHyvaksyttyVaralla(hakukohteet, uusiHakukohdeOid, "112233.222222", 0, 0);

            return true;
        };

        verifyAndCaptureAndAssert(assertFunction);
    }

    private void assertYksiHyvaksyttyKaksiVarallaSamallaVarasijalla(ValintatietoValintatapajonoDTO jonoDto) {
        ArgumentCaptor<SijoitteluAjo> sijoitteluajoCaptor = ArgumentCaptor.forClass(SijoitteluAjo.class);
        ArgumentCaptor<List<Hakukohde>> hakukohteetCaptor = ArgumentCaptor.forClass((Class)List.class);

        verify(valintarekisteriService).tallennaSijoittelu(sijoitteluajoCaptor.capture(),
            hakukohteetCaptor.capture(),
            ((ArgumentCaptor<List<Valintatulos>>) ArgumentCaptor.forClass((Class) List.class)).capture());

        SijoitteluAjo sijoitteluAjo = sijoitteluajoCaptor.getValue();
        List<Hakukohde> hakukohteet = hakukohteetCaptor.getValue();

        assertYksiHyvaksyttyKaksiVarallaYhdessaJonossa(sijoitteluAjo, hakukohteet);

        HakijaDTO ensimmainenHakijaVaralla = jonoDto.getHakija().get(1);
        HakijaDTO toinenHakijaVaralla = jonoDto.getHakija().get(2);

        List<Hakemus> hakemustenTulokset = hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset();
        Hakemus ensimmaisenVarallaolijanTulos = hakemustenTulokset.stream()
            .filter(h -> h.getHakemusOid().equals(ensimmainenHakijaVaralla.getHakemusOid())).findFirst().get();
        Hakemus toisenVarallaolijanTulos = hakemustenTulokset.stream()
            .filter(h -> h.getHakemusOid().equals(toinenHakijaVaralla.getHakemusOid())).findFirst().get();
        assertEquals(HakemuksenTila.VARALLA, ensimmaisenVarallaolijanTulos.getTila());
        assertEquals(HakemuksenTila.VARALLA, toisenVarallaolijanTulos.getTila());
        assertEquals(ensimmaisenVarallaolijanTulos.getJonosija(), toisenVarallaolijanTulos.getJonosija());
        assertEquals(ensimmaisenVarallaolijanTulos.getVarasijanNumero(), toisenVarallaolijanTulos.getVarasijanNumero());
    }

    private void assertYksiHyvaksyttyKaksiVarallaYhdessaJonossa(SijoitteluAjo sijoitteluAjo, List<Hakukohde> hakukohteet) {
        assertNotEquals(sijoitteluajoId, (long) sijoitteluAjo.getSijoitteluajoId());
        assertEquals(1, sijoitteluAjo.getHakukohteet().size());

        assertEquals(Collections.singletonList(uusiHakukohdeOid),
            sijoitteluAjo.getHakukohteet().stream().map(HakukohdeItem::getOid).collect(Collectors.toList()));
        assertThat(hakukohteet, hasSize(1));
        assertThat(hakukohteet.get(0).getValintatapajonot(), hasSize(1));
        String onlyJonoOid = hakukohteet.get(0).getValintatapajonot().get(0).getOid();
        assertHyvaksyttyVaralla(hakukohteet, uusiHakukohdeOid, onlyJonoOid, 1, 2);
    }

    private SijoitteluAjo valintarekisteriSijoitteluajo1() {
        return valintarekisteriSijoitteluajo(valintarekisteriHakukohdeOidit);
    }

    private SijoitteluAjo valintarekisteriSijoitteluajo2() {
        return valintarekisteriSijoitteluajo(hakukohdeOidit);
    }

    private SijoitteluAjo valintarekisteriSijoitteluajo(List<String> hakukohteet) {
        SijoitteluAjo ajo = new SijoitteluAjo();
        ajo.setSijoitteluajoId(sijoitteluajoId);
        ajo.setStartMils(System.currentTimeMillis());
        ajo.setEndMils(System.currentTimeMillis());
        ajo.setHakukohteet(hakukohteet.stream().map(this::item).collect(Collectors.toList()));
        return ajo;
    }

    private HakukohdeItem item(String oid) {
        HakukohdeItem item = new HakukohdeItem();
        item.setOid(oid);
        return item;
    }

    private fi.vm.sade.sijoittelu.laskenta.external.resource.dto.HakuDTO tarjontaHaku() {
        fi.vm.sade.sijoittelu.laskenta.external.resource.dto.HakuDTO haku = new fi.vm.sade.sijoittelu.laskenta.external.resource.dto.HakuDTO();
        haku.setKohdejoukkoUri("haunkohdejoukko_12#1");
        return haku;
    }

    private ParametriDTO haunParametrit() {
        ParametriDTO parametrit = new ParametriDTO();
        parametrit.setPH_HKP(new ParametriArvoDTO(System.currentTimeMillis() + 2000000));
        parametrit.setPH_VTSSV(new ParametriArvoDTO(System.currentTimeMillis() + 1000000));
        parametrit.setPH_VSTP(new ParametriArvoDTO(System.currentTimeMillis() + 1000000));
        parametrit.setPH_VSSAV(new ParametriArvoDTO(System.currentTimeMillis() - 1000000));
        return parametrit;
    }

    private HakuDTO hakuDTO1(boolean lisaaValintarekisterinHakukohteet) {
        return hakuDTO(Collections.singletonList(jonoDTO(uusiHakukohdeOid + ".111111")), lisaaValintarekisterinHakukohteet);
    }

    private HakuDTO hakuDTO2(boolean lisaaValintarekisterinHakukohteet) {
        return hakuDTO(Arrays.asList(jonoDTO(uusiHakukohdeOid + ".111111"),
            jonoDTO(uusiHakukohdeOid + ".222222")), lisaaValintarekisterinHakukohteet);
    }

    private HakuDTO hakuDTO(List<ValintatietoValintatapajonoDTO> jonot, boolean lisaaValintarekisterinHakukohteet) {
        HakuDTO haku = new HakuDTO();
        haku.setHakuOid(hakuOid);

        HakukohdeDTO hakukohde = new HakukohdeDTO();
        hakukohde.setOid(uusiHakukohdeOid);
        ValintatietoValinnanvaiheDTO vaihe = new ValintatietoValinnanvaiheDTO(
            1, "1", hakuOid, "vaihe1", new java.util.Date(), jonot, Collections.emptyList());
        hakukohde.setValinnanvaihe(Collections.singletonList(vaihe));
        List<HakukohdeDTO> hakukohdeDtos = new ArrayList<>();
        if (lisaaValintarekisterinHakukohteet) {
            hakukohdeDtos.addAll(valintarekisteriHakukohdeOidit.stream().map(oid -> {
                HakukohdeDTO hakukohdeDto = new HakukohdeDTO();
                hakukohdeDto.setOid(oid);
                return hakukohdeDto;
            }).collect(Collectors.toList()));
        }
        hakukohdeDtos.add(hakukohde);
        haku.setHakukohteet(hakukohdeDtos);
        return haku;
    }

    private ValintatietoValintatapajonoDTO jonoDTO(String oid) {
        int prioriteetti = Integer.parseInt("" + oid.charAt(oid.length()-1));

        ValintatietoValintatapajonoDTO jono1 = new ValintatietoValintatapajonoDTO();
        jono1.setOid(oid);
        jono1.setSiirretaanSijoitteluun(true);
        jono1.setAktiivinen(true);
        jono1.setEiVarasijatayttoa(false);
        jono1.setVarasijat(2);
        jono1.setAloituspaikat(5);
        jono1.setValmisSijoiteltavaksi(true);
        jono1.setPoissaOlevaTaytto(true);
        jono1.setPrioriteetti(prioriteetti);

        HakijaDTO hakija1 = new HakijaDTO();
        hakija1.setOid(uusiHakukohdeOid + ".111111.11");
        hakija1.setHakemusOid(uusiHakukohdeOid + ".111111.11");
        hakija1.setJonosija(1);
        hakija1.setPrioriteetti(prioriteetti);
        hakija1.setTila(JarjestyskriteerituloksenTilaDTO.HYVAKSYTTAVISSA);

        HakijaDTO hakija2 = new HakijaDTO();
        hakija2.setOid(uusiHakukohdeOid + ".111111.22");
        hakija2.setHakemusOid(uusiHakukohdeOid + ".111111.22");
        hakija2.setJonosija(2);
        hakija2.setPrioriteetti(prioriteetti);
        hakija2.setTila(JarjestyskriteerituloksenTilaDTO.HYVAKSYTTAVISSA);

        jono1.setHakija(Arrays.asList(hakija1, hakija2));
        return jono1;
    }

    private List<Valintatulos> valintarekisteriValintatulokset1() {
        List<Valintatulos> valintatulokset = new ArrayList<>();
        valintarekisteriHakukohdeOidit.forEach(oid ->
            Collections.singletonList(oid + ".111111").forEach(jonoOid ->
                Arrays.asList(oid + ".111111.11", oid + ".111111.22").forEach(hakemusOid -> {
                    Valintatulos valintatulos = new Valintatulos();
                    valintatulos.setHakemusOid(hakemusOid, "");
                    valintatulos.setHakijaOid(hakemusOid, "");
                    valintatulos.setHakukohdeOid(oid, "");
                    valintatulos.setHakuOid(hakuOid, "");
                    valintatulos.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY, "", "");
                    valintatulos.setTila(ValintatuloksenTila.KESKEN, "", "");
                    valintatulos.setValintatapajonoOid(jonoOid, "");
                    valintatulos.setHyvaksyPeruuntunut(true, "");
                    valintatulos.setHyvaksyttyVarasijalta(true, "");
                    valintatulokset.add(valintatulos);
                })));
        return valintatulokset;
    }

    private List<Valintatulos> valintarekisteriValintatulokset2() {
        List<Valintatulos> valintatulokset = new ArrayList<>();
        hakukohdeOidit.forEach(oid ->
            Arrays.asList(oid + ".111111", oid + ".222222").forEach(jonoOid ->
                Arrays.asList(oid + ".111111.11", oid + ".111111.22").forEach(hakemusOid -> {
                    Valintatulos valintatulos = new Valintatulos();
                    valintatulos.setHakemusOid(hakemusOid, "");
                    valintatulos.setHakijaOid(hakemusOid, "");
                    valintatulos.setHakukohdeOid(oid, "");
                    valintatulos.setHakuOid(hakuOid, "");
                    if (jonoOid.equals(oid + ".111111")) {
                        valintatulos.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY, "", "");
                        valintatulos.setTila(ValintatuloksenTila.KESKEN, "", "");
                        valintatulos.setJulkaistavissa(true, "");

                    } else {
                        valintatulos.setIlmoittautumisTila(IlmoittautumisTila.EI_TEHTY, "", "");
                        valintatulos.setTila(ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT, "", "");
                        valintatulos.setJulkaistavissa(true, "");
                    }
                    valintatulos.setValintatapajonoOid(jonoOid, "");
                    valintatulokset.add(valintatulos);
                })));
        return valintatulokset;
    }

    private Valintatapajono jono1(String oid) {
        Valintatapajono jono = new Valintatapajono();
        jono.setOid(oid + ".111111");
        jono.setNimi("jono");
        jono.setAloituspaikat(5);
        jono.setVarasijat(2);
        jono.setEiVarasijatayttoa(false);
        jono.setVarasijojaKaytetaanAlkaen(new java.util.Date(System.currentTimeMillis() - 1000));
        jono.setVarasijojaTaytetaanAsti(new java.util.Date(System.currentTimeMillis() + 1000));
        jono.setPoissaOlevaTaytto(true);
        jono.setPrioriteetti(1);
        jono.setSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa(true);
        jono.setTasasijasaanto(Tasasijasaanto.ARVONTA);
        jono.setHakemukset(
                Stream.of(oid + ".111111.11", oid + ".111111.22").map(hakemusOid -> {
                    Hakemus hakemus = new Hakemus();
                    hakemus.setHakemusOid(hakemusOid);
                    hakemus.setTila(HakemuksenTila.VARALLA);
                    hakemus.setHakijaOid(hakemusOid);
                    hakemus.setJonosija(Integer.parseInt("" + hakemusOid.charAt(hakemusOid.length()-1)));
                    hakemus.setPrioriteetti(1);
                    return hakemus;
                }).collect(Collectors.toList())
        );
        return jono;
    }

    private Valintatapajono jono2(String oid) {
        Valintatapajono jono = new Valintatapajono();
        jono.setOid(oid + ".222222");
        jono.setNimi("jono");
        jono.setAloituspaikat(5);
        jono.setVarasijat(2);
        jono.setEiVarasijatayttoa(false);
        jono.setVarasijojaKaytetaanAlkaen(new java.util.Date(System.currentTimeMillis() - 1000));
        jono.setVarasijojaTaytetaanAsti(new java.util.Date(System.currentTimeMillis() + 1000));
        jono.setPoissaOlevaTaytto(true);
        jono.setSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa(true);
        jono.setTasasijasaanto(Tasasijasaanto.ARVONTA);
        jono.setPrioriteetti(2);
        jono.setHakemukset(
                Stream.of(oid + ".111111.11", oid + ".111111.22").map(hakemusOid -> {
                    Hakemus hakemus = new Hakemus();
                    hakemus.setHakemusOid(hakemusOid);
                    hakemus.setTila(HakemuksenTila.HYVAKSYTTY);
                    hakemus.setHakijaOid(hakemusOid);
                    hakemus.setJonosija(Integer.parseInt("" + hakemusOid.charAt(hakemusOid.length()-1)));
                    hakemus.setPrioriteetti(2);
                    return hakemus;
                }).collect(Collectors.toList())
        );
        return jono;
    }

    private List<Hakukohde> valintarekisteriHakukohteet1() {
        return valintarekisteriHakukohdeOidit.stream().map(oid -> {
                    Hakukohde hakukohde = new Hakukohde();
                    hakukohde.setOid(oid);
                    hakukohde.setSijoitteluajoId(sijoitteluajoId);
                    hakukohde.setValintatapajonot(Collections.singletonList(jono1(oid)));
                    return hakukohde;
                }).collect(Collectors.toList());
    }

    private List<Hakukohde> valintarekisteriHakukohteet2() {
        return hakukohdeOidit.stream().map(oid -> {
            Hakukohde hakukohde = new Hakukohde();
            hakukohde.setOid(oid);
            hakukohde.setSijoitteluajoId(sijoitteluajoId);
            hakukohde.setValintatapajonot(Arrays.asList(jono1(oid), jono2(oid)));
            return hakukohde;
        }).collect(Collectors.toList());
    }
}
