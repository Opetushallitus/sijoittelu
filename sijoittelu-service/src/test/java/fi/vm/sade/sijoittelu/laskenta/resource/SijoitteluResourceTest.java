package fi.vm.sade.sijoittelu.laskenta.resource;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.google.common.collect.Sets;

import fi.vm.sade.service.valintaperusteet.dto.HakijaryhmaValintatapajonoDTO;
import fi.vm.sade.service.valintaperusteet.dto.KoodiDTO;
import fi.vm.sade.service.valintaperusteet.dto.ValintatapajonoDTO;
import fi.vm.sade.service.valintaperusteet.dto.model.Tasapistesaanto;
import fi.vm.sade.service.valintaperusteet.resource.ValintalaskentakoostepalveluResource;
import fi.vm.sade.sijoittelu.domain.SijoitteluajonTila;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.sijoittelu.laskenta.util.EnumConverter;
import fi.vm.sade.valintalaskenta.domain.dto.HakijaryhmaDTO;
import fi.vm.sade.valintalaskenta.domain.dto.HakukohdeDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintakoe.Tasasijasaanto;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.ValintatietoValinnanvaiheDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.ValintatietoValintatapajonoDTO;
import fi.vm.sade.valintalaskenta.tulos.service.impl.ValintatietoService;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * @author Jussi Jartamo
 */
public class SijoitteluResourceTest {

    private final SijoitteluResource sijoitteluResource;
    private final SijoitteluBusinessService sijoitteluBusinessService;
    private final ValintatietoService valintatietoService;
    private final ValintalaskentakoostepalveluResource valintalaskentakoostepalveluResource;
    private final SijoitteluBookkeeperService sijoitteluBookkeeperService = new SijoitteluBookkeeperService();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public SijoitteluResourceTest() {
        sijoitteluBusinessService = mock(SijoitteluBusinessService.class);
        valintatietoService = mock(ValintatietoService.class);
        valintalaskentakoostepalveluResource = mock(ValintalaskentakoostepalveluResource.class);
        sijoitteluResource = new SijoitteluResource(
            sijoitteluBusinessService,
            valintatietoService,
            valintalaskentakoostepalveluResource,
            sijoitteluBookkeeperService);
    }

    @Test
    public void testaaSijoittelunLuonti() {
        String haku1 = "1.2.3.4444";
        String haku2 = "1.2.3.555";

        Long id = sijoitteluResource.sijoittele(haku1);
        Long id2 = sijoitteluResource.sijoittele(haku1);

        String tila = sijoitteluResource.sijoittelunTila(id);
        String tila2 = sijoitteluResource.sijoittelunTila(id2);

        assertEquals(Long.valueOf(-1), id2);
        assertThat(id, Matchers.greaterThan(0L));
        assertEquals(SijoitteluajonTila.KESKEN.toString(), tila);
        assertEquals(SijoitteluajonTila.EI_LOYTYNYT.toString(), tila2);
    }

