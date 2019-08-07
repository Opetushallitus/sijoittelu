package fi.vm.sade.sijoittelu.domain;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TilanKuvaukset {

    public static Map<String, String> tyhja = Collections.emptyMap();

    public static Map<String, String> peruuntunutYlempiToive = new HashMap<>();
    static {
        peruuntunutYlempiToive.put("FI", "Peruuntunut, hyväksytty ylemmälle hakutoiveelle");
        peruuntunutYlempiToive.put("SV", "Annullerad, godkänt till ansökningsmål med högre prioritet");
        peruuntunutYlempiToive.put("EN", "Cancelled, accepted for a study place with higher priority");
    }

    public static Map<String, String> peruuntunutAloituspaikatTaynna = new HashMap<>();
    static {
        peruuntunutAloituspaikatTaynna.put("FI", "Peruuntunut, aloituspaikat täynnä");
        peruuntunutAloituspaikatTaynna.put("SV", "Annullerad, nybörjarplatser fyllda");
        peruuntunutAloituspaikatTaynna.put("EN", "Cancelled, study places are filled");
    }

    public static Map<String, String> peruuntunutHyvaksyttyToisessaJonossa = new HashMap<>();
    static {
        peruuntunutHyvaksyttyToisessaJonossa.put("FI", "Peruuntunut, hyväksytty toisessa valintatapajonossa");
        peruuntunutHyvaksyttyToisessaJonossa.put("SV", "Annullerad, godkänd i en annan urvalsmetodskö");
        peruuntunutHyvaksyttyToisessaJonossa.put("EN", "Cancelled, accepted in another selection method queue");
    }

    public static Map<String, String> varasijaltaHyvaksytty = new HashMap<>();
    static {
        varasijaltaHyvaksytty.put("FI", "Varasijalta hyväksytty");
        varasijaltaHyvaksytty.put("SV", "Godkänd från reservplats");
        varasijaltaHyvaksytty.put("EN", "Accepted from a waiting list");
    }

    public static Map<String, String> peruuntunutEiVastaanottanutMaaraaikana = new HashMap<>();
    static {
        peruuntunutEiVastaanottanutMaaraaikana.put("FI", "Peruuntunut, ei vastaanottanut määräaikana");
        peruuntunutEiVastaanottanutMaaraaikana.put("SV", "Annullerad, har inte tagit emot platsen inom utsatt tid");
        peruuntunutEiVastaanottanutMaaraaikana.put("EN", "Cancelled, has not confirmed the study place within the deadline");
    }

    public static Map<String, String> peruuntunutVastaanottanutToisenOpiskelupaikan = new HashMap<>();
    static {
        peruuntunutVastaanottanutToisenOpiskelupaikan.put("FI", "Peruuntunut, ottanut vastaan toisen opiskelupaikan");
        peruuntunutVastaanottanutToisenOpiskelupaikan.put("SV", "Annullerad, ottanut vastaan toisen opiskelupaikan");
        peruuntunutVastaanottanutToisenOpiskelupaikan.put("EN", "Cancelled, ottanut vastaan toisen opiskelupaikan");
    }

    public static Map<String, String> peruuntunutEiMahduKasiteltavienVarasijojenMaaraan = new HashMap<>();
    static {
        peruuntunutEiMahduKasiteltavienVarasijojenMaaraan.put("FI", "Peruuntunut, pisteesi eivät riittäneet varasijaan");
        peruuntunutEiMahduKasiteltavienVarasijojenMaaraan.put("SV", "Annullerad, dina poäng räckte inte till reservplats");
        peruuntunutEiMahduKasiteltavienVarasijojenMaaraan.put("EN", "Cancelled, you did not get enough points for a place on the waiting list");
    }

    public static Map<String, String> peruuntunutHakukierrosOnPaattynyt = new HashMap<>();
    static {
        peruuntunutHakukierrosOnPaattynyt.put("FI", "Peruuntunut, varasijatäyttö päättynyt");
        peruuntunutHakukierrosOnPaattynyt.put("SV", "Annullerad, besättning av reservplatser har upphört");
        peruuntunutHakukierrosOnPaattynyt.put("EN", "Cancelled, waiting list selection has ended");
    }

    public static Map<String, String> peruuntunutEiVarasijaTayttoa = new HashMap<>();
    static {
        peruuntunutEiVarasijaTayttoa.put("FI", "Peruuntunut, ei varasijatäyttöä");
        peruuntunutEiVarasijaTayttoa.put("SV", "Annullerad, ingen antagning från reservplats");
        peruuntunutEiVarasijaTayttoa.put("EN", "Cancelled, no waiting list practice");
    }

    public static Map<String, String> peruuntunutVastaanottanutToisenOpiskelupaikanYhdenPaikanSaannonPiirissa = new HashMap<>();
    static {
        peruuntunutVastaanottanutToisenOpiskelupaikanYhdenPaikanSaannonPiirissa.put("FI", "Peruuntunut, vastaanottanut toisen korkeakoulupaikan");
        peruuntunutVastaanottanutToisenOpiskelupaikanYhdenPaikanSaannonPiirissa.put("SV", "Annullerad, tagit emot en annan högskoleplats");
        peruuntunutVastaanottanutToisenOpiskelupaikanYhdenPaikanSaannonPiirissa.put("EN", "Cancelled, accepted another higher education study place");
    }

    public static Map<String, String> peruuntunutHyvaksyttyAlemmallaHakutoiveella = new HashMap<>();
    static {
        peruuntunutHyvaksyttyAlemmallaHakutoiveella.put("FI", "Peruuntunut, hyväksytty alemmalle hakutoiveelle");
        peruuntunutHyvaksyttyAlemmallaHakutoiveella.put("SV", "Annullerad, godkänd till ansökningsmål med lägre prioritet");
        peruuntunutHyvaksyttyAlemmallaHakutoiveella.put("EN", "Cancelled, accepted for a study place with lower preference");
    }

    private static final ConcurrentHashMap<String, SoftReference<Map<String, String>>> hyvaksyttyTayttojonoSaannolla = new ConcurrentHashMap<>();
    public static Map<String, String> hyvaksyttyTayttojonoSaannolla(String jono) {
        Map<String, String> k = hyvaksyttyTayttojonoSaannolla.compute(jono, (j, ref) -> {
            if (ref == null || ref.get() == null) {
                Map<String, String> m = new HashMap<>();
                m.put("FI", "Hyväksytty täyttöjonosäännöllä valintatapajonosta: " + j);
                m.put("SV", "Godkänd med köpåfyllningsregel från urvalsmetodskö: " + j);
                m.put("EN", "Accepted from selection method queue: " + j);
                return new SoftReference<>(m);
            } else {
                return ref;
            }
        }).get();
        if (k == null) {
            throw new RuntimeException("Tilan kuvauksen luonti epäonnistui, muisti lienee vähissä");
        }
        return k;
    }

    private static final ConcurrentHashMap<String, SoftReference<Map<String, String>>> hylattyHakijaryhmaanKuulumattomana = new ConcurrentHashMap<>();
    public static Map<String, String> hylattyHakijaryhmaanKuulumattomana(String hakijaryhma) {
        Map<String, String> k = hylattyHakijaryhmaanKuulumattomana.compute(hakijaryhma, (h, ref) -> {
            if (ref == null || ref.get() == null) {
                Map<String, String> m = new HashMap<>();
                m.put("FI", "Hylätty, ei kuulu hakijaryhmään: " + h);
                m.put("SV", "Annullerad, hör inte till gruppen för sökande: " + h);
                m.put("EN", "Cancelled, is not included in the applicant group: " + h);
                return new SoftReference<>(m);
            } else {
                return ref;
            }
        }).get();
        if (k == null) {
            throw new RuntimeException("Tilan kuvauksen luonti epäonnistui, muisti lienee vähissä");
        }
        return k;
    }
}
