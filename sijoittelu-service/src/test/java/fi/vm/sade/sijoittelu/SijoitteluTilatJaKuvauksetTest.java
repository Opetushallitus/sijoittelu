package fi.vm.sade.sijoittelu;

import com.google.common.collect.Sets;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilanKuvaukset;
import fi.vm.sade.sijoittelu.domain.*;
import fi.vm.sade.sijoittelu.domain.dto.VastaanottoDTO;
import fi.vm.sade.sijoittelu.laskenta.external.resource.VirkailijaValintaTulosServiceResource;
import fi.vm.sade.sijoittelu.laskenta.service.business.ActorService;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.sijoittelu.laskenta.service.business.ValintarekisteriService;
import fi.vm.sade.sijoittelu.laskenta.service.it.TarjontaIntegrationService;
import fi.vm.sade.sijoittelu.tulos.service.impl.converters.SijoitteluTulosConverter;
import fi.vm.sade.sijoittelu.tulos.service.impl.converters.SijoitteluTulosConverterImpl;
import fi.vm.sade.valintalaskenta.domain.dto.HakukohdeDTO;
import fi.vm.sade.valintalaskenta.domain.dto.JarjestyskriteerituloksenTilaDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.ValintatietoValinnanvaiheDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.ValintatietoValintatapajonoDTO;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import rx.functions.Func3;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class SijoitteluTilatJaKuvauksetTest {
    private String hakuOid = "12345";
    private long sijoitteluajoId = 12345l;
    private String uusiHakukohdeOid = "112233";
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
    public void setUp() throws Exception {
        sijoitteluTulosConverter = new SijoitteluTulosConverterImpl();
        actorService = mock(ActorService.class);
        tarjontaIntegrationService = mock(TarjontaIntegrationService.class);
        valintaTulosServiceResource = mock(VirkailijaValintaTulosServiceResource.class);
        valintarekisteriService = mock(ValintarekisteriService.class);

        service = new SijoitteluBusinessService(sijoitteluTulosConverter, actorService, tarjontaIntegrationService, valintaTulosServiceResource, valintarekisteriService);

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
        parametrit.setPH_HKP(new fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriArvoDTO(System.currentTimeMillis() + 1000000));
        parametrit.setPH_VTSSV(new fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriArvoDTO(System.currentTimeMillis() + 1000000));
        parametrit.setPH_VSTP(new fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriArvoDTO(System.currentTimeMillis() + 1000000));
        parametrit.setPH_VSSAV(new fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriArvoDTO(System.currentTimeMillis() - 1000000));
        return parametrit;
    }

    @Test
    public void testSijoittelePeruuntunut() {
        setupMocksPeruuntunut();

        service.sijoittele(hakuDTO(), Collections.emptySet(), Sets.newHashSet("112233.000000"));

        Func3<SijoitteluAjo, List<Hakukohde>, List<Valintatulos>, Boolean> assertFunction = (sijoitteluajo, hakukohteet, valintatulokset) -> {
            Hakemus hakemus = hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().get(0);
            assertEquals("peruuntunuthakija", hakemus.getHakijaOid());
            assertEquals(HakemuksenTila.PERUUNTUNUT, hakemus.getTila());
            assertEquals("Peruuntunut, pisteesi eivät riittäneet varasijaan", hakemus.getTilanKuvaukset().get("FI"));
            return true;
        };

        verifyAndCaptureAndAssert(assertFunction);
    }

    @Test
    public void testSijoittelePerunut() {
        setupMocksPerunut();

        service.sijoittele(hakuDTO(), Collections.emptySet(), Sets.newHashSet("112233.000000"));

        Func3<SijoitteluAjo, List<Hakukohde>, List<Valintatulos>, Boolean> assertFunction = (sijoitteluajo, hakukohteet, valintatulokset) -> {
            Hakemus hakemus = hakukohteet.get(0).getValintatapajonot().get(0).getHakemukset().get(0);
            assertEquals("peruuntunuthakija", hakemus.getHakijaOid());
            assertEquals(HakemuksenTila.PERUNUT, hakemus.getTila());
            assertTrue(hakemus.getTilanKuvaukset().isEmpty());
            return true;
        };

        verifyAndCaptureAndAssert(assertFunction);
    }

    private void setupMocksPeruuntunut() {
        when(valintarekisteriService.getLatestSijoitteluajo(hakuOid)).thenReturn(valintarekisteriSijoitteluajo());
        when(valintarekisteriService.getSijoitteluajonHakukohteet(sijoitteluajoId)).thenReturn(valintarekisteriHakukohteetHakemuksenTilalla(HakemuksenTila.PERUUNTUNUT));
        when(valintaTulosServiceResource.haunKoulutuksenAlkamiskaudenVastaanototYhdenPaikanSaadoksenPiirissa(hakuOid)).thenReturn(vastaanototToisessaHakukohteessa());
    }

    private void setupMocksPerunut() {
        when(valintarekisteriService.getLatestSijoitteluajo(hakuOid)).thenReturn(valintarekisteriSijoitteluajo());
        when(valintarekisteriService.getSijoitteluajonHakukohteet(sijoitteluajoId)).thenReturn(valintarekisteriHakukohteetHakemuksenTilalla(HakemuksenTila.PERUNUT));
        when(valintaTulosServiceResource.haunKoulutuksenAlkamiskaudenVastaanototYhdenPaikanSaadoksenPiirissa(hakuOid)).thenReturn(vastaanototToisessaHakukohteessa());
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
        hakukohde.setOid(uusiHakukohdeOid);
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
}
