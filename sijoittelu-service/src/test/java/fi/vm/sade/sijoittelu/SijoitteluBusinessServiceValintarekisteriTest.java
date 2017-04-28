package fi.vm.sade.sijoittelu;

import com.google.common.collect.Sets;
import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.sijoittelu.laskenta.external.resource.VirkailijaValintaTulosServiceResource;
import fi.vm.sade.sijoittelu.laskenta.service.business.ActorService;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.sijoittelu.laskenta.service.business.ValintarekisteriService;
import fi.vm.sade.sijoittelu.laskenta.service.it.TarjontaIntegrationService;
import fi.vm.sade.sijoittelu.tulos.dao.HakukohdeDao;
import fi.vm.sade.sijoittelu.tulos.service.impl.converters.SijoitteluTulosConverter;
import fi.vm.sade.sijoittelu.tulos.service.impl.converters.SijoitteluTulosConverterImpl;
import fi.vm.sade.valintalaskenta.domain.dto.JarjestyskriteerituloksenTilaDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.ValintatietoValinnanvaiheDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.ValintatietoValintatapajonoDTO;
import org.junit.Before;
import org.junit.Test;

import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintalaskenta.domain.dto.HakukohdeDTO;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import rx.functions.Func3;

import java.util.*;
import java.util.stream.Collectors;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.Assert.*;

public class SijoitteluBusinessServiceValintarekisteriTest {

    private String hakuOid = "12345";
    private long sijoitteluajoId = 12345l;
    private String uusiHakukohdeOid = "112233";
    private List<String> valintarekisteriHakukohdeOidit = Arrays.asList("112244","112255");
    private List<String> hakukohdeOidit = Arrays.asList("112233", "112244","112255");

    private SijoitteluBusinessService service;
    private SijoitteluTulosConverter sijoitteluTulosConverter;
    private ActorService actorService;
    private TarjontaIntegrationService tarjontaIntegrationService;
    private VirkailijaValintaTulosServiceResource valintaTulosServiceResource;
    private ValintarekisteriService valintarekisteriService;

