package fi.vm.sade.sijoittelu.laskenta.service.it;

import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.HakuDTO;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriArvoDTO;
import fi.vm.sade.sijoittelu.laskenta.external.resource.dto.ParametriDTO;

import java.time.Instant;
import java.util.Optional;
import java.util.function.Function;

public class Haku {
    public final String haunkohdejoukkoUri;
    public final String haunkohdejoukontarkenneUri;
    public final boolean jarjestetytHakutoiveet;
    public final Instant valintatuloksetSiirrettavaSijoitteluunViimeistaan;
    public final Instant varasijasaannotAstuvatVoimaan;
    public final Instant varasijatayttoPaattyy;
    public final Instant hakukierrosPaattyy;

    public Haku(String haunkohdejoukkoUri,
                String haunkohdejoukontarkenneUri,
                boolean jarjestetytHakutoiveet,
                Instant valintatuloksetSiirrettavaSijoitteluunViimeistaan,
                Instant varasijasaannotAstuvatVoimaan,
                Instant varasijatayttoPaattyy,
                Instant hakukierrosPaattyy) {
        this.haunkohdejoukkoUri = haunkohdejoukkoUri;
        this.haunkohdejoukontarkenneUri = haunkohdejoukontarkenneUri;
        this.jarjestetytHakutoiveet = jarjestetytHakutoiveet;
        this.valintatuloksetSiirrettavaSijoitteluunViimeistaan = valintatuloksetSiirrettavaSijoitteluunViimeistaan;
        this.varasijasaannotAstuvatVoimaan = varasijasaannotAstuvatVoimaan;
        this.varasijatayttoPaattyy = varasijatayttoPaattyy;
        this.hakukierrosPaattyy = hakukierrosPaattyy;
    }

    private Instant getDate(ParametriDTO ohjausparametrit, Function<ParametriDTO, ParametriArvoDTO> f) {
        return Optional.ofNullable(ohjausparametrit)
                .flatMap(o -> Optional.ofNullable(f.apply(o)))
                .flatMap(p -> Optional.ofNullable(p.getDate()))
                .map(Instant::ofEpochMilli)
                .orElse(null);
    }

    public Haku(HakuDTO tarjontaHaku, ParametriDTO ohjausparametrit) {
        this.haunkohdejoukkoUri = tarjontaHaku.getKohdejoukkoUri();
        this.haunkohdejoukontarkenneUri = tarjontaHaku.getKohdejoukonTarkenne();
        this.jarjestetytHakutoiveet = tarjontaHaku.isUsePriority();
        this.valintatuloksetSiirrettavaSijoitteluunViimeistaan =
                this.getDate(ohjausparametrit, ParametriDTO::getPH_VTSSV);
        this.varasijasaannotAstuvatVoimaan =
                this.getDate(ohjausparametrit, ParametriDTO::getPH_VSSAV);
        this.varasijatayttoPaattyy =
                this.getDate(ohjausparametrit, ParametriDTO::getPH_VSTP);
        this.hakukierrosPaattyy =
                this.getDate(ohjausparametrit, ParametriDTO::getPH_HKP);
    }
}
