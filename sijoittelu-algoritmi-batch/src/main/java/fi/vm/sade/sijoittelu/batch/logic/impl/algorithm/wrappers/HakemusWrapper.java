package fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import fi.vm.sade.sijoittelu.domain.HakemuksenTila;
import fi.vm.sade.sijoittelu.domain.Hakemus;
import fi.vm.sade.sijoittelu.domain.Pistetieto;
import fi.vm.sade.sijoittelu.domain.Valintatulos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static fi.vm.sade.sijoittelu.batch.logic.impl.algorithm.wrappers.SijoitteluajoWrapper.ifPresentOrIfNotPresent;

public class HakemusWrapper {
    private static final Logger LOG = LoggerFactory.getLogger(HakemusWrapper.class);
    private Hakemus hakemus;

    private ValintatapajonoWrapper valintatapajono;

    private HenkiloWrapper henkilo;

    private HashCode lahtotilanteenHash;

    // Yhden hakukohderekursion aikainen lippu, jolla katsotaan voidaanko korvata
    private boolean hyvaksyttyHakijaryhmasta = false;

    private boolean hyvaksyttavissaHakijaryhmanJalkeen = true;

    //jos hakemuksen tilaa ei voida muuttaa, esm. ilmoitettu hakijalle jo
    private boolean tilaVoidaanVaihtaa = true;

    private boolean hyvaksyPeruuntunut;

    public HashCode getLahtotilanteenHash() {
        return lahtotilanteenHash;
    }

    public void setLahtotilanteenHash(HashCode lahtotilanteenHash) {
        this.lahtotilanteenHash = lahtotilanteenHash;
    }

    public HenkiloWrapper getHenkilo() {
        return henkilo;
    }

    public void setHenkilo(HenkiloWrapper henkilo) {
        this.henkilo = henkilo;
    }

    public Hakemus getHakemus() {
        return hakemus;
    }
    public boolean isVaralla() {
        return HakemuksenTila.VARALLA.equals(hakemus.getTila());
    }

    public Optional<Valintatulos> getValintatulos() {
        return henkilo.getValintatulos().stream()
                .filter(v -> hakemus.getHakemusOid().equals(v.getHakemusOid()) &&
                        valintatapajono.getValintatapajono().getOid().equals(v.getValintatapajonoOid()))
                .findAny();
    }

    public Stream<HakemusWrapper> getYlemmatTaiSamanarvoisetMuttaKorkeammallaJonoPrioriteetillaOlevatHakutoiveet() {
        return henkilo.getHakemukset().stream().filter(h -> {
            if (Objects.equals(h.hakemus.getPrioriteetti(), this.hakemus.getPrioriteetti())) {
                return h.valintatapajono.getValintatapajono().getPrioriteetti() < this.valintatapajono.getValintatapajono().getPrioriteetti();
            } else {
                return h.hakemus.getPrioriteetti() < this.hakemus.getPrioriteetti();
            }
        });
    }

    public String getHakukohdeOid() {
        return valintatapajono.getHakukohdeWrapper().getHakukohde().getOid();
    }

    public void setHakemus(Hakemus hakemus) {
        this.hakemus = hakemus;
    }

    public ValintatapajonoWrapper getValintatapajono() {
        return valintatapajono;
    }

    public void setValintatapajono(ValintatapajonoWrapper valintatapajono) {
        this.valintatapajono = valintatapajono;
    }


    public boolean isTilaVoidaanVaihtaa() {
        return tilaVoidaanVaihtaa;
    }

    public void setTilaVoidaanVaihtaa(boolean tilaVoidaanVaihtaa) {
        this.tilaVoidaanVaihtaa = tilaVoidaanVaihtaa;
    }

    public boolean isHyvaksyttyHakijaryhmasta() {
        return hyvaksyttyHakijaryhmasta;
    }

    public void setHyvaksyttyHakijaryhmasta(boolean hyvaksyttyHakijaryhmasta) {
        this.hyvaksyttyHakijaryhmasta = hyvaksyttyHakijaryhmasta;
    }

    private static final String VALUE_DELIMETER_HAKEMUS = "_HAKEMUS_";
    private static final String VALUE_DELIMETER_EDELLINEN_TILA = "_EDELLINEN_TILA_";
    private static final String VALUE_DELIMETER_HAKEMUSOID = "_HAKEMUSOID_";
    private static final String VALUE_DELIMETER_ILMOITTAUTUMISTILA = "_ILMOITTAUTUMISTILA_";
    private static final String VALUE_DELIMETER_JONOSIJA = "_JONOSIJA_";
    private static final String VALUE_DELIMETER_PISTEET = "_PISTEET_";
    private static final String VALUE_DELIMETER_PISTETIETO = "_PISTETIETO_";
    private static final String VALUE_DELIMETER_PISTETIETO_ARVO = "_ARVO_";
    private static final String VALUE_DELIMETER_PISTETIETO_LASKENNANLLINEN_ARVO = "_LASKENNALLINEN_ARVO_";
    private static final String VALUE_DELIMETER_PISTETIETO_OSALLISTUMINEN = "_OSALLISTUMINEN_";
    private static final String VALUE_DELIMETER_PISTETIETO_TUNNISTE = "_TUNNISTE_";
    private static final String VALUE_DELIMETER_PRIORITEETTI = "_PRIORITEETTI_";
    private static final String VALUE_DELIMETER_TASASIJAJONOSIJA = "_TASASIJAJONOSIJA_";
    private static final String VALUE_DELIMETER_TILA = "_TILA_";
    private static final String VALUE_DELIMETER_VARASIJAN_NUMERO = "_VARASIJAN_NUMERO_";

