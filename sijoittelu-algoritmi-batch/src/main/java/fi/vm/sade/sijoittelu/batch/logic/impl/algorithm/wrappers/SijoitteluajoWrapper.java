package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers;

import com.google.common.collect.Sets;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import fi.vm.sade.sijoittelu.domain.SijoitteluAjo;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * 
 * @author Kari Kammonen
 * 
 */
public class SijoitteluajoWrapper {
    private static final Logger LOG = LoggerFactory.getLogger(SijoitteluajoWrapper.class);
    //private transient int hashcode = -1;

    private SijoitteluAjo sijoitteluajo;

    private List<HakukohdeWrapper> hakukohteet = new ArrayList<HakukohdeWrapper>();

    private List<Valintatulos> muuttuneetValintatulokset = new ArrayList<>();

    private final LocalDateTime today = LocalDateTime.now();

    private LocalDateTime kaikkiKohteetSijoittelussa = LocalDateTime.now().minusDays(1);

    private LocalDateTime varasijaSaannotAstuvatVoimaan = LocalDateTime.now().minusDays(1);

    private LocalDateTime hakuKierrosPaattyy = LocalDateTime.now().plusYears(100);

    private boolean isKKHaku = false;

    private List<String> varasijapomput = new ArrayList<>();

    public List<HakukohdeWrapper> getHakukohteet() {
        return hakukohteet;
    }


    public void setHakukohteet(List<HakukohdeWrapper> hakukohteet) {
        this.hakukohteet = hakukohteet;
    }

    public SijoitteluAjo getSijoitteluajo() {
        return sijoitteluajo;
    }

    public void setSijoitteluajo(SijoitteluAjo sijoitteluajo) {
        this.sijoitteluajo = sijoitteluajo;
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

    private HashCode jarjestettyValivaiheellinenHashStrategia(HashFunction hashFunction) {
        long t0 = System.currentTimeMillis();
        final Hasher hasher = hashFunction.newHasher();
        // Jokaiselle hakukohteelle oma hasher ja yhdistetaan hash-arvot lopuksi
        // jolloin voidaan seurata yksittaisten hakukohteiden muuttumista
        Supplier<Hasher> hashSupplier = () -> hashFunction.newHasher(); // hasher;

        //Set<HashCode> hashOfEachHakukohde = Sets.new
        hakukohteet.stream().sorted().forEach(h -> {
            Hasher hakemuksetHasher = hashSupplier.get();
            h.hakukohteenHakemukset().forEach(hk -> hk.hash(hakemuksetHasher));
            Hasher valintatuloksetHasher = hashSupplier.get();
            h.hakukohteenHakijat().forEach(hk -> hk.hash(valintatuloksetHasher));
            HashCode hakukohteenHakemustenHash = hakemuksetHasher.hash();
            HashCode hakukohteenValintatulostenHash = valintatuloksetHasher.hash();
            LOG.trace("Hakukohde {}: Valintatulosten HASH = {}, hakemusten HASH = {}",
                    h.getHakukohde().getOid(), hakukohteenValintatulostenHash, hakukohteenHakemustenHash);
            hasher.putBytes(hakukohteenHakemustenHash.asBytes());
            hasher.putBytes(hakukohteenValintatulostenHash.asBytes());
        });
        HashCode hash = hasher.hash();
        LOG.debug("Sijoitteluajon HASH {} (kesto {}ms)", hash, (System.currentTimeMillis() - t0));
        return hash;
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
}