    @Test
    public void testaaSijoitteluReitti() {
        final String hakukohdeOid = UUID.randomUUID().toString();
        final String hakijaryhmaOid = UUID.randomUUID().toString();
        final String valintatapajononHakijaryhmaOid = UUID.randomUUID().toString();
        try {
            ValintatietoValinnanvaiheDTO laskennasta = createValintatietoValinnanvaiheDTO();
            ValintatapajonoDTO valintaperusteista = createValintatapajonoDTO();
            HakijaryhmaValintatapajonoDTO hakijaryhmavalintaperusteista =
                createHakijaryhmaValintatapajonoDTO(hakijaryhmaOid);
            HakijaryhmaValintatapajonoDTO valintatapajononHakijaryhmavalintaperusteista =
                createHakijaryhmaValintatapajonoDTO(valintatapajononHakijaryhmaOid);
            final String valintatapajonoOid = laskennasta.getValintatapajonot().iterator().next().getOid();
            valintaperusteista.setOid(valintatapajonoOid);
            HakuDTO haku = createHakuDTO(
                EMPTY,
                hakukohdeOid,
                createHakijaryhmaDTO(hakijaryhmaOid),
                createHakijaryhmaDTO(valintatapajononHakijaryhmaOid),
                laskennasta);
            {
                when(valintatietoService.haeValintatiedot(anyString())).thenReturn(haku);
                when(valintalaskentakoostepalveluResource.readByHakukohdeOids(anyList()))
                    .thenReturn(asList(hakijaryhmavalintaperusteista));
                when(valintalaskentakoostepalveluResource.readByValintatapajonoOids(anyList()))
                    .thenReturn(asList(valintatapajononHakijaryhmavalintaperusteista));
                final HashMap<String, List<ValintatapajonoDTO>> vpMap = new HashMap<>();
                vpMap.put(hakukohdeOid, Arrays.asList(valintaperusteista));
                when(valintalaskentakoostepalveluResource.haeValintatapajonotSijoittelulle(anyList())).thenReturn(vpMap);
                sijoitteluResource.toteutaSijoittelu(EMPTY, 12345L);
            }
            verify(valintalaskentakoostepalveluResource, times(1)).readByHakukohdeOids(asList(hakukohdeOid));
            verify(valintalaskentakoostepalveluResource, times(1)).readByValintatapajonoOids(asList(valintatapajonoOid));
            verify(valintalaskentakoostepalveluResource, times(1)).haeValintatapajonotSijoittelulle(asList(hakukohdeOid));
            verify(sijoitteluBusinessService, times(1)).sijoittele(haku, new HashSet<>(), Sets.newHashSet(valintatapajonoOid), 12345L);

            HakukohdeDTO hakukohde = haku.getHakukohteet().iterator().next();
            /// ASSERTOIDAAN ETTA JONON TIEDOT PAIVITTYY
            {
                ValintatietoValintatapajonoDTO jono = hakukohde.getValinnanvaihe().iterator().next().getValintatapajonot().iterator().next();
                ValintatietoValintatapajonoDTO alkuperainen = createValintatietoValinnanvaiheDTO().getValintatapajonot().iterator().next();
                assertThat(jono.getAloituspaikat(), is(valintaperusteista.getAloituspaikat()));
                assertNotSame(jono.getAloituspaikat(), alkuperainen.getAloituspaikat());

                assertThat(jono.getEiVarasijatayttoa(), is(valintaperusteista.getEiVarasijatayttoa()));
                assertNotSame(jono.getEiVarasijatayttoa(), alkuperainen.getEiVarasijatayttoa());

                assertThat(jono.getNimi(), is(valintaperusteista.getNimi()));
                assertNotSame(jono.getNimi(), alkuperainen.getNimi());

                assertThat(jono.getTasasijasaanto(), is(EnumConverter
                    .convert(
                        Tasasijasaanto.class, valintaperusteista.getTasapistesaanto())));
                assertNotSame(jono.getTasasijasaanto(), alkuperainen.getTasasijasaanto());
            }
            /// ASSERTOIDAAN ETTA HAKIJARYHMAN TIEDOT PAIVITTYY
            {
                HakijaryhmaDTO hakijaryhma = hakukohde.getHakijaryhma().iterator().next();
                HakijaryhmaDTO alkuperainen = createHakijaryhmaDTO(hakijaryhmaOid);

                assertNotSame(hakijaryhma.isKaytaKaikki(), alkuperainen.isKaytaKaikki());
                assertNotSame(hakijaryhma.isKaytetaanRyhmaanKuuluvia(), alkuperainen.isKaytetaanRyhmaanKuuluvia());
                assertNotSame(hakijaryhma.getKiintio(), alkuperainen.getKiintio());
                assertNotSame(hakijaryhma.getKuvaus(), alkuperainen.getKuvaus());
                assertNotSame(hakijaryhma.getNimi(), alkuperainen.getNimi());
                assertNotSame(hakijaryhma.isTarkkaKiintio(), alkuperainen.isTarkkaKiintio());
                assertNotSame(hakijaryhma.getHakijaryhmatyyppikoodiUri(), alkuperainen.getHakijaryhmatyyppikoodiUri());

                assertThat(hakijaryhma.isKaytaKaikki(), is(hakijaryhmavalintaperusteista.isKaytaKaikki()));
                assertThat(hakijaryhma.isKaytetaanRyhmaanKuuluvia(), is(hakijaryhmavalintaperusteista.isKaytetaanRyhmaanKuuluvia()));
                assertThat(hakijaryhma.getKiintio(), is(hakijaryhmavalintaperusteista.getKiintio()));
                assertThat(hakijaryhma.getKuvaus(), is(hakijaryhmavalintaperusteista.getKuvaus()));
                assertThat(hakijaryhma.getNimi(), is(hakijaryhmavalintaperusteista.getNimi()));
                assertThat(hakijaryhma.isTarkkaKiintio(), is(hakijaryhmavalintaperusteista.isTarkkaKiintio()));
                assertThat(hakijaryhma.getHakijaryhmatyyppikoodiUri(), is(hakijaryhmavalintaperusteista.getHakijaryhmatyyppikoodi().getUri()));

                hakijaryhma = hakukohde.getHakijaryhma().iterator().next();
                alkuperainen = createHakijaryhmaDTO(valintatapajononHakijaryhmaOid);

                assertNotSame(hakijaryhma.isKaytaKaikki(), alkuperainen.isKaytaKaikki());
                assertNotSame(hakijaryhma.isKaytetaanRyhmaanKuuluvia(), alkuperainen.isKaytetaanRyhmaanKuuluvia());
                assertNotSame(hakijaryhma.getKiintio(), alkuperainen.getKiintio());
                assertNotSame(hakijaryhma.getKuvaus(), alkuperainen.getKuvaus());
                assertNotSame(hakijaryhma.getNimi(), alkuperainen.getNimi());
                assertNotSame(hakijaryhma.isTarkkaKiintio(), alkuperainen.isTarkkaKiintio());
                assertNotSame(hakijaryhma.getHakijaryhmatyyppikoodiUri(), alkuperainen.getHakijaryhmatyyppikoodiUri());

                assertThat(hakijaryhma.isKaytaKaikki(), is(hakijaryhmavalintaperusteista.isKaytaKaikki()));
                assertThat(hakijaryhma.isKaytetaanRyhmaanKuuluvia(), is(hakijaryhmavalintaperusteista.isKaytetaanRyhmaanKuuluvia()));
                assertThat(hakijaryhma.getKiintio(), is(hakijaryhmavalintaperusteista.getKiintio()));
                assertThat(hakijaryhma.getKuvaus(), is(hakijaryhmavalintaperusteista.getKuvaus()));
                assertThat(hakijaryhma.getNimi(), is(hakijaryhmavalintaperusteista.getNimi()));
                assertThat(hakijaryhma.isTarkkaKiintio(), is(hakijaryhmavalintaperusteista.isTarkkaKiintio()));
                assertThat(hakijaryhma.getHakijaryhmatyyppikoodiUri(), is(hakijaryhmavalintaperusteista.getHakijaryhmatyyppikoodi().getUri()));
            }
        } finally {
            reset(sijoitteluBusinessService, valintatietoService, valintalaskentakoostepalveluResource);
        }
    }