    public static HashCode luoHash(Hakemus hakemus) {
        Hasher hasher = Hashing.md5().newHasher();
        hash(hasher, hakemus);
        return hasher.hash();
    }

    public static void hash(Hasher hf, Hakemus hakemus) {
        if (hakemus != null) {
            Supplier<Void> undefined = () -> {
                hf.putUnencodedChars(SijoitteluajoWrapper.VALUE_FOR_HASH_FUNCTION_WHEN_UNDEFINED);
                return null;
            };
            Function<String, Supplier<Void>> delimeter = dm -> {
                return () -> {
                    hf.putUnencodedChars(dm);
                    return null;
                };
            };
            delimeter.apply(VALUE_DELIMETER_HAKEMUS).get(); // Uuden hakemuksen alkuun delimeter
            ifPresentOrIfNotPresent(hakemus.getEdellinenTila(), t -> hf.putInt(t.ordinal()), undefined, delimeter.apply(VALUE_DELIMETER_EDELLINEN_TILA));
            ifPresentOrIfNotPresent(hakemus.getHakemusOid(), t -> hf.putUnencodedChars(t), undefined, delimeter.apply(VALUE_DELIMETER_HAKEMUSOID));
            ifPresentOrIfNotPresent(hakemus.getIlmoittautumisTila(), t -> hf.putInt(t.ordinal()), undefined, delimeter.apply(VALUE_DELIMETER_ILMOITTAUTUMISTILA));
            ifPresentOrIfNotPresent(hakemus.getJonosija(), t -> hf.putInt(t), undefined, delimeter.apply(VALUE_DELIMETER_JONOSIJA));
            ifPresentOrIfNotPresent(hakemus.getPisteet(), t -> hf.putUnencodedChars(t.toString()), undefined, delimeter.apply(VALUE_DELIMETER_PISTEET));
            List<Pistetieto> pistetietoList = ofNullable(hakemus.getPistetiedot()).orElse(Collections.<Pistetieto>emptyList());
            if (pistetietoList.isEmpty()) {
                undefined.get();
            } else {
                pistetietoList.forEach(p -> {
                    ifPresentOrIfNotPresent(p.getArvo(), a -> hf.putUnencodedChars(a), undefined, delimeter.apply(VALUE_DELIMETER_PISTETIETO_ARVO));
                    ifPresentOrIfNotPresent(p.getLaskennallinenArvo(), a -> hf.putUnencodedChars(a), undefined, delimeter.apply(VALUE_DELIMETER_PISTETIETO_LASKENNANLLINEN_ARVO));
                    ifPresentOrIfNotPresent(p.getOsallistuminen(), a -> hf.putUnencodedChars(a), undefined, delimeter.apply(VALUE_DELIMETER_PISTETIETO_OSALLISTUMINEN));
                    ifPresentOrIfNotPresent(p.getTunniste(), a -> hf.putUnencodedChars(a), undefined, delimeter.apply(VALUE_DELIMETER_PISTETIETO_TUNNISTE));
                });
            }
            delimeter.apply(VALUE_DELIMETER_PISTETIETO).get(); // Pistetietojen jalkeen delimeter
            ifPresentOrIfNotPresent(hakemus.getPrioriteetti(), t -> hf.putInt(t), undefined, delimeter.apply(VALUE_DELIMETER_PRIORITEETTI));
            ifPresentOrIfNotPresent(hakemus.getTasasijaJonosija(), t -> hf.putInt(t), undefined, delimeter.apply(VALUE_DELIMETER_TASASIJAJONOSIJA));
            ifPresentOrIfNotPresent(hakemus.getTila(), t -> hf.putInt(t.ordinal()), undefined, delimeter.apply(VALUE_DELIMETER_TILA));
            ifPresentOrIfNotPresent(hakemus.getVarasijanNumero(), t -> hf.putInt(t), undefined, delimeter.apply(VALUE_DELIMETER_VARASIJAN_NUMERO));
        } else {
            LOG.error("Hakemuswrapperilla ei ole hakemusta!");
            throw new RuntimeException("Hakemuswrapperilla ei ole hakemusta!");
        }
    }

    public boolean isHyvaksyttavissaHakijaryhmanJalkeen() {
        return hyvaksyttavissaHakijaryhmanJalkeen;
    }

    public void setHyvaksyttavissaHakijaryhmanJalkeen(boolean hyvaksyttavissaHakijaryhmanJalkeen) {
        this.hyvaksyttavissaHakijaryhmanJalkeen = hyvaksyttavissaHakijaryhmanJalkeen;
    }

    public boolean getHyvaksyPeruuntunut() {
        return this.hyvaksyPeruuntunut;
    }

    public void hyvaksyPeruuntunut() {
        this.hyvaksyPeruuntunut = true;
    }
}
