package fi.vm.sade.sijoittelu.domain;

import java.util.Map;
import java.util.Optional;

/**
 * Note: changing these enum values requires changing their mappings in valinta-tulos-service
 * - in code
 * - database enum
 * and updating mutual sijoittelu and VTS dependencies so that they stay in sync.
 */
public enum TilankuvauksenTarkenne {
    PERUUNTUNUT_HYVAKSYTTY_YLEMMALLE_HAKUTOIVEELLE(TilanKuvaukset.peruuntunutYlempiToive),
    PERUUNTUNUT_ALOITUSPAIKAT_TAYNNA(TilanKuvaukset.peruuntunutAloituspaikatTaynna),
    PERUUNTUNUT_HYVAKSYTTY_TOISESSA_JONOSSA(TilanKuvaukset.peruuntunutHyvaksyttyToisessaJonossa),
    HYVAKSYTTY_VARASIJALTA(TilanKuvaukset.varasijaltaHyvaksytty),
    PERUUNTUNUT_EI_VASTAANOTTANUT_MAARAAIKANA(TilanKuvaukset.peruuntunutEiVastaanottanutMaaraaikana),
    PERUUNTUNUT_VASTAANOTTANUT_TOISEN_PAIKAN(TilanKuvaukset.peruuntunutVastaanottanutToisenOpiskelupaikan),
    PERUUNTUNUT_EI_MAHDU_VARASIJOJEN_MAARAAN(TilanKuvaukset.peruuntunutEiMahduKasiteltavienVarasijojenMaaraan),
    PERUUNTUNUT_HAKUKIERROS_PAATTYNYT(TilanKuvaukset.peruuntunutHakukierrosOnPaattynyt),
    PERUUNTUNUT_EI_VARASIJATAYTTOA(TilanKuvaukset.peruuntunutEiVarasijaTayttoa),
    HYVAKSYTTY_TAYTTOJONO_SAANNOLLA,
    HYLATTY_HAKIJARYHMAAN_KUULUMATTOMANA,
    PERUUNTUNUT_VASTAANOTTANUT_TOISEN_PAIKAN_YHDEN_SAANNON_PAIKAN_PIIRISSA(TilanKuvaukset.peruuntunutVastaanottanutToisenOpiskelupaikanYhdenPaikanSaannonPiirissa),
    PERUUNTUNUT_HYVAKSYTTY_ALEMMALLE_HAKUTOIVEELLE(TilanKuvaukset.peruuntunutHyvaksyttyAlemmallaHakutoiveella),
    EI_TILANKUVAUKSEN_TARKENNETTA;

    private final Map<String, String> tilanKuvaukset;

    private TilankuvauksenTarkenne() {
        this.tilanKuvaukset = null;
    }

    private TilankuvauksenTarkenne(Map<String, String> tilanKuvaukset) {
        this.tilanKuvaukset = tilanKuvaukset;
    }

    public Optional<Map<String, String>> vakioTilanKuvaus() {
        return Optional.ofNullable(this.tilanKuvaukset);
    }
}