    @Test
    public void valintaperusteistaKadonneetJonotHuomataanJoTietojaLadatessa() {
        String hakuOid = "hakuOid";
        final String hakukohdeOid = "hakukohdeOid";
        final String hakijaryhmaOid = UUID.randomUUID().toString();
        final String valintatapajononHakijaryhmaOid = UUID.randomUUID().toString();
        try {
            ValintatietoValinnanvaiheDTO laskennasta = createValintatietoValinnanvaiheDTO();
            ValintatapajonoDTO valintaperusteista = createValintatapajonoDTO();
            HakijaryhmaValintatapajonoDTO hakijaryhmavalintaperusteista = createHakijaryhmaValintatapajonoDTO(hakijaryhmaOid);
            HakijaryhmaValintatapajonoDTO valintatapajononHakijaryhmavalintaperusteista =
                createHakijaryhmaValintatapajonoDTO(valintatapajononHakijaryhmaOid);
            ValintatietoValintatapajonoDTO laskennanValintatapajono = laskennasta.getValintatapajonot().iterator().next();
            final String valintatapajonoOid = "valintatapaJonoOid";
            laskennanValintatapajono.setOid(valintatapajonoOid);
            laskennanValintatapajono.setSiirretaanSijoitteluun(true);
            laskennanValintatapajono.setNimi("Varsinainen testivalinta");
            valintaperusteista.setOid(valintatapajonoOid + "-broken");
            HakuDTO haku = createHakuDTO(
                hakuOid,
                hakukohdeOid,
                createHakijaryhmaDTO(hakijaryhmaOid),
                createHakijaryhmaDTO(valintatapajononHakijaryhmaOid),
                laskennasta);
            when(valintatietoService.haeValintatiedot(hakuOid)).thenReturn(haku);
            when(valintalaskentakoostepalveluResource.readByHakukohdeOids(Collections.singletonList(hakukohdeOid)))
                .thenReturn(Collections.singletonList(hakijaryhmavalintaperusteista));
            when(valintalaskentakoostepalveluResource.readByValintatapajonoOids(Collections.singletonList(valintatapajonoOid))).
                thenReturn(Collections.singletonList(valintatapajononHakijaryhmavalintaperusteista));
            final HashMap<String, List<ValintatapajonoDTO>> vpMap = new HashMap<>();
            vpMap.put(hakukohdeOid, Collections.singletonList(valintaperusteista));
            when(valintalaskentakoostepalveluResource.haeValintatapajonotSijoittelulle(Collections.singletonList(hakukohdeOid)))
                .thenReturn(vpMap);

            thrown.expect(IllegalStateException.class);
            thrown.expectMessage("Haun hakuOid sijoittelu : " +
                "Laskennan tuloksista löytyvien jonojen tietoja on kadonnut valintaperusteista: " +
                "[Hakukohde hakukohdeOid , jono \"Varsinainen testivalinta\" (valintatapaJonoOid , prio 0)]");
            sijoitteluResource.toteutaSijoittelu(hakuOid, 12345L);
        } finally{
            reset(sijoitteluBusinessService, valintatietoService, valintalaskentakoostepalveluResource);
        }
    }

