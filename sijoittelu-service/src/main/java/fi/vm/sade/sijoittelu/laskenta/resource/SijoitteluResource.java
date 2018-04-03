package fi.vm.sade.sijoittelu.laskenta.resource;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import fi.vm.sade.service.valintaperusteet.dto.HakijaryhmaValintatapajonoDTO;
import fi.vm.sade.service.valintaperusteet.dto.ValintatapajonoCreateDTO;
import fi.vm.sade.service.valintaperusteet.dto.ValintatapajonoDTO;
import fi.vm.sade.service.valintaperusteet.resource.ValintalaskentakoostepalveluResource;
import fi.vm.sade.sijoittelu.domain.SijoitteluajonTila;
import fi.vm.sade.sijoittelu.laskenta.service.business.SijoitteluBusinessService;
import fi.vm.sade.sijoittelu.laskenta.util.EnumConverter;
import fi.vm.sade.valintalaskenta.domain.dto.HakukohdeDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintakoe.Tasasijasaanto;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.HakuDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.ValintatietoValinnanvaiheDTO;
import fi.vm.sade.valintalaskenta.domain.dto.valintatieto.ValintatietoValintatapajonoDTO;
import fi.vm.sade.valintalaskenta.tulos.service.impl.ValintatietoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import javax.ws.rs.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;

@Path("sijoittele")
@Controller
// @PreAuthorize("isAuthenticated()")
@Api(value = "sijoittele", description = "Resurssi sijoitteluun")
public class SijoitteluResource {
    private final static Logger LOGGER = LoggerFactory.getLogger(SijoitteluResource.class);

    private final SijoitteluBusinessService sijoitteluBusinessService;
    private final ValintatietoService valintatietoService;
    private final ValintalaskentakoostepalveluResource valintalaskentakoostepalveluResource;
    private final SijoitteluBookkeeperService sijoitteluBookkeeperService;

    @Autowired
    public SijoitteluResource(SijoitteluBusinessService sijoitteluBusinessService,
                              ValintatietoService valintatietoService,
                              ValintalaskentakoostepalveluResource valintalaskentakoostepalveluResource,
                              SijoitteluBookkeeperService sijoitteluBookkeeperService) {
        this.sijoitteluBusinessService = sijoitteluBusinessService;
        this.valintatietoService = valintatietoService;
        this.valintalaskentakoostepalveluResource = valintalaskentakoostepalveluResource;
        this.sijoitteluBookkeeperService = sijoitteluBookkeeperService;
    }

    @GET
    @Path("/ajontila/{sijoitteluId}")
    @ApiOperation(value = "Sijoitteluajon tila", response = String.class)
    public String sijoittelunTila(@PathParam("sijoitteluId") Long id) {
        String tila = sijoitteluBookkeeperService.getSijoitteluAjonTila(id);
        LOGGER.info("/ajontila/sijoitteluId: Palautetaan sijoitteluajolle {} tila {}", id, tila);
        return tila;
    }

    @GET
    @Path("/{hakuOid}")
    @ApiOperation(value = "Käynnistä uusi sijoittelu haulle", response = Long.class)
    public Long sijoittele(@PathParam("hakuOid") String hakuOid) {

        Long sijoitteluAjonTunniste = System.currentTimeMillis();
        if (!sijoitteluBookkeeperService.luoUusiSijoitteluAjo(hakuOid, sijoitteluAjonTunniste)) {
            LOGGER.warn("Uuden sijoittelun luominen haulle {} ei onnistunut, luultavasti siksi että edellinen oli vielä KESKEN", hakuOid);
            return -1L;
        } else {
            LOGGER.info("Luodaan ja käynnistetään uusi sijoittelu haulle {}", hakuOid);
            try {
                Runnable kaynnistaSijoittelu = () -> toteutaSijoittelu(hakuOid, sijoitteluAjonTunniste);
                Thread sijoittelu = new Thread(kaynnistaSijoittelu);
                sijoittelu.start();
                return sijoitteluAjonTunniste;
            } catch (Exception e) {
                LOGGER.error("Virhe sijoittelun suorituksessa, ", e);
                throw e;
            }
        }
    }

    private static Set<String> flatMapJonoOids(Map<String, Map<String, ValintatapajonoDTO>> mapToMapOfObjects) {
        return unmodifiableSet(mapToMapOfObjects.values().stream()
                .flatMap(map -> map.values().stream().map(ValintatapajonoDTO::getOid))
                .collect(toSet()));
    }