    @Before
    public void setUp() throws Exception {
        sijoitteluTulosConverter = new SijoitteluTulosConverterImpl();
        actorService = mock(ActorService.class);
        tarjontaIntegrationService = mock(TarjontaIntegrationService.class);
        valintaTulosServiceResource = mock(VirkailijaValintaTulosServiceResource.class);
        valintarekisteriService = mock(ValintarekisteriService.class);

        service = new SijoitteluBusinessService(1, 1, null, null, null, null,
          sijoitteluTulosConverter, actorService, tarjontaIntegrationService, valintaTulosServiceResource, valintarekisteriService);

        ReflectionTestUtils.setField(service,"readSijoitteluFromValintarekisteri",true);

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

    private void verifyAndCaptureAndAssert(Func3<SijoitteluAjo, List<Hakukohde>, List<Valintatulos>, Boolean> assertFunction) {
        verify(valintarekisteriService).getLatestSijoitteluajo(hakuOid);
        verify(valintarekisteriService).getSijoitteluajonHakukohteet(sijoitteluajoId);
        verify(valintarekisteriService).getValintatulokset(hakuOid);

        ArgumentCaptor<SijoitteluAjo> sijoitteluajoCaptor = ArgumentCaptor.forClass(SijoitteluAjo.class);
        ArgumentCaptor<List<Hakukohde>> hakukohteetCaptor = ArgumentCaptor.forClass((Class)List.class);
        ArgumentCaptor<List<Valintatulos>> valintatuloksetCaptor = ArgumentCaptor.forClass((Class)List.class);

        verify(valintarekisteriService).tallennaSijoittelu(sijoitteluajoCaptor.capture(), hakukohteetCaptor.capture(), valintatuloksetCaptor.capture());

        assertFunction.call(sijoitteluajoCaptor.getValue(), hakukohteetCaptor.getValue(), valintatuloksetCaptor.getValue());
    }

    private void assertSijoitteluajo(SijoitteluAjo sijoitteluajo) {
        assertNotEquals(sijoitteluajoId, (long)sijoitteluajo.getSijoitteluajoId());
        assertEquals(hakukohdeOidit.size(), sijoitteluajo.getHakukohteet().size());
        assertTrue(hakukohdeOidit.containsAll(sijoitteluajo.getHakukohteet().stream().map(i -> i.getOid()).collect(Collectors.toList())));
    }

    private void assertHakukohteet(long uusiSijoitteluajoId, List<Hakukohde> hakukohteet) {
        assertEquals(hakukohdeOidit.size(), hakukohteet.size());
        assertTrue(hakukohdeOidit.containsAll(hakukohteet.stream().map(i -> i.getOid()).collect(Collectors.toList())));
        assertFalse(hakukohteet.stream().filter(hk -> hk.getSijoitteluajoId() != uusiSijoitteluajoId).findAny().isPresent());
    }

    private void assertHakemuksetKaksiJonoHyvaksyVarallaJaPeruAlempi(List<Hakukohde> hakukohteet) {
        List<Valintatapajono> valintatapajonot = hakukohteet.stream().filter(hk -> hk.getOid() == uusiHakukohdeOid).findAny().get().getValintatapajonot();

        List<Hakemus> hakemuksetJono1 = valintatapajonot.stream().filter(jono -> "112233.111111".equals(jono.getOid())).findAny().get().getHakemukset();
        List<Hakemus> hakemuksetJono2 = valintatapajonot.stream().filter(jono -> "112233.222222".equals(jono.getOid())).findAny().get().getHakemukset();

        assertEquals(2, hakemuksetJono1.size());
        assertEquals(2, hakemuksetJono2.size());

        assertFalse(hakemuksetJono1.stream().filter(t -> t.getTila() != HakemuksenTila.VARASIJALTA_HYVAKSYTTY).findAny().isPresent());
        assertFalse(hakemuksetJono2.stream().filter(t -> t.getTila() != HakemuksenTila.PERUUNTUNUT).findAny().isPresent());
    }

    private void assertVastaanotetutValintatuloksetUudelleHakukohteelle(List<Valintatulos> valintatulokset) {
        assertFalse(valintatulokset.stream().filter(v -> v.getHakukohdeOid() != uusiHakukohdeOid).findAny().isPresent());
        assertFalse(valintatulokset.stream().filter(v -> v.getTila() != ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI).findAny().isPresent());
        assertEquals(4, valintatulokset.size());
    }

    private void assertHakemukset(List<Hakukohde> hakukohteet, HakemuksenTila uusiHakukohdeTila, HakemuksenTila muutHakukohteetTila) {
        List<Hakemus> uudenHakukohteenHakemukset = new ArrayList<>();
        List<Hakemus> muidenHakukohteidenHakemukset = new ArrayList<>();

        hakukohteet.stream().forEach(hakukohde ->
            hakukohde.getValintatapajonot().stream().forEach(jono -> {
                if(uusiHakukohdeOid.equals(hakukohde.getOid())) {
                    uudenHakukohteenHakemukset.addAll(jono.getHakemukset());
                } else {
                    muidenHakukohteidenHakemukset.addAll(jono.getHakemukset());
                }
            }));

        assertFalse(uudenHakukohteenHakemukset.stream().filter(t -> t.getTila() != uusiHakukohdeTila).findAny().isPresent());
        assertFalse(muidenHakukohteidenHakemukset.stream().filter(t -> t.getTila() != muutHakukohteetTila).findAny().isPresent());

        assertEquals(2, uudenHakukohteenHakemukset.size());
        assertEquals(4, muidenHakukohteidenHakemukset.size());
    }

    private void assertHyvaksyttyVaralla(List<Hakukohde> hakukohteet, String hakukohdeOid, String jonoOid, int hyvaksytty, int varalla) {
        List<Valintatapajono> valintatapajonot = hakukohteet.stream().filter(hk -> hk.getOid() == hakukohdeOid).findAny().get().getValintatapajonot();

        assertEquals(hyvaksytty, (int)valintatapajonot.stream().filter(jono -> jonoOid.equals(jono.getOid())).findAny().get().getHyvaksytty());
        assertEquals(varalla, (int)valintatapajonot.stream().filter(jono -> jonoOid.equals(jono.getOid())).findAny().get().getVaralla());
    }

    @Test
    public void testSijoitteleEiMuuttuneitaValinnantuloksia() {
        setUpMocks1();

        service.sijoittele(hakuDTO1(), Collections.emptySet(), Sets.newHashSet("112233.111111", "112244.111111", "112255.111111"));

        Func3<SijoitteluAjo, List<Hakukohde>, List<Valintatulos>, Boolean> assertFunction = (sijoitteluajo, hakukohteet, valintatulokset) -> {
            assertSijoitteluajo(sijoitteluajo);
            assertHakukohteet(sijoitteluajo.getSijoitteluajoId(), hakukohteet);
            assertHakemukset(hakukohteet, HakemuksenTila.HYVAKSYTTY, HakemuksenTila.VARASIJALTA_HYVAKSYTTY);
            assertHyvaksyttyVaralla(hakukohteet, uusiHakukohdeOid, "112233.111111", 2, 0);
            assertHyvaksyttyVaralla(hakukohteet, "112244", "112244.111111", 2, 0);

            assertEquals(0, valintatulokset.size());
            return true;
        };

        verifyAndCaptureAndAssert(assertFunction);
    }

    @Test
    public void testErillissijoitteleUusiHakukohde() {
        setUpMocks1();

        service.erillissijoittele(hakuDTO1());

        Func3<SijoitteluAjo, List<Hakukohde>, List<Valintatulos>, Boolean> assertFunction = (sijoitteluajo, hakukohteet, valintatulokset) -> {
            assertSijoitteluajo(sijoitteluajo);
            assertHakukohteet(sijoitteluajo.getSijoitteluajoId(), hakukohteet);
            assertHakemukset(hakukohteet, HakemuksenTila.HYVAKSYTTY, HakemuksenTila.VARALLA);
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

        service.sijoittele(hakuDTO2(), Collections.emptySet(), Sets.newHashSet("112233.111111", "112244.111111", "112255.111111", "112233.222222", "112244.222222", "112255.222222"));

        Func3<SijoitteluAjo, List<Hakukohde>, List<Valintatulos>, Boolean> assertFunction = (sijoitteluajo, hakukohteet, valintatulokset) -> {
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

        service.erillissijoittele(hakuDTO2());

        Func3<SijoitteluAjo, List<Hakukohde>, List<Valintatulos>, Boolean> assertFunction = (sijoitteluajo, hakukohteet, valintatulokset) -> {
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
        ajo.setHakukohteet(hakukohteet.stream().map(oid -> item(oid)).collect(Collectors.toList()));
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

    private fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriDTO haunParametrit() {
        fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriDTO parametrit = new fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriDTO();
        parametrit.setPH_HKP(new fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriArvoDTO(System.currentTimeMillis() + 1000000));
        parametrit.setPH_VTSSV(new fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriArvoDTO(System.currentTimeMillis() + 1000000));
        parametrit.setPH_VSTP(new fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriArvoDTO(System.currentTimeMillis() + 1000000));
        parametrit.setPH_VSSAV(new fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriArvoDTO(System.currentTimeMillis() - 1000000));
        return parametrit;
    }

    private HakuDTO hakuDTO1() {
        return hakuDTO(Arrays.asList(jonoDTO(uusiHakukohdeOid + ".111111")));
    }

    private HakuDTO hakuDTO2() {
        return hakuDTO(Arrays.asList(jonoDTO(uusiHakukohdeOid + ".111111"), jonoDTO(uusiHakukohdeOid + ".222222")));
    }

    private HakuDTO hakuDTO(List<ValintatietoValintatapajonoDTO> jonot) {
        HakuDTO haku = new HakuDTO();
        haku.setHakuOid(hakuOid);

        HakukohdeDTO hakukohde = new HakukohdeDTO();
        hakukohde.setOid(uusiHakukohdeOid);
        ValintatietoValinnanvaiheDTO vaihe = new ValintatietoValinnanvaiheDTO(1, "1", hakuOid, "vaihe1", new java.util.Date(), jonot, Collections.emptyList());
        hakukohde.setValinnanvaihe(Arrays.asList(vaihe));
        haku.setHakukohteet(Arrays.asList(hakukohde));
        return haku;
    }

    private ValintatietoValintatapajonoDTO jonoDTO(String oid) {
        int prioriteetti = Integer.parseInt("" + oid.charAt(oid.length()-1));

        fi.vm.sade.valintalaskenta.domain.dto.valintatieto.ValintatietoValintatapajonoDTO jono1 = new fi.vm.sade.valintalaskenta.domain.dto.valintatieto.ValintatietoValintatapajonoDTO();
        jono1.setOid(oid);
        jono1.setSiirretaanSijoitteluun(true);
        jono1.setAktiivinen(true);
        jono1.setEiVarasijatayttoa(false);
        jono1.setVarasijat(2);
        jono1.setAloituspaikat(5);
        jono1.setValmisSijoiteltavaksi(true);
        jono1.setPoissaOlevaTaytto(true);
        jono1.setPrioriteetti(prioriteetti);

        fi.vm.sade.valintalaskenta.domain.dto.HakijaDTO hakija1 = new fi.vm.sade.valintalaskenta.domain.dto.HakijaDTO();
        hakija1.setOid(uusiHakukohdeOid + ".111111.11");
        hakija1.setHakemusOid(uusiHakukohdeOid + ".111111.11");
        hakija1.setJonosija(1);
        hakija1.setPrioriteetti(prioriteetti);
        hakija1.setTila(JarjestyskriteerituloksenTilaDTO.HYVAKSYTTAVISSA);

        fi.vm.sade.valintalaskenta.domain.dto.HakijaDTO hakija2 = new fi.vm.sade.valintalaskenta.domain.dto.HakijaDTO();
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
        valintarekisteriHakukohdeOidit.stream().forEach(oid ->
            Arrays.asList(oid + ".111111").stream().forEach(jonoOid ->
                Arrays.asList(oid + ".111111.11", oid + ".111111.22").stream().forEach(hakemusOid -> {
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
        hakukohdeOidit.stream().forEach(oid ->
                Arrays.asList(oid + ".111111", oid + ".222222").stream().forEach(jonoOid ->
                        Arrays.asList(oid + ".111111.11", oid + ".111111.22").stream().forEach(hakemusOid -> {
                            Valintatulos valintatulos = new Valintatulos();
                            valintatulos.setHakemusOid(hakemusOid, "");
                            valintatulos.setHakijaOid(hakemusOid, "");
                            valintatulos.setHakukohdeOid(oid, "");
                            valintatulos.setHakuOid(hakuOid, "");
                            if(jonoOid.equals(oid + ".111111")) {
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
        jono.setTasasijasaanto(Tasasijasaanto.ARVONTA);
        jono.setHakemukset(
                Arrays.asList(oid + ".111111.11", oid + ".111111.22").stream().map(hakemusOid -> {
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
        jono.setTasasijasaanto(Tasasijasaanto.ARVONTA);
        jono.setPrioriteetti(2);
        jono.setHakemukset(
                Arrays.asList(oid + ".111111.11", oid + ".111111.22").stream().map(hakemusOid -> {
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
                    hakukohde.setValintatapajonot(Arrays.asList(jono1(oid)));
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
