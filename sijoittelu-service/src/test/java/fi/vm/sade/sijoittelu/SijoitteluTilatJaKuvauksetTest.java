package fi.vm.sade.sijoittelu;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.google.common.collect.Sets;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluConfiguration;
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
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluajoResourcesLoader;
import fi.vm.sade.sijoittelu.laskenta.service.business.ValintarekisteriService;
import fi.vm.sade.sijoittelu.laskenta.service.it.Haku;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.time.Instant;
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
    private TarjontaIntegrationService tarjontaIntegrationService;
    private VirkailijaValintaTulosServiceResource valintaTulosServiceResource;
    private ValintarekisteriService valintarekisteriService;

    @BeforeEach
    public void setUp() {
        sijoitteluTulosConverter = new SijoitteluTulosConverterImpl();
        tarjontaIntegrationService = mock(TarjontaIntegrationService.class);
        valintaTulosServiceResource = mock(VirkailijaValintaTulosServiceResource.class);
        valintarekisteriService = mock(ValintarekisteriService.class);

        service = new SijoitteluBusinessService(
            sijoitteluTulosConverter,
            valintaTulosServiceResource,
            valintarekisteriService,
            new SijoitteluConfiguration(),
            new SijoitteluajoResourcesLoader(tarjontaIntegrationService, valintarekisteriService));

        Instant now = Instant.now();
        when(tarjontaIntegrationService.getHaku(hakuOid)).thenReturn(new Haku(
                "hakuOid",
                "haunkohdejoukko_12#1",
                null,
                true,
                now,
                now.minus(Duration.ofDays(1)),
                now.plus(Duration.ofDays(1)),
                now.plus(Duration.ofDays(1)),
                "",
                true
        ));
    }

    @Test
    public void testSijoitteleHakemuksenTilallaPeruuntunut() throws Exception {
        setupMocksHakemuksenTilaPeruuntunut();

        service.sijoittele(hakuDTO(), Collections.emptySet(), Sets.newHashSet("112233.000000"), 1234567890L, Collections.emptyMap());

        AssertFunction assertFunction = (sijoitteluajo, hakukohteet, valintatulokset) -> {
            Hakemus hakemus = hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().get(0);
            Assertions.assertEquals("peruuntunuthakija", hakemus.getHakijaOid());
            Assertions.assertEquals(HakemuksenTila.PERUUNTUNUT, hakemus.getTila());
            Assertions.assertEquals("Peruuntunut, pisteesi eivät riittäneet varasijaan", hakemus.getTilanKuvaukset().get("FI"));
            return true;
        };

        verifyAndCaptureAndAssert(assertFunction);
    }

    @Test
    public void testSijoitteleValintatuloksenTilallaPerunut() throws Exception {
        setupMocksValintatuloksenTilaPerunut();

        service.sijoittele(hakuDTO(), Collections.emptySet(), Sets.newHashSet("112233.000000"), 1234567890L, Collections.emptyMap());

        AssertFunction assertFunction = (sijoitteluajo, hakukohteet, valintatulokset) -> {
            Hakemus hakemus = hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().get(0);
            Assertions.assertEquals("peruuntunuthakija", hakemus.getHakijaOid());
            Assertions.assertEquals(HakemuksenTila.PERUNUT, hakemus.getTila());
            Assertions.assertTrue(hakemus.getTilanKuvaukset().isEmpty());
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
        service.sijoittele(hakuDTO, Collections.emptySet(), Sets.newHashSet("112233.000000"), 1234567890L, Collections.emptyMap());

        AssertFunction assertFunction = (sijoitteluajo, hakukohteet, valintatulokset) -> {
            Hakemus hakemus = hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().get(0);
            Assertions.assertEquals("peruuntunuthakija", hakemus.getHakijaOid());
            Assertions.assertEquals(HakemuksenTila.HYLATTY, hakemus.getTila());
            Assertions.assertEquals("Ei hakukelpoinen", hakemus.getTilanKuvaukset().get("FI"));
            Assertions.assertEquals("Inte ansökningsbehörig", hakemus.getTilanKuvaukset().get("SV"));
            Assertions.assertEquals("Not eligible", hakemus.getTilanKuvaukset().get("EN"));
            return true;
        };

        verifyAndCaptureAndAssert(assertFunction);
    }

    private void setupMocksHakemuksenTilaPeruuntunut() {
        when(valintarekisteriService.getLatestSijoitteluajo(hakuOid)).thenReturn(valintarekisteriSijoitteluajo());
        when(valintarekisteriService.getSijoitteluajonHakukohteet(sijoitteluajoId, hakuOid)).thenReturn(valintarekisteriHakukohteetHakemuksenTilalla(HakemuksenTila.PERUUNTUNUT));
        when(valintaTulosServiceResource.haunKoulutuksenAlkamiskaudenVastaanototYhdenPaikanSaadoksenPiirissa(hakuOid)).thenReturn(vastaanototToisessaHakukohteessa());
    }

    private void setupMocksValintatuloksenTilaPerunut() {
        when(valintarekisteriService.getLatestSijoitteluajo(hakuOid)).thenReturn(valintarekisteriSijoitteluajo());
        when(valintarekisteriService.getSijoitteluajonHakukohteet(sijoitteluajoId, hakuOid)).thenReturn(valintarekisteriHakukohteetHakemuksenTilalla(HakemuksenTila.HYVAKSYTTY));
        when(valintaTulosServiceResource.haunKoulutuksenAlkamiskaudenVastaanototYhdenPaikanSaadoksenPiirissa(hakuOid)).thenReturn(vastaanototToisessaHakukohteessa());

        Valintatulos valintatulos = new Valintatulos(jonoOid, hakijaOid, hakukohdeOid, hakijaOid, hakuOid, 0);
        valintatulos.setTila(ValintatuloksenTila.PERUNUT, "selite", "muokkaaja");
        List<Valintatulos> mockValintatulokset = Arrays.asList(valintatulos);
        when(valintarekisteriService.getValintatulokset(hakuOid)).thenReturn(mockValintatulokset);
    }

    private void setupMocksHakemuksenTilaHylatty() {
        when(valintarekisteriService.getLatestSijoitteluajo(hakuOid)).thenReturn(valintarekisteriSijoitteluajo());
        when(valintarekisteriService.getSijoitteluajonHakukohteet(sijoitteluajoId, hakuOid)).thenReturn(valintarekisteriHakukohteetHakemuksenTilalla(HakemuksenTila.HYLATTY));
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
            hakemus.setTilanKuvaukset(TilanKuvaukset.peruuntunutEiMahduKasiteltavienVarasijojenMaaraan);
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

    public interface AssertFunction {
        Boolean apply(SijoitteluAjo sijoitteluAjo, List<Hakukohde> hakukohde, List<Valintatulos> valintatulos) throws Exception;
    }

    private void verifyAndCaptureAndAssert(AssertFunction assertFunction) throws Exception {
        verify(valintarekisteriService).getLatestSijoitteluajo(hakuOid);
        verify(valintarekisteriService).getSijoitteluajonHakukohteet(sijoitteluajoId, hakuOid);
        verify(valintarekisteriService).getValintatulokset(hakuOid);

        ArgumentCaptor<SijoitteluAjo> sijoitteluajoCaptor = ArgumentCaptor.forClass(SijoitteluAjo.class);
        ArgumentCaptor<List<Hakukohde>> hakukohteetCaptor = ArgumentCaptor.forClass((Class)List.class);
        ArgumentCaptor<List<Valintatulos>> valintatuloksetCaptor = ArgumentCaptor.forClass((Class)List.class);

        verify(valintarekisteriService).tallennaSijoittelu(sijoitteluajoCaptor.capture(), hakukohteetCaptor.capture(), valintatuloksetCaptor.capture());

        assertFunction.apply(sijoitteluajoCaptor.getValue(), hakukohteetCaptor.getValue(), valintatuloksetCaptor.getValue());
    }
}
