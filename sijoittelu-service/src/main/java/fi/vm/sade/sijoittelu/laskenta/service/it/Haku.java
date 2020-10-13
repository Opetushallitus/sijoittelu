package fi.vm.sade.sijoittelu.laskenta.service.it;

import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.HakuDTO;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriArvoDTO;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriDTO;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class Haku {
    private static final String KK_HAUN_KOHDEJOUKKO = "haunkohdejoukko_12";
    private static final Set<String> AMKOPE_HAUN_KOHDEJOUKON_TARKENTEET = new HashSet<>();
    static {
        AMKOPE_HAUN_KOHDEJOUKON_TARKENTEET.add("haunkohdejoukontarkenne_2");
        AMKOPE_HAUN_KOHDEJOUKON_TARKENTEET.add("haunkohdejoukontarkenne_4");
        AMKOPE_HAUN_KOHDEJOUKON_TARKENTEET.add("haunkohdejoukontarkenne_5");
    };

    public final String oid;
    public final String haunkohdejoukkoUri;
    public final String haunkohdejoukontarkenneUri;
    public final boolean jarjestetytHakutoiveet;
    public final Instant valintatuloksetSiirrettavaSijoitteluunViimeistaan;
    public final Instant varasijasaannotAstuvatVoimaan;
    public final Instant varasijatayttoPaattyy;
    public final Instant hakukierrosPaattyy;

    public Haku(String oid,
                String haunkohdejoukkoUri,
                String haunkohdejoukontarkenneUri,
                boolean jarjestetytHakutoiveet,
                Instant valintatuloksetSiirrettavaSijoitteluunViimeistaan,
                Instant varasijasaannotAstuvatVoimaan,
                Instant varasijatayttoPaattyy,
                Instant hakukierrosPaattyy) {
        if (haunkohdejoukkoUri == null) {
            throw new IllegalStateException(String.format("Haulla %s ei ole haun kohdejoukkoa", oid));
        }
        if (hakukierrosPaattyy == null) {
            throw new IllegalStateException(String.format("Haun %s ohjausparametria PH_HKP (hakukierros päättyy) ei ole asetettu", oid));
        }
        if (haunkohdejoukkoUri.startsWith(KK_HAUN_KOHDEJOUKKO + "#")) {
            if (valintatuloksetSiirrettavaSijoitteluunViimeistaan == null) {
                throw new IllegalStateException(String.format("Haku %s on korkeakouluhaku ja ohjausparametria PH_VTSSV (kaikki kohteet sijoittelussa) ei ole asetettu", oid));
            }
            if (varasijasaannotAstuvatVoimaan == null) {
                throw new IllegalStateException(String.format("Haku %s on korkeakouluhaku ja ohjausparametria PH_VSSAV (varasijasäännöt astuvat voimaan) ei ole asetettu", oid));
            }
        }
        if (hakukierrosPaattyy.isBefore(Instant.now())) {
            throw new IllegalStateException(String.format("Haun %s hakukierros on päättynyt %s", oid, hakukierrosPaattyy));
        }
        if (valintatuloksetSiirrettavaSijoitteluunViimeistaan != null && hakukierrosPaattyy.isBefore(valintatuloksetSiirrettavaSijoitteluunViimeistaan)) {
            throw new IllegalStateException(String.format("Haun %s hakukierros on asetettu päättymään %s ennen kuin kaikkien kohteiden tulee olla sijoittelussa %s", oid, hakukierrosPaattyy, valintatuloksetSiirrettavaSijoitteluunViimeistaan));
        }
        if (varasijasaannotAstuvatVoimaan != null && hakukierrosPaattyy.isBefore(varasijasaannotAstuvatVoimaan)) {
            throw new IllegalStateException(String.format("Haun %s hakukierros on asetettu päättymään %s ennen kuin varasija säännöt astuvat voimaan %s", oid, hakukierrosPaattyy, varasijasaannotAstuvatVoimaan));
        }
        this.oid = oid;
        this.haunkohdejoukkoUri = haunkohdejoukkoUri;
        this.haunkohdejoukontarkenneUri = haunkohdejoukontarkenneUri;
        this.jarjestetytHakutoiveet = jarjestetytHakutoiveet;
        this.valintatuloksetSiirrettavaSijoitteluunViimeistaan = valintatuloksetSiirrettavaSijoitteluunViimeistaan;
        this.varasijasaannotAstuvatVoimaan = varasijasaannotAstuvatVoimaan;
        this.varasijatayttoPaattyy = varasijatayttoPaattyy;
        this.hakukierrosPaattyy = hakukierrosPaattyy;
    }

    private static Instant getDate(ParametriDTO ohjausparametrit, Function<ParametriDTO, ParametriArvoDTO> f) {
        return Optional.ofNullable(ohjausparametrit)
                .flatMap(o -> Optional.ofNullable(f.apply(o)))
                .flatMap(p -> Optional.ofNullable(p.getDate()))
                .map(Instant::ofEpochMilli)
                .orElse(null);
    }

    public Haku(HakuDTO tarjontaHaku, ParametriDTO ohjausparametrit) {
        this(
                tarjontaHaku.getOid(),
                tarjontaHaku.getKohdejoukkoUri(),
                tarjontaHaku.getKohdejoukonTarkenne(),
                tarjontaHaku.isUsePriority(),
                getDate(ohjausparametrit, ParametriDTO::getPH_VTSSV),
                getDate(ohjausparametrit, ParametriDTO::getPH_VSSAV),
                getDate(ohjausparametrit, ParametriDTO::getPH_VSTP),
                getDate(ohjausparametrit, ParametriDTO::getPH_HKP)
        );
    }

    public boolean isKk() {
        return this.haunkohdejoukkoUri.startsWith(KK_HAUN_KOHDEJOUKKO + "#");
    }

    public boolean isAmkOpe() {
        return this.isKk() && this.haunkohdejoukontarkenneUri != null && AMKOPE_HAUN_KOHDEJOUKON_TARKENTEET.stream().anyMatch(s -> this.haunkohdejoukontarkenneUri.startsWith(s + "#"));
    }
}