    private void updateValintatapajonotAndRemoveUsed(Map<String, ValintatapajonoDTO> valintatapajonoByOid, ValintatietoValinnanvaiheDTO vaihe) {
        List<ValintatietoValintatapajonoDTO> konvertoidut = new ArrayList<>();
        vaihe.getValintatapajonot().forEach(jono -> {
            if (valintatapajonoByOid.containsKey(jono.getOid())
                    && jono.getValmisSijoiteltavaksi()
                    && jono.getAktiivinen()) {

                ValintatapajonoDTO perusteJono = valintatapajonoByOid.get(jono.getOid());
                jono.setAloituspaikat(perusteJono.getAloituspaikat());
                jono.setEiVarasijatayttoa(perusteJono.getEiVarasijatayttoa());
                jono.setPoissaOlevaTaytto(perusteJono.getPoissaOlevaTaytto());
                jono.setTasasijasaanto(EnumConverter.convert(Tasasijasaanto.class, perusteJono.getTasapistesaanto()));
                jono.setTayttojono(perusteJono.getTayttojono());
                jono.setVarasijat(perusteJono.getVarasijat());
                jono.setVarasijaTayttoPaivat(perusteJono.getVarasijaTayttoPaivat());
                jono.setVarasijojaKaytetaanAlkaen(perusteJono.getVarasijojaKaytetaanAlkaen());
                jono.setVarasijojaTaytetaanAsti(perusteJono.getVarasijojaTaytetaanAsti());
                jono.setAktiivinen(perusteJono.getAktiivinen());
                jono.setKaikkiEhdonTayttavatHyvaksytaan(perusteJono.getKaikkiEhdonTayttavatHyvaksytaan());
                jono.setNimi(perusteJono.getNimi());
                jono.setPrioriteetti(perusteJono.getPrioriteetti());
                konvertoidut.add(jono);
                valintatapajonoByOid.remove(jono.getOid());
            }
        });
        vaihe.setValintatapajonot(konvertoidut);
    }

    private void updateHakijaRyhmat(Map<String, HakijaryhmaValintatapajonoDTO> hakijaryhmaByOid, HakukohdeDTO hakukohde) {
        ofNullable(hakukohde.getHakijaryhma()).orElse(emptyList()).forEach(hakijaryhma -> {
                    if (hakijaryhma != null && hakijaryhmaByOid.containsKey(hakijaryhma.getHakijaryhmaOid())) {
                        HakijaryhmaValintatapajonoDTO h = hakijaryhmaByOid.get(hakijaryhma.getHakijaryhmaOid());
                        hakijaryhma.setKaytaKaikki(h.isKaytaKaikki());
                        hakijaryhma.setKaytetaanRyhmaanKuuluvia(h.isKaytetaanRyhmaanKuuluvia());
                        hakijaryhma.setKiintio(h.getKiintio());
                        hakijaryhma.setKuvaus(h.getKuvaus());
                        hakijaryhma.setNimi(h.getNimi());
                        hakijaryhma.setTarkkaKiintio(h.isTarkkaKiintio());
                        hakijaryhma.setPrioriteetti(h.getPrioriteetti());
                        hakijaryhma.setHakijaryhmatyyppikoodiUri(h.getHakijaryhmatyyppikoodi() != null ? h.getHakijaryhmatyyppikoodi().getUri() : null);
                    }
                }
        );
    }

    private Map<String, HakijaryhmaValintatapajonoDTO> haeMahdollisestiMuuttuneetHakijaryhmat(HakuDTO haku) {
        Set<String> hakukohdeOidsWithHakijaryhma = haku.getHakukohteet().stream()
                .filter(hakukohde -> hakukohde.getHakijaryhma() != null && !hakukohde.getHakijaryhma().isEmpty())
                .map(hakukohde -> hakukohde.getOid()).collect(toSet());
        Set<String> valintatapajonoOidsWithHakijaryhma = haku.getHakukohteet().stream()
                .filter(hakukohde -> hakukohde.getHakijaryhma() != null && !hakukohde.getHakijaryhma().isEmpty())
                .flatMap(hakukohde -> hakukohde.getValinnanvaihe().stream())
                .flatMap(valinnanvaihe -> valinnanvaihe.getValintatapajonot().stream())
                .map(jono -> jono.getOid()).collect(toSet());

        Map<String, HakijaryhmaValintatapajonoDTO> hakijaryhmaByOid = Collections.emptyMap();
        if (!hakukohdeOidsWithHakijaryhma.isEmpty()) {
            LOGGER.info("Haetaan hakijaryhmät sijoittelua varten");
            try {
                LOGGER.info("Haetaan hakukohdekohtaiset hakijaryhmät sijoittelua");
                hakijaryhmaByOid =
                        valintalaskentakoostepalveluResource.readByHakukohdeOids(Lists.newArrayList(hakukohdeOidsWithHakijaryhma))
                                .stream()
                                        // Valintaperusteet pitaisi palauttaa vain aktiivisia mutta filtteroidaan varmuuden vuoksi
                                .filter(v -> TRUE.equals(v.getAktiivinen()))
                                .collect(Collectors.toMap(v -> v.getOid(), v -> v));

                LOGGER.info("Haetaan valintatapajonokohtaiset hakijaryhmät sijoittelua varten");
                Map<String, HakijaryhmaValintatapajonoDTO> valintatapajonojenHakijaryhmaByOid =
                        valintalaskentakoostepalveluResource.readByValintatapajonoOids(Lists.newArrayList(valintatapajonoOidsWithHakijaryhma))
                                .stream()
                                // Valintaperusteet pitaisi palauttaa vain aktiivisia mutta filtteroidaan varmuuden vuoksi
                                .filter(v -> TRUE.equals(v.getAktiivinen()))
                                .collect(Collectors.toMap(v -> v.getOid(), v -> v));

                hakijaryhmaByOid.putAll(valintatapajonojenHakijaryhmaByOid);

            } catch (Exception e) {
                LOGGER.error("Hakijaryhmien hakeminen epäonnistui virheeseen!", e);
                throw e;
            }
            LOGGER.info("Saatiin haun {} {}:lle hakukohteelle yhteensä {} aktiivista hakijaryhmää", haku.getHakuOid(), haku.getHakukohteet().size(),
                    hakijaryhmaByOid.size());
        }
        return hakijaryhmaByOid;
    }

