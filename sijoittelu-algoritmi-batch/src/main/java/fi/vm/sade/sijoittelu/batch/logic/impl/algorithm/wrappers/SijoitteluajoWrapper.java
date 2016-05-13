package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers;

import static java.util.Optional.ofNullable;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;

import fi.vm.sade.sijoittelu.domain.Hakukohde;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.Valintatulos;

public class SijoitteluajoWrapper {
    private static final Logger LOG = LoggerFactory.getLogger(SijoitteluajoWrapper.class);
    public static final String VALUE_FOR_HASH_FUNCTION_WHEN_UNDEFINED = "undefined"; // määrittelemättömän arvon syöte hash-funktioon

    private SijoitteluAjo sijoitteluajo;

    private List<HakukohdeWrapper> hakukohteet = new ArrayList<HakukohdeWrapper>();

    private List<Valintatulos> muuttuneetValintatulokset = new ArrayList<>();

    private final LocalDateTime today = LocalDateTime.now();

    private LocalDateTime kaikkiKohteetSijoittelussa = LocalDateTime.now().minusDays(1);

    private LocalDateTime varasijaSaannotAstuvatVoimaan = LocalDateTime.now().minusDays(1);

    private LocalDateTime varasijaTayttoPaattyy = LocalDateTime.now().plusYears(100);

    private LocalDateTime hakuKierrosPaattyy = LocalDateTime.now().plusYears(100);

    private boolean isKKHaku = false;

    private List<String> varasijapomput = new ArrayList<>();

    public SijoitteluajoWrapper(final SijoitteluAjo sijoitteluAjo) {
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

    public List<String> getVarasijapomput() {
        return varasijapomput;
    }

    public void setVarasijapomput(List<String> varasijapomput) {
        this.varasijapomput = varasijapomput;
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
            h.hakukohteenHakemukset().forEach(hk -> hk.hash(hakemuksetHasher));
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

    // ei juuri yhtaan nopeampi ja epavarmempi
    private HashCode valivaiheetonHashStrategia(HashFunction hashFunction) {
        long t0 = System.currentTimeMillis();
        final Hasher hasher = hashFunction.newHasher();
        hakukohteet.stream().forEach(h -> {
            h.hakukohteenHakemukset().forEach(hk -> hk.hash(hasher));
            h.hakukohteenHakijat().forEach(hk -> hk.hash(hasher));
        });
        HashCode hash = hasher.hash();
        LOG.debug("Sijoitteluajon HASH {} (kesto {}ms)", hash, (System.currentTimeMillis() - t0));
        return hash;
    }

    public boolean onkoVarasijaTayttoPaattynyt(ValintatapajonoWrapper valintatapajono) {
        Date varasijojaTaytetaanAsti = valintatapajono.getValintatapajono().getVarasijojaTaytetaanAsti();
        LocalDateTime varasijaTayttoPaattyy = this.getVarasijaTayttoPaattyy();
        if (varasijojaTaytetaanAsti != null) {
            LocalDateTime tempDate = LocalDateTime.ofInstant(varasijojaTaytetaanAsti.toInstant(), ZoneId.systemDefault());
            if(tempDate.isBefore(varasijaTayttoPaattyy))
                varasijaTayttoPaattyy = tempDate;
        }
        return getToday().isAfter(varasijaTayttoPaattyy);
    }

    public void addMuuttuneetValintatulokset(Valintatulos... valintatulokset) {
        Collections.addAll(muuttuneetValintatulokset, valintatulokset);
    }
}