    private ValintatapajonoDTO createValintatapajonoDTO() {
        ValintatapajonoDTO valintatapajonoDTO = new ValintatapajonoDTO();
        valintatapajonoDTO.setOid(UUID.randomUUID().toString());
        valintatapajonoDTO.setAktiivinen(true);
        valintatapajonoDTO.setAloituspaikat(Integer.MIN_VALUE);
        valintatapajonoDTO.setautomaattinenSijoitteluunSiirto(true);
        valintatapajonoDTO.setEiVarasijatayttoa(false);
        valintatapajonoDTO.setKaikkiEhdonTayttavatHyvaksytaan(false);
        valintatapajonoDTO.setKaytetaanValintalaskentaa(false);
        valintatapajonoDTO.setKuvaus(UUID.randomUUID().toString());
        valintatapajonoDTO.setNimi(UUID.randomUUID().toString());
        valintatapajonoDTO.setPoissaOlevaTaytto(false);
        valintatapajonoDTO.setSiirretaanSijoitteluun(false);
        valintatapajonoDTO.setTasapistesaanto(Tasapistesaanto.ARVONTA);
        valintatapajonoDTO.setTayttojono(UUID.randomUUID().toString());
        valintatapajonoDTO.setValisijoittelu(false);
        valintatapajonoDTO.setVarasijat(Integer.MIN_VALUE);
        valintatapajonoDTO.setVarasijaTayttoPaivat(Integer.MIN_VALUE);
        valintatapajonoDTO.setVarasijojaKaytetaanAlkaen(new Date());
        valintatapajonoDTO.setVarasijojaTaytetaanAsti(new Date());
        return valintatapajonoDTO;
    }

