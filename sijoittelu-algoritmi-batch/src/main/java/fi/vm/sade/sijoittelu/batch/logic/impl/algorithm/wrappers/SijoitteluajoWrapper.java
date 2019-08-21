package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers;

import static fi.vm.sade.sijoittelu.domain.ValintatuloksenTila.EHDOLLISESTI_VASTAANOTTANUT;
import static fi.vm.sade.sijoittelu.domain.ValintatuloksenTila.KESKEN;
import static fi.vm.sade.sijoittelu.domain.ValintatuloksenTila.VASTAANOTTANUT_SITOVASTI;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.SijoitteluConfiguration;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.hakukohteet.LisapaikkaTapa;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilaTaulukot;
import fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.util.TilojenMuokkaus;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.TilaHistoria;
import fi.vm.sade.sijoittelu.domain.TilanKuvaukset;
import fi.vm.sade.sijoittelu.domain.Valintatapajono;
import fi.vm.sade.sijoittelu.domain.ValintatuloksenTila;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import fi.vm.sade.sijoittelu.domain.dto.VastaanottoDTO;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SijoitteluajoWrapper {
    private static final Logger LOG = LoggerFactory.getLogger(SijoitteluajoWrapper.class);
    public static final String VALUE_FOR_HASH_FUNCTION_WHEN_UNDEFINED = "undefined"; // määrittelemättömän arvon syöte hash-funktioon

    public final SijoitteluConfiguration sijoitteluConfiguration;

    private SijoitteluAjo sijoitteluajo;

    private List<HakukohdeWrapper> hakukohteet = new ArrayList<HakukohdeWrapper>();

    private Optional<List<Hakukohde>> edellisenSijoittelunHakukohteet = Optional.empty();

    private List<Valintatulos> muuttuneetValintatulokset = new ArrayList<>();

    private final LocalDateTime today = LocalDateTime.now();

    private LocalDateTime kaikkiKohteetSijoittelussa = LocalDateTime.now().minusDays(1);

    private LocalDateTime varasijaSaannotAstuvatVoimaan = LocalDateTime.now().minusDays(1);

    private LocalDateTime varasijaTayttoPaattyy = LocalDateTime.now().plusYears(100);

    private LocalDateTime hakuKierrosPaattyy = LocalDateTime.now().plusYears(100);

    private boolean isKKHaku = false;

    private boolean isAmkopeHaku = false;

    private LisapaikkaTapa lisapaikkaTapa = LisapaikkaTapa.EI_KAYTOSSA;

    private boolean hakutoiveidenPriorisointi = true;

    public SijoitteluajoWrapper(SijoitteluConfiguration sijoitteluConfiguration, final SijoitteluAjo sijoitteluAjo) {
        this.sijoitteluConfiguration = sijoitteluConfiguration;
        this.sijoitteluajo = sijoitteluAjo;
    }

    public List<HakukohdeWrapper> getHakukohteet() {
        return hakukohteet;
    }

    public void setHakukohteet(List<HakukohdeWrapper> hakukohteet) {
        this.hakukohteet = hakukohteet;
    }

    public SijoitteluAjo getSijoitteluajo() {
        return sijoitteluajo;
    }

    public List<Valintatulos> getMuuttuneetValintatulokset() {
        return muuttuneetValintatulokset;
    }

    public void setMuuttuneetValintatulokset(List<Valintatulos> muuttuneetValintatulokset) {
        this.muuttuneetValintatulokset = muuttuneetValintatulokset;
    }

    public LocalDateTime getToday() {
        return today;
    }

    public LocalDateTime getKaikkiKohteetSijoittelussa() {
        return kaikkiKohteetSijoittelussa;
    }

    public void setKaikkiKohteetSijoittelussa(LocalDateTime kaikkiKohteetSijoittelussa) {
        this.kaikkiKohteetSijoittelussa = kaikkiKohteetSijoittelussa;
    }

    public boolean paivamaaraOhitettu() {
        return today.isAfter(kaikkiKohteetSijoittelussa);
    }

    public boolean varasijaSaannotVoimassa() {
        return today.isAfter(varasijaSaannotAstuvatVoimaan);
    }

    public boolean hakukierrosOnPaattynyt() {
        return today.isAfter(hakuKierrosPaattyy);
    }

    public boolean isKKHaku() {
        return isKKHaku;
    }

    public void setKKHaku(boolean isKKHaku) {
        this.isKKHaku = isKKHaku;
    }

    public boolean isAmkopeHaku() {
        return isAmkopeHaku;
    }

    public void setAmkopeHaku(boolean isAmkopeHaku) {
        this.isAmkopeHaku = isAmkopeHaku;
    }

    public LocalDateTime getVarasijaSaannotAstuvatVoimaan() {
        return varasijaSaannotAstuvatVoimaan;
    }

    public void setVarasijaSaannotAstuvatVoimaan(LocalDateTime varasijaSaannotAstuvatVoimaan) {
        this.varasijaSaannotAstuvatVoimaan = varasijaSaannotAstuvatVoimaan;
    }

    public LocalDateTime getVarasijaTayttoPaattyy() {
        return varasijaTayttoPaattyy;
    }

    public void setVarasijaTayttoPaattyy(LocalDateTime varasijaTayttoPaattyy) {
        this.varasijaTayttoPaattyy = varasijaTayttoPaattyy;
    }

    public LocalDateTime getHakuKierrosPaattyy() {
        return hakuKierrosPaattyy;
    }

    public void setHakuKierrosPaattyy(LocalDateTime hakuKierrosPaattyy) {
        this.hakuKierrosPaattyy = hakuKierrosPaattyy;
    }

    private static final HashFunction MD5 = Hashing.md5(); // hieman nopeampi kuin SHA1 ja yhta tarkoitukseen sopiva

    public HashCode asHash() {
        return asHash(MD5);
    }

    private HashCode asHash(HashFunction hashFunction) {
        return jarjestettyValivaiheellinenHashStrategia(hashFunction);
    }

    public Stream<Hakukohde> sijoitteluAjonHakukohteet() {
        return hakukohteet.stream().map(v -> v.getHakukohde()).filter(Objects::nonNull).distinct();
    }

    public String getSijoitteluAjoId() {
        return (sijoitteluajo.getSijoitteluajoId() == null) ? "?" : sijoitteluajo.getSijoitteluajoId().toString();
    }

    private final String VALUE_DELIMETER_HAKUKOHDE = "_HAKUKOHDE_";
    private final String VALUE_DELIMETER_VALINTATULOKSET = "_VALINTATULOKSET_";

    private HashCode jarjestettyValivaiheellinenHashStrategia(HashFunction hashFunction) {
        long t0 = System.currentTimeMillis();
        final Hasher hasher = hashFunction.newHasher();
        // Jokaiselle hakukohteelle oma hasher ja yhdistetaan hash-arvot lopuksi
        // jolloin voidaan seurata yksittaisten hakukohteiden muuttumista
        Supplier<Hasher> hashSupplier = () -> hashFunction.newHasher();
        hakukohteet.stream().sorted().forEach(h -> {
            Hasher hakemuksetHasher = hashSupplier.get();
            hakemuksetHasher.putUnencodedChars(VALUE_DELIMETER_HAKUKOHDE);
            h.hakukohteenHakemukset().forEach(hw -> HakemusWrapper.hash(hakemuksetHasher, hw.getHakemus()));
            Hasher valintatuloksetHasher = hashSupplier.get();
            valintatuloksetHasher.putUnencodedChars(VALUE_DELIMETER_VALINTATULOKSET);
            h.hakukohteenHakijat().forEach(hk -> hk.hash(valintatuloksetHasher));
            HashCode hakukohteenHakemustenHash = hakemuksetHasher.hash();
            HashCode hakukohteenValintatulostenHash = valintatuloksetHasher.hash();
            LOG.trace("Hakukohde {}: Valintatulosten HASH = {}, hakemusten HASH = {}", h.getHakukohde().getOid(), hakukohteenValintatulostenHash, hakukohteenHakemustenHash);
            hasher.putBytes(hakukohteenHakemustenHash.asBytes());
            hasher.putBytes(hakukohteenValintatulostenHash.asBytes());
        });
        HashCode hash = hasher.hash();
        LOG.debug("Sijoitteluajon HASH {} (kesto {}ms)", hash, (System.currentTimeMillis() - t0));
        return hash;
    }

    public static <U> void ifPresentOrIfNotPresent(U u, Consumer<? super U> present, Supplier<Void> ifNotPresent,
                                                   Supplier<Void> delimeterSupplier) {
        Optional<U> u0 = ofNullable(u);
        if (u0.isPresent()) {
            present.accept(u0.get());
        } else {
            ifNotPresent.get();
            delimeterSupplier.get();
        }
    }

    public Stream<Valintatapajono> valintatapajonotStream() {
        return this.getHakukohteet().stream()
                .flatMap(hkv -> hkv.getValintatapajonot().stream())
                .map(ValintatapajonoWrapper::getValintatapajono);
    }

    public List<Valintatapajono> valintatapajonot() {
        return valintatapajonotStream().collect(Collectors.toList());
    }

    public boolean onkoKaikkiJonotSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa() {
        return valintatapajonotStream().allMatch(Valintatapajono::getSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa);
    }

    public boolean onkoVarasijasaannotVoimassaJaKaikkiJonotSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa() {
        return varasijaSaannotVoimassa() && onkoKaikkiJonotSijoiteltuIlmanVarasijasaantojaNiidenOllessaVoimassa();
    }

    public boolean onkoVarasijaSaannotVoimassaJaVarasijaTayttoPaattynyt(ValintatapajonoWrapper valintatapajono) {
        return varasijaSaannotVoimassa() && onkoVarasijaTayttoPaattynyt(valintatapajono);
    }

    private boolean onkoVarasijaTayttoPaattynyt(ValintatapajonoWrapper valintatapajono) {
        LocalDateTime varasijojaTaytetaanAsti =
                asInstant(valintatapajono.getValintatapajono().getVarasijojaTaytetaanAsti())
                .filter(d -> d.isBefore(varasijaTayttoPaattyy))
                .orElse(varasijaTayttoPaattyy);
        return getToday().isAfter(varasijojaTaytetaanAsti);
    }

    public void addMuuttuneetValintatulokset(Valintatulos... valintatulokset) {
        Collections.addAll(muuttuneetValintatulokset, valintatulokset);
    }

    public Optional<List<Hakukohde>> getEdellisenSijoittelunHakukohteet() {
        return edellisenSijoittelunHakukohteet;
    }

    public void setEdellisenSijoittelunHakukohteet(List<Hakukohde> edellisenSijoittelunHakukohteet) {
        this.edellisenSijoittelunHakukohteet = Optional.of(edellisenSijoittelunHakukohteet);
    }
    private Optional<LocalDateTime> asInstant(Date d) {
        return ofNullable(d).map(i -> LocalDateTime.ofInstant(i.toInstant(), ZoneId.systemDefault()));
    }

    public void setLisapaikkaTapa(LisapaikkaTapa tapa) {
        this.lisapaikkaTapa = tapa;
    }

    public LisapaikkaTapa getLisapaikkaTapa() {
        return this.lisapaikkaTapa;
    }

    public void setHakutoiveidenPriorisointi(boolean hakutoiveidenPriorisointi) {
        this.hakutoiveidenPriorisointi = hakutoiveidenPriorisointi;
    }

    public boolean getHakutoiveidenPriorisointi() {
        return hakutoiveidenPriorisointi;
    }

    public void paivitaVastaanottojenVaikutusHakemustenTiloihin(Map<String, VastaanottoDTO> aiemmanVastaanotonHakukohdePerHakija, Map<String, Map<String, Map<String, Valintatulos>>> indeksoidutTulokset) {
        getHakukohteet().forEach(hakukohdeWrapper -> {
            Map<String, Map<String, Valintatulos>> jonoIndex = indeksoidutTulokset.getOrDefault(hakukohdeWrapper.getHakukohde().getOid(), emptyMap());
            hakukohdeWrapper.getValintatapajonot().forEach(valintatapajonoWrapper -> {
                Map<String, Valintatulos> hakemusIndex = jonoIndex.getOrDefault(valintatapajonoWrapper.getValintatapajono().getOid(), emptyMap());
                valintatapajonoWrapper.getHakemukset().forEach(hakemusWrapper -> {
                    setHakemuksenValintatuloksenTila(
                        hakukohdeWrapper,
                        hakemusWrapper,
                        hakemusIndex.get(hakemusWrapper.getHakemus().getHakemusOid()),
                        ofNullable(aiemmanVastaanotonHakukohdePerHakija.get(hakemusWrapper.getHenkilo().getHakijaOid()))
                    );
                });
            });
        });
    }

    private static boolean vastaanotonTilaSaaMuuttaaHakemuksenTilaa(Hakemus hakemus) {
        return TilaTaulukot.kuuluuVastaanotonMuokattavissaTiloihin(hakemus.getEdellinenTila());
    }

    private static void setHakemuksenValintatuloksenTila(HakukohdeWrapper hakukohdeWrapper,
                                                         HakemusWrapper hakemusWrapper,
                                                         Valintatulos valintatulos,
                                                         Optional<VastaanottoDTO> aiempiVastaanottoSamalleKaudelle) {
        Hakemus hakemus = hakemusWrapper.getHakemus();
        LOG.debug("setHakemuksenValintatuloksenTila alkaa: Hakukohde: {}, valintatapajono: {}, hakemus: {}, hakemuksen tila: {}, hakemuksen edellinen tila: {}, vastaanoton tila: {}, aiempi vo: {}",
                hakemusWrapper.getValintatapajono().getHakukohdeWrapper().getHakukohde().getOid(),
                hakemusWrapper.getValintatapajono().getValintatapajono().getOid(),
                hakemus.getHakemusOid(),
                hakemus.getTila(),
                hakemus.getEdellinenTila(),
                valintatulos != null ? valintatulos.getTila() : "(null valintatulos)",
                aiempiVastaanottoSamalleKaudelle);
        if (estaaVastaanotonYhdenPaikanSaannoksenTakia(aiempiVastaanottoSamalleKaudelle, hakemusWrapper)) {
            if (valintatulos != null && valintatulos.getTila() == ValintatuloksenTila.PERUNUT) {
                TilojenMuokkaus.asetaTilaksiPerunut(hakemusWrapper);
            } else if (hakemus.getTila() == HakemuksenTila.VARALLA) {
                hakemus.setTila(HakemuksenTila.PERUUNTUNUT);
                if (hakemus.getEdellinenTila() != HakemuksenTila.PERUUNTUNUT || hakemus.getTilanKuvaukset() == TilanKuvaukset.tyhja) {
                    TilojenMuokkaus.asetaTilaksiPeruuntunutVastaanottanutToisenPaikanYhdenPaikanSaannonPiirissa(hakemusWrapper);
                }
            }
            hakemusWrapper.setTilaVoidaanVaihtaa(false);
        } else if (valintatulos != null && valintatulos.getTila() != null) {
            if (!vastaanotonTilaSaaMuuttaaHakemuksenTilaa(hakemus)) {
                // Don't write a log entry
                valintatulos.setTila(ValintatuloksenTila.KESKEN, ValintatuloksenTila.KESKEN, "", "");
            }
            LOG.debug("Hakukohde: {}, valintatapajono: {}, hakemus: {}, hakemuksen tila: {}, hakemuksen edellinen tila: {}, vastaanoton tila: {}",
                    hakemusWrapper.getValintatapajono().getHakukohdeWrapper().getHakukohde().getOid(),
                    hakemusWrapper.getValintatapajono().getValintatapajono().getOid(),
                    hakemus.getHakemusOid(),
                    hakemus.getTila(),
                    hakemus.getEdellinenTila(),
                    valintatulos.getTila());
            ValintatuloksenTila tila = valintatulos.getTila();
            boolean voidaanVaihtaa = false;
            if (tila == ValintatuloksenTila.PERUNUT) {
                TilojenMuokkaus.asetaTilaksiPerunut(hakemusWrapper);
            } else if (asList(VASTAANOTTANUT_SITOVASTI, EHDOLLISESTI_VASTAANOTTANUT).contains(tila)) {
                if (viimeisinHyvaksyttyJono(hakemusWrapper)) {
                    if (hakemus.getEdellinenTila() == HakemuksenTila.VARALLA || hakemus.getEdellinenTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY) {
                        hyvaksyVarasijalta(hakemusWrapper, valintatulos);
                    } else {
                        hyvaksy(hakemusWrapper, valintatulos);
                    }
                } else {
                    valintatulos.setTila(KESKEN, "Peitetään vastaanottotieto ei-hyväksytyltä jonolta"); // See ValintatulosWithVastaanotto.persistValintatulokset check
                    voidaanVaihtaa = false;
                }
            } else if (tila == ValintatuloksenTila.EI_VASTAANOTETTU_MAARA_AIKANA) {
                TilojenMuokkaus.asetaTilaksiPerunutEiVastaanottanutMaaraaikana(hakemusWrapper);
            } else if (tila == ValintatuloksenTila.PERUUTETTU) {
                TilojenMuokkaus.asetaTilaksiPeruutettu(hakemusWrapper);
            } else if (tila == ValintatuloksenTila.KESKEN) {
                // tila == KESKEN
                if (valintatulos.getJulkaistavissa() && hakemus.getEdellinenTila() == HakemuksenTila.HYVAKSYTTY) {
                    hyvaksy(hakemusWrapper, valintatulos);
                } else if (valintatulos.getJulkaistavissa() && hakemus.getEdellinenTila() == HakemuksenTila.VARASIJALTA_HYVAKSYTTY) {
                    hyvaksyVarasijalta(hakemusWrapper, valintatulos);
                } else if (valintatulos.getHyvaksyttyVarasijalta()) {
                    if (hasHigherJulkaistuHyvaksytty(hakukohdeWrapper, hakemusWrapper)) {
                        voidaanVaihtaa = true;
                        logDidNotHyvaksy(hakemusWrapper, tila, "hyvaksyttyVarasijalta");
                    } else {
                        hyvaksyVarasijalta(hakemusWrapper, valintatulos);
                    }
                } else if (valintatulos.getHyvaksyPeruuntunut()) {
                    if (hasHigherJulkaistuHyvaksytty(hakukohdeWrapper, hakemusWrapper)) {
                        voidaanVaihtaa = true;
                        logDidNotHyvaksy(hakemusWrapper, tila, "hyvaksyPeruuntunut");
                    } else {
                        hyvaksy(hakemusWrapper, valintatulos);
                        hakemusWrapper.hyvaksyPeruuntunut();
                    }
                } else if (HakemuksenTila.HYLATTY == hakemus.getTila()) {
                    voidaanVaihtaa = false;
                } else {
                    voidaanVaihtaa = true;
                }
            } else {
                throw new IllegalStateException(String.format("Valintatulos %s ja hakemus %s olivat tuntemattomassa tilassa", valintatulos, hakemus));
            }
            hakemusWrapper.setTilaVoidaanVaihtaa(voidaanVaihtaa);
            hakemusWrapper.getHenkilo().getValintatulos().add(valintatulos);
        } else if (hakemus.getTila().equals(HakemuksenTila.HYLATTY)) {
            hakemusWrapper.setTilaVoidaanVaihtaa(false);
        } else if (aiempiVastaanottoSamalleKaudelle.isPresent()) {
            LOG.warn("Ei muutettu hakemuksen tilaa aiemman saman kauden vastaanoton perusteella. " +
                    "Hakukohde: {}, valintatapajono: {}, hakemus: {}, hakemuksen tila: {}, " +
                    "hakemuksen edellinen tila: {}, vastaanoton tila: {}, aiemmin vastaanotettu hakukohde: {} {},",
                    hakemusWrapper.getValintatapajono().getHakukohdeWrapper().getHakukohde().getOid(),
                    hakemusWrapper.getValintatapajono().getValintatapajono().getOid(),
                    hakemus.getHakemusOid(),
                    hakemus.getTila(),
                    hakemus.getEdellinenTila(),
                    valintatulos != null ? valintatulos.getTila() : "- (ei valintatulosta)",
                    aiempiVastaanottoSamalleKaudelle.get().getAction(),
                    aiempiVastaanottoSamalleKaudelle.get().getHakukohdeOid());
        }
    }

    private static void logDidNotHyvaksy(HakemusWrapper hakemusWrapper, ValintatuloksenTila tila, String flagName) {
        Hakemus hakemus = hakemusWrapper.getHakemus();
        LOG.info("Ei hyväksytä jonosta vaikka {} päällä, koska korkeamman prioriteetin jonossa on jo julkaistu hyväksytty: " +
                        "Hakukohde: {}, valintatapajono: {}, hakemus: {}, hakemuksen tila: {}, " +
                        "hakemuksen edellinen tila: {}, vastaanoton tila: {}",
                flagName,
                hakemusWrapper.getValintatapajono().getHakukohdeWrapper().getHakukohde().getOid(),
                hakemusWrapper.getValintatapajono().getValintatapajono().getOid(),
                hakemus.getHakemusOid(),
                hakemus.getTila(),
                hakemus.getEdellinenTila(),
                tila);
    }

    private static boolean hasHigherJulkaistuHyvaksytty(HakukohdeWrapper hakukohdeWrapper,
                                                        HakemusWrapper hakemusWrapper) {
        Valintatapajono currentJono = hakemusWrapper.getValintatapajono().getValintatapajono();
        for (ValintatapajonoWrapper toinenJono : hakukohdeWrapper.getValintatapajonot()) {
            if (toinenJono.getValintatapajono().getPrioriteetti() < currentJono.getPrioriteetti()) {
                for (HakemusWrapper hakemus : toinenJono.getHakemukset()) {
                    if (hakemus.getHakemus().getHakemusOid().equals(hakemusWrapper.getHakemus().getHakemusOid())) {
                        if (hakemus.getValintatulos().map(v -> v.getJulkaistavissa()).orElse(false)
                                && TilaTaulukot.kuuluuHyvaksyttyihinTiloihin(hakemus.getHakemus().getEdellinenTila())) {
                            return true;
                        }
                        break;
                    }
                }
            }
        }
        return false;
    }

    private static boolean estaaVastaanotonYhdenPaikanSaannoksenTakia(Optional<VastaanottoDTO> aiempiVastaanottoOptional, HakemusWrapper hakemusWrapper) {
        if (!aiempiVastaanottoOptional.isPresent() || hakemusWrapper.getHakukohdeOid().equals(aiempiVastaanottoOptional.get().getHakukohdeOid())) {
            return false;
        }
        VastaanottoDTO aiempiVastaanotto = aiempiVastaanottoOptional.get();
        if (aiempiVastaanotto.getAction().sitova) {
            return true;
        }
        return onEriHaun(aiempiVastaanotto, hakemusWrapper);
    }

    private static boolean onEriHaun(VastaanottoDTO aiempiVastaanotto, HakemusWrapper hakemusWrapper) {
        return hakemusWrapper.getHenkilo().getHakemukset().stream().noneMatch(h ->
            h.getHakukohdeOid().equals(aiempiVastaanotto.getHakukohdeOid()));
    }

    private static void hyvaksyVarasijalta(final HakemusWrapper hakemusWrapper, final Valintatulos valintatulos) {
        TilojenMuokkaus.asetaTilaksiVarasijaltaHyvaksytty(hakemusWrapper);
        hakemusWrapper.getHakemus().setIlmoittautumisTila(valintatulos.getIlmoittautumisTila());
    }

    private static void hyvaksy(final HakemusWrapper hakemusWrapper, final Valintatulos valintatulos) {
        TilojenMuokkaus.asetaTilaksiHyvaksytty(hakemusWrapper);
        hakemusWrapper.getHakemus().setIlmoittautumisTila(valintatulos.getIlmoittautumisTila());
    }

    private static boolean viimeisinHyvaksyttyJono(HakemusWrapper hakemusWrapper) {

        if(TilaTaulukot.kuuluuHyvaksyttyihinTiloihin(hakemusWrapper.getHakemus().getEdellinenTila())) return true;

        final String viimeisinHyvaksyttyJonoOid = hakemusWrapper.getHenkilo().getHakemukset().stream()
                .filter(hw -> hw.getHakemus().getTilaHistoria().stream().anyMatch(th -> TilaTaulukot.kuuluuHyvaksyttyihinTiloihin(th.getTila())) == true)
                .map(hw -> {
                    final TilaHistoria tilaHistoria = hw.getHakemus().getTilaHistoria().stream()
                            .filter(th -> TilaTaulukot.kuuluuHyvaksyttyihinTiloihin(th.getTila()))
                            .max(Comparator.comparingLong(th -> th.getLuotu().getTime()))
                            .get();
                    org.apache.commons.lang3.tuple.Pair<String, Date> pair = Pair.of(hw.getValintatapajono().getValintatapajono().getOid(), tilaHistoria.getLuotu());

                    return pair;

                }).collect(Collectors.toList()).stream()
                .max(Comparator.comparingLong(pair -> pair.getRight().getTime())).orElse(Pair.of("", null)).getLeft();

        return viimeisinHyvaksyttyJonoOid.equals(hakemusWrapper.getValintatapajono().getValintatapajono().getOid());


    }
}
