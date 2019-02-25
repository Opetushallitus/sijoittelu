package fi.vm.sade.sijoittelu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.google.common.collect.Sets;

import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.HakukohdeItem;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.Tasasijasaanto;
import fi.vm.sade.sijoittelu.domain.TilanKuvaukset;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.domain.dto.VastaanottoDTO;
import fi.vm.sade.sijoittelu.laskenta.external.resource.VirkailijaValintaTulosServiceResource;
import fi.vm.sade.sijoittelu.laskenta.service.business.ActorService;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.sijoittelu.laskenta.service.business.ValintarekisteriService;
import fi.vm.sade.sijoittelu.laskenta.service.it.TarjontaIntegrationService;
import fi.vm.sade.sijoittelu.tulos.service.impl.converters.SijoitteluTulosConverter;
import fi.vm.sade.sijoittelu.tulos.service.impl.converters.SijoitteluTulosConverterImpl;
import fi.vm.sade.valintalaskenta.domain.dto.AvainArvoDTO;
import fi.vm.sade.valintalaskenta.domain.dto.HakijaDTO;
import fi.vm.sade.valintalaskenta.domain.dto.HakukohdeDTO;
import fi.vm.sade.valintalaskenta.domain.dto.JarjestyskriteerituloksenTilaDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.ValintatietoValinnanvaiheDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.ValintatietoValintatapajonoDTO;
import io.reactivex.functions.Function3;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SijoitteluTilatJaKuvauksetTest {
    private static final Duration ONE_DAY = Duration.ofDays(1);
    private String hakuOid = "12345";
    private long sijoitteluajoId = 12345l;
    private String hakukohdeOid = "112233";
    private String jonoOid = "112233.000000";
    private String hakijaOid = "peruuntunuthakija";

    private SijoitteluBusinessService service;
    private SijoitteluTulosConverter sijoitteluTulosConverter;
    private ActorService actorService;
    private TarjontaIntegrationService tarjontaIntegrationService;
    private VirkailijaValintaTulosServiceResource valintaTulosServiceResource;
    private ValintarekisteriService valintarekisteriService;

    @Before
    public void setUp() {
        sijoitteluTulosConverter = new SijoitteluTulosConverterImpl();
        actorService = mock(ActorService.class);
        tarjontaIntegrationService = mock(TarjontaIntegrationService.class);
        valintaTulosServiceResource = mock(VirkailijaValintaTulosServiceResource.class);
        valintarekisteriService = mock(ValintarekisteriService.class);

        service = new SijoitteluBusinessService(sijoitteluTulosConverter, tarjontaIntegrationService, valintaTulosServiceResource, valintarekisteriService);

        when(tarjontaIntegrationService.getHaunParametrit(hakuOid)).thenReturn(haunParametrit());
        when(tarjontaIntegrationService.getHakuByHakuOid(hakuOid)).thenReturn(tarjontaHaku());
    }

    private fi.vm.sade.sijoittelu.laskenta.external.resource.dto.HakuDTO tarjontaHaku() {
        fi.vm.sade.sijoittelu.laskenta.external.resource.dto.HakuDTO haku = new fi.vm.sade.sijoittelu.laskenta.external.resource.dto.HakuDTO();
        haku.setKohdejoukkoUri("haunkohdejoukko_12#1");
        return haku;
    }

    private fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriDTO haunParametrit() {
        fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriDTO parametrit = new fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriDTO();
        parametrit.setPH_HKP(new fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriArvoDTO(System.currentTimeMillis() + ONE_DAY.toMillis()));
        parametrit.setPH_VTSSV(new fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriArvoDTO(System.currentTimeMillis() + ONE_DAY.toMillis()));
        parametrit.setPH_VSTP(new fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriArvoDTO(System.currentTimeMillis() + ONE_DAY.toMillis()));
        parametrit.setPH_VSSAV(new fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriArvoDTO(System.currentTimeMillis() - ONE_DAY.toMillis()));
        return parametrit;
    }

    @Test
    public void testSijoitteleHakemuksenTilallaPeruuntunut() throws Exception {
        setupMocksHakemuksenTilaPeruuntunut();

        service.sijoittele(hakuDTO(), Collections.emptySet(), Sets.newHashSet("112233.000000"), 1234567890L);

        Function3<SijoitteluAjo, List<Hakukohde>, List<Valintatulos>, Boolean> assertFunction = (sijoitteluajo, hakukohteet, valintatulokset) -> {
            Hakemus hakemus = hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().get(0);
            assertEquals("peruuntunuthakija", hakemus.getHakijaOid());
            assertEquals(HakemuksenTila.PERUUNTUNUT, hakemus.getTila());
            assertEquals("Peruuntunut, pisteesi eivät riittäneet varasijaan", hakemus.getTilanKuvaukset().get("FI"));
            return true;
        };

        verifyAndCaptureAndAssert(assertFunction);
    }

    @Test
    public void testSijoitteleValintatuloksenTilallaPerunut() throws Exception {
        setupMocksValintatuloksenTilaPerunut();

        service.sijoittele(hakuDTO(), Collections.emptySet(), Sets.newHashSet("112233.000000"), 1234567890L);

        Function3<SijoitteluAjo, List<Hakukohde>, List<Valintatulos>, Boolean> assertFunction = (sijoitteluajo, hakukohteet, valintatulokset) -> {
            Hakemus hakemus = hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().get(0);
            assertEquals("peruuntunuthakija", hakemus.getHakijaOid());
            assertEquals(HakemuksenTila.PERUNUT, hakemus.getTila());
            assertTrue(hakemus.getTilanKuvaukset().isEmpty());
            return true;
        };

        verifyAndCaptureAndAssert(assertFunction);
    }

    @Test
    public void testSijoitteluSailyttaaValinnoistaTulevanHylkayksenSyyn() throws Exception {
        setupMocksHakemuksenTilaHylatty();
        HakuDTO hakuDTO = hakuDTO();
        HakijaDTO hakijaDTO = hakuDTO.getHakukohteet().get(0).getValinnanvaihe().get(0).getValintatapajonot().get(0).getHakija().get(0);
        hakijaDTO.setTila(JarjestyskriteerituloksenTilaDTO.HYLATTY);
        hakijaDTO.setTilanKuvaus(Arrays.asList(
            new AvainArvoDTO("FI", "Ei hakukelpoinen"),
            new AvainArvoDTO("SV", "Inte ansökningsbehörig"),
            new AvainArvoDTO("EN", "Not eligible")));
        service.sijoittele(hakuDTO, Collections.emptySet(), Sets.newHashSet("112233.000000"), 1234567890L);

        Function3<SijoitteluAjo, List<Hakukohde>, List<Valintatulos>, Boolean> assertFunction = (sijoitteluajo, hakukohteet, valintatulokset) -> {
            Hakemus hakemus = hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().get(0);
            assertEquals("peruuntunuthakija", hakemus.getHakijaOid());
            assertEquals(HakemuksenTila.HYLATTY, hakemus.getTila());
            assertEquals("Ei hakukelpoinen", hakemus.getTilanKuvaukset().get("FI"));
            assertEquals("Inte ansökningsbehörig", hakemus.getTilanKuvaukset().get("SV"));
            assertEquals("Not eligible", hakemus.getTilanKuvaukset().get("EN"));
            return true;
        };

        verifyAndCaptureAndAssert(assertFunction);
    }

    private void setupMocksHakemuksenTilaPeruuntunut() {
        when(valintarekisteriService.getLatestSijoitteluajo(hakuOid)).thenReturn(valintarekisteriSijoitteluajo());
        when(valintarekisteriService.getSijoitteluajonHakukohteet(sijoitteluajoId)).thenReturn(valintarekisteriHakukohteetHakemuksenTilalla(HakemuksenTila.PERUUNTUNUT));
        when(valintaTulosServiceResource.haunKoulutuksenAlkamiskaudenVastaanototYhdenPaikanSaadoksenPiirissa(hakuOid)).thenReturn(vastaanototToisessaHakukohteessa());
    }

    private void setupMocksValintatuloksenTilaPerunut() {
        when(valintarekisteriService.getLatestSijoitteluajo(hakuOid)).thenReturn(valintarekisteriSijoitteluajo());
        when(valintarekisteriService.getSijoitteluajonHakukohteet(sijoitteluajoId)).thenReturn(valintarekisteriHakukohteetHakemuksenTilalla(HakemuksenTila.HYVAKSYTTY));
        when(valintaTulosServiceResource.haunKoulutuksenAlkamiskaudenVastaanototYhdenPaikanSaadoksenPiirissa(hakuOid)).thenReturn(vastaanototToisessaHakukohteessa());

        Valintatulos valintatulos = new Valintatulos(jonoOid, hakijaOid, hakukohdeOid, hakijaOid, hakuOid, 0);
        valintatulos.setTila(ValintatuloksenTila.PERUNUT, "selite", "muokkaaja");
        List<Valintatulos> mockValintatulokset = Arrays.asList(valintatulos);
        when(valintarekisteriService.getValintatulokset(hakuOid)).thenReturn(mockValintatulokset);
    }

    private void setupMocksHakemuksenTilaHylatty() {
        when(valintarekisteriService.getLatestSijoitteluajo(hakuOid)).thenReturn(valintarekisteriSijoitteluajo());
        when(valintarekisteriService.getSijoitteluajonHakukohteet(sijoitteluajoId)).thenReturn(valintarekisteriHakukohteetHakemuksenTilalla(HakemuksenTila.HYLATTY));
        when(valintaTulosServiceResource.haunKoulutuksenAlkamiskaudenVastaanototYhdenPaikanSaadoksenPiirissa(hakuOid)).thenReturn(Collections.emptyList());
    }

    private SijoitteluAjo valintarekisteriSijoitteluajo() {
        SijoitteluAjo ajo = new SijoitteluAjo();
        ajo.setSijoitteluajoId(sijoitteluajoId);
        ajo.setStartMils(System.currentTimeMillis());
        ajo.setEndMils(System.currentTimeMillis());

        HakukohdeItem item = new HakukohdeItem();
        item.setOid(hakukohdeOid);

        ajo.setHakukohteet(Arrays.asList(item));
        return ajo;
    }

    private HakuDTO hakuDTO() {
        List<ValintatietoValintatapajonoDTO> jonot = Arrays.asList(jonoValinnoistaDTO());
        HakuDTO haku = new HakuDTO();
        haku.setHakuOid(hakuOid);

        HakukohdeDTO hakukohde = new HakukohdeDTO();
        hakukohde.setOid(hakukohdeOid);
        ValintatietoValinnanvaiheDTO vaihe = new ValintatietoValinnanvaiheDTO(1, "1", hakuOid, "vaihe1", new java.util.Date(), jonot, Collections.emptyList());
        hakukohde.setValinnanvaihe(Arrays.asList(vaihe));
        haku.setHakukohteet(Arrays.asList(hakukohde));
        return haku;
    }

    private ValintatietoValintatapajonoDTO jonoValinnoistaDTO() {
        int prioriteetti = 1;

        fi.vm.sade.valintalaskenta.domain.dto.valintatieto.ValintatietoValintatapajonoDTO jono = new fi.vm.sade.valintalaskenta.domain.dto.valintatieto.ValintatietoValintatapajonoDTO();
        jono.setOid(jonoOid);
        jono.setSiirretaanSijoitteluun(true);
        jono.setAktiivinen(true);
        jono.setEiVarasijatayttoa(false);
        jono.setVarasijat(2);
        jono.setAloituspaikat(5);
        jono.setValmisSijoiteltavaksi(true);
        jono.setPoissaOlevaTaytto(true);
        jono.setPrioriteetti(prioriteetti);

        fi.vm.sade.valintalaskenta.domain.dto.HakijaDTO hakija1 = new fi.vm.sade.valintalaskenta.domain.dto.HakijaDTO();
        hakija1.setOid(hakijaOid);
        hakija1.setHakemusOid("peruuntunuthakija");
        hakija1.setJonosija(0);
        hakija1.setPrioriteetti(prioriteetti);
        hakija1.setTila(JarjestyskriteerituloksenTilaDTO.HYVAKSYTTAVISSA);
        jono.setHakija(Arrays.asList(hakija1));

        return jono;
    }

    private List<Hakukohde> valintarekisteriHakukohteetHakemuksenTilalla(HakemuksenTila hakemuksenTila) {
        Hakukohde hakukohde = new Hakukohde();
        hakukohde.setOid(hakukohdeOid);
        hakukohde.setSijoitteluajoId(sijoitteluajoId);
        hakukohde.setValintatapajonot(Arrays.asList(jonoHakemuksenTilalla(hakemuksenTila)));
        return Arrays.asList(hakukohde);
    }

    private Valintatapajono jonoHakemuksenTilalla(HakemuksenTila hakemuksenTila) {
        Valintatapajono jono = new Valintatapajono();
        jono.setOid(hakukohdeOid + ".000000");
        jono.setNimi("jono");
        jono.setAloituspaikat(5);
        jono.setVarasijat(2);
        jono.setEiVarasijatayttoa(false);
        jono.setVarasijojaKaytetaanAlkaen(new java.util.Date(System.currentTimeMillis() - 1000));
        jono.setVarasijojaTaytetaanAsti(new java.util.Date(System.currentTimeMillis() + 1000));
        jono.setPoissaOlevaTaytto(true);
        jono.setTasasijasaanto(Tasasijasaanto.ARVONTA);
        jono.setPrioriteetti(2);

        Hakemus hakemus = new Hakemus();
        hakemus.setHakijaOid(hakijaOid);
        hakemus.setHakemusOid(hakijaOid);
        hakemus.setTila(hakemuksenTila);

        if (hakemuksenTila == HakemuksenTila.PERUUNTUNUT) {
            hakemus.setTilanKuvaukset(TilanKuvaukset.peruuntunutEiMahduKasiteltavienVarasijojenMaaraan());
        }

        if (hakemuksenTila == HakemuksenTila.HYLATTY) {
            hakemus.setTilanKuvaukset(TilanKuvaukset.hylattyHakijaryhmaanKuulumattomana("testiryhmä"));
        }

        hakemus.setJonosija(0);
        hakemus.setPrioriteetti(2);
        jono.setHakemukset(Arrays.asList(hakemus));
        return jono;
    }

    private List<VastaanottoDTO> vastaanototToisessaHakukohteessa() {
        VastaanottoDTO dto = new VastaanottoDTO();
        dto.setHenkiloOid(hakijaOid);
        dto.setHakukohdeOid("jokutoinenhakukohde");
        dto.setAction(ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI);
        return Arrays.asList(dto);
    }

    private void verifyAndCaptureAndAssert(Function3<SijoitteluAjo, List<Hakukohde>, List<Valintatulos>, Boolean> assertFunction) throws Exception {
        verify(valintarekisteriService).getLatestSijoitteluajo(hakuOid);
        verify(valintarekisteriService).getSijoitteluajonHakukohteet(sijoitteluajoId);
        verify(valintarekisteriService).getValintatulokset(hakuOid);

        ArgumentCaptor<SijoitteluAjo> sijoitteluajoCaptor = ArgumentCaptor.forClass(SijoitteluAjo.class);
        ArgumentCaptor<List<Hakukohde>> hakukohteetCaptor = ArgumentCaptor.forClass((Class)List.class);
        ArgumentCaptor<List<Valintatulos>> valintatuloksetCaptor = ArgumentCaptor.forClass((Class)List.class);

        verify(valintarekisteriService).tallennaSijoittelu(sijoitteluajoCaptor.capture(), hakukohteetCaptor.capture(), valintatuloksetCaptor.capture());

        assertFunction.apply(sijoitteluajoCaptor.getValue(), hakukohteetCaptor.getValue(), valintatuloksetCaptor.getValue());
    }
}