    private Map<String, List<ValintatapajonoDTO>> haeValintatapajonotSijoittelulle(HakuDTO haku) {
        final Set<String> aktiivisiaJonojaSisaltavienKohteidenOidit = haku.filtteroiOiditHakukohteilleJoillaOnAktiivisiaJonoja();
        if (!aktiivisiaJonojaSisaltavienKohteidenOidit.isEmpty()) {
            LOGGER.info(String.format("Haetaan valintaperusteista valintatapajonoja sijoittelua varten haun " +
                "%s %s:lle hakukohteelle, jotka löytyvät laskennan tuloksista",
                haku.getHakuOid(), aktiivisiaJonojaSisaltavienKohteidenOidit.size()));
            try {
                Map<String, List<ValintatapajonoDTO>> valintaperusteidenJonotHakukohteittain =
                    valintalaskentakoostepalveluResource.haeValintatapajonotSijoittelulle(new ArrayList<>(aktiivisiaJonojaSisaltavienKohteidenOidit));
                assertKaikkiLaskennanTuloksistaLoytyvatSijoittelunSiirrettavatJonotLoytyvatValintaPerusteista(haku, valintaperusteidenJonotHakukohteittain);
                return valintaperusteidenJonotHakukohteittain;
            } catch (Exception e) {
                LOGGER.error("Valintatapajonojen hakeminen epäonnistui virheeseen!", e);
                throw e;
            }
        } else {
            return Collections.emptyMap();
        }
    }

    private void assertKaikkiLaskennanTuloksistaLoytyvatSijoittelunSiirrettavatJonotLoytyvatValintaPerusteista(HakuDTO hakuDTO, Map<String, List<ValintatapajonoDTO>> valintaperusteidenJonotHakukohteittain) {
        Set<String> laskennanTulostenJonoOidit = hakuDTO.getHakukohteet().stream().flatMap(hk ->
            hk.getValinnanvaihe().stream().flatMap(vv ->
                vv.getValintatapajonot().stream()
                    .filter(fi.vm.sade.valintalaskenta.domain.dto.ValintatapajonoDTO::isSiirretaanSijoitteluun)
                    .map(fi.vm.sade.valintalaskenta.domain.dto.ValintatapajonoDTO::getOid))).collect(toSet());
        Set<String> valintaperusteidenJonoOidit = valintaperusteidenJonotHakukohteittain.values().stream().flatMap(js ->
            js.stream().map(ValintatapajonoDTO::getOid)).collect(toSet());
        Sets.SetView<String> valintaperusteistaKadonneetJonoOidit = Sets.difference(laskennanTulostenJonoOidit, valintaperusteidenJonoOidit);

        if (valintaperusteistaKadonneetJonoOidit.isEmpty()) {
            return;
        }

        List<String> poistuneidenJonojenTiedot = valintaperusteistaKadonneetJonoOidit.stream().map(jonoOid -> {
            Predicate<ValintatietoValintatapajonoDTO> withJonoOid = j -> j.getOid().equals(jonoOid);
            HakukohdeDTO poistuneenJononHakukohde = (hakuDTO.getHakukohteet()).stream()
                .filter(h -> h.getValinnanvaihe().stream().anyMatch(vv -> vv.getValintatapajonot().stream().anyMatch(withJonoOid))).findFirst().get();
            ValintatietoValintatapajonoDTO poistunutJono = poistuneenJononHakukohde.getValinnanvaihe().stream().flatMap(vv -> vv.getValintatapajonot().stream().filter(withJonoOid)).findFirst().get();
            return String.format("Hakukohde %s , jono \"%s\" (%s , prio %d)", poistuneenJononHakukohde.getOid(),
                poistunutJono.getNimi(), poistunutJono.getOid(), poistunutJono.getPrioriteetti());
        }).collect(Collectors.toList());
        throw new IllegalStateException(String.format("Haun %s sijoittelu : Laskennan tuloksista löytyvien jonojen tietoja on kadonnut valintaperusteista: %s",
            hakuDTO.getHakuOid(), poistuneidenJonojenTiedot));
    }