    private ValintatietoValinnanvaiheDTO createValintatietoValinnanvaiheDTO() {
        ValintatietoValinnanvaiheDTO valintatietoValinnanvaiheDTO = new ValintatietoValinnanvaiheDTO();
        ValintatietoValintatapajonoDTO valintatietoValintatapajonoDTO = new ValintatietoValintatapajonoDTO();
        valintatietoValintatapajonoDTO.setOid(UUID.randomUUID().toString());
        valintatietoValintatapajonoDTO.setAloituspaikat(Integer.MAX_VALUE);
        valintatietoValintatapajonoDTO.setEiVarasijatayttoa(true);
        valintatietoValintatapajonoDTO.setPoissaOlevaTaytto(true);
        valintatietoValintatapajonoDTO.setTasasijasaanto(Tasasijasaanto.ALITAYTTO);
        valintatietoValintatapajonoDTO.setTayttojono(UUID.randomUUID().toString());
        valintatietoValintatapajonoDTO.setVarasijat(Integer.MAX_VALUE);
        valintatietoValintatapajonoDTO.setVarasijaTayttoPaivat(Integer.MAX_VALUE);
        valintatietoValintatapajonoDTO.setVarasijojaKaytetaanAlkaen(new Date());
        valintatietoValintatapajonoDTO.setVarasijojaTaytetaanAsti(new Date());
        valintatietoValintatapajonoDTO.setAktiivinen(true);
        valintatietoValintatapajonoDTO.setKaikkiEhdonTayttavatHyvaksytaan(true);
        valintatietoValintatapajonoDTO.setNimi(UUID.randomUUID().toString());
        valintatietoValinnanvaiheDTO.setValintatapajonot(asList(valintatietoValintatapajonoDTO));
        return valintatietoValinnanvaiheDTO;
    }

    private HakuDTO createHakuDTO(String hakuOid,
                                  String hakukohdeOid,
                                  HakijaryhmaDTO hakijaryhma,
                                  HakijaryhmaDTO valintatapajononHakijaryhma,
                                  ValintatietoValinnanvaiheDTO valinnanvaihe) {
        HakuDTO haku = new HakuDTO();
        haku.setHakuOid(hakuOid);
        HakukohdeDTO hakukohde = new HakukohdeDTO();
        hakukohde.setOid(hakukohdeOid);
        hakukohde.setHakijaryhma(asList(hakijaryhma, valintatapajononHakijaryhma));
        hakukohde.setValinnanvaihe(asList(valinnanvaihe));
        haku.setHakukohteet(asList(hakukohde));
        return haku;
    }

    private HakijaryhmaDTO createHakijaryhmaDTO(String hakijaryhmaOid) {
        HakijaryhmaDTO hakijaryhma = new HakijaryhmaDTO();
        hakijaryhma.setHakijaryhmaOid(hakijaryhmaOid);
        hakijaryhma.setKiintio(0);
        hakijaryhma.setKaytetaanRyhmaanKuuluvia(false);
        hakijaryhma.setKaytaKaikki(false);
        hakijaryhma.setKuvaus(EMPTY);
        hakijaryhma.setNimi(EMPTY);
        hakijaryhma.setTarkkaKiintio(false);
        hakijaryhma.setHakijaryhmatyyppikoodiUri(EMPTY);
        return hakijaryhma;
    }
    private HakijaryhmaValintatapajonoDTO createHakijaryhmaValintatapajonoDTO(String hakijaryhmaOid) {
        HakijaryhmaValintatapajonoDTO hakijaryhmaValintatapajonoDTO = new HakijaryhmaValintatapajonoDTO();
        hakijaryhmaValintatapajonoDTO.setOid(hakijaryhmaOid);
        hakijaryhmaValintatapajonoDTO.setAktiivinen(true);
        hakijaryhmaValintatapajonoDTO.setKaytaKaikki(true);
        hakijaryhmaValintatapajonoDTO.setKaytetaanRyhmaanKuuluvia(true);
        hakijaryhmaValintatapajonoDTO.setKiintio(Integer.MAX_VALUE);
        hakijaryhmaValintatapajonoDTO.setKuvaus(UUID.randomUUID().toString());
        hakijaryhmaValintatapajonoDTO.setNimi(UUID.randomUUID().toString());
        hakijaryhmaValintatapajonoDTO.setTarkkaKiintio(true);
        KoodiDTO koodi = new KoodiDTO();
        koodi.setUri("koodi_uri");
        hakijaryhmaValintatapajonoDTO.setHakijaryhmatyyppikoodi(koodi);
        return hakijaryhmaValintatapajonoDTO;
    }
}