    private Map<String, Map<String, ValintatapajonoDTO>> filtteroiAktiivisetJonotMappiinOideittain(Map<String, List<ValintatapajonoDTO>> valintatapajonotSijoittelulle) {
        return valintatapajonotSijoittelulle
            .entrySet()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, v -> {
                Map<String, ValintatapajonoDTO> jonot =
                        v.getValue().stream()
                                .filter(ValintatapajonoCreateDTO::getAktiivinen)
                                .collect(Collectors.toMap(ValintatapajonoDTO::getOid, v0 -> v0));
                return jonot;
            }));
    }

    public void toteutaSijoittelu(String hakuOid, Long sijoitteluAjonTunniste) {

        try {
            LOGGER.info("Valintatietoja valmistetaan haulle {}!", hakuOid);
            HakuDTO haku = valintatietoService.haeValintatiedot(hakuOid);
            LOGGER.info("Valintatiedot haettu serviceltä {}!", hakuOid);
            LOGGER.info("Asetetaan valintaperusteet {}!", hakuOid);

            final Map<String, HakijaryhmaValintatapajonoDTO> hakijaryhmaByOid = haeMahdollisestiMuuttuneetHakijaryhmat(haku);
            final Map<String, List<ValintatapajonoDTO>> jonotSijoittelulleHakukohdeOideittain = haeValintatapajonotSijoittelulle(haku);
            final Map<String, Map<String, ValintatapajonoDTO>> hakukohdeMapToValintatapajonoByOid = Maps.newHashMap(filtteroiAktiivisetJonotMappiinOideittain(jonotSijoittelulleHakukohdeOideittain));
            final Set<String> laskennanTuloksistaJaValintaperusteistaLoytyvatJonot = jonotSijoittelulleHakukohdeOideittain.values().stream()
                    .flatMap(Collection::stream)
                    .map(ValintatapajonoDTO::getOid)
                    .collect(Collectors.toSet());

            haku.getHakukohteet().forEach(hakukohde -> {
                updateHakijaRyhmat(hakijaryhmaByOid, hakukohde);
                Map<String, ValintatapajonoDTO> valintatapajonoByOid = hakukohdeMapToValintatapajonoByOid.getOrDefault(hakukohde.getOid(), new HashMap<>());
                hakukohde.getValinnanvaihe().forEach(vaihe -> {
                    updateValintatapajonotAndRemoveUsed(valintatapajonoByOid, vaihe);
                });
                if (!valintatapajonoByOid.isEmpty()) {
                    LOGGER.warn("Kaikkia jonoja ei ole sijoiteltu {} hakukohteessa {}: {}", hakuOid, hakukohde.getOid(), valintatapajonoByOid.keySet());
                    hakukohde.setKaikkiJonotSijoiteltu(false);
                }
            });
            LOGGER.info("Valintaperusteet asetettu {}!", hakuOid);

            sijoitteluBusinessService.sijoittele(haku,
                flatMapJonoOids(hakukohdeMapToValintatapajonoByOid),
                laskennanTuloksistaJaValintaperusteistaLoytyvatJonot,
                sijoitteluAjonTunniste);
            LOGGER.info("Sijoitteluajo {} suoritettu onnistuneesti haulle {}", sijoitteluAjonTunniste, hakuOid);
            sijoitteluBookkeeperService.merkitseSijoitteluAjonTila(hakuOid, sijoitteluAjonTunniste, SijoitteluajonTila.VALMIS);
        } catch (Throwable t) {
            LOGGER.error("Sijoitteluajo (tunniste " + sijoitteluAjonTunniste +") epäonnistui haulle " + hakuOid + " : " + t.getMessage(), t);
            sijoitteluBookkeeperService.merkitseSijoitteluAjonTila(hakuOid, sijoitteluAjonTunniste, SijoitteluajonTila.VIRHE);
            throw t;
        }
    }

}
